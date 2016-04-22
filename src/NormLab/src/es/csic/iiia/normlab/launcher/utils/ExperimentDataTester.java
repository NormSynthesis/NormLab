package es.csic.iiia.normlab.launcher.utils;

import java.io.IOException;

public class ExperimentDataTester {

	public static void main(String[] args) {
		ExperimentDataProcessor p = new ExperimentDataProcessor();
		
		try {
	    p.computeAverages("experiments/performed/MyExperiment/data",
	    		"TrafficDataOutput.dat");
    }
		catch (IOException e) {
	    e.printStackTrace();
    }
	}
}
