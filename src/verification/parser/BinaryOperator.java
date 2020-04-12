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

	String p4_to_Boogie(String op, String opbuiltin) {
		if(type instanceof Type_Bits) {
			Type_Bits tb = (Type_Bits)type;
			String typeName = "bv"+tb.size;
			String functionName = op+"."+typeName;
			String function = "\nfunction {:bvbuiltin \""+opbuiltin+"\"} "+functionName;
			function += "(left:"+typeName+", right:"+typeName+") returns("+typeName+");";
			Parser.getInstance().addBoogieFunction(functionName, function);
			String code = functionName+"("+left.p4_to_Boogie()+", "+right.p4_to_Boogie()+")";
			return code;
		}
		return "";
	}
}

class BAnd extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"&"+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		return super.p4_to_Boogie("band", "bvand");
	}
}

class BOr extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"|"+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		return super.p4_to_Boogie("bor", "bvor");
	}
}

class BXor extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"^"+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		return super.p4_to_Boogie("bxor", "bvxor");
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
	@Override
	String p4_to_Boogie() {
		String code = "("+left.p4_to_Boogie()+"||"+right.p4_to_Boogie()+")";
		return code;
	}
}

class Shl extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+" << "+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		return super.p4_to_Boogie("shl", "bvshl");
	}
}

class Shr extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+" >> "+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		return super.p4_to_Boogie("shr", "bvshr");
	}
}

class Mul extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"*"+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		return super.p4_to_Boogie("mul", "bvmul");
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
		return super.p4_to_Boogie("add", "bvadd");
	}
}

class Sub extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"-"+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		return super.p4_to_Boogie("sub", "bvsub");
	}
}

class Equ extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"=="+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		String code = left.p4_to_Boogie()+"=="+right.p4_to_Boogie();
		return code;
	}
}

class Neq extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"!="+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		String code = left.p4_to_Boogie()+"!="+right.p4_to_Boogie();
		return code;
	}
}

class ArrayIndex extends BinaryOperator {
	@Override
	String getTypeName() {
		return type.getTypeName();
	}
	@Override
	String p4_to_Boogie() {
		String code = left.p4_to_Boogie()+"["+right.p4_to_Boogie()+"]";
		return code;
	}
}

class Grt extends BinaryOperator {

}