package com.essaid.owlcl.command.module.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.essaid.owlcl.command.module.IModule;
import com.essaid.owlcl.command.module.ModuleVocab;
import com.essaid.owlcl.command.module.Owlcl;
import com.essaid.owlcl.command.module.Util;
import com.essaid.owlcl.core.reasoner.IReasonerManager;
import com.essaid.owlcl.core.util.OwlclUtil;
import com.google.inject.Inject;

public class ModuleConfigurationV1 extends AbstractModuleConfiguration {
  
  @Inject
  IReasonerManager reasonerManager;

  private List<String> builderInferredNames= new ArrayList<String>();

  private List<String> builderNames = new ArrayList<String>();

  private OWLOntology configOntology;

  private String generate = "";

  private String generatedModuleFileName;

  private String generatedModuleInferredFileName;

  private IRI generatedModuleInferredIri;

  private IRI generatedModuleIri;

  private String generateInferred = "";

  private boolean localReasoner;

  private boolean localSourceOntology;

  private String moduleName;

  private String modulePrefix;

  private OWLOntology sourceOntology;

  private OWLReasoner sourceReasoner;

  private OWLOntologyManager sourcesManager;

  private OWLOntologyManager configManager;

  private File directory;

  private IRI includeIri;

  private IRI excludeIri;

  private IRI legacyIri;

  private IRI legacyRemovedIri;

  private OWLOntologyManager localManager;

  private HashSet<IRI> excludedIris;

  private String addLegacy = "";

  private String cleanLegacy = "";

  private boolean loaded;

  public ModuleConfigurationV1(File directory, OWLOntologyManager configManager,
      OWLOntologyManager sourceManager) {
    int version = Util.getModuleVersion(directory);
    if (IModule.VERSION != version)
    {
      throw new RuntimeException("Trying to load module configuration in directory "
          + directory.getAbsolutePath() + " for version " + version + " while it should be V-1. "
          + "Update the module to current version.");
    }

    if (directory == null || configManager == null || sourceManager == null)
    {
      throw new RuntimeException("Constructor with null parameters.");
    }

    File configFile = FileUtils
        .listFiles(directory, new SuffixFileFilter("-module-configuration.owl"), null).iterator()
        .next();

    this.localManager = OWLManager.createOWLOntologyManager();
    this.localManager.setSilentMissingImportsHandling(true);
    this.localManager.clearIRIMappers();

    try
    {
      this.configOntology = this.localManager.loadOntologyFromOntologyDocument(configFile);
    } catch (OWLOntologyCreationException e)
    {
      throw new RuntimeException("Failed to load module configuration V-1 from directory: "
          + directory.getAbsolutePath(), e);
    }

    this.directory = directory;
    this.configManager = configManager;
    this.sourcesManager = sourceManager;

  }

  // public ModuleConfigurationV1(OWLOntology ontology) {
  // this.config = ontology;
  // }

