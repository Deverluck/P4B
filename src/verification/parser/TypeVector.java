package verification.parser;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TypeVector extends Node{
	@Override
	boolean parse(JSONObject object) {
		// TODO Auto-generated method stub
		JSONArray jsonArray = object.getJSONArray(JsonKeyName.VEC);
		for(Object jsonobject : jsonArray.toArray()) {
			Node node = Parser.jsonParse((JSONObject)jsonobject);
			addChild(node);
		}
		return true;
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
		System.out.println(size);
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
