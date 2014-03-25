package com.essaid.owlcl.mapping;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author Shahim Essaid
 * 
 */
public interface Mapping {

	Set<IRI> getForwardMappedIris();

	Set<IRI> getBackwardMappedIris();

	/**
	 * @return the IRI that as found in a forward mapping cycle. This means that
	 *         the IRI was being traversed for mappings but there was another
	 *         later forward mapping to it therefore the cycle. When this is not
	 *         null, look for forward mappings to this IRI and see why those
	 *         mappings are cyclical.
	 */
	IRI hasForwardMappingCycle(IRI iri);

	IRI hasBackwardMappingCycle(IRI iri);

	Set<IRI> getForwardMappings(IRI iri);

	Set<IRI> getForwardTransitiveMappings(IRI iri);

	Set<IRI> getForwardFinalMappings(IRI iri);

	Set<IRI> getBackwardMappings(IRI iri);

	Set<IRI> getBackwardTransitiveMappings(IRI iri);

	Set<IRI> getBackwardFinalMappings(IRI iri);

}
