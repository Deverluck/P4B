package verification.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Statement extends Node {
	
}

class BlockStatement extends Statement {
	Node components;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		components = Parser.jsonParse(object.get(JsonKeyName.COMPONENTS));
	}
	@Override
	String p4_to_C() {
		String code = components.p4_to_C();
		return code;
	}
}

class AssignmentStatement extends Statement {
	Node left;
	Node right;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		left = Parser.jsonParse(object.get(JsonKeyName.LEFT));
		right = Parser.jsonParse(object.get(JsonKeyName.RIGHT));
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
	
}

class MethodCallStatement extends Statement {
	Node methodCall;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		methodCall = Parser.jsonParse(object.get(JsonKeyName.METHODCALL));
	}
	@Override
	String p4_to_C() {
		return methodCall.p4_to_C();
	}
}

class SwitchStatement extends Statement {
	
}

class SwitchCase extends Statement {
	
}