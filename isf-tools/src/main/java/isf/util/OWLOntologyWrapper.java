package isf.util;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLNamedObjectVisitor;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import uk.ac.manchester.cs.owl.owlapi.OWLOntologyImpl;

public class OWLOntologyWrapper implements OWLOntology {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OWLOntologyImpl ontology;

	public OWLOntologyWrapper(OWLOntology ontology) {
		this.ontology = (OWLOntologyImpl) ontology;
	}

	public Set<OWLClassExpression> getNestedClassExpressions() {
		return ontology.getNestedClassExpressions();
	}

	public String toString() {
		return ontology.toString();
	}

	public final int compareTo(OWLObject o) {
		return ontology.compareTo(o);
	}

	public OWLOntologyManager getOWLOntologyManager() {
		return ontology.getOWLOntologyManager();
	}

	public OWLOntologyID getOntologyID() {
		return ontology.getOntologyID();
	}

	public boolean isAnonymous() {
		return ontology.isAnonymous();
	}

	public boolean isEmpty() {
		return ontology.isEmpty();
	}

	public boolean isTopEntity() {
		return ontology.isTopEntity();
	}

	public <T extends OWLAxiom> int getAxiomCount(AxiomType<T> axiomType,
			boolean includeImportsClosure) {
		return ontology.getAxiomCount(axiomType, includeImportsClosure);
	}

	public boolean isBottomEntity() {
		return ontology.isBottomEntity();
	}

	public boolean containsAxiom(OWLAxiom axiom) {
		return ontology.containsAxiom(axiom);
	}

	public int getAxiomCount() {
		return ontology.getAxiomCount();
	}

