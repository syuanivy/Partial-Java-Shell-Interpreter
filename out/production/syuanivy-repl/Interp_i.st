Interp_i(i, j, declaration, statement) ::= <<

import java.io.*;
import java.util.*;
public class Interp_$i$ extends Interp_$j$ {
    $declaration$
    public static void exec() {
    $statement$
    }
}


>>