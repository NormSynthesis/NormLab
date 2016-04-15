package es.csic.iiia.normlab.launcher.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 *
 */
public class StreamLogger extends Thread {
	
	private InputStream is;
	private File logFile;
	private PrintWriter output;
	private String log; 

	/**
	 * 
	 * @param is
	 * @param type
	 */
	public StreamLogger(InputStream is, File logFile) {
		this.is = is;
		this.logFile = logFile;
	}

	/**
	 * 
	 */
	@Override
	public void run() {
		try {
			
			/* Create console reader */
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			
			/* Create Log file writer */
			output = new PrintWriter(logFile);
			
			/* Write console output into log file */
			String line = null;
			while ((line = br.readLine()) != null) {
				output.println(line);
			}
		}
		
		/* Print exception */
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		/* Finally close output */
		output.close();
	}
}