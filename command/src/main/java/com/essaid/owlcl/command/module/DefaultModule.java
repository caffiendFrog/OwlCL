package com.essaid.owlcl.command.module;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.slf4j.Logger;

import com.essaid.owlcl.command.module.builder.IModuleBuilder;
import com.essaid.owlcl.command.module.builder.ModuleBuilderManager;
import com.essaid.owlcl.command.module.config.IModuleConfig;
import com.essaid.owlcl.core.IOwlclManager;
import com.essaid.owlcl.core.annotation.InjectLogger;
import com.essaid.owlcl.core.reasoner.IReasonerManager;
import com.essaid.owlcl.core.util.IInitializable;
import com.essaid.owlcl.core.util.ILoggerOwner;
import com.essaid.owlcl.core.util.IReportFactory;
import com.essaid.owlcl.core.util.OWLOntologyWrapper;
import com.essaid.owlcl.core.util.OwlclUtil;
import com.essaid.owlcl.core.util.Report;
import com.google.inject.Inject;

public class DefaultModule implements IModule, IInitializable, ILoggerOwner {

	@InjectLogger
	private Logger logger;

	@Inject
	private ModuleBuilderManager builderManager;

	@Inject
	IReasonerManager reasonerManager;

	@Inject
	IReportFactory reportFactory;

	@Inject
	IOwlclManager owlclManager;

	private Report reportClassified;
	private Report reportUnclassified;

	private Path outputClassified;

	private OWLOntologyManager classifiedManager;
	private OWLDataFactory df;
	private OWLOntology unclassifiedModule;
	private OWLOntology classifiedModule;

	// private Map<IModule, Boolean> bothImports = new HashMap<IModule,
	// Boolean>();

	private Boolean addLegacyClassified;
	private Boolean addLegacyUnclassified;
	private Boolean cleanLegacyClassified;
	private Boolean cleanLegacyUnclassified;

	private IModuleConfig moduleConfiguration;

	private boolean disposed;
	//
	// private boolean generated;
	//
	// private boolean saved;

	private boolean finalUnclassified;

	private boolean finalClassified;

	private Path outputUnclassified;

	private OWLOntologyManager unclassifiedManager;

	public DefaultModule(IModuleConfig configIModuleConfig,
			Path outputUnclassified, Path outputClassified) {
		this.moduleConfiguration = configIModuleConfig;

		this.outputClassified = outputClassified;

		this.outputUnclassified = outputUnclassified;

		try {
			if (this.outputUnclassified != null) {
				Files.createDirectories(outputUnclassified);
			}
			if (this.outputClassified != null) {
				Files.createDirectories(outputClassified);
			}
		} catch (IOException e) {
			throw new RuntimeException(
					"Error creating module's output directories", e);
		}

		this.classifiedManager = OWLManager.createOWLOntologyManager();
		this.unclassifiedManager = OWLManager.createOWLOntologyManager();
		this.df = classifiedManager.getOWLDataFactory();
	}

	@Override
	public IModuleConfig getModuleConfiguration() {
		return this.moduleConfiguration;
	}

	@Override
	public Path getOutputClassified() {
		return outputClassified;
	}

	@Override
	public Path getOutputUnclassified() {
		return outputUnclassified;
	}

	@Override
	public OWLOntology getBuildersUnclassified() {
		if (unclassifiedModule == null) {
			logger.info("Generating unclassified module for: "
					+ getModuleConfiguration().getName());
			List<IModuleBuilder> builders = new ArrayList<IModuleBuilder>();
			unclassifiedModule = OwlclUtil.createOntology(
					this.moduleConfiguration.getUnclassifiedIri(),
					unclassifiedManager);
			for (String builderName : this.moduleConfiguration
					.getUnclassifiedBuilderNames()) {
				reportUnclassified.info("Doing builder name: " + builderName);
				IModuleBuilder builder = builderManager.getBuilder(builderName,
						this);
				if (builder == null) {
					logger.error("No builder named {} was found for module {}",
							builderName, moduleConfiguration.getName());
					continue;
				}
				reportUnclassified.info("Found builder: "
						+ builder.getClass().getName());
				builder.build(this, false);
				builders.add(builder);
			}

			// notify builders
			Iterator<IModuleBuilder> i = builders.iterator();
			while (i.hasNext()) {
				IModuleBuilder builder = i.next();
				i.remove();
				builder.buildFinished(this);
			}

		}
		return new OWLOntologyWrapper(unclassifiedModule);
	}

