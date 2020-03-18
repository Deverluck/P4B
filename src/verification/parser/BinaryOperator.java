package verification.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class BinaryOperator extends Node {
	Node left;
	Node right;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		left = Parser.jsonParse(object.get(JsonKeyName.LEFT));
		right = Parser.jsonParse(object.get(JsonKeyName.RIGHT));
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