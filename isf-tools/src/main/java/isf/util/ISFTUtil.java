package isf.util;

import isf.module.builder.simple.MBSimpleVocab;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;

/**
 * @author Shahim Essaid
 * 
 */
public class ISFTUtil {

	// ================================================================================
	// OWL helper methods
	// ================================================================================

	private static OWLDataFactory df = OWLManager.getOWLDataFactory();

	@SuppressWarnings("unused")
	private static boolean ___________OWL_HELPERS________________;

	public static OWLAnnotationProperty getAnnotationProperty(String iri) {
		return df.getOWLAnnotationProperty(IRI.create(iri));
	}

	public static OWLObjectProperty getObjectProperty(String iri) {
		return df.getOWLObjectProperty(IRI.create(iri));
	}

	public static OWLDataProperty getDataProperty(String iri) {
		return df.getOWLDataProperty(IRI.create(iri));
	}

	public static Set<String> getOntologyAnnotationLiteralValues(OWLAnnotationProperty property,
			OWLOntology ontology, boolean recursive) {
		Set<String> values = new HashSet<String>();
		Set<OWLOntology> ontologies = null;
		if (recursive)
		{
			ontologies = ontology.getImportsClosure();
		} else
		{
			ontologies = Collections.singleton(ontology);
		}
		for (OWLOntology o : ontologies)
		{
			for (OWLAnnotation a : o.getAnnotations())
			{
				if (a.getProperty().equals(property))
				{
					values.add(((OWLLiteral) a.getValue()).getLiteral());
				}
			}
		}

		return values;
	}

	public static Set<OWLAnnotationAssertionAxiom> getIncludeAxioms(OWLOntology ontology,
			boolean includeImports) {

		return ISFTUtil.getAnnotationAssertionAxioms(ontology, MBSimpleVocab.include.getAP(),
				includeImports);
	}

	public static Set<OWLAnnotationAssertionAxiom> getIncludeInstancesAxioms(OWLOntology ontology,
			boolean includeImports) {

		return ISFTUtil.getAnnotationAssertionAxioms(ontology,
				MBSimpleVocab.include_instances.getAP(), includeImports);
	}

	public static Set<OWLAnnotationAssertionAxiom> getIncludeSubsAxioms(OWLOntology ontology,
			boolean includeImports) {

		return ISFTUtil.getAnnotationAssertionAxioms(ontology, MBSimpleVocab.include_subs.getAP(),
				includeImports);
	}

	public static Set<OWLAnnotationAssertionAxiom> getExcludeAxioms(OWLOntology ontology,
			boolean includeImports) {

		return ISFTUtil.getAnnotationAssertionAxioms(ontology, MBSimpleVocab.exclude.getAP(),
				includeImports);
	}

	public static Set<OWLAnnotationAssertionAxiom> getExcludeSubsAxioms(OWLOntology ontology,
			boolean includeImports) {

		return ISFTUtil.getAnnotationAssertionAxioms(ontology, MBSimpleVocab.exclude_subs.getAP(),
				includeImports);
	}

	public static Set<OWLEntity> getIncludeEntities(OWLOntology ontology, boolean includeImports) {
		Set<OWLAnnotationAssertionAxiom> axioms = getIncludeAxioms(ontology, includeImports);
		return getSubjectEntities(ontology, includeImports, axioms);
	}

	public static Set<OWLEntity> getIncludeInstances(OWLOntology ontology, boolean includeImports) {

		Set<OWLAnnotationAssertionAxiom> axioms = getIncludeInstancesAxioms(ontology,
				includeImports);

		return getSubjectEntities(ontology, includeImports, axioms);

	}

	public static Set<OWLEntity> getIncludeSubsEntities(OWLOntology ontology, boolean includeImports) {
		Set<OWLAnnotationAssertionAxiom> axioms = getIncludeSubsAxioms(ontology, includeImports);
		return getSubjectEntities(ontology, includeImports, axioms);
	}

	public static Set<OWLEntity> getExcludeEntities(OWLOntology ontology, boolean includeImports) {
		Set<OWLAnnotationAssertionAxiom> axioms = getExcludeAxioms(ontology, includeImports);
		return getSubjectEntities(ontology, includeImports, axioms);
	}

	public static Set<OWLEntity> getExcludeSubsEntities(OWLOntology ontology, boolean includeImports) {
		Set<OWLAnnotationAssertionAxiom> axioms = getExcludeSubsAxioms(ontology, includeImports);
		return getSubjectEntities(ontology, includeImports, axioms);
	}

