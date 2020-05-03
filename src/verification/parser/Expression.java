package verification.parser;

import java.util.ArrayList;
import java.util.HashSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;

public class Expression extends Node {

}

class TypeNameExpression extends Expression {

}

class ConstructorCallExpression extends Expression {
	Node type;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
	}
	@Override
	String p4_to_C() {
		type.setEnable();
		String code = type.p4_to_C(JsonKeyName.METHODCALL);
		return code;
	}
	@Override
	String getName() {
		return type.getTypeName();
	}
	@Override
	String getTypeName() {
		return type.Node_Type;
	}
}

class MethodCallExpression extends Expression {
	Node method;
	ArrayList<Node> arguments;
	ArrayList<Node> typeArguments;
	public MethodCallExpression() {
		super();
		arguments = new ArrayList<>();
		typeArguments = new ArrayList<>();
	}
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		method = Parser.getInstance().jsonParse(object.get(JsonKeyName.METHOD));
		ArrayNode arguments_node = (ArrayNode)object.get(JsonKeyName.ARGUMENTS).get(JsonKeyName.VEC);
		if(arguments_node != null) {
			for(JsonNode node : arguments_node) {
				arguments.add(Parser.getInstance().jsonParse(node));
			}
		}
		ArrayNode typeArguments_node = (ArrayNode)object.get(JsonKeyName.TYPEARGUMENTS).get(JsonKeyName.VEC);
		if(typeArguments_node != null) {
			for(JsonNode node : typeArguments_node) {
				typeArguments.add(Parser.getInstance().jsonParse(node));
			}
		}
	}

	@Override
	String p4_to_C() {
		String code = method.p4_to_C()+"(";
		int cnt = 0;
		if(!arguments.isEmpty()) {
			for(Node node : arguments) {
//				System.out.println(code);
				cnt++;
				code += node.p4_to_C();
				if(cnt < arguments.size())
					code += ", ";
			}
		}
//		code = code + ");\n";
		code = code + ")";
		return code;
	}

	@Override
	String p4_to_Boogie() {
		String code = "";
		String methodName = method.p4_to_Boogie();
		if(methodName.equals("extract")) {
//			methodName = "packet_in."+methodName+"."+typeArguments.get(0).getTypeName();
			String argument = arguments.get(0).getName();
			Parser.getInstance().addHeaderValidParserState(arguments.get(0).p4_to_Boogie());
			if(argument.contains(".next"))
				methodName = "packet_in."+methodName+".headers."+arguments.get(0).getName();
			else
				methodName = "packet_in."+methodName;
		}
		else if(methodName.equals("emit")) {
			methodName = "packet_out."+methodName+".headers."+arguments.get(0).getName();
		}
		else if(methodName.length()>10 && methodName.substring(0, 11).equals("setInvalid(")) {
			methodName = "isValid["+methodName.substring(11, methodName.length()-1)+"] := false";
			Parser.getInstance().getCurrentProcedure().updateModifies("isValid");
			return methodName;
		}
		else if(methodName.length()>10 && methodName.substring(0, 9).equals("setValid(")) {
			methodName = "isValid["+methodName.substring(9, methodName.length()-1)+"] := true";
			Parser.getInstance().getCurrentProcedure().updateModifies("isValid");
			return methodName;
		}
		else if(methodName.equals("lookahead")) {
			return "havoc";
//			String body = "";
//			if(typeArguments.get(0) instanceof Type_Bits) {
//				Type_Bits tb = (Type_Bits)typeArguments.get(0);
//				int size = tb.size;
//				for(int i = 0; i < size; i++) {
//					body += "packet.map[packet.index+"+i+"]";
//					if(i != size-1)
//						body += "++";
//				}
//			}
//			return body;
		}
		// deal with isValid()
		else if(methodName.length()>8 && methodName.substring(0, 8).equals("isValid[")) {
			return methodName;
		}
		else {
			Parser.getInstance().addProcedurePrecondition(methodName);
		}
		code = methodName+"(";
		int cnt = 0;
		if(!arguments.isEmpty()) {
			for(Node node : arguments) {
				cnt++;
				if(methodName.contains(".emit"))
					code += node.p4_to_Boogie("emit");
				else
					code += node.p4_to_Boogie();
				if(cnt < arguments.size())
					code += ", ";
			}
		}
		code = code + ")";
		code = code.replace(")(", ", ");
		return code;
	}
	@Override
	BoolExpr getCondition() {
		return method.getCondition();
	}
}