	@Override
	public OWLOntology getFinalUnclassified() {
		if (!finalUnclassified) {
			finalUnclassified = true;
			getBuildersUnclassified();
			addAnnotationsUnclassified(moduleConfiguration.getIncludeOntology()
					.getAnnotations(), null);
			addAxiomsUnclassified(
					OwlclUtil.getAxioms(
							moduleConfiguration.getIncludeOntology(), true),
					null);
			removeAxiomsUnclassified(
					OwlclUtil.getAxioms(
							moduleConfiguration.getExcludeOntology(), true),
					null);

			if (isAddLegacyUnclassified()) {
				addAxiomsUnclassified(
						OwlclUtil.getAxioms(
								moduleConfiguration.getLegacyOntology(), true),
						null);
			}

			for (IRI iri : moduleConfiguration.getUnclassifiedImportIris()) {
				OWLImportsDeclaration id = unclassifiedManager
						.getOWLDataFactory().getOWLImportsDeclaration(iri);
				unclassifiedManager.applyChange(new AddImport(
						unclassifiedModule, id));
			}

		}
		return unclassifiedModule;
	}

	@Override
	public OWLOntology getBuildersClassified() {

		if (classifiedModule == null) {
			logger.info("Generating classified module for: "
					+ getModuleConfiguration().getName());
			List<IModuleBuilder> builders = new ArrayList<IModuleBuilder>();
			classifiedModule = OwlclUtil.createOntology(
					this.moduleConfiguration.getClassifiedIri(),
					classifiedManager);
			for (String builderName : this.moduleConfiguration
					.getClassifiedBuilderNames()) {
				reportClassified.info("Doing builder name: " + builderName);
				IModuleBuilder builder = builderManager.getBuilder(builderName,
						this);
				if (builder == null) {
					logger.error("No builder named {} was found for module {}",
							builderName, moduleConfiguration.getName());
					continue;
				}
				reportClassified.info("Found builder: "
						+ builder.getClass().getName());
				builder.build(this, false);
				builders.add(builder);
			}

			// notify builders
			Iterator<IModuleBuilder> i = builders.iterator();
			while (i.hasNext()) {
				IModuleBuilder builder = i.next();
				i.remove();
				builder.buildFinished(this);
			}

			if (isAddLegacyClassified()) {
				addAxiomsClassified(
						OwlclUtil.getAxioms(
								moduleConfiguration.getLegacyOntology(), true),
						null);
			}

		}

		return new OWLOntologyWrapper(classifiedModule);
	}

	@Override
	public OWLOntology getFinalClassified() {
		if (!finalClassified) {
			finalClassified = true;
			getBuildersClassified();

			addAnnotationsClassified(moduleConfiguration.getIncludeOntology()
					.getAnnotations(), null);
			addAxiomsClassified(
					OwlclUtil.getAxioms(
							moduleConfiguration.getIncludeOntology(), true),
					null);

			removeAnnotationsClassified(moduleConfiguration
					.getExcludeOntology().getAnnotations(), null);
			removeAxiomsClassified(
					OwlclUtil.getAxioms(
							moduleConfiguration.getExcludeOntology(), true),
					null);

			if (isAddLegacyClassified()) {
				addAxiomsClassified(
						OwlclUtil.getAxioms(
								moduleConfiguration.getLegacyOntology(), true),
						null);
			}

			for (IRI iri : moduleConfiguration.getClassifiedImportIris()) {
				OWLImportsDeclaration id = classifiedManager
						.getOWLDataFactory().getOWLImportsDeclaration(iri);
				classifiedManager.applyChange(new AddImport(classifiedModule,
						id));
			}

		}
		return classifiedModule;
	}

	// ================================================================================
	// properties
	// ================================================================================

	private Boolean unclassified = null;

	@Override
	public void setUnclassified(Boolean isUnclassified) {
		this.unclassified = isUnclassified;
	}

	@Override
	public boolean isUnclassified() {
		if (this.unclassified == null) {
			return this.moduleConfiguration.isUnclassified();
		}
		return this.unclassified;
	}

