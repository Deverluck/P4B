package verification.p4verifier;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Type extends Node {
	String name;
	@Override
	String getTypeName() {
		if(name==null)
			return "";
		return name;
	}
	@Override
	String getName() {
		if(name==null)
			return "";
		return name;
	}
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
//	@Override
//	String p4_to_Boogie() {
//		String code = "\n// Header "+name+"\n";
////		code += "type "+name+";\n";
//		for(StructField field : fields) {
////			Parser.getInstance().addBoogieGlobalVariable(name+"."+field.name);
//			String var = name+"."+field.name;
////			code += "const unique "+var+":Field "+field.p4_to_Boogie()+";\n";
//			code += "var "+var+":[Ref]"+field.p4_to_Boogie()+";\n";
//			Parser.getInstance().addBoogieGlobalVariable(var);
////			code += "var "+var+":["+name+"]"+field.p4_to_Boogie()+";\n";
//		}
//		return code;
//	}
	@Override
	String p4_to_Boogie() {
		if(Parser.getInstance().getCommands().ifUseCorral()) {
			String code = "\n// Header "+name+"\n";
			for(StructField field : fields) {
				String var = name+"."+field.name;
				code += "var "+var+":[Ref]"+field.p4_to_Boogie()+";\n";
				Parser.getInstance().addBoogieGlobalVariable(var);
			}
			return super.p4_to_Boogie();
		}
		else {
			String code = "\n// Header "+name+"\n";
			for(StructField field : fields) {
				String var = name+"."+field.name;
				code += "const unique "+var+":Field "+field.p4_to_Boogie()+";\n";
			}
			return code;
		}
	}
	@Override
	String p4_to_Boogie(String arg) {
		String code = "\n// Header "+name+"\n";
		// there may be assignments like header1:=header2;
		
		if(Parser.getInstance().getCommands().ifUseCorral()) {
			code += "var "+arg+":Ref;\n";
			Parser.getInstance().addBoogieGlobalVariable(arg);
			for(StructField field : fields) {
				code += field.p4_to_Boogie(arg);
			}
		}
		else {
			return "";
		}
		return code;
	}
	@Override
	String getTypeName() {
		return name;
	}
}

