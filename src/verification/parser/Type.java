package verification.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Type extends Node {
	String name;
}

class Type_Bits extends Type {
	int size;
	boolean isSigned;
	@Override
	boolean parse(ObjectNode object) {
		super.parse(object);
		size = object.get(JsonKeyName.SIZE).asInt();
		isSigned = object.get(JsonKeyName.ISSIGNED).asBoolean();
		return true;
	}
}

class Type_Action extends Type {
	
}

class TypeParameters extends Type {
	
}

class Type_ActionEnum extends Type {

}

class Type_Control extends Type {

}

class Type_Method extends Type {
	
}

class Type_Name extends Type {
	Node path;
	
	@Override
	boolean parse(ObjectNode object) {
		super.parse(object);
		path = Parser.jsonParse(object.get(JsonKeyName.PATH));
		addChild(path);
		return true;
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
	boolean parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		fields = Parser.jsonParse(object.get(JsonKeyName.FIELDs));
		return true;
	}
}

class Type_Table extends Type {
	
}

class Type_Typedef extends Type {
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

class Type_Unknown extends Type {
	
}

class Type_Error extends Type {
	
}

class Type_Extern extends Type {
	@Override
	boolean parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		return true;
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
	boolean parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		Node child = Parser.jsonParse(object.get(JsonKeyName.FIELDs));
		addChild(child);
		return true;
	}
}

class Type_Enum extends Type {
	
}

class Type_Parser extends Type {
	Node applyParams;
	
	@Override
	boolean parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		applyParams = Parser.jsonParse(object.get(JsonKeyName.APPLYPARAMS));
		return true;
	}
	
	@Override
	String p4_to_C(){
		if(enable) {
			return applyParams.p4_to_C();
		}
		return "";
	}
}