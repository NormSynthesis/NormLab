#!/bin/bash

#------------------------------------------
# Settings for the experiments
#------------------------------------------

outputDataFile="TrafficDataOutput.dat"
totalNormsFile="TrafficNormsTotal.dat"
finalNormsFile="TrafficNormsFinal.dat"
resultsFile="Results"
resultsDir="results"

InitialNumExec=1
NumExecsForExperiment=500
UseSequentialRandomSeeds=false

#TrafficSim
SimMap=1
SimNewCarsFreq=2
SimNumCarsToEmit=3
SimRandomSeed=0l
SimMaxTicks=35000l
SimUseSemaphores=false
SimOnlySemaphores=false
SimNormViolationRate=0.3

#IRON
NormsGenEffThreshold=0.0
NormsGenNecThreshold=0.0
NormsSpecEffThreshold=0.2
NormsSpecNecThreshold=0.2
NormsUtilityWindowSize=50
NormsDefaultUtility=0.5
NumTicksToConverge=4000l
NormsWeightCol=1
NormsWeightNoCol=1
NormsWeightFluidTraffic=1
