package com.essaid.owlcl.core.util;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

public class OntologyLoadingDescriptor implements OWLOntologyIDMapper {

  private String descriptorName;
  private OWLOntologyID ontologyId;

  private List<Path> documentPaths = new ArrayList<Path>();

  private Map<URI, URI> mappings = new TreeMap<URI, URI>();

  private boolean online;

  public OntologyLoadingDescriptor(String name, OWLOntologyID ontologyIri) {
    this.descriptorName = name;
    this.ontologyId = ontologyIri;
  }

  public URI getStatedUriMapping(URI from) {
    return mappings.get(from);
  }

  @Override
  public IRI getDocumentIRI(IRI ontologyIRI) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IRI getDocumentIRI(OWLOntologyID ontologyId) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getDescriptorName() {
    return descriptorName;
  }

  public OWLOntologyID getOntologyId() {
    return ontologyId;
  }

  public List<Path> getDocumentPaths() {
    return new ArrayList<Path>(documentPaths);
  }

  public void setDocumentPaths(List<Path> documentPaths) {
    List<Path> newPaths = new ArrayList<Path>();
    for (Path path : documentPaths)
    {
      try
      {
        Path realPath = path.toRealPath();
        if (!newPaths.contains(realPath))
        {
          newPaths.add(realPath);
        }
      } catch (IOException e)
      {
        throw new RuntimeException("Error converting document path to a real path.", e);
      }
    }
    this.documentPaths = newPaths;
  }

  public boolean addDocumentPath(Path path) {
    try
    {
      Path realPath = path.toRealPath();
      if (documentPaths.contains(realPath))
      {
        return false;
      } else
      {
        documentPaths.add(realPath);
        return true;
      }
    } catch (IOException e)
    {
      throw new RuntimeException("Error converting document path to a real path.", e);
    }
  }

  public Map<URI, URI> getUriMappings() {
    return new TreeMap<URI, URI>(mappings);
  }

  public void setMappings(Map<URI, URI> mappings) throws CyclicUriMapping {
    if (isCyclic(mappings))
    {
      throw new CyclicUriMapping("Cyclic URI mapping");
    }
    this.mappings = new TreeMap<URI, URI>(mappings);
  }

  public void addMapping(URI from, URI to) throws CyclicUriMapping {
    Map<URI, URI> newMapping = new HashMap<URI, URI>(mappings);
    newMapping.put(from, to);
    setMappings(newMapping);
  }

  public boolean isOnline() {
    return online;
  }

  public void setOnline(boolean online) {
    this.online = online;
  }

  // ================================================================================
  //
  // ================================================================================

  private boolean isCyclic(Map<URI, URI> mapping) {

    boolean cyclic = false;
    for (URI uri : mapping.keySet())
    {
      if (isCycle(mapping, uri))
      {
        cyclic = true;
      }
    }
    return cyclic;
  }

  List<URI> seenUris;

  private boolean isCycle(Map<URI, URI> mapping, URI from) {
    if (from == null)
    {
      throw new IllegalStateException("Passed a null URI while checking for mapping cycles.");
    }
    if (seenUris.contains(from))
    {
      return true;
    }
    URI to = mapping.get(from);
    if (to != null)
    {
      seenUris.add(from);
      return isCycle(mapping, to);
    }
    return false;
  }

  // ================================================================================
  // Equality is name based
  // ================================================================================

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (!(obj instanceof OntologyLoadingDescriptor))
    {
      return false;
    }

    OntologyLoadingDescriptor other = (OntologyLoadingDescriptor) obj;

    return this.descriptorName.equals(other.descriptorName);
  }

  @Override
  public int hashCode() {
    return this.descriptorName.hashCode();
  }

}