class Type_Struct extends Type {
	ArrayList<StructField> fields;
	public Type_Struct() {
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

//	@Override
//	String p4_to_Boogie() {
//		// TODO field may be bv, header, typedef
//		String code = "\n// Struct "+name+"\n";
////		code += "type "+name+";\n";
//		for(StructField field : fields) {
//			String var = name+"."+field.name;
//			Parser.getInstance().addBoogieGlobalVariable(var);
////			code += "var "+var+":["+name+"]"+field.p4_to_Boogie()+";\n";
////			code += "const unique "+name+"."+field.name+":Field "+field.p4_to_Boogie()+";\n";
//			if(field.type.Node_Type.equals("Type_Stack")) {
//				code += "var "+name+"."+field.name+":[Ref]HeaderStack;\n";
////				code += "const unique "+name+"."+field.name+":Field "+"HeaderStack"+";\n";
//				Parser.getInstance().addBoogieGlobalDeclaration(field.type.p4_to_Boogie());
//			}
//			else {
//				code += "var "+name+"."+field.name+":[Ref]"+field.p4_to_Boogie()+";\n";
////				code += "const unique "+name+"."+field.name+":Field "+field.p4_to_Boogie()+";\n";
//			}
//		}
//		return code;
//	}
	
	@Override
	String p4_to_Boogie() {
		if(Parser.getInstance().getCommands().ifUseCorral()) {
			String code = "\n// Struct "+name+"\n";
			boolean spec = true;
			String instanceName = "";
			if(name.equals("headers")) {
				instanceName = "hdr";
			}
			else if(name.equals("metadata")) {
				instanceName = "meta";
			}
			else if(name.equals("standard_metadata_t")) {
				instanceName = "standard_metadata";
			}
			else {
				spec = false;
			}
			
			if(spec) {
				for(StructField field:fields) {
					code += field.p4_to_Boogie(instanceName);
				}
				return code;
			}else {
				return "";
			}
		}
		else {
			// Use Boogie as backend
			String code = "\n// Struct "+name+"\n";
			for(StructField field : fields) {
				String var = name+"."+field.name;
				Parser.getInstance().addBoogieGlobalVariable(var);
				if(field.type.Node_Type.equals("Type_Stack")) {
					code += "const unique "+name+"."+field.name+":Field "+"HeaderStack"+";\n";
					Parser.getInstance().addBoogieGlobalDeclaration(field.type.p4_to_Boogie());
				}
				else {
					code += "const unique "+name+"."+field.name+":Field "+field.p4_to_Boogie()+";\n";
				}
			}
			return code;
		}
		
//		// TODO field may be bv, header, typedef
//		String code = "\n// Struct "+name+"\n";
//		for(StructField field : fields) {
//			String var = name+"."+field.name;
//			Parser.getInstance().addBoogieGlobalVariable(var);
////			code += "var "+var+":["+name+"]"+field.p4_to_Boogie()+";\n";
////			code += "const unique "+name+"."+field.name+":Field "+field.p4_to_Boogie()+";\n";
//			if(field.type.Node_Type.equals("Type_Stack")) {
//				code += "var "+name+"."+field.name+":[Ref]HeaderStack;\n";
////				code += "const unique "+name+"."+field.name+":Field "+"HeaderStack"+";\n";
//				Parser.getInstance().addBoogieGlobalDeclaration(field.type.p4_to_Boogie());
//			}
//			else {
//				code += "var "+name+"."+field.name+":[Ref]"+field.p4_to_Boogie()+";\n";
////				code += "const unique "+name+"."+field.name+":Field "+field.p4_to_Boogie()+";\n";
//			}
//		}
//		return code;
	}
	
	@Override
	String p4_to_Boogie(String arg) {
		String code = "";
		for(StructField field : fields) {
			code += field.p4_to_Boogie(arg);
		}
		return code;
	}
	
	@Override
	String getTypeName() {
		return name;
	}
}

class Type_Stack extends Type {
	Node elementType;
	Constant size;
	
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		elementType = Parser.getInstance().jsonParse(object.get(JsonKeyName.ELEMENTTYPE));
		size = (Constant)Parser.getInstance().jsonParse(object.get(JsonKeyName.SIZE));
		name = elementType.getTypeName()+"."+size.value;
		Parser.getInstance().addStack(this);
	}
	
	@Override
	String getTypeName() {
		return elementType.getTypeName();
	}
	
	void addPushFront() {
		BoogieProcedure pushFront = new BoogieProcedure(name+".push_front");
		Parser.getInstance().addProcedure(pushFront);
		Parser.getInstance().setCurrentProcedure(pushFront);
		pushFront.declare = "procedure {:inline 1} "+pushFront.name+"(stack:"+"HeaderStack"+", count:int)\n";
//		pushFront.declare = "procedure {:inline 1} "+pushFront.name+"(stack:"+name+", count:int)\n";
		pushFront.updateModifies("isValid");
		pushFront.updateModifies("stack.index");
		Type_Header th = Parser.getInstance().getHeader(elementType.getTypeName());
		for(StructField field:th.fields) {
			pushFront.updateModifies(th.name+"."+field.name);
		}
		pushFront.localVariables.put("i", "var i:int;\n");
		incIndent();
		Parser.getInstance().addBoogieStatement(addIndent()+"i := size[stack]-1;\n");
		
		String whileStart = addIndent()+"while(i>=0)\n";
		incIndent();
		whileStart += addIndent()+"invariant i>=-1;\n";
		decIndent();
		whileStart += addIndent()+"{\n";
		String whileEnd = addIndent()+"}\n";
		BoogieIfStatement whileBlock = new BoogieIfStatement(whileStart, whileEnd);
		Parser.getInstance().addBoogieBlock(whileBlock);
		
		// ***while loop body***
		incIndent();
		
		// ***if statement***
		BoogieIfStatement ifBlock = new BoogieIfStatement(addIndent()+"if(i >= count){\n", addIndent()+"}\n");
		Parser.getInstance().addBoogieBlock(ifBlock);
		incIndent();
		for(StructField field:th.fields) {
			String statement = addIndent();
			String mapName = th.name+"."+field.name;
			statement += mapName+"[stack[i]] := "+mapName+"[stack[i-count]];\n";
			Parser.getInstance().addBoogieStatement(statement);
		}
		decIndent();
		Parser.getInstance().popBoogieBlock();
		// *** if ends***
		
		// ***else statement***
		BoogieIfStatement elseBlock = new BoogieIfStatement(addIndent()+"else{\n", addIndent()+"}\n");
		Parser.getInstance().addBoogieBlock(elseBlock);
		incIndent();
		Parser.getInstance().addBoogieStatement(addIndent()+"isValid[stack[i]] := false;\n");
		decIndent();
		Parser.getInstance().popBoogieBlock();
		// ***else ends***
		
		Parser.getInstance().addBoogieStatement(addIndent()+"i := i-1;\n");
		decIndent();
		Parser.getInstance().popBoogieBlock();
		//***while loop ends***
		
		Parser.getInstance().addBoogieStatement(addIndent()+"stack.index[stack] := stack.index[stack]+count;\n");
		
		// *** if starts***
		BoogieIfStatement ifBlock2 = new BoogieIfStatement(addIndent()+"if(stack.index[stack]>size[stack]){\n", addIndent()+"}\n");
		Parser.getInstance().addBoogieBlock(ifBlock2);
		incIndent();
		Parser.getInstance().addBoogieStatement(addIndent()+"stack.index[stack] := size[stack];\n");
		decIndent();
		Parser.getInstance().popBoogieBlock();
		// *** if ends***
		decIndent();
	}
	
