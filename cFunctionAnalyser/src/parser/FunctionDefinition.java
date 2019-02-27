package parser;

import java.util.List;

public class FunctionDefinition {
	private String name;
	private List<Ifdef> ifdef;
	private int start;
	private int end;
	
	public FunctionDefinition(String name, List<Ifdef> ifdef, int start) {
		this.name = name;
		this.ifdef = ifdef;
		this.start = start;
	}

	@Override
	public String toString() {
		return "FunctionDefinitionOccurance [name=" + name + ", ifdef=" + ifdef + ", start=" + start + ", end=" + end
				+ "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Ifdef> getIfdef() {
		return ifdef;
	}

	public void setIfdef(List<Ifdef> ifdef) {
		this.ifdef = ifdef;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}
}
