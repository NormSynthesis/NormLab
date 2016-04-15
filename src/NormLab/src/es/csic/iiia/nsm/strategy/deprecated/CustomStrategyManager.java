package es.csic.iiia.nsm.strategy.deprecated;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 *
 */
public class CustomStrategyManager {

	private static JavaCompiler compiler;
	private static DiagnosticCollector<JavaFileObject> diagnostics;
	private static StandardJavaFileManager fileManager;

	/**
	 * 
	 * @param strategyFile
	 * @param strategyProjectDir
	 * @throws Exception
	 */
	public static void compile(File strategyFile, File strategyProjectDir)
			throws Exception {

		compiler = ToolProvider.getSystemJavaCompiler();
		diagnostics =	new DiagnosticCollector<JavaFileObject>();
		fileManager = compiler.getStandardFileManager(diagnostics, null, null);

		if (strategyFile.getParentFile().exists() || 
				strategyFile.getParentFile().mkdirs()) {

			/* Prepare paths for compilation */
			String normLabBinDir = System.getProperty("user.dir") + 
					File.separator + "bin";
			String classpath = System.getProperty("java.class.path") +
					File.pathSeparator + normLabBinDir;

			/* Set up the class path that the compiler will use */
			List<String> options = new ArrayList<String>();
			options.add("-classpath");
			options.add(classpath);

			File strategySrcDir = new File(strategyProjectDir + File.separator + "src");
			
			/* Get all Java files in the directory and prepare compilation unit */
			List<File> javaFiles = getFilesWithExtension(strategySrcDir, "java");
			Iterable<? extends JavaFileObject> compilationUnit =
					fileManager.getJavaFileObjectsFromFiles(javaFiles);

			JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager,
					diagnostics, options, null, compilationUnit);

			/* If compilation was not successful... */
			if (!task.call()) {
				for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
					throw new Exception(d.toString());
				}
			}
			fileManager.close();
		}
	}

	/**
	 * Moves a compiled norm synthesis strategy to the
	 * NormLabSimulators bin folder 
	 * 
	 * @param strategyName the strategy to move
	 */
	public static void export(File strategyProjectDir)
			throws Exception {

		String destPath = System.getProperty("user.dir") + "/bin/";
		List<File> classes = getFilesWithExtension(strategyProjectDir, "class");
		
		for(File clazz : classes) {
			File destFile = new File(destPath + clazz.getName());
			FileUtils.copyFile(clazz, destFile);	
			
			/* TODO: Move each class to its corresponding folder inside the bin folder */
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private static List<File> getFilesWithExtension(File path, String extension) {
		Collection<File> files = new ArrayList<File>();
		files = FileUtils.listFiles(path, new String[]{extension}, true);

		return new ArrayList<File>(files);
	}
}