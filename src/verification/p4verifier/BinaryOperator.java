package verification.p4verifier;

import java.util.HashSet;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntNum;

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
	@Override
	String addAssertStatement(BoolExpr expr) {
		left.addAssertStatement(expr);
		right.addAssertStatement(expr);
		return super.addAssertStatement(expr);
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
	@Override
	BitVecExpr getBitVecExpr() {
		BitVecExpr bv = Parser.getInstance().getContext().mkBVSHL(left.getBitVecExpr(), right.getBitVecExpr());
		return bv;
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
	@Override
	BitVecExpr getBitVecExpr() {
		BitVecExpr bv = Parser.getInstance().getContext().mkBVLSHR(left.getBitVecExpr(), right.getBitVecExpr());
		return bv;
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
	@Override
	BitVecExpr getBitVecExpr() {
		BitVecExpr bv = Parser.getInstance().getContext().mkBVMul(left.getBitVecExpr(), right.getBitVecExpr());
		return bv;
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
	@Override
	BitVecExpr getBitVecExpr() {
		BitVecExpr bv = Parser.getInstance().getContext().mkBVAdd(left.getBitVecExpr(), right.getBitVecExpr());
		return bv;
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
	@Override
	BitVecExpr getBitVecExpr() {
		BitVecExpr bv = Parser.getInstance().getContext().mkBVSub(left.getBitVecExpr(), right.getBitVecExpr());
		return bv;
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
	@Override
	BitVecExpr getBitVecExpr() {
		BitVecExpr bv = Parser.getInstance().getContext().mkBVAND(left.getBitVecExpr(), right.getBitVecExpr());
		return bv;
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
	@Override
	BitVecExpr getBitVecExpr() {
		BitVecExpr bv = Parser.getInstance().getContext().mkBVOR(left.getBitVecExpr(), right.getBitVecExpr());
		return bv;
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
	@Override
	BitVecExpr getBitVecExpr() {
		BitVecExpr bv = Parser.getInstance().getContext().mkBVXOR(left.getBitVecExpr(), right.getBitVecExpr());
		return bv;
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
	@Override
	BoolExpr getCondition() {
		BoolExpr expr = Parser.getInstance().getContext().mkBVUGE(left.getBitVecExpr(), right.getBitVecExpr());
		return expr;
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
	@Override
	BoolExpr getCondition() {
		BoolExpr expr = Parser.getInstance().getContext().mkBVULE(left.getBitVecExpr(), right.getBitVecExpr());
		return expr;
	}
}


// >
class Grt extends BinaryOperator {
	@Override
	String p4_to_Boogie() {
		return super.p4_to_Boogie("bugt", "bvugt");
	}
	@Override
	BoolExpr getCondition() {
		BoolExpr expr = Parser.getInstance().getContext().mkBVUGT(left.getBitVecExpr(), right.getBitVecExpr());
		return expr;
	}
}

// <
class Lss extends BinaryOperator {
	@Override
	String p4_to_Boogie() {
		return super.p4_to_Boogie("bult", "bvult");
	}
	@Override
	BoolExpr getCondition() {
		BoolExpr expr = Parser.getInstance().getContext().mkBVULT(left.getBitVecExpr(), right.getBitVecExpr());
		return expr;
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
	@Override
	BoolExpr getCondition() {
		BoolExpr expr = Parser.getInstance().getContext().mkAnd(left.getCondition(), right.getCondition());
		return expr;
	}
	@Override
	String addAssertStatement() {
		left.addAssertStatement();
		BoolExpr expr = left.getCondition();
		right.addAssertStatement(expr);
		return "";
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
	@Override
	BoolExpr getCondition() {
		BoolExpr expr = Parser.getInstance().getContext().mkOr(left.getCondition(), right.getCondition());
		return expr;
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
	@Override
	BoolExpr getCondition() {
//		BoolExpr expr = Parser.getInstance().getContext().mkEq(left.getCondition(), right.getCondition());
		BoolExpr expr = Parser.getInstance().getContext().mkEq(left.getBitVecExpr(), right.getBitVecExpr());
		return expr;
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
	@Override
	BoolExpr getCondition() {
		BoolExpr expr = Parser.getInstance().getContext().mkNot(Parser.getInstance().getContext()
				.mkEq(left.getBitVecExpr(), right.getBitVecExpr()));
		return expr;
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
		if(Parser.getInstance().getCommands().ifCheckHeaderStackBound()) {
			Parser.getInstance().getResult().headerStackAssertionTotal.inc();
//			Parser.getInstance().addStack(stack);
			if(left instanceof Member) {
				Member m = (Member)left;
				Type_Stack stack = (Type_Stack)m.type;
				int idx = Integer.valueOf(right.p4_to_Boogie());
				int size = stack.size.value;
				if(idx>size) {
					String statement = "\n// Header Stack Out of Bound\n"+addIndent()+"assert(false);\n";
					Parser.getInstance().addBoogieStatement(statement);
					Parser.getInstance().getResult().headerStackOutOfBound.inc();
				}
//				String statement = addIndent()+"assert("+right.p4_to_Boogie()+"<="+stack.size.value+");\n";
//				Parser.getInstance().addBoogieStatement(statement);
			}
		}
		if(Parser.getInstance().getCommands().ifCheckHeaderValidity()&&type.Node_Type.equals("Type_Header")) {
			Context ctx = Parser.getInstance().getContext();
			BoolExpr condition = Parser.getInstance().getSetValidHeaderCondition(this.p4_to_Boogie());
			String statement = addIndent()+"assert(isValid["+this.p4_to_Boogie()+"]);\n";
			if(condition!=null) {
				Parser.getInstance().addBoogieAssertStatement(statement, this.p4_to_Boogie(), ctx.mkNot(condition));
			}
			else
				Parser.getInstance().addBoogieAssertStatement(statement, this.p4_to_Boogie());
			
//			String statement = addIndent()+"assert(isValid["+this.p4_to_Boogie()+"]);\n";
//			Parser.getInstance().addBoogieStatement(statement);
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