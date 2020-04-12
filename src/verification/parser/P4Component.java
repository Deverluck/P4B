package verification.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class P4Component extends Node {

}

class P4Program extends P4Component {
	Node declarations;

	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		this.declarations = Parser.getInstance().jsonParse(object.get(JsonKeyName.DECLARATIONS));
		addChild(this.declarations);
	}

	@Override
	String p4_to_C() {
		return declarations.p4_to_C();
	}

	@Override
	String p4_to_Boogie() {
		return declarations.p4_to_Boogie();
	}
}

/* P4 parser */
class P4Parser extends P4Component {
	String name;
	Node type;
	Node parserLocals;  // local variables
	Node states;        // parse states

	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
		addChild(type);
		parserLocals = Parser.getInstance().jsonParse(object.get(JsonKeyName.PARSERLOCALS));
		addChild(parserLocals);
		states = Parser.getInstance().jsonParse(object.get(JsonKeyName.STATES));
		addChild(states);
	}

	@Override
	String p4_to_C() {
		String code = "// Parser \n"+"void ";
		// Type_Parser is unable by default
		type.setEnable();
		code += type.p4_to_C();
		code += "{\n	start();\n}\n";
		code += states.p4_to_C();
		// TODO support local variables
		return code;
	}

	String p4_to_Boogie() {
		BoogieProcedure procedure = new BoogieProcedure(name);
		Parser.getInstance().setCurrentProcedure(procedure);
		Parser.getInstance().addProcedure(procedure);

		String declare = "\n// Parser "+name+"\n";
		declare += "procedure "+name+"()\n";
		String body = "{\n";
		
		String statement = "	call start();\n";
		Parser.getInstance().addBoogieStatement(statement);
		
		body += statement;
		body += "}\n";
		procedure.childrenNames.add("start");
		procedure.declare = declare;
		procedure.body = body;

		String code = states.p4_to_Boogie();
		return code;

//		String code, body, modifies;
//		code = "\n// Parser "+name+"\n";
//		code += "procedure "+name+"()";
//		body = "{\n	call start();\n}\n";
//		body = states.p4_to_Boogie();
//		code += body;
//		type.setEnable();
//		code += type.p4_to_Boogie();
//		return code;
	}
}

class ParserState extends P4Component {
	String name;
	Node components;        // body
	Node selectExpression;

	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		components = Parser.getInstance().jsonParse(object.get(JsonKeyName.COMPONENTS));
		addChild(components);
		if(object.has(JsonKeyName.SELECTEXPRESSION)) {
			selectExpression = Parser.getInstance().jsonParse(object.get(JsonKeyName.SELECTEXPRESSION));
			addChild(selectExpression);
		}
		else
			selectExpression = null;
//		addChild(selectExpression);
	}
	@Override
	String p4_to_C() {
		String code = "void "+name+"(){\n";
		code += components.p4_to_C();
		if(selectExpression != null)
			code += selectExpression.p4_to_C(JsonKeyName.PARSERSTATE);
		code += "}\n";
		return code;
	}

	@Override
	String p4_to_Boogie() {
		BoogieProcedure procedure = new BoogieProcedure(name);
		Parser.getInstance().setCurrentProcedure(procedure);
		Parser.getInstance().addProcedure(procedure);

		String declare = "\n//Parser State "+name+"\n";
		declare += "procedure "+name+"()\n";
		incIndent();
		String body = "{\n";
		addIndent();
		body += components.p4_to_Boogie();
		if(selectExpression != null)
			body += selectExpression.p4_to_Boogie(JsonKeyName.PARSERSTATE);
		decIndent();
		body += "}\n";
		decIndent();
		procedure.declare = declare;
		procedure.body = body;
		String code = "";
		return code;

//		String code = "\n//Parser State "+name+"\n";
//		code += "procedure "+name+"(){\n";
//
////		Parser.getInstance().clearModifiedGlobalVariables();
//
//		incIndent();
//		code += components.p4_to_Boogie();
//		if(selectExpression != null)
//			code += selectExpression.p4_to_Boogie(JsonKeyName.PARSERSTATE);
//		code += "}\n";
//		decIndent();
//		return code;
	}
}

