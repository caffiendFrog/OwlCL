package com.essaid.owlcl.core.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.FreshEntitiesException;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
// import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;

import com.essaid.owlcl.core.IOwlclManager;

/**
 * @author Shahim Essaid
 * 
 */
public class OwlclUtil {

  private static final OwlclUtil instance = new OwlclUtil();

  public static OwlclUtil instance() {
    return instance;
  }

  private URL codeUrl;
  private File codeJar;
  private File codeDirectory;
  private File codeExtDirectory;

  private File homeDirectory;
  private File currentDirectory;

  private File workDirectory;
  private File workExtDirectory;

  private Path temporaryDirectory;

  private Object lock = new Object();

  private boolean initted;

  public void init() {
    synchronized (lock) {
      if (!initted) {
        return;
      }
      initDirectories();
      initClasspath();
      initTemporaryDirectory();
    }

  }

  private void initTemporaryDirectory() {
    try {
      temporaryDirectory = Files.createTempDirectory("owlcl-tmp-directory");
    } catch (IOException e) {
      throw new RuntimeException("Error creating temporary directory", e);
    }
    Runtime.getRuntime().addShutdownHook(new Thread() {

      public void run() {
        delete(temporaryDirectory.toFile());
      }

      void delete(File f) {
        if (f.isDirectory()) {
          for (File c : f.listFiles())
            delete(c);
        }
        if (!f.delete())
          throw new RuntimeException("Failed to delete file: " + f);
      }
    });

  }

  private void initDirectories() {

    // find code directory
    codeUrl = this.getClass().getProtectionDomain().getCodeSource().getLocation();
    File codeSource = null;

    try {
      codeSource = new File(codeUrl.toURI()).getCanonicalFile();
    } catch (URISyntaxException e1) {
      throw new RuntimeException("Error getting code source URI from invalide URL:" + codeUrl, e1);
    } catch (IOException e) {
      throw new RuntimeException("Error getting canonical code source File from URL:" + codeUrl, e);
    }

    if (codeSource.isDirectory()) {
      codeDirectory = codeSource;

    } else {
      if (codeSource.getPath().endsWith(".jar")) {
        codeJar = codeSource;
        codeDirectory = codeSource.getParentFile();
      } else {
        throw new RuntimeException("Code source is not a directory or a *.jar file.");
      }
    }
    codeExtDirectory = new File(codeDirectory, IOwlclManager.OWLCL_EXT_DIR);

    // find caller's current directory;
    try {
      currentDirectory = new File(System.getProperty("user.dir")).getCanonicalFile();
    } catch (IOException e) {
      throw new RuntimeException("Error getting current directory.", e);
    }

    // find caller's home directory;
    try {
      homeDirectory = new File(System.getProperty("user.home")).getCanonicalFile();
    } catch (IOException e) {
      throw new RuntimeException("Error getting home directory.", e);
    }

    // first find working directory
    String workDir = System.getProperty(IOwlclManager.OWLCL_WORK_DIR_PROPERTY);
    if (workDir == null) {
      for (Entry<String, String> envEntry : System.getenv().entrySet()) {
        if (envEntry.getKey().toUpperCase()
            .equals(IOwlclManager.OWLCL_WORK_DIR_PROPERTY.toUpperCase().replace(".", "_"))) {
          workDir = envEntry.getValue();
          break;
        }
      }
    }
    if (workDir != null) {
      try {
        workDirectory = new File(workDir).getCanonicalFile();
        if (!workDirectory.exists()) {
          throw new IllegalStateException("The work directory: " + workDirectory
              + " does not exist.");
        }
      } catch (IOException e) {
        throw new RuntimeException("Error getting canonical work directory for path: " + workDir, e);
      }
    } else {
      workDirectory = currentDirectory;
    }

    workExtDirectory = new File(workDirectory, IOwlclManager.OWLCL_EXT_DIR);
    //
    System.out.println("Home directory: " + homeDirectory);
    System.out.println("Current directory: " + currentDirectory);

    System.out.println("Code URL: " + codeUrl);
    System.out.println("Code Jar: " + codeJar);
    System.out.println("Code ext: " + codeExtDirectory);
    System.out.println("Code directory: " + codeDirectory);

    System.out.println("Work directory: " + workDirectory);
    System.out.println("Work ext directory: " + workExtDirectory);
  }

