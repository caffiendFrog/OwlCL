package com.essaid.owlcl.command.module.config;

import static com.essaid.owlcl.command.module.ModuleVocab.*;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;

import com.essaid.owlcl.core.annotation.InjectLogger;
import com.essaid.owlcl.core.reasoner.IReasonerManager;
import com.google.inject.Inject;

public abstract class AbstractModuleConfiguration implements IModuleConfigInternal {

  @Inject
  protected IReasonerManager reasonerManager;
  @InjectLogger
  protected Logger logger;

  protected final String moduleName;
  protected final String prefix;
  protected final Path directory;
  protected final OWLOntology configurationOntology;
  protected final OWLOntologyManager configMan;
  protected final OWLDataFactory configDf;

  protected OWLOntology sourceConfigurationOntology;// loaded without imports
                                                    // for configuration
  protected OWLOntology sourceOntology;
  protected OWLReasoner sourceReasoner;
  protected OWLOntologyManager sourceManager;

  protected final Set<OWLOntology> changedOntologies = new HashSet<OWLOntology>();
  private OWLOntologyChangeListener changeListener;

  protected boolean update;
  protected boolean upgrade;

  //
  // protected boolean unclassified;
  // protected boolean classified;
  // protected IRI classifiedIri;
  // protected IRI unclassifiedIri;

  // protected List<String> classifiedBuilderNames = new ArrayList<String>();
  // protected List<String> unclassifiedBuilderNames = new ArrayList<String>();
  // protected String unclassifiedFilename;
  // protected String classifiedFilename;
  // protected HashSet<IRI> excludedIris = new HashSet<IRI>();
  // protected OWLOntology sourceConfigurationOntology;
  // protected boolean classifiedAddlegacy;
  // protected boolean unclassifiedAddlegacy;
  // protected boolean classifiedCleanlegacy;
  // protected boolean unclassifiedCleanlegacy;
  // protected File topFile;

  public AbstractModuleConfiguration(String name, String iriPrefix, Path moduleDirectory,
      OWLOntology configurationOntology) {
    if (name == null || name.isEmpty())
    {
      throw new IllegalStateException("Module constructed with illegal name: " + name);
    }

    if (!moduleDirectory.toFile().exists())
    {

      throw new IllegalStateException("Module constructed with non-existing directory: "
          + moduleDirectory.toAbsolutePath());
    }

    if (iriPrefix == null || configurationOntology == null)
    {

      throw new IllegalStateException(
          "Module constructed with null configuration ontology or iriPrefix.");
    }

    this.moduleName = name;
    this.directory = moduleDirectory;
    this.prefix = iriPrefix;
    this.configurationOntology = configurationOntology;
    this.configMan = configurationOntology.getOWLOntologyManager();
    this.configDf = configMan.getOWLDataFactory();
    this.changeListener = new OWLOntologyChangeListener() {

      @Override
      public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
        for (OWLOntologyChange c : changes)
        {
          changedOntologies.add(c.getOntology());
        }
      }
    };

    configMan.addOntologyChangeListener(changeListener);

