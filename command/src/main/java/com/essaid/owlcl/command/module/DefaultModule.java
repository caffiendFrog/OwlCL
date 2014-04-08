package com.essaid.owlcl.command.module;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.slf4j.Logger;

import com.essaid.owlcl.command.module.builder.IModuleBuilder;
import com.essaid.owlcl.command.module.builder.ModuleBuilderManager;
import com.essaid.owlcl.command.module.config.IModuleConfig;
import com.essaid.owlcl.core.annotation.InjectLogger;
import com.essaid.owlcl.core.reasoner.IReasonerManager;
import com.essaid.owlcl.core.util.IInitializable;
import com.essaid.owlcl.core.util.ILoggerOwner;
import com.essaid.owlcl.core.util.IReportFactory;
import com.essaid.owlcl.core.util.OWLOntologyWrapper;
import com.essaid.owlcl.core.util.OwlclUtil;
import com.essaid.owlcl.core.util.Report;
import com.google.inject.Inject;

public class DefaultModule implements IModule, IInitializable, ILoggerOwner {

  @InjectLogger
  private Logger logger;

  @Inject
  private ModuleBuilderManager builderManager;

  @Inject
  IReasonerManager reasonerManager;

  @Inject
  IReportFactory reportFactory;

  private Report report;

  private File outputDirectory;

  private OWLOntologyManager genManager;
  private OWLDataFactory df;
  private OWLOntology generatedModule;
  private OWLOntology generatedModuleInferred;

  // private Map<IModule, Boolean> bothImports = new HashMap<IModule,
  // Boolean>();

  private Boolean addLegacy;
  private Boolean cleanLegacy;

  private IModuleConfig moduleConfiguration;

  private boolean disposed;

  private boolean generated;

  private boolean saved;

  private boolean finalUnclassified;

  private boolean finalClassified;

  public DefaultModule(IModuleConfig config, File outputDirectory) {
    this.moduleConfiguration = config;
    this.outputDirectory = outputDirectory;
    this.outputDirectory.mkdirs();
    this.genManager = OWLManager.createOWLOntologyManager();
    this.df = genManager.getOWLDataFactory();
  }

  @Override
  public IModuleConfig getModuleConfiguration() {
    return this.moduleConfiguration;
  }

  @Override
  public OWLOntology getBuildUnclassified() {
    if (generatedModule == null)
    {
      logger
          .info("Generating unclassified module for: " + getModuleConfiguration().getName());
      List<IModuleBuilder> builders = new ArrayList<IModuleBuilder>();
      generatedModule = OwlclUtil.createOntology(this.moduleConfiguration.getUnclassifiedIri(),
          genManager);
      for (String builderName : this.moduleConfiguration.getUnclassifiedBuilderNames())
      {
        report.info("Doing builder name: " + builderName);
        IModuleBuilder builder = builderManager.getBuilder(builderName, this);
        if (builder == null)
        {
          logger.error("No builder named {} was found for module {}", builderName,
              moduleConfiguration.getName());
          continue;
        }
        report.info("Found builder: " + builder.getClass().getName());
        builder.build(this, false);
        builders.add(builder);
      }

      // notify builders
      Iterator<IModuleBuilder> i = builders.iterator();
      while (i.hasNext())
      {
        IModuleBuilder builder = i.next();
        i.remove();
        builder.buildFinished(this);
      }

    }
    return new OWLOntologyWrapper(generatedModule);
  }

  @Override
  public OWLOntology getFinalUnclassified() {
    if (!finalUnclassified)
    {
      finalUnclassified = true;
      getBuildUnclassified();
      addAnnotationsUnclassified(moduleConfiguration.getIncludeOntology().getAnnotations(), null);
      addAxiomsUnclassified(OwlclUtil.getAxioms(moduleConfiguration.getIncludeOntology(), true),
          null);
      removeAxiomsUnclassified(OwlclUtil.getAxioms(moduleConfiguration.getExcludeOntology(), true),
          null);

      if (isAddLegacyUnclassified())
      {
        addAxiomsUnclassified(OwlclUtil.getAxioms(moduleConfiguration.getLegacyOntology(), true),
            null);
      }

    }
    return generatedModule;
  }

