package com.essaid.owlcl.command.module.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import com.essaid.owlcl.command.module.ModuleConstant;
import com.essaid.owlcl.command.module.ModuleVocab;
import com.essaid.owlcl.command.module.Util;
import com.essaid.owlcl.core.util.OwlclConstants;
import com.essaid.owlcl.core.util.OwlclUtil;

public class ModuleConfigurationV1 extends AbstractModuleConfiguration {

	public static AbstractModuleConfiguration getExistingInstance(
			Path directoryPath, OWLOntologyManager configManager,
			OWLOntologyManager sourceManager, OWLOntology sourceOntology,
			OWLReasoner sourceReasoner, boolean allowMissingImports) {
		if (directoryPath == null || !directoryPath.toFile().isDirectory()
				|| configManager == null || sourceManager == null) {
			throw new IllegalStateException(
					"Existing module configuration with null or "
							+ "non-existing directory or manager: "
							+ directoryPath);
		}

		File directory = directoryPath.toFile();
		int version = Util.getModuleVersion(directory);
		if (version != 1) {
			throw new IllegalStateException(
					"Loading V-1 configuration form directory with version: "
							+ version);
		}

		AutoIRIMapper mapper = new AutoIRIMapper(directory, false);
		IRI configIri = null;
		for (IRI iri : mapper.getOntologyIRIs()) {
			if (iri.toString().endsWith(
					ModuleConstant.MODULE_CONFIGURATION_IRI_SUFFIX)) {
				configIri = iri;
				break;
			}
		}
		if (configIri == null) {
			throw new IllegalStateException(
					"Failed to find cofiguration ontology in direcotry: "
							+ directoryPath.toAbsolutePath());
		}

		int index = configIri.toString().lastIndexOf("/") + 1;
		String prefix = configIri.toString().substring(0, index);
		String name = configIri.toString().substring(index);
		name = name.substring(0,
				name.indexOf(ModuleConstant.MODULE_CONFIGURATION_IRI_SUFFIX));

		File configFile = new File(directory, name
				+ ModuleConstant.MODULE_CONFIGURATION_IRI_SUFFIX);

		OWLOntologyLoaderConfiguration lc = new OWLOntologyLoaderConfiguration();
		lc.addIgnoredImport(IRI.create(prefix + name
				+ ModuleConstant.MODULE_SOURCE_IRI_SUFFIX));

		OWLOntologyDocumentSource ds = new FileDocumentSource(configFile);

		OWLOntology configOntology = null;
		try {
			configOntology = configManager.loadOntologyFromOntologyDocument(ds,
					lc);
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException("Failed to load configuration file.", e);
		}

		AbstractModuleConfiguration config = new ModuleConfigurationV1(name,
				prefix, directoryPath, configOntology);

		config.sourceManager = sourceManager;
		config.sourceOntology = sourceOntology;
		config.sourceReasoner = sourceReasoner;

		return config;
	}

	public static AbstractModuleConfiguration getNewInstance(String name,
			String iriPrefix, Path directoryPath,
			OWLOntologyManager configManager, OWLOntologyManager sourceManager,
			OWLOntology sourceOntology, OWLReasoner sourceReasoner) {

		if (name == null || iriPrefix == null || directoryPath == null
				|| configManager == null || sourceManager == null) {
			throw new IllegalStateException(
					"New module configuration with null arguments.");
		}

		File directory = directoryPath.toFile();
		if (directory.exists()) {
			if (!directory.isDirectory()) {
				throw new IllegalStateException(
						"New module configuration direcotory is a file.");
			}

			if (directory.list().length > 0) {
				throw new IllegalStateException(
						"New module configuration directory is not empty.");
			}

		} else {
			directory.mkdirs();
		}

		OWLOntology configOntology = OwlclUtil.createOntology(
				IRI.create(iriPrefix + name
						+ ModuleConstant.MODULE_CONFIGURATION_IRI_SUFFIX),
				configManager);
		configManager.setOntologyDocumentIRI(
				configOntology,
				IRI.create(new File(directory, name
						+ ModuleConstant.MODULE_CONFIGURATION_IRI_SUFFIX)));
		File versionFile = new File(directory, "V-1");
		try {
			versionFile.createNewFile();
		} catch (IOException e) {
			throw new RuntimeException("Failed to create version file in:"
					+ versionFile.getAbsolutePath(), e);
		}

		try {
			configManager.saveOntology(configOntology);
		} catch (OWLOntologyStorageException e) {
			throw new RuntimeException(
					"Failed to save new configuration ontology.", e);
		}

		AbstractModuleConfiguration config = new ModuleConfigurationV1(name,
				iriPrefix, directoryPath, configOntology);

		config.sourceManager = sourceManager;
		config.sourceOntology = sourceOntology;
		config.sourceReasoner = sourceReasoner;

		return config;
	}

