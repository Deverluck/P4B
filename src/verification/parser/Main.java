package verification.parser;

public class Main {
	public static void main(String args[]) {
//		Parser myParser = new Parser("E:\\Programs\\P4-verification\\sharedir\\test\\basic--toJSON-json.json");
		Parser myParser = new Parser("E:\\Programs\\P4-verification\\sharedir\\test\\switch-16-toJSON.json");
		myParser.parse();
	}
}
