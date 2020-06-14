package verification.p4verifier;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.z3.BoolExpr;

public class UnaryOperator extends Node {

}

class Cmpl extends UnaryOperator {
	Node type;
	Node expr;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
		expr = Parser.getInstance().jsonParse(object.get(JsonKeyName.EXPR));
	}
	@Override
	String p4_to_Boogie() {
		if(type instanceof Type_Bits) {
			Type_Bits tb = (Type_Bits)type;
			String typeName = "bv"+tb.size;
			String functionName = "bnot."+typeName;
			String function = "\nfunction {:bvbuiltin \"bvnot\"} "+functionName;
			function += "(val:"+typeName+") returns ("+typeName+");";
			Parser.getInstance().addBoogieFunction(functionName, function);
			String code = functionName+"("+expr.p4_to_Boogie()+")";
			return code;
		}
		return super.p4_to_Boogie();
	}
//	function {:bvbuiltin "bvnot"} $not.bv1(i: bv1) returns (bv1);
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
	@Override
	BoolExpr getCondition() {
		BoolExpr e = Parser.getInstance().getContext().mkNot(expr.getCondition());
		return e;
	}
}

