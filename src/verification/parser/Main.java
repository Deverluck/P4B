package verification.parser;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {
	public static void main(String args[]) {
		Parser myParser = Parser.getInstance();
//		Parser myParser = new Parser("E:\\Programs\\P4-verification\\sharedir\\test\\basic--toJSON-json.json");
//		Parser myParser = new Parser("E:\\Programs\\P4-verification\\sharedir\\test\\switch-16-toJSON.json");
		myParser.parse("E:\\Programs\\P4-verification\\sharedir\\test\\basic_tunnel.json");
//		myParser.parse("/media/invincible/WORK/Programs/P4-verification/sharedir/test/basic_tunnel.json");
//		myParser.parse("E:\\Programs\\P4-verification\\sharedir\\test\\switch-16-toJSON.json");

//		HashMap<String, BoogieProcedure> procedures;
//		ArrayList<BoogieProcedure> list;
//		list = new ArrayList<>();
//		procedures = new HashMap<>();
//		BoogieProcedure p1 = new BoogieProcedure("p1");
//		procedures.put(p1.name, p1);
//		list.add(p1);
//		p1.body += "body";
//		System.out.println(procedures.get("p1").body);
//		System.out.println(list.get(0).body);
	}
}
