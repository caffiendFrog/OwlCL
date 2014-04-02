package com.essaid.owlcl.command.module;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

  private Map<IModule, Boolean> generateImports = new HashMap<IModule, Boolean>();
  private Map<IModule, Boolean> generateInferredImports = new HashMap<IModule, Boolean>();
  private Map<IModule, Boolean> bothImports = new HashMap<IModule, Boolean>();

  private Boolean addLegacy;
  private Boolean cleanLegacy;

  private IModuleConfig moduleConfiguration;

  private boolean disposed;

  private boolean generated;

  private boolean saved;

  public DefaultModule(IModuleConfig config, File outputDirectory) {
    this.moduleConfiguration = config;
    this.outputDirectory = outputDirectory;
    this.genManager = OWLManager.createOWLOntologyManager();
    this.df = genManager.getOWLDataFactory();
  }

  // ================================================================================
  // generated module imports
  // ================================================================================

  @Override
  public void importModuleIntoGenerated(IModule module, Boolean inferred) {
    generateImports.put(module, inferred);
    setGenerationType(module, inferred);
  }

  @Override
  public void importModuleIntoGeneratedInferred(IModule module, Boolean inferred) {
    generateInferredImports.put(module, inferred);
    setGenerationType(module, inferred);
  }

  @Override
  public void importModuleIntoBoth(IModule module, Boolean inferred) {
    bothImports.put(module, inferred);
    setGenerationType(module, inferred);
  }

  private void setGenerationType(IModule module, Boolean inferred) {
    if (inferred == null)
    {
      module.setGenerate(true);
      module.setGenerateInferred(true);
    } else
    {
      if (inferred)
      {
        module.setGenerateInferred(true);
        module.setGenerate(false);
      } else
      {
        module.setGenerateInferred(false);
        module.setGenerate(true);
      }
    }

  }

  public OWLOntologyManager getGenerationManager() {
    return this.genManager;
  }

  @Override
  public IModuleConfig getModuleConfiguration() {
    return this.moduleConfiguration;
  }

  //
  // @Override
  // public IRI getModuleIri() {
  // return this.moduleConfiguration.getGeneratedModuleIri();
  // }
  //
  // @Override
  // public IRI getModuleIriInferred() {
  // return generatedModuleInferredIri;
  // }
  //
  // public File getModuleDirectory() {
  // return moduleDirectory;
  // }
  //
  // @Override
  // public String getName() {
  // return name;
  // }
  //
  // @Override
  // public OWLReasoner getSourceReasoned() {
  // return sourceReasoner;
  // }

  @Override
  public void generateModule() {
    if (generated)
    {
      return;
    }
    generated = true;

    logger.info("Generating module: " + getModuleConfiguration().getModuleName());

    List<IModuleBuilder> builders = new ArrayList<IModuleBuilder>();
    List<IModuleBuilder> buildersInferred = new ArrayList<IModuleBuilder>();

    Set<OWLAxiom> includeAxioms = OwlclUtil.getAxioms(
        this.moduleConfiguration.getIncludeOntology(), true);
    Set<OWLAxiom> excludeAxioms = OwlclUtil.getAxioms(
        this.moduleConfiguration.getExcludeOntology(), true);

    if (isGenerate())
    {
      report.info("Is generate is true.");
      generatedModule = OwlclUtil.createOntology(this.moduleConfiguration.getGeneratedModuleIri(),
          genManager);
      for (String builderName : this.moduleConfiguration.getBuildersNames())
      {
        report.info("Doing builder name: " + builderName);
        IModuleBuilder builder = builderManager.getBuilder(builderName, this);
        if (builder == null)
        {
          logger.error("No builder named {} was found for module {}", builderName,
              moduleConfiguration.getModuleName());
          continue;
        }
        report.info("Found builder: " + builder.getClass().getName());
        builder.build(this, false);
        builders.add(builder);
      }
      addAxioms(includeAxioms);
      removeAxioms(excludeAxioms);

      if (isAddLegacy())
      {
        addAxioms(OwlclUtil.getAxioms(this.moduleConfiguration.getLegacyOntology(), true));
      }
    }

    if (isGenerateInferred())
    {
      report.info("Is generate inferred is true.");
      generatedModuleInferred = OwlclUtil.createOntology(
          this.moduleConfiguration.getGeneratedInferredModuleIri(), genManager);
      for (String builderName : this.moduleConfiguration.getBuildersInferredNames())
      {
        report.info("Doing builder name: " + builderName);
        IModuleBuilder builder = builderManager.getBuilder(builderName, this);
        if (builder == null)
        {
          logger.error("No inferred builder named {} was found for module {}", builderName,
              moduleConfiguration.getModuleName());
          continue;
        }
        report.info("Found builder: " + builder.getClass().getName());
        builder.build(this, true);
        buildersInferred.add(builder);
      }
      addAxiomsInferred(includeAxioms);
      removeAxiomsInferred(excludeAxioms);

      if (isAddLegacy())
      {

        addAxiomsInferred(OwlclUtil.getAxioms(this.moduleConfiguration.getLegacyOntology(), true));
      }
    }

    // notify builders, finished
    Iterator<IModuleBuilder> i = builders.iterator();
    while (i.hasNext())
    {
      IModuleBuilder builder = i.next();
      i.remove();
      builder.buildFinished(this);
    }

    i = buildersInferred.iterator();
    while (i.hasNext())
    {
      IModuleBuilder builder = i.next();
      i.remove();
      builder.buildFinished(this);
    }

    // clean legacy
    if (isCleanLegacy())
    {
      OWLOntology legacyOntology = this.moduleConfiguration.getLegacyOntology();
      if (legacyOntology != null)
      {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        if (generatedModule != null)
        {
          axioms = generatedModule.getAxioms();
        }
        if (generatedModuleInferred != null)
        {
          axioms.addAll(generatedModuleInferred.getAxioms());
        }
        axioms.addAll(OwlclUtil.getAxioms(this.moduleConfiguration.getExcludeOntology(), true));

        Set<OWLAxiom> removedAxioms = new HashSet<OWLAxiom>();

        for (OWLOntology o : legacyOntology.getImportsClosure())
        {
          List<OWLOntologyChange> changes = o.getOWLOntologyManager().removeAxioms(o, axioms);
          logger.info("Cleaned legacy ontology: " + o.getOntologyID() + ", change count: "
              + changes.size());

          for (OWLOntologyChange change : changes)
          {
            removedAxioms.add(change.getAxiom());
          }
        }

        OWLOntology legacyRemovedOntology = this.moduleConfiguration.getLegacyRemovedOntology();
        if (legacyRemovedOntology != null)
        {
          legacyRemovedOntology.getOWLOntologyManager().addAxioms(legacyRemovedOntology,
              removedAxioms);
        }
      }
    }

    Set<IModule> imports = new HashSet<IModule>();
    imports.addAll(generateImports.keySet());
    imports.addAll(generateInferredImports.keySet());
    imports.addAll(bothImports.keySet());

    for (IModule module : imports)
    {
      module.generateModule();
    }

    report.finish();
  }

  @Override
  public boolean isAddLegacy() {
    if (addLegacy == null)
    {
      return this.moduleConfiguration.isAddLegacy();
    }
    return addLegacy;
  }

  @Override
  public boolean isCleanLegacy() {
    if (cleanLegacy == null)
    {
      return this.moduleConfiguration.isCleanLegacy();
    }
    return cleanLegacy;
  }

  @Override
  public void saveGeneratedModule() {
    if (saved)
    {
      return;
    }
    saved = true;

    // Set<Entry<Module, Boolean>> mergedImports = new HashSet<>();
    Set<IModule> allImportedModules = new HashSet<IModule>();

    if (isGenerate())
    {
      report.info("Saving generate module.");
      allImportedModules.addAll(bothImports.keySet());
      allImportedModules.addAll(generateImports.keySet());

      for (Entry<IModule, Boolean> entry : bothImports.entrySet())
      {
        if (entry.getValue() == null || entry.getValue() == false)
        {
          // import matching type
          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
              .getModuleConfiguration().getGeneratedModuleIri(), generatedModule);
        } else if (entry.getValue() == true)
        {
          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
              .getModuleConfiguration().getGeneratedInferredModuleIri(), generatedModule);
        }
      }

      for (Entry<IModule, Boolean> entry : generateImports.entrySet())
      {
        if (entry.getValue() == false)
        {
          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
              .getModuleConfiguration().getGeneratedModuleIri(), generatedModule);
        } else if (entry.getValue() == true)
        {
          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
              .getModuleConfiguration().getGeneratedInferredModuleIri(), generatedModule);
        }
      }

      try
      {
        logger.info("Saving module: " + getModuleConfiguration().getModuleName()
            + " into ontology: " + generatedModule.getOntologyID() + " in  directory: "
            + getOutputDirectory() + " and file: "
            + this.moduleConfiguration.getGenerateModuleFileName());

        genManager.saveOntology(generatedModule, new FileOutputStream(new File(
            getOutputDirectory(), this.moduleConfiguration.getGenerateModuleFileName())));
      } catch (OWLOntologyStorageException | FileNotFoundException e)
      {
        throw new RuntimeException("Failed to save module generated "
            + getModuleConfiguration().getModuleName() + " with file name "
            + this.moduleConfiguration.getGenerateModuleFileName(), e);
      }

    }

    if (isGenerateInferred())
    {
      allImportedModules.addAll(bothImports.keySet());
      allImportedModules.addAll(generateInferredImports.keySet());

      for (Entry<IModule, Boolean> entry : bothImports.entrySet())
      {
        if (entry.getValue() == null || entry.getValue() == true)
        {
          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
              .getModuleConfiguration().getGeneratedInferredModuleIri(), generatedModuleInferred);
        } else
        {
          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
              .getModuleConfiguration().getGeneratedModuleIri(), generatedModuleInferred);
        }
      }

      for (Entry<IModule, Boolean> entry : generateInferredImports.entrySet())
      {
        if (entry.getValue() == true)
        {
          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
              .getModuleConfiguration().getGeneratedInferredModuleIri(), generatedModuleInferred);
        } else
        {
          addImport(entry.getKey().getModuleConfiguration().getModuleName(), entry.getKey()
              .getModuleConfiguration().getGeneratedModuleIri(), generatedModuleInferred);
        }
      }

      try
      {
        logger.info("Saving module: " + getModuleConfiguration().getModuleName()
            + " into ontology: " + generatedModuleInferred.getOntologyID() + " in  directory: "
            + getOutputDirectory() + " and file: "
            + this.moduleConfiguration.getGenerateInferredModuleFileName());

        genManager.saveOntology(generatedModuleInferred, new FileOutputStream(new File(
            getOutputDirectory(), this.moduleConfiguration.getGenerateInferredModuleFileName())));
      } catch (FileNotFoundException | OWLOntologyStorageException e)
      {
        throw new RuntimeException("Failed to save module inferred "
            + getModuleConfiguration().getModuleName() + " with file name "
            + this.moduleConfiguration.getGenerateInferredModuleFileName(), e);
      }

    }

    for (IModule module : allImportedModules)
    {
      module.saveGeneratedModule();
    }

  }

  private void addImport(String moduleName, IRI iri, OWLOntology ontology) {
    OWLImportsDeclaration id = df.getOWLImportsDeclaration(iri);
    AddImport i = new AddImport(ontology, id);
    logger.info("Adding generated module import for module: " + moduleName
        + " imported into generated: " + getModuleConfiguration().getModuleName());
    genManager.applyChange(i);
  }

  // @Override
  // public void saveModuleConfiguration() {
  //
  // // TODO: do this only for changed ontologies ???
  // try
  // {
  // moduleManager.saveOntology(configurationOntology);
  //
  // moduleManager.saveOntology(includeOntology);
  // moduleManager.saveOntology(excludeOntology);
  // if (legacySupport)
  // {
  // if (cleanLegacy)
  // {
  // moduleManager.saveOntology(legacyOntology);
  // moduleManager.saveOntology(legacyRemovedOntology);
  // }
  // }
  // } catch (OWLOntologyStorageException e)
  // {
  // throw new RuntimeException("Failed to save module configuration: " +
  // getName(), e);
  // }
  //
  // for (IModule module : generateImports.keySet())
  // {
  // module.saveModuleConfiguration();
  // }
  //
  // for (IModule module : generateInferredImports.keySet())
  // {
  // module.saveModuleConfiguration();
  // }
  // }

  @Override
  public void dispose() {
    if (this.disposed)
    {
      return;
    }
    this.disposed = true;
    // TODO: review

  }

  private Boolean generate = null;

  @Override
  public void setGenerate(Boolean generate) {
    this.generate = generate;
  }

  @Override
  public boolean isGenerate() {
    if (this.generate == null)
    {
      return this.moduleConfiguration.isGenerate();
    }
    return this.generate;
  }

  private Boolean generateInferred = null;

  @Override
  public void setGenerateInferred(Boolean generateInferred) {
    this.generateInferred = generateInferred;
  }

  @Override
  public boolean isGenerateInferred() {
    if (this.generateInferred == null)
    {
      return this.moduleConfiguration.isGenerateInferred();
    }
    return this.generateInferred;
  }

  @Override
  public OWLOntology getGeneratedModule() {
    return new OWLOntologyWrapper(generatedModule);
  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  @Override
  public com.essaid.owlcl.core.util.Report getReport() {
    return report;
  }

  @Override
  public int hashCode() {
    return this.moduleConfiguration.getModuleName().hashCode();
  }

  public void setReport(Report report) {
    this.report = report;
  }

  @Override
  public OWLOntology getGeneratedModuleInferred() {
    return new OWLOntologyWrapper(generatedModuleInferred);
  }

  // ================================================================================
  // Builder related
  // ================================================================================

  // generated version
  @Override
  public void addModuleAnnotation(OWLAnnotation annotation) {

    genManager.applyChange(new AddOntologyAnnotation(generatedModule, annotation));

  }

  @Override
  public void addModuleAnnotations(Set<OWLAnnotation> annotations) {
    for (OWLAnnotation a : annotations)
    {
      addModuleAnnotation(a);
    }
  }

  @Override
  public void removeModuleAnnotation(OWLAnnotation annotation) {
    genManager.applyChange(new RemoveOntologyAnnotation(generatedModule, annotation));

  }

  @Override
  public void removeModuleAnnotations(Set<OWLAnnotation> annotations) {
    for (OWLAnnotation a : annotations)
    {
      removeModuleAnnotation(a);
    }

  }

  // inferred versions

  @Override
  public void addModuleAnnotationInferred(OWLAnnotation annotation) {
    genManager.applyChange(new AddOntologyAnnotation(generatedModuleInferred, annotation));

  }

  @Override
  public void addModuleAnnotationsInferred(Set<OWLAnnotation> annotations) {
    for (OWLAnnotation a : annotations)
    {
      addModuleAnnotationInferred(a);
    }

  }

  @Override
  public void removeModuleAnnotationInferred(OWLAnnotation annotation) {
    genManager.applyChange(new RemoveOntologyAnnotation(generatedModuleInferred, annotation));

  }

  @Override
  public void removeModuleAnnotationsInferred(Set<OWLAnnotation> annotations) {
    for (OWLAnnotation a : annotations)
    {
      removeModuleAnnotationInferred(a);
    }
  }

  @Override
  public void addAxiom(OWLAxiom axiom) {
    genManager.addAxiom(generatedModule, axiom);

  }

  @Override
  public void addAxioms(Set<OWLAxiom> axioms) {
    for (OWLAxiom a : axioms)
    {
      addAxiom(a);
    }

  }

  @Override
  public void removeAxiom(OWLAxiom axiom) {
    genManager.removeAxiom(generatedModule, axiom);

  }

  @Override
  public void removeAxioms(Set<OWLAxiom> axioms) {
    for (OWLAxiom a : axioms)
    {
      removeAxiom(a);
    }

  }

  @Override
  public void addAxiomInferred(OWLAxiom axiom) {
    genManager.addAxiom(generatedModuleInferred, axiom);

  }

  @Override
  public void addAxiomsInferred(Set<OWLAxiom> axioms) {
    for (OWLAxiom a : axioms)
    {
      addAxiomInferred(a);
    }

  }

  @Override
  public void removeAxiomInferred(OWLAxiom axiom) {
    genManager.removeAxiom(generatedModuleInferred, axiom);

  }

  @Override
  public void removeAxiomsInferred(Set<OWLAxiom> axioms) {
    for (OWLAxiom a : axioms)
    {
      removeAxiomInferred(a);
    }

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
      return this.moduleConfiguration.getModuleName().equals(
          ((IModule) obj).getModuleConfiguration().getModuleName());
    }
    return false;
  }

  @Override
  public void setAddLegacy(Boolean generate) {
    addLegacy = generate;

  }

  @Override
  public void setCleanLegacy(Boolean generate) {
    cleanLegacy = generate;

  }

  @Override
  public void initialize() {
    this.report = reportFactory.createReport(
        "GenerateModule-" + this.moduleConfiguration.getModuleName(), outputDirectory, this);
  }

  @Override
  public Logger getLogger() {
    return this.logger;
  }

}