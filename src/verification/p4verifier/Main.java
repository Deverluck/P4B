package verification.p4verifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;

public class Main {
	public File create_file(String path) throws IOException {
		File file = new File(path);
		// File dir = new File("./");
		// listAll(dir);
		if (!file.exists()) {
			if (file.getParentFile()!=null&&!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			file.createNewFile();
		}
		return file;
	}
	public void create_file(String path, String text) throws IOException {
		File file = create_file(path);
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		BufferedWriter bufferedWriter = new BufferedWriter(
				new OutputStreamWriter(fileOutputStream));
		bufferedWriter.write(text);
		bufferedWriter.close();
	}
	
	public void p4_to_Boogie(String input, String output) {
		Parser myParser = Parser.getInstance();
		String code = myParser.parse(input);
		try {
			create_file(output, code);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setCommands() {
		Parser myParser = Parser.getInstance();
		Commands commands = myParser.getCommands();
//		commands.setControlPlaneConstrain();
		commands.setCheckHeaderValidity();
		commands.setCheckHeaderStackBound();
		commands.setCheckForwardOrDrop();
	}
	
	public void printOptions() {
		System.out.println("Usage: java -jar p4b.jar [options]* <inputFile> <outputFile>");
		System.out.println();
		System.out.println("P4B  options:");
		System.out.println("  -h                     show usage");
		System.out.println("  -headerValidity        check header validity");
		System.out.println("  -headerStackBound      check header stack out-of-bounds error");
		System.out.println("  -implicitDrop          check implicit drops, which occur when egress_spec"
				+ " is not assigned");
		System.out.println("  -readOnly              check modification of read-only fields");
		System.out.println("  -all                   verify all the properties above");
		System.out.println("  -control               add control plane constraints (developing)");
		System.out.println();
		System.out.println("If no options are chosen, P4B by default generate Boogie programs without assertions.");
	}
	
	public static void main(String args[]) {
		Main m = new Main();
//		m.setCommands();
		if(args.length<2) {
			HashSet<String> cmd = new HashSet<>();
			for(int i = 0; i < args.length; i++) {
				cmd.add(args[i]);
			}
			if(cmd.contains("-h")) {
				m.printOptions();
				System.exit(0);
			}
			System.out.println("Usage: java -jar p4b.jar [options] <inputFile> <outputFile>");
			System.out.println("use -h option for more information");
			return;
		}
		
		if(args.length>2) {
			HashSet<String> cmd = new HashSet<>();
			for(int i = 0; i < args.length-2; i++) {
				cmd.add(args[i]);
			}
			Parser myParser = Parser.getInstance();
			Commands commands = myParser.getCommands();
			if(cmd.contains("-h")) {
				m.printOptions();
				System.exit(0);
			}
			if(cmd.contains("-headerValidity"))
				commands.setCheckHeaderValidity();
			if(cmd.contains("-headerStackBound"))
				commands.setCheckHeaderStackBound();
			if(cmd.contains("-implicitDrop"))
				commands.setCheckForwardOrDrop();
			if(cmd.contains("-readOnly"))
				commands.setCheckReadOnlyMetadata();
			if(cmd.contains("-control"))
				commands.setControlPlaneConstrain();
			if(cmd.contains("-all")) {
				commands.setCheckHeaderValidity();
				commands.setCheckHeaderStackBound();
				commands.setCheckForwardOrDrop();
				commands.setCheckReadOnlyMetadata();
			}
		}
		
		String input = args[args.length-2];
		File file = new File(input);
		if(!file.exists()) {
			System.out.println("File \""+input+"\" doesn't exists.");
			return;
		}
		String output = args[args.length-1];
		m.p4_to_Boogie(input, output);
		
		
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
		
//		m.p4_to_Boogie("/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/axon/p416-axon-ppc.json", 
//				"/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/axon/p416-axon-ppc-without-map.bpl");
		
//		m.p4_to_Boogie("/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/dapper/p416-rinc-ppc.json", 
//				"/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/dapper/p416-rinc-ppc.bpl");
		
//		m.p4_to_Boogie("/media/invincible/WORK/Programs/P4-verification/sharedir/test/header_stack_test.json", 
//				"/media/invincible/WORK/Programs/P4-verification/sharedir/test/header_stack_test.bpl");
		
//		m.p4_to_Boogie("/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/big-switch/p416-switch.json", 
//		"/media/invincible/WORK/Programs/P4-verification/p4toBoogie/benchmark/vera-testcases/big-switch/p416-switch-without-map.bpl");
		
//		m.p4_to_Boogie("E:\\Programs\\P4-verification\\p4toBoogie\\benchmark\\vera-testcases\\big-switch\\p416-switch.json", 
//				"E:\\Programs\\P4-verification\\p4toBoogie\\benchmark\\vera-testcases\\big-switch\\p416-switch-without-map.bpl");
		
//		p4_to_Boogie("E:\\Programs\\P4-verification\\p4toBoogie\\benchmark\\vera-testcases\\big-switch\\p416-switch.json", 
//				"E:\\Programs\\P4-verification\\p4toBoogie\\benchmark\\vera-testcases\\big-switch\\p416-switch-full-assert.bpl");
		
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
