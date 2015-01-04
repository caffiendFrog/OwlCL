package dave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;

public class FindUnknown {

  static Set<IRI> iris = new HashSet<IRI>();

  public static void main(String[] args) throws OWLOntologyCreationException, IOException {
    OWLOntologyManager man = OWLManager.createOWLOntologyManager();
    AutoIRIMapper m = new AutoIRIMapper(new File(
        "/srv/pass-through/git/googlecode/isf-svn/trunk/src/ontology/"), true);
    man.clearIRIMappers();
    man.addIRIMapper(m);
    OWLOntology o = man.loadOntology(IRI
        .create("http://purl.obolibrary.org/obo/arg/isf-full-dev.owl"));

    for (OWLEntity e : o.getSignature(true))
    {
      iris.add(e.getIRI());
    }
    FileReader fr = new FileReader("/srv/vbox-share-s/dave/classes.txt");
    BufferedReader br = new BufferedReader(fr);
    System.out.println("Classes: ");
    checkIris(br);

    fr = new FileReader("/srv/vbox-share-s/dave/classes_profiles.txt");
    br = new BufferedReader(fr);
    System.out.println("\n\nProfiles classes: ");
    checkIris(br);

    fr = new FileReader("/srv/vbox-share-s/dave/predicates.txt");
    br = new BufferedReader(fr);
    System.out.println("\n\nPredicates: ");
    checkIris(br);

    fr = new FileReader("/srv/vbox-share-s/dave/predicates_profiles.txt");
    br = new BufferedReader(fr);
    System.out.println("\n\nProfiles predicates: ");
    checkIris(br);
  }

  static void checkIris(BufferedReader br) throws IOException {
    String line = null;
    while ((line = br.readLine()) != null)
    {
      String[] parts = line.split("\\|");
      String iri = parts[0];
      String count = parts[1];
      if (iris.contains(IRI.create(iri.trim())))
      {
        System.out
            .println((iri.trim() + "-------------------------------------------------------------------------------------------------------------")
                .substring(0, 130) + "| " + count.trim() + "  yes");

      } else
      {
        System.out
            .println((iri.trim() + "-------------------------------------------------------------------------------------------------------------")
                .substring(0, 130) + "| " + count.trim() + "  no");

      }
    }
  }
}
