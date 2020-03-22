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
	private ArrayList<Type_Struct> structs;
	private ArrayList<Type_Typedef> typeDefinitions;
	
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
		structs = new ArrayList<>();
		typeDefinitions = new ArrayList<>();
	}
	
	private void clear() {
		types.clear();
		allJsonNodes.clear();
		tables.clear();
		controls.clear();
		actions.clear();
		structs.clear();
		typeDefinitions.clear();
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
			System.out.println(allJsonNodes.size());
			
			//parse
			Node program = jsonParse(rootNode);
			System.out.println("jsonParse() ends");
			
			// for testing unhandled types
			String [] handledTypes = {"P4Program", "Type_Error", "Type_Extern", "Type_Header", "StructField",
					"Type_Bits", "Type_Name", "Path", 
					"Parameter", "ParameterList", "PathExpression", "Member", "P4Parser", "Type_Parser",
					"MethodCallStatement", "MethodCallExpression", "Constant", "ParserState",
					"Type_Control", "BlockStatement", "AssignmentStatement", "Add", "Sub", "LAnd", "LOr",
					"BAnd", "BOr", "BXor", "Geq", "Leq", "LAnd", "LOr", "Shl", "Shr", "Mul", "LNot",
					"IfStatement", "ActionListElement", "P4Action", "Type_Struct"};
			for(String str : handledTypes){
				types.remove(str);
			}
			System.out.println("######## Unhandled Types ########");
			for(String type : types) {	
				System.out.println(type);
			}
			System.out.println("######## Program ########");
			String code = declare()+preprocess()+program.p4_to_C();
			System.out.println(code);
			
			long endTime = System.currentTimeMillis(); 
			System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
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
	
	public void addStruct(Type_Struct struct) {
		structs.add(struct);
	}
	
	public void addTypeDef(Type_Typedef typeDef) {
		typeDefinitions.add(typeDef);
	}
	
	// declare struct for controls and tables
	String preprocess() {
		String code = "";
		for(P4Control control : controls) {
			code += control.p4_to_C_preprocess();
		}
		for(P4Table table : tables) {
			code += table.p4_to_C_preprocess();
		}
		return code;
	}
	
	// declare methods for controls, tables and actions
	String declare() {
		String code = "";
		for(Type_Typedef typeDef : typeDefinitions) {
			code += typeDef.declare();
		}
		for(Type_Struct struct : structs) {
			code += struct.declare();
		}
		for(P4Control control : controls) {
			code += control.declare();
		}
		for(P4Table table : tables) {
			code += table.declare();
		}
		for(P4Action action : actions) {
			code += action.declare();
		}
		return code;
	}
}
