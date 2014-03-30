package com.essaid.owlcl.command;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.cli.util.CanonicalFileConverter;
import com.essaid.owlcl.core.util.OntologyFiles;
import com.essaid.owlcl.core.util.Report;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@Parameters(commandNames = "typecheck", commandDescription = "Checks if an IRI has more than "
    + "one type in the files, directories, and any of their imports."
    + " Imports are loaded from local files when possible, otherwise, loaded online. "
    + "All ontologies are loaded from/into a single manager.")
public class TypecheckCommand extends AbstractCommand {

  // ================================================================================
  // Files and directories to check types
  // ================================================================================

  public List<File> files;
  public boolean fileSet;

  @Parameter(names = "-files",
      description = "One or more files or directories to check IRI types.",
      converter = CanonicalFileConverter.class)
  public void setFiles(List<File> files) {
    this.files = files;
    this.fileSet = true;
  }

  public List<File> getFiles() {
    return files;
  }

  // ================================================================================
  // Subdirectories
  // ================================================================================

  public boolean subDir = true;
  public boolean subDirSet;

  public boolean isSubDir() {
    return subDir;
  }

  @Parameter(names = "-subs", arity = 1, description = "Include sub directories when "
      + "looking for ontology/owl files.")
  public void setSubDir(boolean subDir) {
    this.subDir = subDir;
    this.subDirSet = true;
  }

  // ================================================================================
  // Add typing axioms where needed?
  // ================================================================================

  @Parameter(
      names = "-addTypes",
      description = "Will add type axioms (declartions) where an IRI is used "
          + "but its type is not asserted. This helps resolve certain reasoning issues. If an IRI "
          + "has multiple types as reported by this tool, all types will be asserted by this action so "
          + "the files should be cleaned up before doing this if needed and checked again after running "
          + "this command.")
  public boolean addTypes = false;

  // ================================================================================
  // Implementation
  // ================================================================================

  // Map<File, OWLOntology> ontologies = new HashMap<File, OWLOntology>();

  OntologyFiles ontologyFiles;
  Map<IRI, Set<OWLEntity>> iriToEntityMap = new HashMap<IRI, Set<OWLEntity>>();

  List<OWLOntology> ontologies = new ArrayList<OWLOntology>();

  @Inject
  public TypecheckCommand(@Assisted OwlclCommand main) {
    super(main);
    configure();
  }

  Report report;

  public void run() {

    report = new Report("typecheckReport");
    ontologyFiles = new OntologyFiles(files, subDir);

    for (Entry<File, IRI> entry : ontologyFiles.getLocalOntologyFiles(null).entrySet())
    {
      // need new managers to make sure we can load duplicate ontology
      // IRIs
      OWLOntologyManager man = getMain().getNewBaseManager();
      ontologyFiles.setupManager(man, null);
      try
      {
        ontologies.add(man.loadOntology(entry.getValue()));
      } catch (OWLOntologyCreationException e)
      {
        throw new RuntimeException("Failed to load ontology file to find types. " + "File: "
            + entry.getKey() + " ontology IRI: " + entry.getValue(), e);

      }
    }

    for (String action : getAllActions())
    {
      Action.valueOf(action).execute(this);
    }
    report.finish();
  }

  @Override
  protected void addCommandActions(List<String> actionsList) {
    actionsList.add(Action.duplicatIris.name());
    actionsList.add(Action.checkTypes.name());
    if (addTypes)
    {
      actionsList.add(Action.addTypes.name());
    }
  }

