package com.essaid.owlcl.command.mapping;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import com.essaid.owlcl.core.util.OwlclUtil;

public class Mapper {

  private IMappings mapping;

  public Mapper(IMappings mapping) {
    this.mapping = mapping;
  }

  public List<OWLOntologyChange> forwardMap(IRI iri, boolean transitiveMapping,
      OWLOntology ontology, boolean ontologyClosure) {

    IRI newIri = getForwardIri(iri, transitiveMapping);

    return map(iri, newIri, ontology, ontologyClosure);
  }

  public List<OWLOntologyChange> backwardMap(IRI iri, boolean transitiveMapping,
      OWLOntology ontology, boolean ontologyClosure) {
    IRI newIri = getBackwardIri(iri, transitiveMapping);

    return map(iri, newIri, ontology, ontologyClosure);

  }

  private List<OWLOntologyChange> map(IRI iri, IRI newIri, OWLOntology ontology,
      boolean ontologyClosure) {
    Set<OWLOntology> ontologies = null;
    if (ontologyClosure)
    {
      ontologies = ontology.getImportsClosure();
    } else
    {
      ontologies = Collections.singleton(ontology);
    }

    OWLEntityRenamer oer = new OWLEntityRenamer(ontology.getOWLOntologyManager(), ontologies);

    List<OWLOntologyChange> changes = oer.changeIRI(iri, newIri);
    ontology.getOWLOntologyManager().applyChanges(changes);
    return changes;
  }

  private IRI getForwardIri(IRI iri, boolean transitive) {
    Set<IRI> iris = null;
    if (transitive)
    {
      iris = mapping.getForwardFinalMappings(iri);
    } else
    {
      iris = mapping.getForwardMappings(iri);
    }
    if (iris == null)
    {
      throw new MapperException("No forward mappings found for IRI: " + iri);
    }
    if (iris.size() != 1)
    {
      throw new MapperException("Forwards mappings for IRI " + iri + " were not 1.");
    }
    return iris.iterator().next();
  }

  private IRI getBackwardIri(IRI iri, boolean transitiveMapping) {
    Set<IRI> iris = null;
    if (transitiveMapping)
    {
      iris = mapping.getBackwardFinalMappings(iri);
    } else
    {
      iris = mapping.getBackwardMappings(iri);
    }
    if (iris == null)
    {
      throw new MapperException("No backward mappings found for IRI: " + iri);
    }
    if (iris.size() != 1)
    {
      throw new MapperException("Backward mappings for IRI " + iri + " were not 1.");
    }
    return iris.iterator().next();
  }

  public List<OWLOntologyChange> forwardMap(String iri, boolean transitiveMapping,
      OWLOntology ontology, boolean ontologyClosure) {
    return forwardMap(IRI.create(iri), transitiveMapping, ontology, ontologyClosure);

  }

  public List<OWLOntologyChange> backwardMap(String iri, boolean transitiveMapping,
      OWLOntology ontology, boolean ontologyClosure) {
    return backwardMap(IRI.create(iri), transitiveMapping, ontology, ontologyClosure);

  }

  public void forwardMapPrefix(String iriPrefix, boolean transitiveMapping, OWLOntology ontology,
      boolean ontologyClosure) {

    for (IRI iri : getPatternIris(iriPrefix + ".*", ontology, ontologyClosure))
    {
      forwardMap(iri, transitiveMapping, ontology, ontologyClosure);
    }

  }

  public void backwardMapPrefix(String iriPrefix, boolean transitiveMapping, OWLOntology ontology,
      boolean ontologyClosure) {
    for (IRI iri : getPatternIris(iriPrefix + ".*", ontology, ontologyClosure))
    {
      backwardMap(iri, transitiveMapping, ontology, ontologyClosure);
    }

  }

  private Set<IRI> getPatternIris(String pattern, OWLOntology ontology, boolean ontologyClosure) {
    Set<IRI> iris = new HashSet<IRI>();
    for (OWLAxiom a : OwlclUtil.getAxioms(ontology, ontologyClosure))
    {
      for (OWLEntity e : a.getSignature())
      {
        iris.add(e.getIRI());
      }

      traverseAnnotationsForIris(a.getAnnotations(), iris);

      if (a instanceof OWLAnnotationAssertionAxiom)
      {
        OWLAnnotationAssertionAxiom aaa = (OWLAnnotationAssertionAxiom) a;
        if (aaa.getSubject() instanceof IRI)
        {
          iris.add((IRI) aaa.getSubject());
        }
        if (aaa.getValue() instanceof IRI)
        {
          iris.add((IRI) aaa.getValue());
        }
      }

      if (a instanceof OWLAnnotationPropertyDomainAxiom)
      {
        iris.add(((OWLAnnotationPropertyDomainAxiom) a).getDomain());
      }

      if (a instanceof OWLAnnotationPropertyRangeAxiom)
      {
        iris.add(((OWLAnnotationPropertyRangeAxiom) a).getRange());
      }

    }

    Iterator<IRI> i = iris.iterator();
    while (i.hasNext())
    {

      IRI iri = i.next();
      if (!Pattern.matches(pattern, iri.toString()))
      {
        i.remove();
      }
    }

    return iris;
  }

  private void traverseAnnotationsForIris(Set<OWLAnnotation> annotations, Set<IRI> iris) {

    for (OWLAnnotation annotation : annotations)
    {
      if (annotation.getValue() instanceof IRI)
      {
        iris.add((IRI) annotation.getValue());
      }

      traverseAnnotationsForIris(annotation.getAnnotations(), iris);
    }

  }

  public void forwardMapPattern(String pattern, boolean transitiveMapping, OWLOntology ontology,
      boolean ontologyClosure) {
    for (IRI iri : getPatternIris(pattern, ontology, ontologyClosure))
    {
      forwardMap(iri, transitiveMapping, ontology, ontologyClosure);
    }

  }

  public void backwardMapPattern(String pattern, boolean transitiveMapping, OWLOntology ontology,
      boolean ontologyClosure) {
    for (IRI iri : getPatternIris(pattern, ontology, ontologyClosure))
    {
      backwardMap(iri, transitiveMapping, ontology, ontologyClosure);
    }
  }

}