class PathExpression extends Expression {
	Node path;
	Node type;
	public PathExpression() {
		super();
	}
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		path = Parser.getInstance().jsonParse(object.get(JsonKeyName.PATH));
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
		addChild(path);
		addChild(type);
	}
	@Override
	String p4_to_C() {
		return path.p4_to_C();
	}
	@Override
	String p4_to_C(String arg) {
		if(arg.equals(JsonKeyName.PARSERSTATE))
			return path.p4_to_C()+"();\n";
		return p4_to_C();
	}

	@Override
	String p4_to_Boogie() {
		String code = path.p4_to_Boogie();
		if(Parser.getInstance().isParserState() && Parser.getInstance().isParserLocal(code)) {
			code = "parser."+code;
			Parser.getInstance().addModifiedGlobalVariable(code);
		}
		return code;
	}
	@Override
	String p4_to_Boogie(String arg) {
		String code = "";
		if(arg.equals(JsonKeyName.PARSERSTATE)) {
			String methodName = path.p4_to_Boogie();
			
			String statement = addIndent()+"call "+methodName+"();\n";
			Parser.getInstance().addBoogieStatement(statement);
			
			code += statement;
			Parser.getInstance().getCurrentProcedure().childrenNames.add(methodName);
			return code;
		}
		return p4_to_Boogie();
	}
	@Override
	String getTypeName() {
		return type.getTypeName();
	}
	@Override
	String getName() {
		return path.getName();
	}
}

class ExpressionValue extends Expression {
	Node expression;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		expression = Parser.getInstance().jsonParse(object.get(JsonKeyName.EXPRESSION));
	}
	@Override
	String p4_to_C() {
		// TODO Auto-generated method stub
		return super.p4_to_C();
	}
}

class Slice extends Expression {
	Node type;
	Node e0, e1, e2;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
		e0 = Parser.getInstance().jsonParse(object.get(JsonKeyName.E0));
		e1 = Parser.getInstance().jsonParse(object.get(JsonKeyName.E1));
		e2 = Parser.getInstance().jsonParse(object.get(JsonKeyName.E2));
	}
	@Override
	String p4_to_Boogie() {
		String code = e0.p4_to_Boogie();
		if(code.contains("++"))
			code = "("+code+")";
		int end = Integer.parseInt(e1.p4_to_Boogie());
		end++;
		code += "["+end+":"+e2.p4_to_Boogie()+"]";
		return code;
	}
	@Override
	String getTypeName() {
		return type.getTypeName();
	}
	@Override
	BitVecExpr getBitVecExpr() {
		String exprName = p4_to_Boogie();
		exprName = exprName.replace('[', '_');
		exprName = exprName.replace("], ", "_");
		exprName = exprName.replace(", ", "_");
		exprName = exprName.replace(']', '_');
		exprName = exprName.replace('.', '_');
		exprName = exprName.replace(':', '_');
		String typeName = type.getTypeName();
		if(typeName.contains("bv")) {
			int size = Integer.valueOf(typeName.substring(2));
			BitVecExpr bv = Parser.getInstance().getContext().mkBVConst(exprName, size);
			return bv;
		}
		return null;
	}
}

class Cast extends Expression {
	Node destType;
	Node expr;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		destType = Parser.getInstance().jsonParse(object.get(JsonKeyName.DESTTYPE));
		expr = Parser.getInstance().jsonParse(object.get(JsonKeyName.EXPR));
	}
	@Override
	String p4_to_Boogie() {
		if(destType instanceof Type_Bits) {
			Type_Bits tb = (Type_Bits)destType;
			int dstSize = tb.size, srcSize = 0;
			String tb2 = expr.getTypeName();
			if(tb2.contains("bv")) {
				srcSize = Integer.parseInt(tb2.substring(2));
				if(dstSize < srcSize) {
					return expr.p4_to_Boogie()+"["+dstSize+":0]";
				}
				else if(dstSize > srcSize) {
					return "0bv"+(dstSize-srcSize)+"++"+expr.p4_to_Boogie();
				}
				else {
					return expr.p4_to_Boogie();
				}
			}
			return super.p4_to_Boogie();
		}
		return super.p4_to_Boogie();
	}
}

