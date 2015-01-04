package com.essaid.owlcl.command.mapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLOntology;

import com.essaid.owlcl.core.util.OwlclUtil;

public class DefaultMappings implements IMappings {

  private OWLAnnotationProperty mappingProperty = com.essaid.owlcl.core.util.OwlclVocab.iri_mapps_to
      .getAP();

  private Map<IRI, Set<IRI>> forwardMap = new HashMap<IRI, Set<IRI>>();
  private Map<IRI, Set<IRI>> backwardMap = new HashMap<IRI, Set<IRI>>();

  public void clearMappings() {
    forwardMap = new HashMap<IRI, Set<IRI>>();
    backwardMap = new HashMap<IRI, Set<IRI>>();
  }

  public void addMapping(IRI fromIri, IRI toIri) {
    Set<IRI> forwardMappings = forwardMap.get(fromIri);
    if (forwardMappings == null)
    {
      forwardMappings = new HashSet<IRI>();
      forwardMap.put(fromIri, forwardMappings);
    }
    forwardMappings.add(toIri);

    addBackwardMapping(toIri, fromIri);

  }

  private void addBackwardMapping(IRI fromIri, IRI toIri) {
    Set<IRI> backwardMappings = backwardMap.get(fromIri);
    if (backwardMappings == null)
    {
      backwardMappings = new HashSet<IRI>();
      backwardMap.put(fromIri, backwardMappings);
    }
    backwardMappings.add(toIri);
  }

  public void addMappingOntology(OWLOntology ontology) {
    for (OWLAnnotationAssertionAxiom aaa : OwlclUtil.getAnnotationAssertionAxioms(ontology,
        mappingProperty, true))
    {
      OWLAnnotationSubject subject = aaa.getSubject();
      if (subject instanceof IRI)
      {
        IRI subjectIRI = (IRI) subject;

        OWLAnnotationValue value = aaa.getValue();
        if (value instanceof IRI)
        {
          IRI valueIri = (IRI) value;

          addMapping(subjectIRI, valueIri);
        }
      }
    }

  }

  public void addMappingOntologies(Set<OWLOntology> ontologies) {
    for (OWLOntology o : ontologies)
    {
      addMappingOntology(o);
    }
  }

  @Override
  public Set<IRI> getForwardMappings(IRI iri) {
    return forwardMap.get(iri);
  }

  @Override
  public Set<IRI> getBackwardMappings(IRI iri) {
    return backwardMap.get(iri);
  }

  @Override
  public Set<IRI> getForwardTransitiveMappings(IRI iri) {
    Set<IRI> transitiveIris = new HashSet<IRI>();
    Set<IRI> leafIris = new HashSet<IRI>();
    findForwardCycle(iri, transitiveIris, leafIris);
    return transitiveIris;
  }

  @Override
  public Set<IRI> getForwardFinalMappings(IRI iri) {
    Set<IRI> transitiveIris = new HashSet<IRI>();
    Set<IRI> leafIris = new HashSet<IRI>();
    findForwardCycle(iri, transitiveIris, leafIris);
    leafIris.remove(iri);
    return leafIris;
  }

  @Override
  public Set<IRI> getBackwardTransitiveMappings(IRI iri) {
    Set<IRI> transitiveIris = new HashSet<IRI>();
    Set<IRI> leafIris = new HashSet<IRI>();
    findBackwardCycles(iri, transitiveIris, leafIris);
    return transitiveIris;
  }

  @Override
  public Set<IRI> getBackwardFinalMappings(IRI iri) {
    Set<IRI> transitiveIris = new HashSet<IRI>();
    Set<IRI> leafIris = new HashSet<IRI>();
    findBackwardCycles(iri, transitiveIris, leafIris);
    leafIris.remove(iri);
    return leafIris;
  }

  private Set<IRI> findForwardCycle(IRI iri, Set<IRI> transitiveIris, Set<IRI> leafIris) {
    // if we have seen the IRI return, should not happen but just in case
    if (!transitiveIris.add(iri))
    {
      return new HashSet<IRI>();
    }

    boolean foundPathForward = false;
    Set<IRI> cycleIRIs = new HashSet<IRI>();

    if (getForwardMappings(iri) != null)
    {
      for (IRI forwardIri : getForwardMappings(iri))
      {
        if (transitiveIris.contains(forwardIri))
        {
          cycleIRIs.add(forwardIri);
        } else
        {
          foundPathForward = true;
          cycleIRIs.addAll(findForwardCycle(forwardIri, transitiveIris, leafIris));
        }
      }
    }

    if (!foundPathForward)
    {
      leafIris.add(iri);
    }
    return cycleIRIs;
  }

  private Set<IRI> findBackwardCycles(IRI iri, Set<IRI> transitiveIris, Set<IRI> leafIris) {
    // if we have seen the IRI return, should not happen but just in case
    if (!transitiveIris.add(iri))
    {
      return null;
    }

    boolean foundPath = false;
    Set<IRI> cycleIRIs = new HashSet<IRI>();

    if (getBackwardMappings(iri) != null)
    {
      for (IRI backwardIri : getBackwardMappings(iri))
      {
        if (transitiveIris.contains(backwardIri))
        {
          cycleIRIs.add(backwardIri);
        } else
        {
          foundPath = true;
          cycleIRIs.addAll(findBackwardCycles(backwardIri, transitiveIris, leafIris));
        }
      }
    }

    if (!foundPath)
    {
      leafIris.add(iri);
    }
    return cycleIRIs;
  }

  @Override
  public Set<IRI> getForwardMappedIris() {

    return forwardMap.keySet();
  }

  @Override
  public Set<IRI> getBackwardMappedIris() {

    return backwardMap.keySet();
  }

  @Override
  public Set<IRI> hasForwardMappingCycle(IRI iri) {
    Set<IRI> transitiveIris = new HashSet<IRI>();
    Set<IRI> leafIris = new HashSet<IRI>();
    return findForwardCycle(iri, transitiveIris, leafIris);
  }

  @Override
  public Set<IRI> hasBackwardMappingCycle(IRI iri) {
    Set<IRI> transitiveIris = new HashSet<IRI>();
    Set<IRI> leafIris = new HashSet<IRI>();
    return findBackwardCycles(iri, transitiveIris, leafIris);
  }

}