  @SuppressWarnings("resource")
  private void initClasspath() {

    ClassLoader systemLoader = getClass().getClassLoader().getSystemClassLoader();
    if (systemLoader instanceof URLClassLoader) {
      URLClassLoader urlcl = (URLClassLoader) systemLoader;
      Method addURL = null;
      try {
        Class current = urlcl.getClass();
        while (current != ClassLoader.class) {
          for (Method method : current.getDeclaredMethods()) {
            if (method.getName().equals("addURL")) {
              addURL = method;
              break;
            }
          }
          current = current.getSuperclass();

        }
      } catch (SecurityException e) {
        throw new RuntimeException("Could not setup classpath due to security enforced.", e);
      }
      if (addURL != null) {
        addURL.setAccessible(true);
      } else {
        throw new RuntimeException("Failed to find addURL Method in URL classloader. Giving up.");
      }

      if (codeExtDirectory.exists()) {
        for (File file : codeExtDirectory.listFiles()) {
          addFileToClasspath(file, urlcl, addURL);
        }
      }
      if (workExtDirectory.exists()) {
        for (File file : workExtDirectory.listFiles()) {
          addFileToClasspath(file, urlcl, addURL);
        }
      }

    } else {
      throw new IllegalStateException(
          "System class loader is not a URL class loader. Can't load application.");
    }

  }

  private void addFileToClasspath(File file, ClassLoader urlcl, Method addURL) {
    if (file.getPath().endsWith(".jar") || file.isDirectory()) {
      System.out.println("Adding to classpath: " + file);
      URL fileUrl;
      try {
        fileUrl = file.toURI().toURL();
      } catch (MalformedURLException e) {
        throw new RuntimeException("Error creating a URL for file " + file, e);
      }
      try {
        System.out.println("Adding URL to classpath: " + fileUrl);
        addURL.invoke(urlcl, fileUrl);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new RuntimeException("Error adding file: " + file + " to classpath", e);
      }

      for (File subFile : file.listFiles()) {
        if (subFile.isFile() && subFile.getPath().endsWith(".jar")) {
          addFileToClasspath(subFile, urlcl, addURL);
        }
      }
    }

  }

  public Path getTemporaryDirectory() {

    if (!initted) {
      init();
    }

    return temporaryDirectory;
  }

  // ================================================================================
  // OWL helper methods
  // ================================================================================

  private static OWLDataFactory df = OWLManager.getOWLDataFactory();

  @SuppressWarnings("unused")
  private static boolean ___________OWL_HELPERS________________;

  public static OWLAnnotationProperty getAnnotationProperty(String iri) {
    return df.getOWLAnnotationProperty(IRI.create(iri));
  }

  public static OWLObjectProperty getObjectProperty(String iri) {
    return df.getOWLObjectProperty(IRI.create(iri));
  }

  public static OWLDataProperty getDataProperty(String iri) {
    return df.getOWLDataProperty(IRI.create(iri));
  }

  public static Set<String> getOntologyAnnotationLiteralValues(OWLAnnotationProperty property,
      OWLOntology ontology, boolean recursive) {
    Set<String> values = new HashSet<String>();
    Set<OWLOntology> ontologies = null;
    if (recursive) {
      ontologies = ontology.getImportsClosure();
    } else {
      ontologies = Collections.singleton(ontology);
    }
    for (OWLOntology o : ontologies) {
      for (OWLAnnotation a : o.getAnnotations()) {
        if (a.getProperty().equals(property)) {
          values.add(((OWLLiteral) a.getValue()).getLiteral());
        }
      }
    }

    return values;
  }

  public static Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(OWLOntology ontology,
      OWLAnnotationProperty property, boolean includeImports) {
    Set<OWLAnnotationAssertionAxiom> axioms = new HashSet<OWLAnnotationAssertionAxiom>();
    Set<OWLOntology> ontologies;
    if (includeImports) {
      ontologies = ontology.getImportsClosure();
    } else {
      ontologies = Collections.singleton(ontology);
    }
    for (OWLOntology o : ontologies) {
      for (OWLAnnotationAssertionAxiom aaa : o.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
        if (aaa.getProperty().getIRI().equals(property.getIRI())) {
          axioms.add(aaa);
        }
      }
    }
    return axioms;
  }

