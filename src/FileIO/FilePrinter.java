package FileIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class FilePrinter {
    private static PrintWriter out = null;
    private static final FilePrinter FILE_PRINTER = new FilePrinter();
    
    private FilePrinter(){
        File outFile = new File("./output.txt");
        try {
            out = new PrintWriter(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static FilePrinter getFilePrinter() {
        return FILE_PRINTER;
    }
    
    public void outPrintln(String line) {
        //out.println(line);

    }
    
    public void outPrintlnSyntax(String line) {
        out.println(line);
    }

    public void outPrintlnError(String line) {
//        out.println(line);
    }
    
    public void closeOut() {
        out.close();
    }
}
