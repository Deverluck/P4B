package verification.parser;

import net.sf.json.JSONObject;

public class P4Component extends Node {

}

class P4Program extends P4Component {
	Node declarations;
	
	@Override
	boolean parse(JSONObject object) {
		super.parse(object);
		JSONObject declarations = object.getJSONObject(JsonKeyName.DECLARATIONS);
		this.declarations = Parser.jsonParse(declarations);
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
	boolean parse(JSONObject object) {
		super.parse(object);
		name = object.getString(JsonKeyName.NAME);
		type = Parser.jsonParse(object.getJSONObject(JsonKeyName.TYPE));
		addChild(type);
		parserLocals = Parser.jsonParse(object.getJSONObject(JsonKeyName.PARSERLOCALS));
		addChild(parserLocals);
		states = Parser.jsonParse(object.getJSONObject(JsonKeyName.STATES));
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
	boolean parse(JSONObject object) {
		super.parse(object);
		name = object.getString(JsonKeyName.NAME);
		components = Parser.jsonParse(object.getJSONObject(JsonKeyName.COMPONENTS));
		addChild(components);
		if(object.containsKey(JsonKeyName.SELECTEXPRESSION)) {
			selectExpression = Parser.jsonParse(object.getJSONObject(JsonKeyName.SELECTEXPRESSION));
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
	@Override
	boolean parse(JSONObject object) {
		// TODO Auto-generated method stub
		return super.parse(object);
	}
}

/* P4 actions */
class P4Action extends P4Component {
	
}

class Method extends P4Component {
	
}

class ParameterList extends P4Component {
	Node parameters;
	@Override
	boolean parse(JSONObject object) {
		super.parse(object);
		parameters = Parser.jsonParse(object.getJSONObject(JsonKeyName.PARAMETERS));
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
	boolean parse(JSONObject object) {
		super.parse(object);
		name = object.getString(JsonKeyName.NAME);
		direction = object.getString(JsonKeyName.DIRECTION);
		type = Parser.jsonParse(object.getJSONObject(JsonKeyName.TYPE));
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