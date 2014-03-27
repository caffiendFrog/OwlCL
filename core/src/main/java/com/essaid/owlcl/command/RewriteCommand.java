package com.essaid.owlcl.command;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.essaid.owlcl.command.cli.CanonicalFileConverter;
import com.essaid.owlcl.core.command.MainCommand;
import com.essaid.owlcl.util.OntologyFiles;

@Parameters(commandNames = "rewrite", commandDescription = "Rewrites the OWL files.")
public class RewriteCommand extends AbstractCommand {

	// ================================================================================
	// Format
	// ================================================================================
	public String format = "rdfxml";
	public boolean formatSet;

	@Parameter(names = "-format", description = "Which formats, valid options include rdfxml ")
	public void setFormat(String format) {
		this.format = format;
		this.formatSet = true;
	}

	public String getFormat() {
		return format;
	}

	// ================================================================================
	// Files
	// ================================================================================

	@Parameter(names = "-files", description = "Files or directories to search for files "
			+ "to rewrite", converter = CanonicalFileConverter.class)
	public List<File> files;
	public boolean filesSet;

	// ================================================================================
	// Implementation
	// ================================================================================
	public RewriteCommand(MainCommand main) {
		super(main);
		configure();
	}

	@Override
	protected void addCommandActions(List<String> actionsList) {
		// TODO Auto-generated method stub
	}

	@SuppressWarnings("deprecation")
	public void run() {
		OntologyFiles ofu = new OntologyFiles(files, true);
		Set<Entry<File, IRI>> entries = ofu.getLocalOntologyFiles(null).entrySet();

		for (Entry<File, IRI> entry : entries)
		{
			// if (entry.getValue() == null)
			// continue;
			// if (entry.getKey().getPath().endsWith("catalog-v001.xml"))
			// {
			// System.out.println("catalog:");
			// System.out.println(entry.getValue());
			// }
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			man.clearIRIMappers();
			man.setSilentMissingImportsHandling(true);
			OWLOntology ontology = null;
			try
			{
				ontology = man.loadOntologyFromOntologyDocument(entry.getKey());
			} catch (OWLOntologyCreationException e)
			{
				throw new RuntimeException(
						"Format: error loading ontology file: " + entry.getKey(), e);
			}

			OWLOntologyFormat ontologyFormat = null;
			if (format.equals("rdfxml"))
			{
				RDFXMLOntologyFormat rdfformat = new RDFXMLOntologyFormat();
				rdfformat.setAddMissingTypes(true);
				ontologyFormat = rdfformat;
			}

			try
			{
				man.saveOntology(ontology, ontologyFormat);
			} catch (OWLOntologyStorageException e)
			{
				throw new RuntimeException("Format: error saving ontology file: " + entry.getKey(),
						e);
			}
		}

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
