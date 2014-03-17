package isf.util;

import isf.util.ISFT.Vocab;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
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
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * @author Shahim Essaid
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ISFUtil {

	// ================================================================================
	// Initialization, must call. Done this way to delay the logging setup until
	// the final directories are determined from the commandline options or some
	// other client.
	// ================================================================================

	public static void init() {
		//

		//
		SimpleDateFormat df = new SimpleDateFormat("yyMMdd_HHmmss");
		String date = df.format(new Date());
		context = (LoggerContext) LoggerFactory.getILoggerFactory();

		FileAppender appender = new FileAppender<Object>();
		appender.setFile(new File(getGeneratedDirectory().getAbsolutePath() + "/log", date
				+ "-log.txt").getAbsolutePath());
		appender.setContext(context);

		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(context);
		encoder.setPattern("%r %c %level - %msg%n");
		encoder.start();

		appender.setEncoder(encoder);
		appender.start();

		context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME).getAppender("console")
				.addFilter(new Filter<ILoggingEvent>() {

					@Override
					public FilterReply decide(ILoggingEvent event) {
						if (event.getLevel().isGreaterOrEqual(Level.WARN))
						{
							return FilterReply.ACCEPT;
						}
						return FilterReply.DENY;
					}
				});
		;
		// context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME).detachAppender("console");
		context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME).addAppender(appender);

		setLoggingLevel(level.name());

	}

	private static Properties isfProperties = new Properties();
	static
	{
		File isfPropertiesFile = new File(System.getProperty("user.home"), "isf.properties");
		if (isfPropertiesFile.isFile())
		{
			try
			{
				isfProperties.load(new FileReader(isfPropertiesFile));
			} catch (IOException e)
			{
				throw new RuntimeException(
						"Error while loading isf.properties file from home directory.", e);
			}
		}
	}

	// ================================================================================
	// ISF trunk directory
	// ================================================================================
	@SuppressWarnings("unused")
	private static boolean ___________DIRECTORIES________________;

	public static File ISF_TRUNK_DIR = null;

	public static File getTrunkDirectory() {
		if (ISF_TRUNK_DIR == null)
		{
			String isfTrunk = System.getProperty(ISF.ISF_TRUNK_PROPERTY);

			if (isfTrunk == null)
			{
				isfTrunk = System.getenv(ISF.ISF_TRUNK_PROPERTY.toUpperCase().replace(".", "_"));
			}

			if (isfTrunk == null)
			{
				isfTrunk = isfProperties.getProperty(ISF.ISF_TRUNK_PROPERTY);
			}

			if (isfTrunk != null)
			{
				File isfTrunkDir;
				try
				{
					isfTrunkDir = new File(isfTrunk).getCanonicalFile();
				} catch (IOException e)
				{
					throw new IllegalStateException("Error determining ISF trunk directory", e);
				}
				if (checkValidTrunkLocation(isfTrunkDir))
				{
					ISF_TRUNK_DIR = isfTrunkDir;
				} else
				{
					throw new IllegalStateException(
							"The ISF trunk location does not appear to be valid. Location: "
									+ isfTrunkDir.getAbsolutePath());
				}
			} else
			{
				throw new RuntimeException("Failed to find ISF trunk directory.");
			}
		}
		return ISF_TRUNK_DIR;
	}

	public static void setISFTrunkDirecotry(File isfTrunkDirectory) {
		ISF_TRUNK_DIR = isfTrunkDirectory;
	}

	private static boolean checkValidTrunkLocation(File trunkDir) {
		if (trunkDir != null && trunkDir.isDirectory())
		{
			File tools = new File(trunkDir, "src/ontology/isf-dev.owl");
			return tools.exists();
		}
		return false;
	}

	public static File getDefaultModuleDirectory() {
		return new File(getTrunkDirectory(), "src/ontology/module");
	}

	public static boolean datedGenerated = true;
	public static String suffixString = "";
	private static String datedString = null;
	private static File generatedDirectory = null;

	public static void setGeneratedDirectory(File generatedDirectory) {
		ISFUtil.generatedDirectory = generatedDirectory;
	}

	public static File getGeneratedDirectory() {
		if (generatedDirectory == null)
		{
			String directory = isfProperties.getProperty(ISF.ISF_GENERATED_DIRECTORY_PROPERTY);
			if (directory != null)
			{
				try
				{
					generatedDirectory = new File(directory).getCanonicalFile();
				} catch (IOException e)
				{
					throw new RuntimeException(
							"Failed to create generated directory from properties file.", e);
				}
			} else
			{
				generatedDirectory = new File(System.getProperty("user.home") + "/isf.tools",
						"generated");
			}
			if (datedGenerated)
			{
				if (datedString == null)
				{
					SimpleDateFormat df = new SimpleDateFormat("yyMMdd-HHmmss");
					datedString = df.format(new Date());
				}
				generatedDirectory = new File(generatedDirectory.getAbsolutePath(), datedString
						+ "_" + suffixString);
			} else
			{
				generatedDirectory = new File(generatedDirectory.getAbsolutePath() + "_"
						+ suffixString);
			}
		}
		return generatedDirectory;
	}

	// ================================================================================
	// Logging setup
	// ================================================================================

	public enum LogLevel {
		warn, info, debug, trace;
	}

	@SuppressWarnings("unused")
	private static boolean ___________LOGGING________________;

	private static LoggerContext context = null;
	private static LogLevel level = LogLevel.info;

	private static Logger logger = LoggerFactory.getLogger("ISFUtil");

	public static void setLoggingLevel(String level) {
		switch (LogLevel.valueOf(level))
		{
		case warn:
			ISFUtil.level = LogLevel.warn;
			context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME).setLevel(Level.WARN);
			break;
		case info:
			ISFUtil.level = LogLevel.info;
			context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME).setLevel(Level.INFO);
			break;
		case debug:
			ISFUtil.level = LogLevel.debug;
			context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME).setLevel(Level.DEBUG);
			break;
		case trace:
			context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME).setLevel(Level.TRACE);
			break;
		default:
			break;

		}
	}

	public static String getLoggingLevel() {
		return level.name();
	}

	// ================================================================================
	// Shared OWL objects
	// ================================================================================

	@SuppressWarnings("unused")
	private static boolean ___________SHARED_OWL________________;

	private static OWLDataFactory df = OWLManager.getOWLDataFactory();

	/**
	 * It sets the IRI mapper to recursively find ontologies under src/ontology.
	 * All IRIs under that path should be unique.
	 * 
	 * @param man
	 * @return
	 */
	public static OWLOntologyManager setupManagerMapper(OWLOntologyManager man) {
		man.clearIRIMappers();
		OWLOntologyIRIMapper mapper = new AutoIRIMapper(new File(getTrunkDirectory(),
				"src/ontology"), true);
		man.addIRIMapper(mapper);
		return man;
	}

	public static OWLOntology setupAndLoadIsfOntology(OWLOntologyManager man)
			throws OWLOntologyCreationException {
		setupManagerMapper(man);
		logger.info("Loading ISF");
		man.loadOntology(ISF.ISF_DEV_IRI);
		for (OWLOntology o : man.getOntologies())
		{
			logger.debug("\t Loaded ontology " + o.getOntologyID().getOntologyIRI() + " from "
					+ man.getOntologyDocumentIRI(o));
		}

		return man.getOntology(ISF.ISF_DEV_IRI);
	}

	public static OWLReasoner getDefaultReasoner(OWLOntology ontology) {
		return new FaCTPlusPlusReasonerFactory().createReasoner(ontology);
	}

	public static OWLOntology setupAndLoadIsfFullOntology(OWLOntologyManager man)
			throws OWLOntologyCreationException {
		setupManagerMapper(man);
		man.loadOntology(ISF.ISF_FULL_DEV_IRI);
		return man.getOntology(ISF.ISF_FULL_DEV_IRI);
	}

	private static OWLOntologyManager isfManager;

	/**
	 * A shared OWLOntologyManager for the ISF "ontologies"/"owl files" that
	 * can/should be used as much as possible to keep the ontologies consistent
	 * between the various tools. //TODO Need to migrate other managers to this
	 * one.
	 * 
	 * @return
	 */
	public static OWLOntologyManager getIsfManagerSingleton() {
		if (isfManager == null)
		{
			isfManager = OWLManager.createOWLOntologyManager();
			setupManagerMapper(isfManager);
		}
		return isfManager;
	}

	// ================================================================================
	// OWL helper methods
	// ================================================================================

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

	public static OWLOntology getOrLoadOntology(IRI iri, OWLOntologyManager man) {
		OWLOntology o = man.getOntology(iri);
		if (o == null)
		{
			try
			{
				o = man.loadOntology(iri);
			} catch (OWLOntologyCreationException e)
			{
				throw new RuntimeException("Failed to getOrLoad ontology: " + iri, e);
			}
		}
		return o;
	}

	public static Set<OWLAnnotationAssertionAxiom> getIncludeAxioms(OWLOntology ontology,
			boolean includeImports) {

		return ISFUtil
				.getAnnotationAssertionAxioms(ontology,
						df.getOWLAnnotationProperty(ISFT.include.iri()),
						includeImports);
	}

	public static Set<OWLAnnotationAssertionAxiom> getIncludeInstancesAxioms(OWLOntology ontology,
			boolean includeImports) {

		return ISFUtil.getAnnotationAssertionAxioms(ontology,
				df.getOWLAnnotationProperty(IRI.create(Vocab.ISFT_INCLUDE_INSTANCES)),
				includeImports);
	}

	public static Set<OWLAnnotationAssertionAxiom> getIncludeSubsAxioms(OWLOntology ontology,
			boolean includeImports) {

		return ISFUtil.getAnnotationAssertionAxioms(ontology,
				df.getOWLAnnotationProperty(IRI.create(Vocab.ISFT_INCLUDE_SUBS)),
				includeImports);
	}

	public static Set<OWLAnnotationAssertionAxiom> getExcludeAxioms(OWLOntology ontology,
			boolean includeImports) {

		return ISFUtil
				.getAnnotationAssertionAxioms(ontology,
						ISFT.exclude.getAP(),
						includeImports);
	}

	public static Set<OWLAnnotationAssertionAxiom> getExcludeSubsAxioms(OWLOntology ontology,
			boolean includeImports) {

		return ISFUtil.getAnnotationAssertionAxioms(ontology,
				df.getOWLAnnotationProperty(IRI.create(Vocab.ISFT_EXCLUDE_SUBS)),
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

	public static Set<OWLAxiom> getDefiningAxioms(final OWLEntity entity,
			Set<OWLOntology> ontologies, boolean includeImports) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for (OWLOntology o : ontologies)
		{
			axioms.addAll(getDefiningAxioms(entity, o, includeImports));
		}
		return axioms;
	}

	public static Set<OWLAxiom> getDefiningAxioms(final OWLEntity entity, OWLOntology ontology,
			boolean includeImports) {
		final Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<OWLOntology> ontologies;
		if (includeImports)
		{
			ontologies = ontology.getImportsClosure();
		} else
		{
			ontologies = Collections.singleton(ontology);
		}

		for (final OWLOntology o : ontologies)
		{
			entity.accept(new OWLEntityVisitor() {

				@Override
				public void visit(OWLAnnotationProperty property) {
					axioms.addAll(o.getAxioms(property));
				}

				@Override
				public void visit(OWLDatatype datatype) {
					axioms.addAll(o.getAxioms(datatype));

				}

				@Override
				public void visit(OWLNamedIndividual individual) {
					axioms.addAll(o.getAxioms(individual));

				}

				@Override
				public void visit(OWLDataProperty property) {
					axioms.addAll(o.getAxioms(property));

				}

				@Override
				public void visit(OWLObjectProperty property) {
					axioms.addAll(o.getAxioms(property));

				}

				@Override
				public void visit(OWLClass cls) {
					axioms.addAll(o.getAxioms(cls));

				}
			});
		}

		return axioms;
	}

	// TODO: where is this used?
	public static Set<LabelInfo> getLabels(IRI iri, Set<OWLOntology> ontologies) {
		Set<LabelInfo> infos = new HashSet<ISFUtil.LabelInfo>();

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

//	public static Map<IRI, IRI> getMappings(String mappingName, boolean leftToRight)
//			throws OWLOntologyCreationException {
//		Map<IRI, IRI> mappings = new HashMap<IRI, IRI>();
//		OWLOntology mappingOntology = getIsfManagerSingleton().loadOntology(
//				IRI.create(ISF.ISF_ONTOLOGY_IRI_PREFIX + mappingName + ISF.ISF_MAPPING_SUFFIX));
//
//		OWLAnnotationProperty p = getIsfManagerSingleton().getOWLDataFactory()
//				.getOWLAnnotationProperty(ISF.ISF_IRI_MAPPES_TO_IRI);
//		for (OWLAnnotationAssertionAxiom aaa : getAnnotationAssertionAxioms(mappingOntology, p,
//				true))
//		{
//			if (aaa.getProperty().getIRI().equals(ISF.ISF_IRI_MAPPES_TO_IRI))
//			{
//				IRI subjectIri = (IRI) aaa.getSubject();
//				IRI objectIri = (IRI) aaa.getValue();
//
//				if (leftToRight)
//				{
//					duplicateCheck(subjectIri, objectIri, mappingName, mappings);
//					mappings.put(subjectIri, objectIri);
//				} else
//				{
//					duplicateCheck(objectIri, subjectIri, mappingName, mappings);
//					mappings.put(objectIri, subjectIri);
//				}
//			}
//		}
//
//		// apply transitive mappings
//		Map<IRI, IRI> finalMappings = new HashMap<IRI, IRI>();
//		for (IRI from : mappings.keySet())
//		{
//			finalMappings.put(from, getTransitiveMapping(from, mappings));
//		}
//		return finalMappings;
//	}

//	private static IRI getTransitiveMapping(IRI from, Map<IRI, IRI> mappings) {
//		if (mappings.get(from) == null)
//		{
//			return from;
//		} else
//		{
//			return getTransitiveMapping(mappings.get(from), mappings);
//		}
//	}
//
//	private static void duplicateCheck(IRI from, IRI to, String mappingName, Map<IRI, IRI> mappings) {
//		if (mappings.containsKey(from) && !mappings.get(from).equals(to))
//		{
//			throw new IllegalStateException("Mapping " + mappingName
//					+ " contains multiple mappings for IRI " + from + ". Found a mapping to " + to
//					+ " while there was an existing mapping to " + mappings.get(from));
//		}
//
//	}

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
			fis = ISFUtil.class.getResourceAsStream(libPath + libName);
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

}