	// ================================================================================
	//
	// ================================================================================

	private ModuleConfigurationV1(String name, String iriPrefix,
			Path moduleDirectory, OWLOntology configurationOntology) {
		super(name, iriPrefix, moduleDirectory, configurationOntology);
	}

	// ================================================================================
	//
	// ================================================================================

	@Override
	public void update() {

		//
		if (getClassifiedBuilderNamesStated() == null) {
			List<String> builders = new ArrayList<String>();
			builders.add("");
			setClassifiedBuilderNames(builders);
		}
		if (getClassifiedFilenameStated() == null) {
			setClassifiedFilename(getClassifiedFileName());
		}
		if (getClassifiedIriStated() == null) {
			setClassifiedIri(getClassifiedIri());
		}
		if (isClassifiedStated() == null) {
			setClassified(false);
		}
		if (isClassifiedAddlegacyStated() == null) {
			setClassifiedAddlegacy(false);
		}
		if (isClassifiedCleanlegacyStated() == null) {
			setClassifiedCleanlegacy(false);
		}

		//
		if (getUnclassifiedBuilderNamesStated() == null) {
			List<String> builders = new ArrayList<String>();
			builders.add("");
			setUnclassifiedBuilderNames(builders);
		}
		if (getUnclassifiedFilenameStated() == null) {
			setUnclassifiedFilename(getUnclassifiedFileName());
		}
		if (getUnclassifiedIriStated() == null) {
			setUnclassifiedIri(getUnclassifiedIri());
		}
		if (isUnclassifiedStated() == null) {
			setUnclassified(false);
		}
		if (isUnclassifiedAddlegacyStated() == null) {
			setUnclassifiedAddlegacy(false);
		}
		if (isUnclassifiedCleanlegacyStated() == null) {
			setUnclassifiedCleanlegacy(false);
		}

		configMan
				.applyChange(new AddImport(getConfigurationOntology(), configDf
						.getOWLImportsDeclaration(getSourceConfigurationIri())));
		configMan
				.applyChange(new AddImport(
						getConfigurationOntology(),
						configDf.getOWLImportsDeclaration(OwlclConstants.OWLCL_ONTOLOGY_IRI)));

		// check base files

		// // check include
		File includeFile = new File(getDirectory().toFile(), getName()
				+ ModuleConstant.MODULE_INCLUDE_IRI_SUFFIX);
		if (!includeFile.exists()) {
			OWLOntology includeOntology = OwlclUtil.createOntology(
					getIncludeIri(), configMan);
			configMan.setOntologyDocumentIRI(includeOntology,
					IRI.create(includeFile));
			saveOntology(includeOntology);
		}

		//
		// // check exclude
		File excludeFile = new File(getDirectory().toFile(), getName()
				+ ModuleConstant.MODULE_EXCLUDE_IRI_SUFFIX);
		if (!excludeFile.exists()) {
			OWLOntology excludeOntology = OwlclUtil.createOntology(
					getExcludeIri(), configMan);
			configMan.setOntologyDocumentIRI(excludeOntology,
					IRI.create(excludeFile));
			saveOntology(excludeOntology);
		}
		//
		// // check legacy
		File legacyFile = new File(getDirectory().toFile(), getName()
				+ ModuleConstant.MODULE_LEGACY_IRI_SUFFIX);
		if (!legacyFile.exists()) {
			OWLOntology legacyOntology = OwlclUtil.createOntology(
					getLegacyIri(), configMan);
			configMan.setOntologyDocumentIRI(legacyOntology,
					IRI.create(legacyFile));
			saveOntology(legacyOntology);
		}
		//
		// // check legacy removed
		File legacyRemovedFile = new File(getDirectory().toFile(), getName()
				+ ModuleConstant.MODULE_LEGACY_REMOVED_IRI_SUFFIX);
		if (!legacyRemovedFile.exists()) {
			OWLOntology legacyRemovedOntology = OwlclUtil.createOntology(
					getLegacyRemovedIri(), configMan);
			configMan.setOntologyDocumentIRI(legacyRemovedOntology,
					IRI.create(legacyRemovedFile));
			saveOntology(legacyRemovedOntology);
		}
		//
		// // check source
		File sourcesFile = new File(getDirectory().toFile(), getName()
				+ ModuleConstant.MODULE_SOURCE_IRI_SUFFIX);
		if (!sourcesFile.exists()) {
			OWLOntology sourcesOntology = OwlclUtil.createOntology(
					getSourceConfigurationIri(), configMan);
			configMan.setOntologyDocumentIRI(sourcesOntology,
					IRI.create(sourcesFile));
			saveOntology(sourcesOntology);
			// the source manager should load this one
			configMan.removeOntology(sourcesOntology);
		}

		// top
		File file = new File(getDirectory().toFile(), getName()
				+ ModuleConstant.MODULE_TOP_IRI_SUFFIX);
		OWLOntology topOntology;
		if (!file.exists()) {
			topOntology = OwlclUtil.createOntology(getTopIri(), configMan);
			configMan.setOntologyDocumentIRI(topOntology, IRI.create(file));

		} else {
			topOntology = OwlclUtil.loadOntologyIgnoreImports(file, configMan);
		}
		configMan.applyChange(new AddImport(topOntology, configDf
				.getOWLImportsDeclaration(getConfigurationIri())));
		configMan.applyChange(new AddImport(topOntology, configDf
				.getOWLImportsDeclaration(getIncludeIri())));
		configMan.applyChange(new AddImport(topOntology, configDf
				.getOWLImportsDeclaration(getExcludeIri())));
		configMan.applyChange(new AddImport(topOntology, configDf
				.getOWLImportsDeclaration(getLegacyIri())));
		configMan.applyChange(new AddImport(topOntology, configDf
				.getOWLImportsDeclaration(getLegacyRemovedIri())));
		saveOntology(topOntology);

	}

