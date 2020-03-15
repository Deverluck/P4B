package verification.parser;

import net.sf.json.JSONObject;

public class Type extends Node {
	String name;
}

class Type_Bits extends Type {
	int size;
	boolean isSigned;
	@Override
	boolean parse(JSONObject object) {
		super.parse(object);
		size = object.getInt(JsonKeyName.SIZE);
		isSigned = object.getBoolean(JsonKeyName.ISSIGNED);
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
	boolean parse(JSONObject object) {
		super.parse(object);
		path = Parser.jsonParse(object.getJSONObject(JsonKeyName.PATH));
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
	boolean parse(JSONObject object) {
		super.parse(object);
		name = object.getString(JsonKeyName.NAME);
		fields = Parser.jsonParse(object.getJSONObject(JsonKeyName.FIELDs));
		return true;
	}
}

class Type_Table extends Type {
	
}

class Type_Typedef extends Type {
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

class Type_Unknown extends Type {
	
}

class Type_Error extends Type {
	
}

class Type_Extern extends Type {
	@Override
	boolean parse(JSONObject object) {
		super.parse(object);
		name = object.getString(JsonKeyName.NAME);
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
	boolean parse(JSONObject object) {
		super.parse(object);
		name = object.getString(JsonKeyName.NAME);
		Node child = Parser.jsonParse(object.getJSONObject(JsonKeyName.FIELDs));
		addChild(child);
		return true;
	}
}

class Type_Enum extends Type {
	
}

class Type_Parser extends Type {
	Node applyParams;
	
	@Override
	boolean parse(JSONObject object) {
		super.parse(object);
		name = object.getString(JsonKeyName.NAME);
		applyParams = Parser.jsonParse(object.getJSONObject(JsonKeyName.APPLYPARAMS));
		return true;
	}
	
	@Override
	String p4_to_C(){
		if(enable) {
			return applyParams.p4_to_C();
		}
		return "";
	}
	
//	@Override
//	String p4_to_C() {
//		String code = name+"()(";
//		
//		code += ")";
//		return name+"()";
//	}
}