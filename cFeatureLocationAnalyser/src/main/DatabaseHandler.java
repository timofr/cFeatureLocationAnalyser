package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import lexer.Lexer;
import lexer.LexerException;
import lexer.Token;
import parser.FunctionData;
import parser.expressions.Expression;
import parser.expressions.ExpressionParser;
import parser.expressions.ExpressionParserException;

public class DatabaseHandler {
	private final File standardDatabaseFile = new File("cfa_database");
	private final File standardOutputFile = new File("cfa_output");
	private Map<String, List<FunctionData>> analysedFunctions = null;
	private Map<String, Set<String>> transitiveDefine = null;
	private Map<String, Set<String>> transitiveUndefine = null;
	
	private static DatabaseHandler instance;
	
	private DatabaseHandler() {}
	
	public static DatabaseHandler getInstance() {
		return instance == null? instance = new DatabaseHandler() : instance;
	}
	
	public Map<String, List<FunctionData>> getAnalysedFunctions() {
		return analysedFunctions;
	}
	
	public Map<String, Set<String>> getTransitiveDefine() {
		return transitiveDefine;
	}
	public Map<String, Set<String>> getTransitiveUndefine() {
		return transitiveUndefine;
	}
	
	public void initDatabase() {
		analysedFunctions = new TreeMap<String, List<FunctionData>>(); //UNSURE TreeMap or HashMap. prob TreeMap
		transitiveDefine = new TreeMap<String, Set<String>>();
		transitiveUndefine = new TreeMap<String, Set<String>>();
	}
	
	public void initDatabase(File file) {
		readDatabase(file == null ? standardDatabaseFile : file);
	} 
	
