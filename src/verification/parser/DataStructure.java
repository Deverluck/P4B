package verification.parser;

import net.sf.json.JSONObject;

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
	boolean parse(JSONObject object) {
		super.parse(object);
		name = object.getString(JsonKeyName.NAME);
		type = Parser.jsonParse(object.getJSONObject(JsonKeyName.TYPE));
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
	boolean parse(JSONObject object) {
		super.parse(object);
		name = object.getString(JsonKeyName.NAME);
		absolute = object.getBoolean(JsonKeyName.ABSOLUTE);
		return true;
	}
	@Override
	String p4_to_C() {
		return name;
	}
}