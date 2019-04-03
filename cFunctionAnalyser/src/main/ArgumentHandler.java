package main;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class ArgumentHandler {
	private static Map<Character, Consumer<PrinterData>> debugHandle = new HashMap<Character, Consumer<PrinterData>>();
	
	static {
		debugHandle.put('f', Printer::printFunctions);
		debugHandle.put('n', Printer::printFunctionNames);
		debugHandle.put('p', Printer::printAnalysedFunctionPositive);
		debugHandle.put('a', Printer::printAnalysedFunctionAll);
		debugHandle.put('o', Printer::printPatternOccurances);
		debugHandle.put('i', Printer::printIfdefs);
	}
	
	public static CommandLine readArgs(String[] args) {

		Options options = new Options();
		
		Option input = new Option("s", "source", true, "source directory/file");
		input.setRequired(false);
		options.addOption(input);
		
		Option database = new Option("d", "database", true, "database file");
		database.setRequired(false);
		options.addOption(database);
		
		Option functions = new Option("f", "functions", true, "function name file");
		functions.setRequired(false);
		options.addOption(functions);
		
		Option ifdef = new Option("i", "ifdef", true, "ifdef file");
		ifdef.setRequired(false);
		options.addOption(ifdef);
		
		Option debug = new Option("D", "debug", true, "debug options (h for help)");
		debug.setRequired(false);
		options.addOption(debug);
		
		Option extensions = new Option("e", "extensions", true, "file extensions (default: 'c', usage e.g.: -e cpp h c)");
		extensions.setRequired(false);
		extensions.setArgs(-2);
		options.addOption(extensions);
		
		Option help = new Option("h", "help", false, "print help");
		help.setRequired(false);
		options.addOption(help);
		
		Option verbose = new Option("v", "verbose", false, "prints which file is currently processed");
		verbose.setRequired(false);
		options.addOption(verbose);
		
		Option print = new Option("p", "print", true, "prints all function names in database into given file");
		print.setRequired(false);
		options.addOption(print);
	
	
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		if(args.length == 0)
			formatter.printHelp("java -jar cFunctionAnalyser [options]", options);
		
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("java -jar cFunctionAnalyser [options]", options);

			System.exit(1);
		}
		
		if(cmd.hasOption("help")) {
			formatter.printHelp("java -jar cFunctionAnalyser [options]", options);
			printDebugHelp();
		}
		
		String debugOptions = cmd.getOptionValue("debug");
		if(debugOptions != null ) {
			if(debugOptions.contains("h")) {
				printDebugHelp();
				System.exit(0);
			}
			
			if(debugOptions.chars().noneMatch(c -> debugHandle.containsKey((char) c))) {
				System.out.println("Wrong debug usage.");
				printDebugHelp();
				System.exit(1);
			}
		}
		
		return cmd;
	}
	
	public static void debug(String debugOptions, PrinterData data) {
		if(debugOptions != null)
			debugOptions.chars().forEach(c -> debugHandle.get((char) c).accept(data));
	}
	
	public static void printDebugHelp() {
		System.out.println("Debug usage: java -jar cFunctionAnalyser -d [a,p,o,n,f,i]");
		System.out.println("-a: show analysed functions with all ifdefs");
		System.out.println("-p: show analysed functions with positive ifdefs");
		System.out.println("-o: show all pattern occurances defined");
		System.out.println("-n: show all functions names defined");
		System.out.println("-f: show all functions");
		System.out.println("-i: show all ifdef directives");
		System.out.println("-d: show all functions in database");
	}
}
