/**
 * Created by ivy on 1/30/15.
 */

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;

import javax.tools.*;
import com.sun.source.util.JavacTask;
public class JavaREPL {

    public static int i = 0; // count the number of complete declarations/statements
    public static final  String CLASSFILES = "InheritedClasses/"; // path of all generated classes

    public static final String ST = "ST";  //path of string templates
    public static final STListener stListener = new STListener();
    public static final NestedReader nr = new NestedReader(null);
    static STGroup templates = new STGroupDir(ST);
    static {
        templates.setListener(stListener);
        templates.delimiterStartChar = '$';
        templates.delimiterStopChar = '$';
    }

    public static void main(String[] args) throws IOException {
		//Read from stdIn
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		compile(CLASSFILES + "Interp_0.java");
		//Obtain the complete declarations or statements through NestedReader
		NestedReader nestedReader = new NestedReader(input);
		while(nestedReader.c != -1){
			//Obtain a complete declaration/statement as a "line"
			String line = nestedReader.getNestedString();

			String newClassName = "Interp_" + (i + 1);
			boolean isDeclaration = isDeclaration(line);
			writeToFile(i + 1, isDeclaration,line);
			boolean ok = compile(CLASSFILES + newClassName + ".java");
			if (ok){
				i++;
				if (!isDeclaration)
					execute(newClassName);
			}
		}
    }

    //parse the java file with the line as a declaration
	private static boolean isDeclaration(String line) throws IOException{
		//Create a temp.java file with the input line as a declaration
		String tempFileName = CLASSFILES+"temp.java";
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
		System.out.println(diagnostics.getDiagnostics().size() == 0);
		return diagnostics.getDiagnostics().size() == 0;
	}

	//write to a new java file inheriting the previous class.
	private static void writeToFile(int sub, boolean isDeclaration, String line) throws IOException {
		File newClassFile = new File(CLASSFILES+"Interp_"+(i+1)+".java");
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
		System.out.println(newClass);
		dos.writeBytes(newClass);
	}

	//compile the generated file, return true if .class file is generated
	private static boolean compile(String fileName) throws IOException{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		//fileManager.setLocation(StandardLocation.CLASS_PATH, Arrays.asList(new File(System.getProperty("./InheritedClasses"))));
		fileManager.setLocation(StandardLocation.CLASS_PATH, Arrays.asList(new File("InheritedClasses")));

		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromStrings(Arrays.asList(fileName));
		JavacTask task = (JavacTask)
				compiler.getTask(null, fileManager, diagnostics,
						null, null, compilationUnits);
		boolean ok = task.call();
		System.out.println(ok);
		for(Diagnostic diag: diagnostics.getDiagnostics()){
			System.err.println("line" + diag.getLineNumber()+ ": " +diag.getMessage(null));
		}
		return ok;
	}

	//use URLClassLoader to load the new class, execute it
	private static void execute(String className) throws MalformedURLException{
		//Specify the folder of files
		URL[] urls = new URL[] { new URL("file:" + System.getProperty("user.dir") + "/" + CLASSFILES)};
        //Load the classes and execute
		URLClassLoader ucl = new URLClassLoader(urls);
		try {
			ucl.loadClass(className).getDeclaredMethod("exec", new Class[]{})
					.invoke(null, new Object[]{});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
