package com.essaid.owlcl.command.module.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import com.essaid.owlcl.command.module.ModuleVocab;
import com.essaid.owlcl.command.module.Owlcl;
import com.essaid.owlcl.command.module.Util;
import com.essaid.owlcl.core.util.OntologyLoadingDescriptor;
import com.essaid.owlcl.core.util.OwlclUtil;

public class ModuleConfigurationV1 extends AbstractModuleConfiguration {

  public static AbstractModuleConfiguration getExistingInstance(Path directoryPath,
      OWLOntologyManager configManager, OWLOntologyManager sourceManager,
      OWLOntology sourceOntology, OWLReasoner sourceReasoner) {
    if (directoryPath == null || !directoryPath.toFile().isDirectory() || configManager == null
        || sourceManager == null)
    {
      throw new IllegalStateException("Existing module configuration with null or "
          + "non-existing directory or manager: " + directoryPath);
    }

    File directory = directoryPath.toFile();
    int version = Util.getModuleVersion(directory);
    if (version != 1)
    {
      throw new IllegalStateException("Loading V-1 configuration form directory with version: "
          + version);
    }

    AutoIRIMapper mapper = new AutoIRIMapper(directory, false);
    IRI configIri = null;
    for (IRI iri : mapper.getOntologyIRIs())
    {
      if (iri.toString().endsWith(Owlcl.MODULE_CONFIGURATION_IRI_SUFFIX))
      {
        configIri = iri;
        break;
      }
    }
    if (configIri == null)
    {
      throw new IllegalStateException("Failed to find cofiguration ontology in direcotry: "
          + directoryPath.toAbsolutePath());
    }

    int index = configIri.toString().lastIndexOf("/") + 1;
    String prefix = configIri.toString().substring(0, index);
    String name = configIri.toString().substring(index);
    name = name.substring(0, name.indexOf(Owlcl.MODULE_CONFIGURATION_IRI_SUFFIX));

    File configFile = new File(directory, name + Owlcl.MODULE_CONFIGURATION_IRI_SUFFIX);

    OWLOntologyLoaderConfiguration lc = new OWLOntologyLoaderConfiguration();
    lc.addIgnoredImport(IRI.create(prefix + name + Owlcl.MODULE_SOURCE_IRI_SUFFIX));

    OWLOntologyDocumentSource ds = new FileDocumentSource(configFile);

    OWLOntology configOntology = null;
    try
    {
      configOntology = configManager.loadOntologyFromOntologyDocument(ds, lc);
    } catch (OWLOntologyCreationException e)
    {
      throw new RuntimeException("Failed to load configuration file.", e);
    }

    AbstractModuleConfiguration config = new ModuleConfigurationV1(name, prefix, directoryPath,
        configOntology);

    config.sourceManager = sourceManager;
    config.sourceOntology = sourceOntology;
    config.sourceReasoner = sourceReasoner;

    return config;
  }

  public static AbstractModuleConfiguration getNewInstance(String name, String iriPrefix,
      Path directoryPath, OWLOntologyManager configManager, OWLOntologyManager sourceManager,
      OWLOntology sourceOntology, OWLReasoner sourceReasoner) {

    if (name == null || iriPrefix == null || directoryPath == null || configManager == null
        || sourceManager == null)
    {
      throw new IllegalStateException("New module configuration with null arguments.");
    }

    File directory = directoryPath.toFile();
    if (directory.exists())
    {
      if (!directory.isDirectory())
      {
        throw new IllegalStateException("New module configuration direcotory is a file.");
      }

      if (directory.list().length > 0)
      {
        throw new IllegalStateException("New module configuration directory is not empty.");
      }

    } else
    {
      directory.mkdirs();
    }

    OWLOntology configOntology = OwlclUtil.createOntology(
        IRI.create(iriPrefix + name + Owlcl.MODULE_CONFIGURATION_IRI_SUFFIX), configManager);
    configManager.setOntologyDocumentIRI(configOntology,
        IRI.create(new File(directory, name + Owlcl.MODULE_CONFIGURATION_IRI_SUFFIX)));
    File versionFile = new File(directory, "V-1");
    try
    {
      versionFile.createNewFile();
    } catch (IOException e)
    {
      throw new RuntimeException("Failed to create version file in:"
          + versionFile.getAbsolutePath(), e);
    }

    saveOntology(configOntology);

    AbstractModuleConfiguration config = new ModuleConfigurationV1(name, iriPrefix, directoryPath,
        configOntology);

    config.sourceManager = sourceManager;
    config.sourceOntology = sourceOntology;
    config.sourceReasoner = sourceReasoner;

    return config;
  }

