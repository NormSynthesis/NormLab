#!/bin/bash
# Repast Simphony Model Starter
# By Michael J. North and Jonathan Ozik
# 11/12/2007
# Note the Repast Simphony Directories.

PWD="${0%/*}"
cd $PWD

REPAST_SIMPHONY_ROOT=$PWD/NormLabSimulators/lib/repast.simphony/classpath/repast.simphony.runtime_$REPAST_VERSION
REPAST_SIMPHONY_BATCH_ROOT=$PWD/NormLabSimulators/lib/repast.simphony/classpath/repast.simphony.batch_$REPAST_VERSION
REPAST_SIMPHONY_LIB=$REPAST_SIMPHONY_ROOT/lib

NORMLAB_ROOT=$PWD/NormLabSimulators
NORMLAB_LIB=$NORMLAB_ROOT/lib

NSM_ROOT=$PWD/NormSynthesisMachine
NSM_LIB=$NSM_ROOT/lib

# Define the Core Repast Simphony Directories and JARs
CP=$CP:$REPAST_SIMPHONY_ROOT/bin
CP=$CP:$REPAST_SIMPHONY_LIB/saf.core.runtime.jar
CP=$CP:$REPAST_SIMPHONY_LIB/commons-logging-1.1.2.jar
CP=$CP:$REPAST_SIMPHONY_LIB/javassist-3.17.1-GA.jar
CP=$CP:$REPAST_SIMPHONY_LIB/jpf.jar
CP=$CP:$REPAST_SIMPHONY_LIB/jpf-boot.jar
CP=$CP:$REPAST_SIMPHONY_LIB/log4j-1.2.16.jar
CP=$CP:$REPAST_SIMPHONY_LIB/xpp3_min-1.1.4c.jar
CP=$CP:$REPAST_SIMPHONY_LIB/xstream-1.4.7.jar
CP=$CP:$REPAST_SIMPHONY_LIB/xmlpull-1.1.3.1.jar
CP=$CP:$REPAST_SIMPHONY_LIB/commons-cli-1.2.jar

# Load Repast batch libraries
CP=$CP:$REPAST_SIMPHONY_BATCH_ROOT/bin

# Load libraries of the NormLab launcher
CP=$CP:$NORMLAB_LIB/normlab-1.0.jar
CP=$CP:$NORMLAB_LIB/BrowserLauncher2-all-1_3.jar
CP=$CP:$NORMLAB_LIB/commons-io-2.4.jar
CP=$CP:$NORMLAB_LIB/opencsv-3.7.jar
CP=$CP:$NORMLAB_LIB/commons-lang3-3.4.jar

CP=$CP:$NSM_ROOT/bin
CP=$CP:$NSM_LIB/collections-generic-4.01.jar
CP=$CP:$NSM_LIB/colt-1.2.0.jar
CP=$CP:$NSM_LIB/concurrent-1.3.4.jar
CP=$CP:$NSM_LIB/j3d-core-1.3.1.jar
CP=$CP:$NSM_LIB/jcommon-1.0.14.jar
CP=$CP:$NSM_LIB/jess7.1.2.jar
CP=$CP:$NSM_LIB/jfreechart-1.0.11.jar
CP=$CP:$NSM_LIB/jgrapht-jdk1.6.jar
CP=$CP:$NSM_LIB/jung-graph-impl-2.0.1.jar
CP=$CP:$NSM_LIB/stax-api-1.0.1.jar
CP=$CP:$NSM_LIB/vecmath-1.3.1.jar
CP=$CP:$NSM_LIB/wstx-asl-3.2.6.jar

CP=$CP:$PWD/groovylib/$Groovy_All_Jar

# Change to the Default Repast Simphony Directory
cd NormLabSimulators

# Start the Model
java -Xss10M -Xmx400M -cp $CP es.csic.iiia.normlab.launcher.NormLabGUILauncher
