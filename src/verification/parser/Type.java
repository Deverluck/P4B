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
	@Override
	String p4_to_C() {
		return size+"";
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
			return name+applyParams.p4_to_C();
		}
		return "";
	}
	
	@Override
	String p4_to_C(String arg) {
		if(enable && arg.equals(JsonKeyName.METHODCALL)) {
			return name+applyParams.p4_to_C(arg);
		}
		return "";
	}
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
			return name+applyParams.p4_to_C();
		}
		return "";
	}
	
	@Override
	String p4_to_C(String arg) {
		if(enable && arg.equals(JsonKeyName.METHODCALL)) {
			return name+applyParams.p4_to_C(arg);
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
	TypeVector fields;
	
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		fields = (TypeVector)Parser.getInstance().jsonParse(object.get(JsonKeyName.FIELDs));
		Parser.getInstance().addStruct(this);
	}
//	@Override
//	String p4_to_C() {
//		return name;
//	}
	@Override
	String declare() {
		String code = "typedef struct {\n";
		for(Node field : fields.children) {
			code += field.p4_to_C()+";\n";
		}
		code += "} "+name+";\n";
		return code;
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
		Parser.getInstance().addTypeDef(this);
	}
	@Override
	String declare() {
		// TODO support bits of any length
		String code = "typedef uint64_t "+name+";\n";
		return code;
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
	TypeVector fields;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		fields = (TypeVector)Parser.getInstance().jsonParse(object.get(JsonKeyName.FIELDs));
		Parser.getInstance().addHeader(this);
	}
	@Override
	String declare() {
		String code = "typedef struct {\n";
		for(Node field : fields.children) {
			code += field.p4_to_C()+";\n";
		}
		code += "} "+name+";\n";
		return code;
	}
}

class Type_Enum extends Type {
	
}

class Type_Stack extends Type {
	
}