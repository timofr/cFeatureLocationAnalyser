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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
		database = new TreeMap<String, List<FunctionData>>(); //UNSURE TreeMap or HashMap. prob TreeMap
	}
	
	public void initDatabase(File file) {
		readDatabase(file == null ? standardDatabaseFile : file);
	} 
	
	@SuppressWarnings("unchecked")
	private Map<String, List<FunctionData>> readDatabase(File databaseFile) {
		
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(databaseFile))) {
			database = (TreeMap<String, List<FunctionData>>) ois.readObject();
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
			
		    Set<String> calledFunctions = new TreeSet<String>();
		    Set<String> ifdefsToEnable = new TreeSet<String>();
		    
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
		    			.forEach(d -> ifdefsToEnable.addAll(d.getpIfdef().stream().map(i -> i.getName()).collect(Collectors.toSet())));
		    	}
		    }
		    
		    //TODO check illegalIfdef implementation
//		    List<String> illegalIfdefs = illegalSingleIfdefStrings.stream().filter(s -> ifdefsToEnable.contains(s)).collect(Collectors.toList());
//		    if(!illegalIfdefs.isEmpty()) {
//		    	System.err.println("Found non-single ifdefs to enable");
//		    	illegalIfdefs.forEach(i -> System.err.println(i));
//		    }	
		    
		    writeStringList(ifdefsToEnable, bw);
		    System.out.println("Ifdef values are saved in " + ifdefFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeStringList(Set<String> sos, BufferedWriter writer) throws IOException {
		if(sos.isEmpty())
			return;
		
		for(String s : sos) {
			writer.write(s);
			writer.newLine();
		}
	}
	
	private boolean test(String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i); 
	        if (!('A'<=c && c<='Z' || c == '_' || 'a'<=c && c<='z' || '0' <= c && c <= '9')) {
	            return false;
	        }
	    }
		return true;
	}
	
	public void printFunctionNames(File file) {//TODO fix this again
		
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			database.entrySet().stream().forEach(e -> e.getValue().forEach(l -> l.getFunction().getIfdef().stream().filter(i -> !test(i.getName())).forEach(i ->
			{
				try {
				bw.write(i.getName());

					bw.newLine();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			})));
//			for(String s : database.keySet()) {
//				bw.write(s);
//				bw.newLine();
//			}
		} catch (IOException e) {
			System.err.println("Failed to print database funtion names into file " + file.getAbsolutePath());
			e.printStackTrace();
		}
			
		
	}
}
