package isf.command;

import isf.command.cli.CanonicalFileConverter;
import isf.command.cli.DirectoryExistsValueValidator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.protege.xmlcatalog.XMLCatalog;
import org.protege.xmlcatalog.entry.UriEntry;
import org.protege.xmlcatalog.write.XMLCatalogWriter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = "catalog", commandDescription = "Creates catalog files based on the "
		+ "existing OWL files in the specified directory and optionally its subs.")
public class CatalogCommand extends AbstractCommand {

	// ================================================================================
	// The top directory to catalog from
	// ================================================================================

	// public String directory = ISFUtil.getTrunkDirectory().getAbsolutePath() +
	// "/src/ontology";
	public File directory = null;
	public boolean directorySet;

	@Parameter(names = "-directory", description = "The top directory to start cataloging from.",
			converter = CanonicalFileConverter.class,
			validateValueWith = DirectoryExistsValueValidator.class)
	public void setDirectory(File directory) {
		this.directory = directory;
		directorySet = true;
	}

	public File getDirectory() {
		return directory;
	}

	// ================================================================================
	// Do subdirectories?
	// ================================================================================

	public boolean subs = true;
	public boolean subsSet;

	@Parameter(names = "-subs", arity = 1,
			description = "Set to false if you only want the specified "
					+ "directory cataloged without doing the subs.")
	public void setSubs(boolean subs) {
		this.subs = subs;
		this.subsSet = true;
	}

	public boolean isSubs() {
		return subs;
	}

	// ================================================================================
	// Which directories to catalog, all or just *.owl ones
	// ================================================================================
	public boolean all = false;
	public boolean allSet = false;

	@Parameter(names = "-all",
			description = "Catalog all directories. Otherwise, only directories with "
					+ "*.owl files will be cataloged.")
	public void setAll(boolean all) {
		this.all = all;
		this.allSet = true;
	}

	public boolean isAll() {
		return all;
	}

	// ================================================================================
	// Catalog file name
	// ================================================================================

	public String catalogName = "catalog-v001.xml";
	public boolean cagtalogNameSet;

	@Parameter(names = "-name", description = "The file name for generated catalog files.")
	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
		this.cagtalogNameSet = true;
	}

	public String getCatalogName() {
		return catalogName;
	}

	@Override
	protected void preConfigure() {
		if (main.project != null)
		{
			directory = main.project;
		} else
		{
			directory = main.getJobDirectory();
		}

	}

	// ================================================================================
	// Implementation
	// ================================================================================

	@Override
	protected void init() {
		if (main.project != null)
		{
			if (!directorySet)
			{
				directory = main.project;
			}
		} else
		{
			if (!directorySet)
			{
				directory = main.getJobDirectory();
			}
		}

	}

	public CatalogCommand(Main main) {
		super(main);
		preConfigure();
	}

	@Override
	protected void addCommandActions(List<String> actionsList) {
		actionsList.add(Action.create.name());
	}

	@Override
	public void run() {
		init();

		for (String action : getAllActions())
		{
			Action.valueOf(action).execute(this);
		}
	}

	enum Action {
		create {

			@Override
			public void execute(CatalogCommand command) {
				System.out.println("Running create action");
				AutoIRIMapper mapper = new AutoIRIMapper(command.getDirectory(), true);

				File topDirectory = command.getDirectory();

				// for all directories
				for (File d : FileUtils.listFilesAndDirs(topDirectory,
						DirectoryFileFilter.INSTANCE, command.isSubs() ? TrueFileFilter.INSTANCE
								: null))
				{
					System.out.println(FileUtils.listFiles(d, new SuffixFileFilter(".owl"), null)
							.size() + "  " + d);

					// if we have any owl files
					boolean createCatalog = false;
					if (command.isAll())
					{
						createCatalog = true;
					} else
					{
						if (FileUtils.listFiles(d, new SuffixFileFilter(".owl"), null).size() > 0)
						{
							createCatalog = true;
						}
					}
					try
					{
						if (createCatalog)
						{
							Path directoryPath = Paths.get(d.toURI());
							XMLCatalog catalog = new XMLCatalog(directoryPath.toUri());
							Set<IRI> iris = new TreeSet<IRI>(mapper.getOntologyIRIs());
							for (IRI iri : iris)
							{
								Path iriPath = Paths.get(mapper.getDocumentIRI(iri).toURI());

								UriEntry entry = new UriEntry("User Entered Import Resolution",
										catalog, iri.toString(), new URI(null, directoryPath
												.relativize(iriPath).toString(), null), null);
								catalog.addEntry(entry);

							}
							FileWriter fw = new FileWriter(new File(d, command.catalogName));
							XMLCatalogWriter writer = new XMLCatalogWriter(catalog, fw);
							writer.write();
							fw.close();

						}
					} catch (URISyntaxException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParserConfigurationException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformerFactoryConfigurationError e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformerException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		};

		public abstract void execute(CatalogCommand command);
	}

}