	private Boolean classified = null;

	@Override
	public void setClassified(Boolean isClassified) {
		this.classified = isClassified;
	}

	@Override
	public boolean isClassified() {
		if (this.classified == null) {
			return this.moduleConfiguration.isClassified();
		}
		return this.classified;
	}

	// ================================================================================
	// module imports
	// ================================================================================

	private Map<IModule, Boolean> unclassifiedImports = new HashMap<IModule, Boolean>();
	private Map<IModule, Boolean> classifiedImports = new HashMap<IModule, Boolean>();

	@Override
	public void importModuleIntoUnclassified(IModule module, Boolean inferred) {
		unclassifiedImports.put(module, inferred);
	}

	@Override
	public void importModuleIntoClassified(IModule module, Boolean inferred) {
		classifiedImports.put(module, inferred);
	}

	// ================================================================================
	// legacy related
	// ================================================================================

	@Override
	public void cleanLegacyUnclassified() {
		// TODO
	}

	@Override
	public void cleanLegacyClassified() {
		// TODO
	}

	@Override
	public boolean isCleanLegacyUnclassified() {
		if (cleanLegacyUnclassified != null) {
			return this.cleanLegacyUnclassified;
		}
		return getModuleConfiguration().isUnclassifiedCleanLegacy();
	}

	@Override
	public void setCleanLegacyUnclassified(Boolean generate) {
		this.cleanLegacyUnclassified = generate;

	}

	@Override
	public boolean isCleanLegacyClassified() {
		if (cleanLegacyClassified != null) {
			return this.cleanLegacyClassified;
		}
		return getModuleConfiguration().isClassifiedCleanLegacy();
	}

	@Override
	public void setCleanLegacyClassified(Boolean generate) {
		this.cleanLegacyClassified = generate;

	}

	@Override
	public boolean isAddLegacyClassified() {
		if (addLegacyClassified != null) {
			return addLegacyClassified;
		}
		return getModuleConfiguration().isClassifiedAddlegacy();
	}

	@Override
	public void setAddLegacyClassified(Boolean isAddLegacy) {
		this.addLegacyClassified = isAddLegacy;

	}

	@Override
	public boolean isAddLegacyUnclassified() {
		if (addLegacyUnclassified != null) {
			return addLegacyUnclassified;
		}
		return getModuleConfiguration().isUnclassifiedAddlegacy();
	}

	@Override
	public void setAddLegacyUnclassified(Boolean addLegacy) {
		this.addLegacyUnclassified = addLegacy;

	}

	// ================================================================================
	// utility
	// ================================================================================

	@Override
	public void saveModule() {
		saveUnclassifiedModule();
		saveClassifiedModule();
	}

	@Override
	public void saveUnclassifiedModule() {
		
		if(! isUnclassified()){
			return;
		}

		if (outputUnclassified == null) {
			logger.warn("Output directory for unclassified module "
					+ getModuleConfiguration().getName()
					+ " is null, skipping save and all imports.");
			return;
		}

		getFinalUnclassified();

		for (Entry<IModule, Boolean> entry : unclassifiedImports.entrySet()) {
			IModule module = entry.getKey();
			IRI classifiedIri = module.getModuleConfiguration()
					.getClassifiedIri();
			IRI unclassifiedIri = module.getModuleConfiguration()
					.getClassifiedIri();

			if (entry.getValue() == null) {
				unclassifiedManager.applyChange(new AddImport(
						unclassifiedModule, df
								.getOWLImportsDeclaration(classifiedIri)));
				unclassifiedManager.applyChange(new AddImport(
						unclassifiedModule, df
								.getOWLImportsDeclaration(unclassifiedIri)));
			} else if (entry.getValue() == true) {
				unclassifiedManager.applyChange(new AddImport(
						unclassifiedModule, df
								.getOWLImportsDeclaration(classifiedIri)));

			} else {
				unclassifiedManager.applyChange(new AddImport(
						unclassifiedModule, df
								.getOWLImportsDeclaration(unclassifiedIri)));
			}

			module.saveUnclassifiedModule();
		}

		String fileName = getModuleConfiguration().getUnclassifiedFileName();
		RDFXMLOntologyFormat of = new RDFXMLOntologyFormat();
		try {
			unclassifiedManager.saveOntology(unclassifiedModule, of,
					new FileDocumentTarget(new File(
							outputUnclassified.toFile(), fileName)));
		} catch (OWLOntologyStorageException e) {
			throw new RuntimeException(
					"Error savign unclassified module to file: "
							+ outputUnclassified.toAbsolutePath(), e);
		}

	}

