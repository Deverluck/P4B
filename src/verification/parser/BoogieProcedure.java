package verification.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Stack;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

public class BoogieProcedure {
	String name;
	HashSet<BoogieProcedure> parents;  //callers
	HashSet<String> childrenNames;
	HashSet<String> modifies;
	LinkedHashMap<String, String> localVariables;
	String declare;
	String body;
	BoogieBlock preBlock;
	BoogieBlock mainBlock;
	boolean implemented;
	
	// for analyze assert statements
	BoolExpr preCondition;     // (disjunction) the procedure may be called by different execution path
	BoolExpr postCondition;
	Stack<BoolExpr> conditions; // (conjunction) a stack updating with if statements and switch statements
//	ArrayList<BoogieAssertStatement> assertStatements;
	
	private BoogieProcedure() {
		parents = new HashSet<>();
		childrenNames = new HashSet<>();
		modifies = new HashSet<>();
		localVariables = new LinkedHashMap<String, String>();
		preBlock = new BoogieBlock();
		mainBlock = new BoogieBlock();
		implemented = true;
		
		preCondition = null;
		conditions = new Stack<>();
//		assertStatements = new ArrayList<>();
	}
	public BoogieProcedure(String name, String declare, String body) {
		this();
		this.name = name;
		this.declare = declare;
		this.body = body;
	}
	public BoogieProcedure(String name) {
		this(name, "\nprocedure "+name+"()\n", "");
	}
	void initPreCondition(Context ctx) {
		
	}
	public void updateModifies(String var) {
		modifies.add(var);
	}
	public void addLocalVariable(String varName, String declareStatement) {
		localVariables.put(varName, declareStatement);
	}
	public boolean hasLocalVariable(String varName) {
		return localVariables.containsKey(varName);
	}
	public String toBoogie() {
		String code = "";
		code += declare;
		int tmp = modifies.size();
		if(tmp!=0) {
			if(implemented)
				code += "modifies ";
			else
				code += "	modifies ";
		}
		for(String var:modifies) {
			code += var;
			tmp--;
			if(tmp > 0)
				code += ", ";
			else
				code += ";\n";
		}
		if(implemented) {
			code += "{\n";
			for(String localVar:localVariables.keySet()) {
				code += "	"+localVariables.get(localVar);
//				code += "	var "+localVar+":"+localVariables.get(localVar)+";\n";
			}
			code += preBlock.toBoogie();
			code += mainBlock.toBoogie();
			code += "}\n";
		}
//		code += body;
		return code;
	}
	void addCondition(BoolExpr expr) {
		conditions.push(expr);
	}
	void popCondition() {
		conditions.pop();
	}
	Stack<BoolExpr> getConditions() {
		return conditions;
	}
	void setPreCondition(BoolExpr pre) {
		preCondition = pre;
	}
}

// update modifies
class BoogieProcedureOperator {
	ArrayList<BoogieProcedure> procedures;
	public BoogieProcedureOperator() {
		procedures = new ArrayList<>();
	}
	void addProcedure(BoogieProcedure procedure) {
		procedures.add(procedure);
	}
	void updateModify() {
		ArrayList<BoogieProcedure> tmp = new ArrayList<>();
		HashMap<String, Boolean> isVisited = new HashMap<>();
		for(BoogieProcedure procedure:procedures) {
			isVisited.put(procedure.name, false);
		}
		for(BoogieProcedure procedure:procedures) {
			for(BoogieProcedure parent:procedure.parents) {
				for(String key:procedure.modifies) {
					if(!parent.modifies.contains(key)) {
						parent.modifies.add(key);
						if(!isVisited.get(parent.name)) {
							tmp.add(parent);
							isVisited.put(parent.name, true);
						}
					}
				}
			}
		}
		while(!tmp.isEmpty()) {
			BoogieProcedure procedure =  tmp.get(0);
			tmp.remove(0);
			isVisited.put(procedure.name, false);
			for(BoogieProcedure parent:procedure.parents) {
				for(String key:procedure.modifies) {
					if(!parent.modifies.contains(key)) {
						parent.modifies.add(key);
						if(!isVisited.get(parent.name)) {
							tmp.add(parent);
							isVisited.put(parent.name, true);
						}
					}
				}
			}
		}
	}
	void updateCondition() {
		
	}
	void update() {
		updateModify();
		updateCondition();
	}
}