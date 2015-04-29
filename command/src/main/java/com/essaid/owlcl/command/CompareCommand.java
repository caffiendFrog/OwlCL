package com.essaid.owlcl.command;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AnnotationValueShortFormProvider;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.cli.util.CanonicalFileConverter;
import com.essaid.owlcl.core.cli.util.IriConverter;
import com.essaid.owlcl.core.util.IReportFactory;
import com.essaid.owlcl.core.util.OntologyFiles;
import com.essaid.owlcl.core.util.OwlclUtil;
import com.essaid.owlcl.core.util.Report;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@Parameters(commandNames = "compare", commandDescription = "Shows a axiom diff summary between files or directories.")
public class CompareCommand extends AbstractCommand {

	// ================================================================================
	// from files
	// ================================================================================

	@Parameter(names = "-fromFiles", description = "Starting/from files or directories.", converter = CanonicalFileConverter.class, variableArity = true)
	public void setFromFiles(List<File> fromFiles) {
		this.fromFiles = fromFiles;
		this.fromFilesSet = true;
	}

	public List<File> getFromFiles() {
		return fromFiles;
	}

	public boolean isFromFilesSet() {
		return fromFilesSet;
	}

	private List<File> fromFiles = new ArrayList<File>();
	private boolean fromFilesSet;

	// ================================================================================
	// from IRI
	// ================================================================================

	@Parameter(names = "-fromIri", description = "The IRI for the from ontology.", converter = IriConverter.class)
	public void setFromIri(IRI fromIri) {
		this.fromIri = fromIri;
		this.fromIriSet = true;
	}

	public IRI getFromIri() {
		return fromIri;
	}

	public boolean isFromIriSet() {
		return fromIriSet;
	}

	private IRI fromIri;
	private boolean fromIriSet;

	// ================================================================================
	// to files
	// ================================================================================

	@Parameter(names = "-toFiles", description = "The to/end files or directories for the diff.", converter = CanonicalFileConverter.class, variableArity = true)
	public void setToFiles(List<File> tofiles) {
		this.toFiles = tofiles;
		this.toFilesSet = true;
	}

	public List<File> getToFiles() {
		return toFiles;
	}

	public boolean isToFilesSet() {
		return toFilesSet;
	}

	private List<File> toFiles = new ArrayList<File>();
	private boolean toFilesSet;

	// ================================================================================
	// to IRI
	// ================================================================================

	@Parameter(names = "-toIri", description = "IRI for right side of the diff.", converter = IriConverter.class)
	public void setToIri(IRI toIri) {
		this.toIri = toIri;
		this.toIriSet = true;
	}

	public IRI getToIri() {
		return toIri;
	}

	public boolean isToIriSet() {
		return toIriSet;
	}

	private IRI toIri;
	private boolean toIriSet;

	// ================================================================================
	// Include sub directories?
	// ================================================================================

	@Parameter(names = "-noSubs", description = "Include sub directories when "
			+ "loading ontology files? "
			+ "Subdirectories are loaded by default.")
	public void setNoSubDir(boolean noSubDir) {
		this.noSubDir = noSubDir;
		this.noSubDirSet = true;
	}

	public boolean isNoSubDir() {
		return noSubDir;
	}

	public boolean isNoSubDirSet() {
		return noSubDirSet;
	}

	private boolean noSubDir = false;
	private boolean noSubDirSet;

	// ================================================================================
	// Don't report removed
	// ================================================================================

	@Parameter(names = "-noRemoved", description = "Don't report removed items.")
	public void setNoRemoved(boolean noRemoved) {
		this.noRemoved = noRemoved;
		this.noRemovedSet = true;
	}

	public boolean isNoRemoved() {
		return noRemoved;
	}

	public boolean isNoRemovedSet() {
		return noRemovedSet;
	}

	private boolean noRemoved;
	private boolean noRemovedSet;

	// ================================================================================
	// Don't report added
	// ================================================================================

	@Parameter(names = "-noAdded", description = "Don't report added items.")
	public void setNoAdded(boolean noAdded) {
		this.noAdded = noAdded;
		this.noAddedSet = true;
	}