  // ================================================================================
  //
  // ================================================================================

  private ModuleConfigurationV1(String name, String iriPrefix, Path moduleDirectory,
      OWLOntology configurationOntology) {
    super(name, iriPrefix, moduleDirectory, configurationOntology);
  }

  // ================================================================================
  //
  // ================================================================================
  
  @Override
  public void load() {
    this.topFile = new File(getDirectory().toFile(), getName() + Owlcl.MODULE_TOP_IRI_SUFFIX);
    if (topFile.isFile())
    {
      OWLOntologyLoaderConfiguration lc;
    }
  
  }

  @Override
  public void update() {
    // TODO Auto-generated method stub
  
  }

  @Override
  public void loadAndUpdate() {
  
    // make sure the config manager has the directory as a mapping
    configMan.addIRIMapper(new AutoIRIMapper(getDirectory().toFile(), false));
  
    File file = null;
    OWLOntology o = null;
  
    // ================================================================================
    // Top check
    // ================================================================================
    if (update)
    {
  
      file = new File(getDirectory().toFile(), getName() + Owlcl.MODULE_TOP_IRI_SUFFIX);
      if (!file.exists())
      {
        o = OwlclUtil.createOntology(getTopIri(), configMan);
        configMan.setOntologyDocumentIRI(o, IRI.create(file));
  
      } else
      {
        o = OwlclUtil.loadOntologyIgnoreImports(file, configMan);
      }
      configMan.applyChange(new AddImport(o, configDf
          .getOWLImportsDeclaration(getConfigurationIri())));
      configMan.applyChange(new AddImport(o, configDf.getOWLImportsDeclaration(getIncludeIri())));
      configMan.applyChange(new AddImport(o, configDf.getOWLImportsDeclaration(getExcludeIri())));
      configMan.applyChange(new AddImport(o, configDf.getOWLImportsDeclaration(getLegacyIri())));
      configMan.applyChange(new AddImport(o, configDf
          .getOWLImportsDeclaration(getLegacyRemovedIri())));
  
      if (changedOntologies.contains(o))
      {
        changedOntologies.remove(o);
        saveOntology(o);
        configMan.removeOntology(o);
      }
    }
    // ================================================================================
    // Configuration check
    // ================================================================================
    file = new File(getDirectory().toFile(), getName() + Owlcl.MODULE_CONFIGURATION_IRI_SUFFIX);
    if (!file.exists())
    {
      throw new IllegalStateException(
          "Module configuration file missing. This should not be possible");
  
    } else
    {
      o = OwlclUtil.getOrLoadOntology(getConfigurationIri(), configMan);
    }
    // the two imports
    configMan.applyChange(new AddImport(o, configDf
        .getOWLImportsDeclaration(getSourceConfigurationIri())));
    configMan.applyChange(new AddImport(o, configDf.getOWLImportsDeclaration(Owlcl.ISF_TOOLS_IRI)));
  
    // annotations
    checkUnclassifiedIri(o, getUnclassifiedIri(), logger);
    String value = getAnnotations(o, ModuleVocab.module_unclassified_iri.getAP()).iterator().next()
        .getValue().toString();
    this.unclassifiedIri = IRI.create(value);
  
    checkClassifiedIri(o, getClassifiedIri(), logger);
    value = getAnnotations(o, ModuleVocab.module_classified_iri.getAP()).iterator().next()
        .getValue().toString();
    this.classifiedIri = IRI.create(value);
  
    checkUnclassifiedFilename(o, getName() + Owlcl.MODULE_UNCLASSIFIED_SUFFIX, logger);
    value = getAnnotations(o, ModuleVocab.module_unclassified_filename.getAP()).iterator().next()
        .getValue().toString();
    this.unclassifiedFilename = value;
  
    checkClassifiedFilename(o, getName() + Owlcl.MODULE_CLASSIFIED_SUFFIX, logger);
    value = getAnnotations(o, ModuleVocab.module_classified_filename.getAP()).iterator().next()
        .getValue().toString();
    this.classifiedFilename = value;
  
    checkIsUnclassified(o, logger);
    value = getAnnotations(o, ModuleVocab.module_is_unclassified.getAP()).iterator().next()
        .getValue().toString();
    this.unclassified = value.equalsIgnoreCase("true");
  
    checkIsClassified(o, logger);
    value = getAnnotations(o, ModuleVocab.module_is_classified.getAP()).iterator().next()
        .getValue().toString();
    this.classified = value.equalsIgnoreCase("true");
  
    checkUnclassifiedAddlegacy(o, logger);
    value = getAnnotations(o, ModuleVocab.module_unclassified_addlegacy.getAP()).iterator().next()
        .getValue().toString();
    this.unclassifiedAddlegacy = value.equalsIgnoreCase("true");
  
    checkClassifiedAddlegacy(o, logger);
    value = getAnnotations(o, ModuleVocab.module_classified_addlegacy.getAP()).iterator().next()
        .getValue().toString();
    this.classifiedAddlegacy = value.equalsIgnoreCase("true");
  
    checkUnclassifiedCleanlegacy(o, logger);
    value = getAnnotations(o, ModuleVocab.module_unclassified_cleanlegacy.getAP()).iterator()
        .next().getValue().toString();
    this.unclassifiedCleanlegacy = value.equalsIgnoreCase("true");
  
    checkClassifiedCleanlegacy(o, logger);
    value = getAnnotations(o, ModuleVocab.module_classified_cleanlegacy.getAP()).iterator().next()
        .getValue().toString();
    this.classifiedCleanlegacy = value.equalsIgnoreCase("true");
  
    checkUnclassifiedBuilders(o, "", logger);
    value = getAnnotations(o, ModuleVocab.module_unclassified_builders.getAP()).iterator().next()
        .getValue().toString();
    List<String> builderNames = new ArrayList<String>();
    for (String name : value.split(","))
    {
      builderNames.add(name.trim());
    }
    this.unclassifiedBuilderNames = builderNames;
  
    checkClassifiedBuilders(o, "", logger);
    value = getAnnotations(o, ModuleVocab.module_classified_builders.getAP()).iterator().next()
        .getValue().toString();
    builderNames = new ArrayList<String>();
    for (String name : value.split(","))
    {
      builderNames.add(name.trim());
    }
    this.classifiedBuilderNames = builderNames;
  
    if (changedOntologies.contains(o))
    {
      changedOntologies.remove(o);
      saveOntology(o);
    }
  
    // ================================================================================
    // Other owl files
    // ================================================================================
  
    // check include
    file = new File(getDirectory().toFile(), getName() + Owlcl.MODULE_INCLUDE_IRI_SUFFIX);
    if (!file.exists())
    {
      o = OwlclUtil.createOntology(getIncludeIri(), configMan);
      configMan.setOntologyDocumentIRI(o, IRI.create(file));
      saveOntology(o);
    }
  
    // check exclude
    file = new File(getDirectory().toFile(), getName() + Owlcl.MODULE_EXCLUDE_IRI_SUFFIX);
    if (!file.exists())
    {
      o = OwlclUtil.createOntology(getExcludeIri(), configMan);
      configMan.setOntologyDocumentIRI(o, IRI.create(file));
      saveOntology(o);
    }
  
    // check legacy
    file = new File(getDirectory().toFile(), getName() + Owlcl.MODULE_LEGACY_IRI_SUFFIX);
    if (!file.exists())
    {
      o = OwlclUtil.createOntology(getLegacyIri(), configMan);
      configMan.setOntologyDocumentIRI(o, IRI.create(file));
      saveOntology(o);
    }
  
    // check legacy removed
    file = new File(getDirectory().toFile(), getName() + Owlcl.MODULE_LEGACY_REMOVED_IRI_SUFFIX);
    if (!file.exists())
    {
      o = OwlclUtil.createOntology(getLegacyRemovedIri(), configMan);
      configMan.setOntologyDocumentIRI(o, IRI.create(file));
      saveOntology(o);
    }
  
    // check source
    file = new File(getDirectory().toFile(), getName() + Owlcl.MODULE_SOURCE_IRI_SUFFIX);
    if (!file.exists())
    {
      o = OwlclUtil.createOntology(getSourceConfigurationIri(), configMan);
      configMan.setOntologyDocumentIRI(o, IRI.create(file));
      saveOntology(o);
      // the source manager should load this one
      configMan.removeOntology(o);
    }
  }

