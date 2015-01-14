package com.essaid.owlcl.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.essaid.owlcl.core.IOwlclManager;

/**
 * @author Shahim Essaid
 * 
 */
public class DefaultOwlclManager implements IOwlclManager {

	private static Logger logger;

	private URL codeLocationUrl;
	private File codeJar;
	private File codeDirectory;
	private File codeExtDirectory;

	private File currentDirectory;
	private File homeDirectory;

	private File workDirectory;
	private File workExtDirectory;

	private File temporaryDirectory;

	private Object lock = new Object();

	private boolean initted;
	private File outputDirectory;

	public DefaultOwlclManager() {
		init();
	}

	DefaultOwlclManager(URL codeUrl, File codeJar, File codeDirectory,
			File codeExtDirectory, File homeDirectory, File currentDirectory,
			File workDirectory, File outputDirectory, File workExtDirectory,
			File temporaryDirectory) {
		this.codeLocationUrl = codeUrl;
		this.codeJar = codeJar;
		this.codeDirectory = codeDirectory;
		this.codeExtDirectory = codeExtDirectory;
		this.homeDirectory = homeDirectory;
		this.currentDirectory = currentDirectory;
		this.workDirectory = workDirectory;
		this.outputDirectory = outputDirectory;
		this.workExtDirectory = workExtDirectory;
		this.temporaryDirectory = temporaryDirectory;
		this.initted = true;
		logger = LoggerFactory.getLogger(DefaultOwlclManager.class);

	}

	public void init() {
		synchronized (lock) {
			if (initted) {
				return;
			}
			initted = true;

			initDirectories();
			initClasspath();
			initTemporaryDirectory();
			logger = LoggerFactory.getLogger(DefaultOwlclManager.class);
		}

	}

