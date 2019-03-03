package main;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lexer.Lexer;
import parser.Parser;

public class Main {
	public static String path = null;
	
	private interface Invoker {
		public void invoke();
	}
	
	public static void main(String[] args) {
		if(args.length == 0) {
			System.err.println("Not enough arguments. Type 'java -jar cFunctionAnalyser -h' for more info.");
			return;
		}
		
		Set<Character> possibleOptions = Stream.of('h', 'f', 'a', 'i', 'o')
		         .collect(Collectors.toCollection(HashSet::new));
		
		StringBuilder options = new StringBuilder();
		for(int i = 0; i < args.length; i++) {
		    String a = args[i];

		    if(a.charAt(0) == '-') {
		    	String subArg = a.substring(1);
		    	if(subArg.chars().noneMatch(c -> possibleOptions.contains((char) c))) {
		    		System.err.println("Illegal parameter. Type 'java -jar cFunctionAnalyser -h' for more info.");
			        return;
		    	}
		        options.append(subArg);
		    }
		    else if(path == null) {
		        path = a;
		    }
		    else {
		    	System.err.println("Illegal parameter usage. Type 'java -jar cFunctionAnalyser -h' for more info.");
		        return;
		    }
		}
		
		String stringOptions = options.toString();
		
		if(stringOptions.contains("h")) {
			printHelp();
			return;
		}
		
		System.out.println("Starting to process file " + path);
		
		try(BufferedReader br = new BufferedReader(new FileReader(path))) {
			StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append('\n');
		        line = br.readLine();
		    }
		    sb.append(Lexer.EOF);
		    String everything = sb.toString();
		    
		    Parser parser = new Parser(new Lexer(everything));
		    Printer printer = new Printer(parser);
		    
		    parser.analyse();

			Map<Character, Invoker> argsHandle = new HashMap<Character, Invoker>();
			argsHandle.put('f', printer::printFunctions);
			argsHandle.put('n', printer::printFunctionNames);
			argsHandle.put('p', printer::printAnalysedFunctionPositive);
			argsHandle.put('a', printer::printAnalysedFunctionAll);
			argsHandle.put('o', printer::printPatternOccurances);
			argsHandle.put('i', printer::printIfdefs);
			if(options.length() == 0) 
				printer.printAnalysedFunctionAll();
			else
				stringOptions.chars().forEach(c -> argsHandle.get((char) c).invoke());
		}
		catch(Exception e) {
			System.err.println("Failed in file " + path);
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
	private static void printHelp() {
		System.out.println("Usage: java -jar cFunctionAnalyser [options] path");
		System.out.println("-h: show help");
		System.out.println("-a: (default) show analysed functions with all ifdefs");
		System.out.println("-p: show analysed functions with positive ifdefs");
		System.out.println("-o: show all pattern occurances defined");
		System.out.println("-n: show all functions names defined");
		System.out.println("-f: show all functions");
		System.out.println("-i: show all ifdef directives");
	}
}
