package verification.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TypeVector extends Node{
	@Override
	void parse(ObjectNode object) {
		ArrayNode jsonArray = (ArrayNode)object.get(JsonKeyName.VEC);
		for(Object jsonobject : jsonArray) {
			Node node = Parser.getInstance().jsonParse((JsonNode)jsonobject);
			addChild(node);
		}
	}

	@Override
	boolean isVector() {
		return true;
	}

	@Override
	String p4_to_C() {
		switch (T) {
		case JsonKeyName.PARAMETER:
			return parameter_p4_to_C();

		default:
			break;
		}
		String code = "";
		for(Node node : children) {
			code += node.p4_to_C();
		}
		return code;
	}

	@Override
	String p4_to_C(String arg) {
		switch (T) {
		case JsonKeyName.PARAMETER:
			return parameter_p4_to_C(arg);

		default:
			break;
		}
		String code = "";
		for(Node node : children) {
			code += node.p4_to_C(arg);
		}
		return code;
	}

	String parameter_p4_to_C() {
		String code = "(";
		int size = children.size();
		int cnt = 0;
		for(Node node : children) {
			cnt += 1;
			code += node.p4_to_C();
			if(cnt != size)
				code += ", ";
		}
		code += ")";
		return code;
	}

	String parameter_p4_to_C(String arg) {
		String code = "(";
		int size = children.size();
		int cnt = 0;
		for(Node node : children) {
			cnt += 1;
			code += node.p4_to_C(arg);
			if(cnt != size)
				code += ", ";
		}
		code += ")";
		return code;
	}

	// *********** Boogie **********
	@Override
	String p4_to_Boogie() {
		switch (T) {
		case JsonKeyName.PARAMETER:
			return parameter_p4_to_Boogie();

		default:
			break;
		}
		String code = "";
		for(Node node : children) {
			code += node.p4_to_Boogie();
		}
		return code;
	}

	@Override
	String p4_to_Boogie(String arg) {
		switch (T) {
		case JsonKeyName.PARAMETER:
			return parameter_p4_to_Boogie(arg);

		default:
			break;
		}
		String code = "";
		for(Node node : children) {
			code += node.p4_to_Boogie(arg);
		}
		return code;
	}

	String parameter_p4_to_Boogie() {
		String code = "(";
		int size = children.size();
		int cnt = 0;
		for(Node node : children) {
			cnt += 1;
			code += node.p4_to_Boogie();
			if(cnt != size)
				code += ", ";
		}
		code += ")";
		return code;
	}

	String parameter_p4_to_Boogie(String arg) {
		String code = "(";
		int size = children.size();
		int cnt = 0;
		for(Node node : children) {
			cnt += 1;
			code += node.p4_to_Boogie(arg);
			if(cnt != size)
				code += ", ";
		}
		code += ")";
		return code;
	}
}
