package verification.parser;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Statement extends Node {

}

class BlockStatement extends Statement {
	Node components;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		components = Parser.getInstance().jsonParse(object.get(JsonKeyName.COMPONENTS));
	}
	@Override
	String p4_to_C() {
		String code = components.p4_to_C();
		return code;
	}
	@Override
	String p4_to_Boogie() {
		BoogieBlock block = new BoogieBlock();
		Parser.getInstance().addBoogieBlock(block);
		
		String code = components.p4_to_Boogie();
		
		Parser.getInstance().popBoogieBlock();
		return code;
	}
}

class AssignmentStatement extends Statement {
	Node left;
	Node right;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		left = Parser.getInstance().jsonParse(object.get(JsonKeyName.LEFT));
		right = Parser.getInstance().jsonParse(object.get(JsonKeyName.RIGHT));
	}
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"="+right.p4_to_C()+";\n";
		return code;
	}

	@Override
	String p4_to_Boogie() {
		String leftCode = left.p4_to_Boogie();
		
		String modifiedVariable = leftCode;
		if(leftCode.contains("[")) {
			int idx = leftCode.indexOf("[");
			modifiedVariable = leftCode.substring(0, idx);
		}
		Parser.getInstance().addModifiedGlobalVariable(modifiedVariable);
		
		left.addAssertStatement();
		String code = addIndent()+leftCode+" := "+right.p4_to_Boogie()+";\n";
		if(right.p4_to_Boogie().equals("havoc")) {
			code = addIndent()+"havoc "+leftCode+";\n";
		}
		Parser.getInstance().addBoogieStatement(code);
		return code;
	}
}

class EmptyStatement extends Statement {

}

class IfStatement extends Statement {
	Node condition;
	Node ifTrue;
	Node ifFalse;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		condition = Parser.getInstance().jsonParse(object.get(JsonKeyName.CONDITION));
		ifTrue = Parser.getInstance().jsonParse(object.get(JsonKeyName.IFTRUE));
		if(object.has(JsonKeyName.IFFALSE)) {
			ifFalse = Parser.getInstance().jsonParse(object.get(JsonKeyName.IFFALSE));
		}
		else {
			ifFalse = null;
		}
	}
	@Override
	String p4_to_C() {
		String code = "if(";
		code += condition.p4_to_C();
		code += "){\n";
		code += ifTrue.p4_to_C();
		code += "}\n";
		if(ifFalse != null) {
			code += "else {\n";
			code += ifFalse.p4_to_C();
			code += "}\n";
		}
		return code;
	}
	@Override
	String p4_to_Boogie() {
		String code = addIndent()+"if(";
		code += condition.p4_to_Boogie();
		code += "){\n";
		
		String start = code;
		String end = addIndent()+"}\n";
		BoogieIfStatement blockIfTrue = new BoogieIfStatement(start, end);
		Parser.getInstance().addBoogieBlock(blockIfTrue);
		
		incIndent();
		code += ifTrue.p4_to_Boogie();
		decIndent();
		code += addIndent()+"}\n";
		
		Parser.getInstance().popBoogieBlock();
		
		if(ifFalse != null) {
			BoogieIfStatement blockIfFalse = new BoogieIfStatement(addIndent()+"else {\n",
					addIndent()+"}\n");
			Parser.getInstance().addBoogieBlock(blockIfFalse);
			
			code += addIndent()+"else {\n";
			incIndent();
			code += ifFalse.p4_to_Boogie();
			decIndent();
			code += addIndent()+"}\n";
			
			Parser.getInstance().popBoogieBlock();
		}
//		return super.p4_to_Boogie();
		return code;
	}
}