	//

	//
	// checkIsUnclassified(o, logger);
	// value = getAnnotations(o,
	// ModuleVocab.module_is_unclassified.getAP()).iterator().next()
	// .getValue().toString();
	// this.unclassified = value.equalsIgnoreCase("true");
	//
	// checkIsClassified(o, logger);
	// value = getAnnotations(o,
	// ModuleVocab.module_is_classified.getAP()).iterator().next()
	// .getValue().toString();
	// this.classified = value.equalsIgnoreCase("true");
	//
	// checkUnclassifiedAddlegacy(o, logger);
	// value = getAnnotations(o,
	// ModuleVocab.module_unclassified_addlegacy.getAP()).iterator().next()
	// .getValue().toString();
	// this.unclassifiedAddlegacy = value.equalsIgnoreCase("true");
	//
	// checkClassifiedAddlegacy(o, logger);
	// value = getAnnotations(o,
	// ModuleVocab.module_classified_addlegacy.getAP()).iterator().next()
	// .getValue().toString();
	// this.classifiedAddlegacy = value.equalsIgnoreCase("true");
	//
	// checkUnclassifiedCleanlegacy(o, logger);
	// value = getAnnotations(o,
	// ModuleVocab.module_unclassified_cleanlegacy.getAP()).iterator()
	// .next().getValue().toString();
	// this.unclassifiedCleanlegacy = value.equalsIgnoreCase("true");
	//
	// checkClassifiedCleanlegacy(o, logger);
	// value = getAnnotations(o,
	// ModuleVocab.module_classified_cleanlegacy.getAP()).iterator().next()
	// .getValue().toString();
	// this.classifiedCleanlegacy = value.equalsIgnoreCase("true");
	//
	// checkUnclassifiedBuilders(o, "", logger);
	// value = getAnnotations(o,
	// ModuleVocab.module_unclassified_builders.getAP()).iterator().next()
	// .getValue().toString();
	// List<String> builderNames = new ArrayList<String>();
	// for (String name : value.split(","))
	// {
	// builderNames.add(name.trim());
	// }
	// this.unclassifiedBuilderNames = builderNames;
	//
	// checkClassifiedBuilders(o, "", logger);
	// value = getAnnotations(o,
	// ModuleVocab.module_classified_builders.getAP()).iterator().next()
	// .getValue().toString();
	// builderNames = new ArrayList<String>();
	// for (String name : value.split(","))
	// {
	// builderNames.add(name.trim());
	// }
	// this.classifiedBuilderNames = builderNames;
	//
	// if (changedOntologies.contains(o))
	// {
	// changedOntologies.remove(o);
	// saveOntology(o);
	// }
	//
	// //
	// ================================================================================
	// // Other owl files
	// //
	// ================================================================================
	//

