package verification.parser;

import java.util.ArrayList;
import java.util.Stack;

public class BoogieStatement {
	String cont;
	public BoogieStatement(String cont) {
		this.cont = cont;
	}
	String toBoogie(){
		return cont;
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