  public static Set<OWLEntity> getSubs(OWLEntity entity, final boolean closure, final OWLReasoner pr) {
    final Set<OWLEntity> entities = new HashSet<OWLEntity>();
    entities.add(entity);

    entity.accept(new OWLEntityVisitor() {

      @Override
      public void visit(OWLAnnotationProperty property) {
        // TODO Auto-generated method stub

      }

      @Override
      public void visit(OWLDatatype datatype) {
        // TODO Auto-generated method stub

      }

      @Override
      public void visit(OWLNamedIndividual individual) {
        // TODO Auto-generated method stub

      }

      @Override
      public void visit(OWLDataProperty property) {
        entities.add(property);
        entities.addAll(pr.getSubDataProperties(property, !closure).getFlattened());

      }

      @Override
      public void visit(OWLObjectProperty property) {
        entities.add(property);
        Set<OWLObjectPropertyExpression> opes =
            pr.getSubObjectProperties(property, !closure).getFlattened();
        for (OWLObjectPropertyExpression ope : opes) {
          if (ope instanceof OWLObjectProperty) {
            entities.add((OWLObjectProperty) ope);
          }
        }

      }

      @Override
      public void visit(OWLClass cls) {
        entities.add(cls);
        entities.addAll(pr.getSubClasses(cls, !closure).getFlattened());
      }
    });

    return entities;
  }

  public static Set<OWLEntity> getSupers(OWLEntity entity, final boolean closure,
      final OWLReasoner reasoner, final Set<IRI> excludedIris) {
    final Set<OWLEntity> entities = new HashSet<OWLEntity>();
    entities.add(entity);

    entity.accept(new OWLEntityVisitor() {

      @Override
      public void visit(OWLAnnotationProperty property) {
        // TODO Auto-generated method stub

      }

      @Override
      public void visit(OWLDatatype datatype) {
        // TODO Auto-generated method stub

      }

      @Override
      public void visit(OWLNamedIndividual individual) {
        // TODO Auto-generated method stub

      }

      @Override
      public void visit(OWLDataProperty property) {
        entities.add(property);
        try {
          addEntitiesWithoutExcludes(entities, reasoner.getSuperDataProperties(property, closure)
              .getFlattened(), excludedIris);

        } catch (ReasonerInternalException e) {
          // TODO fix this.
          System.err.println("Error getting data superproperties.");
        }

      }


      @Override
      public void visit(OWLObjectProperty property) {
        entities.add(property);
        Set<OWLObjectPropertyExpression> opes = null;

        // TODO: not sure why Fact++ is erroring out here like:

        // <http://purl.obolibrary.org/obo/ERO_0000558> had error: Role
        // expression expected in getSupRoles()

        // <http://eagle-i.org/ont/app/1.0/has_part_construct_insert>
        // had error: Role expression expected in getSupRoles()

        // <http://eagle-i.org/ont/app/1.0/has_measurement_scale> had
        // error: Role expression expected in getSupRoles()
        try {
          opes = reasoner.getSuperObjectProperties(property, closure).getFlattened();
        } catch (ReasonerInternalException e) {
          System.err.println("=================== " + property + " had error: " + e.getMessage());
          System.err.println("============== Type: " + property.getEntityType());
          System.err.flush();
        }
        if (opes == null) {
          return;
        }
        for (OWLObjectPropertyExpression ope : opes) {
          if (ope instanceof OWLObjectProperty) {
            OWLObjectProperty op = (OWLObjectProperty) ope;
            if(excludedIris.contains(op.getIRI())) continue;
            entities.add(op);
          }
        }

      }

      @Override
      public void visit(OWLClass cls) {
        entities.add(cls);
        addEntitiesWithoutExcludes(entities, reasoner.getSuperClasses(cls, closure).getFlattened(), excludedIris);
      }
    });

    return entities;
  }

