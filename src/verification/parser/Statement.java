package verification.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Statement extends Node {
	
}

class BlockStatement extends Statement {
	
}

class AssignmentStatement extends Statement {
	
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