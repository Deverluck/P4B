package verification.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class UnaryOperator extends Node {

}

class Cmpl extends UnaryOperator {

}

class LNot extends UnaryOperator {
	Node expr;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		expr = Parser.getInstance().jsonParse(object.get(JsonKeyName.EXPR));
	}
	@Override
	String p4_to_C() {
		String code = "!";
		code += expr.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		String code = "!"+expr.p4_to_Boogie();
		return code;
	}
}

