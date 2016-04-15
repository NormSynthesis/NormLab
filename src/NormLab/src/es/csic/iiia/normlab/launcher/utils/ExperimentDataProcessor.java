package es.csic.iiia.normlab.launcher.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;

/**
 * 
 * @author Javi
 *
 */
public class ExperimentDataProcessor {

	/**
	 * 
	 * @author Javi
	 *
	 */
	private enum Operation {
		Average, Median;
	}
	
	private List<String[]> avgBody;
	private char sepChar = ';';
	
	/**
	 * 
	 * @return
	 * @throws IOException 
	 */
	public File computeAverages(String path, final String filePattern) 
			throws IOException {

		/* Create average file and readers and writer for data files */
		File avgFile = this.loadAvgFile(path, filePattern);

		/* Retrieve data files with the specified pattern */
		File[] dataFiles = this.retrieveFiles(path, filePattern);

		/* Process each retrieved file by adding its values to the average */
		int numProcessedFiles = 0;
		for(File dataFile : dataFiles) {
			this.add(dataFile, ++numProcessedFiles, Operation.Average);
		}
		
		/* Write avg data to avg file */
		this.writeAvgFile(avgFile);

		return avgFile;
	}

	/**
	 * 
	 * @param path
	 * @param filePattern
	 * @throws IOException 
	 */
	private File loadAvgFile(String path, final String filePattern)
			throws IOException {
		
		/* Create avg file */
		File avgFile = new File(path + File.separator + filePattern + ".avg");
		if(avgFile.exists()) {
			avgFile.delete();
		}
		avgFile.createNewFile();

		/* Create reader and writer for the created avg file */
		CSVReader avgReader = new CSVReader(new FileReader(avgFile), sepChar);
		avgBody = avgReader.readAll();
		avgReader.close();
		
		return avgFile;
  }

	/**
	 * 
	 * @param dataReader
	 * @param avgWriter
	 * @throws IOException
	 */
	private void add(File dataFile, int numProcessedFiles, Operation operation) 
					throws IOException {

		CSVReader dataReader = new CSVReader(new FileReader(dataFile), sepChar);
		String[] dataLine, avgLine = null;
		int line = 0;
		
		while ((dataLine = dataReader.readNext()) != null) {
			
			/* Create new line if it does not exist yet */
			if(avgBody.size() <= line) {
				avgBody.add(dataLine);
			}

			/* If line exists, update it (avoiding header, i.e., line 0) */
			else if(line > 0) {
				if(operation == Operation.Average) {
					avgLine = avgBody.get(line);
					String[] newAvgLine = this.addAverage(numProcessedFiles, 
							dataLine, avgLine);
					
					avgBody.set(line, newAvgLine);
				}
			}
			line++;
		}
		dataReader.close();
	}

	/**
	 * 
	 * @param numProcessedFiles
	 * @param dataLine
	 * @param avgLine
	 */
	private String[] addAverage(int numProcessedFiles, String[] dataLine, 
			String[] avgLine) {
		
		String[] newAvgLine = new String[dataLine.length]; 
	  
		/* If it's the file to be processed, just copy values */
		if(numProcessedFiles == 1) {
			newAvgLine = dataLine;
		}
		else {
			/* Compute averages */
			for(int i=0; i<dataLine.length; i++) {
				double dataNum = Double.valueOf(dataLine[i]);
				double avgNum = Double.valueOf(avgLine[i]);
				
				double newAvgNum = ((numProcessedFiles-1) * dataNum + avgNum) / 
						numProcessedFiles;
				newAvgNum = Math.floor(newAvgNum * 100) / 100;
				newAvgLine[i] = String.valueOf(newAvgNum);
			}
		}
		return newAvgLine;
  }

	/**
	 * Retrieves files with the given name pattern in the given path
	 * 
	 * @param path
	 * @param filePattern
	 */
	private File[] retrieveFiles(String path, final String filePattern) {
		File outputDir = new File(path);
		File[] files = outputDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.contains(filePattern);
			}
		});
		return files;
	}

	/**
	 * @throws IOException 
	 * 
	 */
	private void writeAvgFile(File avgFile) throws IOException {
		BufferedWriter avgWriter = new BufferedWriter(new FileWriter(avgFile));
		for(String[] line : avgBody) {
			String joined = String.join(String.valueOf(sepChar), line);
			avgWriter.write(joined + "\n");
		}
		avgWriter.close();
  }
}