	public boolean isNoAdded() {
		return noAdded;
	}

	public boolean isNoAddedSet() {
		return noAddedSet;
	}

	private boolean noAdded;
	private boolean noAddedSet;

	// ================================================================================
	// ReportName
	// ================================================================================

	@Parameter(names = "-reportName", description = "Report name.")
	public void setReportName(String name) {
		this.reportName = name;
		this.reportNameSet = true;
	}

	public String getReportName() {
		return reportName;
	}

	public boolean isReportNameSet() {
		return reportNameSet;
	}

	private String reportName;
	private boolean reportNameSet;

	// ================================================================================
	// Custom directory for the report
	// ================================================================================
	@Parameter(names = "-reportDirectory", description = "A directory for the generated "
			+ "report. Relative paths whill be under the project directory.", converter = FileConverter.class)
	public void setReportDirectory(File reportDirectory) {
		if (reportDirectory.isAbsolute()) {
			this.reportDirectory = reportDirectory;
		} else {
			this.reportDirectory = new File(getMain().getJobDirectory(),
					reportDirectory.getPath());
		}
		this.reportDirectorySet = true;
	}

	public File getReportDirectory() {
		return reportDirectory;
	}

	public boolean isReportDirectorySet() {
		return reportDirectorySet;
	}

	private File reportDirectory;
	private boolean reportDirectorySet;

	// ================================================================================
	// Initialization
	// ================================================================================

	protected void configure() {
		if (!reportNameSet) {
			reportName = "CompareReport.txt";
		}
		if (!reportDirectorySet) {
			reportDirectory = getMain().getJobDirectory();
		}

	}

	// ================================================================================
	// Implementation
	// ================================================================================

	@Inject
	IReportFactory reportFactory;

	Set<OWLAxiom> fromAxioms = new HashSet<OWLAxiom>();
	Set<OWLAxiom> toAxioms = new HashSet<OWLAxiom>();

	// from
	OntologyFiles fromOntologyFiles;
	OWLOntologyManager fromManager;
	Map<File, OWLOntology> fromFileOntologyMap = new HashMap<File, OWLOntology>();
	Map<File, Path> fromFilePathMap = new HashMap<File, Path>();
	OWLOntology fromOntology = null;

	// to
	OntologyFiles toOntologyFiles;
	OWLOntologyManager toManager;
	Map<File, OWLOntology> toFileOntologyMap = new HashMap<File, OWLOntology>();
	Map<File, Path> toFilePathMap = new HashMap<File, Path>();
	OWLOntology toOntology = null;

	// to find duplicate iris from files.
	Map<IRI, File> irisInFilesMap = new HashMap<IRI, File>();

	Report report;

	Set<OWLOntology> onlyFromOntologies = new TreeSet<OWLOntology>();

	Set<OWLOntology> bothFromOntologies = new TreeSet<OWLOntology>();

	Set<OWLOntology> onlyToOntologies = new TreeSet<OWLOntology>();

	Set<OWLOntology> bothToOntologies = new TreeSet<OWLOntology>();

	@Inject
	public CompareCommand(@Assisted OwlclCommand main) {
		super(main);
	}

	@Override
	protected void doInitialize() {
		configure();
	}