class Member extends Expression {
	String member;
	Node expr;
	Node type;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		member = object.get(JsonKeyName.MEMBER).asText();
		expr = Parser.getInstance().jsonParse(object.get(JsonKeyName.EXPR));
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
	}
	@Override
	String p4_to_C() {
		String code = member;
		code = expr.p4_to_C()+"."+code;
		return code;
	}
	@Override
	String p4_to_Boogie() {
//		if(type instanceof Type_Method) {
//			String code = expr.p4_to_Boogie()+"."+member;
//			return code;
//		}
		if(type.Node_Type.equals("Type_Method") && (member.equals("extract") 
				|| member.equals("emit"))) {
			return member;
		}
		else if(type.Node_Type.equals("Type_Method") && member.equals("setInvalid")) {
			String code = "setInvalid";
			code += "(";
			code += expr.p4_to_Boogie();
			code += ")";
			return code;
		}
		else if(type.Node_Type.equals("Type_Method") && member.equals("setValid")) {
			Parser.getInstance().addProcedureSetValidHeader(expr.p4_to_Boogie());
			String code = "setValid";
			code += "(";
			code += expr.p4_to_Boogie();
			code += ")";
			return code;
		}
		else if(type.Node_Type.equals("Type_Method") && member.equals("isValid")) {
			String code = "isValid";
			code += "[";
			code += expr.p4_to_Boogie();
			code += "]";
			return code;
		}
		else if(type.Node_Type.equals("Type_Method") && member.equals("lookahead")) {
			return member;
		}
		else if(type.Node_Type.equals("Type_Method") && member.equals("push_front")) {
			if(expr.Node_Type.equals("Member")) {
				Member m = (Member)expr;
				String code = m.type.getName()+"."+member;
				code += "(";
				code += expr.p4_to_Boogie();
				code += ")";
				return code;
			}
		}
		else if(type.Node_Type.equals("Type_Method") && (member.equals("push_front")||member.equals("pop_front"))) {
			if(expr.Node_Type.equals("Member")) {
				Member m = (Member)expr;
				String code = m.type.getName()+"."+member;
				code += "(";
				code += expr.p4_to_Boogie();
				code += ")";
				return code;
			}
		}
		else if(member.equals("apply")) {
			return expr.p4_to_Boogie()+"."+member;
		}
		else if(member.equals("next") && expr.Node_Type.equals("Member")) {
			Member m = (Member)expr;
			if(m.type.Node_Type.equals("Type_Stack")) {
				return expr.p4_to_Boogie();
			}
		}
		else if(member.equals("action_run") && type.Node_Type.equals("Type_ActionEnum")) {
			MethodCallExpression mce = (MethodCallExpression)expr;
			Member m = (Member)mce.method;
			return m.expr.p4_to_Boogie()+"."+member;
		}
		else {
			//Parser.getInstance().addModifiedGlobalVariable(expr.getTypeName()+"."+member);
			Parser.getInstance().addModifiedGlobalVariable("Heap");
			String code = "Heap["+expr.p4_to_Boogie()+", "+expr.getTypeName()+"."+member+"]";
			if(member.equals("last")) {
				code = "last["+expr.p4_to_Boogie()+"]";
			}
//			String code = expr.getTypeName()+"."+member;
//			code += "["+expr.p4_to_Boogie()+"]";
			return code;
		}
		return super.p4_to_Boogie();
	}
	@Override
	String getTypeName() {
		return type.getTypeName();
	}
	@Override
	String getName() {
		if(member.equals("next") && expr.Node_Type.equals("Member")) {
			return expr.getName()+"."+member;
		}
		return member;
	}
	@Override
	String addAssertStatement() {
		if(type.Node_Type.equals("Type_Header")) {
			Context ctx = Parser.getInstance().getContext();
//			BoolExpr expr = ctx.mkBool(false);
//			ArrayList<BoogieProcedure> states = Parser.getInstance().fromHeaderToParserStates(this.p4_to_Boogie()); 
//			if(states!=null) {
//				for(BoogieProcedure procedure:states) {
//					BoolExpr preCondition = procedure.getPreCondition();
//					if(preCondition!=null)
//						expr = ctx.mkOr(expr, preCondition);
//				}
//			}
			BoolExpr condition = Parser.getInstance().getSetValidHeaderCondition(this.p4_to_Boogie());
			
			String statement = addIndent()+"assert(isValid["+this.p4_to_Boogie()+"]);\n";
//			if(states!=null)
			if(condition!=null) {
//				System.out.println("****setValid****");
//				System.out.println(condition);
				Parser.getInstance().addBoogieAssertStatement(statement, this.p4_to_Boogie(), ctx.mkNot(condition));
			}
			else
				Parser.getInstance().addBoogieAssertStatement(statement, this.p4_to_Boogie());
//			else
//				Parser.getInstance().addBoogieStatement(statement);
			return statement;
		}
		return expr.addAssertStatement();
	}
	@Override
	HashSet<String> getBranchVariables() {
		HashSet<String> variables = new HashSet<>();
		variables.add(p4_to_Boogie());
		return variables;
	}
	@Override
	BitVecExpr getBitVecExpr() {
		String exprName = p4_to_Boogie();
		exprName = exprName.replace('[', '_');
		exprName = exprName.replace("], ", "_");
		exprName = exprName.replace(", ", "_");
		exprName = exprName.replace(']', '_');
		exprName = exprName.replace('.', '_');
		String typeName = type.getTypeName();
		if(typeName.contains("bv")) {
			int size = Integer.valueOf(typeName.substring(2));
			BitVecExpr bv = Parser.getInstance().getContext().mkBVConst(exprName, size);
			return bv;
		}
		return null;
	}
	@Override
	BoolExpr getCondition() {
		String exprName = p4_to_Boogie();
		exprName = exprName.replace('[', '_');
		exprName = exprName.replace("], ", "_");
		exprName = exprName.replace(", ", "_");
		exprName = exprName.replace(']', '_');
		exprName = exprName.replace('.', '_');
		BoolExpr expr = Parser.getInstance().getContext().mkBoolConst(exprName);
		return expr;
	}
}

class ListExpression extends Expression {

}

class DefaultExpression extends Expression {
	
}