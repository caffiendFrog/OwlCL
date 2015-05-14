package com.essaid.owlcl.command.module.builder.simple;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import com.essaid.owlcl.command.module.IModule;
import com.essaid.owlcl.command.module.Util;
import com.essaid.owlcl.core.util.IReportFactory;
import com.essaid.owlcl.core.util.OwlclUtil;
import com.essaid.owlcl.core.util.Report;
import com.google.inject.Inject;

public class SimpleInferredModuleBuilder extends AbstractSimpleModuleBuilder {

  @Inject
  private IReportFactory reportFactory;

  private Set<IRI> excludeParentIris;

  public SimpleInferredModuleBuilder(IModule simeplModule) {
    super(simeplModule);
  }

  public void run() throws Exception {

    excludeParentIris =
        Util.getExcludeParentIris(module.getModuleConfiguration().getConfigurationOntology(),
            module.getModuleConfiguration().getSourceOntology(), true);

    report.info("");
    report.info("==========================================================");
    report.info("======== Generating inferred simple module "
        + module.getModuleConfiguration().getName() + "================");
    report.info("===========================================================");
    report.info("");

    if (module.getModuleConfiguration().getSourceReasoner().getUnsatisfiableClasses().getEntities()
        .size() > 0) {
      report.info("");
      report.info("Unsatisfied entities:");
      for (OWLEntity entity : module.getModuleConfiguration().getSourceReasoner()
          .getUnsatisfiableClasses().getEntities()) {
        report.info("\t" + entity);
      }
      report.info("Unsatisfieds: "
          + module.getModuleConfiguration().getSourceReasoner().getUnsatisfiableClasses()
              .getEntities());
    }

    report.info("Doing includes: ");
    addIncludes();
    report.info("Doing include subs: ");
    addIncludeSubs();

    report.info("Doing include instances");
    addIncludeInstances();

    report.info("Doing excludes: ");
    removeExcludes();
    report.info("Doing exclude subs: ");
    removeExcludeSubs();

    removeExcludeParents();

    report.info("Adding parents to BFO: ");
    addClosureToBfo();

    report.info("Adding annotations: ");
    addAnnotations();

    report.info("Typing all entities: ");
    typeAllEntities();

    addOntologyAnnotations();

  }

  private void removeExcludeParents() {
    // TODO Auto-generated method stub

  }

  public void addIncludes() {
    Set<OWLEntity> entities =
        Util.getIncludeEntities(module.getModuleConfiguration().getConfigurationOntology(), module
            .getModuleConfiguration().getSourceOntology(), false);

    for (OWLEntity e : entities) {
      addAxiom(module.getDataFactory().getOWLDeclarationAxiom(e));
      addAxioms(getDefiningAxioms(e, module.getModuleConfiguration().getSourceOntology(), true));
    }

  }

  public void addIncludeSubs() {
    Set<OWLEntity> entities =
        Util.getIncludeSubsEntities(module.getModuleConfiguration().getConfigurationOntology(),
            module.getModuleConfiguration().getSourceOntology(), false);
    // report.info("Found sub annotations for: " + entities);
    Set<OWLEntity> closureEntities = new HashSet<OWLEntity>();
    OWLReasoner r = module.getModuleConfiguration().getSourceReasoner();

    for (OWLEntity e : entities) {
      Set<OWLEntity> subEntities = OwlclUtil.getSubs(e, true, r);
      closureEntities.addAll(subEntities);
    }
    for (OWLEntity e : closureEntities) {
      addAxiom(df.getOWLDeclarationAxiom(e));
      addAxioms(getDefiningAxioms(e, module.getModuleConfiguration().getSourceOntology(), true));
    }
  }

  private void addIncludeInstances() {
    Set<OWLEntity> entities =
        Util.getIncludeInstances(module.getModuleConfiguration().getConfigurationOntology(), module
            .getModuleConfiguration().getSourceOntology(), false);
    for (OWLEntity e : entities) {
      if (e instanceof OWLClass) {
        OWLClass classInstances = (OWLClass) e;
        Set<OWLIndividual> instances =
            classInstances.getIndividuals(module.getModuleConfiguration().getSourceOntology()
                .getImportsClosure());
        for (OWLIndividual i : instances) {
          if (i instanceof OWLNamedIndividual) {
            OWLNamedIndividual ni = (OWLNamedIndividual) i;
            addAxioms(getDefiningAxioms(ni, module.getModuleConfiguration().getSourceOntology(),
                true));
          }
        }
      }
    }
  }

  public void removeExcludes() {
    Set<OWLEntity> entities =
        Util.getExcludeEntities(module.getModuleConfiguration().getConfigurationOntology(), module
            .getModuleConfiguration().getSourceOntology(), false);
    for (OWLEntity entity : entities) {
      removeAxiom(df.getOWLDeclarationAxiom(entity));
      removeAxioms(getDefiningAxioms(entity, module.getModuleConfiguration().getSourceOntology(),
          true));

      if (entity instanceof OWLClass) {
        OWLClass c = (OWLClass) entity;
        Set<OWLClass> subs =
            module.getModuleConfiguration().getSourceReasoner().getSubClasses(c, true)
                .getFlattened();
        for (OWLClass sub : subs) {
          OWLSubClassOfAxiom subAxiom = df.getOWLSubClassOfAxiom(sub, c);
          if (module.getBuildersClassified().containsAxiom(subAxiom)) {
            removeAxiom(subAxiom);;
            for (OWLClass supr : module.getModuleConfiguration().getSourceReasoner()
                .getSuperClasses(c, true).getFlattened()) {
              if (module.getBuildersClassified().containsClassInSignature(supr.getIRI())) {
                addAxiom(df.getOWLSubClassOfAxiom(sub, supr));
              }
            }
          }
        }

      }
    }

  }

