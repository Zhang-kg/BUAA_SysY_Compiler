import com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class BasicScanner {
    private String ans = "";
    private Stack<Character> stack = new Stack<>();
    
    public BasicScanner() {
        File inFile = new File("./testfile.txt");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //Scanner in = new Scanner(System.in);
        boolean inLongNote = false;
        boolean inNormalState = true;
        boolean inString = false;
        //while (in.hasNextLine()) {
        //Byte st = in.nextByte();
        while (true) {
            String str = null;
            try {
                if (null == (str = in.readLine())) {
                    break;
                } else {
                    int i = 0;
                    while (i < str.length()) {
                        if (inLongNote) {
                            if (i + 1 < str.length() && str.charAt(i) == '*' && str.charAt(i + 1) == '/') {
                                inLongNote = false;
                                inNormalState = true;
                                inString = false;
                                i = i + 2;
                            } else {
                                i = i + 1;
                            }
                        } else if (inNormalState) {
                            if (str.charAt(i) == '\"') {
                                ans += str.charAt(i);
                                inNormalState = false;
                                inLongNote = false;
                                inString = true;
                                i = i + 1;
                            } else if (i + 1 < str.length() && str.charAt(i) == '/' && str.charAt(i + 1) == '/') {
                                inNormalState = true;
                                inLongNote = false;
                                inString = false;
                                i = str.length();
                            } else if (i + 1 < str.length() && str.charAt(i) == '/' && str.charAt(i + 1) == '*') {
                                inNormalState = false;
                                inLongNote = true;
                                inString = false;
                                i = i + 2;
                            } else {
                                ans += str.charAt(i);
                                i = i + 1;
                            }
                        } else if (inString) {
                            if (str.charAt(i) == '\"') {
                                ans += str.charAt(i);
                                inNormalState = true;
                                inString = false;
                                inLongNote = false;
                                i = i + 1;
                            } else {
                                ans += str.charAt(i);
                                i = i + 1;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        boolean isMatch = true;
        // TODO: 字符串内部的括号匹配和字符串匹配
        for (int i = 0; i < ans.length(); i++) {
            char c = ans.charAt(i);
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else if (c == ')') {
                if (stack.peek() != '(') isMatch = false;
                stack.pop();
            } else if (c == ']') {
                if (stack.peek() != '[') isMatch = false;
                stack.pop();
            } else if (c == '}') {
                if (stack.peek() != '{') isMatch = false;
                stack.pop();
            }
        }
        //System.out.println(isMatch);
    }
    
    public String getAns() {
        return ans;
    }
}