	@Override
	public void saveClassifiedModule() {

		if(! isClassified()){
			return;
		}
		
		if (outputClassified == null) {
			logger.warn("Output directory for classified module "
					+ getModuleConfiguration().getName()
					+ " is null, skipping save and all imports.");
			return;
		}

		getFinalClassified();

		for (Entry<IModule, Boolean> entry : classifiedImports.entrySet()) {
			IModule module = entry.getKey();
			IRI classifiedIri = module.getModuleConfiguration()
					.getClassifiedIri();
			IRI unclassifiedIri = module.getModuleConfiguration()
					.getClassifiedIri();

			if (entry.getValue() == null) {
				classifiedManager.applyChange(new AddImport(classifiedModule,
						df.getOWLImportsDeclaration(classifiedIri)));
				classifiedManager.applyChange(new AddImport(classifiedModule,
						df.getOWLImportsDeclaration(unclassifiedIri)));
			} else if (entry.getValue() == true) {
				classifiedManager.applyChange(new AddImport(classifiedModule,
						df.getOWLImportsDeclaration(classifiedIri)));

			} else {
				classifiedManager.applyChange(new AddImport(classifiedModule,
						df.getOWLImportsDeclaration(unclassifiedIri)));
			}

			module.saveClassifiedModule();
		}

		String fileName = getModuleConfiguration().getClassifiedFileName();
		RDFXMLOntologyFormat of = new RDFXMLOntologyFormat();
		try {
			classifiedManager.saveOntology(classifiedModule, of,
					new FileDocumentTarget(new File(outputClassified.toFile(),
							fileName)));
		} catch (OWLOntologyStorageException e) {
			throw new RuntimeException(
					"Error savign classified module to file: "
							+ outputClassified.toAbsolutePath(), e);
		}

	}

	@Override
	public Report getReportClassified() {
		return reportClassified;
	}

	@Override
	public Report getReportUnclassified() {
		return reportUnclassified;
	}

	// ================================================================================
	// for builders
	// ================================================================================

	@Override
	public void addAnnotationUnclassified(OWLAnnotation annotation,
			IModuleBuilder builder) {

		unclassifiedManager.applyChange(new AddOntologyAnnotation(
				unclassifiedModule, annotation));

	}

	@Override
	public void addAnnotationsUnclassified(Set<OWLAnnotation> annotations,
			IModuleBuilder builder) {
		for (OWLAnnotation a : annotations) {
			addAnnotationUnclassified(a, builder);
		}
	}

	@Override
	public void removeAnnotationUnclassified(OWLAnnotation annotation,
			IModuleBuilder builder) {
		unclassifiedManager.applyChange(new RemoveOntologyAnnotation(
				unclassifiedModule, annotation));

	}

	@Override
	public void removeAnnotationsUnclassified(Set<OWLAnnotation> annotations,
			IModuleBuilder builder) {
		for (OWLAnnotation a : annotations) {
			removeAnnotationUnclassified(a, builder);
		}

	}

	// inferred versions

	@Override
	public void addAnnotationClassified(OWLAnnotation annotation,
			IModuleBuilder builder) {
		classifiedManager.applyChange(new AddOntologyAnnotation(
				classifiedModule, annotation));

	}

	@Override
	public void addAnnotationsClassified(Set<OWLAnnotation> annotations,
			IModuleBuilder builder) {
		for (OWLAnnotation a : annotations) {
			addAnnotationClassified(a, builder);
		}

	}

	@Override
	public void removeAnnotationClassified(OWLAnnotation annotation,
			IModuleBuilder builder) {
		classifiedManager.applyChange(new RemoveOntologyAnnotation(
				classifiedModule, annotation));

	}

	@Override
	public void removeAnnotationsClassified(Set<OWLAnnotation> annotations,
			IModuleBuilder builder) {
		for (OWLAnnotation a : annotations) {
			removeAnnotationClassified(a, builder);
		}
	}