	void addPopFront() {
		BoogieProcedure popFront = new BoogieProcedure(name+".pop_front");
		Parser.getInstance().addProcedure(popFront);
		Parser.getInstance().setCurrentProcedure(popFront);
		popFront.declare = "procedure {:inline 1} "+popFront.name+"(stack:"+"HeaderStack"+", count:int)\n";
//		popFront.declare = "procedure {:inline 1} "+popFront.name+"(stack:"+name+", count:int)\n";
		popFront.updateModifies("isValid");
		popFront.updateModifies("stack.index");
		Type_Header th = Parser.getInstance().getHeader(elementType.getTypeName());
		for(StructField field:th.fields) {
			popFront.updateModifies(th.name+"."+field.name);
		}
		popFront.localVariables.put("i", "var i:int;\n");
		incIndent();
		Parser.getInstance().addBoogieStatement(addIndent()+"i := 0;\n");
		
		String whileStart = addIndent()+"while(i<size[stack])\n";
		incIndent();
		whileStart += addIndent()+"invariant i<=size[stack];\n";
		decIndent();
		whileStart += addIndent()+"{\n";
		String whileEnd = addIndent()+"}\n";
		BoogieIfStatement whileBlock = new BoogieIfStatement(whileStart, whileEnd);
		Parser.getInstance().addBoogieBlock(whileBlock);
		
		// ***while loop body***
		incIndent();
		
		// ***if statement***
		BoogieIfStatement ifBlock = new BoogieIfStatement(addIndent()+"if(i+count < size[stack]){\n", addIndent()+"}\n");
		Parser.getInstance().addBoogieBlock(ifBlock);
		incIndent();
		for(StructField field:th.fields) {
			String statement = addIndent();
			String mapName = th.name+"."+field.name;
			statement += mapName+"[stack[i]] := "+mapName+"[stack[i+count]];\n";
			Parser.getInstance().addBoogieStatement(statement);
		}
		decIndent();
		Parser.getInstance().popBoogieBlock();
		// *** if ends***
		
		// ***else statement***
		BoogieIfStatement elseBlock = new BoogieIfStatement(addIndent()+"else{\n", addIndent()+"}\n");
		Parser.getInstance().addBoogieBlock(elseBlock);
		incIndent();
		Parser.getInstance().addBoogieStatement(addIndent()+"isValid[stack[i]] := false;\n");
		decIndent();
		Parser.getInstance().popBoogieBlock();
		// ***else ends***
		
		Parser.getInstance().addBoogieStatement(addIndent()+"i := i+1;\n");
		decIndent();
		Parser.getInstance().popBoogieBlock();
		//***while loop ends***
		
		// *** if starts***
		BoogieIfStatement ifBlock2 = new BoogieIfStatement(addIndent()+"if(stack.index[stack]>count){\n", addIndent()+"}\n");
		Parser.getInstance().addBoogieBlock(ifBlock2);
		incIndent();
		Parser.getInstance().addBoogieStatement(addIndent()+"stack.index[stack] := stack.index[stack]-count;\n");
		decIndent();
		Parser.getInstance().popBoogieBlock();
		// *** if ends***
		
		// ***else starts***
		BoogieIfStatement elseBlock2 = new BoogieIfStatement(addIndent()+"else{\n", addIndent()+"}\n");
		Parser.getInstance().addBoogieBlock(elseBlock2);
		incIndent();
		Parser.getInstance().addBoogieStatement(addIndent()+"stack.index[stack] := 0;\n");
		decIndent();
		Parser.getInstance().popBoogieBlock();
		// ***else ends***
		decIndent();
	}
	
