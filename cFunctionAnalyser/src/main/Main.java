package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;



public class Main {
	public static void main(String[] args) {
		Set<String> extensionsSet;
		CommandLine cmd = ArgumentHandler.readArgs(args);
		String filename;
		
		filename = cmd.getOptionValue("source");
		File source = null;
		if (filename != null) {
			source = new File(filename);
		}
		
		filename = cmd.getOptionValue("database");
		File database = null;
		if (filename != null) {
			database = new File(filename);
		}
		
		filename = cmd.getOptionValue("functions");
		File functions = null;
		if (filename != null) {
			functions = new File(filename);
		}
		
		filename = cmd.getOptionValue("ifdef");
		File ifdef = null;
		if (filename != null) {
			ifdef = new File(filename);
		}
		
		String[] extensions = cmd.getOptionValues("extensions");
		boolean verbose = cmd.hasOption("verbose");
		
		DatabaseHandler databaseHandler = DatabaseHandler.getInstance();
		
		if(extensions == null ) {
			extensionsSet = new HashSet<String>();
			extensionsSet.add(".c");
		}
		else {
			extensionsSet = Arrays.stream(extensions).map(s -> "." + s).collect(Collectors.toSet());
		}
		
		if (source != null) {
			databaseHandler.initDatabase();
			List<File> files = new ArrayList<File>();
			
			if(source.isDirectory()) 
				listf(source, files, extensionsSet);
			
			 else if(source.isFile()) 
				files.add(source);
			
			
			for(File f : files) {
				if(verbose)
					System.out.println("Processing " + f.getAbsolutePath());
				PrinterData printerData = FunctionAnalyser.anaylse(f);
				if(printerData != null)
					ArgumentHandler.debug(cmd.getOptionValue("debug"), printerData);
				
				System.gc();
			}
			databaseHandler.writeDatabase(database);
		}
		else {
			databaseHandler.initDatabase(database);
		}
		
		if(functions != null) {
			databaseHandler.produceIfdefFile(functions, ifdef);
		}
	}
	
	private static void listf(File directory, List<File> files, Set<String> extensions) {
		// Get all files from a directory.
		File[] fList = directory.listFiles();
		if (fList != null) {
			Arrays.stream(fList).filter(f -> f.isDirectory()).sorted().forEach(f -> listf(f, files, extensions));
			Arrays.stream(fList).filter(f -> f.isFile() && isFileInExtensionset(extensions, f.getName())).sorted().forEach(f -> files.add(f));
			
			
//			for (File file : fList) {
//				if (file.isFile() && isFileInExtensionset(extensions, file.getName()))
//					files.add(file);
//				
//				else if (file.isDirectory())
//					listf(file, files, extensions);
//			}
		}
	}
	
	private static boolean isFileInExtensionset(Set<String> extensions, String name) {
		return extensions.stream().anyMatch(s -> name.endsWith(s));
	}
}
