package verification.parser;

import java.util.ArrayList;
import java.util.Stack;

import com.microsoft.z3.BoolExpr;
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
		if(this instanceof BoogieAssertStatement) {
			BoogieAssertStatement bas = (BoogieAssertStatement)this;
			Solver solver = Parser.getInstance().createSolver();
			solver.add(bas.condition);
			if(condition!=null)
				solver.add(condition);
//			if(cont.contains("Heap[hdr, headers.fabric_header_sflow]")) {
//				System.out.println("***test Heap[hdr, headers.fabric_header_sflow]***");
//				System.out.println(condition);
//				System.out.println(bas.condition);
//			}
			Parser.getInstance().count();
			if(solver.check()==Status.UNSATISFIABLE) {
				Parser.getInstance().decCount();
				System.out.println(Status.UNSATISFIABLE);
			}
			else {
//				System.out.println(cont);
//				System.out.println(Status.SATISFIABLE);
			}
		}
		return toBoogie();
	}
}

class BoogieAssertStatement extends BoogieStatement{
	BoolExpr condition;
	public BoogieAssertStatement(String cont) {
		super(cont);
	}
	void setCondition(BoolExpr c) {
		condition = c;
	}
}

class BoogieHeaderValidityAssertStatement extends BoogieAssertStatement {
	String headerName;
	public BoogieHeaderValidityAssertStatement(String cont, String headerName) {
		super(cont);
		this.headerName = headerName;
	}
	String getHeaderName() {
		return headerName;
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
		if(condition==null)
			return toBoogie();
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