	// }

	public Set<OWLAnnotation> getAnnotations() {
		return getConfigurationOntology().getAnnotations();
	}

	// ================================================================================
	// classified builder names
	// ================================================================================

	@Override
	public List<String> getClassifiedBuilderNamesStated() {
		Iterator<OWLAnnotation> i = getAnnotations(getConfigurationOntology(),
				ModuleVocab.module_classified_builders.getAP(), false)
				.iterator();
		if (i.hasNext()) {
			List<String> builderNames = new ArrayList<String>();
			for (String name : ((OWLLiteral) i.next().getValue()).getLiteral()
					.split(",")) {
				builderNames.add(name.trim());
			}
			return builderNames;
		} else {
			return null;
		}
	}

	@Override
	public List<String> getClassifiedBuilderNames() {
		List<String> names = getClassifiedBuilderNamesStated();
		return names != null ? names : new ArrayList<String>();
	}

	public void setClassifiedBuilderNames(List<String> names) {
		OWLOntology co = getConfigurationOntology();
		removeAnnotations(co, ModuleVocab.module_classified_builders.getAP());
		configMan.applyChange(new AddOntologyAnnotation(co, configDf
				.getOWLAnnotation(
						ModuleVocab.module_classified_builders.getAP(),
						configDf.getOWLLiteral(csv(names)))));
		saveOntology(co);
	}

	// ================================================================================
	// unclassified builder names
	// ================================================================================

	@Override
	public List<String> getUnclassifiedBuilderNamesStated() {
		Iterator<OWLAnnotation> i = getAnnotations(getConfigurationOntology(),
				ModuleVocab.module_unclassified_builders.getAP(), false)
				.iterator();
		if (i.hasNext()) {
			List<String> builderNames = new ArrayList<String>();
			for (String name : ((OWLLiteral) i.next().getValue()).getLiteral()
					.split(",")) {
				builderNames.add(name.trim());
			}
			return builderNames;
		} else {
			return null;
		}
	}

	@Override
	public List<String> getUnclassifiedBuilderNames() {
		List<String> names = getUnclassifiedBuilderNamesStated();
		return names != null ? names : new ArrayList<String>();
	}

	public void setUnclassifiedBuilderNames(List<String> builderNames) {
		OWLOntology co = getConfigurationOntology();
		removeAnnotations(co, ModuleVocab.module_unclassified_builders.getAP());
		configMan.applyChange(new AddOntologyAnnotation(co, configDf
				.getOWLAnnotation(
						ModuleVocab.module_unclassified_builders.getAP(),
						configDf.getOWLLiteral(csv(builderNames)))));
		saveOntology(co);
	}

	// ================================================================================
	// classified file name
	// ================================================================================

	@Override
	public String getClassifiedFilenameStated() {
		Iterator<OWLAnnotation> i = getAnnotations(getConfigurationOntology(),
				ModuleVocab.module_classified_filename.getAP(), false)
				.iterator();
		if (i.hasNext()) {
			return ((OWLLiteral) i.next().getValue()).getLiteral();
		} else {
			return null;
		}

	}

	@Override
	public String getClassifiedFileName() {
		String fileName = getClassifiedFilenameStated();
		return fileName != null ? fileName : getName()
				+ ModuleConstant.MODULE_CLASSIFIED_SUFFIX;
	}

	@Override
	public void setClassifiedFilename(String name) {
		OWLOntology co = getConfigurationOntology();
		removeAnnotations(co, ModuleVocab.module_classified_filename.getAP());
		configMan.applyChange(new AddOntologyAnnotation(co, configDf
				.getOWLAnnotation(
						ModuleVocab.module_classified_filename.getAP(),
						configDf.getOWLLiteral(name))));
		saveOntology(co);
	}

	// ================================================================================
	// unclassified file name
	// ================================================================================

	@Override
	public String getUnclassifiedFilenameStated() {
		Iterator<OWLAnnotation> i = getAnnotations(getConfigurationOntology(),
				ModuleVocab.module_unclassified_filename.getAP(), false)
				.iterator();
		if (i.hasNext()) {
			return ((OWLLiteral) i.next().getValue()).getLiteral();
		} else {
			return null;
		}
	}

