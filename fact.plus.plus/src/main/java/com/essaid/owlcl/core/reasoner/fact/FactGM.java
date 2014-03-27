package com.essaid.owlcl.core.reasoner.fact;

import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.essaid.owlcl.core.OwlclGModule;
import com.essaid.owlcl.util.OwlclUtil;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class FactGM extends AbstractModule implements OwlclGModule {

  @SuppressWarnings("unchecked")
  @Override
  protected void configure() {
    Class<? extends OWLReasonerFactory> cls;
    try
    {
      cls = (Class<? extends OWLReasonerFactory>) Class
          .forName("uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory");

      OwlclUtil.instance().loadNativeLibrary(null);

      bind(OWLReasonerFactory.class).annotatedWith(Names.named("Fact++")).to(cls);
    } catch (ClassNotFoundException e)
    {
      // ignore
    }

  }

}
