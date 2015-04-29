package com.essaid.owlcl.command.module.builder.simple;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

import com.essaid.owlcl.command.module.ModuleConstant;

public enum MBSimpleVocab {
  exclude(ModuleConstant.OWLCL_MODULE_EXCLUDE), exclude_parent(ModuleConstant.OWLCL_MODULE_EXCLUDE_PARENT), exclude_subs(ModuleConstant.OWLCL_MODULE_EXCLUDE_SUBS), include(ModuleConstant.OWLCL_MODULE_INCLUDE), include_subs(
      ModuleConstant.OWLCL_MODULE_INCLUDE_SUBS), include_instances(ModuleConstant.OWLCL_MODULE_INCLUDE_INSTANCES);

  private String vocab;

  private MBSimpleVocab(String value) {
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
  }
}
