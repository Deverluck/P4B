package verification.p4verifier;

import java.util.ArrayList;
import java.util.HashSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

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
		Parser.getInstance().addAssignmentStatement(this);
	}
	@Override
	String p4_to_C() {
		String code = left.p4_to_C()+"="+right.p4_to_C()+";\n";
		return code;
	}

//  Need SSA format
//	@Override
//	BoolExpr getCondition() {
//		Context ctx = Parser.getInstance().getContext();
//		return ctx.mkEq(left.getBitVecExpr(), right.getBitVecExpr());
////		return super.getCondition();
//	}
	
	void checkReadOnly() {
		String leftCode = left.p4_to_Boogie();
		if(leftCode.equals("standard_metadata.ingress_port")||
				leftCode.equals("standard_metadata.packet_length")||
				leftCode.equals("standard_metadata.egress_port")||
				leftCode.equals("standard_metadata.egress_instance")) {
			Parser.getInstance().addBoogieStatement("\n	// Modify ReadOnly Metadata\n	assert(false);\n");
		}
	}

	@Override
	String p4_to_Boogie() {
//		if(left.getBitVecExpr()!=null&&right.getBitVecExpr()!=null) {
//			Context ctx = Parser.getInstance().getContext();
//			BoolExpr expr = getCondition();
//			if(Parser.getInstance().getCurrentProcedure().getConditions().isEmpty()) {
//				Parser.getInstance().updateCondition(expr);
//			}
//			else {
//				BoolExpr oldExpr = Parser.getInstance().getCurrentProcedure().getConditions().peek();
//				Parser.getInstance().popCondition();
//				Parser.getInstance().updateCondition(ctx.mkAnd(expr, oldExpr));
//			}
//		}
		if(Parser.getInstance().getCommands().ifCheckReadOnlyMetadata())
			checkReadOnly();
		
		String leftCode = left.p4_to_Boogie();
		if(leftCode.startsWith("standard_metadata.egress_spec") || 
				leftCode.startsWith("Heap[standard_metadata, standard_metadata_t.egress_spec]")) {
			Parser.getInstance().addBoogieStatement(addIndent()+"forward := true;\n");
			Parser.getInstance().addModifiedGlobalVariable("forward");
		}
		
		String modifiedVariable = leftCode;
		if(leftCode.contains("[")) {
			int idx = leftCode.indexOf("[");
			modifiedVariable = leftCode.substring(0, idx);
		}
		Parser.getInstance().addModifiedGlobalVariable(modifiedVariable);
		
		
		String code = addIndent()+leftCode+" := "+right.p4_to_Boogie()+";\n";
		if(right.p4_to_Boogie().equals("havoc")) {
			code = addIndent()+"havoc "+leftCode+";\n";
		}
		
		/* Add assert statement
		 * Writing invalid fields is allowed
		 */
		left.addAssertStatement();
		right.addAssertStatement();
		
		if(Parser.getInstance().getCommands().ifCheckHeaderValidity()) {
			if(Parser.getInstance().isParserState()||
					(!Parser.getInstance().getCommands().ifRemoveRedundantAssignment()||Parser.getInstance().isUsefulAssignmentStatement(Node_ID))) {
//				if(code.contains(":= true") || code.contains(":= false"))
					Parser.getInstance().addBoogieStatement(code);
			}
		}
		else {
			Parser.getInstance().addBoogieStatement(code);
		}
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
		Parser.getInstance().addIfStatement(this);
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
	BoolExpr getCondition() {
//		System.out.println(condition.p4_to_Boogie());
//		System.out.println(condition.getCondition());
		return condition.getCondition();
//		BoolExpr expr = Parser.getInstance().getContext().mk
	}
	
	BoolExpr getNegCondition() {
		return Parser.getInstance().getContext().mkNot(condition.getCondition());
	}
	
	@Override
	String p4_to_Boogie() {
		getCondition();
		if(Parser.getInstance().getCommands().ifCheckHeaderValidity()) {
			condition.addAssertStatement();
		}
		String code = addIndent()+"if(";
		code += condition.p4_to_Boogie();
		code += "){\n";
		
		String start = code;
		String end = addIndent()+"}\n";
		BoogieIfStatement blockIfTrue = new BoogieIfStatement(start, end);
		Parser.getInstance().updateCondition(getCondition());
		Parser.getInstance().addBoogieBlock(blockIfTrue);
		
		incIndent();
		code += ifTrue.p4_to_Boogie();
		decIndent();
		code += addIndent()+"}\n";
		
		Parser.getInstance().popBoogieBlock();
		Parser.getInstance().popCondition();
		
		if(ifFalse != null) {
			BoogieIfStatement blockIfFalse = new BoogieIfStatement(addIndent()+"else {\n",
					addIndent()+"}\n");
			Parser.getInstance().updateCondition(getNegCondition());
			Parser.getInstance().addBoogieBlock(blockIfFalse);
			
			code += addIndent()+"else {\n";
			incIndent();
			code += ifFalse.p4_to_Boogie();
			decIndent();
			code += addIndent()+"}\n";
			
			Parser.getInstance().popBoogieBlock();
			Parser.getInstance().popCondition();
		}
//		return super.p4_to_Boogie();
		return code;
	}
	@Override
	HashSet<String> getBranchVariables() {
		return condition.getBranchVariables();
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
		if(code.contains(".write(")||code.contains(".read(")||code.contains(".count(")||
				code.contains(".execute_meter(")||code.contains("resubmit(")||code.contains("recirculate(")||
				code.contains("clone(")||code.contains("truncate("))
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
	
	HashSet<String> getCaseNames(){
		HashSet<String> res = new HashSet<>();
		for(Node casenode:cases) {
			res.add(casenode.getName());
		}
		if(default_case!=null) {
			res.add(default_case.getName());
		}
		return res;
	}

	@Override
	String p4_to_Boogie() {
		Context ctx = Parser.getInstance().getContext();
		String code = "";
		int cnt1 = 0; // counter for keys (&&)
		int cnt2 = 0; // counter for cases (else if{})
		for(Node case_node : cases) {
			String condition = "	if(";
			if(cnt2 != 0)
				condition = "	else if(";
			
			
			BoolExpr expr = Parser.getInstance().getContext().mkBool(true);
			cnt1 = 0;
			
			for(Node select_node : select) {
				// TODO deal with argument types (equal width)
				//condition += select_node.p4_to_Boogie()+" == ";
				Node caseValue = cases_value.get(cnt2).get(cnt1);
				if(caseValue instanceof Constant) {
					condition += select_node.p4_to_Boogie()+" == ";
					condition += caseValue.p4_to_Boogie();
					
					BoolExpr e = ctx.mkEq(select_node.getBitVecExpr(), caseValue.getBitVecExpr());
					expr = ctx.mkAnd(expr, e);
				}
				else if(caseValue instanceof Mask) {
					String function = caseValue.p4_to_Boogie();
					Mask mask = (Mask)caseValue;
					condition += function+"("+select_node.p4_to_Boogie()+", "+mask.right.p4_to_Boogie()+") == ";
					condition += function+"("+mask.left.p4_to_Boogie()+", "+mask.right.p4_to_Boogie()+")";
					
					BoolExpr e = ctx.mkAnd(ctx.mkEq(ctx.mkBVAND(select_node.getBitVecExpr(), mask.right.getBitVecExpr()),
							ctx.mkBVAND(mask.left.getBitVecExpr(), mask.right.getBitVecExpr())));
					expr = ctx.mkAnd(expr, e);
				}
				cnt1++;
				if(cnt1 < select.size())
					condition += " && ";
			}
			condition += "){\n";
			
			BoogieIfStatement ifStatement = new BoogieIfStatement(condition, "	}\n");
			Parser.getInstance().updateCondition(expr);
			Parser.getInstance().addBoogieBlock(ifStatement);
			
			code += condition;
			incIndent();
			Parser.getInstance().addProcedurePrecondition(case_node.p4_to_Boogie());
			code += case_node.p4_to_Boogie(JsonKeyName.PARSERSTATE)+"	}\n";
			decIndent();

			Parser.getInstance().popBoogieBlock();
			Parser.getInstance().popCondition();
//			System.out.println(ifStatement.toBoogie());
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
		Parser.getInstance().addSwitchStatement(this);
	}
	
	// may be "action_run", so it's a little hard to deal with
	@Override
	BitVecExpr getBitVecExpr() {
		return expression.getBitVecExpr();
	}
	@Override
	BoolExpr getCondition() {
//		System.out.println(expression.p4_to_Boogie());
//		System.out.println(expression.getCondition());
		return expression.getCondition();
	}
	BoolExpr getNegCondition() {
		return Parser.getInstance().getContext().mkNot(getCondition());
	}
	@Override
	String p4_to_Boogie() {
		String expr = expression.p4_to_Boogie();
		int flag = 0;
		SwitchCase defaultCase = null;
		
		BoolExpr defaultExpr = Parser.getInstance().getContext().mkBool(true);
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
//			Parser.getInstance().updateCondition(Parser.getInstance().getContext().mkEq(, arg1));
			incIndent();
			sc.statement.p4_to_Boogie();
			decIndent();
			Parser.getInstance().popBoogieBlock();
//			Parser.getInstance().popCondition();
//			defaultExpr = Parser.getInstance().getContext().mkAnd(defaultExpr, getNegCondition());
		}
		if(defaultCase!=null && cases.size()>1) {
			BoogieIfStatement ifBlock = new BoogieIfStatement(addIndent()+"else{\n", addIndent()+"}\n");
			Parser.getInstance().addBoogieBlock(ifBlock);
//			Parser.getInstance().updateCondition(defaultExpr);
			incIndent();
			defaultCase.statement.p4_to_Boogie();
			decIndent();
			Parser.getInstance().popBoogieBlock();
//			Parser.getInstance().popCondition();
		}
		//System.out.println(expr);
		return super.p4_to_Boogie();
	}
	@Override
	HashSet<String> getBranchVariables() {
		HashSet<String> variables = new HashSet<>();
		variables.add(expression.p4_to_Boogie());
		return variables;
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