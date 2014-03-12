package isf.module;

import isf.ISF;
import isf.ISFUtil;
import isf.module.internal.SimpleModuleBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.io.OntologyIRIMappingNotFoundException;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleModule extends AbstractModule {

	private static final Logger logger = LoggerFactory.getLogger("SimpleModule");

	private OWLOntology annotationOntology;

	private OWLOntology includeOntology;

	private OWLOntology excludeOntology;

	private OWLOntology legacyOntology;

	private OWLOntology ontology;

	// private OWLOntology sourceOntology;

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

	private final Set<OWLOntology> changedOntologies = new HashSet<OWLOntology>();

	private IRI legacyIri;

	private IRI legacyRemovedIri;

	private OWLOntology legacyRemovedOntology;

	private boolean loaded;

	private String customFileName;

	private OWLOntology mergedSource;

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
	public SimpleModule(String moduleName, OWLOntologyManager mananger, File directory,
			File outputDirectory) {
		super(moduleName, mananger, directory, outputDirectory);
		this.builder = new SimpleModuleBuilder(this);

		annotationIri = IRI.create(ISF.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ ISF.ANNOTATION_IRI_SUFFIX);
		moduleIri = IRI.create(ISF.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ ISF.MODULE_IRI_SUFFIX);
		includeIri = IRI.create(ISF.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ ISF.MODULE_INCLUDE_IRI_SUFFIX);
		excludeIri = IRI.create(ISF.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ ISF.MODULE_EXCLUDE_IRI_SUFFIX);
		legacyIri = IRI.create(ISF.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ ISF.MODULE_LEGACY_IRI_SUFFIX);
		legacyRemovedIri = IRI.create(ISF.ISF_ONTOLOGY_IRI_PREFIX + getName()
				+ ISF.MODULE_LEGACY_REMOVED_IRI_SUFFIX);

		this.changeListener = new OWLOntologyChangeListener() {

			@Override
			public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
					throws OWLException {
				Map<OWLOntology, Set<OWLOntologyChange>> changeMap = new HashMap<OWLOntology, Set<OWLOntologyChange>>(
						changes.size());
				for (OWLOntologyChange change : changes) {
					changedOntologies.add(change.getOntology());
					Set<OWLOntologyChange> ontologyChanges = changeMap.get(change.getOntology());
					if (ontologyChanges == null) {
						ontologyChanges = new HashSet<OWLOntologyChange>();
						changeMap.put(change.getOntology(), ontologyChanges);
					}
					ontologyChanges.add(change);
				}

				for (Entry<OWLOntology, Set<OWLOntologyChange>> entry : changeMap.entrySet()) {
					logger.info("Ontology changed: " + entry.getKey().getOntologyID());
					for (OWLOntologyChange change : entry.getValue()) {
						logger.debug("\t" + change.toString());
					}

				}
			}
		};

		getDefiningManager().addOntologyChangeListener(changeListener);
		ontology = createOntology(moduleIri, getOutputDirectory(), getGeneratedManager());

	}

	public boolean exists() {
		OWLOntology ontology = getDefiningManager().getOntology(annotationIri);
		if (ontology == null) {
			try {
				ontology = getDefiningManager().loadOntology(annotationIri);
			} catch (OntologyIRIMappingNotFoundException e) {
				return false;
			} catch (OWLOntologyCreationException e) {
				throw new RuntimeException(
						"Error while checking or the existence of a SimpleModule with IRI "
								+ annotationIri + " and directory: " + getDirectory(), e);
			}
		}
		logger.debug("Found module annotation ontoloyg for " + annotationIri + " located at "
				+ getDefiningManager().getOntologyDocumentIRI(ontology));

		return true;
	}

	public void create(IRI generatedFinalIri, Collection<IRI> sourceIris, boolean legacy) {
		if (!exists()) {

			// annotation ontology
			annotationOntology = createOntology(annotationIri, getDirectory(), getDefiningManager());
			// add the exclude import
			AddImport ai = new AddImport(annotationOntology, getDataFactory()
					.getOWLImportsDeclaration(excludeIri));
			getDefiningManager().applyChange(ai);
			// add the include import
			ai = new AddImport(annotationOntology, getDataFactory().getOWLImportsDeclaration(
					includeIri));
			getDefiningManager().applyChange(ai);
			// add the source imports
			for (IRI iri : sourceIris) {
				ai = new AddImport(annotationOntology, getDataFactory().getOWLImportsDeclaration(
						iri));
				getDefiningManager().applyChange(ai);
				// save the sources as ontology annotations
				OWLLiteral source = getDataFactory().getOWLLiteral(iri.toString());
				OWLAnnotation a = getDataFactory().getOWLAnnotation(
						getDataFactory().getOWLAnnotationProperty(
								IRI.create(ISF.MODULE_SOURCE_ANNOTATION_IRI)), source);
				AddOntologyAnnotation aoa = new AddOntologyAnnotation(annotationOntology, a);
				getDefiningManager().applyChange(aoa);
			}
			// add the final IRI annotation
			OWLLiteral finalIriLiteral = getDataFactory().getOWLLiteral(
					generatedFinalIri.toString());
			OWLAnnotation a = getDataFactory().getOWLAnnotation(
					getDataFactory().getOWLAnnotationProperty(
							IRI.create(ISF.MODULE_FINAL_IRI_ANNOTATION_IRI)), finalIriLiteral);
			AddOntologyAnnotation aoa = new AddOntologyAnnotation(annotationOntology, a);
			getDefiningManager().applyChange(aoa);

			// the include ontology
			includeOntology = createOntology(includeIri, getDirectory(), getDefiningManager());
			// the exclude ontology
			excludeOntology = createOntology(excludeIri, getDirectory(), getDefiningManager());

			try {
				annotationOntology.getOWLOntologyManager().saveOntology(annotationOntology);
				includeOntology.getOWLOntologyManager().saveOntology(includeOntology);
				excludeOntology.getOWLOntologyManager().saveOntology(excludeOntology);
			} catch (OWLOntologyStorageException e) {
				throw new RuntimeException(
						"Failed to save ontologies for newly created SimpleModule", e);
			}

			if (legacy) {
				legacyOntology = createOntology(legacyIri, getDirectory(), getDefiningManager());
				legacyRemovedOntology = createOntology(legacyRemovedIri, getDirectory(),
						getDefiningManager());
				try {
					legacyOntology.getOWLOntologyManager().saveOntology(legacyOntology);
					legacyRemovedOntology.getOWLOntologyManager().saveOntology(
							legacyRemovedOntology);
				} catch (OWLOntologyStorageException e) {
					throw new RuntimeException(
							"Failed to save legacy ontologies for newly created SimpleModule", e);
				}
			}

		} else {
			throw new IllegalStateException(
					"Attempting to create an already existing SimpleModule named: " + getName());
		}
	}

	public void load() {
		if (this.loaded) {
			return;
		}
		if (exists()) {
			annotationOntology = ISFUtil.getOrLoadOntology(annotationIri, getDefiningManager());
			includeOntology = ISFUtil.getOrLoadOntology(includeIri, getDefiningManager());
			excludeOntology = ISFUtil.getOrLoadOntology(excludeIri, getDefiningManager());

			for (OWLAnnotation a : annotationOntology.getAnnotations()) {
				if (a.getProperty().getIRI().toString()
						.equals(ISF.MODULE_FINAL_IRI_ANNOTATION_IRI)) {
					moduleIri = IRI.create(((OWLLiteral) a.getValue()).getLiteral());
					SetOntologyID setid = new SetOntologyID(ontology, moduleIri);
					getGeneratedManager().applyChange(setid);
				}
				if (a.getProperty().getIRI().toString()
						.equals(ISF.MODULE_FILE_NAME_ANNOTATION_IRI)) {
					customFileName = ((OWLLiteral) a.getValue()).getLiteral();
				}
				try {
					mergedSource = getDefiningManager().createOntology();
				} catch (OWLOntologyCreationException e) {
					throw new RuntimeException(
							"Failed to create merged source ontology while loading module: "
									+ getName(), e);
				}
				if (a.getProperty().getIRI().toString()
						.equals(ISF.MODULE_SOURCE_ANNOTATION_IRI)) {
					String sourceIri = ((OWLLiteral) a.getValue()).getLiteral();

					OWLImportsDeclaration id = getDataFactory().getOWLImportsDeclaration(
							IRI.create(sourceIri));
					AddImport i = new AddImport(mergedSource, id);
					logger.info("Adding source import for module: " + getName() + " imported "
							+ sourceIri);
					getDefiningManager().applyChange(i);
				}
			}

			try {
				legacyOntology = ISFUtil.getOrLoadOntology(legacyIri, getDefiningManager());
				legacyRemovedOntology = ISFUtil.getOrLoadOntology(legacyRemovedIri,
						getDefiningManager());
			} catch (Exception e) {
				logger.warn("Faild to load legacy ontologies, assuming not applicable for module: "
						+ getName(), e);
				System.err.println();
			}

		} else {
			throw new IllegalStateException(
					"Attempting to load a non-existing SimpleModule named: " + getName());
		}
	}

	@Override
	public void close() {
		// TODO do real cleanup.

	}

	@Override
	public void generateModule() throws Exception {
		logger.info("Generating module: " + getName());
		load();
		builder.run();

	}

	public OWLOntology getSourceOntology() {
		return mergedSource;
	}

	@Override
	public void addLegacyOntologies() {
		if (legacyOntology != null) {
			Set<OWLAxiom> axioms = ISFUtil.getAxioms(legacyOntology, true);
			getGeneratedManager().addAxioms(ontology, axioms);
			logger.info("Added legacy axioms for " + getName() + ", axiom count: " + axioms.size());
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
				List<OWLOntologyChange> changes = getDefiningManager().removeAxioms(o, axioms);
				logger.info("Cleaned legacy ontology: " + o.getOntologyID() + ", change count: "
						+ changes.size());

				for (OWLOntologyChange change : changes) {
					getDefiningManager().addAxiom(legacyRemovedOntology, change.getAxiom());
				}
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
	public void saveModule() throws OWLOntologyStorageException {
		logger.info("Saving module: " + getName() + " into ontology: " + ontology.getOntologyID());
		for (Module module : getImports()) {
			OWLImportsDeclaration id = getDataFactory().getOWLImportsDeclaration(module.getIri());
			AddImport i = new AddImport(getOntology(), id);
			logger.info("Adding module import for module: " + module.getName() + " imported into: "
					+ getName());
			getGeneratedManager().applyChange(i);
		}

		if (customFileName == null) {
			getGeneratedManager().saveOntology(ontology);
		} else {
			try {
				getGeneratedManager().saveOntology(ontology,
						new FileOutputStream(new File(getOutputDirectory(), customFileName)));
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Failed to save module " + getName()
						+ " with custom file name " + customFileName, e);
			}
		}

	}

	@Override
	public void saveModuleTransitive() throws OWLOntologyStorageException {
		logger.info("Saving module " + getName() + " transitively");
		for (Module module : getImports()) {
			module.saveModuleTransitive();
		}
		saveModule();

	}

	@Override
	public void saveModuleDefinitionFiles() throws OWLOntologyStorageException {
		if (changedOntologies.remove(annotationOntology)) {
			logger.info("Saving annotation ontology for module: " + getName() + " into ontology: "
					+ annotationOntology.getOntologyID());
			annotationOntology.getOWLOntologyManager().saveOntology(annotationOntology);
		}
		if (changedOntologies.remove(includeOntology)) {
			logger.info("Saving include ontology for module: " + getName() + " into ontology: "
					+ annotationOntology.getOntologyID());
			includeOntology.getOWLOntologyManager().saveOntology(includeOntology);
		}
		if (changedOntologies.remove(excludeOntology)) {
			logger.info("Saving exclude ontology for module: " + getName() + " into ontology: "
					+ annotationOntology.getOntologyID());
			excludeOntology.getOWLOntologyManager().saveOntology(excludeOntology);
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
		for (OWLOntology o : legacyOntology.getImports()) {
			if (changedOntologies.remove(o)) {
				logger.info("Module: " + getName() + " is saving legacy ontology: "
						+ o.getOntologyID());
				o.getOWLOntologyManager().saveOntology(o);
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

	// public OWLOntology getSourceOntology() {
	// return sourceOntology;
	// }

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
		return super.getReasoner();

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return moduleIri.toString() + ontology;
	}
}
