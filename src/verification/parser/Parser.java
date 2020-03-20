package verification.parser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Parser {
	private String jsonFileName;
	static HashSet<String> types;
	static HashMap<Integer, ObjectNode> allJsonNodes;
	private static Parser instance;
	public static Parser getInstance() {
		if(instance == null) {
			instance = new Parser();
		}
		return instance;
	}
	
	private Parser() {
		types = new HashSet<>();
		allJsonNodes = new HashMap<>();
	}
	
//	public Parser(String filename) {
//		jsonFileName = filename;
//		types = new HashSet<>();
//		allJsonNodes = new HashMap<>();
//	}
	
//	public void parse() {
//		parse(jsonFileName);
//	}
	
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
			
			// "Type_Struct", "Type_Typedef",
			// for testing unhandled types
			String [] handledTypes = {"P4Program", "Type_Error", "Type_Extern", "Type_Header", "StructField",
					"Type_Bits", "Type_Name", "Path", 
					"Parameter", "ParameterList", "PathExpression", "Member", "P4Parser", "Type_Parser",
					"MethodCallStatement", "MethodCallExpression", "Constant", "ParserState",
					"Type_Control", "BlockStatement", "AssignmentStatement", "Add", "Sub", "LAnd", "LOr",
					"BAnd", "BOr", "BXor", "Geq", "Leq", "LAnd", "LOr", "Shl", "Shr", "Mul", "LNot",
					"IfStatement", "ActionListElement"};
//			String [] handledTypes = {"P4Program", "Type_Error", "Type_Extern", "Type_Header", "StructField",
//					"Type_Bits", "Type_Name", "Path", "Type_Struct", "Type_Typedef", 
//					"Parameter", "ParameterList", "PathExpression"};
			for(String str : handledTypes){
				types.remove(str);
			}
			System.out.println("######## Unhandled Types ########");
			for(String type : types) {	
				System.out.println(type);
			}
			System.out.println("######## Program ########");
			System.out.println(program.p4_to_C());
			
			long endTime = System.currentTimeMillis(); 
			System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
		}catch(JsonProcessingException e) {
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}
//		File file = new File(filename);
//		if(!file.exists()) {
//			System.out.println("ERROR: File doesn't exist.");
//			return;
//		}
//		System.out.println("parse:");
//		System.out.println("####################");
//		
//		// Read json file
//		BufferedReader bufferedReader;
//		try{
//			long startTime = System.currentTimeMillis();
//			
//			bufferedReader = new BufferedReader(new FileReader(file));
//			StringBuffer stringBuffer = new StringBuffer();
//			String tmpstr;
//			while((tmpstr = bufferedReader.readLine())!=null) {
//				stringBuffer.append(tmpstr);
//			}
//			bufferedReader.close();
//			String res = stringBuffer.toString();
////			JSONObject rootNode = JSONObject.fromObject(res);
//			System.out.println("json file reading ends");
//			
//			long endTime = System.currentTimeMillis(); 
//			System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
//			
////			allJsonNodes.put(rootNode.getInt(JsonKeyName.NODE_ID), rootNode);
////			getAllNodes(rootNode);
////			System.out.println("getAllNodes() ends");
////			System.out.println(allJsonNodes.size());
////			System.out.println(allJsonNodes.get(136));
//			
//			// parse
////			Node program = jsonParse(rootNode);
////			System.out.println("jsonParse() ends");
////			
////			// for testing unhandled types
////			String [] handledTypes = {"P4Program", "Type_Error", "Type_Extern", "Type_Header", "StructField",
////					"Type_Bits", "Type_Name", "Path", "Type_Struct", "Type_Typedef", 
////					"Parameter", "ParameterList", "PathExpression"};
////			for(String str : handledTypes){
////				types.remove(str);
////			}
////			System.out.println("######## Unhandled Types ########");
////			for(String type : types) {	
////				System.out.println(type);
////			}
////			System.out.println("######## Program ########");
////			System.out.println(program.p4_to_C());
//			
//		} catch(IOException e) {
//			e.printStackTrace();
//		}
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
//				for(String key : (Set<String>)rootNode.fieldNames()) {
//					JSONObject node_at_id = allJsonNodes.get(id);
//					if(!node_at_id.containsKey(key))
//						node_at_id.put(key, rootNode.get(key));
//				}
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
////		System.out.println(typeName);
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
//			System.out.println(object.getInt(JsonKeyName.NODE_ID));
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
	
	public static ObjectNode getJsonNode(int key) {
		if(allJsonNodes == null || !allJsonNodes.containsKey(key))
			return null;
		return allJsonNodes.get(key);
	}
	
//	public static JSONObject getJsonNode(JSONObject object) {
//		int id = object.getInt(JsonKeyName.NODE_ID);
//		return getJsonNode(id);
//	}
}