	@Override
	public void addAxiomUnclassified(OWLAxiom axiom, IModuleBuilder builder) {
		unclassifiedManager.addAxiom(unclassifiedModule, axiom);

	}

	@Override
	public void addAxiomsUnclassified(Set<OWLAxiom> axioms,
			IModuleBuilder builder) {
		for (OWLAxiom a : axioms) {
			addAxiomUnclassified(a, builder);
		}

	}

	@Override
	public void removeAxiomUnclassified(OWLAxiom axiom, IModuleBuilder builder) {
		unclassifiedManager.removeAxiom(unclassifiedModule, axiom);

	}

	@Override
	public void removeAxiomsUnclassified(Set<OWLAxiom> axioms,
			IModuleBuilder builder) {
		for (OWLAxiom a : axioms) {
			removeAxiomUnclassified(a, builder);
		}

	}

	@Override
	public void addAxiomClassified(OWLAxiom axiom, IModuleBuilder builder) {
		classifiedManager.addAxiom(classifiedModule, axiom);

	}

	@Override
	public void addAxiomsClassified(Set<OWLAxiom> axioms, IModuleBuilder builder) {
		for (OWLAxiom a : axioms) {
			addAxiomClassified(a, builder);
		}

	}

	@Override
	public void removeAxiomClassified(OWLAxiom axiom, IModuleBuilder builder) {
		classifiedManager.removeAxiom(classifiedModule, axiom);

	}

	@Override
	public void removeAxiomsClassified(Set<OWLAxiom> axioms,
			IModuleBuilder builder) {
		for (OWLAxiom a : axioms) {
			removeAxiomClassified(a, builder);
		}

	}

	// ================================================================================
	// not reviewed
	// ================================================================================

	@Override
	public void dispose() {
		if (this.disposed) {
			return;
		}
		this.disposed = true;
		// TODO: review

	}

	@Override
	public int hashCode() {
		return this.moduleConfiguration.getName().hashCode();
	}

	@Override
	public OWLDataFactory getDataFactory() {
		return df;
	}

	@Override
	public boolean equals(Object obj) {

		// TODO: should it be possible to have multiple modules with same name
		// during runtime? This equality is probably too strong.
		if (this == obj) {
			return true;
		}
		if (obj instanceof IModule) {
			return this.moduleConfiguration.getName().equals(
					((IModule) obj).getModuleConfiguration().getName());
		}
		return false;
	}

	@Override
	public void initialize() {
		if (outputClassified != null) {
			this.reportClassified = reportFactory.createReport(
					getModuleConfiguration().getClassifiedFileName()
							+ ".unclassified-report",
					new File(owlclManager.getJobDirectory(null), "module")
							.toPath(), this);
		}

		if (outputUnclassified != null) {
			this.reportUnclassified = reportFactory.createReport(
					getModuleConfiguration().getUnclassifiedFileName()
							+ ".unclassified-report",
					new File(owlclManager.getJobDirectory(null), "module")
							.toPath(), this);
		}
	}

	@Override
	public Logger getLogger() {
		return this.logger;
	}

