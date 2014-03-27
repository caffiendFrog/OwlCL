package com.essaid.owlcl.core.reasoner.fact;

import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;

import com.essaid.owlcl.core.OwlclGModule;
import com.essaid.owlcl.core.reasoner.IReasonerManager;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class FactGM extends AbstractModule implements OwlclGModule {

  @Override
  protected void configure() {

    bind(FactPlusPlusNativeLoader.class).asEagerSingleton();

    bind(OWLReasonerFactory.class).annotatedWith(
        Names.named(IReasonerManager.FACT_PLUS_PLUS_REASONER_FACTORY)).to(
        FaCTPlusPlusReasonerFactory.class);
  }
}