	@Override
	public Object call() throws Exception {
		configure();

		System.out.println("========= CALLED =================");
		report = reportFactory.createReport(reportName,
				reportDirectory.toPath(), this);

		if (fromIriSet ^ toIriSet) {
			throw new IllegalStateException(
					"Called with one IRI set but not the other.");
		}

		fromOntologyFiles = new OntologyFiles(fromFiles, !noSubDir,
				new HashSet<File>());
		fromManager = getMain().getNewBaseManager();
		fromOntologyFiles.setupManager(fromManager, null);
		if (fromIri != null) {
			fromOntology = OwlclUtil.getOrLoadOntology(fromIri, fromManager);
		} else {
			try {
				fromOntology = fromManager.createOntology(IRI
						.create("http://ontology/aggregator"));
			} catch (OWLOntologyCreationException e) {
				throw new RuntimeException("Failed to create top fromOntology",
						e);
			}

			for (Entry<File, IRI> entry : fromOntologyFiles
					.getLocalOntologyFiles(null).entrySet()) {
				OWLImportsDeclaration id = fromManager.getOWLDataFactory()
						.getOWLImportsDeclaration(entry.getValue());
				OWLOntology ontology = OwlclUtil.getOrLoadOntology(
						entry.getValue(), fromManager);
				if (ontology == null) {
					throw new IllegalStateException(
							"Could not load fromOntology with IRI: "
									+ entry.getValue());
				}
				AddImport ai = new AddImport(fromOntology, id);
				fromManager.applyChange(ai);
			}
		}

		toOntologyFiles = new OntologyFiles(toFiles, !noSubDir);
		toManager = getMain().getNewBaseManager();
		toOntologyFiles.setupManager(toManager, null);
		if (toIri != null) {
			toOntology = OwlclUtil.getOrLoadOntology(toIri, toManager);
		} else {
			try {
				toOntology = toManager.createOntology(IRI
						.create("http://ontology/aggregator"));
			} catch (OWLOntologyCreationException e) {
				throw new RuntimeException("Failed to create top toOntology", e);
			}

			for (Entry<File, IRI> entry : toOntologyFiles
					.getLocalOntologyFiles(null).entrySet()) {
				OWLImportsDeclaration id = toManager.getOWLDataFactory()
						.getOWLImportsDeclaration(entry.getValue());
				OWLOntology ontology = OwlclUtil.getOrLoadOntology(
						entry.getValue(), toManager);
				if (ontology == null) {
					throw new IllegalStateException(
							"Could not load toOntology with IRI: "
									+ entry.getValue());
				}
				AddImport ai = new AddImport(toOntology, id);
				toManager.applyChange(ai);
			}
		}

		onlyFromOntologies.addAll(fromOntology.getImportsClosure());
		onlyFromOntologies.removeAll(toOntology.getImportsClosure());

		bothFromOntologies.addAll(fromOntology.getImportsClosure());
		bothFromOntologies.retainAll(toOntology.getImportsClosure());

		onlyToOntologies.addAll(toOntology.getImportsClosure());
		onlyToOntologies.removeAll(fromOntology.getImportsClosure());

		bothToOntologies.addAll(toOntology.getImportsClosure());
		bothToOntologies.retainAll(fromOntology.getImportsClosure());

		// ================================================================================
		// Run the actions
		// ================================================================================
		for (String action : getAllActions()) {
			Action.valueOf(action).execute(this);
		}

		report.finish();
		return null;
	}

	@Override
	protected void addCommandActions(List<String> actionsList) {

		actionsList.add(Action.imports.name());
		actionsList.add(Action.ontannot.name());
		actionsList.add(Action.entitySummary.name());
		actionsList.add(Action.axiomSummary.name());
		actionsList.add(Action.entities.name());
		actionsList.add(Action.axioms.name());
	}

	OWLOntology getMatchingToOntology(OWLOntology ontology) {
		for (OWLOntology to : toOntology.getImportsClosure()) {
			if (to.equals(ontology)) {
				return to;
			}
		}
		return null;
	}

	enum Action {

