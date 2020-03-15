package verification.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Expression extends Node {

}

class TypeNameExpression extends Expression {
	
}

class ConstructorCallExpression extends Expression {
	
}

class MethodCallExpression extends Expression {
	
}

class PathExpression extends Expression {
	Node path;
	@Override
	boolean parse(ObjectNode object) {
		super.parse(object);
		path = Parser.jsonParse(object.get(JsonKeyName.PATH));
		addChild(path);
		return true;
	}
	@Override
	String p4_to_C() {
		return path.p4_to_C()+"();\n";
	}
}

class SelectExpression extends Expression {
//	@Override
//	boolean parse(JSONObject object) {
//		super.parse(object);
//		
//		return true;
//	}
}

class ExpressionValue extends Expression {
	
}

class Slice extends Expression {
	
}

class Cast extends Expression {
	
}

class Member extends Expression {
	
}