  public Set<OWLAnnotation> getAnnotations() {
    return getConfigurationOntology().getAnnotations();
  }

  @Override
  public List<String> getClassifiedBuilderNames() {

    return this.classifiedBuilderNames;
  }

  @Override
  public String getClassifiedFileName() {
    if (classifiedFilename == null)
    {
      return getName() + Owlcl.MODULE_CLASSIFIED_SUFFIX;
    }
    return classifiedFilename;
  }

  @Override
  public IRI getClassifiedIri() {
    if (classifiedIri == null)
    {
      return IRI.create(getIriPrefix() + getName() + Owlcl.MODULE_CLASSIFIED_SUFFIX);
    }
    return classifiedIri;
  }

  @Override
  public Set<IRI> getExcludedSourceIris() {
    return this.excludedIris;
  }

  @Override
  public IRI getExcludeIri() {
    return IRI.create(getIriPrefix() + getName() + Owlcl.MODULE_EXCLUDE_IRI_SUFFIX);
  }

  @Override
  public OWLOntology getExcludeOntology() {
    return OwlclUtil.getOrLoadOntology(this.getExcludeIri(), configMan);
  }

  @Override
  public IRI getIncludeIri() {
    return IRI.create(getIriPrefix() + getName() + Owlcl.MODULE_INCLUDE_IRI_SUFFIX);
  }