class MethodCallStatement extends Statement {
	Node methodCall;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		methodCall = Parser.getInstance().jsonParse(object.get(JsonKeyName.METHODCALL));
	}
	@Override
	String p4_to_C() {
		return methodCall.p4_to_C()+";\n";
	}
	@Override
	String p4_to_Boogie() {
//		String code = addIndent()+"call ";
//		code = code+methodCall.p4_to_Boogie()+";\n";
		
		//TODO support checksum
		String code = methodCall.p4_to_Boogie()+";\n";
		if(code.contains("update_checksum") || code.contains("verify_checksum"))
			return "";
		if(!code.contains(":="))
			code = "call "+code;
		if(code.contains("call ") && code.contains("(")) {
			int start = 5;
			int end = code.indexOf("(");
			String methodName = code.substring(start, end);
			Parser.getInstance().getCurrentProcedure().childrenNames.add(methodName);
		}
		code = addIndent()+code;
		Parser.getInstance().addBoogieStatement(code);
		return code;
//		return super.p4_to_Boogie();
	}
}

class SelectExpression extends Statement {
	ArrayList<Node> select; // may select more than one key
	ArrayList<ArrayList<Node>> cases_value;
	ArrayList<Node> cases;
	Node default_case;

	public SelectExpression() {
		super();
		select = new ArrayList<>();
		cases_value = new ArrayList<>();
		cases = new ArrayList<>();
		default_case = null;
	}

	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		ArrayNode select_vec = (ArrayNode)object.get(JsonKeyName.SELECT).get(JsonKeyName.COMPONENTS).get(JsonKeyName.VEC);
		for(JsonNode node : select_vec) {
			select.add(Parser.getInstance().jsonParse(node));
		}
		ArrayNode cases_vec = (ArrayNode)object.get(JsonKeyName.SELECTCASES).get(JsonKeyName.VEC);
		for(JsonNode node : cases_vec) {
			node = (JsonNode)node;
			if(!node.get(JsonKeyName.KEYSET).get(JsonKeyName.NODE_TYPE).asText().equals(JsonKeyName.DEFAULTEXPRESSION)) {
				ArrayList<Node> list = new ArrayList<>();
				// multiple keys
				if(node.get(JsonKeyName.KEYSET).has(JsonKeyName.COMPONENTS)) {
					ArrayNode tmpArrayNode = (ArrayNode)node.get(JsonKeyName.KEYSET).get(JsonKeyName.COMPONENTS).get(JsonKeyName.VEC);
					for(JsonNode tmpJsonNode : tmpArrayNode)
						list.add(Parser.getInstance().jsonParse(tmpJsonNode));
				}
				// single key
				else {
					list.add(Parser.getInstance().jsonParse(node.get(JsonKeyName.KEYSET)));
					//TODO Add support for Mask
				}
				cases_value.add(list);
				cases.add(Parser.getInstance().jsonParse(node.get(JsonKeyName.STATE)));
			}
			// default
			else
				default_case = Parser.getInstance().jsonParse(node.get(JsonKeyName.STATE));
		}
	}
	@Override
	String p4_to_C() {
		String code = "";
		int cnt1 = 0; // counter for keys (&&)
		int cnt2 = 0; // counter for cases (else if{})
		for(Node case_node : cases) {
			code += "if(";
			cnt1 = 0;
			for(Node select_node : select) {
				code += select_node.p4_to_C()+"=="+cases_value.get(cnt2).get(cnt1).p4_to_C();
				cnt1++;
				if(cnt1 < select.size())
					code += " && ";
			}
			code += "){\n"+case_node.p4_to_C(JsonKeyName.PARSERSTATE)+"}\n";
			cnt2++;
			if(cnt2 != cases.size())
				code += "else";
		}

		if(default_case != null) {
			if(cases.size()!=0)
				code += "else{\n" + default_case.p4_to_C(JsonKeyName.PARSERSTATE) + "}\n";
			else
				code += default_case.p4_to_C(JsonKeyName.PARSERSTATE);
		}
		// there may be no default case
		else if(cases.size()==1 && default_case == null) {
			code += "else{\n" + cases.get(0).p4_to_C(JsonKeyName.PARSERSTATE) + "}\n";
		}
		return code;
	}

	@Override
	String p4_to_Boogie() {
		// TODO Add support for Mask
		String code = "";
		int cnt1 = 0; // counter for keys (&&)
		int cnt2 = 0; // counter for cases (else if{})
		for(Node case_node : cases) {
			String condition = "	if(";
			if(cnt2 != 0)
				condition = "	else if(";
			
			cnt1 = 0;
			for(Node select_node : select) {
				// TODO deal with argument types (equal width)
				//condition += select_node.p4_to_Boogie()+" == ";
				Node caseValue = cases_value.get(cnt2).get(cnt1);
				if(caseValue instanceof Constant) {
					condition += select_node.p4_to_Boogie()+" == ";
					condition += caseValue.p4_to_Boogie();
				}
				else if(caseValue instanceof Mask) {
					String function = caseValue.p4_to_Boogie();
					Mask mask = (Mask)caseValue;
					condition += function+"("+select_node.p4_to_Boogie()+", "+mask.right.p4_to_Boogie()+") == ";
					condition += function+"("+mask.left.p4_to_Boogie()+", "+mask.right.p4_to_Boogie()+")";
				}
				cnt1++;
				if(cnt1 < select.size())
					condition += " && ";
			}
			condition += "){\n";
			
			BoogieIfStatement ifStatement = new BoogieIfStatement(condition, "	}\n");
			Parser.getInstance().addBoogieBlock(ifStatement);
			
			code += condition;
			incIndent();
			code += case_node.p4_to_Boogie(JsonKeyName.PARSERSTATE)+"	}\n";
			decIndent();

			Parser.getInstance().popBoogieBlock();
			cnt2++;
		}

//		if(default_case != null) {
//			if(cases.size()!=0)
//				code += "else{\n" + default_case.p4_to_C(JsonKeyName.PARSERSTATE) + "}\n";
//			else
//				code += default_case.p4_to_C(JsonKeyName.PARSERSTATE);
//		}
//		// there may be no default case
//		else if(cases.size()==1 && default_case == null) {
//			code += "else{\n" + cases.get(0).p4_to_C(JsonKeyName.PARSERSTATE) + "}\n";
//		}
		return code;
	}
}

