package verification.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Statement extends Node {

}

class BlockStatement extends Statement {
	Node components;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		components = Parser.getInstance().jsonParse(object.get(JsonKeyName.COMPONENTS));
	}
	@Override
	String p4_to_C() {
		String code = components.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		String code = components.p4_to_Boogie();
		return code;
	}
}

class AssignmentStatement extends Statement {
	Node left;
	Node right;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		left = Parser.getInstance().jsonParse(object.get(JsonKeyName.LEFT));
		right = Parser.getInstance().jsonParse(object.get(JsonKeyName.RIGHT));
	}
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"="+right.p4_to_C()+";\n";
		return code;
	}
}

class EmptyStatement extends Statement {

}

class IfStatement extends Statement {
	Node condition;
	Node ifTrue;
	Node ifFalse;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		condition = Parser.getInstance().jsonParse(object.get(JsonKeyName.CONDITION));
		ifTrue = Parser.getInstance().jsonParse(object.get(JsonKeyName.IFTRUE));
		if(object.has(JsonKeyName.IFFALSE)) {
			ifFalse = Parser.getInstance().jsonParse(object.get(JsonKeyName.IFFALSE));
		}
		else {
			ifFalse = null;
		}
	}
	@Override
	String p4_to_C() {
		String code = "if(";
		code += condition.p4_to_C();
		code += "){\n";
		code += ifTrue.p4_to_C();
		code += "}\n";
		if(ifFalse != null) {
			code += "else {\n";
			code += ifFalse.p4_to_C();
			code += "}\n";
		}
		return code;
	}
	@Override
	String p4_to_Boogie() {
		String code = addIndent()+"if(";
		code += condition.p4_to_Boogie();
		code += "){\n";
		incIndent();
		code += ifTrue.p4_to_Boogie();
		decIndent();
		code += addIndent()+"}\n";
		if(ifFalse != null) {
			code += addIndent()+"else {\n";
			incIndent();
			code += ifFalse.p4_to_Boogie();
			decIndent();
			code += addIndent()+"}\n";
		}
		return code;
	}
}

class MethodCallStatement extends Statement {
	Node methodCall;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		methodCall = Parser.getInstance().jsonParse(object.get(JsonKeyName.METHODCALL));
	}
	@Override
	String p4_to_C() {
		return methodCall.p4_to_C()+";\n";
	}
	@Override
	String p4_to_Boogie() {
		String code = addIndent()+"call ";
		return code+methodCall.p4_to_Boogie()+";\n";
	}
}

class SwitchStatement extends Statement {

}

class SwitchCase extends Statement {

}