package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.StyledEditorKit.BoldAction;

public class FunctionAnalyser {
	private List<FunctionDefinition> functions;
	private List<Ifdef> ifdefs;
	
	private Map<FunctionDefinition, List<Ifdef>> functionIfdefMap = new HashMap<FunctionDefinition, List<Ifdef>>();

	public FunctionAnalyser(List<FunctionDefinition> functions, List<Ifdef> ifdefs) {
		this.functions = functions;
		this.ifdefs = ifdefs;
	}
	
	public Map<FunctionDefinition, List<Ifdef>> analyse() {
		functions.stream()
			.filter(f -> f.getIfdef().stream().map(id -> id.isIfdef()).reduce(true, Boolean::logicalAnd))
			.forEach(f -> functionIfdefMap.put(f, new ArrayList<Ifdef>()));
		
		for(Ifdef i : ifdefs) {
			for(FunctionDefinition f : functions) {
				if(i.getElseLine() == -1) {
					if((f.getStart() > i.getStartLine()) && (f.getEnd() < i.getEndLine()))
						functionIfdefMap.get(f).add(i);
				} else {
					if((f.getStart() > i.getStartLine()) && (f.getEnd() < i.getElseLine()))
						functionIfdefMap.get(f).add(i);
				}
			}
		}
		
		return functionIfdefMap;
	}
	
	public void printFunctionsIfdefs() {
		functionIfdefMap.entrySet().stream()
			.forEach(e -> System.out.println(e.getKey().getName() + " " + ifdefListToString(e.getValue())));
	}
	
	private String ifdefListToString(List<Ifdef> loi) {
		if(loi.size() == 0)
			return "";
		
		StringBuilder builder = new StringBuilder();
		loi.stream().forEach(i -> builder.append(i.getName()).append(", "));
		return builder.substring(0, builder.length() - 2);
	}
}