    this.update = !new File(getDirectory().toFile(), "noupdate").exists();
    this.upgrade = !new File(getDirectory().toFile(), "noupgrade").exists();
  }

  @Override
  public String getName() {
    return moduleName;
  }

  @Override
  public Path getDirectory() {
    return directory;
  }

  @Override
  public String getIriPrefix() {
    return prefix;
  }

  @Override
  public IRI getConfigurationIri() {
    return configurationOntology.getOntologyID().getOntologyIRI();
  }

  @Override
  public OWLOntology getConfigurationOntology() {
    return configurationOntology;
  }

  protected void addAnnotation(OWLAnnotation annotation) {
    AddOntologyAnnotation aoa = new AddOntologyAnnotation(configurationOntology, annotation);
    configMan.applyChange(aoa);
  }

  protected void removeAnnotation(OWLAnnotation annotation) {
    RemoveOntologyAnnotation roa = new RemoveOntologyAnnotation(configurationOntology, annotation);
    configMan.applyChange(roa);
  }

  protected void removeAnnotations(OWLOntology o, OWLAnnotationProperty p) {
    OWLOntologyManager man = o.getOWLOntologyManager();
    for (OWLAnnotation a : o.getAnnotations())
    {
      if (a.getProperty().equals(p))
      {
        man.applyChange(new RemoveOntologyAnnotation(o, a));
      }
    }
  }

  public void saveConfiguration() {

    Iterator<OWLOntology> oi = changedOntologies.iterator();
    while (oi.hasNext())
    {
      OWLOntology o = oi.next();
      try
      {
        o.getOWLOntologyManager().saveOntology(o);
      } catch (OWLOntologyStorageException e)
      {
        throw new RuntimeException("Error while saving module configuration", e);
      }
      oi.remove();
    }
  }

  public Set<OWLAnnotation> getAnnotations(OWLOntology ontology, OWLAnnotationProperty p,
      boolean transitive) {
    Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
    if (transitive)
    {

      for (OWLOntology o : ontology.getImportsClosure())
      {
        if (o.getOntologyID().getOntologyIRI().equals(getSourceConfigurationIri()))
        {
          continue;
        }
        for (OWLAnnotation a : o.getAnnotations())
        {
          if (a.getProperty().equals(p))
          {
            annotations.add(a);
          }
        }
      }

    } else
    {

      for (OWLAnnotation a : ontology.getAnnotations())
      {
        if (a.getProperty().equals(p))
        {
          annotations.add(a);
        }
      }
    }

    return annotations;
  }

  public void checkUnclassifiedIri(OWLOntology o, IRI iri, Logger logger) {
    OWLOntologyManager man = o.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    Set<OWLAnnotation> as = getAnnotations(o, module_unclassified_iri.getAP(), false);
    if (as.size() > 1)
    {
      logger.warn("Multiple unclassified iri annotations in : " + o.getOntologyID());
    }
    if (as.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(o, df.getOWLAnnotation(
          module_unclassified_iri.getAP(), df.getOWLLiteral(iri.toString()))));
    }

  }

  public void checkClassifiedIri(OWLOntology o, IRI iri, Logger logger) {
    OWLOntologyManager man = o.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    Set<OWLAnnotation> as = getAnnotations(o, module_classified_iri.getAP(), false);
    if (as.size() > 1)
    {
      logger.warn("Multiple classified iri annotations in : " + o.getOntologyID());
    }
    if (as.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(o, df.getOWLAnnotation(
          module_classified_iri.getAP(), df.getOWLLiteral(iri.toString()))));
    }
  }

  public void checkUnclassifiedFilename(OWLOntology o, String fileName, Logger logger) {
    OWLOntologyManager man = o.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    Set<OWLAnnotation> as = getAnnotations(o, module_unclassified_filename.getAP(), false);
    if (as.size() > 1)
    {
      logger.warn("Multiple unclassified filename in : " + o.getOntologyID());
    }
    if (as.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(o, df.getOWLAnnotation(
          module_unclassified_filename.getAP(), df.getOWLLiteral(fileName))));
    }

  }

  public void checkClassifiedFilename(OWLOntology o, String fileName, Logger logger) {
    OWLOntologyManager man = o.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    Set<OWLAnnotation> as = getAnnotations(o, module_classified_filename.getAP(), false);
    if (as.size() > 1)
    {
      logger.warn("Multiple classified filename in : " + o.getOntologyID());
    }
    if (as.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(o, df.getOWLAnnotation(
          module_classified_filename.getAP(), df.getOWLLiteral(fileName))));
    }
  }

  public void checkUnclassifiedBuilders(OWLOntology o, String builders, Logger logger) {
    OWLOntologyManager man = o.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    Set<OWLAnnotation> as = getAnnotations(o, module_unclassified_builders.getAP(), false);
    if (as.size() > 1)
    {
      logger.warn("Multiple unclassified builders in : " + o.getOntologyID());
    }
    if (as.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(o, df.getOWLAnnotation(
          module_unclassified_builders.getAP(), df.getOWLLiteral(builders))));
    }
  }

  public void checkClassifiedBuilders(OWLOntology o, String builders, Logger logger) {

    OWLOntologyManager man = o.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    Set<OWLAnnotation> as = getAnnotations(o, module_classified_builders.getAP(), false);
    if (as.size() > 1)
    {
      logger.warn("Multiple classified builders in : " + o.getOntologyID());
    }
    if (as.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(o, df.getOWLAnnotation(
          module_classified_builders.getAP(), df.getOWLLiteral(builders))));
    }

  }

  public void checkIsUnclassified(OWLOntology o, Logger logger) {
    OWLOntologyManager man = o.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    Set<OWLAnnotation> as = getAnnotations(o, module_is_unclassified.getAP(), false);
    if (as.size() > 1)
    {
      logger.warn("Multiple is unclassified in : " + o.getOntologyID());
    }
    if (as.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(o, df.getOWLAnnotation(
          module_is_unclassified.getAP(), df.getOWLLiteral("false"))));
    }

  }

  public void checkIsClassified(OWLOntology o, Logger logger) {
    OWLOntologyManager man = o.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    Set<OWLAnnotation> as = getAnnotations(o, module_is_classified.getAP(), false);
    if (as.size() > 1)
    {
      logger.warn("Multiple is classified in : " + o.getOntologyID());
    }
    if (as.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(o, df.getOWLAnnotation(
          module_is_classified.getAP(), df.getOWLLiteral("false"))));
    }
  }

  public void checkUnclassifiedAddlegacy(OWLOntology o, Logger logger) {
    OWLOntologyManager man = o.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    Set<OWLAnnotation> as = getAnnotations(o, module_unclassified_addlegacy.getAP(), false);
    if (as.size() > 1)
    {
      logger.warn("Multiple unclassified add legacy: " + o.getOntologyID());
    }
    if (as.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(o, df.getOWLAnnotation(
          module_unclassified_addlegacy.getAP(), df.getOWLLiteral("false"))));
    }
  }

  public void checkClassifiedAddlegacy(OWLOntology o, Logger logger) {
    OWLOntologyManager man = o.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    Set<OWLAnnotation> as = getAnnotations(o, module_classified_addlegacy.getAP(), false);
    if (as.size() > 1)
    {
      logger.warn("Multiple classified add legacy: " + o.getOntologyID());
    }
    if (as.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(o, df.getOWLAnnotation(
          module_classified_addlegacy.getAP(), df.getOWLLiteral("false"))));
    }
  }

  public void checkUnclassifiedCleanlegacy(OWLOntology o, Logger logger) {
    OWLOntologyManager man = o.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    Set<OWLAnnotation> as = getAnnotations(o, module_unclassified_cleanlegacy.getAP(), false);
    if (as.size() > 1)
    {
      logger.warn("Multiple unclassified clean legacy: " + o.getOntologyID());
    }
    if (as.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(o, df.getOWLAnnotation(
          module_unclassified_cleanlegacy.getAP(), df.getOWLLiteral("false"))));
    }
  }

  public void checkClassifiedCleanlegacy(OWLOntology o, Logger logger) {
    OWLOntologyManager man = o.getOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();

    Set<OWLAnnotation> as = getAnnotations(o, module_classified_cleanlegacy.getAP(), false);
    if (as.size() > 1)
    {
      logger.warn("Multiple classified clean legacy: " + o.getOntologyID());
    }
    if (as.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(o, df.getOWLAnnotation(
          module_classified_cleanlegacy.getAP(), df.getOWLLiteral("false"))));
    }
  }

  protected void saveOntology(OWLOntology o) {
    try
    {
      o.getOWLOntologyManager().saveOntology(o);
    } catch (OWLOntologyStorageException e)
    {
      throw new RuntimeException("Failed to save ontology " + o.getOntologyID().getOntologyIRI()
          + " to file " + o.getOWLOntologyManager().getOntologyDocumentIRI(o), e);
    }
    changedOntologies.remove(o);
  }

  protected static String csv(List<String> values) {
    Iterator<String> i = values.iterator();
    StringBuilder sb = new StringBuilder();
    while (i.hasNext())
    {
      sb.append(i.next());
      if (i.hasNext())
      {
        sb.append(",");
      }
    }
    return sb.toString();
  }

  public void setSourceReasoner(OWLReasoner reasoner){
	  this.sourceReasoner = reasoner;
  }
}
