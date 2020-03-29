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

	@Override
	String p4_to_Boogie() {
		String code = value+"bv64";
		return code;
	}
}

class StructField extends DataStructure {
	String name;
	Node type;
	int len;

	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
		addChild(type);

		// get length
		if(type.Node_Type.equals("Type_Bits")) {
			Type_Bits tb = (Type_Bits)type;
			len = tb.size;
		}
		else if(type.Node_Type.equals("Type_Name")) {
			Type_Name tn = (Type_Name)type;
			len = Parser.getInstance().getTypeLength(tn.name);
		}
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

	@Override
	String p4_to_Boogie() {
		String code = "";
		if(type.Node_Type.equals("Type_Bits"))
			code = "bv"+len;
		else if(type.Node_Type.equals("Type_Name"))
			code = type.p4_to_Boogie();
		return code;
	}
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
	@Override
	String p4_to_Boogie() {
		return name;
	}
}

class BoolLiteral extends DataStructure {

}

class ArrayIndex extends DataStructure {

}

class Declaration_MatchKind extends DataStructure {

}

class StringLiteral extends DataStructure {

}

class NameMapProperty extends DataStructure {

}