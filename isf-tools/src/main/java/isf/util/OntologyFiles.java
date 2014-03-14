package isf.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OntologyFiles {

	private List<File> files;
	private boolean includeSubs;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * A utility class to help discover and analyze ontology files in a set of
	 * files or directories.
	 * 
	 * @param files
	 */
	public OntologyFiles(List<File> files, boolean includeSubs) {
		this.files = files;
		this.includeSubs = includeSubs;
	}

	Map<IRI, List<File>> iriToFileMap = null;

	/**
	 * Duplicate IRIs are IRIs that exist in more than one local file
	 * 
	 * @param includeSubs
	 * @param exceptions
	 * @return
	 */
	public Map<IRI, List<File>> getDuplicateIris(Map<File, Exception> exceptions) {
		if (iriToFileMap == null)
		{

			iriToFileMap = new HashMap<IRI, List<File>>();
			for (Entry<File, IRI> entry : getLocalOntologyFiles(exceptions).entrySet())
			{
				List<File> files = iriToFileMap.get(entry.getValue());
				if (files == null)
				{
					files = new ArrayList<File>();
					iriToFileMap.put(entry.getValue(), files);
				}
				files.add(entry.getKey());
			}

			for (Iterator<Entry<IRI, List<File>>> i = iriToFileMap.entrySet().iterator(); i
					.hasNext();)
			{
				if (i.next().getValue().size() < 2)
				{
					i.remove();
				}
			}
		}
		return iriToFileMap;
	}

	Map<File, IRI> fileToIriMap = null;
	Map<File, OWLOntology> fileToOntologyMap = null;

	@SuppressWarnings("deprecation")
	public Map<File, IRI> getLocalOntologyFiles(Map<File, Exception> exceptions) {
		if (fileToIriMap == null)
		{
			fileToIriMap = new HashMap<File, IRI>();
			fileToOntologyMap = new HashMap<File, OWLOntology>();
			for (File file : getAllFiles())
			{
				OWLOntology o = null;
				OWLOntologyManager man = OWLManager.createOWLOntologyManager();
				man.clearIRIMappers();
				man.setSilentMissingImportsHandling(true);
				try
				{
					logger.info("Trying to load file: " + file.getAbsolutePath());
					o = man.loadOntologyFromOntologyDocument(file);
				} catch (OWLOntologyCreationException e)
				{
					if (exceptions != null)
					{
						exceptions.put(file, e);
					}
					logger.info("Problem loading file as ontology file. " + e.getMessage(), e);
					continue;
				}
				if (o.getOntologyID().isAnonymous())
				{
					continue;
				}
				logger.info("Found ontology: " + o.getOntologyID() + " in file: "
						+ file.getAbsolutePath());
				fileToIriMap.put(file, o.getOntologyID().getOntologyIRI());
			}
		}
		return fileToIriMap;
	}

	List<File> allFiles = null;

	public List<File> getAllFiles() {
		if (allFiles == null)
		{
			allFiles = new ArrayList<File>();
			for (File file : files)
			{
				if (file.isFile())
				{
					try
					{
						allFiles.add(file.getCanonicalFile());
					} catch (IOException e)
					{
						throw new RuntimeException("Failed to get canonical file for: " + file, e);
					}
				} else if (file.isDirectory())
				{
					for (File file2 : FileUtils.listFiles(file, TrueFileFilter.INSTANCE,
							includeSubs ? TrueFileFilter.INSTANCE : null))
					{
						try
						{
							allFiles.add(file2.getCanonicalFile());
						} catch (IOException e)
						{
							throw new RuntimeException(
									"Failed to get canonical file for: " + file2, e);
						}

					}
				}
			}
		}
		return allFiles;
	}

	Map<File, Set<IRI>> iris;

	public Map<File, Set<IRI>> getLocallyUnresolvableIris(Map<File, Exception> exceptions) {
		if (iris == null)
		{
			iris = new HashMap<File, Set<IRI>>();
			Map<File, IRI> localOntologies = getLocalOntologyFiles(exceptions);
			for (final Entry<File, IRI> entry : localOntologies.entrySet())
			{
				OWLOntologyManager man = OWLManager.createOWLOntologyManager();
				// this mapper catches all requests that were not found by
				// higher mappings
				man.addIRIMapper(new OWLOntologyIRIMapper() {

					@Override
					public IRI getDocumentIRI(IRI ontologyIRI) {
						Set<IRI> fileIris = iris.get(entry.getKey());
						if (fileIris == null)
						{
							fileIris = new HashSet<IRI>();
							iris.put(entry.getKey(), fileIris);
						}
						fileIris.add(ontologyIRI);
						return null;
					}
				});
				setupManager(man, exceptions);
				try
				{
					man.loadOntology(entry.getValue());
				} catch (OWLOntologyCreationException e1)
				{
					if (exceptions != null)
					{
						exceptions.put(entry.getKey(), e1);
					}
				}
			}
		}
		return iris;
	}

	public void setupManager(OWLOntologyManager manager, Map<File, Exception> exceptions) {
		for (Entry<File, IRI> entry : getLocalOntologyFiles(exceptions).entrySet())
		{
			manager.addIRIMapper(new SimpleIRIMapper(entry.getValue(), IRI.create(entry.getKey())));
		}
	}
}
