package verification.p4verifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;

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
	private HashMap<String, BoogieProcedure> parserStates;
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
		parserStates = new HashMap<>();
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
		initContext();

		headerToParserStates = new HashMap<>();
		procedurePreconditions = new HashMap<>();
		assertStatements = new LinkedHashSet<>();
		procedureSetValidHeaders = new HashMap<>();
		cnt = 0;
		
		result = new VerificationResult();
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

//			System.out.println("####################");
			System.out.println("Parse starts");
			ObjectNode rootNode = (ObjectNode)mapper.readTree(file);
			getAllNodes(rootNode);
			System.out.println("Finish updating node attributes");
			System.out.println("    Total: "+allJsonNodes.size()+" AST nodes");

			//parse
			Node program = jsonParse(rootNode);
			System.out.println("Finish analyzing AST");

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
				Class<?> nodeClass = Class.forName("verification.p4verifier."+typeName);
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
		if(!getCommands().ifUseCorral()) {
			code += "type Field T;\n";
			code += "type HeapType = <T>[Ref, Field T]T;\n";
			code += "var Heap:HeapType;\n";
		}

		code += "type HeaderStack = [int]Ref;\n";

		code += "var last:[HeaderStack]Ref;\n";
		addBoogieGlobalVariable("Heap");
		
