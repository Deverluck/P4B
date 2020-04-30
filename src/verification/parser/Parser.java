package verification.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

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
	private HashMap<String, Type_Stack> stacks;
	private ArrayList<Type_Typedef> typeDefinitions;
	private HashMap<String, Integer> typeDefLength;  // Record the length of user-defined type
	private HashSet<String> parserStates;
	private HashSet<String> parserLocals;
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
		stacks = new HashMap<>();
		typeDefinitions = new ArrayList<>();
		typeDefLength = new HashMap<>();
		parserStates = new HashSet<>();
		parserLocals = new HashSet<>();
		
		indent = 0;
		globalVariables = new HashSet<>();
		modifiedGlobalVariables = new HashSet<>();
		procedures = new LinkedHashMap<>();
		boogieFunctions = new HashMap<>();
		blockStack = new BoogieBlockStack();
		globalBoogieDeclarationBlock = new BoogieBlock();
		globalBoogieDeclaration = new HashSet<String>();
		mainProcedure = new BoogieProcedure("mainProcedure");
		
		procedures.put(mainProcedure.name, mainProcedure);
		
		commands = new Commands();
		ifStatements = new ArrayList<>();
		assignmentStatements = new ArrayList<>();
		switchStatements = new ArrayList<>();
	}

	private void clear() {
		types.clear();
		allJsonNodes.clear();
		tables.clear();
		controls.clear();
		actions.clear();
		headers.clear();
		structs.clear();
		stacks.clear();
		typeDefinitions.clear();
		typeDefLength.clear();
		parserStates.clear();
		parserLocals.clear();
		
		globalVariables.clear();
		modifiedGlobalVariables.clear();
		procedures.clear();
		boogieFunctions.clear();
		blockStack.clear();
		globalBoogieDeclarationBlock.clear();
		globalBoogieDeclaration.clear();
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

	public String parse(String filename) {
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
//			System.out.println("######## Boogie Program ########");
//			System.out.println(Boogie_code);
			long endTime = System.currentTimeMillis();
			System.out.println("Time: " + (endTime - startTime) + "ms");
			clear();
			return Boogie_code;
		}catch(JsonProcessingException e) {
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}
		return "";
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
	
	public void addStack(Type_Stack stack) {
		if(!stacks.containsKey(stack.name))
			stacks.put(stack.name, stack);
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
	String p4_to_Boogie_Main_Declaration() {
		String code = "";
		code += "\ntype Ref;\n";
		code += "type Field T;\n";
		code += "type HeapType = <T>[Ref, Field T]T;\n";
		code += "type HeaderStack = [int]Ref;\n";
		code += "var Heap:HeapType;\n";
		code += "var last:[HeaderStack]Ref;\n";
		addBoogieGlobalVariable("Heap");
		
//		code += "type HeaderStack = <T>[int]T";
		return code;
	}
	
	String p4_to_Boogie_Header_isValid() {
		String code = "\nvar isValid:<T>[T]bool;\n";
		addBoogieGlobalVariable("isValid");
		
		code += "\nvar emit:<T>[T]bool;\n";
		addBoogieGlobalVariable("emit");

		BoogieProcedure clear_valid = new BoogieProcedure("clear_valid");
		clear_valid.implemented = false;
		addProcedure(clear_valid);
		clear_valid.declare = "\nprocedure clear_valid();\n";
		clear_valid.modifies.add("isValid");
		clear_valid.declare += "	ensures (forall header:Ref";
		clear_valid.declare += ":: isValid[header]==false);\n";
//		for(String name:headers.keySet()) {
//			clear_valid.declare += "	ensures (forall header:"+name;
//			clear_valid.declare += ":: isValid[header]==false);\n";
//		}
		addMainPreBoogieStatement("	call clear_valid();\n");
		mainProcedure.childrenNames.add(clear_valid.name);
		
		BoogieProcedure clear_emit = new BoogieProcedure("clear_emit");
		clear_emit.implemented = false;
		addProcedure(clear_emit);
		clear_emit.declare = "\nprocedure clear_emit();\n";
		clear_emit.modifies.add("emit");
		clear_emit.declare += "	ensures (forall header:Ref";
		clear_emit.declare += ":: emit[header]==false);\n";
//		for(String name:headers.keySet()) {
//			clear_emit.declare += "	ensures (forall header:"+name;
//			clear_emit.declare += ":: emit[header]==false);\n";
//		}
		addMainPreBoogieStatement("	call clear_emit();\n");
		mainProcedure.childrenNames.add(clear_emit.name);
		
		// support header stack
		code += "var stack.index:<T>[T]int;\n";
		addBoogieGlobalVariable("stack.index");
		code += "var size:<T>[T]int;\n";
		addBoogieGlobalVariable("size");
		
		BoogieProcedure initStackIndex = new BoogieProcedure("init.stack.index");
		initStackIndex.implemented = false;
		addProcedure(initStackIndex);
		String declare3 = "\nprocedure init.stack.index();\n";
		initStackIndex.modifies.add("stack.index");
		declare3 += "ensures (forall <T>s:T::stack.index[s]==0);\n";
		initStackIndex.declare = declare3;
		addMainPreBoogieStatement("	call init.stack.index();\n");
		mainProcedure.childrenNames.add(initStackIndex.name);
		
		return code;
	}

	String p4_to_Boogie_extern() {
		BoogieProcedure procedure = new BoogieProcedure("mark_to_drop");
		setCurrentProcedure(procedure);
		addProcedure(procedure);

		String declare = "";
		declare += "\nprocedure {:inline 1} mark_to_drop()\n";
		String body = "{\n";
		incIndent();
		String statement = addIndent()+"drop := true;\n";
		procedure.updateModifies("drop");
		addBoogieStatement(statement);
		body += "}\n";
		procedure.declare = declare;
		procedure.body = body;

		String code = "var drop:bool;\n";
		addBoogieGlobalVariable("drop");
		return code;
	}

	String p4_to_Boogie_extract() {
		String code = "";
		Type_Struct myheaders = structs.get(headersName);
		int totalLen = myheaders.length();
		int start = 0; //for extracting
		addBoogieGlobalVariable("packet.index");
		for(StructField headersField:myheaders.fields) {
			if(headersField.type.Node_Type.equals("Type_Stack")) {
				Type_Stack ts = (Type_Stack)headersField.type;
//				String name = ts.name;
				String procedureName = "packet_in.extract.headers."+headersField.name+".next";
				BoogieProcedure procedure = new BoogieProcedure(procedureName);
				addProcedure(procedure);
				setCurrentProcedure(procedure);
				String declare = "\nprocedure {:inline 1} "+procedureName+"(stack:"+"HeaderStack"+")\n";
//				String declare = "\nprocedure {:inline 1} "+procedureName+"(stack:"+name+")\n";
				getCurrentProcedure().declare = declare;
				addModifiedGlobalVariable("isValid");
				addModifiedGlobalVariable("stack.index");
//				addModifiedGlobalVariable("packet.index");
				
				//Useless
//				for(StructField field:headers.get(ts.elementType.getTypeName()).fields) {
//					addModifiedGlobalVariable(ts.elementType.getTypeName()+"."+field.name);
//				}
				
				String body = "";
				body += "{\n";
				incIndent();
				addBoogieStatement(addIndent()+"isValid[stack[stack.index[stack]]] := true;\n");
				addBoogieStatement(addIndent()+"stack.index[stack] := stack.index[stack]+1;\n");
//				addBoogieStatement(addIndent()+"packet.index := "+start+"+stack.index[stack]*"+
//						headers.get(ts.elementType.getTypeName()).length()+";\n");
				decIndent();
				body += "}\n";
				procedure.body = body;
				
				// parser may extract the header in a stack using its index instead of "stack.next"
//				String childProcedureName = "packet_in.extract.headers."+headersField.name;
//				BoogieProcedure childProcedure = new BoogieProcedure(childProcedureName);
//				childProcedure.implemented = false;
//				addProcedure(childProcedure);
//				setCurrentProcedure(childProcedure);
//				childProcedure.declare = "\nprocedure "+childProcedureName+"(header:"
//						+"Ref"+");\n";
//				childProcedure.declare += "	ensures (isValid[header] == true);\n";
				
//				addModifiedGlobalVariable("isValid");
//				addModifiedGlobalVariable("packet.index");
//				incIndent();
//				addBoogieStatement(addIndent()+"isValid[header] := true;\n");
//				addBoogieStatement(addIndent()+"packet.index := "+"packet.index+"+
//						headers.get(ts.elementType.getTypeName()).length()+";\n");
//				decIndent();
			}
			else {
				String name = headersField.getTypeName();
				String procedureName = "packet_in.extract";
				BoogieProcedure procedure = new BoogieProcedure(procedureName);
				procedure.implemented = false;
				addProcedure(procedure);
				setCurrentProcedure(procedure);
				String declare = "\nprocedure "+procedureName+"(header:Ref);\n";
				declare += "	ensures (isValid[header] == true);\n";
				getCurrentProcedure().declare = declare;
//				String name = headersField.getTypeName();
//				String procedureName = "packet_in.extract.headers."+headersField.name;
//				BoogieProcedure procedure = new BoogieProcedure(procedureName);
//				procedure.implemented = false;
//				addProcedure(procedure);
//				setCurrentProcedure(procedure);
//				String declare = "\nprocedure "+procedureName+"(header:Ref);\n";
//				declare += "	ensures (isValid[header] == true);\n";
//				getCurrentProcedure().declare = declare;
////				addModifiedGlobalVariable("isValid");
//				
//				String body = "";
//				body += "{\n";
//				incIndent();
//				for(StructField field:headers.get(name).fields) {
//					start += field.len;
//				}
//				decIndent();
//				body += "}\n";
//				getCurrentProcedure().body = body;
			}
		}
		code += "\ntype packet_in = bv"+totalLen+";\n";
		code += "const packet:packet_in;\n";
		
		// TODO header variables name may not be hdr
		code += "\nvar hdr:Ref;\n";
		code += "\nvar meta:Ref;\n";
		code += "\nvar standard_metadata:Ref;\n";
		
//		code += "\nvar hdr:headers;\n";
//		code += "\nvar meta:metadata;\n";
//		code += "\nvar standard_metadata:standard_metadata_t;\n";
		
//		code += "\nvar packet.map:[int]bv1;\n";
//		code += "\nvar packet.index:int;\n";
		addBoogieGlobalVariable("hdr");
		addBoogieGlobalVariable("meta");
		addBoogieGlobalVariable("standard_metadata");
//		addBoogieGlobalVariable("packet.map");
//		addBoogieGlobalVariable("packet.index");
		return code;
	}
	
	String p4_to_Boogie_emit() {
		String packetoutName = "packet_o";
		String code = "";
		code += "\n//type packet_out = packet_in;\n";
		code += "//var "+packetoutName+":packet_in;\n";
		addBoogieGlobalVariable(packetoutName);
		
		Type_Struct myheaders = structs.get(headersName);
//		int totalLen = myheaders.length();
//		int start = 0; //for extracting
		for(StructField headersField:myheaders.fields) {
			if(headersField.type.Node_Type.equals("Type_Stack")) {
				Type_Stack ts = (Type_Stack)headersField.type;
				String name = ts.name;
				String procedureName = "packet_out.emit.headers."+headersField.name;
				BoogieProcedure procedure = new BoogieProcedure(procedureName);
				addProcedure(procedure);
				setCurrentProcedure(procedure);
				String declare = "\nprocedure {:inline 1} "+procedureName+"(stack:"+"HeaderStack"+", index:int)\n";
//				String declare = "\nprocedure {:inline 1} "+procedureName+"(stack:"+name+", index:int)\n";
				getCurrentProcedure().declare = declare;
				addModifiedGlobalVariable("emit");
				
				// body starts
				incIndent();
				String ifStart = addIndent();
				ifStart += "if(isValid[stack[index]]) {\n";
				String ifEnd = addIndent()+"}\n";
				BoogieIfStatement boogieIfStatement = new BoogieIfStatement(ifStart, ifEnd);
				addBoogieBlock(boogieIfStatement);
				incIndent();
				addBoogieStatement(addIndent()+"emit[stack[index]] := true;\n");
				decIndent();
				popBoogieBlock();
				// body ends
				decIndent();
			}
			else {
				String name = headersField.getTypeName();
				String procedureName = "packet_out.emit.headers."+headersField.name;
				BoogieProcedure procedure = new BoogieProcedure(procedureName);
				procedure.implemented = false;
				addProcedure(procedure);
				setCurrentProcedure(procedure);
				String declare = "\nprocedure "+procedureName+"(header:Ref);\n";
				declare += "	ensures isValid[header]!=true || emit[header]==true;\n";
				getCurrentProcedure().declare = declare;
			}
		}
		return code;
	}
	
	void analyzeControlFlow() {
		HashSet<String> branchVariables = new HashSet<>();
		for(IfStatement ifStatement:ifStatements) {
			HashSet<String> tmp = ifStatement.getBranchVariables();
			if(tmp != null)
				branchVariables.addAll(tmp);
		}
		for(SwitchStatement switchStatement:switchStatements) {
			HashSet<String> tmp = switchStatement.getBranchVariables();
			if(tmp != null)
				branchVariables.addAll(tmp);
		}
		System.out.println(branchVariables.size());
		System.out.println(assignmentStatements.size());
		
		// store the variables dependency graph
		HashMap<String, HashSet<String>> variableDependency = new HashMap<>();
		
		// store all variables in a statement
		HashMap<Integer, HashSet<String>> assignment = new HashMap<>();
		
		for(AssignmentStatement assignmentStatement:assignmentStatements) {
			HashSet<String> left, right;
			left = assignmentStatement.left.getBranchVariables();
			right = assignmentStatement.right.getBranchVariables();
			if(left!=null && right!=null) {
				// all variables that the assignment statement contains
				HashSet<String> all = new HashSet<>();
				all.addAll(left);
				all.addAll(right);
				assignment.put(assignmentStatement.Node_ID, all);
				
				for(String key:left) {
					if(!variableDependency.containsKey(key)) {
						HashSet<String> set = new HashSet<>();
						variableDependency.put(key, set);
					}
					variableDependency.get(key).addAll(right);
				}
			}
		}
		
		// update branchVariables
		ArrayList<String> queue = new ArrayList<>();
		HashMap<String, Boolean> inQueue = new HashMap<>();
		queue.addAll(branchVariables);
		for(String var:branchVariables)
			inQueue.put(var, true);
		System.out.println("Here");
		while(!queue.isEmpty()) {
			String var = queue.get(0);
			queue.remove(0);
			inQueue.put(var, false);
			if(!variableDependency.containsKey(var))
				continue;
			for(String dependencyVar:variableDependency.get(var)) {
				if(!branchVariables.contains(dependencyVar)) {
					branchVariables.add(dependencyVar);
					queue.add(dependencyVar);
					inQueue.put(dependencyVar, true);
				}
			}
		}
		System.out.println(branchVariables.size());
		
		HashSet<Integer> usefulAssignment = new HashSet<>();
		for(int id:assignment.keySet()) {
			HashSet<String> tmp = new HashSet<>();
			tmp.addAll(assignment.get(id));
			tmp.retainAll(branchVariables);
			if(tmp.isEmpty())
				usefulAssignment.add(id);
		}
		System.out.println("useful assign: "+usefulAssignment.size());
		System.out.println("useless assign: "+(assignmentStatements.size()-usefulAssignment.size()));
	}

	String p4_to_Boogie(Node program) {
		analyzeControlFlow();
		System.out.println("######## Unhandled Types ########");
		String [] handledTypes = {"Path", "Type_Name", "StructField", "Type_Struct",
				"MethodCallStatement", "Constant", "MethodCallExpression", "Type_Header",
				"P4Program", "Type_Typedef", "BlockStatement", "AssignmentStatement",
				"LNot", "LAnd", "Add", "Sub", "Mul", "Shl", "BAnd", "BOr", "BXor",
				"Declaration_Instance", "Type_Specialized", "IfStatement"};
		HashSet<String> mytypes = new HashSet<>();
		mytypes.addAll(types);
		for(String str : handledTypes){
			mytypes.remove(str);
		}
		for(String type : mytypes) {
			System.out.println(type);
		}
		String code = p4_to_Boogie_Main_Declaration();
		code += program.p4_to_Boogie();
		// Add global declarations
		code += globalBoogieDeclarationBlock.toBoogie();
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
	private LinkedHashMap<String, BoogieProcedure> procedures;
	private BoogieProcedure currentProcedure;
	private BoogieBlockStack blockStack;
	private BoogieBlock globalBoogieDeclarationBlock;
	private HashSet<String> globalBoogieDeclaration;
	private HashMap<String, String> boogieFunctions; //SMT bit-vector
	private String headersName;
	private BoogieProcedure mainProcedure;
	private Commands commands;
	
	// For analyzing control flow
	private ArrayList<AssignmentStatement> assignmentStatements;
	private ArrayList<IfStatement> ifStatements;
	private ArrayList<SwitchStatement> switchStatements;

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
	
	void addMainPreBoogieStatement(String cont) {
		BoogieStatement statement = new BoogieStatement(cont);
		mainProcedure.preBlock.addToFirst(statement);
	}
	
	void addMainBoogieStatement(String cont) {
		BoogieStatement statement = new BoogieStatement(cont);
		mainProcedure.mainBlock.addToFirst(statement);
	}

	BoogieProcedure getCurrentProcedure() {
		return this.currentProcedure;
	}
	
	BoogieProcedure getMainProcedure() {
		return this.mainProcedure;
	}

	void addModifiedGlobalVariable(String var) {
		if(currentProcedure!=null && globalVariables.contains(var))
			currentProcedure.updateModifies(var);
	}

	void addBoogieGlobalVariable(String var) {
		globalVariables.add(var);
	}
	
	void addBoogieGlobalDeclaration(String cont) {
		if(globalBoogieDeclaration.contains(cont))
			return;
		BoogieStatement statement = new BoogieStatement(cont);
		globalBoogieDeclarationBlock.add(statement);
		globalBoogieDeclaration.add(cont);
	}

	void addBoogieFunction(String name, String cont) {
		if(!boogieFunctions.containsKey(name))
			boogieFunctions.put(name, cont);
	}
	
	void setHeadersName(String headersName) {
		this.headersName = headersName;
	}
	
	// set current procedure type to parser state
	void setParserState() {
		if(currentProcedure!=null) {
			parserStates.add(currentProcedure.name);
//			for(String var:parserLocals) {
//				currentProcedure.updateModifies("parser."+var);
//			}
		}
	}
	
	// if current procedure is parser state
	boolean isParserState() {
		if(currentProcedure!=null)
			return parserStates.contains(currentProcedure.name);
		return false;
	}
	
	void addParserLocal(String var) {
		parserLocals.add(var);
	}
	
	boolean isParserLocal(String var) {
		return parserLocals.contains(var);
	}
	
	boolean isTypeDef(String var) {
		return typeDefLength.containsKey(var);
	}

	Type_Header getHeader(String name) {
		return headers.get(name);
	}
	
	Commands getCommands() {
		return commands;
	}
	
	void addIfStatement(IfStatement ifStatement) {
		ifStatements.add(ifStatement);
	}
	
	void addAssignmentStatement(AssignmentStatement assignmentStatement) {
		assignmentStatements.add(assignmentStatement);
	}
	
	void addSwitchStatement(SwitchStatement switchStatement) {
		switchStatements.add(switchStatement);
	}
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
