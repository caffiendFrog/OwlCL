package isf.module;

import isf.ISFUtil;
import isf.module.internal.SimpleModuleBuilder;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OntologyIRIMappingNotFoundException;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;

public class SimpleModule extends AbstractModule {

	private static final Logger logger = ISFUtil.getLogger("SimpleModule");

	private OWLOntology annotationOntology;

	private OWLOntology includeOntology;

	private OWLOntology excludeOntology;

	private OWLOntology legacyOntology;

	private OWLOntology ontology;

	private OWLOntology sourceOntology;

	private final SimpleModuleBuilder builder;

	private IRI moduleIri;

	private IRI includeIri;

	public IRI getIri() {
		return moduleIri;
	}

	public IRI getIncludeIri() {
		return includeIri;
	}

	public IRI getExcludeIri() {
		return excludeIri;
	}

	public IRI getAnnotationIri() {
		return annotationIri;
	}

	private IRI excludeIri;

	private IRI annotationIri;

	private OWLOntologyChangeListener changeListener;

	/**
	 * @param moduleName
	 * @param moduleTrunkRelativePath
	 *            Can be null to use the default: src/ontology/module/moduleName
	 * @param sourceOntology
	 *            The source ontology for generating the module ontology. Any
	 *            needed ontologies should either be already loaded, or
	 *            accessible from the manager of this ontology (i.e. proper IRI
	 *            mapping is already setup in the manager).
	 * @param trunkPath
	 *            can be null if the environment variable ISF_TRUNK is set or if
	 *            the system property isf.trunk is set.
	 * @param outputPath
	 *            Can be null to use the default output of
	 *            trunk/../generated/module/moduleName
	 */
	public SimpleModule(String moduleName, String moduleTrunkRelativePath,
			OWLOntology sourceOntology, String trunkPath, String outputPath) {
		super(moduleName, moduleTrunkRelativePath, trunkPath, outputPath);

		this.sourceOntology = sourceOntology;
		init();
		this.builder = new SimpleModuleBuilder(this);

	}

	private final Set<OWLOntology> changedOntologies = new HashSet<OWLOntology>();

	private IRI legacyIri;

	private IRI legacyRemovedIri;

	private OWLOntology legacyRemovedOntology;

	private HashSet<OWLOntology> legacyOntologies;

