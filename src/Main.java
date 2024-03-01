import expressions.Expr;

import java.util.Scanner;

public class Main {
    public static void main(String[] s) {

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();

        expr.simplify();

        System.out.println(expr);

        return;
    }
}