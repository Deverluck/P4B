package verification.p4verifier;

import java.util.ArrayList;
import java.util.HashSet;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;

public class Node {
	int Node_ID;
	String Node_Type;
	ArrayList<Node> children;
	boolean enable;
	String T;
	boolean inParserState;

	public Node() {
		children = new ArrayList<>();
		enable = false;
		T = "";
		inParserState = false;
	}

	public void addChild(Node child) {
		children.add(child);
	}

	void parse(ObjectNode object) {
//		System.out.println(object);
		Node_ID = object.get(JsonKeyName.NODE_ID).asInt();
		Node_Type = object.get(JsonKeyName.NODE_TYPE).asText();
	}
	
	// mark the assignment statement in parser state
	void markParserStateAssignmentStatement() {
		for(Node child:children) {
			child.setInParserState();
		}
	}

	String p4_to_Boogie() {
		return "";
	}

	String p4_to_Boogie(String arg) {
		return p4_to_Boogie();
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

	String p4_to_C_declare() {
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
	
	void setInParserState() {
		inParserState = true;
	}
	
	boolean isInParserState() {
		return inParserState;
	}

	void setVectorType(String type) {
		T = type;
	}

	String addIndent() {
		return Parser.getInstance().addIndent();
	}

	void incIndent() {
		Parser.getInstance().incIndent();
	}

	void decIndent() {
		Parser.getInstance().decIndent();
	}

	String getTypeName() {
		return "";
	}
	
	// for methodCall extract()
	String getName() {
		return "";
	}
	
	String addAssertStatement() {
		return "";
	}
	
	/** 
	 * For some particular properties, useless statements can be deleted.
	 * Useful statements are those that may be related to branches.
	 * The parser should analyze the control flow to find useful statements 
	 * by the variables they may change.
	 * @return the variables related to branches
	 */
	HashSet<String> getBranchVariables() {
		return null;
	}
	
	/*
	 * Use Z3 to record conditions of execution paths.
	 * Purpose: remove useless assert statements
	 */
	BoolExpr getCondition() {
		return null;
	}
	BitVecExpr getBitVecExpr() {
		return null;
	}
}