	private void init() {
		this.changeListener = new OWLOntologyChangeListener() {

			@Override
			public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
					throws OWLException {
				Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
				for (OWLOntologyChange change : changes) {
					changedOntologies.add(change.getOntology());
					ontologies.add(change.getOntology());
				}

				for (OWLOntology o : ontologies) {
					logger.info("Ontology changed: " + o.getOntologyID());
					for (OWLOntologyChange change : changes) {
						if (change.getOntology().equals(o)) {
							logger.debug("\t" + change.toString());
						}
					}
				}

			}
		};

		ISFUtil.setupManagerMapper(getManager());
		getManager().addOntologyChangeListener(changeListener);

		annotationIri = IRI.create(ISFUtil.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ Util.ANNOTATION_IRI_SUFFIX);
		moduleIri = IRI
				.create(ISFUtil.ISF_ONTOLOGY_IRI_PREFIX + getName() + Util.MODULE_IRI_SUFFIX);
		includeIri = IRI.create(ISFUtil.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ Util.MODULE_INCLUDE_IRI_SUFFIX);
		excludeIri = IRI.create(ISFUtil.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ Util.MODULE_EXCLUDE_IRI_SUFFIX);
		legacyIri = IRI.create(ISFUtil.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ Util.MODULE_LEGACY_IRI_SUFFIX);
		legacyRemovedIri = IRI.create(ISFUtil.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ Util.MODULE_LEGACY_REMOVED_IRI_SUFFIX);

		// annotation
		try {
			annotationOntology = getManager().loadOntology(annotationIri);
			logger.debug("Loaded annotation ontnology for module: " + getName()
					+ " with axiom count: " + annotationOntology.getAxioms().size());
		} catch (OWLOntologyCreationException e1) {
			if (e1.getCause() instanceof FileNotFoundException
					|| e1 instanceof OntologyIRIMappingNotFoundException) {
				System.err
						.println("Warning: SimpleModule didn't find the module annotation file for "
								+ getName() + ". Creating a new one.");
				annotationOntology = createOntology(annotationIri, getDirectory());

				// add the exclude import
				AddImport ai = new AddImport(annotationOntology, getDataFactory()
						.getOWLImportsDeclaration(excludeIri));
				getManager().applyChange(ai);

				// add the include import
				ai = new AddImport(annotationOntology, getDataFactory().getOWLImportsDeclaration(
						includeIri));
				getManager().applyChange(ai);

				// add the ISF import
				ai = new AddImport(annotationOntology, getDataFactory().getOWLImportsDeclaration(
						ISFUtil.ISF_IRI));
				getManager().applyChange(ai);

				try {
					saveOntology(annotationOntology);
				} catch (OWLOntologyStorageException e) {
					throw new RuntimeException(
							"Failed to save initial module annotation ontology.", e);
				}
			}

			else {
				throw new IllegalStateException(
						"Failed to create initial module annotation ontology", e1);
			}

		}

		// include
		try {
			includeOntology = getManager().loadOntology(includeIri);
			logger.debug("Loaded include ontnology for module: " + getName()
					+ " with axiom count: " + includeOntology.getAxiomCount());
		} catch (OWLOntologyCreationException e1) {
			if (e1.getCause() instanceof FileNotFoundException
					|| e1 instanceof OntologyIRIMappingNotFoundException) {
				logger.warn("Warning: SimpleModule didn't find the module include file for "
						+ getName() + ". Creating a new one.");
				;
				includeOntology = createOntology(includeIri, getDirectory());
				logger.debug("Created new include ontology: " + includeOntology.getOntologyID());
				try {
					saveOntology(includeOntology);
				} catch (OWLOntologyStorageException e) {
					throw new RuntimeException("Failed to save initial module include ontology.", e);
				}
			}

			else {
				throw new RuntimeException("Failed to create initial module include ontology", e1);
			}
		}

		// exclude
		try {
			excludeOntology = getManager().loadOntology(excludeIri);
			logger.debug("Loaded exclude ontnology for module: " + getName()
					+ " with axiom count: " + excludeOntology.getAxiomCount());
		} catch (OWLOntologyCreationException e1) {
			if (e1.getCause() instanceof FileNotFoundException
					|| e1 instanceof OntologyIRIMappingNotFoundException) {
				logger.warn("Warning: SimpleModule didn't find the module exclude file for "
						+ getName() + ". Creating a new one.");
				;
				excludeOntology = createOntology(excludeIri, getDirectory());
				logger.debug("Created new include ontology: " + excludeOntology.getOntologyID());
				try {
					saveOntology(excludeOntology);
				} catch (OWLOntologyStorageException e) {
					throw new RuntimeException("Failed to save initial module exclude ontology.", e);
				}
			}

			else {
				throw new RuntimeException("Failed to create initial module exclude ontology", e1);
			}
		}

		// legacy and the saving file, if they exists
		try {
			legacyOntology = getManager().loadOntology(legacyIri);
			legacyOntologies = new HashSet<OWLOntology>(legacyOntology.getImportsClosure());
			logger.debug("Loaded legacy ontnology for module: " + getName());
			for (OWLOntology o : legacyOntology.getImportsClosure()) {
				logger.debug("\t Legacy ontology: " + o.getOntologyID() + " with axiom count: "
						+ o.getAxiomCount());
			}
			try {
				legacyRemovedOntology = getManager().loadOntology(legacyRemovedIri);
				logger.debug("Loaded legacy removed ontnology for module: " + getName()
						+ " with axiom count: " + legacyRemovedOntology.getAxiomCount());
			} catch (OWLOntologyCreationException e1) {
				if (e1.getCause() instanceof FileNotFoundException
						|| e1 instanceof OntologyIRIMappingNotFoundException) {
					logger.warn("Warning: SimpleModule didn't find the module legacyRemoved file for "
							+ getName() + ". Creating a new one.");
					;
					legacyRemovedOntology = createOntology(legacyRemovedIri, getDirectory());
					logger.debug("Created new include ontology: "
							+ legacyRemovedOntology.getOntologyID());
					try {
						saveOntology(legacyRemovedOntology);
					} catch (OWLOntologyStorageException e) {
						throw new RuntimeException(
								"Failed to save initial module exclude ontology.", e);
					}
				} else {
					throw new RuntimeException(
							"Failed to create initial module legacy-removed  ontology", e1);
				}
			}

		} catch (OWLOntologyCreationException e1) {
			logger.info("Could not load lagacy ontology for " + legacyIri);

		}

		ontology = createOntology(moduleIri, getOutputDirectory());
	}

	@Override
	public void generateModule() throws Exception {
		logger.info("Generating module: " + getName());
		builder.run();

	}

	@Override
	public void addLegacyOntologies() {
		if (legacyOntology != null) {
			Set<OWLAxiom> axioms = ISFUtil.getAxioms(legacyOntology, true);
			getManager().addAxioms(ontology, axioms);
			logger.info("Added legacy axioms for " + getName() + ", axiom count: " + axioms.size());
			// TODO do the debug logging
		}
	}

	@Override
	public void addLegacyOntologiesTransitive() {
		logger.info("Adding transitive legacy axioms for " + getName());
		for (Module module : getImports()) {
			module.addLegacyOntologiesTransitive();
		}
		addLegacyOntologies();
	}

	@Override
	public void cleanLegacyOntologies() {
		if (legacyOntology != null) {
			for (OWLOntology o : legacyOntology.getImportsClosure()) {
				Set<OWLAxiom> axioms = ontology.getAxioms();
				logger.info("Cleaning legacy ontology: " + o.getOntologyID() + ", axiom count: "
						+ axioms.size());
				getManager().removeAxioms(o, axioms);
				// TODO do the debug log
			}
		}
	}