  @Override
  public OWLOntology getBuildClassified() {

    if (generatedModuleInferred == null)
    {
      logger.info("Generating classified module for: " + getModuleConfiguration().getName());
      List<IModuleBuilder> builders = new ArrayList<IModuleBuilder>();
      generatedModule = OwlclUtil.createOntology(
          this.moduleConfiguration.getClassifiedIri(), genManager);
      for (String builderName : this.moduleConfiguration.getClassifiedBuilderNames())
      {
        report.info("Doing builder name: " + builderName);
        IModuleBuilder builder = builderManager.getBuilder(builderName, this);
        if (builder == null)
        {
          logger.error("No builder named {} was found for module {}", builderName,
              moduleConfiguration.getName());
          continue;
        }
        report.info("Found builder: " + builder.getClass().getName());
        builder.build(this, false);
        builders.add(builder);
      }

      // notify builders
      Iterator<IModuleBuilder> i = builders.iterator();
      while (i.hasNext())
      {
        IModuleBuilder builder = i.next();
        i.remove();
        builder.buildFinished(this);
      }

      if (isAddLegacyClassified())
      {
        addAxiomsClassified(OwlclUtil.getAxioms(moduleConfiguration.getLegacyOntology(), true),
            null);
      }

    }

    return new OWLOntologyWrapper(generatedModuleInferred);
  }
  
  @Override
  public OWLOntology getFinalClassified() {
    if (!finalClassified)
    {
      finalClassified = true;
      getBuildClassified();
      addAnnotationsClassified(moduleConfiguration.getIncludeOntology().getAnnotations(), null);
      addAxiomsClassified(OwlclUtil.getAxioms(moduleConfiguration.getIncludeOntology(), true),
          null);
      removeAxiomsClassified(OwlclUtil.getAxioms(moduleConfiguration.getExcludeOntology(), true),
          null);

      if (isAddLegacyClassified())
      {
        addAxiomsClassified(OwlclUtil.getAxioms(moduleConfiguration.getLegacyOntology(), true),
            null);
      }

    }
    return generatedModuleInferred;
  }

  // ================================================================================
  // properties
  // ================================================================================

  private Boolean generate = null;

  @Override
  public void setUnclassified(Boolean generate) {
    this.generate = generate;
  }

  @Override
  public boolean isUnclassified() {
    if (this.generate == null)
    {
      return this.moduleConfiguration.isUnclassified();
    }
    return this.generate;
  }

  private Boolean generateInferred = null;

  @Override
  public void setClassified(Boolean generateInferred) {
    this.generateInferred = generateInferred;
  }

  @Override
  public boolean isClassified() {
    if (this.generateInferred == null)
    {
      return this.moduleConfiguration.isClassified();
    }
    return this.generateInferred;
  }

  // ================================================================================
  // module imports
  // ================================================================================

  private Map<IModule, Boolean> generateImports = new HashMap<IModule, Boolean>();
  private Map<IModule, Boolean> generateInferredImports = new HashMap<IModule, Boolean>();

  @Override
  public void importModuleIntoUnclassified(IModule module, Boolean inferred) {
    generateImports.put(module, inferred);
    // setGenerationType(module, inferred);
  }

  @Override
  public void importModuleIntoClassified(IModule module, Boolean inferred) {
    generateInferredImports.put(module, inferred);
    // setGenerationType(module, inferred);
  }

  private void setGenerationType(IModule module, Boolean inferred) {
    if (inferred == null)
    {
      module.setUnclassified(true);
      module.setClassified(true);
    } else
    {
      if (inferred)
      {
        module.setClassified(true);
        module.setUnclassified(false);
      } else
      {
        module.setClassified(false);
        module.setUnclassified(true);
      }
    }

  }

  // ================================================================================
  // legacy related
  // ================================================================================

  @Override
  public void cleanLegacyUnclassified() {
    // TODO
  }

  @Override
  public boolean isCleanLegacyUnclassified() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setCleanLegacyUnclassified(Boolean generate) {
    // TODO Auto-generated method stub

  }

  @Override
  public void cleanLegacyClassified() {
    // TODO
  }

  @Override
  public boolean isCleanLegacyClassified() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setCleanLegacyClassified(Boolean generate) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isAddLegacyClassified() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setAddLegacyClassified(Boolean generate) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isAddLegacyUnclassified() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setAddLegacyUnclassified(Boolean generate) {
    // TODO Auto-generated method stub

  }

  // ================================================================================
  // utility
  // ================================================================================

