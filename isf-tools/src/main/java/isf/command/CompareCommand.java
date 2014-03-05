package isf.command;

import isf.ISFUtil;
import isf.command.cli.CanonicalFileConverter;
import isf.command.cli.IriConverter;
import isf.command.cli.Main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = "compare",
		commandDescription = "Shows a axiom diff summary between files or directories. ")
public class CompareCommand extends AbstractCommand {

	// ================================================================================
	// Left files
	// ================================================================================

	public List<File> lFiles;
	public boolean lFilesSet;

	@Parameter(names = "-lfiles", description = "Files or directories for left side of the diff.",
			converter = CanonicalFileConverter.class)
	public void setlFiles(List<File> lfiles) {
		this.lFiles = lfiles;
		this.lFilesSet = true;
	}

	public List<File> getlFiles() {
		return lFiles;
	}

	// ================================================================================
	// Left IRIs
	// ================================================================================

	public IRI lIri;
	public boolean lIriSet;

	@Parameter(names = "-lIri", description = "IRI for left side of the diff.",
			converter = IriConverter.class)
	public void setlIris(IRI lIri) {
		this.lIri = lIri;
		this.lIriSet = true;
	}

	public IRI getlIri() {
		return lIri;
	}

	// ================================================================================
	// Right files
	// ================================================================================

	public List<File> rFiles;
	public boolean rFilesSet;

	@Parameter(names = "-rfiles", description = "Files or directories for other side of the diff.",
			converter = CanonicalFileConverter.class)
	public void setrFiles(List<File> rfiles) {
		this.rFiles = rfiles;
		this.rFilesSet = true;
	}

	public List<File> getrFiles() {
		return rFiles;
	}

	// ================================================================================
	// Right IRIs
	// ================================================================================

	public IRI rIri;
	public boolean rIriSet;

	@Parameter(names = "-rIri", description = "IRI for right side of the diff.",
			converter = IriConverter.class)
	public void setrIri(IRI rIri) {
		this.rIri = rIri;
		this.rIriSet = true;
	}

	public IRI getrIri() {
		return rIri;
	}

	// ================================================================================
	// Include sub directories?
	// ================================================================================

	public boolean subDir = true;
	public boolean subDirSet;

	@Parameter(names = "-subdir", arity = 1, description = "Include sub directories?")
	public void setSubDir(boolean subDir) {
		this.subDir = subDir;
		this.subDirSet = true;
	}

	public boolean isSubDir() {
		return subDir;
	}

	// ================================================================================
	// Local imports?
	// ================================================================================

	public boolean localImports = true;
	public boolean localImportsSet;

	@Parameter(names = "-localImports", arity = 1, description = "Only resolve imports "
			+ "from one of the specified files or directories.")
	public void setLocalImports(boolean localImports) {
		this.localImports = localImports;
		this.localImportsSet = true;
	}

	public boolean isLocalImports() {
		return localImports;
	}

	// ================================================================================
	// Remote imports?
	// ================================================================================

	public boolean remoteImports = false;
	public boolean remoteImportsSet;

	@Parameter(names = "-remoteImports", description = "Allow remote resolution (online)?")
	public void setRemoteImports(boolean remoteImports) {
		this.remoteImports = remoteImports;
	}

	public boolean isRemoteImports() {
		return remoteImports;
	}

	// ================================================================================
	// Detailed output?
	// ================================================================================

	public boolean detail;
	public boolean detailSet;

	public boolean isDetail() {
		return detail;
	}

	@Parameter(names = "-detail", description = "Show axioms in addition to summaries.")
	public void setDetail(boolean detail) {

		this.detail = detail;
		this.detailSet = true;
	}

	// ================================================================================
	// Debug output
	// ================================================================================

	public boolean debug;
	public boolean debugSet;

	public boolean isDebug() {
		return debug;
	}

	@Parameter(names = "-debug", description = "Show errors and other debug information.")
	public void setDebug(boolean debug) {
		this.debug = debug;
		this.debugSet = true;
	}

	// ================================================================================
	// Implementation
	// ================================================================================

	Set<OWLAxiom> leftAxioms = new HashSet<OWLAxiom>();
	Set<OWLAxiom> rightAxioms = new HashSet<OWLAxiom>();

	Map<File, OWLOntology> leftFileOntologies = new HashMap<File, OWLOntology>();
	Map<File, Path> leftFilePaths = new HashMap<File, Path>();
	// to find duplicate iris from files.
	Map<IRI, File> irisInFilesMap = new HashMap<IRI, File>();
	OWLOntology leftIriOntology = null;

