package com.essaid.owlcl.command;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.cli.util.IriConverter;
import com.essaid.owlcl.core.util.OntologyFiles;
import com.essaid.owlcl.core.util.OwlclUtil;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@Parameters(commandNames = "annotate", commandDescription = "Add/Replace entity annotations in "
    + "in target ontologies based on source ontologies and annotation properties.")
public class AnnotateCommand extends AbstractCommand {

  // ================================================================================
  // source ontologies
  // ================================================================================

  @Parameter(names = "-sourceIris", description = "The IRIs of the source ontologies.",
      converter = IriConverter.class, variableArity = true)
  public void setSourceIris(List<IRI> sourceIris) {
    this.sourceIris = sourceIris;
    this.sourceIrisSet = true;
  }

  public List<IRI> getSourceIris() {
    return sourceIris;
  }

  public boolean isSourceIrisSet() {
    return sourceIrisSet;
  }

  private List<IRI> sourceIris = new ArrayList<IRI>();
  private boolean sourceIrisSet = false;

  // ================================================================================
  // Source files
  // ================================================================================

  @Parameter(names = "-sourceFiles", converter = FileConverter.class, variableArity = true)
  public void setSourceFiles(List<File> sourceFiles) {
    this.sourceFiles = sourceFiles;
    this.sourceFilesSet = true;
  }

  public List<File> getSourceFiles() {
    return sourceFiles;
  }

  public boolean isSourceFilesSet() {
    return sourceFilesSet;
  }

  private List<File> sourceFiles = new ArrayList<File>();
  private boolean sourceFilesSet = false;

  // ================================================================================
  // source subs
  // ================================================================================

  @Parameter(names = "-noSourceSubs")
  public void setNoSourceSubs(boolean noSourceSubs) {
    this.noSourceSubs = noSourceSubs;
    this.noSourceSubsSet = true;
  }

  public boolean isNoSourceSubs() {
    return noSourceSubs;
  }

  public boolean isNoSourceSubsSet() {
    return noSourceSubsSet;
  }

  private boolean noSourceSubs = false;
  private boolean noSourceSubsSet = false;

  // ================================================================================
  // no source imports
  // ================================================================================

  @Parameter(names = "-noSourceImports", description = "Don't add source imports.")
  public void setNoSourceImports(boolean noSourceImports) {
    this.noSourceImports = noSourceImports;
    this.noSourceImportsSet = true;
  }

  public boolean isNoSourceImports() {
    return noSourceImports;
  }

  public boolean isNoSourceImportsSet() {
    return noSourceImportsSet;
  }

  private boolean noSourceImports = false;
  private boolean noSourceImportsSet = false;

  // ================================================================================
  // target ontologies
  // ================================================================================

  @Parameter(names = "-targetIris", description = "The IRIs of the target ontologies.",
      converter = IriConverter.class, variableArity = true)
  public void setTargetIris(List<IRI> targetIris) {
    this.targetIris = targetIris;
    this.targetIrisSet = true;
  }

  public List<IRI> getTargetIris() {
    return targetIris;
  }

  public boolean isTargetIrisSet() {
    return targetIrisSet;
  }

  private List<IRI> targetIris = new ArrayList<IRI>();
  private boolean targetIrisSet = false;

  // ================================================================================
  // target files
  // ================================================================================

  public List<File> getTargetFiles() {
    return targetFiles;
  }

  @Parameter(names = "-targetFiles", converter = FileConverter.class, variableArity = true)
  public void setTargetFiles(List<File> sourceFiles) {
    this.targetFiles = sourceFiles;
    this.targetFilesSet = true;
  }

  public boolean isTargetFilesSet() {
    return targetFilesSet;
  }

  private List<File> targetFiles = new ArrayList<File>();
  private boolean targetFilesSet = false;

  // ================================================================================
  // target iri pattern
  // ================================================================================
  @Parameter(names = "-targetIriPatterns", variableArity = true)
  public void setTargetIriPattersn(List<String> targetPatterns) {
    this.targetIriPatterns = targetPatterns;
    this.targetIriPatternsSet = true;
  }

  public List<String> getTargetPatterns() {
    return targetIriPatterns;
  }

  public boolean isTargetIriPatternsSet() {
    return targetIriPatternsSet;
  }

  private List<String> targetIriPatterns = new ArrayList<String>();
  private boolean targetIriPatternsSet = false;

  // ================================================================================
  // target subs
  // ================================================================================

