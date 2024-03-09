import expressions.Base;
import expressions.Exp;
import expressions.Expr;
import expressions.Factor;
import expressions.Num;
import expressions.Operator;
import expressions.Term;
import expressions.Var;

import java.math.BigInteger;

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
        setOpt(term);
        term.addFactor(parseFactor());
        while (lexer.type() == Lexer.TokenType.MUL) {
            lexer.next(); // jump "*" token
            term.addFactor(parseFactor());
        }
        return term;
    }

    private void setOpt(Term term) {
        // Read +/- before the Term.
        switch (lexer.type()) {
            case ADD:
                term.setOptTerm(Operator.POS);
                lexer.next();
                break;
            case SUB:
                term.setOptTerm(Operator.NEG);
                lexer.next();
                break;
            default: // No operator -> positive
                term.setOptTerm(Operator.POS);
                break;
        }
        // Read the +/- before ALL FACTORS OF A TERM and store
        //
        // May cause inconsistency with requirement here:
        // the program accepts "+/-" before a var/(expr), while
        // this isn't required.
        switch (lexer.type()) {
            case ADD:
                term.setOptFact(Operator.POS);
                lexer.next();
                break;
            case SUB:
                term.setOptFact(Operator.NEG);
                lexer.next();
                break;
            default: // No operator -> positive
                term.setOptFact(Operator.POS);
                break;
        }
    }

    private Factor parseFactor() {
        Factor factor = new Factor();

        // Parse factor's base
        if (lexer.peek().equals("(")) { // (expr) ^ expo
            lexer.next(); // jump the "(" token
            factor.setBase(parseExpr());
            lexer.next(); // jump the ")" token
        } else { // non-expr ^ expo
            factor.setBase(parseBase());
        }

        // Parse factor's exponent
        if (lexer.type() == Lexer.TokenType.IND) {
            lexer.next(); // jump the "^" token
            // NOTE: only add allowed before exponent
            if (lexer.type() == Lexer.TokenType.ADD) {
                lexer.next();
            }
            factor.setIndex(new BigInteger(lexer.next()));
        } else {
            factor.setIndex(BigInteger.ONE); // No exp operator means "x ^ 1"
        }

        return factor;
    }

    private Base parseBase() {
        switch (lexer.type()) {
            case NUM:
                return new Num(lexer.next());
            case VAR:
                // For Exp:
                if (lexer.peek().equals("exp")) {
                    lexer.next();
                    lexer.next(); // jump "(" token after "exp"
                    Expr expr = parseExpr();
                    Exp exp = new Exp(expr);
                    lexer.next(); // jump ")" token after "exp"
                    return exp;
                }
                // For Var:
                return new Var(lexer.next());
            case ADD:
            case SUB:
                StringBuilder sb = new StringBuilder();
                sb.append(lexer.next()); // append +/- of a base(must be int)
                // only int allowed for base after +/-
                assert lexer.type() == Lexer.TokenType.NUM;
                sb.append(lexer.next());
                return new Num(sb.toString());
            default:
                throw new IllegalArgumentException(
                        "parseFactor(): Wrong Factor format"
                );
        }
    }
}
