package verification.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Parser {
	private static Parser instance;
	private HashSet<String> types;
	private HashMap<Integer, ObjectNode> allJsonNodes;
	private ArrayList<P4Table> tables;
	private ArrayList<P4Control> controls;
	private ArrayList<P4Action> actions;
	private HashMap<String, Type_Header> headers;
	private HashMap<String, Type_Struct> structs;
	private ArrayList<Type_Typedef> typeDefinitions;
	private HashMap<String, Integer> typeDefLength;  // Record the length of user-defined type
	private int indent;
	private final String INDENT = "	";

	public static Parser getInstance() {
		if(instance == null) {
			instance = new Parser();
		}
		return instance;
	}

	private Parser() {
		types = new HashSet<>();
		allJsonNodes = new HashMap<>();
		tables = new ArrayList<>();
		controls = new ArrayList<>();
		actions = new ArrayList<>();
		headers = new HashMap<>();
		structs = new HashMap<>();
		typeDefinitions = new ArrayList<>();
		typeDefLength = new HashMap<>();
		indent = 0;
		globalVariables = new HashSet<>();
		modifiedGlobalVariables = new HashSet<>();
		procedures = new HashMap<>();
		boogieFunctions = new HashMap<>();
		blockStack = new BoogieBlockStack();
	}

	private void clear() {
		types.clear();
		allJsonNodes.clear();
		tables.clear();
		controls.clear();
		actions.clear();
		headers.clear();
		structs.clear();
		typeDefinitions.clear();
		globalVariables.clear();
		modifiedGlobalVariables.clear();
		procedures.clear();
		boogieFunctions.clear();
		blockStack.clear();
	}

	public String addIndent() {
		String str="";
		for(int i = 0; i < indent; i++)
			str += INDENT;
		return str;
	}

	public void incIndent() {
		indent++;
	}

	public void decIndent() {
		if(indent>0)
			indent--;
	}

	public void parse(String filename) {
		ObjectMapper mapper = new ObjectMapper();
		File file = new File(filename);
		try {
			long startTime = System.currentTimeMillis();

			System.out.println("parse:");
			System.out.println("####################");
			ObjectNode rootNode = (ObjectNode)mapper.readTree(file);
			getAllNodes(rootNode);
			System.out.println("getAllNodes() ends");
			System.out.println("total: "+allJsonNodes.size());

			//parse
			Node program = jsonParse(rootNode);
			System.out.println("jsonParse() ends");

//			System.out.println("######## C Program ########");
//			String C_code = p4_to_C(program);
//			System.out.println(C_code);


			String Boogie_code = p4_to_Boogie(program);
			System.out.println("######## Boogie Program ########");
			for(String key:procedures.keySet()) {
				System.out.println(key);
				System.out.println(procedures.get(key).mainBlock.toBoogie());
				System.out.println();
			}
//			System.out.println(Boogie_code);

			long endTime = System.currentTimeMillis();
			System.out.println("Time: " + (endTime - startTime) + "ms");
			clear();
		}catch(JsonProcessingException e) {
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

	// get all nodes from JSON file and update their attributes
	@SuppressWarnings("deprecation")
	public void getAllNodes(ObjectNode rootNode) {
		if(rootNode.has(JsonKeyName.NODE_ID)) {
			int id = rootNode.get(JsonKeyName.NODE_ID).asInt();
			if(allJsonNodes.containsKey(id)) {
				// update attributes
				Iterator<String> it = rootNode.fieldNames();
				while(it.hasNext()) {
					String key = it.next();
					ObjectNode node_at_id = allJsonNodes.get(id);
					if(!node_at_id.has(key))
						node_at_id.put(key, rootNode.get(key));
				}
			}
			else {
//				// add new node
				allJsonNodes.put(id, rootNode);
			}
		}

		Iterator<String> keyset = rootNode.fieldNames();
		while(keyset.hasNext()) {
			String key = keyset.next();
			Object child = rootNode.get(key);
			if(key.equals(JsonKeyName.VEC)) {
				ArrayNode jsonArray = (ArrayNode)child;
				for(Object obj : jsonArray) {
					ObjectNode childNode = (ObjectNode)obj;
					getAllNodes(childNode);
				}
			}
			else if(child instanceof ObjectNode) {
				ObjectNode childNode = (ObjectNode)child;
				getAllNodes(childNode);
			}
		}
	}

	public  Node jsonParse(JsonNode jsonNode) {
		ObjectNode object = (ObjectNode)jsonNode;
		object = getJsonNode(object.get(JsonKeyName.NODE_ID).asInt());
		String typeName = object.get(JsonKeyName.NODE_TYPE).asText();
		try{
			Node node;
			if(object.has(JsonKeyName.VEC)) {
				node = new TypeVector();
			}
			else {
				types.add(typeName);
				Class<?> nodeClass = Class.forName("verification.parser."+typeName);
				node = (Node)nodeClass.newInstance();
			}
			node.parse(object);
			return node;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ObjectNode getJsonNode(int key) {
		if(allJsonNodes == null || !allJsonNodes.containsKey(key))
			return null;
		return allJsonNodes.get(key);
	}

	public void addTable(P4Table table) {
		tables.add(table);
	}

	public void addControl(P4Control control) {
		controls.add(control);
	}

	public void addAction(P4Action action) {
		actions.add(action);
	}

	public void addHeader(Type_Header header) {
		if(!headers.containsKey(header.name))
			headers.put(header.name, header);
	}

	public void addStruct(Type_Struct struct) {
		if(!structs.containsKey(struct.name))
			structs.put(struct.name, struct);
	}

	public void addTypeDef(Type_Typedef typeDef) {
		typeDefinitions.add(typeDef);
		if(!typeDefLength.containsKey(typeDef.name)) {
			typeDefLength.put(typeDef.name, typeDef.len);
		}
	}

	public int getTypeLength(String name) {
		if(typeDefLength.containsKey(name))
			return typeDefLength.get(name);
		else if(headers.containsKey(name))
			return headers.get(name).length();
		else
			return -1;
	}

	String p4_to_C(Node program) {
		// for testing unhandled types
		String [] handledTypes = {"P4Program", "Type_Error", "Type_Extern", "StructField",
			"Type_Bits", "Type_Name", "Path",
			"Parameter", "ParameterList", "PathExpression", "Member", "P4Parser", "Type_Parser",
			"MethodCallStatement", "MethodCallExpression", "Constant", "ParserState",
			"Type_Control", "BlockStatement", "AssignmentStatement", "Add", "Sub", "LAnd", "LOr",
			"BAnd", "BOr", "BXor", "Geq", "Leq", "LAnd", "LOr", "Shl", "Shr", "Mul", "LNot",
			"IfStatement", "ActionListElement", "P4Action", "Type_Struct", "Type_Header",
			"ConstructorCallExpression", "P4Control"};
		HashSet<String> mytypes = new HashSet<>();
		mytypes.addAll(types);
		for(String str : handledTypes){
			mytypes.remove(str);
		}
		System.out.println("######## Unhandled Types ########");
		for(String type : mytypes) {
			System.out.println(type);
		}
//		System.out.println("######## Program ########");
		String code = p4_to_C_declare()+p4_to_C_preprocess()+program.p4_to_C();
//		System.out.println(code);
		return code;
	}

	// declare struct for controls and tables
	String p4_to_C_preprocess() {
		String code = "";
		for(P4Control control : controls) {
			code += control.p4_to_C_preprocess();
		}
		for(P4Table table : tables) {
			code += table.p4_to_C_preprocess();
		}
		code += "//over\n";
		return code;
	}

	// declare methods for controls, tables and actions
	String p4_to_C_declare() {
		String code = "";
		for(Type_Typedef typeDef : typeDefinitions) {
			code += typeDef.p4_to_C_declare();
		}
		for(String key : headers.keySet()) {
			code += headers.get(key).p4_to_C_declare();
		}
		for(String key : structs.keySet()) {
			code += structs.get(key).p4_to_C_declare();
		}
		for(P4Control control : controls) {
			code += control.p4_to_C_declare();
		}
		for(P4Table table : tables) {
			code += table.p4_to_C_declare();
		}
		for(P4Action action : actions) {
			code += action.p4_to_C_declare();
		}
		return code;
	}


	// ******************* Boogie **********************
	String p4_to_Boogie_Header_isValid() {
		String code = "\nvar isValid:<T>[T]bool;\n";
//		code += "function isValid<T>(header: T) returns (bool) { valid(header) }";

		BoogieProcedure procedure = new BoogieProcedure("clear_valid");
		addProcedure(procedure);

		String declare = "\nprocedure clear_valid();\n";
		procedure.modifies.add("isValid");
		for(String name:headers.keySet()) {
			declare += "	ensures (forall header:"+name;
			declare += ":: isValid[header]==false);\n";
		}
//		declare += "	modifies isValid;\n";
		procedure.declare = declare;
		
		BoogieProcedure procedure2 = new BoogieProcedure("setInvalid");
		addProcedure(procedure2);
		setCurrentProcedure(procedure2);
		procedure2.modifies.add("isValid");
		String declare2 = "\nprocedure setInvalid<T>(header:T)\n";
		String body2 = "{\n";
		
		String statement = "	isValid[header]:false;\n";
		addBoogieStatement(statement);
		
		body2 += statement;
		body2 += "}\n";
		procedure2.declare = declare2;
		procedure2.body = body2;

		addBoogieGlobalVariable("isValid");
		return code;
	}

	String p4_to_Boogie_extern() {
		BoogieProcedure procedure = new BoogieProcedure("mark_to_drop");
		addProcedure(procedure);

		String declare = "";
		declare += "\nprocedure mark_to_drop()\n";
		String body = "{\n";
		body += "}\n";
		procedure.declare = declare;
		procedure.body = body;

		String code = "";
		return code;
	}

	String p4_to_Boogie_extract() {
		String code = "";
		Type_Struct myheaders = structs.get(headersName);
		int totalLen = myheaders.length();
		int start = 0; //for extracting
		for(StructField headersField:myheaders.fields) {
			String name = headersField.getTypeName();
			String procedureName = "packet_in.extract.headers."+headersField.name;
			BoogieProcedure procedure = new BoogieProcedure(procedureName);
			addProcedure(procedure);
			setCurrentProcedure(procedure);
			String declare = "\nprocedure "+procedureName+"(header:"+name+")\n";
			getCurrentProcedure().declare = declare;
			addModifiedGlobalVariable("isValid");

			for(StructField field:headers.get(name).fields) {
				addModifiedGlobalVariable(name+"."+field.name);
			}
			String body = "";
			body += "{\n";
			for(StructField field:headers.get(name).fields) {
				String statement = "	"+name+"."+field.name+"[header] := packet";
				statement += "["+(start+field.len)+":"+start+"];\n";
				addBoogieStatement(statement);
				
				body += statement;
				start += field.len;
			}
			String statement = "	isValid[header] := true;\n";
			addBoogieStatement(statement);
			
			body += statement;
			body += "}\n";
			getCurrentProcedure().body = body;
		}
		code += "\ntype packet_in = bv"+totalLen+";\n";
		code += "const packet:packet_in;\n";
		return code;
	}
	
	String p4_to_Boogie_emit() {
		String packetoutName = "packet_o";
		String code = "";
		code += "\ntype packet_out = packet_in;\n";
		code += "var "+packetoutName+":packet_in;\n";
		addBoogieGlobalVariable(packetoutName);
		
		Type_Struct myheaders = structs.get(headersName);
		int totalLen = myheaders.length();
		int start = 0; //for extracting
		for(StructField headersField:myheaders.fields) {
			String name = headersField.getTypeName();
			String procedureName = "packet_out.emit.headers."+headersField.name;
			BoogieProcedure procedure = new BoogieProcedure(procedureName);
			addProcedure(procedure);
			setCurrentProcedure(procedure);
			String declare = "\nprocedure "+procedureName+"(header:"+name+")\n";
			getCurrentProcedure().declare = declare;
			addModifiedGlobalVariable(packetoutName);

//			for(StructField field:headers.get(name).fields) {
//				addModifiedGlobalVariable(name+"."+field.name);
//			}
			String body = "";
			body += "{\n";
			incIndent();
			
			String ifStart = addIndent()+"if(isValid[header]){\n";
			String ifEnd = addIndent()+"}\n";
			BoogieIfStatement boogieIfStatement = new BoogieIfStatement(ifStart, ifEnd);
			addBoogieBlock(boogieIfStatement);
			
			body += ifStart;
			incIndent();
			for(StructField field:headers.get(name).fields) {
				int end = start+field.len;
				
				String statement = "";
				statement += addIndent();
				statement += packetoutName+" := ";
				if(start+field.len != totalLen) {
					statement += packetoutName+"["+totalLen+":"+end+"]++";
				}
				statement += name+"."+field.name+"[header]";
				if(start!=0) {
					statement += "++"+packetoutName+"["+start+":"+"0"+"]";
				}
				statement += ";\n";
				addBoogieStatement(statement);
				body += statement;
				start += field.len;
			}
			decIndent();
			body += ifEnd;
			popBoogieBlock();
			decIndent();
			body += "}\n";
			getCurrentProcedure().body = body;
		}
		return code;
	}

	String p4_to_Boogie(Node program) {
		System.out.println("######## Unhandled Types ########");
		String [] handledTypes = {"Path", "Type_Name", "StructField", "Type_Struct",
				"MethodCallStatement", "Constant", "MethodCallExpression", "Type_Header",
				"P4Program", "Type_Typedef", "BlockStatement", "AssignmentStatement",
				"LNot", "LAnd", "Add", "Sub", "Mul", "Shl", "BAnd", "BOr", "BXor"};
		HashSet<String> mytypes = new HashSet<>();
		mytypes.addAll(types);
		for(String str : handledTypes){
			mytypes.remove(str);
		}
		for(String type : mytypes) {
			System.out.println(type);
		}
		String code = program.p4_to_Boogie();
		// Add SMT built-in functions
		for(String functionName:boogieFunctions.keySet()) {
			code += boogieFunctions.get(functionName)+"\n";
		}
		// Add map isValid and procedure clear_valid()
		code += p4_to_Boogie_Header_isValid();
		// Add extract procedures
		code += p4_to_Boogie_extract();
		// Add emit procedures
		code += p4_to_Boogie_emit();
		// Add support for extern methods
		code += p4_to_Boogie_extern();

//		for(String name:procedures.keySet()) {
//			BoogieProcedure procedure = procedures.get(name);
//			System.out.println(name+":");
//			for(String childName:procedure.childrenNames) {
//				System.out.println("	"+childName);
//			}
//		}
		for(String name:procedures.keySet()) {
			BoogieProcedure procedure = procedures.get(name);
			for(String childName:procedure.childrenNames) {
				if(procedures.containsKey(childName))
					procedures.get(childName).parents.add(procedure);
			}
		}
		BoogieProcedureOperator bpo = new BoogieProcedureOperator();
		for(String name:procedures.keySet()) {
			bpo.addProcedure(procedures.get(name));
		}
		bpo.update();
		for(BoogieProcedure procedure:bpo.procedures) {
			code += procedure.toBoogie();
//			System.out.println(procedure.toBoogie());
		}
		return code;
	}

	private HashSet<String> globalVariables;
	private HashSet<String> modifiedGlobalVariables;
	private HashMap<String, BoogieProcedure> procedures;
	private BoogieProcedure currentProcedure;
	private BoogieBlockStack blockStack;
	private HashMap<String, String> boogieFunctions; //SMT bit-vector
	private String headersName;

	void addProcedure(BoogieProcedure procedure) {
		procedures.put(procedure.name, procedure);
	}

	void setCurrentProcedure(BoogieProcedure procedure) {
		blockStack.clear();
		this.currentProcedure = procedure;
		blockStack.addBlock(procedure.mainBlock);
	}
	
	void popBoogieBlock() {
		blockStack.popBlock();
	}
	
	void addBoogieBlock(BoogieBlock block) {
		blockStack.addBlock(block);
	}
	
	void addBoogieStatement(BoogieStatement statement) {
		blockStack.addStatement(statement);
	}
	
	void addBoogieStatement(String cont) {
		BoogieStatement statement = new BoogieStatement(cont);
		addBoogieStatement(statement);
	}

	BoogieProcedure getCurrentProcedure() {
		return this.currentProcedure;
	}

	void addModifiedGlobalVariable(String var) {
		if(currentProcedure!=null && globalVariables.contains(var))
			currentProcedure.updateModifies(var);
	}

	void addBoogieGlobalVariable(String var) {
		globalVariables.add(var);
	}

	void addBoogieFunction(String name, String cont) {
		if(!boogieFunctions.containsKey(name))
			boogieFunctions.put(name, cont);
	}
	
	void setHeadersName(String headersName) {
		this.headersName = headersName;
	}

//
//	void addModifiedGlobalVariable(String var) {
//		if(globalVariables.contains(var))
//			modifiedGlobalVariables.add(var);
//	}
//
//	HashSet<String> getModifiedGlobalVariables() {
//		return modifiedGlobalVariables;
//	}
//
//	void clearModifiedGlobalVariables() {
//		modifiedGlobalVariables.clear();
//	}
//
//	void addProcedure(String name) {
//		HashSet<String> list = new HashSet<>();
//		procedures.put(name, list);
//	}
//
//	void addProcedureModifiedVariable(String name, String var) {
//		if(!procedures.containsKey(name))
//			addProcedure(name);
//		procedures.get(name).add(var);
//	}
}
