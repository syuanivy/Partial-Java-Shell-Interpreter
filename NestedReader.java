import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by ivy on 1/27/15.
 */
public class NestedReader {
    StringBuilder buf; //building a complete declaration/statement
    BufferedReader input;
    int c;//look ahead character

    public NestedReader(BufferedReader input) {
        this.input = input;
        buf = new StringBuilder();
    }

    public String getNestedString() throws IOException {
        Stack<Character> stack = new Stack<Character>();
        c = input.read();
        while(c != -1 && !(c == (int)'\n' && stack.isEmpty() )){
            if(isBracket(c)){
                processBracket(c, stack);
                continue;
            }
            if(isQuotation(c)){
                processQuotation(c, stack);
                continue;
            }
            if(isSlash(c)) {
                processSlash(c);
                continue;
            }
            else
                consume();
        }
        String line = buf.toString();
        buf.delete(0,buf.length());
        if(line.startsWith("print "))
            return replacePrint(line);
        return line ;
    }

    private boolean isBracket(int c){
        ArrayList<Character> brackets = new ArrayList<Character>();
        brackets.add('{');brackets.add('(');brackets.add(')');brackets.add('}'); brackets.add('[');brackets.add(']');
        if(brackets.contains((char)c))
            return true;
        else
            return false;
    }

    private void processBracket(int c, Stack<Character> stack) throws IOException{
        switch ((char)c){
            case '{' : stack.push('}'); consume();return;
            case '(' : stack.push(')'); consume();return;
            case '[' : stack.push(']'); consume();return;
        }

        if(!stack.isEmpty() && stack.peek() == (char)c){
            stack.pop();
            consume();
        }else{               // invalid parentheses detected
            stack.clear();   // clear the stack
            while(this.c != -1 && this.c != (int) '\n')   // read to the end
                consume();
        }
    }

    private boolean isQuotation(int c){
        if(c == 34 || c == 39) // 34 is ", 39 is '
            return true;
        else
            return false;
    }

    private void processQuotation(int c, Stack<Character> stack) throws IOException{
        int start = c;
        consume();
        while(this.c != -1 && this.c != start && this.c != (int) '\n'){ // exits on closing quotation mark on the same line
            if(this.c == 92) //back slash
                processSlash(this.c);
            else
                consume();
        }
        if(this.c == (int)'\n'){
            stack.clear();
            return;
        }
        consume();//consume the closing quotation mark
    }

    private boolean isSlash(int c){
        if(c == 47 || c == 92) // c is /, 92 is \
            return true;
        else
            return false;
    }

    private void processSlash(int c) throws IOException{
        if(c == 92){  // back slash
            consume();
            if(this.c != -1 && this.c!= (int)'\n' )// consume the following character
                consume();
        }else{        //forward slash
            consume();
            if(this.c == 47)  {// second forward slash, this is a comment
                buf.deleteCharAt(buf.length()-1);
                while(this.c != -1 && this.c!= (int)'\n')   // read to the end
                    this.c=input.read();
            }
        }
    }

    //allow statement print ***; or print ***
    private String replacePrint(String line){
        String[] splits = line.split("print ",2);
        String after = splits[splits.length-1];
        String replaced;
        if(after.charAt(after.length()-1) == ';'){
            int lastNonSemiColon = after.length()-2;
            //suports multiple semi colons after the print statement
            while(lastNonSemiColon >= 0 && after.charAt(lastNonSemiColon) == ';'){
                lastNonSemiColon--;
            }
            replaced = after.substring(0,lastNonSemiColon+1);
        }else{
            replaced = after;
        }
        return "System.out.println("+replaced+");";
    }

    private void consume() throws IOException {
        buf.append((char)c);
        c = input.read();
    }
}