  public void loadConfiguration() {

    if (loaded)
    {
      return;
    }
    loaded = true;
    String configIriString = configOntology.getOntologyID().getOntologyIRI().toString();
    int index = configIriString.lastIndexOf('/');
    modulePrefix = configIriString.substring(0, index + 1);
    moduleName = configIriString.substring(index + 1);
    index = moduleName.lastIndexOf("-module-configuration.owl");
    moduleName = moduleName.substring(0, index);

    excludedIris = new HashSet<IRI>();

    for (OWLAnnotation a : configOntology.getAnnotations())
    {
      // look for custom IRI
      if (a.getProperty().equals(ModuleVocab.module_iri.getAP()))
      {
        generatedModuleIri = IRI.create(((OWLLiteral) a.getValue()).getLiteral());

      }
      if (a.getProperty().equals(ModuleVocab.module_iri_inferred.getAP()))
      {
        generatedModuleInferredIri = IRI.create(((OWLLiteral) a.getValue()).getLiteral());
      }

      // look for custom file name.
      if (a.getProperty().equals(ModuleVocab.module_file_name.getAP()))
      {
        generatedModuleFileName = ((OWLLiteral) a.getValue()).getLiteral();
      }
      if (a.getProperty().equals(ModuleVocab.module_file_name_inferred.getAP()))
      {
        generatedModuleInferredFileName = ((OWLLiteral) a.getValue()).getLiteral();
      }

      // look for excluded sources
      if (a.getProperty().equals(ModuleVocab.module_source_exclude.getAP()))
      {
        String sourceExcludeIri = ((OWLLiteral) a.getValue()).getLiteral();
        excludedIris.add(IRI.create(sourceExcludeIri));
      }

      // look for builders
      if (a.getProperty().equals(ModuleVocab.module_builders.getAP()))
      {
        String[] names = ((OWLLiteral) a.getValue()).getLiteral().split(",");
        for (int i = 0; i < names.length; ++i)
        {
          builderNames.add(names[i].toLowerCase().trim());
        }
      }

      // look for builders inferred
      if (a.getProperty().equals(ModuleVocab.module_inferred_builders.getAP()))
      {
        String[] names = ((OWLLiteral) a.getValue()).getLiteral().split(",");
        for (int i = 0; i < names.length; ++i)
        {
          builderInferredNames.add(names[i].toLowerCase().trim());
        }
      }

      // generate
      if (a.getProperty().equals(ModuleVocab.module_generate.getAP()))
      {

        generate = ((OWLLiteral) a.getValue()).getLiteral().trim();
      }

      // generate inferred
      if (a.getProperty().equals(ModuleVocab.module_generate_inferred.getAP()))
      {
        generateInferred = ((OWLLiteral) a.getValue()).getLiteral().trim();
      }

      // add legacy
      if (a.getProperty().equals(ModuleVocab.module_add_legacy.getAP()))
      {
        addLegacy = ((OWLLiteral) a.getValue()).getLiteral().trim();
      }

      // clean legacy
      if (a.getProperty().equals(ModuleVocab.module_clean_legacy.getAP()))
      {
        cleanLegacy = ((OWLLiteral) a.getValue()).getLiteral().trim();
      }
    }

    // ================================================================================
    // sources
    // ================================================================================
    // load the source if needed
    if (this.sourceOntology == null)
    {
      sourceOntology = OwlclUtil.createOntology(
          IRI.create("http://owlcl/" + moduleName + "-merged-module-sources.owl"), sourcesManager);
      for (OWLImportsDeclaration id : configOntology.getImportsDeclarations())
      {
        if (!excludedIris.contains(id.getIRI()))
        {
          // we now have a direct import that is considered a source. It will
          // likely have its own import chain so it needs to be loaded and the
          // chain inspected for excludes. before building the final source
          // ontology. The loading is now done from the source manager.
          OWLOntology sourceOntology = OwlclUtil.getOrLoadOntology(id.getIRI(), sourcesManager);

          for (OWLOntology o : sourceOntology.getImportsClosure())
          {
            if (!excludedIris.contains(o.getOntologyID().getOntologyIRI()))
            {
              sourcesManager.addAxioms(this.sourceOntology, o.getAxioms());
            }
          }

        }
      }
      this.localSourceOntology = true;
    }

    // load the reasoner if needed
    if (this.sourceReasoner == null)
    {
      this.sourceReasoner = reasonerManager.getReasonedOntology(sourceOntology);
      this.localReasoner = true;
    }

    if (this.generatedModuleIri == null)
    {
      this.generatedModuleIri = IRI.create(this.modulePrefix + this.moduleName
          + Owlcl.MODULE_IRI_SUFFIX);
    }
    this.includeIri = IRI.create(this.modulePrefix + this.moduleName
        + Owlcl.MODULE_INCLUDE_IRI_SUFFIX);
    this.excludeIri = IRI.create(this.modulePrefix + this.moduleName
        + Owlcl.MODULE_EXCLUDE_IRI_SUFFIX);
    this.legacyIri = IRI.create(this.modulePrefix + this.moduleName
        + Owlcl.MODULE_LEGACY_IRI_SUFFIX);
    this.legacyRemovedIri = IRI.create(this.modulePrefix + this.moduleName
        + Owlcl.MODULE_LEGACY_REMOVED_IRI_SUFFIX);

  }

  public String getGenerate() {
    return generate;
  }

  public String getGeneratedModuleFileName() {
    return generatedModuleFileName;
  }

  public String getGeneratedModuleInferredFileName() {
    return generatedModuleInferredFileName;
  }

  public IRI getGeneratedModuleInferredIri() {
    return generatedModuleInferredIri;
  }

  public IRI getGeneratedModuleIri() {
    return generatedModuleIri;
  }

  public String getGenerateInferred() {
    return generateInferred;
  }

  public String getModuleName() {
    return moduleName;
  }

  public String getModulePrefix() {
    return modulePrefix;
  }

  public OWLOntology getSourceOntology() {
    return sourceOntology;
  }

  public OWLReasoner getSourceReasoner() {
    return sourceReasoner;
  }

  public OWLOntologyManager getSourcesManager() {
    return sourcesManager;
  }

