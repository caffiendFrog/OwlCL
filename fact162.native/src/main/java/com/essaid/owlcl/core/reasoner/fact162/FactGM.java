package com.essaid.owlcl.core.reasoner.fact162;

import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;

import com.essaid.owlcl.core.OwlclGuiceModule;
import com.essaid.owlcl.core.reasoner.IReasonerManager;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class FactGM extends AbstractModule implements OwlclGuiceModule {

  @Override
  protected void configure() {

    bind(FactPlusPlusNativeLoader.class).asEagerSingleton();

    bind(OWLReasonerFactory.class).annotatedWith(
        Names.named(IReasonerManager.FACT_PLUS_PLUS_FACTORY_BINDING_NAME)).to(
        FaCTPlusPlusReasonerFactory.class);
  }
}
