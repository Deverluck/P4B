package verification.p4verifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Queue;
import java.util.Stack;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

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
	BoogieBlock postBlock;
	boolean implemented;
	
	HashSet<String> refHeaders;
	
	// for analyze assert statements
	BoolExpr preCondition;     // (disjunction) the procedure may be called by different execution path
	BoolExpr postCondition;
	Stack<BoolExpr> conditions; // (conjunction) a stack updating with if statements and switch statements
	Solver solver;
//	ArrayList<BoogieAssertStatement> assertStatements;
	
	private BoogieProcedure() {
		parents = new HashSet<>();
		childrenNames = new HashSet<>();
		modifies = new HashSet<>();
		localVariables = new LinkedHashMap<String, String>();
		preBlock = new BoogieBlock();
		mainBlock = new BoogieBlock();
		postBlock = new BoogieBlock();
		implemented = true;
		
		refHeaders = new HashSet<>();
		
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
	public void addHeaderRef(String header) {
		refHeaders.add(header);
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
//			if(name.equals("parse_fabric_header_cpu")) {
//				System.out.println("***test***");
//				System.out.println(preCondition);
//			}
//			System.out.println(name);
			code += mainBlock.toBoogie(preCondition);
			code += postBlock.toBoogie();
			code += "}\n";
		}
//		code += body;
		return code;
	}
	void addCondition(BoolExpr expr) {
		conditions.push(expr);
	}
	boolean popCondition() {
		if(conditions.empty())
			return false;
		conditions.pop();
		return true;
	}
	Stack<BoolExpr> getConditions() {
		return conditions;
	}
	void setPreCondition(BoolExpr pre) {
		preCondition = pre;
	}
	BoolExpr getPreCondition() {
//		if(preCondition==null) {
//			return Parser.getInstance().getContext().mkBool(true);
//		}
		return preCondition;
	}
	void setSolver(Solver solver) {
		this.solver = solver;
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
	void updateCondition(Context ctx) {
		HashMap<String, BoogieProcedure> procedureMap = new HashMap<>();
		HashMap<String, Boolean> inQueue = new HashMap<>();
		ArrayList<BoogieProcedure> queue = new ArrayList<>();
		for(BoogieProcedure procedure:procedures) {
			procedureMap.put(procedure.name, procedure);
			inQueue.put(procedure.name, true);
			queue.add(procedure);
		}
		while(!queue.isEmpty()) {
			BoogieProcedure procedure = queue.get(0);
			queue.remove(0);
			inQueue.put(procedure.name, false);
			for(String child:procedure.childrenNames) {
				if(child.equals(procedure.name))
					continue;
				if(procedureMap.containsKey(child)) {
					BoolExpr parentToChild = Parser.getInstance().getProcedurePrecondition(child, procedure.name);
					BoolExpr parentCondition = procedure.getPreCondition();
					BoolExpr childCondition = procedureMap.get(child).getPreCondition();
					if(parentCondition==null&&parentToChild==null)
						continue;
					if(childCondition==null) {
						inQueue.put(child, true);
						queue.add(procedureMap.get(child));
						if(parentCondition==null) {
							procedureMap.get(child).setPreCondition(parentToChild);
						}
						else if(parentToChild==null) {
							procedureMap.get(child).setPreCondition(parentCondition);
						}
						else {
							procedureMap.get(child).setPreCondition(ctx.mkAnd(parentToChild, parentCondition));
						}
					}
					else {
						BoolExpr newChildCondition;
						if(parentToChild==null)
							newChildCondition=parentCondition;
						else if(parentCondition==null)
							newChildCondition=parentToChild;
						else
							newChildCondition=ctx.mkAnd(parentToChild, parentCondition);
						newChildCondition=ctx.mkOr(newChildCondition, childCondition);
						Solver solver = ctx.mkSolver();
						solver.add(ctx.mkNot(ctx.mkIff(childCondition, newChildCondition)));
						if(solver.check()==Status.SATISFIABLE) {
							inQueue.put(child, true);
							queue.add(procedureMap.get(child));
							procedureMap.get(child).setPreCondition(newChildCondition);
						}
					}
				}
			}
		}
	}
//	void updateCondition(Context ctx) {
//		HashMap<String, BoogieProcedure> procedureMap = new HashMap<>();
//		HashMap<String, Boolean> inQueue = new HashMap<>();
//		HashMap<String, Boolean> isVisited = new HashMap<>();
//		ArrayList<BoogieProcedure> queue = new ArrayList<>();
//		for(BoogieProcedure procedure:procedures) {
//			procedureMap.put(procedure.name, procedure);
//			inQueue.put(procedure.name, true);
//			isVisited.put(procedure.name, false);
//			queue.add(procedure);
//		}
//		while(!queue.isEmpty()) {
//			BoogieProcedure procedure = queue.get(0);
//			System.out.println(procedure.name);
//			queue.remove(0);
//			inQueue.put(procedure.name, false);
//			for(String child:procedure.childrenNames) {
//				if(child.equals(procedure.name))
//					continue;
//				if(procedureMap.containsKey(child)) {
//					BoolExpr expr1 = procedure.getPreCondition();
//					BoolExpr expr2 = procedureMap.get(child).getPreCondition();
//					if(expr1==null)
//						continue;
//					if(inQueue.get(child)==false) {
//						inQueue.put(child, true);
//						queue.add(procedureMap.get(child));
//					}
//					if(expr2==null) {
////						expr2 = expr1;
//						expr2 = ctx.mkAnd(ctx.mkBool(true), expr1);
//						continue;
//					}
//					expr2 = ctx.mkAnd(expr1, expr2);
//					procedureMap.get(child).setPreCondition(expr2);
//				}
//			}
//		}
//	}
	void update(Context ctx) {
		updateModify();
//		System.out.println("wryyyyyyyy");
		updateCondition(ctx);
	}
}