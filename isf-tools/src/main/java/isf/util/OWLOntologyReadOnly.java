package isf.util;

import java.util.List;

import org.semanticweb.owlapi.model.ImmutableOWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class OWLOntologyReadOnly extends OWLOntologyWrapper {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OWLOntologyReadOnly(OWLOntology ontology) {
		super(ontology);
	}

	@Override
	public List<OWLOntologyChange> applyChange(OWLOntologyChange change) {
		throw new ImmutableOWLOntologyChangeException(change);
	}

	@Override
	public List<OWLOntologyChange> applyChanges(List<OWLOntologyChange> changes) {
		throw new UnsupportedOperationException("Read only ontology");
	}

}