	@Override
	public String getUnclassifiedFileName() {
		String fileName = getUnclassifiedFilenameStated();
		return fileName != null ? fileName : getName()
				+ ModuleConstant.MODULE_UNCLASSIFIED_SUFFIX;
	}

	@Override
	public void setUnclassifiedFilename(String name) {
		OWLOntology co = getConfigurationOntology();
		removeAnnotations(co, ModuleVocab.module_unclassified_filename.getAP());
		configMan.applyChange(new AddOntologyAnnotation(co, configDf
				.getOWLAnnotation(
						ModuleVocab.module_unclassified_filename.getAP(),
						configDf.getOWLLiteral(name))));
		saveOntology(co);
	}

	// ================================================================================
	// classified IRI
	// ================================================================================
	@Override
	public IRI getClassifiedIriStated() {
		Iterator<OWLAnnotation> i = getAnnotations(getConfigurationOntology(),
				ModuleVocab.module_classified_iri.getAP(), false).iterator();
		if (i.hasNext()) {
			return IRI.create(((OWLLiteral) i.next().getValue()).getLiteral());
		} else {
			return null;
		}
	}

	@Override
	public IRI getClassifiedIri() {
		IRI iri = getClassifiedIriStated();
		return iri != null ? iri : IRI.create(getIriPrefix() + getName()
				+ ModuleConstant.MODULE_CLASSIFIED_SUFFIX);
	}

	@Override
	public void setClassifiedIri(IRI iri) {
		OWLOntology co = getConfigurationOntology();
		removeAnnotations(co, ModuleVocab.module_classified_iri.getAP());
		configMan.applyChange(new AddOntologyAnnotation(co, configDf
				.getOWLAnnotation(ModuleVocab.module_classified_iri.getAP(),
						configDf.getOWLLiteral(iri.toString()))));
		saveOntology(co);
	}

	// ================================================================================
	// unclassified IRI
	// ================================================================================

	@Override
	public IRI getUnclassifiedIriStated() {
		Iterator<OWLAnnotation> i = getAnnotations(getConfigurationOntology(),
				ModuleVocab.module_unclassified_iri.getAP(), false).iterator();
		if (i.hasNext()) {
			return IRI.create(((OWLLiteral) i.next().getValue()).getLiteral());
		} else {
			return null;
		}
	}

	@Override
	public IRI getUnclassifiedIri() {
		IRI iri = getUnclassifiedIriStated();
		return iri != null ? iri : IRI.create(getIriPrefix() + getName()
				+ ModuleConstant.MODULE_UNCLASSIFIED_SUFFIX);
	}

	@Override
	public void setUnclassifiedIri(IRI iri) {
		OWLOntology co = getConfigurationOntology();
		removeAnnotations(co, ModuleVocab.module_unclassified_iri.getAP());
		configMan.applyChange(new AddOntologyAnnotation(co, configDf
				.getOWLAnnotation(ModuleVocab.module_unclassified_iri.getAP(),
						configDf.getOWLLiteral(iri.toString()))));
		saveOntology(co);
	}

	// ================================================================================
	// excluded source iris
	// ================================================================================

	@Override
	public Set<IRI> getExcludedSourceIris() {
		OWLOntology so = getSourceConfigurationOntology();
		Set<IRI> iris = new HashSet<IRI>();

		Iterator<OWLAnnotation> i = getAnnotations(so,
				ModuleVocab.module_source_exclude.getAP(), false).iterator();
		while (i.hasNext()) {
			iris.add(IRI.create(((OWLLiteral) i.next().getValue()).getLiteral()));
		}
		return iris;
	}

	@Override
	public void setSourceExcludedIris(Set<IRI> iris) {
		OWLOntology so = getSourceConfigurationOntology();
		removeAnnotations(so, ModuleVocab.module_source_exclude.getAP());
		for (IRI iri : iris) {
			so.getOWLOntologyManager().applyChange(
					new AddOntologyAnnotation(so, so
							.getOWLOntologyManager()
							.getOWLDataFactory()
							.getOWLAnnotation(
									ModuleVocab.module_source_exclude.getAP(),
									so.getOWLOntologyManager()
											.getOWLDataFactory()
											.getOWLLiteral(iri.toString()))));
		}
		saveOntology(so);
	}

	// ================================================================================
	// source iris
	// ================================================================================

	@Override
	public Set<IRI> getSourceIris() {
		Set<IRI> iris = new HashSet<IRI>();
		OWLOntology so = getSourceConfigurationOntology();
		for (OWLImportsDeclaration id : so.getImportsDeclarations()) {
			iris.add(id.getIRI());
		}
		return iris;
	}

