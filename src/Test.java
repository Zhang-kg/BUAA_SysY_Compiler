import java.util.Arrays;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String a = in.nextLine();
//        System.out.println(Arrays.toString(a.split("%d")).length());
        String s[] = a.split("%d");
        System.out.println(s.length);
//        switch (a.length()) {
//            case 1 -> {
//                System.out.println("1");
//                break;
//            }
//            case 2 -> {
//                System.out.println("2");
//                break;
//            }
//        }
    }
}
