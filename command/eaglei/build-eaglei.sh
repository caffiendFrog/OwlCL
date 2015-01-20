#!/bin/sh

TRUNK_DIR=/home/shahim/svn/open.med.harvard.edu/eagle-i-dev/datamodel/trunk
MODULE_DIR=${TRUNK_DIR}/src/isf/module

WORK_DIR=${TRUNK_DIR}/../eaglei-work
OUT_DIR=${TRUNK_DIR}/../eaglei-out
BUILD_DIR=${TRUNK_DIR}/../eaglei-build


export OWLCL_WORK_DIR=${WORK_DIR}
export OWLCL_OUTPUT_DIR=${OUT_DIR}

rm -rf ${BUILD_DIR} || true

##############
# core/ero.owl
##############

owlcl \
-ontologyFiles \
${TRUNK_DIR}/src/isf/ontology \
${MODULE_DIR} \
-jobName \
build-eaglei \
module \
-name \
eaglei \
-classified \
-directory \
${MODULE_DIR}/eaglei \
-outputClassified \
${BUILD_DIR}/core

##############
# core/ero-extended.owl
##############

owlcl \
-ontologyFiles \
${TRUNK_DIR}/src/isf/ontology \
${MODULE_DIR} \
-jobName \
build-eaglei \
module \
-name \
eaglei-extended \
-classified \
-directory \
${MODULE_DIR}/eaglei-extended \
-outputClassified \
${BUILD_DIR}/core

##############
# core/ero-extended.owl  go
##############

owlcl \
-ontologyFiles \
${TRUNK_DIR}/src/isf/ontology \
${MODULE_DIR} \
-jobName \
build-eaglei \
module \
-name \
eaglei-extended-go \
-classified \
-directory \
${MODULE_DIR}/eaglei-extended-go \
-outputClassified \
${BUILD_DIR}/imports

##############
# core/ero-extended.owl  mesh
##############

owlcl \
-ontologyFiles \
${TRUNK_DIR}/src/isf/ontology \
${MODULE_DIR} \
-jobName \
build-eaglei \
module \
-name \
eaglei-extended-mesh \
-classified \
-directory \
${MODULE_DIR}/eaglei-extended-mesh \
-outputClassified \
${BUILD_DIR}/imports

##############
# core/ero-extended.owl   pato
##############

owlcl \
-ontologyFiles \
${TRUNK_DIR}/src/isf/ontology \
${MODULE_DIR} \
-jobName \
build-eaglei \
module \
-name \
eaglei-extended-pato \
-classified \
-directory \
${MODULE_DIR}/eaglei-extended-pato \
-outputClassified \
${BUILD_DIR}/imports

##############
# core/ero-extended.owl  uberon
##############

owlcl \
-ontologyFiles \
${TRUNK_DIR}/src/isf/ontology \
${MODULE_DIR} \
-jobName \
build-eaglei \
module \
-name \
eaglei-extended-uberon \
-classified \
-directory \
${MODULE_DIR}/eaglei-extended-uberon \
-outputClassified \
${BUILD_DIR}/imports

##############
# core/ero-extended.owl   eagel i app
##############

owlcl \
-ontologyFiles \
${TRUNK_DIR}/src/isf/ontology \
${MODULE_DIR} \
-jobName \
build-eaglei \
module \
-name \
eaglei-app \
-classified \
-directory \
${MODULE_DIR}/eaglei-app \
-outputClassified \
${BUILD_DIR}/application-specific-files

##############
# core/ero-extended.owl   eagel i extended app
##############

owlcl \
-ontologyFiles \
${TRUNK_DIR}/src/isf/ontology \
${MODULE_DIR} \
-jobName \
build-eaglei \
module \
-name \
eaglei-extended-app \
-classified \
-directory \
${MODULE_DIR}/eaglei-extended-app \
-outputClassified \
${BUILD_DIR}/application-specific-files


##############
# core/ero-extended.owl   eagel i app def
##############

owlcl \
-ontologyFiles \
${TRUNK_DIR}/src/isf/ontology \
${MODULE_DIR} \
-jobName \
build-eaglei \
module \
-name \
eaglei-app-def \
-classified \
-directory \
${MODULE_DIR}/eaglei-app-def \
-outputClassified \
${BUILD_DIR}/application-specific-files


##############
# core/ero-extended.owl   eaglei-extended-go-app
##############

owlcl \
-ontologyFiles \
${TRUNK_DIR}/src/isf/ontology \
${MODULE_DIR} \
-jobName \
build-eaglei \
module \
-name \
eaglei-extended-go-app \
-classified \
-directory \
${MODULE_DIR}/eaglei-extended-go-app \
-outputClassified \
${BUILD_DIR}/application-specific-files


##############
# core/ero-extended.owl   eaglei-extended-mesh-app
##############

owlcl \
-ontologyFiles \
${TRUNK_DIR}/src/isf/ontology \
${MODULE_DIR} \
-jobName \
build-eaglei \
module \
-name \
eaglei-extended-mesh-app \
-classified \
-directory \
${MODULE_DIR}/eaglei-extended-mesh-app \
-outputClassified \
${BUILD_DIR}/application-specific-files


##############
# core/ero-extended.owl   eaglei-extended-mp-app
##############

owlcl \
-ontologyFiles \
${TRUNK_DIR}/src/isf/ontology \
${MODULE_DIR} \
-jobName \
build-eaglei \
module \
-name \
eaglei-extended-mp-app \
-classified \
-directory \
${MODULE_DIR}/eaglei-extended-mp-app \
-outputClassified \
${BUILD_DIR}/application-specific-files


##############
# core/ero-extended.owl   eaglei-extended-pato-app
##############

owlcl \
-ontologyFiles \
${TRUNK_DIR}/src/isf/ontology \
${MODULE_DIR} \
-jobName \
build-eaglei \
module \
-name \
eaglei-extended-pato-app \
-classified \
-directory \
${MODULE_DIR}/eaglei-extended-pato-app \
-outputClassified \
${BUILD_DIR}/application-specific-files


##############
# core/ero-extended.owl   eaglei-extended-go-app
##############

owlcl \
-ontologyFiles \
${TRUNK_DIR}/src/isf/ontology \
${MODULE_DIR} \
-jobName \
build-eaglei \
module \
-name \
eaglei-extended-uberon-app \
-classified \
-directory \
${MODULE_DIR}/eaglei-extended-uberon-app \
-outputClassified \
${BUILD_DIR}/application-specific-files

##########
# catalogs
##########

owlcl \
catalog \
-sources \
${BUILD_DIR} \
-target \
${BUILD_DIR}
