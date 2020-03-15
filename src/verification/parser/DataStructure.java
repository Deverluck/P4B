package verification.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class DataStructure extends Node{

}

class Declaration_Instance extends DataStructure {
	
}

class Declaration_Variable extends DataStructure {
	
}

class Constant extends DataStructure {
	
}

class BoolLiteral extends DataStructure {
	
}

class ArrayIndex extends DataStructure {
	
}

class StructField extends DataStructure {
	String name;
	Node type;
	
	@Override
	boolean parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		type = Parser.jsonParse(object.get(JsonKeyName.TYPE));
		addChild(type);
		return true;
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
	boolean parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		absolute = object.get(JsonKeyName.ABSOLUTE).asBoolean();
		return true;
	}
	@Override
	String p4_to_C() {
		return name;
	}
}