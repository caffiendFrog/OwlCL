package isf.util;

import org.semanticweb.owlapi.model.IRI;

public class ISF {

	/**
	 * The system property that points to the trunk/master (not the parent of
	 * trunk in Google Code SVN) checkout of the ISF repository
	 */
	public static final String ISF_TRUNK_PROPERTY = "isf.trunk";
	public static final String ISF_GENERATED_DIRECTORY_PROPERTY = "isf.generated";
	public static final String ISF_ONTOLOGY_IRI_PREFIX = "http://purl.obolibrary.org/obo/arg/";
	public static final IRI ISF_DEV_IRI = IRI.create(ISF_ONTOLOGY_IRI_PREFIX + "isf-dev.owl");
	public static final IRI ISF_DEV_REASONED_IRI = IRI.create(ISF_ONTOLOGY_IRI_PREFIX
			+ "isf-dev-reasoned.owl");
	public static final IRI ISF_FULL_DEV_IRI = IRI.create(ISF_ONTOLOGY_IRI_PREFIX
			+ "isf-full-dev.owl");
	public static final IRI ISF_FULL_DEV_REASONED_IRI = IRI.create(ISF_ONTOLOGY_IRI_PREFIX
			+ "isf-full-dev-reasoned.owl");
	public static final String ISF_MAPPING_SUFFIX = "-mapping.owl";
	public static final IRI ISF_SKOS_IRI = IRI.create(ISF_ONTOLOGY_IRI_PREFIX + "isf-skos.owl");
	public static final String ANNOTATION_IRI_SUFFIX = "-module-annotation.owl";
	public static final String MODULE_IRI_SUFFIX = "-module.owl";
	public static final String MODULE_INCLUDE_IRI_SUFFIX = "-module-include.owl";
	public static final String MODULE_EXCLUDE_IRI_SUFFIX = "-module-exclude.owl";
	public static final String MODULE_LEGACY_IRI_SUFFIX = "-module-legacy.owl";
	public static final String MODULE_LEGACY_REMOVED_IRI_SUFFIX = "-module-legacy-removed.owl";

}