	Map<File, OWLOntology> rightFileOntologies = new HashMap<File, OWLOntology>();
	Map<File, Path> rightFilePaths = new HashMap<File, Path>();
	OWLOntology rightIriOntology = null;

	public CompareCommand(Main main) {

		super(main);
	}

	@Override
	public void run() {

		if (lIriSet ^ rIriSet)
		{
			throw new IllegalStateException("Called with one IRI set but not the other.");
		}

		for (String action : getAllActions())
		{
			Action.valueOf(action).execute(this);
		}

	}

	@Override
	protected List<String> getCommandActions(List<String> actionsList) {

		actionsList.add(Action.load.name());
		actionsList.add(Action.iriPairs.name());
		return actionsList;
	}

	enum Action {
		load {

			@Override
			public void execute(CompareCommand command) {
				command.info("===   LOADING LEFT FILES   ===");
				command.loadFiles(true);

				command.info("===   LOADING LEFT IRIs   ===");
				command.loadIri(true);

				command.info("===   LOADING RIGHT FILES   ===");
				command.loadFiles(false);

				command.info("===   LOADING RIGHT IRIs   ===");
				command.loadIri(false);

			}
		},
		all {

			@Override
			public void execute(CompareCommand command) {

			}
		},
		filePairs {

			@Override
			public void execute(CompareCommand command) {
				if (command.lIriSet)
				{
					// skip files level mode if we have IRIs
					return;
				}

			}
		},
		iriPairs {

			@Override
			public void execute(CompareCommand command) {
				if (!command.lIriSet)
				{
					return;
				}
				command.info("\n===  COMPARING IRI ONTOLOGIES  ===\n");

				command.showIriCompare(command.leftIriOntology, command.rightIriOntology);

				command.showAxiomCompare(command.leftIriOntology, command.rightIriOntology);

			}

		},
		summary {

			@Override
			public void execute(CompareCommand command) {

			}
		};

		public abstract void execute(CompareCommand command);

	}

