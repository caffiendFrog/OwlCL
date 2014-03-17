package isf.mapping;

import isf.util.ISFT;
import isf.util.ISFUtil;

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

public class DefaultMapping implements Mapping {

	private OWLAnnotationProperty property = ISFT.iri_mapps_to.getAP();

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
		for (OWLAnnotationAssertionAxiom aaa : ISFUtil.getAnnotationAssertionAxioms(ontology,
				property, true))
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
		findForwardMappings(iri, transitiveIris, leafIris);
		return transitiveIris;
	}

	@Override
	public Set<IRI> getForwardFinalMappings(IRI iri) {
		Set<IRI> transitiveIris = new HashSet<IRI>();
		Set<IRI> leafIris = new HashSet<IRI>();
		findForwardMappings(iri, transitiveIris, leafIris);
		return leafIris;
	}

	@Override
	public Set<IRI> getBackwardTransitiveMappings(IRI iri) {
		Set<IRI> transitiveIris = new HashSet<IRI>();
		Set<IRI> leafIris = new HashSet<IRI>();
		findBackwardMappings(iri, transitiveIris, leafIris);
		return transitiveIris;
	}

	@Override
	public Set<IRI> getBackwardFinalMappings(IRI iri) {
		Set<IRI> transitiveIris = new HashSet<IRI>();
		Set<IRI> leafIris = new HashSet<IRI>();
		findBackwardMappings(iri, transitiveIris, leafIris);
		return leafIris;
	}

	private IRI findForwardMappings(IRI iri, Set<IRI> transitiveIris, Set<IRI> leafIris) {
		// if we have seen the IRI return, should not happen but just in case
		if (!transitiveIris.add(iri))
		{
			return null;
		}

		boolean foundPath = false;
		IRI cycleIRI = null;

		if (getForwardMappings(iri) != null)
		{
			for (IRI forwardIri : getForwardMappings(iri))
			{
				if (transitiveIris.contains(forwardIri))
				{
					cycleIRI = forwardIri;
				} else
				{
					foundPath = true;
					cycleIRI = findForwardMappings(forwardIri, transitiveIris, leafIris);
				}
			}
		}

		if (!foundPath)
		{
			leafIris.add(iri);
		}
		return cycleIRI;
	}

	private IRI findBackwardMappings(IRI iri, Set<IRI> transitiveIris, Set<IRI> leafIris) {
		// if we have seen the IRI return, should not happen but just in case
		if (!transitiveIris.add(iri))
		{
			return null;
		}

		boolean foundPath = false;
		IRI cycleIRI = null;

		if (getBackwardMappings(iri) != null)
		{
			for (IRI backwardIri : getBackwardMappings(iri))
			{
				if (transitiveIris.contains(backwardIri))
				{
					cycleIRI = backwardIri;
				} else
				{
					foundPath = true;
					cycleIRI = findBackwardMappings(backwardIri, transitiveIris, leafIris);
				}
			}
		}

		if (!foundPath)
		{
			leafIris.add(iri);
		}
		return cycleIRI;
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
	public IRI hasForwardMappingCycle(IRI iri) {
		Set<IRI> transitiveIris = new HashSet<IRI>();
		Set<IRI> leafIris = new HashSet<IRI>();
		return findForwardMappings(iri, transitiveIris, leafIris);
	}

	@Override
	public IRI hasBackwardMappingCycle(IRI iri) {
		Set<IRI> transitiveIris = new HashSet<IRI>();
		Set<IRI> leafIris = new HashSet<IRI>();
		return findBackwardMappings(iri, transitiveIris, leafIris);
	}

}
