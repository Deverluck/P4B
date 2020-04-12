package verification.parser;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
			methodName = "packet_in."+methodName+".headers."+arguments.get(0).getName();
		}
		else if(methodName.equals("emit")) {
			methodName = "packet_out."+methodName+".headers."+arguments.get(0).getName();
		}
		else if(methodName.length()>10 && methodName.substring(0, 11).equals("setInvalid(")) {
			methodName = "isValid["+methodName.substring(11, methodName.length()-1)+"] := false";
			Parser.getInstance().getCurrentProcedure().updateModifies("isValid");
			return methodName;
		}
		// deal with isValid()
		else if(methodName.length()>8 && methodName.substring(0, 8).equals("isValid[")) {
			return methodName;
		}
//		System.out.println(Parser.getInstance().getCurrentProcedure().name+":"+methodName);
		Parser.getInstance().getCurrentProcedure().childrenNames.add(methodName);
		code = methodName+"(";
		int cnt = 0;
		if(!arguments.isEmpty()) {
			for(Node node : arguments) {
				cnt++;
				code += node.p4_to_Boogie();
				if(cnt < arguments.size())
					code += ", ";
			}
		}
		code = code + ")";
		return code;
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
		return path.p4_to_Boogie();
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

}

class Cast extends Expression {

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
		else if(type.Node_Type.equals("Type_Method") && member.equals("isValid")) {
			String code = "isValid";
			code += "[";
			code += expr.p4_to_Boogie();
			code += "]";
			return code;
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
		else {
			Parser.getInstance().addModifiedGlobalVariable(expr.getTypeName()+"."+member);
			String code = expr.getTypeName()+"."+member;
			code += "["+expr.p4_to_Boogie()+"]";
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
			String statement = addIndent()+"assert(isValid["+this.p4_to_Boogie()+"]);\n";
			Parser.getInstance().addBoogieStatement(statement);
			return statement;
		}
		return expr.addAssertStatement();
	}
}

class ListExpression extends Expression {

}