		imports {

			@Override
			public void execute(CompareCommand command) {

				command.report.info("");
				command.report.info("===========================");
				command.report.info("=====  Imports report =====");
				command.report.info("===========================");
				command.report.info("");
				ImportCompare ic = new ImportCompare(command.fromOntology,
						command.toOntology);

				int depth = 0;
				while (ic.next()) {
					int currentDepth = ic.getDepth();

					if (ic.getCurrentFromOntology() != null) {

						if (ic.getCurrentToOntology() != null) {

							if (currentDepth >= depth) {
								String indentString = "= "
										+ new String(new char[ic.getDepth()])
												.replace('\0', '_')
										+ currentDepth + " ";
								String info = indentString
										+ ic.getCurrentToOntology()
												.getOntologyID()
												.getOntologyIRI();
								IRI document = ic
										.getCurrentToOntology()
										.getOWLOntologyManager()
										.getOntologyDocumentIRI(
												ic.getCurrentToOntology());
								command.report
										.info(info + "  <--  " + document);
								if (ic.isCyclic()) {
									indentString = "  "
											+ new String(
													new char[ic.getDepth()])
													.replace('\0', '_')
											+ (currentDepth + 1) + " ";
									command.report.info(indentString + "...");
								}

							}
							depth = currentDepth;
						} else {
							if (currentDepth >= depth) {
								String indentString = "- "
										+ new String(new char[ic.getDepth()])
												.replace('\0', '_')
										+ currentDepth + " ";
								String info = indentString
										+ ic.getCurrentFromOntology()
												.getOntologyID()
												.getOntologyIRI();
								IRI document = ic
										.getCurrentFromOntology()
										.getOWLOntologyManager()
										.getOntologyDocumentIRI(
												ic.getCurrentFromOntology());
								command.report
										.info(info + "  <--  " + document);
								if (ic.isCyclic()) {
									indentString = "  "
											+ new String(
													new char[ic.getDepth()])
													.replace('\0', '_')
											+ (currentDepth + 1) + " ";
									command.report.info(indentString + "...");
								}
							}
							depth = currentDepth;
						}
					} else {
						if (currentDepth >= depth) {
							String indentString = "+ "
									+ new String(new char[ic.getDepth()])
											.replace('\0', '_') + currentDepth
									+ " ";
							String info = indentString
									+ ic.getCurrentToOntology().getOntologyID()
											.getOntologyIRI();
							IRI document = ic
									.getCurrentToOntology()
									.getOWLOntologyManager()
									.getOntologyDocumentIRI(
											ic.getCurrentToOntology());
							command.report.info(info + "  <--  " + document);
							if (ic.isCyclic()) {
								indentString = "  "
										+ new String(new char[ic.getDepth()])
												.replace('\0', '_')
										+ (currentDepth + 1) + " ";
								command.report.info(indentString + "...");
							}
						}
						depth = currentDepth;
					}

				}
			}
		},
		entities {

			@Override
			public void execute(CompareCommand command) {

				command.report.info("");
				command.report.info("===========================");
				command.report.info("=== Entity diff ===========");
				command.report.info("===========================");
				command.report.info("");

				for (OWLOntology o : command.onlyFromOntologies) {
					int removed = 0;

					command.report.info("");
					command.report.info("-"
							+ o.getOntologyID().getOntologyIRI());
					Set<OWLEntity> entities = new TreeSet<OWLEntity>(
							o.getSignature(false));
					for (OWLEntity e : entities) {
						if (reportEntity(e)) {
							command.report.detail("\t- " + e.getEntityType()
									+ " " + e);
							++removed;
						}
					}
					command.report.info("\tCounts: -" + removed + " +0");
				}

				for (OWLOntology o : command.onlyToOntologies) {

					int added = 0;
					command.report.info("");
					command.report.info("+"
							+ o.getOntologyID().getOntologyIRI());
					Set<OWLEntity> entities = new TreeSet<OWLEntity>(
							o.getSignature(false));
					for (OWLEntity e : entities) {
						if (reportEntity(e)) {
							command.report.detail("\t+ " + e.getEntityType()
									+ " " + e);
							++added;
						}
					}
					command.report.info("\tCounts: -0" + " +" + added);
				}

				for (OWLOntology bothfromOntology : command.bothFromOntologies) {

					int added = 0;
					int removed = 0;

					OWLOntology bothToOntology = command
							.getMatchingToOntology(bothfromOntology);

					command.report.info("");
					command.report
							.info("="
									+ bothfromOntology.getOntologyID()
											.getOntologyIRI());

					Set<OWLEntity> entities = new TreeSet<OWLEntity>(
							bothfromOntology.getSignature(false));
					for (OWLEntity e : entities) {
						if (!bothToOntology.containsEntityInSignature(e, false)) {
							if (reportEntity(e)) {
								command.report.detail("\t- "
										+ e.getEntityType() + " " + e);
								++removed;
							}
						}
					}
					entities = new TreeSet<OWLEntity>(
							bothToOntology.getSignature(false));
					for (OWLEntity e : entities) {
						if (!bothfromOntology.containsEntityInSignature(e,
								false))

						{
							if (reportEntity(e)) {
								command.report.detail("\t+ "
										+ e.getEntityType() + " " + e);
								++added;
							}
						}
					}

					command.report.info("\tCounts: -" + removed + " +" + added);
				}

			}

			private boolean reportEntity(OWLEntity e) {
				if (excludedEntities.contains(e.getIRI())) {
					return false;
				}
				return true;
			}
		},
		ontannot {

			@Override
			public void execute(CompareCommand command) {
				command.report.info("");
				command.report.info("============================");
				command.report.info("== Ontology annotations ====");
				command.report.info("============================");
				command.report.info("");

				if (!command.isNoRemoved()) {
					for (OWLOntology o : command.onlyFromOntologies) {

						command.report.info("");
						command.report.info("- "
								+ o.getOntologyID().getOntologyIRI());
						int count = 0;
						for (OWLAnnotation a : o.getAnnotations()) {
							command.report.detail("\t-" + a);
							++count;
						}
						command.report.info("\tCounts: -" + count + " +0");
					}
				}

				if (!command.isNoAdded()) {
					for (OWLOntology o : command.onlyToOntologies) {
						command.report.info("");
						command.report.info("+ "
								+ o.getOntologyID().getOntologyIRI());
						int count = 0;
						for (OWLAnnotation a : o.getAnnotations()) {
							command.report.detail("\t+" + a);
							++count;
						}
						command.report.info("\tCounts: -0 " + "+" + count);
					}
				}

				for (OWLOntology from : command.bothFromOntologies) {
					OWLOntology to = command.getMatchingToOntology(from);
					command.report.info("");
					command.report.info("= "
							+ from.getOntologyID().getOntologyIRI());
					int removed = 0;
					int added = 0;
					for (OWLAnnotation a : from.getAnnotations()) {
						if (!to.getAnnotations().contains(a)) {
							if (!command.isNoRemoved()) {
								command.report.detail("\t-" + a);
							}
							++removed;
						}
					}

					for (OWLAnnotation a : to.getAnnotations()) {
						if (!from.getAnnotations().contains(a)) {
							if (!command.isNoAdded()) {
								command.report.detail("\t+" + a);
							}
							++added;
						}
					}
					command.report.detail("\tCounts: -" + removed + " +"
							+ added);
				}

			}
		},
		axioms {

			@Override
			public void execute(CompareCommand command) {
				command.report.info("");
				command.report
						.info("==========================================");
				command.report
						.info("=======  Axioms report  ==================");
				command.report
						.info("==========================================");
				command.report.info("");

				if (!command.isNoRemoved()) {
					for (OWLOntology from : command.onlyFromOntologies) {
						command.report.info("");
						command.report.info("- "
								+ from.getOntologyID().getOntologyIRI());
						int removed = 0;
						Set<OWLAxiom> axioms = new TreeSet<OWLAxiom>(
								from.getAxioms());
						for (OWLAxiom a : axioms) {
							command.report.detail("\t-" + a);
							++removed;
						}
						command.report.info("\tCounts: -" + removed + " +0");
					}

				}

				if (!command.isNoAdded()) {
					for (OWLOntology to : command.onlyToOntologies) {
						command.report.info("");
						command.report.info("+ "
								+ to.getOntologyID().getOntologyIRI());
						int added = 0;

						Set<OWLAxiom> axioms = new TreeSet<OWLAxiom>(
								to.getAxioms());
						for (OWLAxiom a : axioms) {
							command.report.detail("\t+" + a);
							++added;
						}
						command.report.info("\tCounts: -0" + " +" + added);
					}
				}

				for (OWLOntology bothFrom : command.bothFromOntologies) {
					command.report.info("");
					command.report.info("= "
							+ bothFrom.getOntologyID().getOntologyIRI());
					OWLOntology bothTo = command
							.getMatchingToOntology(bothFrom);
					int added = 0;
					int removed = 0;

					Set<OWLAxiom> axioms = new TreeSet<OWLAxiom>(
							bothFrom.getAxioms());
					for (OWLAxiom a : axioms) {
						if (!bothTo.containsAxiom(a)) {
							if (!command.isNoRemoved()) {
								command.report.detail("\t-" + a);
							}
							++removed;
						}
					}

					axioms = new TreeSet<OWLAxiom>(bothTo.getAxioms());
					for (OWLAxiom a : axioms) {
						if (!bothFrom.containsAxiom(a)) {
							if (!command.isNoAdded()) {
								command.report.detail("\t+" + a);
							}
							++added;
						}
					}
					command.report.info("\tCounts: -" + removed + " +" + added);

				}

			}
		},
		entitySummary {

			@Override
			public void execute(CompareCommand command) {

				command.report.info("");
				command.report.info("=================================");
				command.report.info("==== Entity summary =============");
				command.report.info("=================================");
				command.report.info("");

				Set<OWLEntity> fromEntities = new TreeSet<OWLEntity>(
						command.fromOntology.getSignature(true));

				Set<OWLEntity> toEntities = new TreeSet<OWLEntity>(
						command.toOntology.getSignature(true));

				List<OWLAnnotationProperty> properties = new ArrayList<OWLAnnotationProperty>();
				properties.add(OWLManager.getOWLDataFactory()
						.getOWLAnnotationProperty(
								OWLRDFVocabulary.RDFS_LABEL.getIRI()));
				// to
				AnnotationValueShortFormProvider tosfp = new AnnotationValueShortFormProvider(
						properties,
						new HashMap<OWLAnnotationProperty, List<String>>(),
						command.toOntology.getOWLOntologyManager());
				StringWriter tosw = new StringWriter();
				ManchesterOWLSyntaxObjectRenderer tor = new ManchesterOWLSyntaxObjectRenderer(
						tosw, tosfp);

				// from
				AnnotationValueShortFormProvider fromsfp = new AnnotationValueShortFormProvider(
						properties,
						new HashMap<OWLAnnotationProperty, List<String>>(),
						command.fromOntology.getOWLOntologyManager());
				StringWriter fromsw = new StringWriter();
				ManchesterOWLSyntaxObjectRenderer fromr = new ManchesterOWLSyntaxObjectRenderer(
						fromsw, fromsfp);

				int added = 0;
				int removed = 0;

				for (OWLEntity e : fromEntities) {
					if (!toEntities.remove(e)) {
						if (!command.isNoRemoved()) {
							e.accept(fromr);
							command.report.detail("- ");
							command.report.detail("- " + e.getEntityType()
									+ " " + e);
							command.report.detail("- " + e.getEntityType()
									+ " " + fromsw.getBuffer().toString());
							fromsw.getBuffer().setLength(0);
						}
						++removed;
					}
				}

				for (OWLEntity e : toEntities) {
					if (!command.isNoAdded()) {
						e.accept(tor);
						command.report.detail("+ ");
						command.report.detail("+ " + e.getEntityType() + " "
								+ e);
						command.report.detail("+ " + e.getEntityType() + " "
								+ tosw.getBuffer().toString());
						tosw.getBuffer().setLength(0);
					}
					++added;
				}

				command.report.info("Counts: -" + removed + " +" + added);
			}
		},
		axiomSummary {

			@Override
			public void execute(CompareCommand command) {

				command.report.info("");
				command.report.info("=================================");
				command.report.info("==== Axioms summary =============");
				command.report.info("=================================");
				command.report.info("");

				Set<OWLAxiom> fromAxiomsTmp = new TreeSet<OWLAxiom>(
						OwlclUtil.getAxioms(command.fromOntology, true));
				Set<OWLAxiom> fromAxioms = new TreeSet<OWLAxiom>();
				for (OWLAxiom axiom : fromAxiomsTmp) {
					if (axiom instanceof OWLAnnotationAssertionAxiom) {
						OWLAnnotationAssertionAxiom aaa = (OWLAnnotationAssertionAxiom) axiom;
						if (aaa.getAnnotation().getValue() instanceof OWLLiteral) {
							OWLLiteral l = (OWLLiteral) aaa.getAnnotation()
									.getValue();
							String string = l.getLiteral();
							fromAxioms
									.add(OWLManager
											.getOWLDataFactory()
											.getOWLAnnotationAssertionAxiom(
													aaa.getSubject(),
													OWLManager
															.getOWLDataFactory()
															.getOWLAnnotation(
																	aaa.getProperty(),
																	OWLManager
																			.getOWLDataFactory()
																			.getOWLLiteral(
																					string))));
						} else {
							fromAxioms.add(axiom);
						}
					} else {
						fromAxioms.add(axiom);
					}
				}

				Set<OWLAxiom> toAxiomsTemp = new TreeSet<OWLAxiom>(
						OwlclUtil.getAxioms(command.toOntology, true));

				Set<OWLAxiom> toAxioms = new TreeSet<OWLAxiom>();
				for (OWLAxiom axiom : toAxiomsTemp) {
					if (axiom instanceof OWLAnnotationAssertionAxiom) {
						OWLAnnotationAssertionAxiom aaa = (OWLAnnotationAssertionAxiom) axiom;
						if (aaa.getAnnotation().getValue() instanceof OWLLiteral) {
							OWLLiteral l = (OWLLiteral) aaa.getAnnotation()
									.getValue();
							String string = l.getLiteral();
							toAxioms
									.add(OWLManager
											.getOWLDataFactory()
											.getOWLAnnotationAssertionAxiom(
													aaa.getSubject(),
													OWLManager
															.getOWLDataFactory()
															.getOWLAnnotation(
																	aaa.getProperty(),
																	OWLManager
																			.getOWLDataFactory()
																			.getOWLLiteral(
																					string))));
						} else {
							toAxioms.add(axiom);
						}
					} else {
						toAxioms.add(axiom);
					}
				}
				int removed = 0;
				int added = 0;

				List<OWLAnnotationProperty> properties = new ArrayList<OWLAnnotationProperty>();
				properties.add(OWLManager.getOWLDataFactory()
						.getOWLAnnotationProperty(
								OWLRDFVocabulary.RDFS_LABEL.getIRI()));

				// to
				AnnotationValueShortFormProvider tosfp = new AnnotationValueShortFormProvider(
						properties,
						new HashMap<OWLAnnotationProperty, List<String>>(),
						command.toOntology.getOWLOntologyManager());
				StringWriter tosw = new StringWriter();
				ManchesterOWLSyntaxObjectRenderer tor = new ManchesterOWLSyntaxObjectRenderer(
						tosw, tosfp);

				// from
				AnnotationValueShortFormProvider fromsfp = new AnnotationValueShortFormProvider(
						properties,
						new HashMap<OWLAnnotationProperty, List<String>>(),
						command.fromOntology.getOWLOntologyManager());
				StringWriter fromsw = new StringWriter();
				ManchesterOWLSyntaxObjectRenderer fromr = new ManchesterOWLSyntaxObjectRenderer(
						fromsw, fromsfp);

				for (OWLAxiom a : fromAxioms) {
					if (!toAxioms.remove(a)) {
						if (!command.isNoRemoved()) {
							a.accept(fromr);
							command.report.detail("- ");
							command.report.detail("- " + a);
							command.report.detail("- "
									+ fromsw.getBuffer().toString());
							fromsw.getBuffer().setLength(0);
						}
						++removed;
					}
				}

				for (OWLAxiom a : toAxioms) {
					if (!command.isNoAdded()) {
						a.accept(tor);
						command.report.detail("+ ");
						command.report.detail("+ " + a);
						command.report.detail("+ "
								+ tosw.getBuffer().toString());
						tosw.getBuffer().setLength(0);
					}
					++added;
				}
				command.report.info("counts: -" + removed + " +" + added);
			}
		};

