package com.essaid.owlcl.command;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.essaid.owlcl.command.mapping.DefaultMappings;
import com.essaid.owlcl.command.mapping.Mapper;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.annotation.InjectLogger;
import com.essaid.owlcl.core.cli.util.CanonicalFileConverter;
import com.essaid.owlcl.core.cli.util.IriConverter;
import com.essaid.owlcl.core.cli.util.ManualIriMapping;
import com.essaid.owlcl.core.util.IReportFactory;
import com.essaid.owlcl.core.util.OntologyFiles;
import com.essaid.owlcl.core.util.OwlclUtil;
import com.essaid.owlcl.core.util.Report;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@Parameters(commandNames = "map", commandDescription = "Mapps IRIs from one IRI to another.")
public class MapperCommand extends AbstractCommand {

  // ================================================================================
  // The IRIs to map
  // ================================================================================

  @Parameter(names = "-fromIris", description = "The IRIs to map from. If not set, all "
      + "possible mappings will be applied.", converter = IriConverter.class, variableArity = true)
  public void setIris(List<IRI> iris) {
    this.iris = iris;
    this.irisSet = true;
  }

  public List<IRI> getIris() {
    return iris;
  }

  public boolean isIrisSet() {
    return irisSet;
  }

  private List<IRI> iris = new ArrayList<IRI>();
  private boolean irisSet = false;

  // ================================================================================
  // The IRI prefixs to map
  // ================================================================================

  @Parameter(names = "-iriPrefixes", description = "The IRI prefixes to find IRIs to map.",
      variableArity = true)
  public void setPrefixes(List<String> prefixes) {
    this.prefixes = prefixes;
    this.prefixesSet = true;
  }

  public List<String> getPrefixes() {
    return prefixes;
  }

  public boolean isPrefixesSet() {
    return prefixesSet;
  }

  private List<String> prefixes = new ArrayList<String>();
  private boolean prefixesSet = false;

  // ================================================================================
  // IRI patterns
  // ================================================================================

  @Parameter(names = "-iriPatterns", description = "Regex patterns for finding IRIs to map.",
      variableArity = true)
  public void setPatterns(List<String> patterns) {
    this.patterns = patterns;
    this.patternsSet = true;
  }

  public List<String> getPatterns() {
    return patterns;
  }

  public boolean isPatternsSet() {
    return patternsSet;
  }

  private List<String> patterns = new ArrayList<String>();
  private boolean patternsSet = false;

  // ================================================================================
  // Transitive mapping?
  // ================================================================================

  @Parameter(names = "-notTransitive", description = "If a mapping iriA -> iriB -> iriC exists "
      + "should iriA be mapped to iriC (transitive to a final IRI). Transitive by default.")
  public void setNoTransitive(boolean notTransitive) {
    this.notTransitive = notTransitive;
    this.notTransitiveSet = true;
  }

  public boolean isNotTransitive() {
    return notTransitive;
  }

  public boolean isNotTransitiveSet() {
    return notTransitiveSet;
  }

  private boolean notTransitive;
  private boolean notTransitiveSet;

  // ================================================================================
  // The ontology IRIs that have the mapping definitions
  // ================================================================================

  @Parameter(names = "-mappingFileIris", description = "The IRIs for the  OWL files that "
      + "define the mappings. ", converter = IriConverter.class, variableArity = true)
  public void setMappingIris(List<IRI> mappingIris) {
    this.mappingIris = mappingIris;
    this.mappingIrisSet = true;
  }

  public List<IRI> getMappingIris() {
    return mappingIris;
  }

  public boolean isMappingIrisSet() {
    return mappingIrisSet;
  }

  private List<IRI> mappingIris = new ArrayList<IRI>();
  private boolean mappingIrisSet = false;

  // ================================================================================
  // Mapping files and/or folders
  // ================================================================================

  @Parameter(names = "-mapFiles", description = "The paths to mapping files and/or folders.",
      converter = CanonicalFileConverter.class, variableArity = true)
  public void setMappingFiles(List<File> mappingFiles) {
    this.mappingFiles = mappingFiles;
    this.mappingFilesSet = true;
  }

  public List<File> getMappingFiles() {
    return mappingFiles;
  }

  public boolean isMappingFilesSet() {
    return mappingFilesSet;
  }

  private List<File> mappingFiles = new ArrayList<File>();
  private boolean mappingFilesSet = false;

  // ================================================================================
  // Manual mappings
  // ================================================================================