  @Override
  public OWLOntology getIncludeOntology() {
    return OwlclUtil.getOrLoadOntology(this.getIncludeIri(), configMan);
  }

  @Override
  public IRI getLegacyIri() {
    return IRI.create(this.getIriPrefix() + this.getName() + Owlcl.MODULE_LEGACY_IRI_SUFFIX);
  }

  @Override
  public OWLOntology getLegacyOntology() {
    return OwlclUtil.getOrLoadOntology(this.getLegacyIri(), configMan);
  }

  @Override
  public IRI getLegacyRemovedIri() {
    return IRI
        .create(this.getIriPrefix() + this.getName() + Owlcl.MODULE_LEGACY_REMOVED_IRI_SUFFIX);
  }

  @Override
  public OWLOntology getLegacyRemovedOntology() {
    return OwlclUtil.getOrLoadOntology(this.getLegacyRemovedIri(), configMan);
  }

  @Override
  public IRI getSourceConfigurationIri() {
    return IRI.create(this.getIriPrefix() + this.getName() + Owlcl.MODULE_SOURCE_IRI_SUFFIX);
  }

  @Override
  public OWLOntology getSourceConfigurationOntology() {
    if (sourceConfigurationOntology == null)
    {
      File sourceFile = new File(getDirectory().toFile(), getName()
          + Owlcl.MODULE_SOURCE_IRI_SUFFIX);
      sourceConfigurationOntology = OwlclUtil.loadOntology(sourceFile, getSourcesManager());
    }
    return this.sourceConfigurationOntology;
  }