class P4Control extends P4Component {
	String name;
	Node type;
	Node controlLocals;
	Node body;
	
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
		controlLocals = Parser.getInstance().jsonParse(object.get(JsonKeyName.CONTROLLOCALS));
		body = Parser.getInstance().jsonParse(object.get(JsonKeyName.BODY));
		Parser.getInstance().addControl(this);
	}

	@Override
	String p4_to_C() {
//		System.out.println(name);
		type.setEnable();
		String code = "// Control \n"+"void ";
		code += type.p4_to_C();
		code += "{\n";
		code += body.p4_to_C();
		code += "}\n";
		code += controlLocals.p4_to_C();
		return code;
	}
	@Override
	String p4_to_C_declare() {
		type.setEnable();
		String code = "void "+name+" "+type.p4_to_C()+";\n";
		return code;
	}

	@Override
	String p4_to_Boogie() {
		BoogieProcedure procedure = new BoogieProcedure(name);
		Parser.getInstance().setCurrentProcedure(procedure);
		Parser.getInstance().addProcedure(procedure);

		String declare = "\n// Control "+name+"\n";
		//TODO Add parameters
		declare += "procedure "+name+"()\n";
		incIndent();
		String body = "{\n";
		body += this.body.p4_to_Boogie();
		body += "}\n";
		decIndent();

		procedure.declare = declare;
		procedure.body = body;
		//TODO may be mistakes
//		controlLocals.p4_to_Boogie();
		return controlLocals.p4_to_Boogie();
	}
}

/* P4 actions */
class P4Action extends P4Component {
	String name;
	Node parameters;
	Node body;

	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		parameters = Parser.getInstance().jsonParse(object.get(JsonKeyName.PARAMETERS));
		body = Parser.getInstance().jsonParse(object.get(JsonKeyName.BODY));
		Parser.getInstance().addAction(this);
	}
	@Override
	String p4_to_C() {
		String code = "// Action \n"+"void "+name;
		code += parameters.p4_to_C();
		code += "{\n";
		code += body.p4_to_C();
		code += "}\n";
		return code;
	}
	@Override
	String p4_to_C_declare() {
		String code = "void "+name+parameters.p4_to_C()+";\n";
		return code;
	}

	@Override
	String p4_to_Boogie() {
		BoogieProcedure procedure = new BoogieProcedure(name);
		Parser.getInstance().setCurrentProcedure(procedure);
		Parser.getInstance().addProcedure(procedure);

		String declare = "\n// Action "+name+"\n";
		declare += "procedure "+name;
		declare += parameters.p4_to_Boogie()+"\n";
		incIndent();
		String body = "{\n";
		body += this.body.p4_to_Boogie();
		body += "}\n";
		decIndent();

		procedure.declare = declare;
		procedure.body = body;
		return super.p4_to_Boogie();
	}
}

// important methods
class Method extends P4Component {

}

class ParameterList extends P4Component {
	Node parameters;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		parameters = Parser.getInstance().jsonParse(object.get(JsonKeyName.PARAMETERS));
		parameters.setVectorType(JsonKeyName.PARAMETER);
		addChild(parameters);
	}
	@Override
	String p4_to_C() {
		return parameters.p4_to_C();
	}
	@Override
	String p4_to_C(String arg) {
		return parameters.p4_to_C(arg);
	}
	@Override
	String p4_to_Boogie() {
		return parameters.p4_to_Boogie();
	}
	@Override
	String p4_to_Boogie(String arg) {
		return parameters.p4_to_Boogie(arg);
	}
}

class Parameter extends P4Component {
	String name;
	String direction;
	Node type;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		direction = object.get(JsonKeyName.DIRECTION).asText();
		type = Parser.getInstance().jsonParse(object.get(JsonKeyName.TYPE));
	}
	@Override
	String p4_to_C() {
		type.setEnable();
		return type.p4_to_C()+" "+name;
	}
	@Override
	String p4_to_C(String arg) {
		if(arg.equals(JsonKeyName.METHODCALL)) {
			return name;
		}
		return p4_to_C();
	}
	@Override
	String p4_to_Boogie() {
		type.setEnable();
		return name+":"+type.p4_to_Boogie();
	}
	@Override
	String getName() {
		return name;
	}
	@Override
	String getTypeName() {
		return type.getTypeName();
	}
}