  @Parameter(names = "-mappings",
      description = "Manual mappings in the form of someIri => someOtherIri. "
          + "There has to be at least one white space before and after the => ",
      variableArity = true)
  public void setManualMappings(List<ManualIriMapping> manualMappings) {
    this.manualMappings = manualMappings;
    this.manualMappingsSet = true;
  }

  public List<ManualIriMapping> getManualMappings() {
    return manualMappings;
  }

  public boolean isManualMappingsSet() {
    return manualMappingsSet;
  }

  private List<ManualIriMapping> manualMappings = new ArrayList<ManualIriMapping>();
  private boolean manualMappingsSet = false;

  // ================================================================================
  // The ontologies that will be mapped
  // ================================================================================

  @Parameter(names = "-ontologyIris",
      description = "The IRIs of ontologies that will be modified. "
          + "If not set, all OWL files in any specified folders will be mapped.",
      converter = IriConverter.class, variableArity = true)
  public void setOntologyIris(List<IRI> ontologyIris) {
    this.ontologyIris = ontologyIris;
    this.ontologyIrisSet = true;
  }

  public List<IRI> getOntologyIris() {
    return ontologyIris;
  }

  public boolean isOntologyIrisSet() {
    return ontologyIrisSet;
  }

  private List<IRI> ontologyIris = new ArrayList<IRI>();
  private boolean ontologyIrisSet = false;

  // ================================================================================
  // also map imports?
  // ================================================================================

  public void setNoMapImports() {
    this.noMapImports = true;
    this.noMapImportsSet = true;

  }

  public boolean isNoMapImports() {
    return this.noMapImports;
  }

  public boolean isNoMapImportsSet() {
    return this.noMapImportsSet;
  }

  private boolean noMapImports;
  private boolean noMapImportsSet;

  // ================================================================================
  // The files and/or folders for the OWL files that will be mapped.
  // ================================================================================

  @Parameter(names = "-ontologyFiles",
      description = "The files and/or folders to OWL files that will be modified. "
          + "If not set, all OWL files in any specified folders will be mapped.", required = true,
      converter = CanonicalFileConverter.class)
  public void setOntologyFiles(List<File> ontologyFiles) {
    this.ontologyFiles = ontologyFiles;
    this.ontologyFilesSet = true;
  }

  public List<File> getOntologyFiles() {
    return ontologyFiles;
  }

  public boolean isOntologyFilesSet() {
    return ontologyFilesSet;
  }

  private List<File> ontologyFiles = new ArrayList<File>();
  private boolean ontologyFilesSet = false;

  // ================================================================================
  // ontology subfiles
  // ================================================================================

  @Parameter(names = "-noSubFiles",
      description = "If a file from the -ontologyFiles is a directory, "
          + "should subdirectories be considered? True by default.")
  public void setNoSubFiles(boolean ontologySubFiles) {
    this.noSubFiles = ontologySubFiles;
    this.noSubFilesSet = true;
  }

  public boolean isNoSubFiles() {
    return noSubFiles;
  }

  public boolean isNoSubFilesSet() {
    return noSubFilesSet;
  }

  private boolean noSubFiles = false;
  private boolean noSubFilesSet = false;

  // ================================================================================
  // Initialization
  // ================================================================================

  protected void configure() {

    MainCommand main = getMain();

    if (!isManualMappingsSet())
    {
      if (main.getProject() != null)
      {
        this.mappingFiles.add(main.getProject());
      }
    }

    if (!isOntologyFilesSet())
    {
      if (main.getProject() != null)
      {
        this.ontologyFiles.add(getMain().getProject());
      }
    }

    if (!isMappingFilesSet())
    {
      if (main.getProject() != null)
      {
        mappingFiles.add(getMain().getProject());
      }
    }

  }

  // ================================================================================
  // Implementation
  // ================================================================================

  @InjectLogger
  private Logger logger;

  @Inject
  IReportFactory reportFactory;

  @Inject
  public MapperCommand(@Assisted OwlclCommand main) {
    super(main);
    configure();
  }

  @Override
  protected void doInitialize() {
    mappedReport = reportFactory.createReport("MappedIrisReport", getMain().getJobDirectory()
        .toPath(), this);
  }

  @Override
  protected void addCommandActions(List<String> actionsList) {
    // actionsList.add(Action.map.name());
  }

  OntologyFiles ontologyFilesFinder;
  OWLOntologyManager ontologyManager;
  OntologyFiles mappingFilesFinder;
  private Mapper mapper;

  private Report mappedReport;

  private Map<IRI, IRI> appliedMappings = new HashMap<IRI, IRI>();

