package verification.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class BinaryOperator extends Node {
	int left;
	int right;
	@Override
	void parse(ObjectNode object) {
		// TODO Auto-generated method stub
		super.parse(object);
	}
}

class BAnd extends BinaryOperator {
	
}

class BOr extends BinaryOperator {
	
}

class BXor extends BinaryOperator {
	
}

class Geq extends BinaryOperator {
	
}

class Leq extends BinaryOperator {
	
}

class LAnd extends BinaryOperator {
	
}

class LOr extends BinaryOperator {
	
}

class Shl extends BinaryOperator {
	
}

class Shr extends BinaryOperator {
	
}

class Mul extends BinaryOperator {
	
}

class Add extends BinaryOperator {
	
}

class Sub extends BinaryOperator {
	
}

class Neq extends BinaryOperator {
	
}

class Equ extends BinaryOperator {
	
}

class Grt extends BinaryOperator {
	
}