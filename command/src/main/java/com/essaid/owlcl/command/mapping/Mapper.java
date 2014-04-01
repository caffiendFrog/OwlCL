package com.essaid.owlcl.command.mapping;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

public class Mapper {

	private Mapping mapping;

	public Mapper(Mapping mapping) {
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

		for (IRI iri : getPrefixIris(iriPrefix, ontology, ontologyClosure))
		{
			forwardMap(iri, transitiveMapping, ontology, ontologyClosure);
		}

	}

	public void backwardMapPrefix(String iriPrefix, boolean transitiveMapping,
			OWLOntology ontology, boolean ontologyClosure) {
		for (IRI iri : getPrefixIris(iriPrefix, ontology, ontologyClosure))
		{
			backwardMap(iri, transitiveMapping, ontology, ontologyClosure);
		}

	}

	private Set<IRI> getPrefixIris(String iriPrefix, OWLOntology ontology, boolean ontologyClosure) {
		Set<IRI> iris = new HashSet<IRI>();
		for (OWLEntity e : ontology.getSignature(ontologyClosure))
		{
			if (e.getIRI().toString().startsWith(iriPrefix))
			{
				iris.add(e.getIRI());
			}
		}
		return iris;
	}

	private Set<IRI> getPatternIris(String pattern, OWLOntology ontology, boolean ontologyClosure) {
		Set<IRI> iris = new HashSet<IRI>();
		for (OWLEntity e : ontology.getSignature(ontologyClosure))
		{
			if (Pattern.matches(pattern, e.getIRI().toString()))
			{
				iris.add(e.getIRI());
			}
		}
		return iris;
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
