package parser;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class FunctionData implements Serializable {
	private static final long serialVersionUID = -5625682032759792996L;
	
	private final FunctionDefinition function;
	private final File file;
	private final List<Ifdef> pIfdef;
	private final List<Ifdef> nIfdef;
	
	
	public FunctionData() {
		super();
		function = null;
		file = null;
		pIfdef = null;
		nIfdef = null;
	}

	public FunctionData(FunctionDefinition function, File file, List<Ifdef> pIfdef, List<Ifdef> nIfdef) {
		this.function = function;
		this.file = file;
		this.pIfdef = pIfdef;
		this.nIfdef = nIfdef;
	}
	
	public FunctionDefinition getFunction() {
		return function;
	}
	
	public File getFile() {
		return file;
	}
	
	public List<Ifdef> getpIfdef() {
		return pIfdef;
	}
	
	public List<Ifdef> getnIfdef() {
		return nIfdef;
	}
}