  public OWLOntology getSourceOntology() {
    if (sourceOntology == null)
    {
      sourceOntology = OwlclUtil.createOntology(IRI.create("http://owlcl/merged-source"),
          getSourcesManager());
      Set<IRI> excludes = getExcludedSourceIris();
      for (OWLImportsDeclaration id : getSourceConfigurationOntology().getImportsDeclarations())
      {
        if (excludes.contains(id.getIRI()))
        {
          continue;
        } else
        {
          OWLOntology source = OwlclUtil.getOrLoadOntology(id.getIRI(), getSourcesManager());

          for (OWLOntology o : source.getImportsClosure())
          {
            if (!excludes.contains(o.getOntologyID().getOntologyIRI()))
            {
              getSourcesManager().addAxioms(sourceOntology, o.getAxioms());
            }
          }
        }
      }

    }
    return sourceOntology;
  }

  public OWLReasoner getSourceReasoner() {
    if (sourceReasoner == null)
    {
      sourceReasoner = reasonerManager.getReasonedOntology(getSourceOntology());
    }
    return sourceReasoner;
  }

  public OWLOntologyManager getSourcesManager() {
    return sourceManager;
  }

  @Override
  public IRI getTopIri() {
    return IRI.create(getIriPrefix() + getName() + Owlcl.MODULE_TOP_IRI_SUFFIX);
  }

  @Override
  public OWLOntology getTopOntology() {
    return OwlclUtil.getOrLoadOntology(getTopIri(), configMan);
  }

  @Override
  public List<String> getUnclassifiedBuilderNames() {

    return this.unclassifiedBuilderNames;
  }

  public IRI getUnclassifiedIri() {
    if (unclassifiedIri == null)
    {
      return IRI.create(getIriPrefix() + getName() + Owlcl.MODULE_UNCLASSIFIED_SUFFIX);
    }
    return unclassifiedIri;
  }

  @Override
  public String getUnclassifiedFileName() {
    if (unclassifiedFilename == null)
    {
      return getName() + Owlcl.MODULE_UNCLASSIFIED_SUFFIX;
    }
    return unclassifiedFilename;
  }

  @Override
  public boolean isClassified() {
    return classified;
  }

  @Override
  public boolean isClassifiedAddlegacy() {
    return this.classifiedAddlegacy;
  }

  @Override
  public boolean isClassifiedCleanLegacy() {
    return this.classifiedCleanlegacy;
  }

  @Override
  public boolean isUnclassified() {
    return unclassified;
  }

  @Override
  public boolean isUnclassifiedAddlegacy() {
    return this.unclassifiedAddlegacy;
  }

  @Override
  public boolean isUnclassifiedCleanLegacy() {
    return this.unclassifiedCleanlegacy;
  }

  @Override
  public void setClassified(boolean classified) {
    this.classified = classified;
    String value = classified ? "true" : "false";
    OWLOntology co = getConfigurationOntology();
    OWLOntologyManager man = co.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    for (OWLAnnotation a : co.getAnnotations())
    {
      if (a.getProperty().equals(ModuleVocab.module_is_classified.getAP()))
      {
        man.applyChange(new RemoveOntologyAnnotation(co, a));
      }
    }
    man.applyChange(new AddOntologyAnnotation(co, df.getOWLAnnotation(
        ModuleVocab.module_is_classified.getAP(), df.getOWLLiteral(value))));

    saveOntology(getConfigurationOntology());

  }