	@Override
	public void setSourceIris(Set<IRI> iris) {
		OWLOntology so = getSourceConfigurationOntology();
		OWLOntologyManager man = so.getOWLOntologyManager();
		OWLDataFactory df = man.getOWLDataFactory();

		for (OWLImportsDeclaration id : so.getImportsDeclarations()) {
			man.applyChange(new RemoveImport(so, id));
		}
		for (IRI iri : iris) {
			man.applyChange(new AddImport(so, df.getOWLImportsDeclaration(iri)));
		}
		saveOntology(so);
	}

	// ================================================================================
	// classified
	// ================================================================================

	@Override
	public Boolean isClassifiedStated() {
		Iterator<OWLAnnotation> i = getAnnotations(getConfigurationOntology(),
				ModuleVocab.module_is_classified.getAP(), false).iterator();
		if (i.hasNext()) {
			return ((OWLLiteral) i.next().getValue()).getLiteral()
					.equalsIgnoreCase("true");
		} else {
			return null;
		}
	}

	@Override
	public boolean isClassified() {
		Boolean classified = isClassifiedStated();
		return classified != null ? classified : false;
	}

	@Override
	public void setClassified(boolean classified) {
		String value = classified ? "true" : "false";
		OWLOntology co = getConfigurationOntology();
		removeAnnotations(co, ModuleVocab.module_is_classified.getAP());
		configMan.applyChange(new AddOntologyAnnotation(co, configDf
				.getOWLAnnotation(ModuleVocab.module_is_classified.getAP(),
						configDf.getOWLLiteral(value))));
		saveOntology(co);
	}

	// ================================================================================
	// unclassified
	// ================================================================================

	@Override
	public Boolean isUnclassifiedStated() {
		Iterator<OWLAnnotation> i = getAnnotations(getConfigurationOntology(),
				ModuleVocab.module_is_unclassified.getAP(), false).iterator();
		if (i.hasNext()) {
			return ((OWLLiteral) i.next().getValue()).getLiteral()
					.equalsIgnoreCase("true");
		} else {
			return null;
		}
	}

	@Override
	public boolean isUnclassified() {
		Boolean classified = isUnclassifiedStated();
		return classified != null ? classified : false;
	}

	@Override
	public void setUnclassified(boolean unclassified) {
		String value = unclassified ? "true" : "false";
		OWLOntology co = getConfigurationOntology();
		removeAnnotations(co, ModuleVocab.module_is_unclassified.getAP());
		configMan.applyChange(new AddOntologyAnnotation(co, configDf
				.getOWLAnnotation(ModuleVocab.module_is_unclassified.getAP(),
						configDf.getOWLLiteral(value))));
		saveOntology(co);
	}

	// ================================================================================
	// classified add legacy
	// ================================================================================

	@Override
	public Boolean isClassifiedAddlegacyStated() {
		Iterator<OWLAnnotation> i = getAnnotations(getConfigurationOntology(),
				ModuleVocab.module_classified_addlegacy.getAP(), false)
				.iterator();
		if (i.hasNext()) {
			return ((OWLLiteral) i.next().getValue()).getLiteral()
					.equalsIgnoreCase("true");
		} else {
			return null;
		}
	}

	@Override
	public boolean isClassifiedAddlegacy() {
		Boolean classified = isClassifiedAddlegacyStated();
		return classified != null ? classified : false;
	}

	@Override
	public void setClassifiedAddlegacy(boolean addlegacy) {
		String value = addlegacy ? "true" : "false";
		OWLOntology co = getConfigurationOntology();
		removeAnnotations(co, ModuleVocab.module_classified_addlegacy.getAP());
		configMan.applyChange(new AddOntologyAnnotation(co, configDf
				.getOWLAnnotation(
						ModuleVocab.module_classified_addlegacy.getAP(),
						configDf.getOWLLiteral(value))));
		saveOntology(co);
	}

	// ================================================================================
	// classified clean legacy
	// ================================================================================

	@Override
	public Boolean isClassifiedCleanlegacyStated() {
		Iterator<OWLAnnotation> i = getAnnotations(getConfigurationOntology(),
				ModuleVocab.module_classified_cleanlegacy.getAP(), false)
				.iterator();
		if (i.hasNext()) {
			return ((OWLLiteral) i.next().getValue()).getLiteral()
					.equalsIgnoreCase("true");
		} else {
			return null;
		}
	}