	void addSizeConstrain() {
		BoogieProcedure constrain = new BoogieProcedure(name+".constrain");
		Parser.getInstance().addProcedure(constrain);
		Parser.getInstance().setCurrentProcedure(constrain);
		incIndent();
		constrain.declare = "procedure "+name+".constrain();\n";
		constrain.declare += addIndent()+"ensures (forall stack:"+name+"::size[stack]=="+size.value+");\n";
		constrain.implemented = false;
		decIndent();
		
		BoogieProcedure mainProcedure = Parser.getInstance().getMainProcedure();
		if(!mainProcedure.childrenNames.contains(constrain.name)) {
			incIndent();
			String statement = addIndent()+"call "+constrain.name+"();\n";
			decIndent();
			Parser.getInstance().addMainPreBoogieStatement(statement);
			mainProcedure.childrenNames.add(constrain.name);
		}
	}
	
	@Override
	String p4_to_Boogie() {
//		addPushFront();
//		addPopFront();
//		addSizeConstrain();
//		
//		String code = "\n// Header Stack: "+getTypeName()+" "+size.value+"\n";
//		code += "type "+name+"=[int]"+getTypeName()+";\n";
//		code += "var "+getTypeName()+".last:["+name+"]"+getTypeName()+";\n";
//		return code;
		return super.p4_to_Boogie();
	}
	@Override
	String p4_to_Boogie(String arg) {
		String code = "";
		Type_Header header = Parser.getInstance().getHeader(elementType.p4_to_Boogie());
		if(header!=null) {
			code += header.p4_to_Boogie(arg+".last");
			for(int i = 0; i < size.value; i++) {
				code += header.p4_to_Boogie(arg+"."+i);
			}
		}
		return code;
	}
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
	@Override
	String p4_to_Boogie() {
		return "bv"+size;
	}
	@Override
	String getTypeName() {
		return "bv"+size;
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
		
		// get the second parameter which should represent headers
		if(applyParams instanceof ParameterList) {
			ParameterList pl = (ParameterList)applyParams;
			Parser.getInstance().setHeadersName(((TypeVector)pl.parameters).children.get(1).getTypeName());
		}
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

class Type_Name extends Type {
	Node path;
	String name;

	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		path = Parser.getInstance().jsonParse(object.get(JsonKeyName.PATH));
		addChild(path);
		name = path.getTypeName();
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

class Type_Specialized extends Type {
	Node baseType;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		baseType = Parser.getInstance().jsonParse(object.get(JsonKeyName.BASETYPE));
	}
	@Override
	String getTypeName() {
		return baseType.getTypeName();
	}
}

class Type_Package extends Type {

}

class Type_Table extends Type {

}

class Type_Method extends Type {

}

class Type_Unknown extends Type {

}

class Type_Error extends Type {

}

class Type_Enum extends Type {

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

class Type_InfInt extends Type {

}

class Type_Boolean extends Type {

}

class Type_SpecializedCanonical extends Type {
	
}

class Type_Set extends Type{
	Node elementType;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		elementType = Parser.getInstance().jsonParse(object.get(JsonKeyName.ELEMENTTYPE));
	}
}