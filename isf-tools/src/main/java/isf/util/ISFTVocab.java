package isf.util;

import static isf.util.ISFTVocab.Vocab.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

public enum ISFTVocab {

	prefix(ISFT_PREFIX), exclude(ISFT_EXCLUDE), exclude_subs(ISFT_EXCLUDE_SUBS), include(
			ISFT_INCLUDE), include_subs(ISFT_INCLUDE_SUBS), include_instances(
			ISFT_INCLUDE_INSTANCES), iri_mapps_to(ISFT_IRI_MAPPES_TO), module_builders(
			MODULE_BUILDERS), module_file_name(MODULE_FILE_NAME), module_iri(MODULE_IRI), module_source(
			MODULE_SOURCE);

	private String vocab;

	private ISFTVocab(String vocab) {
		this.vocab = vocab;
	}

	public IRI iri() {
		return IRI.create(vocab);
	}

	public OWLAnnotationProperty getAP() {
		return OWLManager.getOWLDataFactory().getOWLAnnotationProperty(iri());
	}

	public String vocab() {
		return vocab;
	}

	// ================================================================================
	// The previous strings in the code before using the enum.
	// ================================================================================
	public static class Vocab {

		static final String ISFT_PREFIX = ISF.ISF_ONTOLOGY_IRI_PREFIX + "isftools-";
		static final String ISFT_EXCLUDE = ISFT_PREFIX + "module-exclude";
		static final String ISFT_EXCLUDE_SUBS = ISFT_PREFIX + "module-exclude-subs";
		static final String ISFT_INCLUDE = ISFT_PREFIX + "module-include";
		static final String ISFT_INCLUDE_INSTANCES = ISFT_PREFIX + "module-include-instances";
		static final String ISFT_INCLUDE_SUBS = ISFT_PREFIX + "module-include-subs";
		static final String ISFT_IRI_MAPPES_TO = ISFT_PREFIX + "iri-mappes-to";
		static final String MODULE_BUILDERS = ISFT_PREFIX + "module-builders";
		static final String MODULE_FILE_NAME = ISFT_PREFIX + "module-file-name";
		static final String MODULE_IRI = ISFT_PREFIX + "module-iri";
		static final String MODULE_SOURCE = ISFT_PREFIX + "module-source";

	}

}
