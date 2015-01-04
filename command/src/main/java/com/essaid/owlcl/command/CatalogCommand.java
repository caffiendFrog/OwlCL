package com.essaid.owlcl.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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

  @Parameter(names = "-target",
      description = "The top directory to start cataloging from.",
      converter = CanonicalFileConverter.class,
      validateValueWith = DirectoryExistsValueValidator.class)
  public void setTargetDirectory(File directory) {
    this.targetDirectory = directory;
    this.targetDirectorySet = true;
  }

  public File getTargetDirectory() {
    return targetDirectory;
  }

  public boolean isTargetDirectorySet() {
    return targetDirectorySet;
  }

  private File targetDirectory;
  private boolean targetDirectorySet;

  // ================================================================================
  // Do subdirectories?
  // ================================================================================

  @Parameter(names = "-noTargetSubs", description = "Set to false if you only want the specified "
      + "directory cataloged without doing the sub directories.")
  public void setNoTargetSubs(boolean noSubs) {
    this.noTargetSubs = noSubs;
    this.noTargetSubsSet = true;
  }

  public boolean isNoTargetSubs() {
    return noTargetSubs;
  }

  public boolean isNoTargetSubsSet() {
    return noTargetSubsSet;
  }

  private boolean noTargetSubs = false;
  private boolean noTargetSubsSet;

  // ================================================================================
  // source files/folders
  // ================================================================================

  @Parameter(names = "-sources",
      description = "The source files/directories to consider in the catalog.",
      converter = CanonicalFileConverter.class, variableArity = true)
  public void setSourceFiles(List<File> sourceFiles) {
    this.sourceFiles = sourceFiles;
    this.sourceFilesSet = true;
  }

  public List<File> getSourceFiles() {
    return sourceFiles;
  }

  public boolean isSourceFilesSet() {
    return sourceFilesSet;
  }

  private List<File> sourceFiles = new ArrayList<File>();
  private boolean sourceFilesSet = false;

  // ================================================================================
  // source subs
  // ================================================================================

  @Parameter(names = "-noSourceSubs", description = "Don't consider source sub directories.")
  public void setNoSourceSubs(boolean noSourceSubs) {
    this.noSourceSubs = noSourceSubs;
    this.noSourceSubsSet = true;
  }

  public boolean isNoSourceSubs() {
    return noSourceSubs;
  }

  public boolean isNoSourceSubsSet() {
    return noSourceSubsSet;
  }

  private boolean noSourceSubs = false;
  private boolean noSourceSubsSet = false;

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

    if (!targetDirectorySet)
    {
      targetDirectory = getMain().getProject();
    }

    if (!sourceFilesSet)
    {
      sourceFiles.add(getMain().getProject());
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

        Map<IRI, IRI> ontologyToDocumentMap = new HashMap<IRI, IRI>();

        for (File file : command.getSourceFiles())
        {
          if (file.isDirectory())
          {
            AutoIRIMapper mapper = new AutoIRIMapper(file, !command.noTargetSubs);
            for (IRI oIri : mapper.getOntologyIRIs())
            {
              ontologyToDocumentMap.put(oIri, mapper.getDocumentIRI(oIri));
            }
          } else
          {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            OWLOntologyLoaderConfiguration lc = new OWLOntologyLoaderConfiguration();
            lc = lc.setFollowRedirects(false);
            lc = lc.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
            IRI oIri = null;
            try
            {
              oIri = man.loadOntologyFromOntologyDocument(new FileDocumentSource(file), lc)
                  .getOntologyID().getOntologyIRI();
            } catch (OWLOntologyCreationException e)
            {
              throw new RuntimeException("Failed to load source file: " + file, e);
            }
            ontologyToDocumentMap.put(oIri, IRI.create(file));
          }
        }

        File topDirectory = command.getTargetDirectory();

        // for all directories
        for (File d : FileUtils.listFilesAndDirs(topDirectory, DirectoryFileFilter.INSTANCE,
            command.isNoTargetSubs() ? null : TrueFileFilter.INSTANCE))
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
              for (IRI iri : ontologyToDocumentMap.keySet())
              {
                Path iriPath = Paths.get(ontologyToDocumentMap.get(iri).toURI());

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