	private static Set<OWLEntity> getSubjectEntities(OWLOntology ontology, boolean includeImports,
			Set<OWLAnnotationAssertionAxiom> axioms) {
		Set<OWLEntity> entities = new HashSet<OWLEntity>();
		IRI subject;
		for (OWLAnnotationAssertionAxiom a : axioms)
		{
			if (a.getSubject() instanceof IRI)
			{
				subject = (IRI) a.getSubject();
				entities.addAll(ontology.getEntitiesInSignature(subject, includeImports));
			}
		}
		return entities;
	}

	public static Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(
			OWLOntology ontology, OWLAnnotationProperty property, boolean includeImports) {
		Set<OWLAnnotationAssertionAxiom> axioms = new HashSet<OWLAnnotationAssertionAxiom>();
		Set<OWLOntology> ontologies;
		if (includeImports)
		{
			ontologies = ontology.getImportsClosure();
		} else
		{
			ontologies = Collections.singleton(ontology);
		}
		for (OWLOntology o : ontologies)
		{
			for (OWLAnnotationAssertionAxiom aaa : o.getAxioms(AxiomType.ANNOTATION_ASSERTION))
			{
				if (aaa.getProperty().getIRI().equals(property.getIRI()))
				{
					axioms.add(aaa);
				}
			}
		}
		return axioms;
	}

	public static Set<OWLEntity> getSubs(OWLEntity entity, final boolean closure,
			final OWLReasoner pr) {
		final Set<OWLEntity> entities = new HashSet<OWLEntity>();
		entities.add(entity);

		entity.accept(new OWLEntityVisitor() {

			@Override
			public void visit(OWLAnnotationProperty property) {
				// TODO Auto-generated method stub

			}

			@Override
			public void visit(OWLDatatype datatype) {
				// TODO Auto-generated method stub

			}

			@Override
			public void visit(OWLNamedIndividual individual) {
				// TODO Auto-generated method stub

			}

			@Override
			public void visit(OWLDataProperty property) {
				entities.add(property);
				entities.addAll(pr.getSubDataProperties(property, !closure).getFlattened());

			}

			@Override
			public void visit(OWLObjectProperty property) {
				entities.add(property);
				Set<OWLObjectPropertyExpression> opes = pr.getSubObjectProperties(property,
						!closure).getFlattened();
				for (OWLObjectPropertyExpression ope : opes)
				{
					if (ope instanceof OWLObjectProperty)
					{
						entities.add((OWLObjectProperty) ope);
					}
				}

			}

			@Override
			public void visit(OWLClass cls) {
				entities.add(cls);
				entities.addAll(pr.getSubClasses(cls, !closure).getFlattened());
			}
		});

		return entities;
	}

	public static Set<OWLEntity> getSupers(OWLEntity entity, final boolean closure,
			final OWLReasoner pr) {
		final Set<OWLEntity> entities = new HashSet<OWLEntity>();
		entities.add(entity);

		entity.accept(new OWLEntityVisitor() {

			@Override
			public void visit(OWLAnnotationProperty property) {
				// TODO Auto-generated method stub

			}

			@Override
			public void visit(OWLDatatype datatype) {
				// TODO Auto-generated method stub

			}

			@Override
			public void visit(OWLNamedIndividual individual) {
				// TODO Auto-generated method stub

			}

			@Override
			public void visit(OWLDataProperty property) {
				entities.add(property);
				entities.addAll(pr.getSuperDataProperties(property, closure).getFlattened());

			}

			@Override
			public void visit(OWLObjectProperty property) {
				entities.add(property);
				Set<OWLObjectPropertyExpression> opes = null;

				// TODO: not sure why Fact++ is erroring out here like:

				// <http://purl.obolibrary.org/obo/ERO_0000558> had error: Role
				// expression expected in getSupRoles()

				// <http://eagle-i.org/ont/app/1.0/has_part_construct_insert>
				// had error: Role expression expected in getSupRoles()

				// <http://eagle-i.org/ont/app/1.0/has_measurement_scale> had
				// error: Role expression expected in getSupRoles()
				try
				{
					opes = pr.getSuperObjectProperties(property, closure).getFlattened();
				} catch (ReasonerInternalException e)
				{
					System.err.println(property + " had error: " + e.getMessage());
				}
				if (opes == null)
				{
					return;
				}
				for (OWLObjectPropertyExpression ope : opes)
				{
					if (ope instanceof OWLObjectProperty)
					{
						entities.add((OWLObjectProperty) ope);
					}
				}

			}

			@Override
			public void visit(OWLClass cls) {
				entities.add(cls);
				entities.addAll(pr.getSuperClasses(cls, closure).getFlattened());

			}
		});

		return entities;
	}

