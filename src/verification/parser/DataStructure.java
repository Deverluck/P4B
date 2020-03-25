package verification.parser;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DataStructure extends Node{

}

class Declaration_Instance extends DataStructure {
	// TODO support "VSS"
	String name;
	ArrayList<Node> arguments;
	public Declaration_Instance() {
		super();
		arguments = new ArrayList<>();
	}
	
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		ArrayNode argumentArray = (ArrayNode)object.get(JsonKeyName.ARGUMENTS).get(JsonKeyName.VEC);
		for(JsonNode arg : argumentArray) {
			arguments.add(Parser.getInstance().jsonParse(arg));
		}
	}
	
	@Override
	String p4_to_C() {
		String code = "";
		if(name.equals("main")) {
			code += "void main_method() {\n";
			for(Node node : arguments) {
				//TODO add declaration for arguments' arguments
				code += node.p4_to_C()+";\n";
			}
			code += "}\n";
		}
		return code;
	}
}

class Declaration_Variable extends DataStructure {
	
}

class Constant extends DataStructure {
	int value;
	int base;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		value = object.get(JsonKeyName.VALUE).asInt();
		base = object.get(JsonKeyName.BASE).asInt();
	}
	@Override
	String p4_to_C() {
		String code = value+"";
		return code;
	}
}

class BoolLiteral extends DataStructure {
	
}

class ArrayIndex extends DataStructure {
	
}

class StructField extends DataStructure {
	String name;
	Node type;
	
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
		addChild(type);
	}
	@Override
	String p4_to_C() {
		// TODO support bits for any length
		String code = "";
		if(type.Node_Type.equals("Type_Bits"))
			code = "uint64_t "+name+": "+type.p4_to_C();
		else if(type.Node_Type.equals("Type_Name"))
			code = type.p4_to_C()+" "+name;
		return code;
	}
}

class Declaration_MatchKind extends DataStructure {
	
}

class StringLiteral extends DataStructure {
	
}

class NameMapProperty extends DataStructure {
	
}

class Path extends DataStructure {
	String name;
	boolean absolute;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		absolute = object.get(JsonKeyName.ABSOLUTE).asBoolean();
	}
	@Override
	String p4_to_C() {
		return name;
	}
}