package expressions;

public class Exp implements Calc, Base {
    private Expr expr;

    public Exp(Expr e) {
        expr = e;
    }

    @Override
    public boolean simplify() {
        expr.simplify();
        return true;
    }

    @Override
    public Calc cloneSubTree() {
        return new Exp((Expr) expr.cloneSubTree());
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("exp(");
        if (expr.getTerms().size() == 1) {
            for (Term t : expr.getTerms()) { // only 1 term here
                if (t.singleFactor()) {
                    out.append(expr.toString()).append(")");
                    return out.toString();
                }
            }
        }
        out.append("(");
        out.append(expr.toString()).append(")");
        out.append(")");
        return out.toString();

    }

    @Override
    public boolean mergeWith(Base next) {
        if (next instanceof Exp) {
            this.expr.mergeWith(((Exp) next).expr);
            this.expr.simplify();
            return true;
        }
        return false;
    }
}