  public void removeExcludeSubs() {
    Set<OWLEntity> entities =
        Util.getExcludeSubsEntities(module.getModuleConfiguration().getConfigurationOntology(),
            module.getModuleConfiguration().getSourceOntology(), false);
    // report.info("Excluding class: " + entities);
    Set<OWLEntity> entityiesClosure = new HashSet<OWLEntity>();
    for (OWLEntity entity : entities) {
      entityiesClosure.addAll(OwlclUtil.getSubs(entity, true, module.getModuleConfiguration()
          .getSourceReasoner()));
    }
    entityiesClosure.removeAll(entities);
    // report.info("Excluding class closure: " + entityiesClosure);
    for (OWLEntity entity : entityiesClosure) {
      removeAxiom(df.getOWLDeclarationAxiom(entity));
      removeAxioms(getDefiningAxioms(entity, module.getModuleConfiguration().getSourceOntology(),
          true));
    }

  }

  public void addClosureToBfo() {
    for (OWLEntity entity : module.getBuildersClassified().getSignature()) {
      Set<OWLEntity> supers =
          OwlclUtil.getSupers(entity, true, module.getModuleConfiguration().getSourceReasoner(),
              Util.getExcludeParentIris(module.getModuleConfiguration().getConfigurationOntology(),
                  module.getModuleConfiguration().getSourceConfigurationOntology(), true));
      for (final OWLEntity supr : supers) {
        // if (!supr.getIRI().toString().contains("BFO_")) {
        Set<OWLAxiom> axioms =
            getDefiningAxioms(supr, module.getModuleConfiguration().getSourceOntology(), true);
        for (OWLAxiom axiom : axioms) {
          axiom.accept(new OWLAxiomVisitorAdapter() {

            @Override
            public void visit(OWLSubClassOfAxiom axiom) {
              if (axiom.getSubClass() instanceof OWLClass
                  && axiom.getSubClass().asOWLClass().getIRI().equals(supr.getIRI())) {

                if (axiom.getSuperClass() instanceof OWLClass) {
                  addAxiom(axiom);
                }
              }
            }
            // TODO the other types of entities
          });
        }
        // }
      }
    }

  }

  public void addAnnotations() {
    Set<OWLEntity> entitiesToAnnotate = new HashSet<OWLEntity>();
    entitiesToAnnotate.addAll(module.getBuildersClassified().getSignature());

    Set<OWLEntity> annotatedEntities = new HashSet<OWLEntity>();

    while (entitiesToAnnotate.size() > 0) {
      Set<OWLEntity> newEntities = new HashSet<OWLEntity>();
      Iterator<OWLEntity> i = entitiesToAnnotate.iterator();
      while (i.hasNext()) {
        OWLEntity entity = i.next();
        i.remove();
        annotatedEntities.add(entity);
        Set<OWLAnnotationAssertionAxiom> axioms =
            OwlclUtil.getSubjectAnnotationAxioms(module.getModuleConfiguration()
                .getSourceOntology(), true, entity.getIRI());
        addAxioms(axioms);
        for (OWLAnnotationAssertionAxiom a : axioms) {
          Set<OWLEntity> signature = a.getSignature();
          signature.removeAll(annotatedEntities);
          newEntities.addAll(signature);
        }
      }

      entitiesToAnnotate.addAll(newEntities);
    }

  }

  public void typeAllEntities() {
    for (OWLEntity e : module.getBuildersClassified().getSignature()) {
      addAxiom(df.getOWLDeclarationAxiom(e));
    }

  }

  private void addAxioms(Set<? extends OWLAxiom> axioms) {
    for (OWLAxiom axiom : axioms) {
      addAxiom(axiom);
    }
  }

  private void addAxiom(OWLAxiom axiom) {

    if (axiom instanceof OWLDeclarationAxiom) {
      OWLDeclarationAxiom da = (OWLDeclarationAxiom) axiom;
      if (da.getEntity().getIRI().equals(OWLRDFVocabulary.OWL_NOTHING.getIRI())) {
        return;
      }
      if (excludeParentIris.contains(da.getEntity().getIRI())) {
        return;
      }
    }
    if (!removedAxioms.contains(axiom)) {
//      if(axiom instanceof OWLSubClassOfAxiom){
//        OWLSubClassOfAxiom sca = (OWLSubClassOfAxiom) axiom;
//        if(sca.getSuperClass() instanceof OWLClass && excludeParentIris.contains(((OWLClass)sca.getSuperClass()).getIRI())){
//          return;
//        }
//      }
      
      for(OWLEntity entity : axiom.getSignature()){
        if(excludeParentIris.contains(entity.getIRI())){
          getLogger().info("Ecluding axiom {} due IRI {} in exclude_parent.", axiom, entity.getIRI());
          return;
        }
      }
      
      module.addAxiomClassified(axiom, this);
    }
  }

  private void removeAxioms(Set<? extends OWLAxiom> axioms) {
    for (OWLAxiom axiom : axioms) {
      removeAxiom(axiom);
    }
  }

  Set<OWLAxiom> removedAxioms = new HashSet<OWLAxiom>();
  private Report report;

  private void removeAxiom(OWLAxiom axiom) {
    module.removeAxiomClassified(axiom, this);
    removedAxioms.add(axiom);
  }

  @Override
  public void build(IModule module, boolean inferred) {
    try {
      run();
    } catch (Exception e) {
      throw new RuntimeException("Error while building inferred simple module "
          + module.getModuleConfiguration().getName(), e);
    }

  }

  @Override
  public void buildFinished(IModule module) {
    // TODO Auto-generated method stub

  }

  @Override
  public void initialize() {
    this.report = module.getReportClassified();
  }

}
