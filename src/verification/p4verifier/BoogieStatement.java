package verification.p4verifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

public class BoogieStatement {
	String cont;
	public BoogieStatement(String cont) {
		this.cont = cont;
	}
	String toBoogie() {
		return cont;
	}
	String toBoogie(BoolExpr condition){
//		if(this instanceof BoogieAssertStatement) {
//			BoogieAssertStatement bas = (BoogieAssertStatement)this;
//			Solver solver = Parser.getInstance().createSolver();
//			solver.add(bas.condition);
////			if(condition!=null)
////				solver.add(condition);
//			Parser.getInstance().count();
//			
////			System.out.println("verifying:");
////			System.out.println(toBoogie());
////			System.out.println(solver.toString());
//			if(solver.check()==Status.UNSATISFIABLE) {
//				Parser.getInstance().decCount();
////				System.out.println(Status.UNSATISFIABLE);
//				return "";
//			}
//			else {
//				System.out.println(cont);
////				System.out.println(Status.SATISFIABLE);
//			}
////			System.out.println("verification ends\n");
//		}
		return toBoogie();
	}
}

class BoogieAssertStatement extends BoogieStatement{
	BoolExpr condition;
	String procedureName;
	public BoogieAssertStatement(String cont, String procedureName) {
		super(cont);
		this.procedureName = procedureName;
	}
	void setCondition(BoolExpr c) {
		condition = c;
	}
	
	boolean removeDuplicate() {
		ArrayList<String> names = new ArrayList<>();
		
		int cnt = -1;
		boolean duplicate = false;
		Iterator<BoogieAssertStatement> it = Parser.getInstance().getAssertStatements().iterator();
//		for(BoogieAssertStatement statement:Parser.getInstance().getAssertStatements()) {
		while(it.hasNext()) {
			BoogieAssertStatement statement = it.next();
			if(!statement.cont.equals(this.cont))
				continue;
			BoogieProcedure p1 = Parser.getInstance().getProcedrue(procedureName);
			BoogieProcedure p2 = Parser.getInstance().getProcedrue(statement.procedureName);
			if(p1.getPreCondition()==null&&p2.getPreCondition()==null) {
				names.add(statement.procedureName);
				duplicate = true;
				it.remove();
				cnt++;
			}
			if(p1.getPreCondition()==null)
				continue;
			if(p2.getPreCondition()==null)
				continue;
			Context ctx = Parser.getInstance().getContext();
			BoolExpr c1 = ctx.mkAnd(condition, p1.getPreCondition());
			BoolExpr c2 = ctx.mkAnd(statement.condition, p2.getPreCondition());
			if(c1.toString().equals(c2.toString())) {
				names.add(statement.procedureName);
				
				duplicate = true;
				it.remove();
//				Parser.getInstance().getAssertStatements().remove(statement);
				cnt++;
			}
		}
//		if(cnt>0) {
//			System.out.println("\n****** Remove Duplicate ******");
//			System.out.println("procedure "+procedureName+":");
//			System.out.println(names);
//			System.out.println(cont);
//			System.out.println("Remove duplicate assert statements num: "+cnt);
//			System.out.println("****** Remove Duplicate ENDS ******\n");
//		}
		return duplicate;
	}
	
	String toBoogie(BoolExpr condition){
		if(Parser.getInstance().getCommands().ifShowLog()) {
			System.out.println("##verifying##");
			System.out.println(this.procedureName);
		}
//		System.out.println(condition);
		
		if(!Parser.getInstance().getAssertStatements().contains(this)) {
			return "";
		}
		if(Parser.getInstance().getCommands().ifCheckHeaderValidity()
				&&Parser.getInstance().getCommands().ifRemoveRedundantAssertions()) {
			removeDuplicate();
		}
		
//		BoogieAssertStatement bas = (BoogieAssertStatement)this;
		Solver solver = Parser.getInstance().createSolver();
		solver.add(this.condition);
//		System.out.println(this.condition);
		if(condition!=null)
			solver.add(condition);
//		Parser.getInstance().count();
			
		if(solver.check()==Status.UNSATISFIABLE) {
//			Parser.getInstance().decCount();
//			System.out.println(Status.UNSATISFIABLE);
//			System.out.println();
			return "";
		}
		else {
			Parser.getInstance().count();
			if(Parser.getInstance().getCommands().ifShowLog()) {
				System.out.println("!!!Bugs in "+this.procedureName+"!!!");
				System.out.println(this.cont);
			}
//			System.out.println(condition);
//			System.out.println(this.condition);
			return "";
//			System.out.println(cont);
		}
//		return toBoogie();
	}
}