	public static Set<OWLAnnotationAssertionAxiom> getSubjectAnnotationAxioms(
			Set<OWLOntology> ontologies, boolean includeImports, OWLAnnotationSubject subject) {
		Set<OWLAnnotationAssertionAxiom> axioms = new HashSet<OWLAnnotationAssertionAxiom>();
		for (OWLOntology o : ontologies)
		{
			axioms.addAll(getSubjectAnnotationAxioms(o, includeImports, subject));
		}
		return axioms;
	}

	public static Set<OWLAnnotationAssertionAxiom> getSubjectAnnotationAxioms(OWLOntology ontology,
			boolean includeImports, OWLAnnotationSubject subject) {
		Set<OWLAnnotationAssertionAxiom> axioms = new HashSet<OWLAnnotationAssertionAxiom>();
		Set<OWLOntology> ontologies;
		if (includeImports)
		{
			ontologies = ontology.getImportsClosure();
		} else
		{
			ontologies = Collections.singleton(ontology);
		}
		for (OWLOntology o : ontologies)
		{
			axioms.addAll(o.getAnnotationAssertionAxioms(subject));
		}
		return axioms;

	}


	// TODO: where is this used?
	public static Set<LabelInfo> getLabels(IRI iri, Set<OWLOntology> ontologies) {
		Set<LabelInfo> infos = new HashSet<ISFTUtil.LabelInfo>();

		for (OWLOntology ontology : ontologies)
		{
			Set<OWLAnnotationAssertionAxiom> axioms = ontology.getAnnotationAssertionAxioms(iri);
			for (OWLAnnotationAssertionAxiom axiom : axioms)
			{
				if (axiom.getProperty().getIRI().equals(OWLRDFVocabulary.RDFS_LABEL.getIRI()))
				{
					infos.add(new LabelInfo(ontology, axiom));
				}
			}
		}

		return infos;
	}

	public static List<OWLEntity> getEntitiesSortedByIri(OWLOntology ontology,
			boolean includeImports) {
		ArrayList<OWLEntity> entities = new ArrayList<OWLEntity>(
				ontology.getSignature(includeImports));
		Collections.sort(entities, new Comparator<OWLEntity>() {

			@Override
			public int compare(OWLEntity o1, OWLEntity o2) {

				return o1.getIRI().compareTo(o2.getIRI());
			}
		});
		return entities;

	}

	public static class LabelInfo {

		public final OWLAnnotationAssertionAxiom axiom;
		public final OWLOntology ontology;

		public LabelInfo(OWLOntology ontology, OWLAnnotationAssertionAxiom axiom) {
			this.ontology = ontology;
			this.axiom = axiom;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "Ontology: " + ontology.getOntologyID() + " has label: "
					+ axiom.getValue().toString();
		}
	}