  private void map(IRI fromIri, OWLOntology ontology) {
    Set<IRI> newIris = mapper.getForwardIri(fromIri, !notTransitive);
    if (newIris == null || newIris.size() != 1)
    {
      logger.warn(
          "Mapping for IRI {} skipped becuase mappings are null, 0,  mulitiple. Mappings: ",
          newIris == null ? "null" : newIris.size());
      return;
    }

    IRI newIri = newIris.iterator().next();
    List<OWLOntologyChange> changes = mapper.map(fromIri, newIri, ontology, !noMapImports);

    changes = ontologyManager.applyChanges(changes);
    if (changes.size() > 0)
    {
      mappedReport.info(fromIri.toString() + "," + newIri);
      for (OWLOntologyChange c : changes)
      {
        mappedReport.detail("\t" + c.toString());
      }
      mappedReport.detail("");
      logger.info("IRI {} was mapped to {} with {} ontology changes.", fromIri, newIri,
          changes.size());
      IRI toIri = appliedMappings.get(fromIri);
      if (toIri == null)
      {
        appliedMappings.put(fromIri, newIri);

      } else
      {
        if (!toIri.equals(newIri))
        {
          throw new IllegalStateException("IRI mappings are not consistent.\n" + "The IRI "
              + fromIri + " was mapped to " + toIri + " while now being mapped to " + newIri);
        }
      }
    }
  }

  @Override
  public Object call() throws Exception {
    configure();

    ontologyManager = getMain().getNewBaseManager();

    ontologyFilesFinder = new OntologyFiles(ontologyFiles, !noSubFiles);
    ontologyFilesFinder.setupManager(ontologyManager, null);

    DefaultMappings defaultMapping = new DefaultMappings();
    OWLOntologyManager mappingManager = getMain().getNewBaseManager();
    mappingFilesFinder = new OntologyFiles(mappingFiles, false);
    mappingFilesFinder.setupManager(mappingManager, null);

    for (IRI iri : mappingIris)
    {
      defaultMapping.addMappingOntologies(OwlclUtil.getOrLoadOntology(iri, mappingManager)
          .getImportsClosure());
    }

    for (ManualIriMapping mm : manualMappings)
    {
      defaultMapping.addMapping(mm.fromIri, mm.toIri);
    }

    // do mapping

    // watch for ontology changes to only save changed ones
    final Set<OWLOntology> changed = new HashSet<OWLOntology>();
    ontologyManager.addOntologyChangeListener(new OWLOntologyChangeListener() {

      @Override
      public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
        for (OWLOntologyChange c : changes)
        {
          changed.add(c.getOntology());
        }
      }
    });

    mapper = new Mapper(defaultMapping);

    Set<IRI> ontologyIrisToMap = new HashSet<IRI>();
    if (ontologyIris.size() > 0)
    {
      ontologyIrisToMap.addAll(ontologyIris);
    } else
    {
      ontologyIrisToMap.addAll(ontologyFilesFinder.getLocalOntologyFiles(null).values());
    }

    boolean mappAll = false;
    if (iris.size() == 0 && prefixes.size() == 0 && patterns.size() == 0)
    {
      mappAll = true;
    }

    for (IRI iri : ontologyIrisToMap)
    {
      OWLOntology o = OwlclUtil.getOrLoadOntology(iri, ontologyManager);

      if (mappAll)
      {
        for (IRI mappedIri : defaultMapping.getBackwardMappedIris())
        {
          map(mappedIri, o);
        }
      } else
      {
        // do specific IRIs
        for (IRI mappedIri : iris)
        {
          map(mappedIri, o);
        }

        for (String prefix : prefixes)
        {
          for (IRI mappedIri : mapper.getPatternIris(prefix + ".*", o, !noMapImports))
          {
            map(mappedIri, o);
          }
        }

        for (String pattern : patterns)
        {
          for (IRI mappedIri : mapper.getPatternIris(pattern, o, !noMapImports))
          {
            map(mappedIri, o);
          }
        }
      }
    }

    // save
    for (OWLOntology o : changed)
    {
      try
      {
        ontologyManager.saveOntology(o);
      } catch (OWLOntologyStorageException e)
      {
        throw new RuntimeException(
            "Failed to save ontology: " + o.getOntologyID().getOntologyIRI(), e);
      }
    }

    FileWriter writer = new FileWriter(new File(getMain().getJobDirectory(), "AppliedMappings.txt"));
    for (Entry<IRI, IRI> e : appliedMappings.entrySet())
    {
      writer.write(e.getKey().toString() + "," + e.getValue() + "\n");
    }
    writer.close();

    mappedReport.finish();

    return null;
  }

}
