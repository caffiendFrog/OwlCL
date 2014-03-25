package com.essaid.owlcl.command;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.beust.jcommander.CommandResult;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.essaid.owlcl.command.cli.CanonicalFileConverter;
import com.essaid.owlcl.command.cli.IriConverter;
import com.essaid.owlcl.command.cli.ManualIriMapping;
import com.essaid.owlcl.mapping.DefaultMapping;
import com.essaid.owlcl.mapping.Mapper;
import com.essaid.owlcl.mapping.Mapping;
import com.essaid.owlcl.util.OwlclUtil;
import com.essaid.owlcl.util.OntologyFiles;

@Parameters(commandNames = "map", commandDescription = "Mapps IRIs from one IRI to another.")
public class MapperCommand extends AbstractCommand<CommandResult> {

	// ================================================================================
	// The IRIs to map
	// ================================================================================

	@Parameter(names = "-fromIris", description = "The IRIs to map from. If not set, all "
			+ "possible mappings will be applied.", converter = IriConverter.class)
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

	private List<IRI> iris = null;
	private boolean irisSet = false;

	// ================================================================================
	// The IRI prefixs to map
	// ================================================================================

	@Parameter(names = "-iriPrefixes", description = "The IRI prefixes to find IRIs to map.")
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

	private List<String> prefixes = null;
	private boolean prefixesSet = false;

	// ================================================================================
	// IRI patterns
	// ================================================================================

	@Parameter(names = "-iriPatterns", description = "Regex patterns for finding IRIs to map.")
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

	private List<String> patterns = null;
	private boolean patternsSet = false;

	// ================================================================================
	// Transitive mapping?
	// ================================================================================

	@Parameter(names = "-transitive", arity = 1,
			description = "If a mapping iriA -> iriB -> iriC exists "
					+ "should iriA be mapped to iriC (transitive to a final IRI). True by default.")
	public void setMapTransitively(boolean mapTransitively) {
		this.mapTransitively = mapTransitively;
		this.mapTransitivelySet = true;
	}

	public boolean isMapTransitively() {
		return mapTransitively;
	}

	public boolean isMapTransitivelySet() {
		return mapTransitivelySet;
	}

	private boolean mapTransitively = true;
	private boolean mapTransitivelySet = false;

	// ================================================================================
	// The ontology IRIs that have the mapping definitions
	// ================================================================================

	@Parameter(names = "-mapIris", description = "The IRIs for the  OWL files that "
			+ "define the mappings. ", converter = IriConverter.class)
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

	private List<IRI> mappingIris = null;
	private boolean mappingIrisSet = false;

	// ================================================================================
	// Mapping files and/or folders
	// ================================================================================