	@Override
	public boolean isClassifiedCleanLegacy() {
		Boolean classified = isClassifiedCleanlegacyStated();
		return classified != null ? classified : false;
	}

	@Override
	public void setClassifiedCleanlegacy(boolean cleanlegacy) {
		String value = cleanlegacy ? "true" : "false";
		OWLOntology co = getConfigurationOntology();
		removeAnnotations(co, ModuleVocab.module_classified_cleanlegacy.getAP());
		configMan.applyChange(new AddOntologyAnnotation(co, configDf
				.getOWLAnnotation(
						ModuleVocab.module_classified_cleanlegacy.getAP(),
						configDf.getOWLLiteral(value))));
		saveOntology(co);
	}

	// ================================================================================
	// unclassified add legacy
	// ================================================================================

	@Override
	public Boolean isUnclassifiedAddlegacyStated() {
		Iterator<OWLAnnotation> i = getAnnotations(getConfigurationOntology(),
				ModuleVocab.module_unclassified_addlegacy.getAP(), false)
				.iterator();
		if (i.hasNext()) {
			return ((OWLLiteral) i.next().getValue()).getLiteral()
					.equalsIgnoreCase("true");
		} else {
			return null;
		}
	}

	@Override
	public boolean isUnclassifiedAddlegacy() {
		Boolean classified = isUnclassifiedAddlegacyStated();
		return classified != null ? classified : false;
	}

	@Override
	public void setUnclassifiedAddlegacy(boolean addlegacy) {
		String value = addlegacy ? "true" : "false";
		OWLOntology co = getConfigurationOntology();
		removeAnnotations(co, ModuleVocab.module_unclassified_addlegacy.getAP());
		configMan.applyChange(new AddOntologyAnnotation(co, configDf
				.getOWLAnnotation(
						ModuleVocab.module_unclassified_addlegacy.getAP(),
						configDf.getOWLLiteral(value))));
		saveOntology(co);
	}

	// ================================================================================
	// unclassified clean legacy
	// ================================================================================

	@Override
	public Boolean isUnclassifiedCleanlegacyStated() {
		Iterator<OWLAnnotation> i = getAnnotations(getConfigurationOntology(),
				ModuleVocab.module_unclassified_cleanlegacy.getAP(), false)
				.iterator();
		if (i.hasNext()) {
			return ((OWLLiteral) i.next().getValue()).getLiteral()
					.equalsIgnoreCase("true");
		} else {
			return null;
		}
	}

	@Override
	public boolean isUnclassifiedCleanLegacy() {
		Boolean classified = isUnclassifiedCleanlegacyStated();
		return classified != null ? classified : false;
	}

	@Override
	public void setUnclassifiedCleanlegacy(boolean cleanlegacy) {
		String value = cleanlegacy ? "true" : "false";
		OWLOntology co = getConfigurationOntology();
		removeAnnotations(co,
				ModuleVocab.module_unclassified_cleanlegacy.getAP());
		configMan.applyChange(new AddOntologyAnnotation(co, configDf
				.getOWLAnnotation(
						ModuleVocab.module_unclassified_cleanlegacy.getAP(),
						configDf.getOWLLiteral(value))));
		saveOntology(co);
	}

	// ================================================================================
	//
	// ================================================================================

	// ================================================================================
	//
	// ================================================================================

	@Override
	public IRI getExcludeIri() {
		return IRI.create(getIriPrefix() + getName()
				+ ModuleConstant.MODULE_EXCLUDE_IRI_SUFFIX);
	}

	@Override
	public OWLOntology getExcludeOntology() {
		return OwlclUtil.getOrLoadOntology(this.getExcludeIri(), configMan);
	}

	@Override
	public IRI getIncludeIri() {
		return IRI.create(getIriPrefix() + getName()
				+ ModuleConstant.MODULE_INCLUDE_IRI_SUFFIX);
	}

	@Override
	public OWLOntology getIncludeOntology() {
		return OwlclUtil.getOrLoadOntology(this.getIncludeIri(), configMan);
	}

	@Override
	public IRI getLegacyIri() {
		return IRI.create(this.getIriPrefix() + this.getName()
				+ ModuleConstant.MODULE_LEGACY_IRI_SUFFIX);
	}

	@Override
	public OWLOntology getLegacyOntology() {
		return OwlclUtil.getOrLoadOntology(this.getLegacyIri(), configMan);
	}

