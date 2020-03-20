package verification.parser;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Node {
	int Node_ID;
	String Node_Type;
	ArrayList<Node> children;
	boolean enable;
	String T;
	
	public Node() {
		children = new ArrayList<>();
		enable = false;
		T = "";
	}
	
	public void addChild(Node child) {
		children.add(child);
	}
	
	void parse(ObjectNode object) {
//		System.out.println(object);
		Node_ID = object.get(JsonKeyName.NODE_ID).asInt();
		Node_Type = object.get(JsonKeyName.NODE_TYPE).asText();
	}
	
	String p4_to_C() {
		return "";
	}
	
	String p4_to_C(String arg) {
		return p4_to_C();
	}
	
	// for declaring structs and important instances
	String p4_to_C_preprocess() {
		return "";
	}
	
	boolean isVector() {
		return false;
	}
	
	boolean setEnable() {
		enable = true;
		return enable;
	}
	
	boolean isEnable() {
		return enable;
	}
	
	void setVectorType(String type) {
		T = type;
	}
}