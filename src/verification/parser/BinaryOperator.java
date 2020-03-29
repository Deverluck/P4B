package verification.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class BinaryOperator extends Node {
	Node left;
	Node right;
	Node type;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		left = Parser.getInstance().jsonParse(object.get(JsonKeyName.LEFT));
		right = Parser.getInstance().jsonParse(object.get(JsonKeyName.RIGHT));
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
	}
}

class BAnd extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"&"+right.p4_to_C();
		return code;
	}
}

class BOr extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"|"+right.p4_to_C();
		return code;
	}
}

class BXor extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"^"+right.p4_to_C();
		return code;
	}
}

class Geq extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+">="+right.p4_to_C();
		return code;
	}
}

class Leq extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"<="+right.p4_to_C();
		return code;
	}
}

class LAnd extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"&&"+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		String code = left.p4_to_Boogie()+"&&"+right.p4_to_Boogie();
		return code;
	}
}

class LOr extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"||"+right.p4_to_C();
		return code;
	}
}

class Shl extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+" << "+right.p4_to_C();
		return code;
	}
}

class Shr extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+" >> "+right.p4_to_C();
		return code;
	}
}

class Mul extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"*"+right.p4_to_C();
		return code;
	}
}

class Add extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"+"+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		if(type instanceof Type_Bits) {
			Type_Bits tb = (Type_Bits)type;
			String typeName = "bv"+tb.size;
			String functionName = "add."+typeName;
			String function = "function {:bvbuiltin \"bvadd\"} "+functionName;
			function += "(left:"+typeName+", right:"+typeName+") returns("+typeName+");";
			Parser.getInstance().addBoogieFunction(functionName, function);

			String code = functionName+"("+left.p4_to_Boogie()+", "+right.p4_to_Boogie()+")";
			return code;
		}

		return super.p4_to_Boogie();
	}
}

class Sub extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"-"+right.p4_to_C();
		return code;
	}
}

class Neq extends BinaryOperator {

}

class Equ extends BinaryOperator {

}

class Grt extends BinaryOperator {

}