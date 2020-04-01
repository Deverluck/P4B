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
			code += addIndent();
			String methodName = path.p4_to_Boogie();
			code += "call "+methodName+"();\n";
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

class SelectExpression extends Expression {
	ArrayList<Node> select; // may select more than one key
	ArrayList<ArrayList<Node>> cases_value;
	ArrayList<Node> cases;
	Node default_case;

	public SelectExpression() {
		super();
		select = new ArrayList<>();
		cases_value = new ArrayList<>();
		cases = new ArrayList<>();
		default_case = null;
	}

	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		ArrayNode select_vec = (ArrayNode)object.get(JsonKeyName.SELECT).get(JsonKeyName.COMPONENTS).get(JsonKeyName.VEC);
		for(JsonNode node : select_vec) {
			select.add(Parser.getInstance().jsonParse(node));
		}
		ArrayNode cases_vec = (ArrayNode)object.get(JsonKeyName.SELECTCASES).get(JsonKeyName.VEC);
		for(JsonNode node : cases_vec) {
			node = (JsonNode)node;
			if(!node.get(JsonKeyName.KEYSET).get(JsonKeyName.NODE_TYPE).asText().equals(JsonKeyName.DEFAULTEXPRESSION)) {
				ArrayList<Node> list = new ArrayList<>();
				// multiple keys
				if(node.get(JsonKeyName.KEYSET).has(JsonKeyName.COMPONENTS)) {
					ArrayNode tmpArrayNode = (ArrayNode)node.get(JsonKeyName.KEYSET).get(JsonKeyName.COMPONENTS).get(JsonKeyName.VEC);
					for(JsonNode tmpJsonNode : tmpArrayNode)
						list.add(Parser.getInstance().jsonParse(tmpJsonNode));
				}
				// single key
				else {
					list.add(Parser.getInstance().jsonParse(node.get(JsonKeyName.KEYSET)));
					//TODO Add support for Mask
				}
				cases_value.add(list);
				cases.add(Parser.getInstance().jsonParse(node.get(JsonKeyName.STATE)));
			}
			// default
			else
				default_case = Parser.getInstance().jsonParse(node.get(JsonKeyName.STATE));
		}
	}
	@Override
	String p4_to_C() {
		String code = "";
		int cnt1 = 0; // counter for keys (&&)
		int cnt2 = 0; // counter for cases (else if{})
		for(Node case_node : cases) {
			code += "if(";
			cnt1 = 0;
			for(Node select_node : select) {
				code += select_node.p4_to_C()+"=="+cases_value.get(cnt2).get(cnt1).p4_to_C();
				cnt1++;
				if(cnt1 < select.size())
					code += " && ";
			}
			code += "){\n"+case_node.p4_to_C(JsonKeyName.PARSERSTATE)+"}\n";
			cnt2++;
			if(cnt2 != cases.size())
				code += "else";
		}

		if(default_case != null) {
			if(cases.size()!=0)
				code += "else{\n" + default_case.p4_to_C(JsonKeyName.PARSERSTATE) + "}\n";
			else
				code += default_case.p4_to_C(JsonKeyName.PARSERSTATE);
		}
		// there may be no default case
		else if(cases.size()==1 && default_case == null) {
			code += "else{\n" + cases.get(0).p4_to_C(JsonKeyName.PARSERSTATE) + "}\n";
		}
		return code;
	}

	@Override
	String p4_to_Boogie() {
		// TODO Add support for Mask
		String code = "";
		int cnt1 = 0; // counter for keys (&&)
		int cnt2 = 0; // counter for cases (else if{})
		for(Node case_node : cases) {
			code += "	if(";
			cnt1 = 0;
			for(Node select_node : select) {
				// TODO deal with argument types (equal width)
				code += select_node.p4_to_Boogie()+" == ";
				Node caseValue = cases_value.get(cnt2).get(cnt1);
				if(caseValue instanceof Constant) {
					code += caseValue.p4_to_Boogie();
				}
				cnt1++;
				if(cnt1 < select.size())
					code += " && ";
			}
			code += "){\n";

			incIndent();
			code += case_node.p4_to_Boogie(JsonKeyName.PARSERSTATE)+"	}\n";
			decIndent();

			cnt2++;
			if(cnt2 != cases.size())
				code += "	else";
		}

//		if(default_case != null) {
//			if(cases.size()!=0)
//				code += "else{\n" + default_case.p4_to_C(JsonKeyName.PARSERSTATE) + "}\n";
//			else
//				code += default_case.p4_to_C(JsonKeyName.PARSERSTATE);
//		}
//		// there may be no default case
//		else if(cases.size()==1 && default_case == null) {
//			code += "else{\n" + cases.get(0).p4_to_C(JsonKeyName.PARSERSTATE) + "}\n";
//		}
		return code;
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
		if(member.equals("extract")) {
			return member;
		}
		else if(member.equals("isValid")) {
			String code = "isValid";
			code += "[";
			code += expr.p4_to_Boogie();
			code += "]";
			return code;
		}
		else if(member.equals("apply")) {
			return expr.p4_to_Boogie()+"."+member;
		}
		else if(member.equals("emit")) {
			// TODO
			return member;
		}
		else {
			Parser.getInstance().addModifiedGlobalVariable(expr.getTypeName()+"."+member);
			String code = expr.getTypeName()+"."+member;
			code += "["+expr.p4_to_Boogie()+"]";
			return code;
		}
	}
	@Override
	String getTypeName() {
		return type.getTypeName();
	}
	@Override
	String getName() {
		return member;
	}
}

class ListExpression extends Expression {

}