	//
	// @Override
	// public void generateModule() {
	// if (generated)
	// {
	// return;
	// }
	// generated = true;
	//
	// List<IModuleBuilder> buildersInferred = new ArrayList<IModuleBuilder>();
	//
	// Set<OWLAxiom> includeAxioms = OwlclUtil.getAxioms(
	// this.moduleConfiguration.getIncludeOntology(), true);
	// Set<OWLAxiom> excludeAxioms = OwlclUtil.getAxioms(
	// this.moduleConfiguration.getExcludeOntology(), true);
	//
	// if (isUnclassified())
	// {
	// report.info("Is generate is true.");
	//
	// addAxiomsUnclassified(includeAxioms);
	// removeAxiomsUnclassified(excludeAxioms);
	//
	// if (isAddLegacy())
	// {
	// addAxiomsUnclassified(OwlclUtil.getAxioms(this.moduleConfiguration.getLegacyOntology(),
	// true));
	// }
	// }
	//
	// if (isClassified())
	// {
	// report.info("Is generate inferred is true.");
	// generatedModuleInferred = OwlclUtil.createOntology(
	// this.moduleConfiguration.getGeneratedInferredModuleIri(), genManager);
	// for (String builderName :
	// this.moduleConfiguration.getBuildersInferredNames())
	// {
	// report.info("Doing builder name: " + builderName);
	// IModuleBuilder builder = builderManager.getBuilder(builderName, this);
	// if (builder == null)
	// {
	// logger.error("No inferred builder named {} was found for module {}",
	// builderName,
	// moduleConfiguration.getModuleName());
	// continue;
	// }
	// report.info("Found builder: " + builder.getClass().getName());
	// builder.build(this, true);
	// buildersInferred.add(builder);
	// }
	// addAxiomsClassified(includeAxioms);
	// removeAxiomsClassified(excludeAxioms);
	//
	// if (isAddLegacy())
	// {
	//
	// addAxiomsClassified(OwlclUtil.getAxioms(this.moduleConfiguration.getLegacyOntology(),
	// true));
	// }
	// }
	//
	// // notify builders, finished
	// Iterator<IModuleBuilder> i = builders.iterator();
	// while (i.hasNext())
	// {
	// IModuleBuilder builder = i.next();
	// i.remove();
	// builder.buildFinished(this);
	// }
	//
	// i = buildersInferred.iterator();
	// while (i.hasNext())
	// {
	// IModuleBuilder builder = i.next();
	// i.remove();
	// builder.buildFinished(this);
	// }
	//
	// // clean legacy
	// if (isCleanLegacy())
	// {
	// OWLOntology legacyOntology =
	// this.moduleConfiguration.getLegacyOntology();
	// if (legacyOntology != null)
	// {
	// Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
	// if (generatedModule != null)
	// {
	// axioms = generatedModule.getAxioms();
	// }
	// if (generatedModuleInferred != null)
	// {
	// axioms.addAll(generatedModuleInferred.getAxioms());
	// }
	// axioms.addAll(OwlclUtil.getAxioms(this.moduleConfiguration.getExcludeOntology(),
	// true));
	//
	// Set<OWLAxiom> removedAxioms = new HashSet<OWLAxiom>();
	//
	// for (OWLOntology o : legacyOntology.getImportsClosure())
	// {
	// List<OWLOntologyChange> changes =
	// o.getOWLOntologyManager().removeAxioms(o,
	// axioms);
	// logger.info("Cleaned legacy ontology: " + o.getOntologyID() +
	// ", change count: "
	// + changes.size());
	//
	// for (OWLOntologyChange change : changes)
	// {
	// removedAxioms.add(change.getAxiom());
	// }
	// }
	//
	// OWLOntology legacyRemovedOntology =
	// this.moduleConfiguration.getLegacyRemovedOntology();
	// if (legacyRemovedOntology != null)
	// {
	// legacyRemovedOntology.getOWLOntologyManager().addAxioms(legacyRemovedOntology,
	// removedAxioms);
	// }
	// }
	// }
	//
	// Set<IModule> imports = new HashSet<IModule>();
	// imports.addAll(generateImports.keySet());
	// imports.addAll(generateInferredImports.keySet());
	// imports.addAll(bothImports.keySet());
	//
	// for (IModule module : imports)
	// {
	// module.generateModule();
	// }
	//
	// report.finish();
	// }
	//
	// @Override
	// public boolean isAddLegacy() {
	// if (addLegacy == null)
	// {
	// return this.moduleConfiguration.isAddLegacy();
	// }
	// return addLegacy;
	// }
	//
	// @Override
	// public boolean isCleanLegacy() {
	// if (cleanLegacy == null)
	// {
	// return this.moduleConfiguration.isCleanLegacy();
	// }
	// return cleanLegacy;
	// }

