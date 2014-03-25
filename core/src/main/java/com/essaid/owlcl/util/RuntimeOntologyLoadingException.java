package com.essaid.owlcl.util;

import org.semanticweb.owlapi.io.OWLOntologyCreationIOException;
import org.semanticweb.owlapi.io.OntologyIRIMappingNotFoundException;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyDocumentAlreadyExistsException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class RuntimeOntologyLoadingException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RuntimeOntologyLoadingException(String message, OWLOntologyCreationException cause) {
		super(message, cause);

	}

	public boolean isIriMapping() {
		return getCause() instanceof OntologyIRIMappingNotFoundException;
	}

	public OntologyIRIMappingNotFoundException getIriMapping() {
		return (OntologyIRIMappingNotFoundException) getCause();
	}

	public boolean isOntologyExists() {
		return getCause() instanceof OWLOntologyAlreadyExistsException;
	}

	public OWLOntologyAlreadyExistsException getOntologyExists() {
		return (OWLOntologyAlreadyExistsException) getCause();
	}

	public boolean isIO() {
		return getCause() instanceof OWLOntologyCreationIOException;
	}

	public OWLOntologyCreationIOException getIO() {
		return (OWLOntologyCreationIOException) getCause();
	}

	public boolean isDocumentExists() {
		return getCause() instanceof OWLOntologyDocumentAlreadyExistsException;
	}

	public OWLOntologyDocumentAlreadyExistsException getDocumentExists() {
		return (OWLOntologyDocumentAlreadyExistsException) getCause();
	}

	public boolean isParsing() {
		return getCause() instanceof UnparsableOntologyException;
	}

	public UnparsableOntologyException getParsing() {
		return (UnparsableOntologyException) getCause();
	}

	public boolean isUnloadableImport() {
		return getCause() instanceof UnloadableImportException;
	}

	public UnloadableImportException getUnloadableImport() {
		return (UnloadableImportException) getCause();
	}
}
