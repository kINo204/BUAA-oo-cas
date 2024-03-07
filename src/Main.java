import expressions.Expr;

import java.util.Scanner;

public class Main {
    public static void main(String[] s) {

        Scanner scanner = new Scanner(System.in);

        // Preprocessing
        int funcNum = Integer.parseInt(scanner.nextLine());
        PreProcessor preProcessor = new PreProcessor();
        for (int i = 0; i < funcNum; i++) {
            preProcessor.readFunc(new Lexer(scanner.nextLine()));
        }

        String last = scanner.nextLine();
        String recent = preProcessor.subsFunc(last);
        while (!last.equals(recent)) {
            last = recent;
            recent = preProcessor.subsFunc(last);
        }
        String input = recent;

        // Parsing and Simplification
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        expr.simplify();

        System.out.println(expr);
    }
}