	// @Override
	// public void saveGeneratedModule() {
	// if (saved)
	// {
	// return;
	// }
	// saved = true;
	//
	// Set<IModule> allImportedModules = new HashSet<IModule>();
	//
	// if (isUnclassified())
	// {
	// report.info("Saving generate module.");
	// allImportedModules.addAll(bothImports.keySet());
	// allImportedModules.addAll(generateImports.keySet());
	//
	// for (Entry<IModule, Boolean> entry : bothImports.entrySet())
	// {
	// if (entry.getValue() == null || entry.getValue() == false)
	// {
	// // import matching type
	// addImport(entry.getKey().getModuleConfiguration().getModuleName(),
	// entry.getKey()
	// .getModuleConfiguration().getGeneratedModuleIri(), generatedModule);
	// } else if (entry.getValue() == true)
	// {
	// addImport(entry.getKey().getModuleConfiguration().getModuleName(),
	// entry.getKey()
	// .getModuleConfiguration().getGeneratedInferredModuleIri(),
	// generatedModule);
	// }
	// }
	//
	// for (Entry<IModule, Boolean> entry : generateImports.entrySet())
	// {
	// if (entry.getValue() == false)
	// {
	// addImport(entry.getKey().getModuleConfiguration().getModuleName(),
	// entry.getKey()
	// .getModuleConfiguration().getGeneratedModuleIri(), generatedModule);
	// } else if (entry.getValue() == true)
	// {
	// addImport(entry.getKey().getModuleConfiguration().getModuleName(),
	// entry.getKey()
	// .getModuleConfiguration().getGeneratedInferredModuleIri(),
	// generatedModule);
	// }
	// }
	//
	// try
	// {
	// logger.info("Saving module: " + getModuleConfiguration().getModuleName()
	// + " into ontology: " + generatedModule.getOntologyID() +
	// " in  directory: "
	// + getOutputDirectory() + " and file: "
	// + this.moduleConfiguration.getGenerateModuleFileName());
	//
	// genManager.saveOntology(generatedModule, new FileOutputStream(new File(
	// getOutputDirectory(),
	// this.moduleConfiguration.getGenerateModuleFileName())));
	// } catch (OWLOntologyStorageException | FileNotFoundException e)
	// {
	// throw new RuntimeException("Failed to save module generated "
	// + getModuleConfiguration().getModuleName() + " with file name "
	// + this.moduleConfiguration.getGenerateModuleFileName(), e);
	// }
	//
	// }
	//
	// if (isClassified())
	// {
	// allImportedModules.addAll(bothImports.keySet());
	// allImportedModules.addAll(generateInferredImports.keySet());
	//
	// for (Entry<IModule, Boolean> entry : bothImports.entrySet())
	// {
	// if (entry.getValue() == null || entry.getValue() == true)
	// {
	// addImport(entry.getKey().getModuleConfiguration().getModuleName(),
	// entry.getKey()
	// .getModuleConfiguration().getGeneratedInferredModuleIri(),
	// generatedModuleInferred);
	// } else
	// {
	// addImport(entry.getKey().getModuleConfiguration().getModuleName(),
	// entry.getKey()
	// .getModuleConfiguration().getGeneratedModuleIri(),
	// generatedModuleInferred);
	// }
	// }
	//
	// for (Entry<IModule, Boolean> entry : generateInferredImports.entrySet())
	// {
	// if (entry.getValue() == true)
	// {
	// addImport(entry.getKey().getModuleConfiguration().getModuleName(),
	// entry.getKey()
	// .getModuleConfiguration().getGeneratedInferredModuleIri(),
	// generatedModuleInferred);
	// } else
	// {
	// addImport(entry.getKey().getModuleConfiguration().getModuleName(),
	// entry.getKey()
	// .getModuleConfiguration().getGeneratedModuleIri(),
	// generatedModuleInferred);
	// }
	// }
	//
	// try
	// {
	// logger.info("Saving module: " + getModuleConfiguration().getModuleName()
	// + " into ontology: " + generatedModuleInferred.getOntologyID() +
	// " in  directory: "
	// + getOutputDirectory() + " and file: "
	// + this.moduleConfiguration.getGenerateInferredModuleFileName());
	//
	// genManager.saveOntology(generatedModuleInferred, new FileOutputStream(new
	// File(
	// getOutputDirectory(),
	// this.moduleConfiguration.getGenerateInferredModuleFileName())));
	// } catch (FileNotFoundException | OWLOntologyStorageException e)
	// {
	// throw new RuntimeException("Failed to save module inferred "
	// + getModuleConfiguration().getModuleName() + " with file name "
	// + this.moduleConfiguration.getGenerateInferredModuleFileName(), e);
	// }
	//
	// }
	//
	// for (IModule module : allImportedModules)
	// {
	// module.saveGeneratedModule();
	// }
	//
	// }

	// ================================================================================
	// Builder related
	// ================================================================================

}