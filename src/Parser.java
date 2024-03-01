import expressions.Expr;
import expressions.Factor;
import expressions.Num;
import expressions.OptPosNeg;
import expressions.Term;
import expressions.Var;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();

        expr.addTerm(parseTerm());
        while (lexer.type() == Lexer.TokenType.ADD
                || lexer.type() == Lexer.TokenType.SUB) {
            expr.addTerm(parseTerm());
        }
        return expr;
    }

    private Term parseTerm() {
        Term term = new Term();

        // Read the +/- before Term and store
        // The first +/- sig when starting parse WILL BECOME TERM'S SIG. TODO
        if (lexer.type() == Lexer.TokenType.ADD) {
            term.setOptTerm(OptPosNeg.POS);
            lexer.next();
        } else if (lexer.type() == Lexer.TokenType.SUB) {
            term.setOptTerm(OptPosNeg.NEG);
            lexer.next();
        } else {
            // No operator -> positive
            term.setOptTerm(OptPosNeg.POS);
        }

        // Read the +/- of ALL FACTORS OF A TERM and store
        //
        // May cause inconsistency with requirement here:
        // the program accepts "+/-" before a var/(expr), while
        // this isn't required.
        if (lexer.type() == Lexer.TokenType.ADD) {
            term.setOptFact(OptPosNeg.POS);
            lexer.next();
        } else if (lexer.type() == Lexer.TokenType.SUB) {
            term.setOptFact(OptPosNeg.NEG);
            lexer.next();
        } else {
            term.setOptFact(OptPosNeg.POS);
        }

        term.addFactor(parseFactor());
        while (lexer.type() == Lexer.TokenType.MUL) {
            lexer.next(); // jump "*" token
            term.addFactor(parseFactor());
        }

        return term;
    }

    private Factor parseFactor() {
        Factor factor = new Factor();

        // Parse factor's base
        if (lexer.peek().equals("(")) {
            lexer.next();
            factor.setBase(parseExpr());
            lexer.next(); // TODO: check next() behavior here
        } else if (lexer.type() == Lexer.TokenType.NUM) {
            factor.setBase(new Num(lexer.peek()));
            lexer.next();
        } else if (lexer.type() == Lexer.TokenType.VAR) {
            factor.setBase(new Var(lexer.peek()));
            lexer.next();
        } else if (
                lexer.type() == Lexer.TokenType.ADD
                || lexer.type() == Lexer.TokenType.SUB
        ) {
            StringBuilder sb = new StringBuilder();
            sb.append(lexer.peek()); // append +/- of a base(must be int)
            lexer.next();
            assert lexer.type() == Lexer.TokenType.NUM;
            sb.append(lexer.peek());
            lexer.next();
            factor.setBase(new Num(sb.toString()));
        } else {
            throw new IllegalArgumentException(
                    "parseFactor(): Wrong Factor format"
            );
        }

        // Parse factor's exp
        //
        // For now only numbers(int) are allowed for exp.
        if (lexer.type() == Lexer.TokenType.EXP) {
            lexer.next();
            // NOTE: only add allowed here
            if (lexer.type() == Lexer.TokenType.ADD) {
                lexer.next();
            }
            factor.setExp(Integer.parseInt(lexer.peek()));
            lexer.next();
        } else {
            factor.setExp(1); // No exp operator means "x ^ 1"
        }

        return factor;
    }
}
