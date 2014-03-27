package com.essaid.owlcl.module;

import org.semanticweb.owlapi.model.IRI;

public class Owlcl {
  



	public static final String ISFT_PREFIX = Owlcl.ISF_ONTOLOGY_IRI_PREFIX + "isftools-";
	
	public static final String ISF_ONTOLOGY_IRI_PREFIX = "http://purl.obolibrary.org/obo/arg/";
	public static final IRI ISF_DEV_IRI = IRI.create(ISF_ONTOLOGY_IRI_PREFIX + "isf-dev.owl");
	public static final IRI ISF_TOOLS_IRI = IRI.create(ISF_ONTOLOGY_IRI_PREFIX + "isf-tools.owl");
	public static final IRI ISF_DEV_REASONED_IRI = IRI.create(ISF_ONTOLOGY_IRI_PREFIX
			+ "isf-dev-reasoned.owl");
	public static final IRI ISF_FULL_DEV_IRI = IRI.create(ISF_ONTOLOGY_IRI_PREFIX
			+ "isf-full-dev.owl");
	public static final IRI ISF_FULL_DEV_REASONED_IRI = IRI.create(ISF_ONTOLOGY_IRI_PREFIX
			+ "isf-full-dev-reasoned.owl");
	public static final String ISF_MAPPING_SUFFIX = "-mapping.owl";
	public static final IRI ISF_SKOS_IRI = IRI.create(ISF_ONTOLOGY_IRI_PREFIX + "isf-skos.owl");
	public static final String CONFIGURATION_IRI_SUFFIX = "-module-configuration.owl";
	public static final String TOP_IRI_SUFFIX = "-module-top.owl";
	public static final String MODULE_IRI_SUFFIX = "-module.owl";
	public static final String MODULE_IRI_INRERRED_SUFFIX = "-module-inferred.owl";
	public static final String MODULE_INCLUDE_IRI_SUFFIX = "-module-include.owl";
	public static final String MODULE_EXCLUDE_IRI_SUFFIX = "-module-exclude.owl";
	public static final String MODULE_LEGACY_IRI_SUFFIX = "-module-legacy.owl";
	public static final String MODULE_LEGACY_REMOVED_IRI_SUFFIX = "-module-legacy-removed.owl";
	public static final String MODULE_TOP_IRI_SUFFIX = "-module-top.owl";

}