	public Set<OWLAxiom> getAxioms() {
		return ontology.getAxioms();
	}

	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType) {
		return ontology.getAxioms(axiomType);
	}

	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType,
			boolean includeImportsClosure) {
		return ontology.getAxioms(axiomType, includeImportsClosure);
	}

	public Set<OWLAxiom> getTBoxAxioms(boolean includeImportsClosure) {
		return ontology.getTBoxAxioms(includeImportsClosure);
	}

	public Set<OWLAxiom> getABoxAxioms(boolean includeImportsClosure) {
		return ontology.getABoxAxioms(includeImportsClosure);
	}

	public Set<OWLAxiom> getRBoxAxioms(boolean includeImportsClosure) {
		return ontology.getRBoxAxioms(includeImportsClosure);
	}

	public <T extends OWLAxiom> int getAxiomCount(AxiomType<T> axiomType) {
		return ontology.getAxiomCount(axiomType);
	}

	public Set<OWLLogicalAxiom> getLogicalAxioms() {
		return ontology.getLogicalAxioms();
	}

	public int getLogicalAxiomCount() {
		return ontology.getLogicalAxiomCount();
	}

	public Set<OWLAnnotation> getAnnotations() {
		return ontology.getAnnotations();
	}

	public Set<OWLDeclarationAxiom> getDeclarationAxioms(OWLEntity entity) {
		return ontology.getDeclarationAxioms(entity);
	}

	public Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(
			OWLAnnotationSubject subject) {
		return ontology.getAnnotationAssertionAxioms(subject);
	}

	public Set<OWLClassAxiom> getGeneralClassAxioms() {
		return ontology.getGeneralClassAxioms();
	}

	public boolean containsAxiom(OWLAxiom axiom, boolean includeImportsClosure) {
		return ontology.containsAxiom(axiom, includeImportsClosure);
	}

	public boolean containsAxiomIgnoreAnnotations(OWLAxiom axiom) {
		return ontology.containsAxiomIgnoreAnnotations(axiom);
	}

	public boolean containsAxiomIgnoreAnnotations(OWLAxiom axiom, boolean includeImportsClosure) {
		return ontology.containsAxiomIgnoreAnnotations(axiom, includeImportsClosure);
	}

	public Set<OWLAxiom> getAxiomsIgnoreAnnotations(OWLAxiom axiom) {
		return ontology.getAxiomsIgnoreAnnotations(axiom);
	}

	public Set<OWLAxiom> getAxiomsIgnoreAnnotations(OWLAxiom axiom, boolean includeImportsClosure) {
		return ontology.getAxiomsIgnoreAnnotations(axiom, includeImportsClosure);
	}

	public boolean containsClassInSignature(IRI owlClassIRI) {
		return ontology.containsClassInSignature(owlClassIRI);
	}

	public boolean containsClassInSignature(IRI owlClassIRI, boolean includeImportsClosure) {
		return ontology.containsClassInSignature(owlClassIRI, includeImportsClosure);
	}

	public boolean containsObjectPropertyInSignature(IRI propIRI) {
		return ontology.containsObjectPropertyInSignature(propIRI);
	}

	public boolean containsObjectPropertyInSignature(IRI propIRI, boolean includeImportsClosure) {
		return ontology.containsObjectPropertyInSignature(propIRI, includeImportsClosure);
	}

	public boolean containsDataPropertyInSignature(IRI propIRI) {
		return ontology.containsDataPropertyInSignature(propIRI);
	}

	public boolean containsDataPropertyInSignature(IRI propIRI, boolean includeImportsClosure) {
		return ontology.containsDataPropertyInSignature(propIRI, includeImportsClosure);
	}

	public boolean containsAnnotationPropertyInSignature(IRI propIRI) {
		return ontology.containsAnnotationPropertyInSignature(propIRI);
	}

	public boolean containsAnnotationPropertyInSignature(IRI propIRI, boolean includeImportsClosure) {
		return ontology.containsAnnotationPropertyInSignature(propIRI, includeImportsClosure);
	}

	public boolean containsIndividualInSignature(IRI individualIRI) {
		return ontology.containsIndividualInSignature(individualIRI);
	}

	public boolean containsIndividualInSignature(IRI individualIRI, boolean includeImportsClosure) {
		return ontology.containsIndividualInSignature(individualIRI, includeImportsClosure);
	}

	public boolean containsDatatypeInSignature(IRI datatypeIRI) {
		return ontology.containsDatatypeInSignature(datatypeIRI);
	}

	public boolean containsDatatypeInSignature(IRI datatypeIRI, boolean includeImportsClosure) {
		return ontology.containsDatatypeInSignature(datatypeIRI, includeImportsClosure);
	}

	public Set<OWLEntity> getEntitiesInSignature(IRI iri) {
		return ontology.getEntitiesInSignature(iri);
	}

	public Set<OWLEntity> getEntitiesInSignature(IRI iri, boolean includeImportsClosure) {
		return ontology.getEntitiesInSignature(iri, includeImportsClosure);
	}

	public boolean containsReference(OWLClass owlClass) {
		return ontology.containsReference(owlClass);
	}

	public boolean containsReference(OWLObjectProperty prop) {
		return ontology.containsReference(prop);
	}

	public boolean containsReference(OWLDataProperty prop) {
		return ontology.containsReference(prop);
	}

	public boolean containsReference(OWLNamedIndividual ind) {
		return ontology.containsReference(ind);
	}

	public boolean containsReference(OWLDatatype dt) {
		return ontology.containsReference(dt);
	}

	public boolean containsReference(OWLAnnotationProperty property) {
		return ontology.containsReference(property);
	}

	public boolean isDeclared(OWLEntity entity) {
		return ontology.isDeclared(entity);
	}

	public Set<OWLDatatypeDefinitionAxiom> getDatatypeDefinitions(OWLDatatype datatype) {
		return ontology.getDatatypeDefinitions(datatype);
	}

	public Set<OWLSubAnnotationPropertyOfAxiom> getSubAnnotationPropertyOfAxioms(
			OWLAnnotationProperty subProperty) {
		return ontology.getSubAnnotationPropertyOfAxioms(subProperty);
	}

	public Set<OWLAnnotationPropertyDomainAxiom> getAnnotationPropertyDomainAxioms(
			OWLAnnotationProperty property) {
		return ontology.getAnnotationPropertyDomainAxioms(property);
	}

	public Set<OWLAnnotationPropertyRangeAxiom> getAnnotationPropertyRangeAxioms(
			OWLAnnotationProperty property) {
		return ontology.getAnnotationPropertyRangeAxioms(property);
	}

	public boolean isDeclared(OWLEntity owlEntity, boolean includeImportsClosure) {
		return ontology.isDeclared(owlEntity, includeImportsClosure);
	}

	public boolean containsEntityInSignature(OWLEntity owlEntity) {
		return ontology.containsEntityInSignature(owlEntity);
	}

	public boolean containsEntityInSignature(OWLEntity owlEntity, boolean includeImportsClosure) {
		return ontology.containsEntityInSignature(owlEntity, includeImportsClosure);
	}

	public boolean containsEntityInSignature(IRI entityIRI) {
		return ontology.containsEntityInSignature(entityIRI);
	}

	public boolean containsEntityInSignature(IRI entityIRI, boolean includeImportsClosure) {
		return ontology.containsEntityInSignature(entityIRI, includeImportsClosure);
	}

	public Set<OWLAxiom> getReferencingAxioms(OWLEntity owlEntity) {
		return ontology.getReferencingAxioms(owlEntity);
	}

	public Set<OWLAxiom> getReferencingAxioms(OWLEntity owlEntity, boolean includeImportsClosure) {
		return ontology.getReferencingAxioms(owlEntity, includeImportsClosure);
	}

	public Set<OWLAxiom> getReferencingAxioms(OWLAnonymousIndividual individual) {
		return ontology.getReferencingAxioms(individual);
	}

	public Set<OWLClassAxiom> getAxioms(OWLClass cls) {
		return ontology.getAxioms(cls);
	}

	public Set<OWLObjectPropertyAxiom> getAxioms(OWLObjectPropertyExpression prop) {
		return ontology.getAxioms(prop);
	}

	public Set<OWLAnnotationAxiom> getAxioms(OWLAnnotationProperty prop) {
		return ontology.getAxioms(prop);
	}

	public Set<OWLDataPropertyAxiom> getAxioms(OWLDataProperty prop) {
		return ontology.getAxioms(prop);
	}

	public Set<OWLIndividualAxiom> getAxioms(OWLIndividual individual) {
		return ontology.getAxioms(individual);
	}

	public Set<OWLDatatypeDefinitionAxiom> getAxioms(OWLDatatype datatype) {
		return ontology.getAxioms(datatype);
	}

	@SuppressWarnings("deprecation")
	public Set<OWLNamedObject> getReferencedObjects() {
		return ontology.getReferencedObjects();
	}

	public Set<OWLEntity> getSignature() {
		return ontology.getSignature();
	}

	public Set<OWLEntity> getSignature(boolean includeImportsClosure) {
		return ontology.getSignature(includeImportsClosure);
	}

	public Set<OWLAnonymousIndividual> getAnonymousIndividuals() {
		return ontology.getAnonymousIndividuals();
	}

	public Set<OWLClass> getClassesInSignature() {
		return ontology.getClassesInSignature();
	}

	public Set<OWLDataProperty> getDataPropertiesInSignature() {
		return ontology.getDataPropertiesInSignature();
	}

	public Set<OWLObjectProperty> getObjectPropertiesInSignature() {
		return ontology.getObjectPropertiesInSignature();
	}

	public Set<OWLNamedIndividual> getIndividualsInSignature() {
		return ontology.getIndividualsInSignature();
	}

	public Set<OWLDatatype> getDatatypesInSignature() {
		return ontology.getDatatypesInSignature();
	}

	public Set<OWLClass> getClassesInSignature(boolean includeImportsClosure) {
		return ontology.getClassesInSignature(includeImportsClosure);
	}

	public Set<OWLObjectProperty> getObjectPropertiesInSignature(boolean includeImportsClosure) {
		return ontology.getObjectPropertiesInSignature(includeImportsClosure);
	}

	public Set<OWLDataProperty> getDataPropertiesInSignature(boolean includeImportsClosure) {
		return ontology.getDataPropertiesInSignature(includeImportsClosure);
	}

	public Set<OWLNamedIndividual> getIndividualsInSignature(boolean includeImportsClosure) {
		return ontology.getIndividualsInSignature(includeImportsClosure);
	}

	public Set<OWLAnonymousIndividual> getReferencedAnonymousIndividuals() {
		return ontology.getReferencedAnonymousIndividuals();
	}

	public Set<OWLDatatype> getDatatypesInSignature(boolean includeImportsClosure) {
		return ontology.getDatatypesInSignature(includeImportsClosure);
	}

	public Set<OWLAnnotationProperty> getAnnotationPropertiesInSignature() {
		return ontology.getAnnotationPropertiesInSignature();
	}

	@SuppressWarnings("deprecation")
	public Set<OWLAnnotationProperty> getReferencedAnnotationProperties(
			boolean includeImportsClosure) {
		return ontology.getReferencedAnnotationProperties(includeImportsClosure);
	}

	public Set<OWLImportsDeclaration> getImportsDeclarations() {
		return ontology.getImportsDeclarations();
	}

	public Set<IRI> getDirectImportsDocuments() throws UnknownOWLOntologyException {
		return ontology.getDirectImportsDocuments();
	}

	public Set<OWLOntology> getImports() throws UnknownOWLOntologyException {
		return ontology.getImports();
	}

	public Set<OWLOntology> getDirectImports() throws UnknownOWLOntologyException {
		return ontology.getDirectImports();
	}

	public Set<OWLOntology> getImportsClosure() throws UnknownOWLOntologyException {
		return ontology.getImportsClosure();
	}

	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSubClass(OWLClass cls) {
		return ontology.getSubClassAxiomsForSubClass(cls);
	}

	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSuperClass(OWLClass cls) {
		return ontology.getSubClassAxiomsForSuperClass(cls);
	}

	public Set<OWLEquivalentClassesAxiom> getEquivalentClassesAxioms(OWLClass cls) {
		return ontology.getEquivalentClassesAxioms(cls);
	}

	public Set<OWLDisjointClassesAxiom> getDisjointClassesAxioms(OWLClass cls) {
		return ontology.getDisjointClassesAxioms(cls);
	}

	public Set<OWLDisjointUnionAxiom> getDisjointUnionAxioms(OWLClass owlClass) {
		return ontology.getDisjointUnionAxioms(owlClass);
	}

	public Set<OWLHasKeyAxiom> getHasKeyAxioms(OWLClass cls) {
		return ontology.getHasKeyAxioms(cls);
	}

	public Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSubProperty(
			OWLObjectPropertyExpression property) {
		return ontology.getObjectSubPropertyAxiomsForSubProperty(property);
	}

	public Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSuperProperty(
			OWLObjectPropertyExpression property) {
		return ontology.getObjectSubPropertyAxiomsForSuperProperty(property);
	}

	public Set<OWLObjectPropertyDomainAxiom> getObjectPropertyDomainAxioms(
			OWLObjectPropertyExpression property) {
		return ontology.getObjectPropertyDomainAxioms(property);
	}

	public Set<OWLObjectPropertyRangeAxiom> getObjectPropertyRangeAxioms(
			OWLObjectPropertyExpression property) {
		return ontology.getObjectPropertyRangeAxioms(property);
	}

	public Set<OWLInverseObjectPropertiesAxiom> getInverseObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ontology.getInverseObjectPropertyAxioms(property);
	}

	public Set<OWLEquivalentObjectPropertiesAxiom> getEquivalentObjectPropertiesAxioms(
			OWLObjectPropertyExpression property) {
		return ontology.getEquivalentObjectPropertiesAxioms(property);
	}

	public Set<OWLDisjointObjectPropertiesAxiom> getDisjointObjectPropertiesAxioms(
			OWLObjectPropertyExpression property) {
		return ontology.getDisjointObjectPropertiesAxioms(property);
	}

	public Set<OWLFunctionalObjectPropertyAxiom> getFunctionalObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ontology.getFunctionalObjectPropertyAxioms(property);
	}

	public Set<OWLInverseFunctionalObjectPropertyAxiom> getInverseFunctionalObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ontology.getInverseFunctionalObjectPropertyAxioms(property);
	}

	public Set<OWLSymmetricObjectPropertyAxiom> getSymmetricObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ontology.getSymmetricObjectPropertyAxioms(property);
	}

	public Set<OWLAsymmetricObjectPropertyAxiom> getAsymmetricObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ontology.getAsymmetricObjectPropertyAxioms(property);
	}

	public Set<OWLReflexiveObjectPropertyAxiom> getReflexiveObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ontology.getReflexiveObjectPropertyAxioms(property);
	}

	public Set<OWLIrreflexiveObjectPropertyAxiom> getIrreflexiveObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ontology.getIrreflexiveObjectPropertyAxioms(property);
	}

	public Set<OWLTransitiveObjectPropertyAxiom> getTransitiveObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		return ontology.getTransitiveObjectPropertyAxioms(property);
	}

	public Set<OWLFunctionalDataPropertyAxiom> getFunctionalDataPropertyAxioms(
			OWLDataPropertyExpression property) {
		return ontology.getFunctionalDataPropertyAxioms(property);
	}

	public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSubProperty(
			OWLDataProperty lhsProperty) {
		return ontology.getDataSubPropertyAxiomsForSubProperty(lhsProperty);
	}

	public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSuperProperty(
			OWLDataPropertyExpression property) {
		return ontology.getDataSubPropertyAxiomsForSuperProperty(property);
	}

	public Set<OWLDataPropertyDomainAxiom> getDataPropertyDomainAxioms(OWLDataProperty property) {
		return ontology.getDataPropertyDomainAxioms(property);
	}

	public Set<OWLDataPropertyRangeAxiom> getDataPropertyRangeAxioms(OWLDataProperty property) {
		return ontology.getDataPropertyRangeAxioms(property);
	}

	public Set<OWLEquivalentDataPropertiesAxiom> getEquivalentDataPropertiesAxioms(
			OWLDataProperty property) {
		return ontology.getEquivalentDataPropertiesAxioms(property);
	}

	public Set<OWLDisjointDataPropertiesAxiom> getDisjointDataPropertiesAxioms(
			OWLDataProperty property) {
		return ontology.getDisjointDataPropertiesAxioms(property);
	}

	public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLIndividual individual) {
		return ontology.getClassAssertionAxioms(individual);
	}

	public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLClassExpression type) {
		return ontology.getClassAssertionAxioms(type);
	}

	public Set<OWLDataPropertyAssertionAxiom> getDataPropertyAssertionAxioms(
			OWLIndividual individual) {
		return ontology.getDataPropertyAssertionAxioms(individual);
	}

	public Set<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertionAxioms(
			OWLIndividual individual) {
		return ontology.getObjectPropertyAssertionAxioms(individual);
	}

	public Set<OWLNegativeObjectPropertyAssertionAxiom> getNegativeObjectPropertyAssertionAxioms(
			OWLIndividual individual) {
		return ontology.getNegativeObjectPropertyAssertionAxioms(individual);
	}

	public Set<OWLNegativeDataPropertyAssertionAxiom> getNegativeDataPropertyAssertionAxioms(
			OWLIndividual individual) {
		return ontology.getNegativeDataPropertyAssertionAxioms(individual);
	}

	public Set<OWLSameIndividualAxiom> getSameIndividualAxioms(OWLIndividual individual) {
		return ontology.getSameIndividualAxioms(individual);
	}

	public Set<OWLDifferentIndividualsAxiom> getDifferentIndividualAxioms(OWLIndividual individual) {
		return ontology.getDifferentIndividualAxioms(individual);
	}

	public List<OWLOntologyChange> applyChange(OWLOntologyChange change) {
		return ontology.applyChange(change);
	}

	public List<OWLOntologyChange> applyChanges(List<OWLOntologyChange> changes) {
		return ontology.applyChanges(changes);
	}

	public void accept(OWLObjectVisitor visitor) {
		ontology.accept(visitor);
	}

	public void accept(OWLNamedObjectVisitor visitor) {
		ontology.accept(visitor);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor) {
		return ontology.accept(visitor);
	}

	public boolean equals(Object obj) {
		return ontology.equals(obj);
	}

	public int hashCode() {
		return ontology.hashCode();
	}

}