  @Override
  public void setClassifiedAddlegacy(boolean addlegacy) {
    this.classifiedAddlegacy = addlegacy;
    String value = addlegacy ? "true" : "false";
    OWLOntology co = getConfigurationOntology();
    OWLOntologyManager man = co.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    for (OWLAnnotation a : co.getAnnotations())
    {
      if (a.getProperty().equals(ModuleVocab.module_classified_addlegacy.getAP()))
      {
        man.applyChange(new RemoveOntologyAnnotation(co, a));
      }
    }
    man.applyChange(new AddOntologyAnnotation(co, df.getOWLAnnotation(
        ModuleVocab.module_classified_addlegacy.getAP(), df.getOWLLiteral(value))));

    saveOntology(getConfigurationOntology());

  }

  public void setClassifiedBuilderNames(List<String> names) {
    // these are not changable for now
    this.classifiedBuilderNames = names;
  }

  @Override
  public void setClassifiedCleanlegacy(boolean cleanlegacy) {
    this.classifiedCleanlegacy = cleanlegacy;
    String value = cleanlegacy ? "true" : "false";
    OWLOntology co = getConfigurationOntology();
    OWLOntologyManager man = co.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    for (OWLAnnotation a : co.getAnnotations())
    {
      if (a.getProperty().equals(ModuleVocab.module_classified_cleanlegacy.getAP()))
      {
        man.applyChange(new RemoveOntologyAnnotation(co, a));
      }
    }
    man.applyChange(new AddOntologyAnnotation(co, df.getOWLAnnotation(
        ModuleVocab.module_classified_cleanlegacy.getAP(), df.getOWLLiteral(value))));

    saveOntology(getConfigurationOntology());

  }

  @Override
  public void setClassifiedFilename(String name) {
    this.classifiedFilename = name;

    OWLOntology co = getConfigurationOntology();
    OWLOntologyManager man = co.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    for (OWLAnnotation a : co.getAnnotations())
    {
      if (a.getProperty().equals(ModuleVocab.module_classified_filename.getAP()))
      {
        man.applyChange(new RemoveOntologyAnnotation(co, a));
      }
    }
    man.applyChange(new AddOntologyAnnotation(co, df.getOWLAnnotation(
        ModuleVocab.module_classified_filename.getAP(), df.getOWLLiteral(name))));

    saveOntology(getConfigurationOntology());

  }

  @Override
  public void setClassifiedIri(IRI iri) {
    this.classifiedIri = iri;

    OWLOntology co = getConfigurationOntology();
    OWLOntologyManager man = co.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    for (OWLAnnotation a : co.getAnnotations())
    {
      if (a.getProperty().equals(ModuleVocab.module_classified_iri.getAP()))
      {
        man.applyChange(new RemoveOntologyAnnotation(co, a));
      }
    }
    man.applyChange(new AddOntologyAnnotation(co, df.getOWLAnnotation(
        ModuleVocab.module_classified_iri.getAP(), df.getOWLLiteral(iri.toString()))));

    saveOntology(getConfigurationOntology());

  }

  @Override
  public void setSourceExcludedIris(Set<IRI> iris) {
    OWLOntology so = getSourceConfigurationOntology();
    OWLOntologyManager man = so.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    for (OWLAnnotation a : so.getAnnotations())
    {
      if (a.getProperty().equals(ModuleVocab.module_source_exclude.getAP()))
      {
        man.applyChange(new RemoveOntologyAnnotation(so, a));
      }
    }

    for (IRI iri : iris)
    {
      man.applyChange(new AddOntologyAnnotation(so, df.getOWLAnnotation(
          ModuleVocab.module_source_exclude.getAP(), df.getOWLLiteral(iri.toString()))));
    }
    saveOntology(so);
  }

  @Override
  public void setSourceIris(Set<IRI> iris) {
    OWLOntology so = getSourceConfigurationOntology();
    OWLOntologyManager man = so.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    for (OWLImportsDeclaration id : so.getImportsDeclarations())
    {
      man.applyChange(new RemoveImport(so, id));
    }
    for (IRI iri : iris)
    {
      man.applyChange(new AddImport(so, df.getOWLImportsDeclaration(iri)));
    }
    saveOntology(so);
  }

