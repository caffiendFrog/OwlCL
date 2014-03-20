package isf.module.builder.simple;

import isf.module.Module;
import isf.module.builder.ModuleBuilder;
import isf.util.ISFTUtil;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class InferredSimpleModuleBuilder extends AbstractSimpleModuleBuilder {

	public InferredSimpleModuleBuilder(Module simeplModule) {
		super(simeplModule);
	}

	public void run() throws Exception {

		report.info("");
		report.info("==========================================================");
		report.info("======== Generating inferred simple module " + module.getName()
				+ "================");
		report.info("===========================================================");
		report.info("");

		if (module.getSourceReasoned().getUnsatisfiableClasses().getEntities().size() > 0)
		{
			report.info("");
			report.info("Unsatisfied entities:");
			for (OWLEntity entity : module.getSourceReasoned().getUnsatisfiableClasses()
					.getEntities())
			{
				report.info("\t" + entity);
			}
			report.info("Unsatisfieds: "
					+ module.getSourceReasoned().getUnsatisfiableClasses().getEntities());
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

		report.info("Adding parents to BFO: ");
		addClosureToBfo();

		report.info("Adding annotations: ");
		addAnnotations();

		report.info("Typing all entities: ");
		typeAllEntities();

		addOntologyAnnotations();

	}

	public void addIncludes() {
		Set<OWLEntity> entities = ISFTUtil.getIncludeEntities(module.getModuleConfiguration(),
				false);

		for (OWLEntity e : entities)
		{
			addAxiom(module.getDataFactory().getOWLDeclarationAxiom(e));
			addAxioms(getDefiningAxioms(e, module.getSource(), true));
		}

	}

	public void addIncludeSubs() {
		Set<OWLEntity> entities = ISFTUtil.getIncludeSubsEntities(module.getModuleConfiguration(),
				false);
		// report.info("Found sub annotations for: " + entities);
		Set<OWLEntity> closureEntities = new HashSet<OWLEntity>();

		for (OWLEntity e : entities)
		{
			closureEntities.addAll(ISFTUtil.getSubs(e, true, module.getSourceReasoned()));
		}
		for (OWLEntity e : closureEntities)
		{
			addAxiom(df.getOWLDeclarationAxiom(e));
			addAxioms(getDefiningAxioms(e, module.getSource(), true));
		}
	}

	private void addIncludeInstances() {
		Set<OWLEntity> entities = ISFTUtil.getIncludeInstances(module.getModuleConfiguration(),
				false);

		for (OWLEntity e : entities)
		{
			addAxiom(df.getOWLDeclarationAxiom(e));
			addAxioms(getDefiningAxioms(e, module.getSource(), true));
		}

	}

	public void removeExcludes() {
		Set<OWLEntity> entities = ISFTUtil.getExcludeEntities(module.getModuleConfiguration(),
				false);
		for (OWLEntity entity : entities)
		{
			removeAxiom(df.getOWLDeclarationAxiom(entity));
			removeAxioms(getDefiningAxioms(entity, module.getSource(), true));

			if (entity instanceof OWLClass)
			{
				OWLClass c = (OWLClass) entity;
				Set<OWLClass> subs = module.getSourceReasoned().getSubClasses(c, true)
						.getFlattened();
				for (OWLClass sub : subs)
				{
					OWLSubClassOfAxiom subAxiom = df.getOWLSubClassOfAxiom(sub, c);
					if (module.getGeneratedModule().containsAxiom(subAxiom))
					{
						removeAxiom(subAxiom);
						;
						for (OWLClass supr : module.getSourceReasoned().getSuperClasses(c, true)
								.getFlattened())
						{
							if (module.getGeneratedModule().containsClassInSignature(supr.getIRI()))
							{
								addAxiom(df.getOWLSubClassOfAxiom(sub, supr));
							}
						}
					}
				}

			}
		}

	}

	public void removeExcludeSubs() {
		Set<OWLEntity> entities = ISFTUtil.getExcludeSubsEntities(module.getModuleConfiguration(),
				false);
		// report.info("Excluding class: " + entities);
		Set<OWLEntity> entityiesClosure = new HashSet<OWLEntity>();
		for (OWLEntity entity : entities)
		{
			entityiesClosure.addAll(ISFTUtil.getSubs(entity, true, module.getSourceReasoned()));
		}
		// report.info("Excluding class closure: " + entityiesClosure);
		for (OWLEntity entity : entityiesClosure)
		{
			removeAxiom(df.getOWLDeclarationAxiom(entity));
			removeAxioms(getDefiningAxioms(entity, module.getSource(), true));
		}

	}

	public void addClosureToBfo() {
		for (OWLEntity entity : module.getGeneratedModule().getSignature())
		{
			Set<OWLEntity> supers = ISFTUtil.getSupers(entity, true, module.getSourceReasoned());
			for (final OWLEntity supr : supers)
			{
				if (!supr.getIRI().toString().contains("BFO_"))
				{
					Set<OWLAxiom> axioms = getDefiningAxioms(supr, module.getSource(), true);
					for (OWLAxiom axiom : axioms)
					{
						axiom.accept(new OWLAxiomVisitorAdapter() {

							@Override
							public void visit(OWLSubClassOfAxiom axiom) {
								if (axiom.getSubClass() instanceof OWLClass
										&& axiom.getSubClass().asOWLClass().getIRI()
												.equals(supr.getIRI()))
								{

									if (axiom.getSuperClass() instanceof OWLClass)
									{
										addAxiom(axiom);
									}
								}
							}
							// TODO the other types of entities
						});
					}
				}
			}
		}

	}

	public void addAnnotations() {
		Set<OWLEntity> entitiesToAnnotate = new HashSet<OWLEntity>();
		entitiesToAnnotate.addAll(module.getGeneratedModule().getSignature());

		Set<OWLEntity> annotatedEntities = new HashSet<OWLEntity>();

		while (entitiesToAnnotate.size() > 0)
		{
			Set<OWLEntity> newEntities = new HashSet<OWLEntity>();
			Iterator<OWLEntity> i = entitiesToAnnotate.iterator();
			while (i.hasNext())
			{
				OWLEntity entity = i.next();
				i.remove();
				annotatedEntities.add(entity);
				Set<OWLAnnotationAssertionAxiom> axioms = ISFTUtil.getSubjectAnnotationAxioms(
						module.getSource(), true, entity.getIRI());
				addAxioms(axioms);
				for (OWLAnnotationAssertionAxiom a : axioms)
				{
					Set<OWLEntity> signature = a.getSignature();
					signature.removeAll(annotatedEntities);
					newEntities.addAll(signature);
				}
			}

			entitiesToAnnotate.addAll(newEntities);
		}

	}

	public void typeAllEntities() {
		for (OWLEntity e : module.getGeneratedModule().getSignature())
		{
			addAxiom(df.getOWLDeclarationAxiom(e));
		}

	}

	private void addAxioms(Set<? extends OWLAxiom> axioms) {
		for (OWLAxiom axiom : axioms)
		{
			addAxiom(axiom);
		}
	}

	private void addAxiom(OWLAxiom axiom) {
		if (axiom instanceof OWLDeclarationAxiom)
		{
			OWLDeclarationAxiom da = (OWLDeclarationAxiom) axiom;
			if (da.getEntity().getIRI().equals(OWLRDFVocabulary.OWL_NOTHING.getIRI()))
			{
				return;
			}
		}
		if (!removedAxioms.contains(axiom))
		{
			module.addAxiomInferred(axiom);
		}
	}

	private void removeAxioms(Set<? extends OWLAxiom> axioms) {
		for (OWLAxiom axiom : axioms)
		{
			removeAxiom(axiom);
		}
	}

	Set<OWLAxiom> removedAxioms = new HashSet<OWLAxiom>();

	private void removeAxiom(OWLAxiom axiom) {
		module.removeAxiomInferred(axiom);
		removedAxioms.add(axiom);
	}

	@Override
	public void build(Module module) {
		try
		{
			run();
		} catch (Exception e)
		{
			throw new RuntimeException("Error while building inferred simple module "
					+ module.getName(), e);
		}

	}

	@Override
	public void buildFinished(Module module) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return "simple-inferred";
	}

	@Override
	public String getDescription() {
		return "A simple inferred builder factory.";
	}

	@Override
	public ModuleBuilder createBuilder(Module module) {
		return new InferredSimpleModuleBuilder(module);
	}
}