  @Parameter(names = "-noTargetSubs", description = "Don't add target sub directories.")
  public void setNoTargetSubs(boolean noTargetSubs) {
    this.noTargetSubs = noTargetSubs;
    this.noTargetSubsSet = true;
  }

  public boolean isNoTargetSubs() {
    return noTargetSubs;
  }

  public boolean isNoTargetSubsSet() {
    return noTargetSubsSet;
  }

  private boolean noTargetSubs = false;
  private boolean noTargetSubsSet = false;

  // ================================================================================
  // no target imports
  // ================================================================================

  @Parameter(names = "-noTargetImports", description = "Don't add target imports.")
  public void setNoTargetImports(boolean noTargetImports) {
    this.noTargetImports = noTargetImports;
    this.noTargetImportsSet = true;
  }

  public boolean isNoTargetImports() {
    return noTargetImports;
  }

  public boolean isNoTargetImportsSet() {
    return noTargetImportsSet;
  }

  private boolean noTargetImports = false;
  private boolean noTargetImportsSet = false;

  // ================================================================================
  // Properties
  // ================================================================================

  @Parameter(names = "-properties", description = "The IRIs of the annotation properties to use.",
      converter = IriConverter.class, variableArity = true)
  public void setProperties(List<IRI> properties) {
    this.properties = properties;
    this.propertiesSet = true;
  }

  public List<IRI> getProperties() {
    return properties;
  }

  public boolean isPropertiesSet() {
    return propertiesSet;
  }

  private List<IRI> properties = new ArrayList<IRI>();
  private boolean propertiesSet = false;

  // ================================================================================
  // replace
  // ================================================================================

  @Parameter(names = "-replace", description = "Replace existing annotations with same property.")
  public void setReplace(boolean replace) {
    this.replace = replace;
    this.replaceSet = true;
  }

  public boolean isReplace() {
    return replace;
  }

  public boolean isReplaceSet() {
    return replaceSet;
  }

  private boolean replace = false;
  private boolean replaceSet = false;

  // ================================================================================
  // add to all entities
  // ================================================================================

  @Parameter(names = "-addToAll", description = "Add annotatio(s) to all instances of "
      + "the entity. False by default. By default annotation is only added (or replaces) "
      + "if there is already an annotation with the same property.")
  public void setAddToAll(boolean replace) {
    this.addToAll = replace;
    this.addToAllSet = true;
  }

  public boolean isAddToAll() {
    return addToAll;
  }

  public boolean isAddToAllSet() {
    return addToAllSet;
  }

  private boolean addToAll = false;
  private boolean addToAllSet = false;
  private HashSet<OWLOntology> finalTargetOntologies;
  private HashSet<OWLOntology> finalSourceOntologies;
  private HashSet<OWLOntology> changedOntologies;

  // ================================================================================
  //
  // ================================================================================

  private void configure() {
    if (!isSourceFilesSet())
    {
      this.sourceFiles.add(getMain().getProject());
    }

    if (!isTargetFilesSet())
    {
      this.targetFiles.add(getMain().getProject());
    }

  }

  // ================================================================================
  //
  // ================================================================================

  @Inject
  public AnnotateCommand(@Assisted OwlclCommand main) {
    super(main);
    configure();
  }

  @Override
  protected void doInitialize() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void addCommandActions(List<String> actionsList) {
    // TODO Auto-generated method stub

  }