  enum Action {
    duplicatIris {

      @Override
      public void execute(TypecheckCommand command) {

        command.report.info("");
        command.report.info("==================================================");
        command.report.info("==========   Checking for duplicate IRIs  ========");
        command.report.info("==================================================");
        command.report.info("");

        // first warn about duplicate IRIs that will hide files from the
        // type
        // checking
        for (Entry<IRI, List<File>> entry : command.ontologyFiles.getDuplicateIris(null).entrySet())
        {
          command.report.info("Duplicate ontology IRI: " + entry.getKey());
          for (File file : entry.getValue())
          {
            command.report.info("\tIn file: " + file.getAbsolutePath());
          }
        }
      }
    },
    checkTypes {

      @Override
      public void execute(TypecheckCommand command) {

        command.report.info("");
        command.report.info("==================================================");
        command.report.info("==========   Checking for multiple types  ========");
        command.report.info("==================================================");
        command.report.info("");

        for (OWLOntology o : command.ontologies)
        {
          for (OWLEntity entity : o.getSignature(true))
          {
            Set<OWLEntity> entities = command.iriToEntityMap.get(entity.getIRI());
            if (entities == null)
            {
              entities = new HashSet<OWLEntity>();
              command.iriToEntityMap.put(entity.getIRI(), entities);

            }
            entities.add(entity);
          }

        }

        // now iterate over all IRIs and their entities
        for (Entry<IRI, Set<OWLEntity>> entry : command.iriToEntityMap.entrySet())
        {
          if (entry.getValue().size() > 1)
          {
            command.report.info("Multiple types for IRI: " + entry.getKey());
            String types = "";
            for (OWLEntity entity : entry.getValue())
            {
              types += entity.getEntityType() + " ";
            }
            command.report.info("\tTypes: " + types);
          }
        }
        command.ontologies.clear();
      }

    },
    addTypes {

      @SuppressWarnings("deprecation")
      @Override
      public void execute(TypecheckCommand command) {

        command.report.info("");
        command.report.info("==================================================");
        command.report.info("==========   Adding types  =======================");
        command.report.info("==================================================");
        command.report.info("");

        for (Entry<File, IRI> entry : command.ontologyFiles.getLocalOntologyFiles(null).entrySet())
        {
          // we want to intentionally ignore imports
          OWLOntologyManager man = OWLManager.createOWLOntologyManager();
          man.clearIRIMappers();
          man.setSilentMissingImportsHandling(true);

          boolean changed = false;

          Set<IRI> iris = new HashSet<IRI>();

          OWLOntology o = null;
          try
          {
            o = man.loadOntologyFromOntologyDocument(entry.getKey());
          } catch (OWLOntologyCreationException e2)
          {
            throw new RuntimeException("Failed to load ontology to add types. " + "File: "
                + entry.getKey() + " and IRI: " + entry.getValue(), e2);
          }

          for (OWLEntity e : o.getSignature(false))
          {
            iris.add(e.getIRI());
          }
          for (OWLAnnotationAssertionAxiom a : o.getAxioms(AxiomType.ANNOTATION_ASSERTION))
          {
            if (a.getSubject() instanceof IRI)
            {
              iris.add((IRI) a.getSubject());
            }
            if (a.getValue() instanceof IRI)
            {
              iris.add((IRI) a.getValue());
            }
          }

          for (IRI iri : iris)
          {
            Set<OWLEntity> entities = command.iriToEntityMap.get(iri);
            if (entities != null)
            {
              if (entities.size() > 1)
              {
                String types = "";
                for (OWLEntity e : entities)
                {
                  types += e.getEntityType() + " ";
                }
                command.report.info("IRI " + iri + " will have multiple types in file: "
                    + entry.getKey());

                command.report.info("\tTypes: " + types);
              }
              // add the types, and save later if changed.
              for (OWLEntity e : entities)
              {
                OWLDeclarationAxiom da = man.getOWLDataFactory().getOWLDeclarationAxiom(e);
                if (!man.addAxiom(o, da).isEmpty())
                {
                  changed = true;
                  command.report.detail("Added type " + e.getEntityType() + " to IRI " + iri
                      + " in file: " + entry.getValue() + " for ontology IRI: " + entry.getValue());
                }
              }
            }
          }

          if (changed)
          {
            try
            {
              man.saveOntology(o);
              command.report.info("Saving ontology: " + entry.getValue() + " to file: "
                  + entry.getKey());
            } catch (OWLOntologyStorageException e1)
            {
              throw new RuntimeException("Failed to save ontology after adding types. " + "File: "
                  + entry.getKey() + " IRI: " + entry.getValue(), e1);
            }
          }
        }

      }
    };

    public abstract void execute(TypecheckCommand command);
  }

  protected void configure() {
    // TODO Auto-generated method stub

  }

  protected void init() {
    // TODO Auto-generated method stub

  }

  @Override
  public Object call() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
