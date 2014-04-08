package com.essaid.owlcl.command.module;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

public enum ModuleVocab {

  module_unclassified_builders(ModuleConstant.MODULE_UNCLASSIFIED_BUILDERS),

  module_classified_builders(ModuleConstant.MODULE_CLASSIFIED_BUILDERS),

  module_unclassified_filename(ModuleConstant.MODULE_UNCLASSIFIED_FILENAME),

  module_classified_filename(ModuleConstant.MODULE_CLASSIFIED_FILENAME),

  module_unclassified_iri(ModuleConstant.MODULE_UNCLASSIFIED_IRI),

  module_classified_iri(ModuleConstant.MODULE_CLASSIFIED_IRI),

  module_source_exclude(ModuleConstant.MODULE_SOURCE_EXCLUDE),

  module_is_unclassified(ModuleConstant.MODULE_IS_UNCLASSIFIED),

  module_is_classified(ModuleConstant.MODULE_IS_CLASSIFIED),

  module_classified_addlegacy(ModuleConstant.MODULE_CLASSIFIED_ADDLEGACY),

  module_unclassified_addlegacy(ModuleConstant.MODULE_UNCLASSIFIED_ADDLEGACY),

  module_classified_cleanlegacy(ModuleConstant.MODULE_CLASSIFIED_CLEANLEGACY),

  module_unclassified_cleanlegacy(ModuleConstant.MODULE_UNCLASSIFIED_CLEANLEGACY),

  exclude(ModuleConstant.OWLCL_MODULE_EXCLUDE),

  exclude_subs(ModuleConstant.OWLCL_MODULE_EXCLUDE_SUBS),

  include(ModuleConstant.OWLCL_MODULE_INCLUDE),

  include_subs(ModuleConstant.OWLCL_MODULE_INCLUDE_SUBS),

  include_instances(ModuleConstant.OWLCL_MODULE_INCLUDE_INSTANCES);

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

}
