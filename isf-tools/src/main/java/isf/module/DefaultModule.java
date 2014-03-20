package isf.module;

import isf.command.AbstractCommand.Report;
import isf.module.builder.ModuleBuilder;
import isf.module.builder.ModuleBuilderManager;
import isf.util.ISFT;
import isf.util.ISFTUtil;
import isf.util.OWLOntologyWrapper;
import isf.util.RuntimeOntologyLoadingException;

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
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultModule implements Module {

	private static Logger logger = LoggerFactory.getLogger(DefaultModule.class);

	private String name;
	private File moduleDirectory;

	private IRI configurationIri;
	private OWLOntology configurationOntology;

	private IRI includeIri;
	private OWLOntology includeOntology;

	private IRI excludeIri;
	private OWLOntology excludeOntology;

	private IRI legacyIri;
	private OWLOntology legacyOntology;

	private IRI legacyRemovedIri;
	private OWLOntology legacyRemovedOntology;

	private IRI generatedModuleIri;
	private OWLOntology generatedModule;
	private String generatedModuleFileName;

	private IRI generatedModuleInferredIri;
	private OWLOntology generatedModuleInferred;
	private String generatedModuleInferredFileName;

	private OWLOntologyManager moduleManager;
	private OWLDataFactory moduleDataFactory;
	private File outputDirectory;

	private OWLOntologyManager genManager;
	private OWLDataFactory genDataFactory;

	private OWLOntologyManager sourcesManager;
	private OWLOntology sourceOntology;
	private OWLReasoner sourceReasoner;

	private List<String> builderNames = new ArrayList<String>();
	private Set<ModuleBuilder> builders = new HashSet<ModuleBuilder>();

	private Map<Module, Boolean> generateImports = new HashMap<Module, Boolean>();
	private Map<Module, Boolean> generateInferredImports = new HashMap<Module, Boolean>();
	private Map<Module, Boolean> bothImports = new HashMap<Module, Boolean>();

	private boolean legacySupport;
	private boolean addLegacy;
	private boolean cleanLegacy;

	private boolean loaded;

	private boolean generate;
	private boolean generateInferred;

	private boolean localSources;

	private boolean localReasoner;

	public DefaultModule(String moduleName, File moduleDirectory, OWLOntology sourceOntology,
			OWLReasoner sourceReasoner, File outputDirectory, boolean addLegacy, boolean cleanLegacy) {
		this(moduleName, moduleDirectory, sourceOntology.getOWLOntologyManager(), outputDirectory,
				addLegacy, cleanLegacy);
		this.sourceOntology = sourceOntology;
		this.sourceReasoner = sourceReasoner;
	}

	public DefaultModule(String moduleName, File moduleDirectory,
			OWLOntologyManager sourcesManager, File outputDirectory, boolean addLegacy,
			boolean cleanLegacy) {

		if (moduleName == null || moduleDirectory == null || outputDirectory == null)
		{
			throw new IllegalStateException("Module name, directory, or output cannot be null.");
		}
		this.name = moduleName;
		this.moduleDirectory = moduleDirectory;
		this.sourcesManager = sourcesManager;

		this.outputDirectory = outputDirectory;
		this.outputDirectory.mkdirs();

		this.moduleManager = OWLManager.createOWLOntologyManager();
		this.moduleManager.clearIRIMappers();
		this.moduleManager.setSilentMissingImportsHandling(true);
		this.moduleManager.addIRIMapper(new AutoIRIMapper(moduleDirectory, false));
		this.moduleDataFactory = this.moduleManager.getOWLDataFactory();

		this.genManager = OWLManager.createOWLOntologyManager();
		this.genDataFactory = genManager.getOWLDataFactory();

		this.addLegacy = addLegacy;
		this.cleanLegacy = cleanLegacy;

		configurationIri = IRI.create(ISFT.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ ISFT.CONFIGURATION_IRI_SUFFIX);
		includeIri = IRI.create(ISFT.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ ISFT.MODULE_INCLUDE_IRI_SUFFIX);
		excludeIri = IRI.create(ISFT.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ ISFT.MODULE_EXCLUDE_IRI_SUFFIX);
		legacyIri = IRI.create(ISFT.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ ISFT.MODULE_LEGACY_IRI_SUFFIX);
		legacyRemovedIri = IRI.create(ISFT.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ ISFT.MODULE_LEGACY_REMOVED_IRI_SUFFIX);

		generatedModuleIri = IRI.create(ISFT.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ ISFT.MODULE_IRI_SUFFIX);
		generatedModuleInferredIri = IRI.create(ISFT.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ ISFT.MODULE_IRI_INRERRED_SUFFIX);
		generatedModuleFileName = moduleName + ISFT.MODULE_IRI_SUFFIX;
		generatedModuleInferredFileName = moduleName + ISFT.MODULE_IRI_INRERRED_SUFFIX;
	}

	public void loadConfiguration() {
		if (this.loaded)
		{
			return;
		}
		configurationOntology = ISFTUtil.getOrLoadOntology(configurationIri, moduleManager);
		excludeOntology = ISFTUtil.getOrLoadOntology(excludeIri, moduleManager);
		includeOntology = ISFTUtil.getOrLoadOntology(includeIri, moduleManager);

		try
		{
			legacyOntology = ISFTUtil.getOrLoadOntology(legacyIri, moduleManager);
			legacyRemovedOntology = ISFTUtil.getOrLoadOntology(legacyRemovedIri, moduleManager);
			this.legacySupport = true;
		} catch (RuntimeOntologyLoadingException e)
		{
			if (e.isIriMapping())
			{
				logger.info("Legacy not supported for module: " + getName());
			}
		}

		Set<IRI> excludedIris = new HashSet<IRI>();

		for (OWLAnnotation a : configurationOntology.getAnnotations())
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
				for (int i = 0; i > names.length; ++i)
				{
					builderNames.add(names[i].toLowerCase().trim());
				}
			}

			if (a.getProperty().equals(ModuleVocab.module_generate.getAP()))
			{
				generate = true;
			}

			if (a.getProperty().equals(ModuleVocab.module_generate_inferred.getAP()))
			{
				generateInferred = true;
			}
		}

		if (this.sourceOntology == null)
		{
			sourceOntology = ISFTUtil.createOntology(
					IRI.create("http://isf-tools/" + getName() + "-merged-module-sources.owl"),
					sourcesManager);
			for (OWLOntology o : configurationOntology.getImports())
			{
				if (!excludedIris.contains(o.getOntologyID().getOntologyIRI()))
				{
					sourcesManager.addAxioms(sourceOntology, o.getAxioms());
				}
			}
			this.localSources = true;
			this.initialized = true;
		}

		if (this.sourceReasoner == null)
		{
			this.sourceReasoner = ISFTUtil.getReasoner(sourceOntology);
			this.localReasoner = true;
			this.initialized = true;
		}

	}

	@Override
	public void importModuleIntoGenerated(Module module, Boolean inferred) {
		generateImports.put(module, inferred);
		setGenerationType(module, inferred);
	}

	private void setGenerationType(Module module, Boolean inferred) {
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

	@Override
	public void importModuleIntoGeneratedInferred(Module module, Boolean inferred) {
		generateInferredImports.put(module, inferred);
		setGenerationType(module, inferred);
	}

	@Override
	public void importModuleIntoBoth(Module module, Boolean inferred) {
		bothImports.put(module, inferred);
		setGenerationType(module, inferred);
	}

	private Report report;

	private boolean initialized;

	private boolean disposed;

	private boolean generated;

	private boolean saved;

	@Override
	public void addBuilder(ModuleBuilder builder) {
		builders.add(builder);

	}

	@Override
	public Set<ModuleBuilder> getBuilders() {
		return builders;
	}

	@Override
	public boolean equals(Object obj) {

		// TODO: should it be possible to have multiple modules with same name
		// during runtime? This equality is probably too strong.
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof Module)
		{
			return this.name.equals(((Module) obj).getName());
		}
		return false;
	}

	public OWLOntologyManager getModuleManager() {
		return moduleManager;
	}

	public OWLOntologyManager getGenerationManager() {
		return this.genManager;
	}

	@Override
	public IRI getModuleIri() {
		return generatedModuleIri;
	}

	@Override
	public IRI getModuleIriInferred() {
		return generatedModuleInferredIri;
	}

	public File getModuleDirectory() {
		return moduleDirectory;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public OWLReasoner getSourceReasoned() {
		return sourceReasoner;
	}

	@Override
	public void generateModule() {
		if (generated)
		{
			return;
		}
		generated = true;

		loadConfiguration();
		logger.info("Generating module: " + getName());

		generatedModule = ISFTUtil.createOntology(generatedModuleIri, genManager);
		generatedModuleInferred = ISFTUtil.createOntology(generatedModuleInferredIri, genManager);

		for (String builderName : builderNames)
		{
			ModuleBuilder builder = ModuleBuilderManager.instance.getBuilder(builderName, this);
			// the builder should do the right thing based on whether or not
			// this is generated or generateInferred
			builder.build(this);
			builders.add(builder);
		}

		Iterator<ModuleBuilder> i = builders.iterator();
		while (i.hasNext())
		{
			ModuleBuilder builder = i.next();
			i.remove();
			builder.buildFinished(this);
		}

		if (legacySupport)
		{
			if (cleanLegacy)
			{
				if (legacyOntology != null)
				{
					Set<OWLAxiom> axioms = generatedModule.getAxioms();
					axioms.addAll(generatedModuleInferred.getAxioms());

					for (OWLOntology o : legacyOntology.getImportsClosure())
					{
						List<OWLOntologyChange> changes = getModuleManager()
								.removeAxioms(o, axioms);
						logger.info("Cleaned legacy ontology: " + o.getOntologyID()
								+ ", change count: " + changes.size());

						for (OWLOntologyChange change : changes)
						{
							getModuleManager().addAxiom(legacyRemovedOntology, change.getAxiom());
						}
					}
				}
			}
			if (addLegacy)
			{
				Set<OWLAxiom> axioms = ISFTUtil.getAxioms(legacyOntology, true);
				if (generate)
				{
					genManager.addAxioms(generatedModule, axioms);
				}
				if (generateInferred)
				{
					genManager.addAxioms(generatedModuleInferred, axioms);
				}
			}

		}

		Set<Module> imports = new HashSet<Module>();
		imports.addAll(generateImports.keySet());
		imports.addAll(generateInferredImports.keySet());
		imports.addAll(bothImports.keySet());

		for (Module module : imports)
		{
			module.generateModule();
		}

	}

	private void addImport(String moduleName, IRI iri, OWLOntology ontology) {
		OWLImportsDeclaration id = genDataFactory.getOWLImportsDeclaration(iri);
		AddImport i = new AddImport(ontology, id);
		logger.info("Adding generated module import for module: " + moduleName
				+ " imported into generated: " + getName());
		genManager.applyChange(i);
	}

	@Override
	public void saveGeneratedModule() {
		if (saved)
		{
			return;
		}
		saved = true;

		// Set<Entry<Module, Boolean>> mergedImports = new HashSet<>();
		Set<Module> allImportedModules = new HashSet<Module>();

		if (generate)
		{
			allImportedModules.addAll(bothImports.keySet());
			allImportedModules.addAll(generateImports.keySet());

			for (Entry<Module, Boolean> entry : bothImports.entrySet())
			{
				if (entry.getValue() == null || entry.getValue() == false)
				{
					// import matching type
					addImport(entry.getKey().getName(), entry.getKey().getModuleIri(),
							generatedModule);
				} else if (entry.getValue() == false)
				{
					addImport(entry.getKey().getName(), entry.getKey().getModuleIriInferred(),
							generatedModule);
				}
			}

			for (Entry<Module, Boolean> entry : generateImports.entrySet())
			{
				if (entry.getValue() == false)
				{
					addImport(entry.getKey().getName(), entry.getKey().getModuleIri(),
							generatedModule);
				} else if (entry.getValue() == true)
				{
					addImport(entry.getKey().getName(), entry.getKey().getModuleIriInferred(),
							generatedModule);
				}
			}

			try
			{
				logger.info("Saving module: " + getName() + " into ontology: "
						+ generatedModule.getOntologyID() + " in  directory: "
						+ getOutputDirectory() + " and file: " + generatedModuleFileName);

				genManager.saveOntology(generatedModule, new FileOutputStream(new File(
						getOutputDirectory(), generatedModuleFileName)));
			} catch (OWLOntologyStorageException | FileNotFoundException e)
			{
				throw new RuntimeException("Failed to save module generated " + getName()
						+ " with file name " + generatedModuleFileName, e);
			}

		}

		if (generateInferred)
		{
			allImportedModules.addAll(bothImports.keySet());
			allImportedModules.addAll(generateInferredImports.keySet());

			for (Entry<Module, Boolean> entry : bothImports.entrySet())
			{
				if (entry.getValue() == null || entry.getValue() == true)
				{
					addImport(entry.getKey().getName(), entry.getKey().getModuleIriInferred(),
							generatedModuleInferred);
				} else
				{
					addImport(entry.getKey().getName(), entry.getKey().getModuleIri(),
							generatedModuleInferred);
				}
			}

			for (Entry<Module, Boolean> entry : generateInferredImports.entrySet())
			{
				if (entry.getValue() == true)
				{
					addImport(entry.getKey().getName(), entry.getKey().getModuleIriInferred(),
							generatedModuleInferred);
				} else
				{
					addImport(entry.getKey().getName(), entry.getKey().getModuleIri(),
							generatedModuleInferred);

				}
			}

			try
			{
				logger.info("Saving module: " + getName() + " into ontology: "
						+ generatedModuleInferred.getOntologyID() + " in  directory: "
						+ getOutputDirectory() + " and file: " + generatedModuleInferredFileName);

				genManager.saveOntology(generatedModuleInferred, new FileOutputStream(new File(
						getOutputDirectory(), generatedModuleInferredFileName)));
			} catch (FileNotFoundException | OWLOntologyStorageException e)
			{
				throw new RuntimeException("Failed to save module inferred " + getName()
						+ " with file name " + generatedModuleInferredFileName, e);
			}

		}

		for (Module module : allImportedModules)
		{
			module.saveGeneratedModule();
		}

	}

	@Override
	public void saveModuleConfiguration() {

		// TODO: do this only for changed ontologies ???
		try
		{
			moduleManager.saveOntology(configurationOntology);

			moduleManager.saveOntology(includeOntology);
			moduleManager.saveOntology(excludeOntology);
			if (legacySupport)
			{
				if (cleanLegacy)
				{
					moduleManager.saveOntology(legacyOntology);
					moduleManager.saveOntology(legacyRemovedOntology);
				}
			}
		} catch (OWLOntologyStorageException e)
		{
			throw new RuntimeException("Failed to save module configuration: " + getName(), e);
		}

		for (Module module : generateImports.keySet())
		{
			module.saveModuleConfiguration();
		}

		for (Module module : generateInferredImports.keySet())
		{
			module.saveModuleConfiguration();
		}
	}

	@Override
	public void dispose() {
		// TODO: review
		this.disposed = true;
		if (localSources)
		{
			sourcesManager.removeOntology(sourceOntology);
		}

		if (localReasoner)
		{
			ISFTUtil.disposeReasoner(sourceReasoner);
		}

	}

	@Override
	public void setGenerate(boolean generate) {
		this.generate = generate;
	}

	@Override
	public boolean isGenerate() {
		return generate;
	}

	@Override
	public void setGenerateInferred(boolean generateInferred) {
		this.generateInferred = generateInferred;
	}

	@Override
	public boolean isGenerateInferred() {
		return generateInferred;
	}

	@Override
	public OWLOntology getGeneratedModule() {
		throw new UnsupportedOperationException();
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	@Override
	public Report getReport() {
		return report;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	public void setReasoner(OWLReasoner reasoner) {
		this.sourceReasoner = reasoner;
	}

	public void setReport(Report report) {
		this.report = report;

	}

	@Override
	public OWLOntology getGeneratedModuleInferred() {
		return new OWLOntologyWrapper(generatedModuleInferred);
	}

	@Override
	public OWLOntology getSource() {
		return new OWLOntologyWrapper(sourceOntology);
	}

	@Override
	public void addModuleAnnotation(OWLAnnotation annotation) {

		genManager.applyChange(new AddOntologyAnnotation(generatedModule, annotation));

	}

	@Override
	public void removeModuleAnnotation(OWLAnnotation annotation) {
		genManager.applyChange(new RemoveOntologyAnnotation(generatedModule, annotation));

	}

	@Override
	public void addModuleAnnotations(Set<OWLAnnotation> annotations) {
		for (OWLAnnotation a : annotations)
		{
			addModuleAnnotation(a);
		}
	}

	@Override
	public void removeModuleAnnotations(Set<OWLAnnotation> annotations) {
		for (OWLAnnotation a : annotations)
		{
			removeModuleAnnotation(a);
		}

	}

	@Override
	public void addModuleAnnotationInferred(OWLAnnotation annotation) {
		genManager.applyChange(new AddOntologyAnnotation(generatedModuleInferred, annotation));

	}

	@Override
	public void removeModuleAnnotationInferred(OWLAnnotation annotation) {
		genManager.applyChange(new RemoveOntologyAnnotation(generatedModuleInferred, annotation));

	}

	@Override
	public void addModuleAnnotationsInferred(Set<OWLAnnotation> annotations) {
		for (OWLAnnotation a : annotations)
		{
			addModuleAnnotationInferred(a);
		}

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
	public void removeAxiom(OWLAxiom axiom) {
		genManager.removeAxiom(generatedModule, axiom);

	}

	@Override
	public void addAxioms(Set<OWLAxiom> axioms) {
		for (OWLAxiom a : axioms)
		{
			addAxiom(a);
		}

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
	public void removeAxiomInferred(OWLAxiom axiom) {
		genManager.removeAxiom(generatedModuleInferred, axiom);

	}

	@Override
	public void addAxiomsInferred(Set<OWLAxiom> axioms) {
		for (OWLAxiom a : axioms)
		{
			addAxiomInferred(a);
		}

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
		return moduleDataFactory;
	}

	@Override
	public OWLOntology getModuleConfiguration() {
		return new OWLOntologyWrapper(configurationOntology);
	}

}