	@SuppressWarnings("unchecked")
	private void readDatabase(File databaseFile) {
		
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(databaseFile))) {
			analysedFunctions = (TreeMap<String, List<FunctionData>>) ois.readObject();
			transitiveDefine = (Map<String, Set<String>>) ois.readObject();
			transitiveUndefine = (Map<String, Set<String>>) ois.readObject();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
		}
		System.out.println("Deserialized " + Main.name + " database is loaded from " + databaseFile.getAbsolutePath());
	}

	public void writeDatabase() {
		writeDatabase(standardDatabaseFile);
	}
	
	public void writeDatabase(File databaseFile) {
		if(databaseFile == null)
			databaseFile = standardDatabaseFile;
		
		if(databaseFile.exists()) {
			try {
				Files.copy(databaseFile.toPath(), new File(databaseFile.getAbsolutePath() + ".bak").toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(databaseFile))) {
			oos.writeObject(analysedFunctions);
			oos.writeObject(transitiveDefine);
			oos.writeObject(transitiveUndefine);
			System.out.println("Serialized cFunctionAnalyser database is saved in " + databaseFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void produceIfdefFile(File calledFunctionsFile) {
		produceIfdefFile(calledFunctionsFile, standardOutputFile);
	}
	
	public void produceIfdefFile(File calledFunctionsFile, File outputFile) {
		if (outputFile == null)
			outputFile = standardOutputFile;

		Set<String> calledFunctions = new HashSet<String>();
		Set<String> ifdefsToEnable = new TreeSet<String>();

		fileContentIntoSet(calledFunctionsFile, calledFunctions);

		for (String f : calledFunctions) {
			List<FunctionData> functionDataList = analysedFunctions.get(f);
			if (functionDataList == null) {
				System.err.println("Function " + f + " not found in database");
			} else {
				functionDataList.stream()
						// .filter(d -> d.getnIfdef().isEmpty())
						.forEach(d -> d.getpIfdef().stream().map(i -> i.getName())
								.forEach(i -> addToSet(i, ifdefsToEnable, true)));
			}
		}

		writeStringSet(outputFile, ifdefsToEnable);
		System.out.println("Macros are saved in " + outputFile.getAbsolutePath());

	}
	
	private void addToSet(String expression, Set<String> set, boolean onlyPositive) {
		if(isIdentifier(expression)) {
			set.add(expression);
			return;
		}
		
		if(onlyPositive)
			set.addAll(ExpressionParser.positiveIdentifiers(expression));
		else
			set.addAll(ExpressionParser.getIdentifier(expression));
	}
	

	
	private boolean isIdentifier(String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i); 
	        if (!('A'<=c && c<='Z' || c == '_' || 'a'<=c && c<='z' || '0' <= c && c <= '9')) {
	            return false;
	        }
	    }
		return true;
	}
	
	public void printAllFunctionNames(File outputFile) {
		if (outputFile == null)
			outputFile = standardOutputFile;
		
		writeStringSet(outputFile, analysedFunctions.keySet());	
	}
	
	public void printAllIfdefs(File outputFile) {
		if (outputFile == null)
			outputFile = standardOutputFile;
		
		Set<String> macros = new TreeSet<String>();
		for(Entry<String, List<FunctionData>> entry : analysedFunctions.entrySet()) {
			for(FunctionData data : entry.getValue()) {
				data.getpIfdef().forEach(i -> macros.addAll(ExpressionParser.getIdentifier(i.getName())));
				data.getnIfdef().forEach(i -> macros.addAll(ExpressionParser.getIdentifier(i.getName())));
			}
		}
		writeStringSet(outputFile, macros);	
	}
	
	private void addFunctionDataMacrosToSet(FunctionData data, Set<String> set) {
		data.getpIfdef().forEach(i -> set.addAll(ExpressionParser.getIdentifier(i.getName())));
		data.getnIfdef().forEach(i -> set.addAll(ExpressionParser.getIdentifier(i.getName())));
	}
	
	public void printFunctionsDependingOnIfdefs(File inputFile, File outputFile) {
		throw new RuntimeException("Currently not finished");
	}
	
	public void printIfdefsDependingOnFunctions(File inputFile, File outputFile) {
		Set<String> calledFunctions = new HashSet<String>();
		Set<String> macros = new TreeSet<String>();


		fileContentIntoSet(inputFile, calledFunctions);

		for (String f : calledFunctions) {
			List<FunctionData> functionDataList = analysedFunctions.get(f);
			if (functionDataList == null) {
				System.err.println("Function " + f + " not found in database");
			} else {
				functionDataList.stream()
						// .filter(d -> d.getnIfdef().isEmpty())
						.forEach(d -> d.getpIfdef().stream().map(i -> i.getName())
								.forEach(i -> addToSet(i, macros, false)));
			}
		}
		
		writeStringSet(outputFile, macros);

	}
	

	public void printDirectivesDependingOnDirectives(File inputFile, File outputFile) {
		Set<String> inputDirectives = new HashSet<String>();
		Set<String> outputDirectives  = new TreeSet<String>();
		fileContentIntoSet(inputFile, inputDirectives);
		
//		System.out.println(transitiveDefine);
//		System.out.println(transitiveUndefine);
		boolean affected = false;
		int counter = 0;
		for(String s : inputDirectives) {
			affected = false;
			Set<String> temp = null;
			if((temp = transitiveDefine.get(s)) != null) {
				temp.forEach(i -> outputDirectives.addAll(ExpressionParser.getIdentifier(i)));
				affected = true;
			}
			if((temp = transitiveUndefine.get(s)) != null) {
				temp.forEach(i -> outputDirectives.addAll(ExpressionParser.getIdentifier(i)));
				affected = true;
			}
			if(affected)
				counter++;
		}
		System.out.println(counter + " of " + inputDirectives.size() + " macors are affected by " + outputDirectives.size() + " macros");
		
		
		writeStringSet(outputFile, outputDirectives);
	}
	
	private void writeStringSet(File file, Set<String> set) {
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			for(String s : set) {
				bw.write(s);
				bw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void fileContentIntoSet(File file, Set<String> set) {
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = br.readLine();

		    while (line != null) {
		    	set.add(line);
		        line = br.readLine();
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
