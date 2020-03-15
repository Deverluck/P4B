package verification.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class DataStructure extends Node{

}

class Declaration_Instance extends DataStructure {
	
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
		type = Parser.jsonParse(object.get(JsonKeyName.TYPE));
		addChild(type);
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