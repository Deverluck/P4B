package verification.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Type extends Node {
	String name;
}

class Type_Bits extends Type {
	int size;
	boolean isSigned;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		size = object.get(JsonKeyName.SIZE).asInt();
		isSigned = object.get(JsonKeyName.ISSIGNED).asBoolean();
	}
}

class Type_Action extends Type {
	
}

class TypeParameters extends Type {
	
}

class Type_ActionEnum extends Type {

}

class Type_Control extends Type {
	Node applyParams;
	
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		applyParams = Parser.getInstance().jsonParse(object.get(JsonKeyName.APPLYPARAMS));
	}
	
	@Override
	String p4_to_C(){
		if(enable) {
			return applyParams.p4_to_C();
		}
		return "";
	}
}

class Type_Method extends Type {
	
}

class Type_Name extends Type {
	Node path;
	
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		path = Parser.getInstance().jsonParse(object.get(JsonKeyName.PATH));
		addChild(path);
	}
	@Override
	String p4_to_C() {
		return path.p4_to_C();
	}
}

class Type_Package extends Type {
	
}

class Type_Struct extends Type {
	Node fields;
	
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		fields = Parser.getInstance().jsonParse(object.get(JsonKeyName.FIELDs));
	}
}

class Type_Table extends Type {
	
}

class Type_Typedef extends Type {
	Node type;
	
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
		addChild(type);
	}
}

class Type_Unknown extends Type {
	
}

class Type_Error extends Type {
	
}

class Type_Extern extends Type {
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
	}
	@Override
	String p4_to_C() {
		if(isEnable())
			return name;
		return "";
	}
}

// Header
class Type_Header extends Type {
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		Node child = Parser.getInstance().jsonParse(object.get(JsonKeyName.FIELDs));
		addChild(child);
	}
}

class Type_Enum extends Type {
	
}

class Type_Parser extends Type {
	Node applyParams;
	
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		applyParams = Parser.getInstance().jsonParse(object.get(JsonKeyName.APPLYPARAMS));
	}
	
	@Override
	String p4_to_C(){
		if(enable) {
			return applyParams.p4_to_C();
		}
		return "";
	}
}

class Type_Stack extends Type {
	
}