	private void initDirectories() {

		// find code directory
		codeLocationUrl = this.getClass().getProtectionDomain().getCodeSource()
				.getLocation();
		File codeLocationFile = null;

		try {
			codeLocationFile = new File(codeLocationUrl.toURI())
					.getCanonicalFile();
		} catch (URISyntaxException e1) {
			throw new RuntimeException(
					"Error getting code source URI from invalide URL:"
							+ codeLocationUrl, e1);
		} catch (IOException e) {
			throw new RuntimeException(
					"Error getting canonical code source File from URL:"
							+ codeLocationUrl, e);
		}

		if (codeLocationFile.isDirectory()) {
			codeDirectory = codeLocationFile;

		} else {
			if (codeLocationFile.getPath().endsWith(".jar")) {
				codeJar = codeLocationFile;
				codeDirectory = codeLocationFile.getParentFile();
			} else {
				throw new RuntimeException(
						"Code source is not a directory or a *.jar file.");
			}
		}
		codeExtDirectory = new File(codeDirectory, IOwlclManager.OWLCL_EXT_DIR);

		// find caller's current directory;
		try {
			currentDirectory = new File(System.getProperty("user.dir"))
					.getCanonicalFile();
			// System.out.println("CURRENT DIRECTORY IS: " + currentDirectory);
		} catch (IOException e) {
			throw new RuntimeException("Error getting current directory.", e);
		}

		// find caller's owlcl home directory;
		try {
			homeDirectory = new File(System.getProperty("user.home"), ".owlcl")
					.getCanonicalFile();
			if (!homeDirectory.exists()) {
				homeDirectory.mkdirs();
			}
			// System.out.println("HOME DIRECTORY IS: " + homeDirectory);
		} catch (IOException e) {
			throw new RuntimeException("Error getting home directory.", e);
		}

		// first find working directory
		String workDir = System
				.getProperty(IOwlclManager.OWLCL_WORK_DIR_PROPERTY);
		if (workDir == null) {
			for (Entry<String, String> envEntry : System.getenv().entrySet()) {
				if (envEntry
						.getKey()
						.toUpperCase()
						.equals(IOwlclManager.OWLCL_WORK_DIR_PROPERTY
								.toUpperCase().replace(".", "_"))) {
					workDir = envEntry.getValue();
					break;
				}
			}
		}
		if (workDir != null) {
			try {
				workDirectory = new File(workDir).getCanonicalFile();
				if (!workDirectory.exists()) {
					workDirectory.mkdirs();
				}
			} catch (IOException e) {
				throw new RuntimeException(
						"Error getting canonical work directory for path: "
								+ workDir, e);
			}
		} else {
			workDirectory = currentDirectory;
		}

		// System.out.println("Work DIRECTORY IS: " + workDirectory);

		workExtDirectory = new File(workDirectory, IOwlclManager.OWLCL_EXT_DIR);

		// find output directory
		String outputDirectory = System
				.getProperty(IOwlclManager.OWLCL_OUTPUT_DIR_PROPERTY);
		if (outputDirectory == null) {
			for (Entry<String, String> envEntry : System.getenv().entrySet()) {
				if (envEntry
						.getKey()
						.toUpperCase()
						.equals(IOwlclManager.OWLCL_OUTPUT_DIR_PROPERTY
								.toUpperCase().replace(".", "_"))) {
					outputDirectory = envEntry.getValue();
					break;
				}
			}
		}
		if (outputDirectory != null) {
			try {
				this.outputDirectory = new File(outputDirectory)
						.getCanonicalFile();

			} catch (IOException e) {
				throw new RuntimeException(
						"Error getting canonical output directory for path: "
								+ outputDirectory, e);
			}
		} else {
			try {
				// make the default output directory under the OwlCL home
				// directory
				Path workPath = this.workDirectory.toPath();
				Path relativeWorkPath = workPath.subpath(0,
						workPath.getNameCount());
				this.outputDirectory = new File(homeDirectory, "output/"
						+ relativeWorkPath.toString()).getCanonicalFile();;

//				this.outputDirectory = new File(this.workDirectory.getParent(),
//						this.workDirectory.getName() + "_"
//								+ IOwlclManager.OWLCL_OUTPUT_DIR_NAME)
//						.getCanonicalFile();
			} catch (IOException e) {
				throw new RuntimeException(
						"Error getting canonical output directory for path: "
								+ this.workDirectory.getAbsolutePath()
								+ IOwlclManager.OWLCL_OUTPUT_DIR_NAME, e);
			}
		}
		if (!this.outputDirectory.exists()) {
			this.outputDirectory.mkdirs();
		}

	}

	private void initTemporaryDirectory() {
		try {
			Path tmpPath = Files.createTempDirectory("owlcl-tmp-");
			temporaryDirectory = tmpPath.toFile();
			temporaryDirectory.mkdirs();
			temporaryDirectory.deleteOnExit();
		} catch (IOException e) {
			throw new RuntimeException("Error creating temporary directory", e);
		}
		// Runtime.getRuntime().addShutdownHook(new Thread() {
		//
		// public void run() {
		//
		// delete(temporaryDirectory);
		// }
		//
		// void delete(File f) {
		// if (f.isDirectory())
		// {
		// for (File c : f.listFiles())
		// {
		// delete(c);
		// }
		// f.delete();
		// } else
		// {
		// f.delete();
		// }
		//
		// }
		// });

	}

	private void initClasspath() {

		ClassLoader systemLoader = ClassLoader.getSystemClassLoader();

		Method addUrlMethod = null;
		try {
			addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL",
					URL.class);
			addUrlMethod.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e1) {
			throw new RuntimeException(
					"Error trying to get the addURL ClassLoader method "
							+ "to augment classpath.", e1);
		}

