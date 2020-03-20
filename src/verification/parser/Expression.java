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
		method = Parser.jsonParse(object.get(JsonKeyName.METHOD));
		ArrayNode arguments_node = (ArrayNode)object.get(JsonKeyName.ARGUMENTS).get(JsonKeyName.VEC);
		if(arguments_node != null) {
			for(JsonNode node : arguments_node) {
				arguments.add(Parser.jsonParse(node));
			}
		}
		ArrayNode typeArguments_node = (ArrayNode)object.get(JsonKeyName.TYPEARGUMENTS).get(JsonKeyName.VEC);
		if(typeArguments_node != null) {
			for(JsonNode node : typeArguments_node) {
				typeArguments.add(Parser.jsonParse(node));
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
}

class PathExpression extends Expression {
	Node path;
	public PathExpression() {
		super();
	}
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		path = Parser.jsonParse(object.get(JsonKeyName.PATH));
		addChild(path);
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
			select.add(Parser.jsonParse(node));
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
						list.add(Parser.jsonParse(tmpJsonNode));
				}
				// single key
				else {
					list.add(Parser.jsonParse(node.get(JsonKeyName.KEYSET)));
					//TODO Add support for Mask
				}
				cases_value.add(list);
				cases.add(Parser.jsonParse(node.get(JsonKeyName.STATE)));
			}
			// default
			else
				default_case = Parser.jsonParse(node.get(JsonKeyName.STATE));
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
}

class ExpressionValue extends Expression {
	Node expression;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		expression = Parser.jsonParse(object.get(JsonKeyName.EXPRESSION));
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
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		member = object.get(JsonKeyName.MEMBER).asText();
		expr = Parser.jsonParse(object.get(JsonKeyName.EXPR));
	}
	@Override
	String p4_to_C() {
		String code = member;
		code = expr.p4_to_C()+"."+code;
		return code;
	}
}

class ListExpression extends Expression {
	
}