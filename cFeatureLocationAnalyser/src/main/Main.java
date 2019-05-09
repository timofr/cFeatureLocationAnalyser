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
	public static final String name = "cFeatureLocationAnalyser";
	
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
		
		filename = cmd.getOptionValue("input");
		File input = null;
		if (filename != null) {
			input = new File(filename);
		}
		
		filename = cmd.getOptionValue("output");
		File output = null;
		if (filename != null) {
			output = new File(filename);
		}
		
		String print = cmd.getOptionValue("print");
		
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
				PrinterData printerData = FeatureLocationAnalyser.anaylse(f);
				if(printerData != null)
					ArgumentHandler.debug(cmd.getOptionValue("debug"), printerData);
				
				//System.gc();
			}
			databaseHandler.writeDatabase(database);
		}
		else if(print != null) {
			databaseHandler.initDatabase(database);
		}
		
		if(print != null) {
			if(print.equals("o")) {
				if(input == null ) {
					System.out.println("Option o requires an input file");
					System.exit(0);
				}
				databaseHandler.produceIfdefFile(input, output);
			}
			else if(print.equals("d")) {
				databaseHandler.printAllIfdefs(output);
			}
			else if(print.equals("f")) {
				databaseHandler.printAllFunctionNames(output);
			}
			else if(print.equals("daf")) {
				if(input == null ) {
					System.out.println("Option daf requires an input file");
					System.exit(0);
				}
				databaseHandler.printIfdefsDependingOnFunctions(input, output);
			}
			else if(print.equals("fdd")) {
				if(input == null ) {
					System.out.println("Option fdd requires an input file");
					System.exit(0);
				}
				databaseHandler.printFunctionsDependingOnIfdefs(input, output);
			}
			else if(print.equals("dad")) {
				if(input == null ) {
					System.out.println("Option fdd requires an input file");
					System.exit(0);
				}
				databaseHandler.printDirectivesDependingOnDirectives(input, output);
			}
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
