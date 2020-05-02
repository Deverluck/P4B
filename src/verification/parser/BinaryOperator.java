package verification.parser;

import java.util.ArrayList;
import java.util.HashSet;

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
			if(!right.p4_to_Boogie().contains(typeName) && !right.p4_to_Boogie().contains("[")) {
				String str = right.p4_to_Boogie();
				boolean isDigit = true;
				for(int i = 0; i < str.length(); i++) {
					if(!Character.isDigit(str.charAt(i))) {
						isDigit = false;
						break;
					}
				}
				if(isDigit)
					code = functionName+"("+left.p4_to_Boogie()+", "+right.p4_to_Boogie()+typeName+")";
			}
			return code;
		}
		else if(type instanceof Type_Boolean) {
			String typeName = "bool";
			String bvName = left.getTypeName();
			String functionName = op+"."+bvName;
			String function = "\nfunction {:bvbuiltin \""+opbuiltin+"\"} "+functionName;
			function += "(left:"+bvName+", right:"+bvName+") returns("+typeName+");";
			Parser.getInstance().addBoogieFunction(functionName, function);
			String code = functionName+"("+left.p4_to_Boogie()+", "+right.p4_to_Boogie()+")";
			return code;
		}
		return "";
	}
	@Override
	String getTypeName() {
		return type.getTypeName();
	}
	@Override
	HashSet<String> getBranchVariables() {
		HashSet<String> list1, list2;
		list1 = left.getBranchVariables();
		list2 = right.getBranchVariables();
		HashSet<String> res = new HashSet<>();
		if(list1 != null)
			res.addAll(list1);
		if(list2 != null)
			res.addAll(list2);
		return res;
	}
	@Override
	String addAssertStatement() {
		left.addAssertStatement();
		right.addAssertStatement();
		return super.addAssertStatement();
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
		return super.p4_to_Boogie("shr", "bvlshr");
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


/* 
 *  Logic Operations
 */

// >=
class Geq extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+">="+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		return super.p4_to_Boogie("bsge", "bvsge");
	}
}

// <= 
class Leq extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"<="+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		return super.p4_to_Boogie("bsle", "bvsle");
	}
}


// >
class Grt extends BinaryOperator {
	@Override
	String p4_to_Boogie() {
		return super.p4_to_Boogie("bugt", "bvugt");
	}
}

// <
class Lss extends BinaryOperator {
	@Override
	String p4_to_Boogie() {
		return super.p4_to_Boogie("bult", "bvult");
	}
}

// &&
class LAnd extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"&&"+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		String code = "("+left.p4_to_Boogie()+") && ("+right.p4_to_Boogie()+")";
		return code;
	}
}

// ||
class LOr extends BinaryOperator {
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"||"+right.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		String code = "("+left.p4_to_Boogie()+")||("+right.p4_to_Boogie()+")";
		return code;
	}
}

// ==
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

// !=
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
	String getName() {
		return left.getName();
	}
	@Override
	String getTypeName() {
		return type.getTypeName();
	}
	@Override
	String p4_to_Boogie() {
		String code = left.p4_to_Boogie()+"["+right.p4_to_Boogie()+"]";
		return code;
	}
	@Override
	String p4_to_Boogie(String arg) {
		if(arg.equals("emit")) {
			String code = left.p4_to_Boogie()+", "+right.p4_to_Boogie();
			return code;
		}
		return super.p4_to_Boogie(arg);
	}
	@Override
	String addAssertStatement() {
		if(type.Node_Type.equals("Type_Header")) {
			String statement = addIndent()+"assert(isValid["+this.p4_to_Boogie()+"]);\n";
			Parser.getInstance().addBoogieStatement(statement);
			return statement;
		}
		return super.addAssertStatement();
	}
}

class Mask extends BinaryOperator {
	@Override
	String p4_to_Boogie() {
		if(type instanceof Type_Set) {
			Type_Set ts = (Type_Set)type;
			if(ts.elementType instanceof Type_Bits) {
				Type_Bits tb = (Type_Bits)ts.elementType;
				String typeName = "bv"+tb.size;
				String functionName = "band."+typeName;
				String opbuiltin = "bvand";
				String function = "\nfunction {:bvbuiltin \""+opbuiltin+"\"} "+functionName;
				function += "(left:"+typeName+", right:"+typeName+") returns("+typeName+");";
				Parser.getInstance().addBoogieFunction(functionName, function);
				return functionName;
			}
		}
		return super.p4_to_Boogie();
	}
}