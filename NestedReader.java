import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by ivy on 1/27/15.
 */
public class NestedReader {
    StringBuilder buf;    // fill this as you process, character by character
    BufferedReader input; // where are we reading from?
    int c; // current character of lookahead; reset upon each getNestedString() call

    public NestedReader(BufferedReader input) {
        this.input = input;
    }
    public String getNestedString() throws IOException {
        String line = input.readLine();
        //System.out.println(line);
        return line;
    }
    public void consume() throws IOException {
        buf.append((char)c);
        c = input.read();
    }
}