  @Override
  public void saveModule() {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveUnclassifiedModule() {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveUnclassifiedModule(Path fielOrDirectory) {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveClassifiedModule() {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveClassifiedModule(Path fielOrDirectory) {
    // TODO Auto-generated method stub

  }

  @Override
  public Report getReportClassified() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Report getReportUnclassified() {
    // TODO Auto-generated method stub
    return null;
  }

  // ================================================================================
  // for builders
  // ================================================================================

  @Override
  public void addAnnotationUnclassified(OWLAnnotation annotation, IModuleBuilder builder) {

    genManager.applyChange(new AddOntologyAnnotation(generatedModule, annotation));

  }

  @Override
  public void addAnnotationsUnclassified(Set<OWLAnnotation> annotations, IModuleBuilder builder) {
    for (OWLAnnotation a : annotations)
    {
      addAnnotationUnclassified(a, builder);
    }
  }

  @Override
  public void removeAnnotationUnclassified(OWLAnnotation annotation, IModuleBuilder builder) {
    genManager.applyChange(new RemoveOntologyAnnotation(generatedModule, annotation));

  }

  @Override
  public void removeAnnotationsUnclassified(Set<OWLAnnotation> annotations, IModuleBuilder builder) {
    for (OWLAnnotation a : annotations)
    {
      removeAnnotationUnclassified(a, builder);
    }

  }

  // inferred versions

  @Override
  public void addAnnotationClassified(OWLAnnotation annotation, IModuleBuilder builder) {
    genManager.applyChange(new AddOntologyAnnotation(generatedModuleInferred, annotation));

  }

  @Override
  public void addAnnotationsClassified(Set<OWLAnnotation> annotations, IModuleBuilder builder) {
    for (OWLAnnotation a : annotations)
    {
      addAnnotationClassified(a, builder);
    }

  }

  @Override
  public void removeAnnotationClassified(OWLAnnotation annotation, IModuleBuilder builder) {
    genManager.applyChange(new RemoveOntologyAnnotation(generatedModuleInferred, annotation));

  }

  @Override
  public void removeAnnotationsClassified(Set<OWLAnnotation> annotations, IModuleBuilder builder) {
    for (OWLAnnotation a : annotations)
    {
      removeAnnotationClassified(a, builder);
    }
  }

  @Override
  public void addAxiomUnclassified(OWLAxiom axiom, IModuleBuilder builder) {
    genManager.addAxiom(generatedModule, axiom);

  }

  @Override
  public void addAxiomsUnclassified(Set<OWLAxiom> axioms, IModuleBuilder builder) {
    for (OWLAxiom a : axioms)
    {
      addAxiomUnclassified(a, builder);
    }

  }

  @Override
  public void removeAxiomUnclassified(OWLAxiom axiom, IModuleBuilder builder) {
    genManager.removeAxiom(generatedModule, axiom);

  }

  @Override
  public void removeAxiomsUnclassified(Set<OWLAxiom> axioms, IModuleBuilder builder) {
    for (OWLAxiom a : axioms)
    {
      removeAxiomUnclassified(a, builder);
    }

  }

  @Override
  public void addAxiomClassified(OWLAxiom axiom, IModuleBuilder builder) {
    genManager.addAxiom(generatedModuleInferred, axiom);

  }

  @Override
  public void addAxiomsClassified(Set<OWLAxiom> axioms, IModuleBuilder builder) {
    for (OWLAxiom a : axioms)
    {
      addAxiomClassified(a, builder);
    }

  }

  @Override
  public void removeAxiomClassified(OWLAxiom axiom, IModuleBuilder builder) {
    genManager.removeAxiom(generatedModuleInferred, axiom);

  }

  @Override
  public void removeAxiomsClassified(Set<OWLAxiom> axioms, IModuleBuilder builder) {
    for (OWLAxiom a : axioms)
    {
      removeAxiomClassified(a, builder);
    }

  }

  // ================================================================================
  // not reviewed
  // ================================================================================

  public OWLOntologyManager getGenerationManager() {
    return this.genManager;
  }
  

  private void addImport(String moduleName, IRI iri, OWLOntology ontology) {
    OWLImportsDeclaration id = df.getOWLImportsDeclaration(iri);
    AddImport i = new AddImport(ontology, id);
    logger.info("Adding generated module import for module: " + moduleName
        + " imported into generated: " + getModuleConfiguration().getName());
    genManager.applyChange(i);
  }


  @Override
  public void dispose() {
    if (this.disposed)
    {
      return;
    }
    this.disposed = true;
    // TODO: review

  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  @Override
  public int hashCode() {
    return this.moduleConfiguration.getName().hashCode();
  }

  public void setReport(Report report) {
    this.report = report;
  }
  

  @Override
  public OWLDataFactory getDataFactory() {
    return df;
  }

  @Override
  public boolean equals(Object obj) {

    // TODO: should it be possible to have multiple modules with same name
    // during runtime? This equality is probably too strong.
    if (this == obj)
    {
      return true;
    }
    if (obj instanceof IModule)
    {
      return this.moduleConfiguration.getName().equals(
          ((IModule) obj).getModuleConfiguration().getName());
    }
    return false;
  }

//  @Override
//  public void setAddLegacy(Boolean generate) {
//    addLegacy = generate;
//
//  }
//
//  @Override
//  public void setCleanLegacy(Boolean generate) {
//    cleanLegacy = generate;
//
//  }

  @Override
  public void initialize() {
    this.report = reportFactory.createReport(
        "GenerateModule-" + this.moduleConfiguration.getName(), outputDirectory, this);
  }

  @Override
  public Logger getLogger() {
    return this.logger;
  }

  
  //
  // @Override
  // public void generateModule() {
  // if (generated)
  // {
  // return;
  // }
  // generated = true;
  //
  // List<IModuleBuilder> buildersInferred = new ArrayList<IModuleBuilder>();
  //
  // Set<OWLAxiom> includeAxioms = OwlclUtil.getAxioms(
  // this.moduleConfiguration.getIncludeOntology(), true);
  // Set<OWLAxiom> excludeAxioms = OwlclUtil.getAxioms(
  // this.moduleConfiguration.getExcludeOntology(), true);
  //
  // if (isUnclassified())
  // {
  // report.info("Is generate is true.");
  //
  // addAxiomsUnclassified(includeAxioms);
  // removeAxiomsUnclassified(excludeAxioms);
  //
  // if (isAddLegacy())
  // {
  // addAxiomsUnclassified(OwlclUtil.getAxioms(this.moduleConfiguration.getLegacyOntology(),
  // true));
  // }
  // }
  //
  // if (isClassified())
  // {
  // report.info("Is generate inferred is true.");
  // generatedModuleInferred = OwlclUtil.createOntology(
  // this.moduleConfiguration.getGeneratedInferredModuleIri(), genManager);
  // for (String builderName :
  // this.moduleConfiguration.getBuildersInferredNames())
  // {
  // report.info("Doing builder name: " + builderName);
  // IModuleBuilder builder = builderManager.getBuilder(builderName, this);
  // if (builder == null)
  // {
  // logger.error("No inferred builder named {} was found for module {}",
  // builderName,
  // moduleConfiguration.getModuleName());
  // continue;
  // }
  // report.info("Found builder: " + builder.getClass().getName());
  // builder.build(this, true);
  // buildersInferred.add(builder);
  // }
  // addAxiomsClassified(includeAxioms);
  // removeAxiomsClassified(excludeAxioms);
  //
  // if (isAddLegacy())
  // {
  //
  // addAxiomsClassified(OwlclUtil.getAxioms(this.moduleConfiguration.getLegacyOntology(),
  // true));
  // }
  // }
  //
  // // notify builders, finished
  // Iterator<IModuleBuilder> i = builders.iterator();
  // while (i.hasNext())
  // {
  // IModuleBuilder builder = i.next();
  // i.remove();
  // builder.buildFinished(this);
  // }
  //
  // i = buildersInferred.iterator();
  // while (i.hasNext())
  // {
  // IModuleBuilder builder = i.next();
  // i.remove();
  // builder.buildFinished(this);
  // }
  //
  // // clean legacy
  // if (isCleanLegacy())
  // {
  // OWLOntology legacyOntology = this.moduleConfiguration.getLegacyOntology();
  // if (legacyOntology != null)
  // {
  // Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
  // if (generatedModule != null)
  // {
  // axioms = generatedModule.getAxioms();
  // }
  // if (generatedModuleInferred != null)
  // {
  // axioms.addAll(generatedModuleInferred.getAxioms());
  // }
  // axioms.addAll(OwlclUtil.getAxioms(this.moduleConfiguration.getExcludeOntology(),
  // true));
  //
  // Set<OWLAxiom> removedAxioms = new HashSet<OWLAxiom>();
  //
  // for (OWLOntology o : legacyOntology.getImportsClosure())
  // {
  // List<OWLOntologyChange> changes = o.getOWLOntologyManager().removeAxioms(o,
  // axioms);
  // logger.info("Cleaned legacy ontology: " + o.getOntologyID() +
  // ", change count: "
  // + changes.size());
  //
  // for (OWLOntologyChange change : changes)
  // {
  // removedAxioms.add(change.getAxiom());
  // }
  // }
  //
  // OWLOntology legacyRemovedOntology =
  // this.moduleConfiguration.getLegacyRemovedOntology();
  // if (legacyRemovedOntology != null)
  // {
  // legacyRemovedOntology.getOWLOntologyManager().addAxioms(legacyRemovedOntology,
  // removedAxioms);
  // }
  // }
  // }
  //
  // Set<IModule> imports = new HashSet<IModule>();
  // imports.addAll(generateImports.keySet());
  // imports.addAll(generateInferredImports.keySet());
  // imports.addAll(bothImports.keySet());
  //
  // for (IModule module : imports)
  // {
  // module.generateModule();
  // }
  //
  // report.finish();
  // }
//
//  @Override
//  public boolean isAddLegacy() {
//    if (addLegacy == null)
//    {
//      return this.moduleConfiguration.isAddLegacy();
//    }
//    return addLegacy;
//  }
//
//  @Override
//  public boolean isCleanLegacy() {
//    if (cleanLegacy == null)
//    {
//      return this.moduleConfiguration.isCleanLegacy();
//    }
//    return cleanLegacy;
//  }

//  @Override
//  public void saveGeneratedModule() {
//    if (saved)
//    {
//      return;
//    }
//    saved = true;
//
//    Set<IModule> allImportedModules = new HashSet<IModule>();
//
//    if (isUnclassified())
//    {
//      report.info("Saving generate module.");
//      allImportedModules.addAll(bothImports.keySet());
//      allImportedModules.addAll(generateImports.keySet());
//
//      for (Entry<IModule, Boolean> entry : bothImports.entrySet())
//      {
//        if (entry.getValue() == null || entry.getValue() == false)
//        {
//          // import matching type
//          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
//              .getModuleConfiguration().getGeneratedModuleIri(), generatedModule);
//        } else if (entry.getValue() == true)
//        {
//          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
//              .getModuleConfiguration().getGeneratedInferredModuleIri(), generatedModule);
//        }
//      }
//
//      for (Entry<IModule, Boolean> entry : generateImports.entrySet())
//      {
//        if (entry.getValue() == false)
//        {
//          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
//              .getModuleConfiguration().getGeneratedModuleIri(), generatedModule);
//        } else if (entry.getValue() == true)
//        {
//          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
//              .getModuleConfiguration().getGeneratedInferredModuleIri(), generatedModule);
//        }
//      }
//
//      try
//      {
//        logger.info("Saving module: " + getModuleConfiguration().getModuleName()
//            + " into ontology: " + generatedModule.getOntologyID() + " in  directory: "
//            + getOutputDirectory() + " and file: "
//            + this.moduleConfiguration.getGenerateModuleFileName());
//
//        genManager.saveOntology(generatedModule, new FileOutputStream(new File(
//            getOutputDirectory(), this.moduleConfiguration.getGenerateModuleFileName())));
//      } catch (OWLOntologyStorageException | FileNotFoundException e)
//      {
//        throw new RuntimeException("Failed to save module generated "
//            + getModuleConfiguration().getModuleName() + " with file name "
//            + this.moduleConfiguration.getGenerateModuleFileName(), e);
//      }
//
//    }
//
//    if (isClassified())
//    {
//      allImportedModules.addAll(bothImports.keySet());
//      allImportedModules.addAll(generateInferredImports.keySet());
//
//      for (Entry<IModule, Boolean> entry : bothImports.entrySet())
//      {
//        if (entry.getValue() == null || entry.getValue() == true)
//        {
//          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
//              .getModuleConfiguration().getGeneratedInferredModuleIri(), generatedModuleInferred);
//        } else
//        {
//          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
//              .getModuleConfiguration().getGeneratedModuleIri(), generatedModuleInferred);
//        }
//      }
//
//      for (Entry<IModule, Boolean> entry : generateInferredImports.entrySet())
//      {
//        if (entry.getValue() == true)
//        {
//          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
//              .getModuleConfiguration().getGeneratedInferredModuleIri(), generatedModuleInferred);
//        } else
//        {
//          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
//              .getModuleConfiguration().getGeneratedModuleIri(), generatedModuleInferred);
//        }
//      }
//
//      try
//      {
//        logger.info("Saving module: " + getModuleConfiguration().getModuleName()
//            + " into ontology: " + generatedModuleInferred.getOntologyID() + " in  directory: "
//            + getOutputDirectory() + " and file: "
//            + this.moduleConfiguration.getGenerateInferredModuleFileName());
//
//        genManager.saveOntology(generatedModuleInferred, new FileOutputStream(new File(
//            getOutputDirectory(), this.moduleConfiguration.getGenerateInferredModuleFileName())));
//      } catch (FileNotFoundException | OWLOntologyStorageException e)
//      {
//        throw new RuntimeException("Failed to save module inferred "
//            + getModuleConfiguration().getModuleName() + " with file name "
//            + this.moduleConfiguration.getGenerateInferredModuleFileName(), e);
//      }
//
//    }
//
//    for (IModule module : allImportedModules)
//    {
//      module.saveGeneratedModule();
//    }
//
//  }



  // ================================================================================
  // Builder related
  // ================================================================================

}