	@Parameter(names = "-mapFiles", description = "The paths to mapping files and/or folders.",
			converter = CanonicalFileConverter.class)
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
					+ "There has to be at least one white space before and after the => ")
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
			converter = IriConverter.class)
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
	// The files and/or folders for the OWL files that will be mapped.
	// ================================================================================

	@Parameter(names = "-ontologyFiles",
			description = "The files and/or folders to OWL files that will be modified. "
					+ "If not set, all OWL files in any specified folders will be mapped.",
			required = true, converter = CanonicalFileConverter.class)
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

	@Parameter(names = "-ontologySubFiles", arity = 1,
			description = "If a file from the -ontologyFiles is a directory, "
					+ "should subdirectories be considered? True by default.")
	public void setOntologySubFiles(boolean ontologySubFiles) {
		this.ontologySubFiles = ontologySubFiles;
		this.ontologySubFilesSet = true;
	}

	public boolean isOntologySubFiles() {
		return ontologySubFiles;
	}

	public boolean isOntologySubFilesSet() {
		return ontologySubFilesSet;
	}

	private boolean ontologySubFiles = true;
	private boolean ontologySubFilesSet = false;

	// ================================================================================
	// Initialization
	// ================================================================================

	protected void configure() {

	}

	// ================================================================================
	// Implementation
	// ================================================================================
	public MapperCommand(MainCommand main) {
		super(main);
		configure();
	}

	@Override
	protected void addCommandActions(List<String> actionsList) {
		actionsList.add(Action.map.name());
	}

	OntologyFiles ontologyFilesFinder;
	OWLOntologyManager man;
	OntologyFiles mappingFilesFinder;
	Mapping mapping;

	public void run() {
		man = getMain().getNewBaseManager();

		ontologyFilesFinder = new OntologyFiles(ontologyFiles, ontologySubFiles);

		Map<File, Exception> exceptions = new HashMap<File, Exception>();
		ontologyFilesFinder.setupManager(man, exceptions);

		for (IRI iri : ontologyIris)
		{
			OwlclUtil.getOrLoadOntology(iri, man);
		}
		for (IRI iri : ontologyFilesFinder.getLocalOntologyFiles(exceptions).values())
		{
			OwlclUtil.getOrLoadOntology(iri, man);
		}

		DefaultMapping defaultMapping = new DefaultMapping();
		mappingFilesFinder = new OntologyFiles(mappingFiles, false);
		for (IRI iri : mappingIris)
		{
			OwlclUtil.getOrLoadOntology(iri, man);
			defaultMapping.addMappingOntologies(man.getOntology(iri).getImportsClosure());
		}
		for (IRI iri : mappingFilesFinder.getLocalOntologyFiles(exceptions).values())
		{
			OwlclUtil.getOrLoadOntology(iri, man);
			defaultMapping.addMappingOntologies(man.getOntology(iri).getImportsClosure());
		}
		for (ManualIriMapping mm : manualMappings)
		{
			defaultMapping.addMapping(mm.fromIri, mm.toIri);
		}
		mapping = defaultMapping;

		final Set<OWLOntology> changed = new HashSet<OWLOntology>();
		man.addOntologyChangeListener(new OWLOntologyChangeListener() {

			@Override
			public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
					throws OWLException {
				for (OWLOntologyChange c : changes)
				{
					changed.add(c.getOntology());
				}

			}
		});

		// do the actions.
		for (String acion : getAllActions())
		{
			Action.valueOf(acion).execute(this);
		}

		for (OWLOntology o : changed)
		{
			try
			{
				man.saveOntology(o);
			} catch (OWLOntologyStorageException e)
			{
				throw new RuntimeException("Failed to save ontology: "
						+ o.getOntologyID().getOntologyIRI(), e);
			}
		}

	}

	// ================================================================================
	// Actions
	// ================================================================================

	enum Action {
		map {

			@Override
			public void execute(MapperCommand command) {
				Mapper mapper = new Mapper(command.mapping);
				if (!command.irisSet && !command.prefixesSet && !command.patternsSet)
				{
					// this means map all possible IRIs
					for (IRI iri : command.mapping.getForwardMappedIris())
					{
						mapIri(iri, command, mapper);
					}

				} else
				{
					// iris
					for (IRI iri : command.iris)
					{
						mapIri(iri, command, mapper);
					}
					// prefixes
					for (String prefix : command.prefixes)
					{
						for (IRI ontologyIri : command.ontologyIris)
						{
							mapper.forwardMapPrefix(prefix, command.mapTransitively,
									command.man.getOntology(ontologyIri), true);
						}
						for (IRI ontologyIri : command.ontologyFilesFinder.getLocalOntologyFiles(
								null).values())
						{
							mapper.forwardMapPrefix(prefix, command.mapTransitively,
									command.man.getOntology(ontologyIri), true);
						}

					}
					// patterns
					for (String pattern : command.patterns)
					{
						for (IRI ontologyIri : command.ontologyIris)
						{

							mapper.forwardMapPattern(pattern, command.mapTransitively,
									command.man.getOntology(ontologyIri), true);
						}
						for (IRI ontologyIri : command.ontologyFilesFinder.getLocalOntologyFiles(
								null).values())
						{
							mapper.forwardMapPattern(pattern, command.mapTransitively,
									command.man.getOntology(ontologyIri), true);
						}
					}
				}
			}

			private void mapIri(IRI iri, MapperCommand command, Mapper mapper) {
				for (IRI ontologyIri : command.ontologyIris)
				{
					mapper.forwardMap(iri, command.mapTransitively,
							command.man.getOntology(ontologyIri), true);
				}
				for (IRI ontologyIri : command.ontologyFilesFinder.getLocalOntologyFiles(null)
						.values())
				{
					mapper.forwardMap(iri, command.mapTransitively,
							command.man.getOntology(ontologyIri), true);
				}
			}
		};

		abstract public void execute(MapperCommand command);
	}

  @Override
  public CommandResult call() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
