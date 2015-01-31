/**
 * Created by ivy on 1/30/15.
 */

import java.io.*;
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
		//Obtain the complete declarations or statements through NestedReader
		NestedReader nestedReader = new NestedReader(input);
		//while(true){
			//Obtain a complete declaration/statement as a "line"
			String line = nestedReader.getNestedString();
			i++;
			String newClassName = "Interp_"+i;
			if(isDeclaration(line))
				writeToFile(i, line, "");
			else
				writeToFile(i, "", line);
			//TODO: Compile and Execute
		//}
    }

    //parse the java file with the line as a declaration
	private static boolean isDeclaration(String line) throws IOException{
		//Create a temp.java file to see if it can parse properly
		String tempFileName = CLASSFILES+"temp.java";
		File temp = new File(tempFileName);
		FileOutputStream fos = new FileOutputStream(temp);
		DataOutputStream dos = new DataOutputStream(fos);
		ST st = templates.getInstanceOf("Interp_i");
		st.add("i", 0);
		st.add("declaration", line);
		st.add("statement", "");
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
	private static void writeToFile(int i, String declaration, String statement) throws IOException {
		File newClassFile = new File(CLASSFILES+"Interp_"+i+".java");
		FileOutputStream fos = new FileOutputStream(newClassFile);
		DataOutputStream dos = new DataOutputStream(fos);
		ST st = templates.getInstanceOf("Interp_i");
		st.add("i", i);
		st.add("declaration", declaration);
		st.add("statement", statement);
		String newClass = st.render();
		//System.out.println(newClass);
		dos.writeBytes(newClass);
	}

	private static void compile(){


	}


}
