package FileIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class BasicScanner {
    private String ans = "";
    private int lineNumber = 1;
    private HashMap<Integer, Integer> lineStartNumbers = new HashMap<>();
    private HashMap<Integer, Integer> lineEndNumbers = new HashMap<>();

    public BasicScanner() {
        File inFile = new File("./testfile.txt");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        boolean inLongNote = false;
        boolean inNormalState = true;
        boolean inString = false;

        while (true) {
            String str = null;
            try {
                if (null == (str = in.readLine())) {
                    break;
                } else {
                    lineStartNumbers.put(lineNumber, ans.length());
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
                    lineEndNumbers.put(lineNumber, ans.length());
                    lineNumber++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public String getAns() {
        return ans;
    }

    public HashMap<Integer, Integer> getLineStartNumbers() {
        return lineStartNumbers;
    }

    public HashMap<Integer, Integer> getLineEndNumbers() {
        return lineEndNumbers;
    }
}
