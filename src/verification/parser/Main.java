package verification.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Main {
	public static File create_file(String path) throws IOException {
		File file = new File(path);
		// File dir = new File("./");
		// listAll(dir);
		if (!file.exists()) {
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			file.createNewFile();
		}
		return file;
	}
	public static void create_file(String path, String text) throws IOException {
		File file = create_file(path);
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		BufferedWriter bufferedWriter = new BufferedWriter(
				new OutputStreamWriter(fileOutputStream));
		bufferedWriter.write(text);
		bufferedWriter.close();
	}
	public static void p4_to_Boogie(String input, String output) {
		Parser myParser = Parser.getInstance();
		String code = myParser.parse(input);
		try {
			create_file(output, code);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String args[]) {
//		if(args.length<2) {
//			System.out.println("Usage: java -jar p2b.jar <inputFile> <outputFile>");
//			return;
//		}
//		String input = args[0];
//		File file = new File(input);
//		if(!file.exists()) {
//			System.out.println("File \""+input+"\" doesn't exists.");
//			return;
//		}
		
		
//		Parser myParser = Parser.getInstance();
//		String input = "/media/invincible/WORK/Programs/P4-verification/sharedir/test/header_stack_test.json";
//		input = "/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/big-switch/p416-switch.json";
////		input = "/media/invincible/WORK/Programs/P4-verification/sharedir/test/basic--toJSON-json.json";
////		input = "/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/axon/p416-axon-ppc.json";
//		String code = myParser.parse(input);
////		System.out.println(code);
		
//		String res = myParser.parse(input);
//		File output = new File(args[1]);
		
//		p4_to_Boogie("/media/invincible/WORK/Programs/P4-verification/sharedir/test/switch-16-toJSON.json", 
//				"/media/invincible/WORK/Programs/P4-verification/sharedir/test/switch-16-toJSON.bpl");
		
//		p4_to_Boogie("/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/axon/p416-axon-ppc.json", 
//				"/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/axon/p416-axon-ppc-optimized2.bpl");
		
		p4_to_Boogie("/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/axon/p416-axon-ppc.json", 
		"/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/axon/p416-axon-ppc-corral.bpl");
		
//		p4_to_Boogie("/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/cases/cases/vpc/p416-vpc.json", 
//				"/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/cases/cases/vpc/p416-vpc.bpl");
		
//		p4_to_Boogie("/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/copy-to-cpu/p416-copy_to_cpu-ppc.json",
//				"/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/copy-to-cpu/p416-copy_to_cpu-ppc.bpl");

//		p4_to_Boogie("/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/ndp-router/p416-ndp_router-ppc.json",
//				"/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/ndp-router/p416-ndp_router-ppc.bpl");
	
//		p4_to_Boogie("/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/simple-router/p416-simple_router-ppc.json",
//				"/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/simple-router/p416-simple_router-ppc.bpl");
		
//		Parser myParser = new Parser("E:\\Programs\\P4-verification\\sharedir\\test\\basic--toJSON-json.json");
//		Parser myParser = new Parser("E:\\Programs\\P4-verification\\sharedir\\test\\switch-16-toJSON.json");
//		myParser.parse("E:\\Programs\\P4-verification\\sharedir\\test\\basic_tunnel.json");
//		myParser.parse("/media/invincible/WORK/Programs/P4-verification/sharedir/test/basic_tunnel.json");
//		myParser.parse("E:\\Programs\\P4-verification\\sharedir\\test\\switch-16-toJSON.json");
		
//		myParser.parse("E:\\Programs\\P4-verification\\sharedir\\test\\header_stack_test.json");
		
//		myParser.parse("/media/invincible/WORK/Programs/P4-verification/sharedir/test/header_stack_test.json");
	}
}
