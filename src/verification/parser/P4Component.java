package verification.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class P4Component extends Node {

}

class P4Program extends P4Component {
	Node declarations;
	
	@Override
	boolean parse(ObjectNode object) {
		super.parse(object);
		this.declarations = Parser.jsonParse(object.get(JsonKeyName.DECLARATIONS));
		addChild(this.declarations);
		return true;
	}
	
	@Override
	String p4_to_C() {
		return declarations.p4_to_C();
	}
}

/* P4 parser */
class P4Parser extends P4Component {
	String name;
	Node type;
	Node parserLocals;  // local variables
	Node states;        // parse states
	
	@Override
	boolean parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		type = Parser.jsonParse(object.get(JsonKeyName.TYPE));
		addChild(type);
		parserLocals = Parser.jsonParse(object.get(JsonKeyName.PARSERLOCALS));
		addChild(parserLocals);
		states = Parser.jsonParse(object.get(JsonKeyName.STATES));
		addChild(states);
		return true;
	}
	
	@Override
	String p4_to_C() {
		String code = "void "+name;
		type.setEnable();
		code += type.p4_to_C();
		code += "{\n	start();\n}\n";
		code += states.p4_to_C();
		// TODO support local variables
		return code;
	}
}

class ParserState extends P4Component {
	String name;
	Node components;        // body
	Node selectExpression;
	
	@Override
	boolean parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		components = Parser.jsonParse(object.get(JsonKeyName.COMPONENTS));
		addChild(components);
		if(object.has(JsonKeyName.SELECTEXPRESSION)) {
			selectExpression = Parser.jsonParse(object.get(JsonKeyName.SELECTEXPRESSION));
			addChild(selectExpression);
		}
		else
			selectExpression = null;
//		addChild(selectExpression);
		return true;
	}
	@Override
	String p4_to_C() {
		String code = "void "+name+"(){\n";
		if(selectExpression != null)
			code += selectExpression.p4_to_C();
		code += "}\n";
		return code;
	}
}

class P4Control extends P4Component {
//	@Override
//	boolean parse(JSONObject object) {
//		// TODO Auto-generated method stub
//		return super.parse(object);
//	}
}

/* P4 actions */
class P4Action extends P4Component {
	
}

class Method extends P4Component {
	
}

class ParameterList extends P4Component {
	Node parameters;
	@Override
	boolean parse(ObjectNode object) {
		super.parse(object);
		parameters = Parser.jsonParse(object.get(JsonKeyName.PARAMETERS));
		addChild(parameters);
		return true;
	}
	@Override
	String p4_to_C() {
		parameters.setVectorType(JsonKeyName.PARAMETER);
		return parameters.p4_to_C();
	}
}

class Parameter extends P4Component {
	String name;
	String direction;
	Node type;
	@Override
	boolean parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		direction = object.get(JsonKeyName.DIRECTION).asText();
		type = Parser.jsonParse(object.get(JsonKeyName.TYPE));
		return true;
	}
	@Override
	String p4_to_C() {
		type.setEnable();
		return type.p4_to_C()+" "+name;
	}
}

/* P4 table and properties*/
class P4Table extends P4Component {
	
}

class Property extends P4Component {
	
}

class TableProperties extends P4Component {
	
}

class ActionList extends P4Component {
	
}

class ActionListElement extends P4Component {
	
}

class Key extends P4Component {
	
}