  @Override
  public Object call() throws Exception {
    configure();

    // ================================================================================
    // sources
    // ================================================================================
    // load source ontologies
    OntologyFiles sources = new OntologyFiles(sourceFiles, !noSourceSubs);
    OWLOntologyManager smanager = getMain().getNewBaseManager();
    sources.setupManager(smanager, null);

    if (sourceIris.isEmpty())
    {
      sourceIris.addAll(sources.getLocalOntologyFiles(null).values());
    }
    Set<OWLOntology> sourceOntologies = new HashSet<OWLOntology>();
    for (IRI iri : sourceIris)
    {
      sourceOntologies.add(OwlclUtil.getOrLoadOntology(iri, smanager));
    }
    finalSourceOntologies = new HashSet<OWLOntology>(sourceOntologies);
    if (!noSourceImports)
    {
      for (OWLOntology o : sourceOntologies)
      {
        finalSourceOntologies.addAll(o.getImports());
      }
    }

    Set<String> sourceDocumentIris = new HashSet<String>();
    for (OWLOntology o : finalSourceOntologies)
    {
      sourceDocumentIris.add(smanager.getOntologyDocumentIRI(o).toString());
    }

    // ================================================================================
    // targets
    // ================================================================================
    // load target ontologies
    OntologyFiles targetOntologyFiles = new OntologyFiles(targetFiles, !noTargetSubs);
    OWLOntologyManager tmanager = getMain().getNewBaseManager();
    sources.setupManager(tmanager, null);

    if (targetIris.isEmpty())
    {
      targetIris.addAll(targetOntologyFiles.getLocalOntologyFiles(null).values());
    }
    Set<OWLOntology> targetOntologies = new HashSet<OWLOntology>();
    for (IRI iri : targetIris)
    {
      targetOntologies.add(OwlclUtil.getOrLoadOntology(iri, tmanager));
    }
    // augment with imports if needed

    finalTargetOntologies = new HashSet<OWLOntology>(targetOntologies);
    if (!noTargetImports)
    {
      for (OWLOntology o : targetOntologies)
      {
        finalTargetOntologies.addAll(o.getImports());
      }
    }

    // remove target ontologies if they are also a source
    Iterator<OWLOntology> i = finalTargetOntologies.iterator();
    while (i.hasNext())
    {
      OWLOntology o = i.next();
      String oDocumentIri = tmanager.getOntologyDocumentIRI(o).toString();
      if (sourceDocumentIris.contains(oDocumentIri))
      {
        i.remove();
        getLogger().warn("Ontology document {} is a source and a target, ignoring from targets.",
            oDocumentIri);
      }
      if (!oDocumentIri.startsWith("file:"))
      {
        i.remove();
        getLogger().warn("Ontology document {} is not a local document, ignoring from targets.",
            oDocumentIri);
      }
    }

    changedOntologies = new HashSet<OWLOntology>();
    OWLOntologyChangeListener cl = new OWLOntologyChangeListener() {

      @Override
      public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
        for (OWLOntologyChange c : changes)
        {
          changedOntologies.add(c.getOntology());
        }

      }
    };
    tmanager.addOntologyChangeListener(cl);

    // for each target ontology
    for (OWLOntology o : finalTargetOntologies)
    {
      // for each entity
      for (OWLEntity e : o.getSignature(false))
      {
        IRI iri = e.getIRI();
        // if the entity IRI is applicable
        if (processIri(iri))
        {
          // for each applicable annotation from source
          for (OWLAnnotation a : getAnnotations(iri))
          {
            // decide if the annotation should be added to this ontology
            if (isApplicable(o, iri, a))
            {
              // clean other ones?
              if (replace)
              {
                for (OWLAnnotationAssertionAxiom aaa : o.getAnnotationAssertionAxioms(iri))
                {
                  if (a.getProperty().equals(aaa.getProperty()))
                  {
                    tmanager.removeAxiom(o, aaa);
                  }
                }
              }

              // add the annotation
              tmanager.addAxiom(o,
                  tmanager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(iri, a));
            }

          }
        }
      }

    }

    for (OWLOntology o : changedOntologies)
    {
      tmanager.saveOntology(o);
    }

    return null;
  }

  private boolean isApplicable(OWLOntology o, IRI iri, OWLAnnotation a) {
    if (addToAll)
    {
      return true;
    }
    if (!properties.isEmpty() && !properties.contains(a.getProperty().getIRI()))
    {
      return false;
    }

    for (OWLAnnotationAssertionAxiom aaa : o.getAnnotationAssertionAxioms(iri))
    {
      if (aaa.getProperty().equals(a.getProperty()))
      {
        return true;
      }
    }
    return false;
  }

  private Map<IRI, Set<OWLAnnotation>> iriAnnoationsMap = new HashMap<IRI, Set<OWLAnnotation>>();

  private Set<OWLAnnotation> getAnnotations(IRI iri) {
    Set<OWLAnnotation> annotations = iriAnnoationsMap.get(iri);
    if (annotations == null)
    {
      annotations = new HashSet<OWLAnnotation>();
      for (OWLOntology o : finalSourceOntologies)
      {
        for (OWLAnnotationAssertionAxiom aaa : o.getAnnotationAssertionAxioms(iri))
        {
          annotations.add(aaa.getAnnotation());
        }
      }

      iriAnnoationsMap.put(iri, annotations);
    }
    return annotations;
  }

  private boolean processIri(IRI iri) {
    if (targetIriPatterns.isEmpty())
    {
      return true;
    }
    for (String pattern : targetIriPatterns)
    {
      if (Pattern.matches(pattern, iri.toString()))
      {
        return true;
      }
    }

    return false;
  }

}