	void loadFiles(final boolean leftSide) {

		final List<File> files;
		// final Map<File, OWLOntology> ontologies;
		// final Map<File, Path> paths;
		if (leftSide)
		{
			files = lFiles;
			// ontologies = leftFileOntologies;
			// paths = leftFilePaths;
		} else
		{
			files = rFiles;
			// ontologies = rightFileOntologies;
			// paths = rightFilePaths;
		}

		for (File file : files)
		{
			if (file.isFile())
			{
				info("file: " + file.getAbsolutePath());
				loadFile(file, null, leftSide);
			} else if (file.isDirectory() && subDir)
			{
				info("dir: " + file.getAbsolutePath());
				++indent;
				final Path basePath = Paths.get(file.toURI());
				try
				{
					Files.walkFileTree(basePath, new FileVisitor<Path>() {

						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
								throws IOException {
							if (subDir)
							{
								info("entering directory: " + dir);
								++indent;
								return FileVisitResult.CONTINUE;
							} else
							{

								info("skipping directory: " + dir);
								return FileVisitResult.SKIP_SUBTREE;
							}
						}

						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
								throws IOException {
							info("file:" + file);
							++indent;
							loadFile(file.toFile(), basePath, leftSide);
							--indent;
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFileFailed(Path file, IOException exc)
								throws IOException {
							debug("Failed while walking the directory tree at file: " + file
									+ " in directory " + basePath, exc);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc)
								throws IOException {
							--indent;
							info("exiting directory: " + dir);
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e)
				{
					debug("Error whiel walking the directory: " + basePath, e);
				} finally
				{
					--indent;
				}
			}

		}

	}

	void showIriCompare(OWLOntology leftOntology, OWLOntology rightOntology) {
		List<IRI> leftIris = new ArrayList<IRI>(getIriClosure(leftOntology));
		Collections.sort(leftIris);

		List<IRI> rightIris = new ArrayList<IRI>(getIriClosure(rightOntology));
		Collections.sort(rightIris);

		Set<IRI> leftOnly = new HashSet<IRI>(leftIris);
		leftOnly.removeAll(rightIris);

		Set<IRI> rightOnly = new HashSet<IRI>(rightIris);
		rightOnly.removeAll(leftIris);

		Set<IRI> shared = new HashSet<IRI>(leftIris);
		shared.retainAll(rightIris);
		info("IRI changes: (L " + leftOnly.size() + ",= " + shared.size() + ",R "
				+ rightOnly.size() + ")");
		indent++;
		for (IRI iri : leftIris)
		{
			if (leftOnly.contains(iri))
				infoDetail("left only: " + iri);
		}
		for (IRI iri : rightIris)
		{
			if (rightOnly.contains(iri))
				infoDetail("right only: " + iri);
		}
		for (IRI iri : leftIris)
		{
			if (shared.contains(iri))
				infoDetail("both: " + iri);
		}

		infoDetail("\nLeft import chain:");
		showImportChain(leftOntology);

		infoDetail("\nRight import chain:");
		showImportChain(rightOntology);

	}

	void showImportChain(OWLOntology o) {
		infoDetail(o.getOntologyID().getOntologyIRI().toString());
		++indent;
		for (OWLOntology imprt : o.getDirectImports())
		{
			showImportChain(imprt);
		}
		--indent;
	}

	void showAxiomCompare(OWLOntology leftOntology, OWLOntology rightOntology) {

		Set<OWLAxiom> leftAxioms = ISFUtil.getAxioms(leftOntology, true);
		Set<OWLAxiom> rightAxioms = ISFUtil.getAxioms(rightOntology, true);

		Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>();
		allAxioms.addAll(leftAxioms);
		allAxioms.addAll(rightAxioms);

		Set<OWLAxiom> leftOnly = new HashSet<OWLAxiom>(allAxioms);
		leftOnly.removeAll(rightAxioms);

		Set<OWLAxiom> rightOnly = new HashSet<OWLAxiom>(allAxioms);
		rightOnly.removeAll(leftAxioms);

		Set<OWLAxiom> shared = new HashSet<OWLAxiom>(leftAxioms);
		shared.retainAll(rightAxioms);

		info("Total axiom compare: (L " + leftOnly.size() + ",= " + shared.size() + ",R "
				+ rightOnly.size() + ")");
		++indent;
		infoDetail("Left only axioms:");
		++indent;
		showAxioms(leftOnly);
		--indent;
		infoDetail("Right only axioms:");
		++indent;
		showAxioms(rightOnly);
		--indent;
		--indent;

		info("For left imports:");
		++indent;
		for (OWLOntology o : leftOntology.getImportsClosure())
		{
			Set<OWLAxiom> imprt = o.getAxioms();

			Set<OWLAxiom> imprtOnly = new HashSet<OWLAxiom>(imprt);
			imprtOnly.removeAll(rightAxioms);

			Set<OWLAxiom> imprtShared = new HashSet<OWLAxiom>(imprt);
			imprtShared.retainAll(rightAxioms);

			info("(L " + imprtOnly.size() + ",= " + imprtShared.size() + ") IRI: "
					+ o.getOntologyID().getOntologyIRI());
			if (imprtOnly.size() > 0)
			{
				++indent;
				infoDetail("Left only axioms:");
				++indent;
				showAxioms(imprtOnly);
				--indent;
				--indent;
			}
		}
		--indent;

		info("For right imports:");
		++indent;
		for (OWLOntology o : rightOntology.getImportsClosure())
		{
			Set<OWLAxiom> imprt = o.getAxioms();

			Set<OWLAxiom> imprtOnly = new HashSet<OWLAxiom>(imprt);
			imprtOnly.removeAll(leftAxioms);

			Set<OWLAxiom> imprtShared = new HashSet<OWLAxiom>(imprt);
			imprtShared.retainAll(leftAxioms);

			info("(R " + imprtOnly.size() + ",= " + imprtShared.size() + ") IRI: "
					+ o.getOntologyID().getOntologyIRI());

			if (imprtOnly.size() > 0)
			{
				++indent;
				infoDetail("Right only axioms:");
				++indent;
				showAxioms(imprtOnly);
				--indent;
				--indent;
			}
		}
		--indent;

	}

	private void showAxioms(Set<OWLAxiom> axioms) {
		for (OWLAxiom axiom : axioms)
		{
			infoDetail(axiom.toString());
		}

	}

	private void loadFile(File file, Path basePath, boolean leftSide) {
		OWLOntologyManager man;
		if (leftSide)
		{
			man = getLeftManager();
		} else
		{
			man = getRightManager();
		}

		OWLOntology o = null;
		try
		{
			o = man.loadOntologyFromOntologyDocument(new FileDocumentSource(file), getLoadConfig());

		} catch (OWLOntologyCreationException e)
		{
			if (e instanceof OWLOntologyAlreadyExistsException)
			{
				OWLOntologyAlreadyExistsException ae = (OWLOntologyAlreadyExistsException) e;
				o = man.getOntology(ae.getOntologyID());
			} else
			{
				warn("failed to load file: " + file, e);
				return;
			}
		}

		if (leftSide)
		{
			leftFileOntologies.put(file, o);
		} else
		{
			rightFileOntologies.put(file, o);
		}
		IRI iri = o.getOntologyID().getOntologyIRI();
		if (irisInFilesMap.keySet().contains(iri))
		{
			info("found DUPLICATE iri: " + iri);
		} else
		{
			info("found iri: " + iri);
			irisInFilesMap.put(iri, file);
		}

		if (basePath != null)
		{
			if (leftSide)
			{
				leftFilePaths.put(file, basePath.relativize(file.toPath()));
			} else
			{
				rightFilePaths.put(file, basePath.relativize(file.toPath()));

			}
		} else
		{
			if (leftSide)
			{
				leftFilePaths.put(file, null);
			} else
			{
				rightFilePaths.put(file, null);

			}
		}

	}

	void loadIri(boolean leftSide) {
		OWLOntologyManager man;

		if (leftSide)
		{
			man = getLeftManager();
			try
			{
				// OWLOntology o = man.loadOntologyFromOntologyDocument(new
				// IRIDocumentSource(lIri),
				// getLoadConfig());
				OWLOntology o = man.loadOntology(lIri);
				leftIriOntology = o;
			} catch (OWLOntologyCreationException e)
			{
				debug("Failed to load left ontology from IRI: " + lIri, e);
			}
		} else
		{
			man = getRightManager();
			try
			{
				// OWLOntology o = man.loadOntologyFromOntologyDocument(new
				// IRIDocumentSource(rIri),
				// getLoadConfig());
				OWLOntology o = man.loadOntology(rIri);
				rightIriOntology = o;
			} catch (OWLOntologyCreationException e)
			{
				debug("Failed to load right ontology from IRI: " + rIri, e);
			}
		}

	}

	// ================================================================================
	// Helpers
	// ================================================================================

	static Set<IRI> getIriClosure(OWLOntology o) {
		Set<IRI> iris = new HashSet<IRI>();
		for (OWLOntology imprt : o.getImportsClosure())
		{
			iris.add(imprt.getOntologyID().getOntologyIRI());
		}
		return iris;
	}

	OWLOntologyManager leftManager = null;

	OWLOntologyManager getLeftManager() {
		if (!remoteImports && !localImports)
		{
			return OWLManager.createOWLOntologyManager();
		} else
		{
			if (leftManager == null)
			{
				leftManager = OWLManager.createOWLOntologyManager();

				if (remoteImports && localImports)
				{
					mapFiles(leftManager, lFiles);
					return leftManager;
				}

				if (remoteImports && !localImports)
				{
					return leftManager;
				}

				if (!remoteImports && localImports)
				{
					leftManager.clearIRIMappers();
					mapFiles(leftManager, lFiles);
				}
			}
			return leftManager;
		}
	}

	OWLOntologyManager rightManager = null;

	OWLOntologyManager getRightManager() {
		if (!remoteImports && !localImports)
		{
			return OWLManager.createOWLOntologyManager();
		} else
		{
			if (rightManager == null)
			{
				rightManager = OWLManager.createOWLOntologyManager();

				if (remoteImports && localImports)
				{
					mapFiles(rightManager, rFiles);
					return rightManager;
				}

				if (remoteImports && !localImports)
				{
					return rightManager;
				}

				if (!remoteImports && localImports)
				{
					rightManager.clearIRIMappers();
					mapFiles(rightManager, rFiles);
				}
			}
			return rightManager;
		}
	}

	void mapFiles(OWLOntologyManager man, List<File> files) {
		for (File file : files)
		{
			if (file.isFile())
			{
				OWLOntologyIRIMapper mapper = new SimpleIRIMapper(getOntologyIriForFile(file),
						IRI.create(file));
				man.addIRIMapper(mapper);
			} else if (file.isDirectory())
			{
				OWLOntologyIRIMapper mapper = new AutoIRIMapper(file, subDir);
				man.addIRIMapper(mapper);
			}
		}
	}

	private OWLOntologyLoaderConfiguration config;

	OWLOntologyLoaderConfiguration getLoadConfig() {
		if (config == null)
		{
			config = new OWLOntologyLoaderConfiguration();
			if (!localImports || !remoteImports)
			{
				config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
			}
		}
		return config;
	}

	@SuppressWarnings("deprecation")
	private IRI getOntologyIriForFile(File file) {
		IRI iri = null;
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		man.clearIRIMappers();
		man.setSilentMissingImportsHandling(true);
		try
		{
			iri = man.loadOntologyFromOntologyDocument(file).getOntologyID().getOntologyIRI();
		} catch (OWLOntologyCreationException e)
		{
			debug("Failed to find ontology IRI from file: " + file.getAbsolutePath(), e);
		}
		return iri;
	}
}
