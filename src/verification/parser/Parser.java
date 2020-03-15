package verification.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Parser {
	private String jsonFileName;
	static HashSet<String> types;
	static HashMap<Integer, JSONObject> allJsonNodes;
	
	public Parser(String filename) {
		jsonFileName = filename;
		types = new HashSet<>();
		allJsonNodes = new HashMap<>();
	}
	
	public void parse() {
		parse(jsonFileName);
	}
	
	public void parse(String filename) {
		File file = new File(filename);
		if(!file.exists()) {
			System.out.println("ERROR: File doesn't exist.");
			return;
		}
		System.out.println("parse:");
		System.out.println("####################");
		
		// Read json file
		BufferedReader bufferedReader;
		try{
			long startTime = System.currentTimeMillis();
			
			bufferedReader = new BufferedReader(new FileReader(file));
			StringBuffer stringBuffer = new StringBuffer();
			String tmpstr;
			while((tmpstr = bufferedReader.readLine())!=null) {
				stringBuffer.append(tmpstr);
			}
			bufferedReader.close();
			String res = stringBuffer.toString();
//			JSONObject rootNode = JSONObject.fromObject(res);
			System.out.println("json file reading ends");
			
			long endTime = System.currentTimeMillis(); 
			System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
			
//			allJsonNodes.put(rootNode.getInt(JsonKeyName.NODE_ID), rootNode);
//			getAllNodes(rootNode);
//			System.out.println("getAllNodes() ends");
//			System.out.println(allJsonNodes.size());
//			System.out.println(allJsonNodes.get(136));
			
			// parse
//			Node program = jsonParse(rootNode);
//			System.out.println("jsonParse() ends");
//			
//			// for testing unhandled types
//			String [] handledTypes = {"P4Program", "Type_Error", "Type_Extern", "Type_Header", "StructField",
//					"Type_Bits", "Type_Name", "Path", "Type_Struct", "Type_Typedef", 
//					"Parameter", "ParameterList", "PathExpression"};
//			for(String str : handledTypes){
//				types.remove(str);
//			}
//			System.out.println("######## Unhandled Types ########");
//			for(String type : types) {	
//				System.out.println(type);
//			}
//			System.out.println("######## Program ########");
//			System.out.println(program.p4_to_C());
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	// get all nodes from JSON file and update their attributes
	public void getAllNodes(JSONObject rootNode) {
		if(rootNode.containsKey(JsonKeyName.NODE_ID)) {
			int id = rootNode.getInt(JsonKeyName.NODE_ID);
			if(allJsonNodes.containsKey(id)) {
				// update attributes
				for(String key : (Set<String>)rootNode.keySet()) {
					JSONObject node_at_id = allJsonNodes.get(id);
					if(!node_at_id.containsKey(key))
						node_at_id.put(key, rootNode.get(key));
				}
			}
			else {
				// add new node
				allJsonNodes.put(id, rootNode);
			}
		}
		
		Set<String> keyset = rootNode.keySet();
		for(String key : keyset) {
			Object child = rootNode.get(key);
			if(key.equals(JsonKeyName.VEC)) {
				JSONArray jsonArray = (JSONArray)child;
				for(Object jo : jsonArray.toArray()) {
					getAllNodes((JSONObject)jo);
				}
			}
			else if(child instanceof JSONObject) {
				JSONObject childNode = (JSONObject)child;
				getAllNodes(childNode);
			}
		}
	}
	
	public static Node jsonParse(JSONObject object) {
		object = getJsonNode(object.getInt(JsonKeyName.NODE_ID));
		String typeName = object.getString(JsonKeyName.NODE_TYPE);
//		System.out.println(typeName);
		try{
			Node node;
			if(object.has(JsonKeyName.VEC)) {
				node = new TypeVector();
			}
			else {
				types.add(typeName);
				Class nodeClass = Class.forName("verification.parser."+typeName);
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
	
	public static JSONObject getJsonNode(int key) {
		if(allJsonNodes == null || !allJsonNodes.containsKey(key))
			return null;
		return allJsonNodes.get(key);
	}
	
//	public static JSONObject getJsonNode(JSONObject object) {
//		int id = object.getInt(JsonKeyName.NODE_ID);
//		return getJsonNode(id);
//	}
}