/* P4 table and properties*/
class P4Table extends P4Component {
	String name;
	Node key;
	Node actions;
	Node size;
	Node default_action;

	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		ArrayNode properties = (ArrayNode)object.get(JsonKeyName.PROPERTIES).get(JsonKeyName.PROPERTIES)
				.get(JsonKeyName.VEC);
		for(JsonNode property:properties) {
//			System.out.println(property);
			if(property.has(JsonKeyName.NAME)) {
				String property_name = property.get(JsonKeyName.NAME).asText();
				switch (property_name) {
					case JsonKeyName.KEY:
						key = Parser.getInstance().jsonParse(property);
						break;
					case JsonKeyName.ACTIONS:
						actions = Parser.getInstance().jsonParse(property);
						break;
					case JsonKeyName.SIZE:
						size = Parser.getInstance().jsonParse(property);
						break;
					case JsonKeyName.DEFAULT_ACTION:
						default_action = Parser.getInstance().jsonParse(property);
						break;
					default:
						break;
				}
			}
		}
		Parser.getInstance().addTable(this);
	}
	@Override
	String p4_to_C() {
		String methodName = name+"_method";
		String code="// Table\n"+"void "+methodName+"();\n";
		code += name+".apply="+methodName+";\n";
		return code;
	}
	@Override
	String p4_to_C_preprocess() {
		// declare struct for apply() function
		String code = "// Table declaration\n"+"typedef struct "+name+"_struct "+"{\n";
		code += "	void (*apply)();\n";
		code += "}"+name+"_type"+";\n";
		code += name+"_type"+" "+name+";\n";
		return code;
	}
	@Override
	String p4_to_C_declare() {
		String code = "void "+name+"_method();\n";
		return code;
	}

	@Override
	String p4_to_Boogie() {
		BoogieProcedure procedure = new BoogieProcedure(name+".apply");
		Parser.getInstance().setCurrentProcedure(procedure);
		Parser.getInstance().addProcedure(procedure);

		String declare = "\n// Table "+name+"\n";
		declare += "procedure "+name+".apply()\n";
		incIndent();
		String body = "{\n";
		if(actions != null) {
			Property property = (Property)actions;
			ActionList actionList = (ActionList)property.value;
			// declare local variables for actions
			for(String actionName:actionList.actionList) {
				for(String parameter:actionList.actionParameters.get(actionName).keySet()) {
					procedure.addLocalVariable(parameter, "var "+parameter+":"+
							actionList.actionParameters.get(actionName).get(parameter)+";\n");
				}
			}
			int cnt = 0;
			for(String actionName:actionList.actionList) {
				String condition = addIndent();
				if(cnt != 0)
					condition += "else ";
				condition += "if("+name+".select == "+"action."+actionName+"){\n";
				String end = addIndent()+"}\n";
				BoogieIfStatement ifStatement = new BoogieIfStatement(condition, end);
				Parser.getInstance().addBoogieBlock(ifStatement);
				
				body += condition;
				incIndent();
				String statement = addIndent()+"call "+actionName;
				statement += "(";
				int tmp_cnt = actionList.actionParameters.get(actionName).size();
				for(String parameter:actionList.actionParameters.get(actionName).keySet()) {
					statement += parameter;
					tmp_cnt--;
					if(tmp_cnt>0)
						statement += ", ";
				}
				statement += ");\n";
				Parser.getInstance().addBoogieStatement(statement);
				body += statement;
				// TODO deal with action arguments
				Parser.getInstance().getCurrentProcedure().childrenNames.add(actionName);
				decIndent();
				body += end;
				cnt++;
				
				Parser.getInstance().popBoogieBlock();
//				if(cnt != 0)
//					body += addIndent()+"else";
			}
		}
		body += "}\n";
		decIndent();
		procedure.declare = declare;
		procedure.body = body;
		
		String code = "";
		if(actions != null) {
			code += "\n// Table "+name+" Actionlist Declaration\n";
			code += "type "+name+".action;\n";
			code += actions.p4_to_Boogie(name+".action");
			code += "var "+name+".select : "+name+".action;\n";
		}
		return code;
	}
}