//		code += "type HeaderStack = <T>[int]T";
		return code;
	}
	
	String p4_to_Boogie_Header_isValid() {
		String code = "\nvar isValid:[Ref]bool;\n";
		addBoogieGlobalVariable("isValid");
		
		code += "\nvar emit:[Ref]bool;\n";
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
		code += "var stack.index:[HeaderStack]int;\n";
		addBoogieGlobalVariable("stack.index");
		code += "var size:[HeaderStack]int;\n";
		addBoogieGlobalVariable("size");
		
		BoogieProcedure initStackIndex = new BoogieProcedure("init.stack.index");
		initStackIndex.implemented = false;
		addProcedure(initStackIndex);
		String declare3 = "\nprocedure init.stack.index();\n";
		initStackIndex.modifies.add("stack.index");
		declare3 += "ensures (forall s:HeaderStack::stack.index[s]==0);\n";
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
		String procedureName = "clear_drop";
		BoogieProcedure clear_drop = new BoogieProcedure(procedureName);
		clear_drop.implemented = false;
		addProcedure(clear_drop);
		setCurrentProcedure(clear_drop);
		clear_drop.declare = "\nprocedure "+procedureName+"();\n";
		clear_drop.declare += "	ensures drop==false;\n";
		addModifiedGlobalVariable("drop");
		addMainPreBoogieStatement("	call clear_drop();\n");
		mainProcedure.childrenNames.add(procedureName);
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
//				incIndent();
//				addBoogieStatement(addIndent()+"isValid[header] := true;\n");
//				decIndent();
				addModifiedGlobalVariable("isValid");
				
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
		code += "\nconst hdr:Ref;\n";
		code += "\nconst meta:Ref;\n";
		code += "\nconst standard_metadata:Ref;\n";
		
//		code += "\nvar hdr:headers;\n";
//		code += "\nvar meta:metadata;\n";
//		code += "\nvar standard_metadata:standard_metadata_t;\n";
		
//		code += "\nvar packet.map:[int]bv1;\n";
//		code += "\nvar packet.index:int;\n";
//		addBoogieGlobalVariable("hdr");
//		addBoogieGlobalVariable("meta");
//		addBoogieGlobalVariable("standard_metadata");
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
				addModifiedGlobalVariable("emit");
			}
		}
		return code;
	}
	
	/******** Implicit Drop ********/
	String implicitDrop() {
		String code = "";
		code += "var forward:bool;\n";
		addBoogieGlobalVariable("forward");
		
		String procedureName = "clear_forward";
		BoogieProcedure procedure = new BoogieProcedure(procedureName);
		procedure.implemented = false;
		addProcedure(procedure);
		setCurrentProcedure(procedure);
		String declare = "\nprocedure "+procedureName+"();\n";
		declare += "	ensures forward==false;\n";
		procedure.declare = declare;
		addModifiedGlobalVariable("forward");
		addMainPreBoogieStatement("	call clear_forward();\n");
		mainProcedure.childrenNames.add(procedureName);
		return code;
	}
	
	HashSet<Integer> analyzeControlFlow() {
//		int cnt = 0;
//		for(AssignmentStatement assignmentStatement:assignmentStatements) {
//			if(assignmentStatement.isInParserState())
//				cnt++;
//		}
//		System.out.println("in parser state: "+cnt);
		
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
		
		// store the variables dependency graph
		HashMap<String, HashSet<String>> variableDependency = new HashMap<>();
		
		// store all variables in a statement
		HashMap<Integer, HashSet<String>> assignment = new HashMap<>();
		
		for(AssignmentStatement assignmentStatement:assignmentStatements) {
			if(assignmentStatement.isInParserState())
				continue;
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
		
		HashSet<Integer> usefulAssignment = new HashSet<>();
		for(int id:assignment.keySet()) {
			HashSet<String> tmp = new HashSet<>();
			tmp.addAll(assignment.get(id));
			tmp.retainAll(branchVariables);
			if(tmp.isEmpty())
				usefulAssignment.add(id);
		}
		System.out.println("Analyze useless assignment statements:");
		System.out.println("    useful assign: "+usefulAssignment.size());
		System.out.println("    useless assign: "+(assignmentStatements.size()-usefulAssignment.size()));
		return usefulAssignment;
	}
	
	boolean isUsefulAssignmentStatement(int id) {
		if(usefulAssignmentStatements!=null)
			return usefulAssignmentStatements.contains(id);
		return false;
	}

	String p4_to_Boogie(Node program) {
		usefulAssignmentStatements = analyzeControlFlow();
//		System.out.println("######## Unhandled Types ########");
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
//			System.out.println(type);
		}
		String code = p4_to_Boogie_Main_Declaration();
		
		code += implicitDrop();
		
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
		
		if(commands.ifConstrainControlPlane()) {
			for(P4Table table:tables) {
				table.addAssumeStatement();
			}
		}

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
		
		if(getCommands().ifCheckForwardOrDrop()) {
			addMainPostBoogieStatement("\n	// Check Implicit Drop\n	assert(forward||drop);\n");
		}
		
		BoogieProcedureOperator bpo = new BoogieProcedureOperator();
		for(String name:procedures.keySet()) {
//			procedures.get(name).setPreCondition(getProcedurePrecondition(name));
			bpo.addProcedure(procedures.get(name));
		}
		bpo.updateModify();
		if(getCommands().ifCheckHeaderValidity()) {
			System.out.println("Updating procedure modifies set and precondition");
			bpo.updateCondition(ctx);
//			bpo.update(ctx);
			System.out.println("Updating assert statement condition");
			updateBoogieAssertStatementCondition();
		}
		
		System.out.println("Generating Boogie code");
		for(BoogieProcedure procedure:bpo.procedures) {
			code += procedure.toBoogie();
//			System.out.println(procedure.toBoogie());
		}
		
		System.out.println();
		if(cnt == 0)
			System.out.println("Header Validity: "+cnt+" bug");
		else
			System.out.println("Header Validity: "+cnt+" bugs");
		result.show();
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
	private HashSet<Integer> usefulAssignmentStatements;
	
	// For analyzing conditions
	private Context ctx;
	private HashMap<String, ArrayList<BoogieProcedure>> headerToParserStates;
//	private HashMap<String, ArrayList<BoolExpr>> headerValidConditions;
	private HashMap<String, HashMap<String, BoolExpr>> procedurePreconditions;
//	private ArrayList<BoogieAssertStatement> assertStatements;
	private LinkedHashSet<BoogieAssertStatement> assertStatements;
	private HashMap<BoogieProcedure, HashMap<String, BoolExpr>> procedureSetValidHeaders;
	
	// Verification Result
	private VerificationResult result;

	private void initContext() {
		HashMap<String, String> cfg = new HashMap<String, String>();
        cfg.put("model", "true");
        ctx = new Context(cfg);
	}
	
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
		if(cont.contains("](") || cont.contains("clone3") || cont.contains(", ,") || cont.contains(" .") ||
				cont.contains(", )") || cont.contains(".push_front(") || cont.contains(".pop_front(") ||
				cont.contains("random"))
			return;
		BoogieStatement statement = new BoogieStatement(cont);
		addBoogieStatement(statement);
	}
	
	LinkedHashSet<BoogieAssertStatement> getAssertStatements(){
		return assertStatements;
	}
	
	// Used when inserting assert statements
	boolean isAssertStatementDuplicate(String cont, BoolExpr condition, String procedureName) {
		if(!commands.ifRemoveRedundantAssertions())
			return false;
		for(BoogieAssertStatement statement:assertStatements) {
			if(statement.procedureName.equals(procedureName)&&
					statement.cont.equals(cont)&&statement.condition.toString().equals(condition.toString())) {
				return true;
			}
		}
		return false;
	}
	
	void addBoogieAssertStatement(String cont) {
		BoogieAssertStatement statement = new BoogieAssertStatement(cont, currentProcedure.name);
		addBoogieStatement(statement);
	}
	
	void addBoogieAssertStatement(String cont, BoolExpr expr) {
		BoogieAssertStatement statement = new BoogieAssertStatement(cont, currentProcedure.name);
		BoolExpr condition = expr;
		for(BoolExpr e:getCurrentProcedure().getConditions()) {
			condition = getContext().mkAnd(condition, e);
		}
		if(isAssertStatementDuplicate(cont, condition, statement.procedureName))
			return;
		statement.setCondition(condition);
		addBoogieStatement(statement);
		assertStatements.add(statement);
	}
	
	void showAssertStatement(BoogieHeaderValidityAssertStatement s) {
//		System.out.println("In procedure "+getCurrentProcedure().name);
//		System.out.println(s.cont);
	}
	
	void addBoogieAssertStatement(String cont, String headerName) {
		result.headerValidityAssertionTotal.inc();
		BoogieHeaderValidityAssertStatement statement = new BoogieHeaderValidityAssertStatement(cont, headerName, currentProcedure.name);
		BoolExpr condition = getContext().mkBool(true);
		for(BoolExpr expr:getCurrentProcedure().getConditions()) {
			condition = getContext().mkAnd(condition, expr);
		}
		if(isAssertStatementDuplicate(cont, condition, statement.procedureName))
			return;
		statement.setCondition(condition);
		addBoogieStatement(statement);
		assertStatements.add(statement);
		showAssertStatement(statement);
	}
	
	void addBoogieAssertStatement(String cont, String headerName, BoolExpr c) {
		result.headerValidityAssertionTotal.inc();
		BoogieHeaderValidityAssertStatement statement = new BoogieHeaderValidityAssertStatement(cont, headerName, currentProcedure.name);
		BoolExpr condition = c;
		for(BoolExpr expr:getCurrentProcedure().getConditions()) {
			condition = getContext().mkAnd(condition, expr);
		}
		if(isAssertStatementDuplicate(cont, condition, statement.procedureName))
			return;
		statement.setCondition(condition);
		addBoogieStatement(statement);
		assertStatements.add(statement);
		showAssertStatement(statement);
	}
	
	void updateBoogieAssertStatementCondition() {
		for(BoogieAssertStatement statement:assertStatements) {
			if(statement instanceof BoogieHeaderValidityAssertStatement) {
				
				BoogieHeaderValidityAssertStatement bhvas = (BoogieHeaderValidityAssertStatement)statement;
				BoolExpr expr = statement.condition;
				ArrayList<BoogieProcedure> states = fromHeaderToParserStates(bhvas.headerName); 
				if(states!=null) {
					BoolExpr expr2 = null;
					for(BoogieProcedure procedure:states) {
						BoolExpr preCondition = procedure.getPreCondition();
						if(preCondition!=null) {
							if(expr2 == null)
								expr2 = preCondition;
							else
								expr2 = ctx.mkOr(expr2, preCondition);
						}
						else {
							expr2 = ctx.mkBool(true);
							break;
						}
					}
					if(expr2!=null) {
//						if(states.get(0).name.equals("parse_fabric_header_cpu")) {
//							System.out.println("before updating:");
//							System.out.println(expr);
//							System.out.println(expr2);
//						}
						expr = ctx.mkAnd(expr, ctx.mkNot(expr2));
						statement.setCondition(expr);
//						if(states.get(0).name.equals("parse_fabric_header_cpu")) {
//							System.out.println("after updating:");
//							System.out.println(expr);
//						}
					}
				}
			}
		}
	}
	
	void addMainPreBoogieStatement(String cont) {
		BoogieStatement statement = new BoogieStatement(cont);
		mainProcedure.preBlock.addToFirst(statement);
	}
	
	void addMainBoogieStatement(String cont) {
		BoogieStatement statement = new BoogieStatement(cont);
		mainProcedure.mainBlock.addToFirst(statement);
	}
	
	void addMainPostBoogieStatement(String cont) {
		BoogieStatement statement = new BoogieStatement(cont);
		mainProcedure.postBlock.addToFirst(statement);
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
			parserStates.put(currentProcedure.name, currentProcedure);
//			for(String var:parserLocals) {
//				currentProcedure.updateModifies("parser."+var);
//			}
		}
	}
	
	// if current procedure is parser state
	boolean isParserState() {
		if(currentProcedure!=null)
			return parserStates.containsKey(currentProcedure.name);
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
	
	Type_Struct getStruct(String name) {
		return structs.get(name);
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
	
	void updateCondition(BoolExpr expr) {
		getCurrentProcedure().addCondition(expr);
	}
	
	boolean popCondition() {
		return getCurrentProcedure().popCondition();
	}
	
	Context getContext() {
		return ctx;
	}
	
	void addHeaderValidParserState(String headerName) {
		if(isParserState()) {
			if(!headerToParserStates.containsKey(headerName)) {
				ArrayList<BoogieProcedure> procedures = new ArrayList<>();
				headerToParserStates.put(headerName, procedures);
			}
			headerToParserStates.get(headerName).add(currentProcedure);
		}
	}
	
	ArrayList<BoogieProcedure> fromHeaderToParserStates(String headerName) {
		if(!headerToParserStates.containsKey(headerName))
			return null;
		return headerToParserStates.get(headerName);
	}
	
//	void updateProcedureCondition() {
//		BoogieProcedureOperator bpo = new BoogieProcedureOperator();
//		for(String name:procedures.keySet()) {
////			System.out.println(name);
//			procedures.get(name).setPreCondition(getProcedurePrecondition(name));
//			bpo.addProcedure(procedures.get(name));
//		}
//		bpo.updateCondition(ctx);
////		for(String key:headerToParserStates.keySet()) {
////			System.out.println(key+":");
////			for(BoogieProcedure procedure:headerToParserStates.get(key))
////				System.out.println("	"+procedure.name);
////		}
////		System.out.println(headerToParserStates.size());
////		System.out.println(procedurePreconditions);
//	}
	
	BoolExpr getCurrentCondition() {
		if(currentProcedure==null)
			return null;
		BoolExpr expr = ctx.mkBool(true);
		for(BoolExpr e:currentProcedure.getConditions()) {
			expr = ctx.mkAnd(expr, e);
		}
		return expr;
	}
	
	void addProcedurePrecondition(String procedureName) {
		if(procedureName.contains("](") || procedureName.contains("clone3") || procedureName.contains(", ,") || procedureName.contains(" .") ||
				procedureName.contains(", )") || procedureName.contains(".push_front(") || procedureName.contains(".pop_front(") ||
				procedureName.contains("random") || procedureName.contains("["))
			return;
		if(!procedurePreconditions.containsKey(procedureName)) {
			HashMap<String, BoolExpr> map = new HashMap<>();
			procedurePreconditions.put(procedureName, map);
		}
		if(currentProcedure!=null) {
			// Or
			if(!procedurePreconditions.get(procedureName).containsKey(currentProcedure.name)) {
				procedurePreconditions.get(procedureName).put(currentProcedure.name, getCurrentCondition());
			}
			else {
				BoolExpr condition = procedurePreconditions.get(procedureName).get(currentProcedure.name);
				procedurePreconditions.get(procedureName).put(currentProcedure.name, ctx.mkOr(condition, getCurrentCondition()));
			}
		}
//		System.out.println()
	}
	
	
	
	BoolExpr getProcedurePrecondition(String procedureName, String caller) {
		if(!procedurePreconditions.containsKey(procedureName))
			return null;
		if(!procedurePreconditions.get(procedureName).containsKey(caller))
			return null;
		return procedurePreconditions.get(procedureName).get(caller);
//		BoolExpr expr = ctx.mkBool(false);
//		for(BoolExpr e:procedurePreconditions.get(procedureName)) {
//			expr = ctx.mkOr(expr, e);
//		}
//		return expr;
	}
	
	BoogieProcedure getProcedrue(String procedureName) {
		if(!procedures.containsKey(procedureName))
			return null;
		return procedures.get(procedureName);
	}
	
	Solver createSolver() {
		return ctx.mkSolver();
	}
	
	int cnt;
	void count() {
		cnt++;
	}
	void decCount() {
		if(cnt>0)
			cnt--;
	}
	int getCount() {
		return cnt;
	}
	
	void addProcedureSetValidHeader(String headerName) {
		if(!procedureSetValidHeaders.containsKey(currentProcedure)) {
			HashMap<String, BoolExpr> map = new HashMap<>();
			procedureSetValidHeaders.put(currentProcedure, map);
		}
		BoolExpr condition = getContext().mkBool(true);
		for(BoolExpr expr:getCurrentProcedure().getConditions()) {
			condition = getContext().mkAnd(condition, expr);
		}
		procedureSetValidHeaders.get(currentProcedure).put(headerName, condition);
//		procedureSetValidHeaders.put(currentProcedure, hea)
	}
	
	BoolExpr getSetValidHeaderCondition(String headerName) {
		if(!procedureSetValidHeaders.containsKey(currentProcedure))
			return null;
		return procedureSetValidHeaders.get(currentProcedure).get(headerName);
	}
	
	boolean isSetValidInProcedure(String header, BoogieProcedure procedure) {
		if(!procedureSetValidHeaders.containsKey(procedure))
			return false;
		return procedureSetValidHeaders.get(procedure).containsKey(header);
	}
	
	VerificationResult getResult() {
		return result;
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
