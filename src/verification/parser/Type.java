package verification.parser;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Type extends Node {
	String name;
}

//Header
class Type_Header extends Type {
	ArrayList<StructField> fields;
	public Type_Header() {
		fields = new ArrayList<>();
	}
	int length() {
		int len = 0;
		for(StructField field : fields) {
			len += field.len;
		}
		return len;
	}
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		ArrayNode field_array= (ArrayNode)object.get(JsonKeyName.FIELDs).get(JsonKeyName.VEC);
		for(JsonNode field_node : field_array) {
			fields.add((StructField)Parser.getInstance().jsonParse(field_node));
		}
		Parser.getInstance().addHeader(this);
	}
	@Override
	String p4_to_C_declare() {
		String code = "typedef struct {\n";
		for(StructField field : fields) {
			code += field.p4_to_C()+";\n";
		}
		code += "} "+name+";\n";
		return code;
	}
	@Override
	String p4_to_Boogie() {
//		String code = "type "+name+" = bv"+length()+";\n";
//		int start = 0;
//		for(StructField field : fields) {
//			code += "function "+name+"."+field.name;
//			code += "(header : "+name+") returns (bv"+field.len+")";
//			code += " {header["+(start+field.len)+":"+start+"]}\n";
//			start += field.len;
//		}
		String code = "\n// Header "+name+"\n";
		code += "type "+name+";\n";
		for(StructField field : fields) {
//			Parser.getInstance().addBoogieGlobalVariable(name+"."+field.name);
			code += "var "+name+"."+field.name+":["+name+"]"+field.p4_to_Boogie()+";\n";
		}
		return code;
	}
	@Override
	String getTypeName() {
		return name;
	}
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

	@Override
	String p4_to_Boogie() {
		if(enable) {
			return name;
		}
		return "";
	}
}

class Type_Method extends Type {

}

class Type_Name extends Type {
	Node path;
	String name;

	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		path = Parser.getInstance().jsonParse(object.get(JsonKeyName.PATH));
		addChild(path);
		name = path.p4_to_C();
	}
	@Override
	String p4_to_C() {
		return path.p4_to_C();
	}
	@Override
	String p4_to_Boogie() {
		return path.p4_to_Boogie();
	}
	@Override
	String getTypeName() {
		return name;
	}
}

class Type_Package extends Type {

}

class Type_Struct extends Type {
	ArrayList<StructField> fields;
//	TypeVector fields;

	public Type_Struct() {
		fields = new ArrayList<>();
	}

	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		ArrayNode field_array= (ArrayNode)object.get(JsonKeyName.FIELDs).get(JsonKeyName.VEC);
		for(JsonNode field_node : field_array) {
			fields.add((StructField)Parser.getInstance().jsonParse(field_node));
		}
//		fields = (TypeVector)Parser.getInstance().jsonParse(object.get(JsonKeyName.FIELDs));
		Parser.getInstance().addStruct(this);
	}

//	@Override
//	String p4_to_C() {
//		return name;
//	}

	@Override
	String p4_to_C_declare() {
		String code = "typedef struct {\n";
		for(Node field : fields) {
			code += field.p4_to_C()+";\n";
		}
		code += "} "+name+";\n";
		return code;
	}

	@Override
	String p4_to_Boogie() {
		// TODO field may be bv, header, typedef
		String code = "\n// Struct "+name+"\n";
		code += "type "+name+";\n";
		for(StructField field : fields) {
//			Parser.getInstance().addBoogieGlobalVariable(name+"."+field.name);
			code += "var "+name+"."+field.name+":["+name+"]"+field.p4_to_Boogie()+";\n";
		}
		return code;
	}

	@Override
	String getTypeName() {
		return name;
	}
}

class Type_Table extends Type {

}

class Type_Typedef extends Type {
	Node type;
	int len;

	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
		addChild(type);
		len = -1;
		if(type instanceof Type_Bits) {
			Type_Bits tb= (Type_Bits)type;
			len = tb.size;
		}
		Parser.getInstance().addTypeDef(this);
	}

	@Override
	String p4_to_C_declare() {
		// TODO support bits of any length
		String code = "typedef uint64_t "+name+";\n";
		return code;
	}

	@Override
	String p4_to_Boogie() {
		String code = "type "+name+" = bv"+len+";\n";
		if(len != -1)
			return code;
		return "";
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

class Type_Enum extends Type {

}

class Type_Stack extends Type {

}

class Type_Action extends Type {

}

class TypeParameters extends Type {

}

class Type_ActionEnum extends Type {

}

class Type_State extends Type {

}

class Type_MatchKind extends Type {

}