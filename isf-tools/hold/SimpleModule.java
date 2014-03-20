package isf.module;

import isf.module.builder.ModuleBuilder;
import isf.util.ISFTUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleModule extends DefaultModule {

	private static final Logger logger = LoggerFactory.getLogger("SimpleModule");

	// ================================================================================
	// Reviewed end
	// ================================================================================

	private OWLOntologyChangeListener changeListener;

	private final Set<OWLOntology> changedOntologies = new HashSet<OWLOntology>();

	private boolean loaded;

	private String customFileName;

	private OWLOntology mergedSource;

	private List<String> builderNames = new ArrayList<String>();

	private OWLOntology moduleInferred;

	private String customInferredFileName;

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
	public SimpleModule(String moduleName, File moduleDirectory,
			OWLOntologyManager sourcesMananger, File outputDirectory, boolean addLegacy,
			boolean cleanLegacy) {
		super(moduleName, moduleDirectory, sourcesMananger, outputDirectory, addLegacy, cleanLegacy);
		// this.builder = new SimpleModuleBuilder(this);

		// moduleIri = IRI.create(ISFT.ISF_ONTOLOGY_IRI_PREFIX + getName() +
		// ISFT.MODULE_IRI_SUFFIX);
		// moduleInferredIri = IRI.create(ISFT.ISF_ONTOLOGY_IRI_PREFIX +
		// getName()
		// + ISFT.MODULE_IRI_INRERRED_SUFFIX);

		this.changeListener = new OWLOntologyChangeListener() {

			@Override
			public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
					throws OWLException {
				// Map<OWLOntology, Set<OWLOntologyChange>> changeMap = new
				// HashMap<OWLOntology, Set<OWLOntologyChange>>(
				// changes.size());
				for (OWLOntologyChange change : changes)
				{
					changedOntologies.add(change.getOntology());
					// Set<OWLOntologyChange> ontologyChanges =
					// changeMap.get(change.getOntology());
					// if (ontologyChanges == null)
					// {
					// ontologyChanges = new HashSet<OWLOntologyChange>();
					// changeMap.put(change.getOntology(), ontologyChanges);
					// }
					// ontologyChanges.add(change);
				}

				// for (Entry<OWLOntology, Set<OWLOntologyChange>> entry :
				// changeMap.entrySet())
				// {
				// logger.info("Ontology changed: " +
				// entry.getKey().getOntologyID());
				// for (OWLOntologyChange change : entry.getValue())
				// {
				// logger.debug("\t" + change.toString());
				// }
				//
				// }
			}
		};

		getModuleManager().addOntologyChangeListener(changeListener);

	}

	public SimpleModule(String moduleName, File moduleDirectory, OWLOntologyManager sourcesManager,
			File outputDirectory) {
		this(moduleName, moduleDirectory, sourcesManager, outputDirectory, false, false);
	}

}
