package main;

import java.io.File;
import java.util.List;
import java.util.Map;

import parser.FunctionData;
import parser.FunctionDefinition;
import parser.Ifdef;
import parser.Parser;
import parser.PatternOccurance;

public class PrinterData {
	private File file;
	private List<FunctionDefinition> functions;
	private List<Ifdef> ifdefs;
	private Map<String, List<FunctionData>> analysedFunctions;
	private List<PatternOccurance> occurances;
	
	public PrinterData(File file, List<FunctionDefinition> functions, List<Ifdef> ifdefs,
			Map<String, List<FunctionData>> analysedFunctions, List<PatternOccurance> occurances) {
		this.file = file;
		this.functions = functions;
		this.ifdefs = ifdefs;
		this.analysedFunctions = analysedFunctions;
		this.occurances = occurances;
	}
	
	public PrinterData(Parser parser) {
		this.file = parser.getFile();
		this.functions = parser.getFunctions();
		this.ifdefs = parser.getIfdefs();
		this.analysedFunctions = parser.getAnalysedFunctions();
		this.occurances = parser.getOccurances();
	}

	public File getFile() {
		return file;
	}
	
	public List<FunctionDefinition> getFunctions() {
		return functions;
	}

	public List<Ifdef> getIfdefs() {
		return ifdefs;
	}

	public Map<String, List<FunctionData>> getAnalysedFunctions() {
		return analysedFunctions;
	}

	public List<PatternOccurance> getOccurances() {
		return occurances;
	}
	
	
}