		if (systemLoader instanceof URLClassLoader) {

			// load core jars first, they have higher priority.
			File coreJars = new File(codeExtDirectory, "core");
			if (codeJar != null) // running in packaged mode, not in IDE.
			{
				if (coreJars.exists()) // they should exist.
				{
					for (File file : coreJars.listFiles()) {
						addFileToClasspath(file, systemLoader, addUrlMethod);
						// look one directory deep for addition jar files
						if (file.isDirectory()) {
							for (File subFile : file.listFiles()) {
								if (subFile.isFile()
										&& subFile.getPath().endsWith(".jar")) {
									addFileToClasspath(subFile, systemLoader,
											addUrlMethod);
								}
							}
						}
					}
				} else
				// give up
				{
					throw new IllegalStateException(
							"Core jar files are missing from "
									+ coreJars.getAbsolutePath());
				}
			}

			// load any other extensions
			if (codeExtDirectory.exists()) {
				for (File file : codeExtDirectory.listFiles()) {
					if (file.equals(coreJars)) {
						// already loaded
						continue;
					}
					addFileToClasspath(file, systemLoader, addUrlMethod);
					// look one directory deep for addition jar files
					if (file.isDirectory()) {
						for (File subFile : file.listFiles()) {
							if (subFile.isFile()
									&& subFile.getPath().endsWith(".jar")) {
								addFileToClasspath(subFile, systemLoader,
										addUrlMethod);
							}
						}
					}
				}
			}
			if (workExtDirectory.exists() && workExtDirectory.isDirectory()) {
				for (File file : workExtDirectory.listFiles()) {
					addFileToClasspath(file, systemLoader, addUrlMethod);
					// look one directory deep for addition jar files
					if (file.isDirectory()) {
						for (File subFile : file.listFiles()) {
							if (subFile.isFile()
									&& subFile.getPath().endsWith(".jar")) {
								addFileToClasspath(subFile, systemLoader,
										addUrlMethod);
							}
						}
					}
				}
			}

		} else {
			throw new IllegalStateException(
					"System class loader is not a URL class loader. Can't load application.");
		}

	}

	private void addFileToClasspath(File file, ClassLoader urlcl, Method addURL) {
		// System.out.println("Adding file " + file);
		if (file.getPath().endsWith(".jar") || file.isDirectory()) {
			URL fileUrl;
			try {
				fileUrl = file.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new RuntimeException("Error creating a URL for file "
						+ file, e);
			}
			try {
				addURL.invoke(urlcl, fileUrl);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException("Error adding file: " + file
						+ " to classpath", e);
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.essaid.owlcl.util.IOwlclManager#getTemporaryDirectory()
	 */
	@Override
	public File getTemporaryDirectory() {

		if (!initted) {
			init();
		}

		return temporaryDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.essaid.owlcl.util.IOwlclManager#loadNativeLibrary(java.io.InputStream
	 * )
	 */
	@Override
	public void loadNativeLibrary(InputStream stream, String name) {
		Path tmpLibDir;
		try {
			tmpLibDir = Files.createTempDirectory(getTemporaryDirectory()
					.toPath(), "nativelib-");
			File tmpFile = new File(tmpLibDir.toFile(), name);
			// tmpFile.deleteOnExit();
			OutputStream fos = new FileOutputStream(tmpFile);
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			while ((bytesRead = stream.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
			stream.close();
			fos.close();
		} catch (IOException e1) {
			throw new RuntimeException("Failed while loading native library",
					e1);
		}

		String libPath = System.getProperty("java.library.path");
		libPath += File.pathSeparatorChar
				+ tmpLibDir.toAbsolutePath().toString();
		System.setProperty("java.library.path", libPath);

		Field fieldSysPath;
		try {
			fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Error resetting java library path", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.essaid.owlcl.util.IOwlclManager#getNativeResourcePrefix(java.lang
	 * .String )
	 */
	@Override
	public String getNativeResourcePrefix(String resourceGroupName) {
		String path = "native/" + resourceGroupName;

		if (isWindows()) {
			if (is64Arch()) {
				path += "/windows/64/";
			} else {
				path += "/windows/32/";
			}
		} else if (isLinux()) {
			if (is64Arch()) {
				path += "/linux/64/";
			} else {
				path += "/linux/32/";
			}
		} else if (isMacOs()) {
			if (is64Arch()) {
				path += "/macos/64/";
			} else {
				path += "/macos/32/";
			}
		}

		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.essaid.owlcl.util.IOwlclManager#getCodeUrl()
	 */
	@Override
	public URL getCodeUrl() {
		return codeLocationUrl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.essaid.owlcl.util.IOwlclManager#getCodeJar()
	 */
	@Override
	public File getCodeJar() {
		return codeJar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.essaid.owlcl.util.IOwlclManager#getCodeDirectory()
	 */
	@Override
	public File getCodeDirectory() {
		return codeDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.essaid.owlcl.util.IOwlclManager#getCodeExtDirectory()
	 */
	@Override
	public File getCodeExtDirectory() {
		return codeExtDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.essaid.owlcl.util.IOwlclManager#getHomeDirectory()
	 */
	@Override
	public File getHomeDirectory() {
		return homeDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.essaid.owlcl.util.IOwlclManager#getCurrentDirectory()
	 */
	@Override
	public File getCurrentDirectory() {
		return currentDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.essaid.owlcl.util.IOwlclManager#getWorkDirectory()
	 */
	@Override
	public File getWorkDirectory() {
		return workDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.essaid.owlcl.util.IOwlclManager#getWorkExtDirectory()
	 */
	@Override
	public File getWorkExtDirectory() {
		return workExtDirectory;
	}

	private String osName = System.getProperty("os.name");
	private String osArch = System.getProperty("os.arch");

	@Override
	public boolean isWindows() {
		return osName.toLowerCase().startsWith("windows");
	}

	@Override
	public boolean isMacOs() {
		return osName.toLowerCase().contains("mac");
	}

	@Override
	public boolean isLinux() {
		return osName.toLowerCase().startsWith("linux");
	}

	@Override
	public boolean is32Arch() {
		return osArch.contains("32");
	}

	@Override
	public boolean is64Arch() {
		return osArch.contains("64");
	}

	@Override
	public File getOutputDirectory() {
		return outputDirectory;
	}

	//
	//
	// //
	// ================================================================================
	// // OWL helper methods
	// //
	// ================================================================================
	//
	// private static OWLDataFactory df = OWLManager.getOWLDataFactory();
	//
	// @SuppressWarnings("unused")
	// private static boolean ___________OWL_HELPERS________________;
	//
	// public static OWLAnnotationProperty getAnnotationProperty(String iri) {
	// return df.getOWLAnnotationProperty(IRI.create(iri));
	// }
	//
	// public static OWLObjectProperty getObjectProperty(String iri) {
	// return df.getOWLObjectProperty(IRI.create(iri));
	// }
	//
	// public static OWLDataProperty getDataProperty(String iri) {
	// return df.getOWLDataProperty(IRI.create(iri));
	// }
	//
	// public static Set<String>
	// getOntologyAnnotationLiteralValues(OWLAnnotationProperty property,
	// OWLOntology ontology, boolean recursive) {
	// Set<String> values = new HashSet<String>();
	// Set<OWLOntology> ontologies = null;
	// if (recursive)
	// {
	// ontologies = ontology.getImportsClosure();
	// } else
	// {
	// ontologies = Collections.singleton(ontology);
	// }
	// for (OWLOntology o : ontologies)
	// {
	// for (OWLAnnotation a : o.getAnnotations())
	// {
	// if (a.getProperty().equals(property))
	// {
	// values.add(((OWLLiteral) a.getValue()).getLiteral());
	// }
	// }
	// }
	//
	// return values;
	// }
	//
	// public static Set<OWLAnnotationAssertionAxiom>
	// getIncludeAxioms(OWLOntology
	// ontology,
	// boolean includeImports) {
	//
	// return OwlclUtil2.getAnnotationAssertionAxioms(ontology,
	// MBSimpleVocab.include.getAP(),
	// includeImports);
	// }
	//
	// public static Set<OWLAnnotationAssertionAxiom>
	// getIncludeInstancesAxioms(OWLOntology ontology,
	// boolean includeImports) {
	//
	// return OwlclUtil2.getAnnotationAssertionAxioms(ontology,
	// MBSimpleVocab.include_instances.getAP(), includeImports);
	// }
	//
	// public static Set<OWLAnnotationAssertionAxiom>
	// getIncludeSubsAxioms(OWLOntology ontology,
	// boolean includeImports) {
	//
	// return OwlclUtil2.getAnnotationAssertionAxioms(ontology,
	// MBSimpleVocab.include_subs.getAP(),
	// includeImports);
	// }
	//
	// public static Set<OWLAnnotationAssertionAxiom>
	// getExcludeAxioms(OWLOntology
	// ontology,
	// boolean includeImports) {
	//
	// return OwlclUtil2.getAnnotationAssertionAxioms(ontology,
	// MBSimpleVocab.exclude.getAP(),
	// includeImports);
	// }
	//
	// public static Set<OWLAnnotationAssertionAxiom>
	// getExcludeSubsAxioms(OWLOntology ontology,
	// boolean includeImports) {
	//
	// return OwlclUtil2.getAnnotationAssertionAxioms(ontology,
	// MBSimpleVocab.exclude_subs.getAP(),
	// includeImports);
	// }
	//
	// public static Set<OWLEntity> getIncludeEntities(OWLOntology ontology,
	// boolean includeImports) {
	// Set<OWLAnnotationAssertionAxiom> axioms = getIncludeAxioms(ontology,
	// includeImports);
	// return getSubjectEntities(ontology, includeImports, axioms);
	// }
	//
	// public static Set<OWLEntity> getIncludeInstances(OWLOntology ontology,
	// boolean includeImports) {
	//
	// Set<OWLAnnotationAssertionAxiom> axioms =
	// getIncludeInstancesAxioms(ontology, includeImports);
	//
	// return getSubjectEntities(ontology, includeImports, axioms);
	//
	// }
	//
	// public static Set<OWLEntity> getIncludeSubsEntities(OWLOntology ontology,
	// boolean includeImports) {
	// Set<OWLAnnotationAssertionAxiom> axioms = getIncludeSubsAxioms(ontology,
	// includeImports);
	// return getSubjectEntities(ontology, includeImports, axioms);
	// }
	//
	// public static Set<OWLEntity> getExcludeEntities(OWLOntology ontology,
	// boolean includeImports) {
	// Set<OWLAnnotationAssertionAxiom> axioms = getExcludeAxioms(ontology,
	// includeImports);
	// return getSubjectEntities(ontology, includeImports, axioms);
	// }
	//
	// public static Set<OWLEntity> getExcludeSubsEntities(OWLOntology ontology,
	// boolean includeImports) {
	// Set<OWLAnnotationAssertionAxiom> axioms = getExcludeSubsAxioms(ontology,
	// includeImports);
	// return getSubjectEntities(ontology, includeImports, axioms);
	// }
	//
	// private static Set<OWLEntity> getSubjectEntities(OWLOntology ontology,
	// boolean includeImports,
	// Set<OWLAnnotationAssertionAxiom> axioms) {
	// Set<OWLEntity> entities = new HashSet<OWLEntity>();
	// IRI subject;
	// for (OWLAnnotationAssertionAxiom a : axioms)
	// {
	// if (a.getSubject() instanceof IRI)
	// {
	// subject = (IRI) a.getSubject();
	// entities.addAll(ontology.getEntitiesInSignature(subject,
	// includeImports));
	// }
	// }
	// return entities;
	// }
	//
	// public static Set<OWLAnnotationAssertionAxiom>
	// getAnnotationAssertionAxioms(OWLOntology ontology,
	// OWLAnnotationProperty property, boolean includeImports) {
	// Set<OWLAnnotationAssertionAxiom> axioms = new
	// HashSet<OWLAnnotationAssertionAxiom>();
	// Set<OWLOntology> ontologies;
	// if (includeImports)
	// {
	// ontologies = ontology.getImportsClosure();
	// } else
	// {
	// ontologies = Collections.singleton(ontology);
	// }
	// for (OWLOntology o : ontologies)
	// {
	// for (OWLAnnotationAssertionAxiom aaa :
	// o.getAxioms(AxiomType.ANNOTATION_ASSERTION))
	// {
	// if (aaa.getProperty().getIRI().equals(property.getIRI()))
	// {
	// axioms.add(aaa);
	// }
	// }
	// }
	// return axioms;
	// }
	//
	// public static Set<OWLEntity> getSubs(OWLEntity entity, final boolean
	// closure, final OWLReasoner pr) {
	// final Set<OWLEntity> entities = new HashSet<OWLEntity>();
	// entities.add(entity);
	//
	// entity.accept(new OWLEntityVisitor() {
	//
	// @Override
	// public void visit(OWLAnnotationProperty property) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void visit(OWLDatatype datatype) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void visit(OWLNamedIndividual individual) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void visit(OWLDataProperty property) {
	// entities.add(property);
	// entities.addAll(pr.getSubDataProperties(property,
	// !closure).getFlattened());
	//
	// }
	//
	// @Override
	// public void visit(OWLObjectProperty property) {
	// entities.add(property);
	// Set<OWLObjectPropertyExpression> opes =
	// pr.getSubObjectProperties(property,
	// !closure)
	// .getFlattened();
	// for (OWLObjectPropertyExpression ope : opes)
	// {
	// if (ope instanceof OWLObjectProperty)
	// {
	// entities.add((OWLObjectProperty) ope);
	// }
	// }
	//
	// }
	//
	// @Override
	// public void visit(OWLClass cls) {
	// entities.add(cls);
	// entities.addAll(pr.getSubClasses(cls, !closure).getFlattened());
	// }
	// });
	//
	// return entities;
	// }
	//
	// public static Set<OWLEntity> getSupers(OWLEntity entity, final boolean
	// closure,
	// final OWLReasoner pr) {
	// final Set<OWLEntity> entities = new HashSet<OWLEntity>();
	// entities.add(entity);
	//
	// entity.accept(new OWLEntityVisitor() {
	//
	// @Override
	// public void visit(OWLAnnotationProperty property) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void visit(OWLDatatype datatype) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void visit(OWLNamedIndividual individual) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void visit(OWLDataProperty property) {
	// entities.add(property);
	// entities.addAll(pr.getSuperDataProperties(property,
	// closure).getFlattened());
	//
	// }
	//
	// @Override
	// public void visit(OWLObjectProperty property) {
	// entities.add(property);
	// Set<OWLObjectPropertyExpression> opes = null;
	//
	// // TODO: not sure why Fact++ is erroring out here like:
	//
	// // <http://purl.obolibrary.org/obo/ERO_0000558> had error: Role
	// // expression expected in getSupRoles()
	//
	// // <http://eagle-i.org/ont/app/1.0/has_part_construct_insert>
	// // had error: Role expression expected in getSupRoles()
	//
	// // <http://eagle-i.org/ont/app/1.0/has_measurement_scale> had
	// // error: Role expression expected in getSupRoles()
	// try
	// {
	// opes = pr.getSuperObjectProperties(property, closure).getFlattened();
	// } catch (ReasonerInternalException e)
	// {
	// System.err.println(property + " had error: " + e.getMessage());
	// }
	// if (opes == null)
	// {
	// return;
	// }
	// for (OWLObjectPropertyExpression ope : opes)
	// {
	// if (ope instanceof OWLObjectProperty)
	// {
	// entities.add((OWLObjectProperty) ope);
	// }
	// }
	//
	// }
	//
	// @Override
	// public void visit(OWLClass cls) {
	// entities.add(cls);
	// entities.addAll(pr.getSuperClasses(cls, closure).getFlattened());
	//
	// }
	// });
	//
	// return entities;
	// }
	//
	// public static Set<OWLAnnotationAssertionAxiom>
	// getSubjectAnnotationAxioms(
	// Set<OWLOntology> ontologies, boolean includeImports, OWLAnnotationSubject
	// subject) {
	// Set<OWLAnnotationAssertionAxiom> axioms = new
	// HashSet<OWLAnnotationAssertionAxiom>();
	// for (OWLOntology o : ontologies)
	// {
	// axioms.addAll(getSubjectAnnotationAxioms(o, includeImports, subject));
	// }
	// return axioms;
	// }
	//
	// public static Set<OWLAnnotationAssertionAxiom>
	// getSubjectAnnotationAxioms(OWLOntology ontology,
	// boolean includeImports, OWLAnnotationSubject subject) {
	// Set<OWLAnnotationAssertionAxiom> axioms = new
	// HashSet<OWLAnnotationAssertionAxiom>();
	// Set<OWLOntology> ontologies;
	// if (includeImports)
	// {
	// ontologies = ontology.getImportsClosure();
	// } else
	// {
	// ontologies = Collections.singleton(ontology);
	// }
	// for (OWLOntology o : ontologies)
	// {
	// axioms.addAll(o.getAnnotationAssertionAxioms(subject));
	// }
	// return axioms;
	//
	// }
	//
	// // TODO: where is this used?
	// public static Set<LabelInfo> getLabels(IRI iri, Set<OWLOntology>
	// ontologies) {
	// Set<LabelInfo> infos = new HashSet<OwlclUtil2.LabelInfo>();
	//
	// for (OWLOntology ontology : ontologies)
	// {
	// Set<OWLAnnotationAssertionAxiom> axioms =
	// ontology.getAnnotationAssertionAxioms(iri);
	// for (OWLAnnotationAssertionAxiom axiom : axioms)
	// {
	// if
	// (axiom.getProperty().getIRI().equals(OWLRDFVocabulary.RDFS_LABEL.getIRI()))
	// {
	// infos.add(new LabelInfo(ontology, axiom));
	// }
	// }
	// }
	//
	// return infos;
	// }
	//
	// public static List<OWLEntity> getEntitiesSortedByIri(OWLOntology
	// ontology,
	// boolean includeImports) {
	// ArrayList<OWLEntity> entities = new
	// ArrayList<OWLEntity>(ontology.getSignature(includeImports));
	// Collections.sort(entities, new Comparator<OWLEntity>() {
	//
	// @Override
	// public int compare(OWLEntity o1, OWLEntity o2) {
	//
	// return o1.getIRI().compareTo(o2.getIRI());
	// }
	// });
	// return entities;
	//
	// }
	//
	// public static class LabelInfo {
	//
	// public final OWLAnnotationAssertionAxiom axiom;
	// public final OWLOntology ontology;
	//
	// public LabelInfo(OWLOntology ontology, OWLAnnotationAssertionAxiom axiom)
	// {
	// this.ontology = ontology;
	// this.axiom = axiom;
	// }
	//
	// @Override
	// public String toString() {
	// // TODO Auto-generated method stub
	// return "Ontology: " + ontology.getOntologyID() + " has label: " +
	// axiom.getValue().toString();
	// }
	// }
	//
	// public static Set<OWLAxiom> getAxioms(OWLOntology ontology, boolean
	// recursive) {
	// Set<OWLAxiom> axioms = new HashSet<OWLAxiom>(ontology.getAxioms());
	// if (recursive)
	// {
	// for (OWLOntology o : ontology.getImports())
	// {
	// axioms.addAll(o.getAxioms());
	// }
	// }
	// return axioms;
	// }
	//

	//
	// //
	// ================================================================================
	// // Load Fact++ native library
	// //
	// ================================================================================
	// // load native libraries
	//
	// @SuppressWarnings("unused")
	// private static boolean ___________FACTPP_NATIVE________________;
	//
	// static
	// {
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
	// fis = OwlclUtil2.class.getResourceAsStream(libPath + libName);
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
	// throw new RuntimeException("ISFUtil: failed to copy native library.", e);
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
	//
	// }
	//
	// static public OWLOntology getOrLoadOntology(IRI iri, OWLOntologyManager
	// man)
	// throws RuntimeOntologyLoadingException {
	// OWLOntology o = man.getOntology(iri);
	// if (o == null)
	// {
	// try
	// {
	// o = man.loadOntology(iri);
	// } catch (OWLOntologyCreationException e)
	// {
	// throw new
	// RuntimeOntologyLoadingException("Failed while getOrLoadOntology IRI: " +
	// iri, e);
	// }
	// }
	// return o;
	// }
	//
	// static public OWLOntology loadOntology(File file, OWLOntologyManager man)
	// throws RuntimeOntologyLoadingException {
	// OWLOntology o = null;
	// if (o == null)
	// {
	// try
	// {
	// o = man.loadOntologyFromOntologyDocument(file);
	// } catch (OWLOntologyCreationException e)
	// {
	// throw new
	// RuntimeOntologyLoadingException("Failed while loadOntology File: "
	// + file.getAbsolutePath(), e);
	// }
	// }
	// return o;
	//
	// }
	//
	// static public OWLOntology createOntology(IRI iri, OWLOntologyManager man)
	// throws RuntimeOntologyLoadingException {
	//
	// OWLOntology o = null;
	//
	// try
	// {
	// o = man.createOntology(iri);
	// } catch (OWLOntologyCreationException e)
	// {
	// throw new
	// RuntimeOntologyLoadingException("Faild to createOntology for IRI: " +
	// iri,
	// e);
	// }
	//
	// return o;
	//
	// }
	//
	// /**
	// * This method ignore missing IRI mappings when loading. If there is no
	// * mapping, a new ontology with the IRI is created in the manager. This
	// means
	// * that the mappers has to be set correctly to avoid getting a new
	// ontology
	// * when it should have been loaded. Use the getOrLoadOntology if the
	// ontology
	// * mapping should exist.
	// *
	// * @param iri
	// * @param man
	// * @return
	// */
	// static public OWLOntology getOrLoadOrCreateOntology(IRI iri,
	// OWLOntologyManager man)
	// throws RuntimeOntologyLoadingException {
	// OWLOntology o = null;
	// try
	// {
	// o = getOrLoadOntology(iri, man);
	// } catch (RuntimeOntologyLoadingException e1)
	// {
	// if (e1.isIriMapping())
	// {
	// o = createOntology(iri, man);
	// } else
	// {
	// throw e1;
	// }
	// }
	// return o;
	// }
	//
	// private static Map<IRI, OWLReasoner> reasoners = new HashMap<IRI,
	// OWLReasoner>();
	// private static FaCTPlusPlusReasonerFactory rf = new
	// FaCTPlusPlusReasonerFactory();
	//
	// static public OWLReasoner getReasoner(OWLOntology ontology) {
	// OWLReasoner r = reasoners.get(ontology.getOntologyID().getOntologyIRI());
	// if (r == null)
	// {
	// r = rf.createNonBufferingReasoner(ontology);
	// reasoners.put(ontology.getOntologyID().getOntologyIRI(), r);
	// }
	// return r;
	// }
	//
	// static public void disposeReasoner(OWLReasoner reasoner) {
	// IRI iri = reasoner.getRootOntology().getOntologyID().getOntologyIRI();
	// reasoner.dispose();
	// reasoners.remove(iri);
	// }

}