  public static Set<OWLAnnotationAssertionAxiom> getSubjectAnnotationAxioms(
      Set<OWLOntology> ontologies, boolean includeImports, OWLAnnotationSubject subject) {
    Set<OWLAnnotationAssertionAxiom> axioms = new HashSet<OWLAnnotationAssertionAxiom>();
    for (OWLOntology o : ontologies) {
      axioms.addAll(getSubjectAnnotationAxioms(o, includeImports, subject));
    }
    return axioms;
  }

  public static Set<OWLAnnotationAssertionAxiom> getSubjectAnnotationAxioms(OWLOntology ontology,
      boolean includeImports, OWLAnnotationSubject subject) {
    Set<OWLAnnotationAssertionAxiom> axioms = new HashSet<OWLAnnotationAssertionAxiom>();
    Set<OWLOntology> ontologies;
    if (includeImports) {
      ontologies = ontology.getImportsClosure();
    } else {
      ontologies = Collections.singleton(ontology);
    }
    for (OWLOntology o : ontologies) {
      axioms.addAll(o.getAnnotationAssertionAxioms(subject));
    }
    return axioms;

  }

  // TODO: where is this used?
  public static Set<LabelInfo> getLabels(IRI iri, Set<OWLOntology> ontologies) {
    Set<LabelInfo> infos = new HashSet<OwlclUtil.LabelInfo>();

    for (OWLOntology ontology : ontologies) {
      Set<OWLAnnotationAssertionAxiom> axioms = ontology.getAnnotationAssertionAxioms(iri);
      for (OWLAnnotationAssertionAxiom axiom : axioms) {
        if (axiom.getProperty().getIRI().equals(OWLRDFVocabulary.RDFS_LABEL.getIRI())) {
          infos.add(new LabelInfo(ontology, axiom));
        }
      }
    }

    return infos;
  }

  public static List<OWLEntity> getEntitiesSortedByIri(OWLOntology ontology, boolean includeImports) {
    ArrayList<OWLEntity> entities = new ArrayList<OWLEntity>(ontology.getSignature(includeImports));
    Collections.sort(entities, new Comparator<OWLEntity>() {

      @Override
      public int compare(OWLEntity o1, OWLEntity o2) {

        return o1.getIRI().compareTo(o2.getIRI());
      }
    });
    return entities;

  }

  public static class LabelInfo {

    public final OWLAnnotationAssertionAxiom axiom;
    public final OWLOntology ontology;

    public LabelInfo(OWLOntology ontology, OWLAnnotationAssertionAxiom axiom) {
      this.ontology = ontology;
      this.axiom = axiom;
    }

    @Override
    public String toString() {
      // TODO Auto-generated method stub
      return "Ontology: " + ontology.getOntologyID() + " has label: " + axiom.getValue().toString();
    }
  }

  public static Set<OWLAxiom> getAxioms(OWLOntology ontology, boolean recursive) {
    Set<OWLAxiom> axioms = new HashSet<OWLAxiom>(ontology.getAxioms());
    if (recursive) {
      for (OWLOntology o : ontology.getImports()) {
        axioms.addAll(o.getAxioms());
      }
    }
    return axioms;
  }

  public static String getOsName() {
    return System.getProperty("os.name");
  }

  public static String getOsArch() {
    return System.getProperty("os.arch");
  }

  // ================================================================================
  // Load Fact++ native library
  // ================================================================================
  // load native libraries

  @SuppressWarnings("unused")
  private static boolean ___________FACTPP_NATIVE________________;

