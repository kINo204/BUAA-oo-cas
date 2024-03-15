package expressions;

import java.math.BigInteger;

public class Diff implements Base {
    private Expr expr;

    public Diff(Expr expr) {
        this.expr = expr;
    }

    @Override // unused
    public boolean mergeWith(Calc next) {
        return false;
    }

    @Override // unused
    public Calc diff() {
        return null;
    }

    // Never used.
    @Override
    public boolean simplify() {
        return false;
    }

    public void simplify(Factor factor) {
        expr.simplify();                        // Simplify before diff.
        Expr diffedExpr = (Expr) expr.diff();   // Perform diff.
        diffedExpr.simplify();      // Simplify after diff.
        factor.setBase(diffedExpr);
        factor.setIndex(BigInteger.ONE);
    }

    @Override
    public String toString() {
        return "dx(" + expr.toString() + ")";
    }

    @Override
    public Calc cloneSubTree() {
        return new Diff((Expr) expr.cloneSubTree());
    }
}
