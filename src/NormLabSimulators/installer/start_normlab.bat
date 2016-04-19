@ECHO OFF
TITLE NormLabSimulators

REM Repast Simphony Model Starter
REM By Michael J. North
REM 
REM Please note that the paths given below use
REM a unusual Linux-like notation. This is a
REM unfortunate requirement of the Java Plugin
REM framework application loader.

REM Note the Repast Simphony Directories.
set REPAST_SIMPHONY_ROOT=../NormLabSimulators/lib/repast.simphony/classpath/repast.simphony.runtime_$REPAST_VERSION/
set REPAST_SIMPHONY_BATCH_ROOT=../NormLabSimulators/lib/repast.simphony/classpath/repast.simphony.batch_$REPAST_VERSION/
set REPAST_SIMPHONY_LIB=%REPAST_SIMPHONY_ROOT%lib/

set NORMLAB_ROOT=../NormLabSimulators/
set NORMLAB_LIB=%NORMLAB_ROOT%lib/

set NSM_ROOT=../NormSynthesisMachine/
set NSM_LIB=%NSM_ROOT%lib/

REM load Repast classes and libraries
SET CP=%CP%;%REPAST_SIMPHONY_ROOT%bin
SET CP=%CP%;%REPAST_SIMPHONY_LIB%saf.core.runtime.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%commons-logging-1.1.2.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%javassist-3.17.1-GA.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%jpf.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%jpf-boot.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%log4j-1.2.16.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%xpp3_min-1.1.4c.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%xstream-1.4.7.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%xmlpull-1.1.3.1.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%commons-cli-1.2.jar

REM load Repast batch classes
SET CP=%CP%;%REPAST_SIMPHONY_BATCH_ROOT%bin

REM classes to load NormLab
SET CP=%CP%;%NORMLAB_LIB%normlab-1.0.jar
SET CP=%CP%;%NORMLAB_LIB%BrowserLauncher2-all-1_3.jar
SET CP=%CP%;%NORMLAB_LIB%commons-io-2.4.jar
SET CP=%CP%;%NORMLAB_LIB%opencsv-3.7.jar
SET CP=%CP%;%NORMLAB_LIB%commons-lang3-3.4.jar

REM classes and libraries of the Norm Synthesis Machine 
SET CP=%CP%;%NSM_ROOT%bin
SET CP=%CP%;%NSM_LIB%collections-generic-4.01.jar
SET CP=%CP%;%NSM_LIB%colt-1.2.0.jar
SET CP=%CP%;%NSM_LIB%concurrent-1.3.4.jar
SET CP=%CP%;%NSM_LIB%j3d-core-1.3.1.jar
SET CP=%CP%;%NSM_LIB%jcommon-1.0.14.jar
SET CP=%CP%;%NSM_LIB%jess7.1.2.jar
SET CP=%CP%;%NSM_LIB%jfreechart-1.0.11.jar
SET CP=%CP%;%NSM_LIB%jgrapht-jdk1.6.jar
SET CP=%CP%;%NSM_LIB%jung-3d-2.0.1.jar
SET CP=%CP%;%NSM_LIB%jung-3d-demos-2.0.1.jar
SET CP=%CP%;%NSM_LIB%jung-algorithms-2.0.1.jar
SET CP=%CP%;%NSM_LIB%jung-api-2.0.1.jar
SET CP=%CP%;%NSM_LIB%jung-graph-impl-2.0.1.jar
SET CP=%CP%;%NSM_LIB%jung-io-2.0.1.jar
SET CP=%CP%;%NSM_LIB%jung-jai-2.0.1.jar
SET CP=%CP%;%NSM_LIB%jung-jai-samples-2.0.1.jar
SET CP=%CP%;%NSM_LIB%jung-samples-2.0.1.jar
SET CP=%CP%;%NSM_LIB%jung-visualization-2.0.1.jar
SET CP=%CP%;%NSM_LIB%stax-api-1.0.1.jar
SET CP=%CP%;%NSM_LIB%vecmath-1.3.1.jar
SET CP=%CP%;%NSM_LIB%wstx-asl-3.2.6.jar

SET CP=%CP%;../groovylib/$Groovy_All_Jar

REM Change to the Default Repast Simphony Directory
CD NormLabSimulators

REM Start the Model
START javaw -Xss10M -Xmx400M -cp %CP% es.csic.iiia.normlab.launcher.NormLabGUILauncher