class BoogieHeaderValidityAssertStatement extends BoogieAssertStatement {
	String headerName;
	public BoogieHeaderValidityAssertStatement(String cont, String headerName, String procedureName) {
		super(cont, procedureName);
		this.headerName = headerName;
	}
	String getHeaderName() {
		return headerName;
	}
	@Override
	String toBoogie(BoolExpr condition) {
		String exprName = "isValid["+headerName+"]";
		exprName = exprName.replace('[', '_');
		exprName = exprName.replace("], ", "_");
		exprName = exprName.replace(", ", "_");
		exprName = exprName.replace(']', '_');
		exprName = exprName.replace('.', '_');
		BoolExpr expr = Parser.getInstance().getContext().mkBoolConst(exprName);
		BoolExpr c = null;
		if(condition!=null&&condition.toString().contains(exprName))
			c = condition;
		else if(this.condition!=null&&this.condition.toString().contains(exprName))
			c = this.condition;
		if(c!=null&&c.toString().contains(exprName)) {
			Solver solver = Parser.getInstance().createSolver();
			solver.add(c);
			solver.add(Parser.getInstance().getContext().mkNot(expr));
			if(solver.check()==Status.UNSATISFIABLE) {
//				Parser.getInstance().decCount();
				return "";
			}
		}
		ArrayList<BoogieProcedure> states = Parser.getInstance().fromHeaderToParserStates(headerName);
		if(states!=null) {
			for(BoogieProcedure state:states) {
				if(StateMachine.getInstance().work(state.name)) {
					return "";
				}
			}
		}
		return super.toBoogie(condition);
	}
}

class BoogieBlock extends BoogieStatement {
	ArrayList<BoogieStatement> conts;
	public BoogieBlock() {
		super("");
		conts = new ArrayList<>();
	}
	void add(BoogieStatement statement) {
		conts.add(statement);
	}
	void addToFirst(BoogieStatement statement) {
		conts.add(0, statement);
	}
	String toBoogie() {
		String code = "";
		for(BoogieStatement bs:conts) {
			code += bs.toBoogie();
		}
		return code;
	}
	String toBoogie(BoolExpr condition) {
//		if(condition==null)
//			return toBoogie();
		String code = "";
		for(BoogieStatement bs:conts) {
			code += bs.toBoogie(condition);
		}
		return code;
	}
	boolean isEmpty() {
		return conts.isEmpty();
	}
	void clear() {
		conts.clear();
	}
}

/** for if(){}, start is "if(...){", end is "}"
    for else{}, start is "else{", end is "}" **/
class BoogieIfStatement extends BoogieBlock {
	String start;
	String end;
	public BoogieIfStatement(String start, String end) {
		super();
		this.start = start;
		this.end = end;
	}
	String toBoogie() {
		String code = start;
		for(BoogieStatement bs:conts) {
			code += bs.toBoogie();
		}
		code += end;
		return code;
	}
	@Override
	String toBoogie(BoolExpr condition) {
		String code = start;
		for(BoogieStatement bs:conts) {
			code += bs.toBoogie(condition);
		}
		code += end;
		return code;
	}
}

class BoogieBlockStack {
	Stack<BoogieBlock> blockStack;
	public BoogieBlockStack() {
		blockStack = new Stack<>();
	}
	void addBlock(BoogieBlock block) {
		blockStack.push(block);
	}
	BoogieBlock top() {
		return blockStack.peek();
	}
	// pop top block and add it to new top block's content
	BoogieBlock popBlock() {
		BoogieBlock peek = top();
		blockStack.pop();
		if(!blockStack.empty())
			top().add(peek);
		return peek;
	}
	void addStatement(BoogieStatement statement) {
		top().add(statement);
	}
	void clear() {
		blockStack.clear();
	}
}