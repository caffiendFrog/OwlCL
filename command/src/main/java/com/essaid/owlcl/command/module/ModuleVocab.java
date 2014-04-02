package com.essaid.owlcl.command.module;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

import static com.essaid.owlcl.command.module.ModuleVocab.Constant.*;

public enum ModuleVocab {

  module_builders(MODULE_BUILDERS), module_inferred_builders(MODULE_INFERRED_BUILDERS), module_file_name(
      MODULE_FILE_NAME), module_file_name_inferred(MODULE_FILE_NAME_INFERRED), module_iri(
      MODULE_IRI), module_iri_inferred(MODULE_IRI_INFERRED), module_source_exclude(
      MODULE_SOURCE_EXCLUDE), module_generate(MODULE_GENERATE), module_generate_inferred(
      MODULE_GENERATE_INFERRED), module_add_legacy(MODULE_ADD_LEGACY), module_clean_legacy(
      MODULE_CLEAN_LEGACY);

  private String vocab;

  private ModuleVocab(String value) {
    this.vocab = value;
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

  public static class Constant {

    static final String ISFT_PREFIX = Owlcl.ISF_ONTOLOGY_IRI_PREFIX + "isftools-";
    static final String MODULE_BUILDERS = ISFT_PREFIX + "module-builders";
    static final String MODULE_INFERRED_BUILDERS = ISFT_PREFIX + "module-builders-inferred";
    static final String MODULE_FILE_NAME = ISFT_PREFIX + "module-file-name";
    static final String MODULE_GENERATE = ISFT_PREFIX + "module-generate";
    static final String MODULE_GENERATE_INFERRED = ISFT_PREFIX + "module-generate-inferred";
    static final String MODULE_FILE_NAME_INFERRED = ISFT_PREFIX + "module-file-name-inferred";
    static final String MODULE_IRI = ISFT_PREFIX + "module-iri";
    static final String MODULE_IRI_INFERRED = ISFT_PREFIX + "module-iri-inferred";
    static final String MODULE_SOURCE_EXCLUDE = ISFT_PREFIX + "module-source-exclude";
    static final String MODULE_ADD_LEGACY = ISFT_PREFIX + "module-add-legacy";
    static final String MODULE_CLEAN_LEGACY = ISFT_PREFIX + "module-clean-legacy";

  }
}
