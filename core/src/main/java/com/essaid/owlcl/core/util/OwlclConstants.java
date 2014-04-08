package com.essaid.owlcl.core.util;

import org.semanticweb.owlapi.model.IRI;


public class OwlclConstants {

  public static final IRI OWLCL_ONTOLOGY_IRI = IRI
  .create("https://raw.github.com/ShahimEssaid/OwlCL/master/owl/owlcl.owl");
  public static final String OWLCL_ENTITY_IRI_PREFIX = "http://owl.essaid.com/owlcl/owlcl_";
  public static final String IRI_MAPPES_TO = OWLCL_ENTITY_IRI_PREFIX + "iri_mappes_to";

}
