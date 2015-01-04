package com.essaid.owlcl.command;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.slf4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.annotation.InjectLogger;
import com.essaid.owlcl.core.cli.util.CanonicalFileConverter;
import com.essaid.owlcl.core.cli.util.IriConverter;
import com.essaid.owlcl.core.reasoner.IReasonerManager;
import com.essaid.owlcl.core.util.OwlclUtil;
import com.google.inject.assistedinject.Assisted;

@Parameters(commandNames = "merge", commandDescription = "Merges an ontology into a single file.")
public class MergeCommand extends AbstractCommand {

  // ================================================================================
  //
  // ================================================================================

  @Parameter(names = "-ontology", description = "The ontology IRI to merge.",
      converter = IriConverter.class)
  public void setOntologyIri(IRI ontologyIri) {
    this.ontologyIri = ontologyIri;
    this.ontologyIriSet = true;
  }

  public IRI getOntologyIri() {
    return ontologyIri;
  }

  public boolean isOntologyIriSet() {
    return ontologyIriSet;
  }

  private IRI ontologyIri;
  private boolean ontologyIriSet;

  // ================================================================================
  //
  // ================================================================================

  @Parameter(names = "-reason", description = "If the ontology should be reasoned before merging. "
      + "This does not imply that it will be saved as reasoned, that is another option.")
  public void setReason(boolean reason) {
    this.reason = reason;
    this.reasonSet = true;
  }

  public boolean isReason() {
    return reason;
  }

  public boolean isReasonSet() {
    return reasonSet;
  }

  private boolean reasonSet;
  private boolean reason;

  // ================================================================================
  //
  // ================================================================================

  @Parameter(names = "-abort", description = "Abort if the ontology is inconsistent, or if "
      + "there are unsatisfiable classes.")
  public void setAbortReasoningProblems(boolean abortReasoningProblems) {
    this.abortReasoningProblems = abortReasoningProblems;
    this.abortReasoningProblemsSet = true;
  }

  public boolean isAbortReasoningProblems() {
    return abortReasoningProblems;
  }

  public boolean isAbortReasoningProblemsSet() {
    return abortReasoningProblemsSet;
  }

  private boolean abortReasoningProblems;
  private boolean abortReasoningProblemsSet;

  // ================================================================================
  //
  // ================================================================================

  public File getOutputFile() {
    return outputFile;
  }

  @Parameter(names = "-outfile", description = "Name and/or path of output file",
      converter = CanonicalFileConverter.class)
  public void setOutputFile(File outputFile) {
    this.outputFile = outputFile;
    this.outputFileSet = true;
  }

  public boolean isOutputFileSet() {
    return outputFileSet;
  }

  private File outputFile;
  private boolean outputFileSet;

  // ================================================================================
  //
  // ================================================================================

  public boolean isSaveReasoned() {
    return saveReasoned;
  }

  @Parameter(names = "-saveReasoned", description = "Save the reasoned ontology.")
  public void setSaveReasoned(boolean saveReasoned) {
    this.saveReasoned = saveReasoned;
    this.saveReasonedSet = true;
  }

  public boolean isSaveReasonedSet() {
    return saveReasonedSet;
  }

  private boolean saveReasoned;
  private boolean saveReasonedSet;

  // ================================================================================
  //
  // ================================================================================

  private void configure() {
    if (!reasonSet)
    {
      reason = false;
    }

    if (!abortReasoningProblemsSet)
    {
      abortReasoningProblems = true;
    }

    if (!outputFileSet)
    {
      outputFile = new File(getMain().getJobDirectory(), "merged-ontology.owl");
    }

    if (!saveReasonedSet)
    {
      saveReasoned = false;
    }

  }

  // ================================================================================
  // Implementation
  // ================================================================================

  @javax.inject.Inject
  private IReasonerManager reasonerManager;

  @InjectLogger
  private Logger logger;

  @javax.inject.Inject
  public MergeCommand(@Assisted OwlclCommand main) {
    super(main);
    configure();
  }

  @Override
  protected void addCommandActions(List<String> actionsList) {
    // TODO Auto-generated method stub

  }

  @Override
  public Object call() throws Exception {
    configure();
    if (ontologyIri == null)
    {
      logger.error("Merge command called without an ontology IRI, aborting.");
      return null;
    }
    final OWLOntology o = OwlclUtil
        .getOrLoadOntology(ontologyIri, getMain().getSharedBaseManager());
    OWLOntologySetProvider provider = new OWLOntologySetProvider() {

      @Override
      public Set<OWLOntology> getOntologies() {
        // TODO Auto-generated method stub
        return o.getImportsClosure();
      }
    };

    OWLOntologyMerger merger = new OWLOntologyMerger(provider);
    OWLOntologyManager man = OWLManager.createOWLOntologyManager();
    OWLOntology mergedontology = merger.createMergedOntology(man, ontologyIri);

    OWLReasoner reasoner = null;
    if (saveReasoned || reason)
    {
      reasoner = reasonerManager.getReasonedOntology(mergedontology);
    }
    if (reasoner != null && abortReasoningProblems)
    {
      if (!reasoner.isConsistent())
      {
        logger.error("Merged ontology is inconsistent, aborting.");
        return null;
      } else
      {
        logger.info("Ontology was consistent during reasoned merge.");
      }
      Set<OWLClass> nothings = reasoner.getUnsatisfiableClasses().getEntitiesMinus(
          man.getOWLDataFactory().getOWLNothing());
      if (nothings.size() > 0)
      {
        logger.error("Unsatisfiable classes found during merge. Classes: " + nothings.toString());
        return null;
      } else
      {
        logger.info("No unsatisfiable classes found during reasoned merge.");
      }
    }

    if (saveReasoned)
    {
      InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner);
      iog.fillOntology(man, mergedontology);
    }

    man.saveOntology(mergedontology, new FileOutputStream(outputFile));

    return null;
  }

  @Override
  protected void doInitialize() {
    // TODO Auto-generated method stub

  }

}
