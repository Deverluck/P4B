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
	Node type;
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
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
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
	
	@Override
	String getTypeName() {
		return type.getTypeName();
	}
	@Override
	String p4_to_Boogie() {
		String code = "";
		if(getTypeName().equals("V1Switch")) {
			BoogieProcedure procedure = new BoogieProcedure(name);
			procedure.declare = "procedure {:inline 1} "+name+"()\n";
			Parser.getInstance().addProcedure(procedure);
			Parser.getInstance().setCurrentProcedure(procedure);
			incIndent();
			for(Node node : arguments) {
				String s = addIndent()+"call "+node.getName()+"();\n";
				procedure.childrenNames.add(node.getName());
				Parser.getInstance().addBoogieStatement(s);
				if(node.getTypeName().equals("Type_Parser")) {
					BoogieIfStatement ifBlock = new BoogieIfStatement(addIndent()+"if(drop != true){\n", 
							addIndent()+"}\n");
					Parser.getInstance().addBoogieBlock(ifBlock);
					incIndent();
				}
			}
			decIndent();
			Parser.getInstance().popBoogieBlock();
			
			String statement = addIndent()+"call "+name+"();\n";
			Parser.getInstance().addMainBoogieStatement(statement);
			Parser.getInstance().getMainProcedure().childrenNames.add(name);
			decIndent();
		}
		return code;
	}
}

class Declaration_Variable extends DataStructure {

}

class Constant extends DataStructure {
	int value;
	int base;
	Node type;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		value = object.get(JsonKeyName.VALUE).asInt();
		base = object.get(JsonKeyName.BASE).asInt();
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
	}

	@Override
	String p4_to_C() {
		String code = value+"";
		return code;
	}

	@Override
	String p4_to_Boogie() {
		String code = value+"";
		if(type instanceof Type_Bits) {
			Type_Bits tb = (Type_Bits)type;
			code += "bv"+tb.size;
		}
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
		
		len = 0;
		// get length
		if(type.Node_Type.equals("Type_Bits")) {
			Type_Bits tb = (Type_Bits)type;
			len = tb.size;
		}
		else if(type.Node_Type.equals("Type_Name")) {
			Type_Name tn = (Type_Name)type;
			len = Parser.getInstance().getTypeLength(tn.name);
		}
		else if(type.Node_Type.equals("Type_Header")) {
			Type_Header th = (Type_Header)type;
			len = Parser.getInstance().getTypeLength(th.name);
		}
		else if(type.Node_Type.equals("Type_Stack")) {
			Type_Stack ts = (Type_Stack)type;
			len = Parser.getInstance().getTypeLength(ts.elementType.getTypeName())*ts.size.value;
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
		else if(type.Node_Type.equals("Type_Stack")) {
			Type_Stack ts = (Type_Stack)type;
			code = ts.name;
		}
		return code;
	}
	@Override
	String getTypeName() {
		return type.getTypeName();
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
	@Override
	String getTypeName() {
		return name;
	}
	@Override
	String getName() {
		return name;
	}
}

class BoolLiteral extends DataStructure {

}

class Declaration_MatchKind extends DataStructure {

}

class StringLiteral extends DataStructure {

}

class NameMapProperty extends DataStructure {

}