  public boolean isLocalReasoner() {
    return localReasoner;
  }

  public boolean isLocalSources() {
    return localSourceOntology;
  }

  public void setBuilderInferredNames(List<String> builderInferredNames) {
    this.builderInferredNames = builderInferredNames;
  }

  public void setBuilderNames(List<String> builderNames) {
    this.builderNames = builderNames;
  }

  public void setConfig(OWLOntology config) {
    this.configOntology = config;
  }

  public void setGenerate(String generate) {
    this.generate = generate;
  }

  public void setGeneratedModuleFileName(String generatedModuleFileName) {
    this.generatedModuleFileName = generatedModuleFileName;
  }

  public void setGeneratedModuleInferredFileName(String generatedModuleInferredFileName) {
    this.generatedModuleInferredFileName = generatedModuleInferredFileName;
  }

  public void setGeneratedModuleInferredIri(IRI generatedModuleInferredIri) {
    this.generatedModuleInferredIri = generatedModuleInferredIri;
  }

  public void setGeneratedModuleIri(IRI generatedModuleIri) {
    this.generatedModuleIri = generatedModuleIri;
  }

  public void setGenerateInferred(String generateInferred) {
    this.generateInferred = generateInferred;
  }

  public void setLocalReasoner(boolean localReasoner) {
    this.localReasoner = localReasoner;
  }

  public void setLocalSources(boolean localSources) {
    this.localSourceOntology = localSources;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  public void setModulePrefix(String modulePrefix) {
    this.modulePrefix = modulePrefix;
  }

  public void setSourceOntology(OWLOntology sourceOntology) {
    this.sourceOntology = sourceOntology;
  }

  public void setSourceReasoner(OWLReasoner sourceReasoner) {
    this.sourceReasoner = sourceReasoner;
  }

  public void setSourcesManager(OWLOntologyManager sourcesManager) {
    this.sourcesManager = sourcesManager;
  }

  @Override
  public IRI getConfigurationIri() {
    return configOntology.getOntologyID().getOntologyIRI();
  }

  @Override
  public OWLOntology getConfigurationOntology() {
    return configOntology;
  }

  @Override
  public IRI getIncludeIri() {
    return IRI.create(modulePrefix + moduleName + Owlcl.MODULE_INCLUDE_IRI_SUFFIX);
  }

  @Override
  public OWLOntology getIncludeOntology() {
    return OwlclUtil.getOrLoadOntology(this.includeIri, configManager);
  }

  @Override
  public IRI getExcludeIri() {
    return this.excludeIri;
  }

  @Override
  public OWLOntology getExcludeOntology() {
    return OwlclUtil.getOrLoadOntology(this.excludeIri, configManager);
  }

  @Override
  public IRI getLegacyIri() {
    return this.legacyIri;
  }

  @Override
  public OWLOntology getLegacyOntology() {
    return OwlclUtil.getOrLoadOntology(this.legacyIri, configManager);
  }

  @Override
  public IRI getLegacyRemovedIri() {
    return this.legacyRemovedIri;
  }

  @Override
  public OWLOntology getLegacyRemovedOntology() {
    return OwlclUtil.getOrLoadOntology(this.legacyRemovedIri, configManager);
  }

  @Override
  public OWLOntologyManager getSourceManager() {
    return sourcesManager;
  }

  @Override
  public List<String> getBuildersNames() {
    return this.builderNames;
  }

  @Override
  public List<String> getBuildersInferredNames() {
    return this.builderInferredNames;
  }

  @Override
  public Set<IRI> getExcludeSourceIris() {
    return this.excludedIris;
  }

  @Override
  public boolean isGenerate() {
    return this.generate.equalsIgnoreCase("true");
  }

  @Override
  public boolean isGenerateInferred() {
    return this.generateInferred.equalsIgnoreCase("true");
  }

  @Override
  public boolean isAddLegacy() {
    return this.addLegacy.equalsIgnoreCase("true");
  }

  @Override
  public boolean isCleanLegacy() {
    return this.cleanLegacy.equalsIgnoreCase("true");
  }

  @Override
  public Set<OWLAnnotation> getAnnotations() {
    return this.configOntology.getAnnotations();
  }

  @Override
  public String getGenerateModuleFileName() {
    return this.generatedModuleFileName;
  }

  @Override
  public IRI getGeneratedInferredModuleIri() {
    return this.generatedModuleInferredIri;
  }

  @Override
  public String getGenerateInferredModuleFileName() {
    return this.generatedModuleInferredFileName;
  }

}