		static Set<IRI> excludedEntities = new HashSet<IRI>();
		static {
			excludedEntities.add(OWLRDFVocabulary.OWL_VERSION_INFO.getIRI());
		}

		public abstract void execute(CompareCommand command);

	}

	public static class ImportCompare {

		List<OWLOntology> fromPath = new ArrayList<OWLOntology>();
		List<List<OWLOntology>> fromPaths = new ArrayList<List<OWLOntology>>();

		List<OWLOntology> toPath = new ArrayList<OWLOntology>();
		List<List<OWLOntology>> toPaths = new ArrayList<List<OWLOntology>>();

		ImportCompare(OWLOntology from, OWLOntology to) {

			for (OWLOntology o : from.getImportsClosure()) {
				if (o.isAnonymous()) {
					throw new IllegalStateException(
							"The from ontology closure contains an anonymous ontology");
				}
			}
			for (OWLOntology o : to.getImportsClosure()) {
				if (o.isAnonymous()) {
					throw new IllegalStateException(
							"The to ontology closure contains an anonymous ontology");
				}
			}

			List<OWLOntology> paths = new ArrayList<OWLOntology>();
			paths.add(from);
			fromPaths.add(paths);

			paths = new ArrayList<OWLOntology>();
			paths.add(to);
			toPaths.add(paths);
		}