	@Override
	public void cleanLegacyOntologiesTransitive() {
		logger.info("Cleaning transitive legacy axioms for " + getName());
		for (Module module : getImports()) {
			module.cleanLegacyOntologiesTransitive();
		}
		cleanLegacyOntologies();
	}

	@Override
	public void generateModuleTransitive() throws Exception {
		logger.info("Generating module transitive for: " + getName());
		for (Module module : getImports()) {
			module.generateModuleTransitive();
		}
		generateModule();
	}

	@Override
	public void addImport(Module module) {
		OWLImportsDeclaration id = getDataFactory().getOWLImportsDeclaration(module.getIri());
		AddImport i = new AddImport(getOntology(), id);
		logger.info("Adding module import for module: " + module.getName() + " imported into: "
				+ getName());
		getManager().applyChange(i);
		super.addImport(module);
	}

	@Override
	public void removeImport(Module module) {

		OWLImportsDeclaration id = getDataFactory().getOWLImportsDeclaration(module.getIri());
		RemoveImport i = new RemoveImport(getOntology(), id);
		logger.info("Removing module import for module: " + module.getName()
				+ " was imported into: " + getName());
		getManager().applyChange(i);
		super.removeImport(module);
	}

	@Override
	public void saveGeneratedModule() throws OWLOntologyStorageException {
		logger.info("Saving module: " + getName() + " into ontology: " + ontology.getOntologyID());
		saveOntology(ontology);

	}

	@Override
	public void saveGeneratedModuleTransitive() throws OWLOntologyStorageException {
		logger.info("Saving module transitively");
		for (Module module : getImports()) {
			module.saveGeneratedModuleTransitive();
		}
		saveGeneratedModule();

	}

	@Override
	public void saveModuleDefinitionFiles() throws OWLOntologyStorageException {
		if (changedOntologies.remove(annotationOntology)) {
			logger.info("Saving annotation ontology for module: " + getName() + " into ontology: "
					+ annotationOntology.getOntologyID());
			saveOntology(annotationOntology);
		}
		if (changedOntologies.remove(includeOntology)) {
			logger.info("Saving include ontology for module: " + getName() + " into ontology: "
					+ annotationOntology.getOntologyID());
			saveOntology(includeOntology);
		}
		if (changedOntologies.remove(excludeOntology)) {
			logger.info("Saving exclude ontology for module: " + getName() + " into ontology: "
					+ annotationOntology.getOntologyID());
			saveOntology(excludeOntology);
		}
	}

	@Override
	public void saveModuleDefinitionFilesTransitive() throws OWLOntologyStorageException {
		logger.info("Saving definition files transitively for: " + getName());
		for (Module module : getImports()) {
			module.saveModuleDefinitionFilesTransitive();
		}
		saveModuleDefinitionFiles();
	}

	@Override
	public void saveLegacyOntologies() throws OWLOntologyStorageException {
		for (OWLOntology o : legacyOntologies) {
			if (changedOntologies.remove(o)) {
				logger.info("Module: " + getName() + " is saving legacy ontology: "
						+ o.getOntologyID());
				saveOntology(o);
			}
		}
	}

	@Override
	public void saveLegacyOntologiesTransitive() throws OWLOntologyStorageException {
		logger.info("Saving legacy ontologies transitively for module: " + getName());
		for (Module module : getImports()) {
			module.saveLegacyOntologies();
		}
		saveLegacyOntologies();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	public OWLOntology getAnnotationOntology() {
		return annotationOntology;
	}

	public OWLOntology getExcludeOntology() {
		return excludeOntology;
	}

	public OWLOntology getIncludeOntology() {
		return includeOntology;
	}

	public OWLOntology getOntology() {

		return ontology;
	}

	public OWLOntology getSourceOntology() {
		return sourceOntology;
	}

	public void setAnnotationOntology(OWLOntology annotationOntology) {
		this.annotationOntology = annotationOntology;
	}

	public void setExcludeOntology(OWLOntology excludeOntology) {
		this.excludeOntology = excludeOntology;
	}

	public void setIncludeOntology(OWLOntology includeOntology) {
		this.includeOntology = includeOntology;
	}

	public void setOntology(OWLOntology moduleOntology) {
		this.ontology = moduleOntology;
	}

	@Override
	public OWLReasoner getReasoner() {
		OWLReasoner r = super.getReasoner();
		if (r == null) {
			r = ISFUtil.getDefaultReasoner(getSourceOntology());
		}

		return r;
	}

	public static void main(String[] args) throws Exception {
		String moduleName = args[0];
		String trunkPath = null;
		if (args.length > 1) {
			trunkPath = args[1];
		}
		String outputPath = null;
		if (args.length > 2) {
			outputPath = args[2];
		}

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology sourceOntology = ISFUtil.setupAndLoadIsfOntology(man);
		SimpleModule module = new SimpleModule(moduleName, null, sourceOntology, trunkPath,
				outputPath);
		module.generateModule();
		module.saveGeneratedModule();
		module.saveModuleDefinitionFiles();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return moduleIri.toString() + ontology;
	}
}
