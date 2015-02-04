/**
 * Created by ivy on 1/30/15.
 */

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;

import javax.tools.*;
import com.sun.source.util.JavacTask;

public class JavaREPL {

    public static int i = 0; // count the number of complete declarations/statements
	public static final String tempDir = System.getProperty("java.io.tmpdir")+"/";  //system temp directory to saved the generated files

    public static final String ST = "ST";  //path of string templates
    static STGroup templates = new STGroupDir(ST);
    static {
        templates.delimiterStartChar = '$';
        templates.delimiterStopChar = '$';
    }

    public static void main(String[] args) throws IOException {
		deleteFiles(); //make sure the folder is empty
        createInterp0();//create and compile Interp_0 for subsequent inheritance

		//Specify the folder of files and create a ClassLoader
		URL[] urls = new URL[] { new URL("file:" + tempDir)};
		URLClassLoader ucl = new URLClassLoader(urls);

		//Read from stdIn
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		//Obtain the complete declarations or statements through NestedReader
		NestedReader nestedReader = new NestedReader(input);
		while(nestedReader.c != -1){
			System.out.print(">");
			//Obtain a complete declaration/statement as a "line"
			String line = nestedReader.getNestedString();
            if(nestedReader.c == -1) return;
			if(line.length() == 0) continue;

			String newClassName = "Interp_" + (i + 1);//name of new class
			boolean isDeclaration = isDeclaration(line);//if it is declaration
			writeToFile(i + 1, isDeclaration,line); //write to .java file
			boolean ok = compile(tempDir + newClassName + ".java"); //compile
			if (ok){
				i++; //compiled successfully
				if (!isDeclaration)
					execute(newClassName, ucl); //execute statement
			}
		}
		deleteFiles(); //clear generated files
	}

    //parse the java file with the line as a declaration
	private static boolean isDeclaration(String line) throws IOException{
		//Create a temp.java file with the input line as a declaration
		String tempFileName = tempDir +"temp.java";
		File temp = new File(tempFileName);
		FileOutputStream fos = new FileOutputStream(temp);
		DataOutputStream dos = new DataOutputStream(fos);
		ST st = templates.getInstanceOf("temp");
		st.add("declaration", line);
		String tempContent = st.render();
		dos.writeBytes(tempContent);

		//parse temp.java
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromStrings(Arrays.asList(tempFileName));
		JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics, null,
				null, compilationUnits);
		task.parse();
		temp.delete();
		return diagnostics.getDiagnostics().size() == 0;
	}

	//write to a new java file inheriting the previous class.
	private static void writeToFile(int sub, boolean isDeclaration, String line) throws IOException {
		File newClassFile = new File(tempDir +"Interp_"+(i+1)+".java");
		FileOutputStream fos = new FileOutputStream(newClassFile);
		DataOutputStream dos = new DataOutputStream(fos);
		ST st = templates.getInstanceOf("Interp_i");
		st.add("i", sub);
		st.add("j", i);
		if(isDeclaration){
			st.add("declaration", "public static "+ line);
			st.add("statement", "");
		}else{
			st.add("declaration", "");
			st.add("statement", line);
		}
		String newClass = st.render();
		dos.writeBytes(newClass);
	}

	//compile the generated file, return true if .class file is generated
	private static boolean compile(String fileName) throws IOException{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		fileManager.setLocation(StandardLocation.CLASS_PATH, Arrays.asList(new File(tempDir)));

		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromStrings(Arrays.asList(fileName));
		JavacTask task = (JavacTask)
				compiler.getTask(null, fileManager, diagnostics,
						null, null, compilationUnits);
		boolean ok = task.call();
		if(!ok){
			for(Diagnostic diag: diagnostics.getDiagnostics()) {
				System.err.println("line" + diag.getLineNumber() + ": " +diag.getMessage(null));
			}
			Files.deleteIfExists(Paths.get(fileName));
		}
		return ok;
	}

	//use URLClassLoader to load the new class, execute it
	private static void execute(String className, URLClassLoader ucl) throws MalformedURLException{
		try {
			ucl.loadClass(className).getDeclaredMethod("exec", new Class[]{})
					.invoke(null, new Object[]{});
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			return;
		}
	}

	//after existing the while loop, delete all generated .java and .class files
	private static void deleteFiles() throws IOException{
		int numOfFiles = new File(tempDir).listFiles().length;
		for(int j = 0; j<=numOfFiles/2; j++){
			Files.deleteIfExists(Paths.get(tempDir +"Interp_"+j+".java"));
			Files.deleteIfExists(Paths.get(tempDir +"Interp_"+j+".class"));
		}
	}

	//create the first file for subsequent inheritance
	private static void createInterp0() throws IOException{
		//Create a temp.java file with the input line as a declaration
		String firstFileName = tempDir +"Interp_0.java";
		File first = new File(firstFileName);
		FileOutputStream fos = new FileOutputStream(first);
		DataOutputStream dos = new DataOutputStream(fos);
		ST Interp0 = templates.getInstanceOf("Interp_0");
		dos.writeBytes(Interp0.render());
		compile(firstFileName);
	}
}