	@Override
	public IRI getLegacyRemovedIri() {
		return IRI.create(this.getIriPrefix() + this.getName()
				+ ModuleConstant.MODULE_LEGACY_REMOVED_IRI_SUFFIX);
	}

	@Override
	public OWLOntology getLegacyRemovedOntology() {
		return OwlclUtil.getOrLoadOntology(this.getLegacyRemovedIri(),
				configMan);
	}

	@Override
	public IRI getSourceConfigurationIri() {
		return IRI.create(this.getIriPrefix() + this.getName()
				+ ModuleConstant.MODULE_SOURCE_IRI_SUFFIX);
	}

	@Override
	public OWLOntology getSourceConfigurationOntology() {
		if (sourceConfigurationOntology == null) {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			man.clearIRIMappers();
			OWLOntologyLoaderConfiguration lc = new OWLOntologyLoaderConfiguration();
			lc = lc.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
			try {
				File sourceFile = new File(getDirectory().toFile(), getName()
						+ ModuleConstant.MODULE_SOURCE_IRI_SUFFIX);
				if (sourceFile.exists()) {
					sourceConfigurationOntology = man
							.loadOntologyFromOntologyDocument(
									new FileDocumentSource(sourceFile), lc);
				} else {

					sourceConfigurationOntology = man
							.createOntology(getSourceConfigurationIri());
					man.setOntologyDocumentIRI(sourceConfigurationOntology,
							IRI.create(sourceFile));
					man.saveOntology(sourceConfigurationOntology);
				}
			} catch (Exception e) {
				throw new RuntimeException(
						"Failed to create or load source configuration ontology.",
						e);
			}
		}

		return sourceConfigurationOntology;
	}

	public OWLOntology getSourceOntology() {
		if (sourceOntology == null) {
			sourceOntology = OwlclUtil.createOntology(
					IRI.create("http://owlcl/merged-source"),
					getSourcesManager());
			Set<IRI> excludes = getExcludedSourceIris();
			for (OWLImportsDeclaration id : getSourceConfigurationOntology()
					.getImportsDeclarations()) {
				if (excludes.contains(id.getIRI())) {
					continue;
				} else {
					OWLOntology source = OwlclUtil.getOrLoadOntology(
							id.getIRI(), getSourcesManager());

					for (OWLOntology o : source.getImportsClosure()) {
						if (!excludes.contains(o.getOntologyID()
								.getOntologyIRI())) {
							getSourcesManager().addAxioms(sourceOntology,
									o.getAxioms());
						}
					}
				}
			}

		}
		return sourceOntology;
	}

	public OWLReasoner getSourceReasoner() {
		if (sourceReasoner == null) {
			sourceReasoner = reasonerManager
					.getReasonedOntology(getSourceOntology());
		}
		return sourceReasoner;
	}

	public OWLOntologyManager getSourcesManager() {
		return sourceManager;
	}

	@Override
	public IRI getTopIri() {
		return IRI.create(getIriPrefix() + getName()
				+ ModuleConstant.MODULE_TOP_IRI_SUFFIX);
	}

	@Override
	public OWLOntology getTopOntology() {
		return OwlclUtil.getOrLoadOntology(getTopIri(), configMan);
	}

	@Override
	public Set<IRI> getClassifiedImportIris() {
		return getClassifiedImportIrisStated();
	}

	private Set<IRI> getClassifiedImportIrisStated() {
		Set<IRI> iris = new HashSet<IRI>();
		Set<OWLAnnotation> importAnnotations = getAnnotations(
				getConfigurationOntology(),
				ModuleVocab.module_classified_import_iri.getAP(), false);

		for (OWLAnnotation importAnnotation : importAnnotations) {
			iris.add(IRI.create(((OWLLiteral) importAnnotation.getValue())
					.getLiteral()));
		}
		return iris;
	}

	@Override
	public Set<IRI> getUnclassifiedImportIris() {
		return getUnclassifiedImportIrisStated();
	}

	private Set<IRI> getUnclassifiedImportIrisStated() {
		Set<IRI> iris = new HashSet<IRI>();
		Set<OWLAnnotation> importAnnotations = getAnnotations(
				getConfigurationOntology(),
				ModuleVocab.module_unclassified_import_iri.getAP(), false);

		for (OWLAnnotation importAnnotation : importAnnotations) {
			iris.add(IRI.create(((OWLLiteral) importAnnotation.getValue())
					.getLiteral()));
		}
		return iris;
	}

}