  static {
    // String osName = System.getProperty("os.name");
    // String osArch = System.getProperty("os.arch");
    // String libName = null;
    // String libPath = null;
    //
    // if (osName.toLowerCase().startsWith("windows"))
    // {
    // if (osArch.contains("64"))
    // {
    // libName = "FaCTPlusPlusJNI.dll";
    // libPath = "/fact162/win64/";
    // } else
    // {
    // libName = "FaCTPlusPlusJNI.dll";
    // libPath = "/fact162/win32/";
    // }
    // } else if (osName.toLowerCase().startsWith("linux"))
    // {
    // if (osArch.contains("64"))
    // {
    // libName = "libFaCTPlusPlusJNI.so";
    // libPath = "/fact162/linux64/";
    // } else
    // {
    // libName = "libFaCTPlusPlusJNI.so";
    // libPath = "/fact162/linux32/";
    // }
    // } else if (osName.toLowerCase().contains("mac"))
    // {
    // if (osArch.contains("64"))
    // {
    // libName = "libFaCTPlusPlusJNI.jnilib";
    // libPath = "/fact162/os64/";
    // } else
    // {
    // libName = "libFaCTPlusPlusJNI.jnilib";
    // libPath = "/fact162/os32/";
    // }
    // }
    // FileOutputStream fos = null;
    // InputStream fis = null;
    // try
    // {
    // final Path libDir = Files.createTempDirectory("factpp-");
    // libDir.toFile().deleteOnExit();
    // fos = new FileOutputStream(new File(libDir.toFile(), libName));
    // fis = OwlclUtil.class.getResourceAsStream(libPath + libName);
    // byte[] buffer = new byte[1024];
    // int bytesRead = 0;
    // while ((bytesRead = fis.read(buffer)) != -1)
    // {
    // fos.write(buffer, 0, bytesRead);
    // }
    // System.setProperty("java.library.path",
    // libDir.toFile().getAbsolutePath());
    // Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
    // fieldSysPath.setAccessible(true);
    // fieldSysPath.set(null, null);
    // Runtime.getRuntime().addShutdownHook(new Thread() {
    //
    // public void run() {
    // delete(libDir.toFile());
    // }
    //
    // void delete(File f) {
    // if (f.isDirectory())
    // {
    // for (File c : f.listFiles())
    // delete(c);
    // }
    // if (!f.delete())
    // throw new RuntimeException("Failed to delete file: " + f);
    // }
    // });
    // } catch (IOException | NoSuchFieldException | SecurityException |
    // IllegalArgumentException
    // | IllegalAccessException e)
    // {
    // throw new RuntimeException("ISFUtil: failed to copy native library.",
    // e);
    // } finally
    // {
    // if (fos != null)
    // {
    // try
    // {
    // fos.close();
    // } catch (IOException e)
    // {
    // }
    // }
    // if (fis != null)
    // {
    // try
    // {
    // fis.close();
    // } catch (IOException e)
    // {
    // }
    // }
    //
    // }

  }

  static public OWLOntology getOrLoadOntology(IRI iri, OWLOntologyManager man)
      throws RuntimeOntologyLoadingException {
    OWLOntology o = man.getOntology(iri);
    if (o == null) {
      try {
        o = man.loadOntology(iri);
      } catch (OWLOntologyCreationException e) {
        throw new RuntimeOntologyLoadingException("Failed while getOrLoadOntology IRI: " + iri, e);
      }
    }
    return o;
  }

  static public OWLOntology loadOntology(File file, OWLOntologyManager man)
      throws RuntimeOntologyLoadingException {
    OWLOntology o = null;
    if (o == null) {
      try {
        o = man.loadOntologyFromOntologyDocument(file);
      } catch (OWLOntologyCreationException e) {
        throw new RuntimeOntologyLoadingException("Failed while loadOntology File: "
            + file.getAbsolutePath(), e);
      }
    }
    return o;

  }

