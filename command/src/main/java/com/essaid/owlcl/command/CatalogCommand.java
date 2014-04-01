package com.essaid.owlcl.command;

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
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.cli.util.CanonicalFileConverter;
import com.essaid.owlcl.core.cli.util.DirectoryExistsValueValidator;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@Parameters(commandNames = "catalog", commandDescription = "Creates catalog files based on the "
    + "existing OWL files in the specified directory and optionally its sub directories. "
    + "It is important that this directory does not contain multiple ontology files with "
    + "same ontology IRI. Otherwise, the catalog will only point to one of the files and this "
    + "could lead to unexpected results.")
public class CatalogCommand extends AbstractCommand {

  // ================================================================================
  // The top directory to catalog from
  // ================================================================================

  @Parameter(names = "-directory", description = "The top directory to start cataloging from.",
      converter = CanonicalFileConverter.class,
      validateValueWith = DirectoryExistsValueValidator.class)
  public void setDirectory(File directory) {
    this.directory = directory;
    this.directorySet = true;
  }

  public File getDirectory() {
    return directory;
  }

  public boolean isDirectorySet() {
    return directorySet;
  }

  private File directory;
  private boolean directorySet;

  // ================================================================================
  // Do subdirectories?
  // ================================================================================

  @Parameter(names = "-noSubs", description = "Set to false if you only want the specified "
      + "directory cataloged without doing the sub directories.")
  public void setNoSubs(boolean noSubs) {
    this.noSubs = noSubs;
    this.noSubsSet = true;
  }

  public boolean isNoSubs() {
    return noSubs;
  }

  public boolean isNoSubsSet() {
    return noSubsSet;
  }

  private boolean noSubs = false;
  private boolean noSubsSet;

  // ================================================================================
  // Which directories to catalog, all or just *.owl ones

  // ================================================================================

  @Parameter(names = "-all",
      description = "Create catalog files in all directories. Otherwise, only directories with "
          + "*.owl files will be cataloged. Default is only for *.owl directories.")
  public void setAll(boolean all) {
    this.all = all;
    this.allSet = true;
  }

  public boolean isAll() {
    return all;
  }

  public boolean isAllSet() {
    return allSet;
  }

  private boolean all = false;
  private boolean allSet = false;

  // ================================================================================
  // Catalog file name
  // ================================================================================

  @Parameter(names = "-name", description = "The file name for generated catalog files. "
      + "The protege catalog name is the default value.")
  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
    this.catalogNameSet = true;
  }

  public String getCatalogName() {
    return catalogName;
  }

  public boolean isCatalogNameSet() {
    return catalogNameSet;
  }

  private String catalogName = "catalog-v001.xml";
  private boolean catalogNameSet;

  // ================================================================================
  // Initialization
  // ================================================================================
  protected void configure() {

    if (!directorySet)
    {
      directory = getMain().getProject();
    }

  }

  // ================================================================================
  // Implementation
  // ================================================================================

  @Inject
  public CatalogCommand(@Assisted OwlclCommand main) {
    super(main);
  }

  @Override
  protected void doInitialize() {
    configure();
  }

  @Override
  protected void addCommandActions(List<String> actionsList) {
    actionsList.add(Action.create.name());
  }

  @Override
  public Object call() throws Exception {
    configure();

    for (String action : getAllActions())
    {
      Action.valueOf(action).execute(this);
    }
    return null;
  }

  enum Action {
    create {

      @Override
      public void execute(CatalogCommand command) {
        command.getLogger().info("Creating catalogs.");
        AutoIRIMapper mapper = new AutoIRIMapper(command.getDirectory(), !command.noSubs);

        File topDirectory = command.getDirectory();

        // for all directories
        for (File d : FileUtils.listFilesAndDirs(topDirectory, DirectoryFileFilter.INSTANCE,
            command.isNoSubs() ? null : TrueFileFilter.INSTANCE))
        {

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

                UriEntry entry = new UriEntry("User Entered Import Resolution", catalog,
                    iri.toString(), new URI(null, directoryPath.relativize(iriPath).toString(),
                        null), null);
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