  @Override
  public void setUnclassified(boolean unclassified) {
    this.unclassified = unclassified;
    String value = unclassified ? "true" : "false";
    OWLOntology co = getConfigurationOntology();
    OWLOntologyManager man = co.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    for (OWLAnnotation a : co.getAnnotations())
    {
      if (a.getProperty().equals(ModuleVocab.module_is_unclassified.getAP()))
      {
        man.applyChange(new RemoveOntologyAnnotation(co, a));
      }
    }
    man.applyChange(new AddOntologyAnnotation(co, df.getOWLAnnotation(
        ModuleVocab.module_is_unclassified.getAP(), df.getOWLLiteral(value))));

    saveOntology(getConfigurationOntology());

  }

  @Override
  public void setUnclassifiedAddlegacy(boolean addlegacy) {
    this.unclassifiedAddlegacy = addlegacy;
    String value = addlegacy ? "true" : "false";
    OWLOntology co = getConfigurationOntology();
    OWLOntologyManager man = co.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    for (OWLAnnotation a : co.getAnnotations())
    {
      if (a.getProperty().equals(ModuleVocab.module_unclassified_addlegacy.getAP()))
      {
        man.applyChange(new RemoveOntologyAnnotation(co, a));
      }
    }
    man.applyChange(new AddOntologyAnnotation(co, df.getOWLAnnotation(
        ModuleVocab.module_unclassified_addlegacy.getAP(), df.getOWLLiteral(value))));

    saveOntology(getConfigurationOntology());

  }

  public void setUnclassifiedBuilderNames(List<String> builderNames) {
    // these are not changable for now
    this.unclassifiedBuilderNames = builderNames;
  }

  @Override
  public void setUnclassifiedCleanlegacy(boolean cleanlegacy) {
    this.unclassifiedCleanlegacy = cleanlegacy;
    String value = cleanlegacy ? "true" : "false";
    OWLOntology co = getConfigurationOntology();
    OWLOntologyManager man = co.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    for (OWLAnnotation a : co.getAnnotations())
    {
      if (a.getProperty().equals(ModuleVocab.module_unclassified_cleanlegacy.getAP()))
      {
        man.applyChange(new RemoveOntologyAnnotation(co, a));
      }
    }
    man.applyChange(new AddOntologyAnnotation(co, df.getOWLAnnotation(
        ModuleVocab.module_unclassified_cleanlegacy.getAP(), df.getOWLLiteral(value))));

    saveOntology(getConfigurationOntology());

  }

  @Override
  public void setUnclassifiedFilename(String name) {
    this.unclassifiedFilename = name;

    OWLOntology co = getConfigurationOntology();
    OWLOntologyManager man = co.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    for (OWLAnnotation a : co.getAnnotations())
    {
      if (a.getProperty().equals(ModuleVocab.module_unclassified_filename.getAP()))
      {
        man.applyChange(new RemoveOntologyAnnotation(co, a));
      }
    }
    man.applyChange(new AddOntologyAnnotation(co, df.getOWLAnnotation(
        ModuleVocab.module_unclassified_filename.getAP(), df.getOWLLiteral(name))));

    saveOntology(getConfigurationOntology());

  }

  @Override
  public void setUnclassifiedIri(IRI iri) {
    this.unclassifiedIri = iri;

    OWLOntology co = getConfigurationOntology();
    OWLOntologyManager man = co.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    for (OWLAnnotation a : co.getAnnotations())
    {
      if (a.getProperty().equals(ModuleVocab.module_unclassified_iri.getAP()))
      {
        man.applyChange(new RemoveOntologyAnnotation(co, a));
      }
    }
    man.applyChange(new AddOntologyAnnotation(co, df.getOWLAnnotation(
        ModuleVocab.module_unclassified_iri.getAP(), df.getOWLLiteral(iri.toString()))));

    saveOntology(getConfigurationOntology());
  }

}
