/**
 * Created by ivy on 1/30/15.
 */

import java.io.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;
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
		while(true){
			//Obtain a complete declaration/statement as a "line"
			String line = nestedReader.getNestedString();
			i++;
			String newClassName = "Interp_"+i;
			if(isDeclaration(line))
				writeToFile(i, line, "");
			else
				writeToFile(i, "", line);

			//TODO: Compile and Execute
		}
    }

    //if the line is a declaration
	private static boolean isDeclaration(String line) throws IOException{
		File temp = new File(CLASSFILES+"temp.java");
		FileOutputStream fos = new FileOutputStream(temp);
		DataOutputStream dos = new DataOutputStream(fos);
		ST st = templates.getInstanceOf("Interp_i");
		st.add("i", 0);
		st.add("declaration", line);
		st.add("statement", "");
		String tempContent = st.render();
		dos.writeBytes(tempContent);
		//TODO: compile a java file
		boolean isDeclaration = false;
		temp.delete();
		return isDeclaration;
	}

	//return the name of the new class
	private static void writeToFile(int i, String declaration, String statement) throws IOException {
		File newClassFile = new File(CLASSFILES+"Interp_"+i+".java");
		FileOutputStream fos = new FileOutputStream(newClassFile);
		DataOutputStream dos = new DataOutputStream(fos);
		ST st = templates.getInstanceOf("Interp_i");
		st.add("i", i);
		st.add("declaration", declaration);
		st.add("statement", statement);
		String newClass = st.render();
		System.out.println(newClass);
		dos.writeBytes(newClass);
	}


}