	public static Set<OWLAxiom> getAxioms(OWLOntology ontology, boolean recursive) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>(ontology.getAxioms());
		if (recursive)
		{
			for (OWLOntology o : ontology.getImports())
			{
				axioms.addAll(o.getAxioms());
			}
		}
		return axioms;
	}

	// ================================================================================
	// Load Fact++ native library
	// ================================================================================
	// load native libraries

	@SuppressWarnings("unused")
	private static boolean ___________FACTPP_NATIVE________________;

	static
	{
		String osName = System.getProperty("os.name");
		String osArch = System.getProperty("os.arch");
		String libName = null;
		String libPath = null;

		if (osName.toLowerCase().startsWith("windows"))
		{
			if (osArch.contains("64"))
			{
				libName = "FaCTPlusPlusJNI.dll";
				libPath = "/fact162/win64/";
			} else
			{
				libName = "FaCTPlusPlusJNI.dll";
				libPath = "/fact162/win32/";
			}
		} else if (osName.toLowerCase().startsWith("linux"))
		{
			if (osArch.contains("64"))
			{
				libName = "libFaCTPlusPlusJNI.so";
				libPath = "/fact162/linux64/";
			} else
			{
				libName = "libFaCTPlusPlusJNI.so";
				libPath = "/fact162/linux32/";
			}
		} else if (osName.toLowerCase().contains("mac"))
		{
			if (osArch.contains("64"))
			{
				libName = "libFaCTPlusPlusJNI.jnilib";
				libPath = "/fact162/os64/";
			} else
			{
				libName = "libFaCTPlusPlusJNI.jnilib";
				libPath = "/fact162/os32/";
			}
		}
		FileOutputStream fos = null;
		InputStream fis = null;
		try
		{
			final Path libDir = Files.createTempDirectory("factpp-");
			libDir.toFile().deleteOnExit();
			fos = new FileOutputStream(new File(libDir.toFile(), libName));
			fis = ISFTUtil.class.getResourceAsStream(libPath + libName);
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			while ((bytesRead = fis.read(buffer)) != -1)
			{
				fos.write(buffer, 0, bytesRead);
			}
			System.setProperty("java.library.path", libDir.toFile().getAbsolutePath());
			Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
			Runtime.getRuntime().addShutdownHook(new Thread() {

				public void run() {
					delete(libDir.toFile());
				}

				void delete(File f) {
					if (f.isDirectory())
					{
						for (File c : f.listFiles())
							delete(c);
					}
					if (!f.delete())
						throw new RuntimeException("Failed to delete file: " + f);
				}
			});
		} catch (IOException | NoSuchFieldException | SecurityException | IllegalArgumentException
				| IllegalAccessException e)
		{
			throw new RuntimeException("ISFUtil: failed to copy native library.", e);
		} finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
				} catch (IOException e)
				{
				}
			}
			if (fis != null)
			{
				try
				{
					fis.close();
				} catch (IOException e)
				{
				}
			}

		}

	}

	static public OWLOntology getOrLoadOntology(IRI iri, OWLOntologyManager man)
			throws RuntimeOntologyLoadingException {
		OWLOntology o = man.getOntology(iri);
		if (o == null)
		{
			try
			{
				o = man.loadOntology(iri);
			} catch (OWLOntologyCreationException e)
			{
				throw new RuntimeOntologyLoadingException("Failed while getOrLoadOntology IRI: "
						+ iri, e);
			}
		}
		return o;
	}

	static public OWLOntology createOntology(IRI iri, OWLOntologyManager man)
			throws RuntimeOntologyLoadingException {

		OWLOntology o = null;

		try
		{
			o = man.createOntology(iri);
		} catch (OWLOntologyCreationException e)
		{
			throw new RuntimeOntologyLoadingException("Faild to createOntology for IRI: " + iri, e);
		}

		return o;

	}

	/**
	 * This method ignore missing IRI mappings when loading. If there is no
	 * mapping, a new ontology with the IRI is created in the manager. This
	 * means that the mappers has to be set correctly to avoid getting a new
	 * ontology when it should have been loaded. Use the getOrLoadOntology if
	 * the ontology mapping should exist.
	 * 
	 * @param iri
	 * @param man
	 * @return
	 */
	static public OWLOntology getOrLoadOrCreateOntology(IRI iri, OWLOntologyManager man)
			throws RuntimeOntologyLoadingException {
		OWLOntology o = null;
		try
		{
			o = getOrLoadOntology(iri, man);
		} catch (RuntimeOntologyLoadingException e1)
		{
			if (e1.isIriMapping())
			{
				o = createOntology(iri, man);
			} else
			{
				throw e1;
			}
		}
		return o;
	}

	private static Map<IRI, OWLReasoner> reasoners = new HashMap<IRI, OWLReasoner>();
	private static FaCTPlusPlusReasonerFactory rf = new FaCTPlusPlusReasonerFactory();

	static public OWLReasoner getReasoner(OWLOntology ontology) {
		OWLReasoner r = reasoners.get(ontology.getOntologyID().getOntologyIRI());
		if (r == null)
		{
			r = rf.createNonBufferingReasoner(ontology);
			reasoners.put(ontology.getOntologyID().getOntologyIRI(), r);
		}
		return r;
	}

	static public void disposeReasoner(OWLReasoner reasoner) {
		IRI iri = reasoner.getRootOntology().getOntologyID().getOntologyIRI();
		reasoner.dispose();
		reasoners.remove(iri);
	}
}