class Property extends P4Component {
	String name;  //  key/actions/size/default_action
	Node value;

	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		name = object.get(JsonKeyName.NAME).asText();
		value = Parser.getInstance().jsonParse(object.get(JsonKeyName.VALUE));
	}
	@Override
	String p4_to_C() {
		// TODO Auto-generated method stub
		return super.p4_to_C();
	}
	@Override
	String p4_to_Boogie(String arg) {
		if(name.equals(JsonKeyName.ACTIONS)) {
			return value.p4_to_Boogie(arg);
		}
		return super.p4_to_Boogie(arg);
	}
}

class TableProperties extends P4Component {
	ArrayList<Property> properties;
	public TableProperties() {
		super();
		properties = new ArrayList<>();
	}
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		ArrayNode properties_node = (ArrayNode)object.get(JsonKeyName.PROPERTIES).get(JsonKeyName.VEC);
		for(JsonNode property : properties_node) {
			properties.add((Property)Parser.getInstance().jsonParse(property));
		}
	}
	@Override
	String p4_to_C() {
		// TODO Auto-generated method stub
		// translate based on Property type(name)
		return super.p4_to_C();
	}
}

class ActionList extends P4Component {
	ArrayList<String> actionList;
	HashMap<String, LinkedHashMap<String, String>> actionParameters;
	public ActionList() {
		super();
		actionList = new ArrayList<>();
		actionParameters = new HashMap<>();
	}
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		ArrayNode elements = (ArrayNode)object.get(JsonKeyName.ACTIONLIST).get(JsonKeyName.VEC);
		for(JsonNode element : elements) {
			String action = element.get(JsonKeyName.EXPRESSION).get(JsonKeyName.METHOD)
					.get(JsonKeyName.PATH).get(JsonKeyName.NAME).asText();
			ArrayNode arrayNode = (ArrayNode)element.get(JsonKeyName.EXPRESSION).get(JsonKeyName.TYPE)
					.get(JsonKeyName.PARAMETERS).get(JsonKeyName.PARAMETERS).get(JsonKeyName.VEC);
			LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
			for(JsonNode jsonNode:arrayNode) {
				Node node = Parser.getInstance().jsonParse(jsonNode);
				if(!node.getName().equals("")&&!node.getTypeName().equals("")) {
					String parameterName = node.getName();
					String parameterTypeName = node.getTypeName();
					parameters.put(parameterName, parameterTypeName);
				}
			}
			actionParameters.put(action, parameters);
			actionList.add(action);
//			actionList.add((ActionListElement)Parser.getInstance().jsonParse(element));
		}
	}
	@Override
	String p4_to_C() {
		// TODO Auto-generated method stub
		return super.p4_to_C();
	}
	@Override
	String p4_to_Boogie(String arg) {
		String code = "";
		for(String actionName:actionList) {
			code += "const unique action."+actionName+" : "+arg+";\n";
		}
		int cnt = actionList.size();
		if(cnt != 0) {
			code += "axiom(forall action:"+arg+" :: ";
			for(String actionName:actionList) {
				code += "action==action."+actionName;
				cnt--;
				if(cnt!=0)
					code += " || ";
			}
			code += ");\n";
		}
		return code;
	}
}

class ActionListElement extends P4Component {
	Node expression;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		expression = Parser.getInstance().jsonParse(object.get(JsonKeyName.EXPRESSION));
	}
	@Override
	String p4_to_C() {
		return expression.p4_to_C();
	}
}

class Key extends P4Component {
	ArrayList<Node> expressions;
	ArrayList<Node> matchTypes;
	public Key() {
		super();
		expressions = new ArrayList<>();
		matchTypes = new ArrayList<>();
	}
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		ArrayNode keyElements = (ArrayNode)object.get(JsonKeyName.KEYELEMENTS).get(JsonKeyName.VEC);
		for(JsonNode keyElement : keyElements) {
			expressions.add(Parser.getInstance().jsonParse(keyElement.get(JsonKeyName.EXPRESSION)));
			matchTypes.add(Parser.getInstance().jsonParse(keyElement.get(JsonKeyName.MATCHTYPE)));
		}
	}
	@Override
	String p4_to_C() {
		// TODO Auto-generated method stub
		return super.p4_to_C();
	}
}

class Mask extends P4Component {

}