class SwitchStatement extends Statement {
	Node expression;
	ArrayList<SwitchCase> cases;
	public SwitchStatement() {
		cases = new ArrayList<>();
	}
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		expression = Parser.getInstance().jsonParse(object.get(JsonKeyName.EXPRESSION));
		ArrayNode array = (ArrayNode)object.get(JsonKeyName.CASES).get(JsonKeyName.VEC);
		for(JsonNode node:array) {
			cases.add((SwitchCase)Parser.getInstance().jsonParse(node));
		}
	}
	@Override
	String p4_to_Boogie() {
		String expr = expression.p4_to_Boogie();
		int flag = 0;
		SwitchCase defaultCase = null;
		for(SwitchCase sc:cases) {
			if(sc.isDefault) {
				defaultCase = sc;
				continue;
			}
			String ifStart = addIndent();
			String ifEnd = addIndent()+"}\n";
			if(flag == 0) {
				flag = 1;
			}
			else {
				ifStart += "else ";
			}
			ifStart += "if("+expr+"==";
			if(expr.contains(".action_run"))
				ifStart += "action."+sc.label.p4_to_Boogie();
			ifStart += "){\n";
			BoogieIfStatement ifBlock = new BoogieIfStatement(ifStart, ifEnd);
			Parser.getInstance().addBoogieBlock(ifBlock);
			incIndent();
			sc.statement.p4_to_Boogie();
			decIndent();
			Parser.getInstance().popBoogieBlock();
		}
		if(defaultCase!=null && cases.size()>1) {
			BoogieIfStatement ifBlock = new BoogieIfStatement(addIndent()+"else{\n", addIndent()+"}\n");
			Parser.getInstance().addBoogieBlock(ifBlock);
			incIndent();
			defaultCase.statement.p4_to_Boogie();
			decIndent();
			Parser.getInstance().popBoogieBlock();
		}
		//System.out.println(expr);
		return super.p4_to_Boogie();
	}
}

class SwitchCase extends Statement {
	Node label;
	Node statement;
	boolean isDefault;
	@Override
	void parse(ObjectNode object) {
		super.parse(object);
		label = Parser.getInstance().jsonParse(object.get(JsonKeyName.LABEL));
		statement = Parser.getInstance().jsonParse(object.get(JsonKeyName.STATEMENT));
		isDefault = false;
		if(label.Node_Type.equals("DefaultExpression"))
			isDefault = true;
	}
}