  static public OWLOntology loadOntologyIgnoreImports(File file, OWLOntologyManager man) {
    OWLOntology o = null;
    if (o == null) {
      try {
        man.setSilentMissingImportsHandling(true);
        OWLOntologyLoaderConfiguration lc = new OWLOntologyLoaderConfiguration();
        lc = lc.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);

        o = man.loadOntologyFromOntologyDocument(new FileDocumentSource(file), lc);
      } catch (OWLOntologyCreationException e) {
        throw new RuntimeOntologyLoadingException("Failed while loadOntology File: "
            + file.getAbsolutePath(), e);
      }
    }
    return o;
  }

  static public OWLOntology createOntology(IRI iri, IRI versionIri, OWLOntologyManager man)
      throws RuntimeOntologyLoadingException {

    OWLOntology o = null;
    OWLOntologyID id = new OWLOntologyID(iri, versionIri);
    
    try {
      o = man.createOntology(id);
      
    } catch (OWLOntologyCreationException e) {
      throw new RuntimeOntologyLoadingException("Faild to createOntology for IRI: " + iri, e);
    }

    return o;

  }

  /**
   * This method ignore missing IRI mappings when loading. If there is no mapping, a new ontology
   * with the IRI is created in the manager. This means that the mappers has to be set correctly to
   * avoid getting a new ontology when it should have been loaded. Use the getOrLoadOntology if the
   * ontology mapping should exist.
   * 
   * @param iri
   * @param man
   * @return
   */
  static public OWLOntology getOrLoadOrCreateOntology(IRI iri, IRI versionIri, OWLOntologyManager man)
      throws RuntimeOntologyLoadingException {
    OWLOntology o = null;
    try {
      o = getOrLoadOntology(iri, man);
    } catch (RuntimeOntologyLoadingException e1) {
      if (e1.isIriMapping()) {
        o = createOntology(iri, versionIri, man);
      } else {
        throw e1;
      }
    }
    return o;
  }

  private static Map<IRI, OWLReasoner> reasoners = new HashMap<IRI, OWLReasoner>();

  // private static FaCTPlusPlusReasonerFactory rf = new
  // FaCTPlusPlusReasonerFactory();

  static public OWLReasoner getReasoner(OWLOntology ontology) {
    OWLReasoner r = reasoners.get(ontology.getOntologyID().getOntologyIRI());
    if (r == null) {
      // r = rf.createNonBufferingReasoner(ontology);
      reasoners.put(ontology.getOntologyID().getOntologyIRI(), r);
    }
    return r;
  }

  static public void disposeReasoner(OWLReasoner reasoner) {
    IRI iri = reasoner.getRootOntology().getOntologyID().getOntologyIRI();
    reasoner.dispose();
    reasoners.remove(iri);
  }


  static public void addEntitiesWithoutExcludes(Set<OWLEntity> toEntities,
      Set<? extends OWLEntity> fromEntities, Set<IRI> excludedIris) {
    for(OWLEntity e : fromEntities){
      if(!excludedIris.contains(e.getIRI())){
        toEntities.add(e);
      }
    }
  }

  //
  // public void loadNativeLibrary(InputStream stream) {
  // Path tmp;
  // try
  // {
  // tmp = Files.createTempFile(getTemporaryDirectory(), "nativelib-", null);
  // OutputStream fos = new FileOutputStream(tmp.toFile());
  // byte[] buffer = new byte[1024];
  // int bytesRead = 0;
  // while ((bytesRead = stream.read(buffer)) != -1)
  // {
  // fos.write(buffer, 0, bytesRead);
  // }
  // stream.close();
  // fos.close();
  // } catch (IOException e1)
  // {
  // throw new RuntimeException("Failed while loading native library", e1);
  // }
  //
  // String libPath = System.getProperty("java.library.path");
  // libPath += File.pathSeparatorChar + tmp.toAbsolutePath().toString();
  // System.setProperty("java.library.path", libPath);
  //
  // Field fieldSysPath;
  // try
  // {
  // fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
  // fieldSysPath.setAccessible(true);
  // fieldSysPath.set(null, null);
  // } catch (NoSuchFieldException | SecurityException |
  // IllegalArgumentException
  // | IllegalAccessException e)
  // {
  // throw new RuntimeException("Error resetting java library path", e);
  // }
  //
  // }

  public String getNativeResourcePrefix(String resourceGroupName) {
    String path = "/native/" + resourceGroupName;

    String osName = getOsName();
    String osArch = getOsArch();

    if (osName.toLowerCase().startsWith("windows")) {
      if (osArch.contains("64")) {
        path += "/windows/64/";
      } else {
        path += "/windows/32/";
      }
    } else if (osName.toLowerCase().startsWith("linux")) {
      if (osArch.contains("64")) {
        path += "/linux/64/";
      } else {
        path += "/linux/32/";
      }
    } else if (osName.toLowerCase().contains("mac")) {
      if (osArch.contains("64")) {
        path += "/macos/64/";
      } else {
        path += "/macos/32/";
      }
    }

    return path;
  }

}
