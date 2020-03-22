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
}
