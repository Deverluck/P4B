package verification.parser;

import java.util.ArrayList;

import net.sf.json.JSONObject;

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
	
	boolean parse(JSONObject object) {
//		System.out.println(object);
		Node_ID = object.getInt(JsonKeyName.NODE_ID);
		Node_Type = object.getString(JsonKeyName.NODE_TYPE);
		return true;
	}
	
	String p4_to_C() {
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