		public OWLOntology getCurrentToOntology() {
			if (toPath.size() == 0) {
				return null;
			}
			return toPath.get(toPath.size() - 1);
		}

		public OWLOntology getCurrentFromOntology() {
			if (fromPath.size() == 0) {
				return null;
			}
			return fromPath.get(fromPath.size() - 1);
		}

		public int getDepth() {
			return fromPath.size();
		}

		public boolean isCyclic() {
			OWLOntology fromOntology = getCurrentFromOntology();
			if (fromOntology != null) {
				int first = fromPath.indexOf(fromOntology);
				return first != fromPath.size() - 1;
			}

			OWLOntology toOntology = getCurrentToOntology();
			if (toOntology != null) {
				int first = toPath.indexOf(toOntology);
				return first != toPath.size() - 1;
			}

			return false;
		}

		boolean next() {
			if (!fromPaths.get(fromPaths.size() - 1).isEmpty() && !isCyclic()) {
				OWLOntology fromOntology = fromPaths.get(fromPaths.size() - 1)
						.remove(0);
				fromPath.add(fromOntology);
				List<OWLOntology> imports = new ArrayList<OWLOntology>(
						fromOntology.getDirectImports());
				Collections.sort(imports);
				fromPaths.add(imports);
				boolean foundMatch = false;
				for (OWLOntology o : toPaths.get(toPaths.size() - 1)) {
					if (o.getOntologyID().equals(fromOntology.getOntologyID())) {
						toPath.add(o);
						toPaths.get(toPaths.size() - 1).remove(o);
						foundMatch = true;
						imports = new ArrayList<OWLOntology>(
								o.getDirectImports());
						Collections.sort(imports);
						toPaths.add(imports);
						break;
					}
				}
				if (!foundMatch) {
					toPath.add(null);
					toPaths.add(new ArrayList<OWLOntology>());
				}

			} else if (!toPaths.get(toPaths.size() - 1).isEmpty()
					&& !isCyclic()) {
				OWLOntology toOntology = toPaths.get(toPaths.size() - 1)
						.remove(0);
				toPath.add(toOntology);
				List<OWLOntology> imports = new ArrayList<OWLOntology>(
						toOntology.getDirectImports());
				Collections.sort(imports);
				toPaths.add(imports);
				fromPath.add(null);
				fromPaths.add(new ArrayList<OWLOntology>());

			} else {
				fromPath.remove(fromPath.size() - 1);
				fromPaths.remove(fromPaths.size() - 1);
				toPath.remove(toPath.size() - 1);
				toPaths.remove(toPaths.size() - 1);
			}

			if (fromPath.size() != fromPaths.size() - 1
					|| fromPaths.size() - 1 != toPath.size()
					|| toPath.size() != toPaths.size() - 1) {
				throw new IllegalStateException(
						"The ImportCompare doens't have consistent depths. Depths: "
								+ fromPath.size() + " " + fromPaths.size()
								+ " " + toPath.size() + " " + toPaths.size());
			}
			return !finished();
		}

		boolean finished() {
			return fromPath.isEmpty() && fromPaths.get(0).isEmpty()
					&& toPath.isEmpty() && toPaths.get(0).isEmpty();
		}
	}

}
