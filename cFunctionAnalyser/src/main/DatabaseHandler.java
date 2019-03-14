package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import parser.FunctionData;

public class DatabaseHandler {
	private final File standardDatabaseFile = new File("cfa_database");
	private final File standardIfdefFile = new File("cfa_ifdefs");
	private Map<String, List<FunctionData>> database = null;
	
	private static final List<String> illegalSingleIfdefStrings = Arrays.asList(" ", "(", ")", "|", "&");
	
	private static DatabaseHandler instance;
	
	private DatabaseHandler() {}
	
	public static DatabaseHandler getInstance() {
		return instance == null? instance = new DatabaseHandler() : instance;
	}
	
	public Map<String, List<FunctionData>> getDatabase() {
		return database;
	}
	
	public void initDatabase() {
		database = new HashMap<String, List<FunctionData>>();
	}
	
	public void initDatabase(File file) {
		if(file == null) {
			initDatabase();
			return;
		}
		readDatabase(file);
	} 
	
	@SuppressWarnings("unchecked")
	private Map<String, List<FunctionData>> readDatabase(File databaseFile) {
		
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(databaseFile))) {
			database = (HashMap<String, List<FunctionData>>) ois.readObject();
			ois.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
		}
		System.out.println("Deserialized cFunctionAnalyser database is loaded from " + databaseFile.getAbsolutePath());
		return database;
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
			oos.writeObject(database);
			System.out.println("Serialized cFunctionAnalyser database is saved in " + databaseFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void produceIfdefFile(File calledFunctionsFile) {
		produceIfdefFile(calledFunctionsFile, standardIfdefFile);
	}
	
	public void produceIfdefFile(File calledFunctionsFile, File ifdefFile) {
		if(ifdefFile == null)
			ifdefFile = standardIfdefFile;
		
		try(BufferedReader br = new BufferedReader(new FileReader(calledFunctionsFile));
				BufferedWriter bw = new BufferedWriter(new FileWriter(ifdefFile))) {
			
		    List<String> calledFunctions = new ArrayList<String>();
		    List<String> ifdefsToEnable = new ArrayList<String>();
		    
		    String line = br.readLine();

		    while (line != null) {
		    	calledFunctions.add(line);
		        line = br.readLine();
		    }
		    
		    for(String f : calledFunctions) {
		    	List<FunctionData> functionDataList = database.get(f);
		    	if (functionDataList == null) {
					System.err.println("Function " + f + " not found in database");
				}
		    	else {
		    		functionDataList.stream()
		    			//.filter(d -> d.getnIfdef().isEmpty())
		    			.forEach(d -> ifdefsToEnable.addAll(d.getpIfdef().stream().map(i -> i.getName()).collect(Collectors.toList())));
		    	}
		    }
		    
		    List<String> illegalIfdefs = illegalSingleIfdefStrings.stream().filter(s -> ifdefsToEnable.contains(s)).collect(Collectors.toList());
		    if(!illegalIfdefs.isEmpty()) {
		    	System.err.println("Found non-single ifdefs to enable");
		    	illegalIfdefs.forEach(i -> System.err.println(i));
		    }	
		    
		    writeStringList(ifdefsToEnable, bw);
		    System.out.println("Ifdef values are saved in " + ifdefFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeStringList(List<String> los, BufferedWriter writer) throws IOException {
		if(los.isEmpty())
			return;
		
		for(String s : los) {
			writer.write(s);
			writer.newLine();
		}
	}
}
