package verification.parser;

import java.util.ArrayList;

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
	String toBoogie() {
		String code = "";
		for(BoogieStatement bs:conts) {
			code += bs.toBoogie();
		}
		return code;
	}
}

/** for if(){}, start is "if(...){", end is "}"
    for else{}, start is "else{", end is "}" **/
class BoogieIfStatement extends BoogieStatement {
	String start;
	String end;
	ArrayList<BoogieStatement> conts;
	public BoogieIfStatement(String start, String end) {
		super("");
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