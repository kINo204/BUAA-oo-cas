package expressions;

public class Factor implements Calc {

    private Base base;
    private int exp;

    public void setBase(Base base) {
        this.base = base;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (base instanceof Expr
        /*|| (base instanceof Num && base.toString().charAt(0) == '-'*/
        ) {
            sb.append("(");
        }
        sb.append(base.toString());
        if (base instanceof Expr
        /*|| (base instanceof Num && base.toString().charAt(0) == '-'*/
        ) {
            sb.append(")");
        }
        if (exp != 1) {
            sb.append("^");
            sb.append(exp);
        }

        return sb.toString();
    }

    public boolean simplify() {
        if (exp == 0) {
            exp = 1;
            base = new Num("1");
            return true;
        } else if (base instanceof Expr) {
            base.simplify();
        } else if (base instanceof Num) {
            if (exp > 1) {
                Num ori = (Num) base.cloneTree();
                for (int i = 0; i < exp - 1; i++) {
                    ((Num) base).mul(ori);
                }
                exp = 1;
            }
        }
        return true; // TODO: the 2 return values seem useless
    }

    public Calc cloneTree() {
        Factor factor = new Factor();
        factor.setBase((Base) base.cloneTree());
        factor.setExp(exp);
        return factor;
    }

    public boolean isBaseExpr() {
        return base instanceof Expr;
    }

    public boolean isBaseVar() {
        return base instanceof Var;
    }

    public boolean isBaseNum() {
        return base instanceof Num;
    }

    public int getExp() {
        return exp;
    }

    public Base getBase() {
        return base;
    }

    public boolean mergeWith(Factor next) {
        assert !(next.base instanceof Expr);
        assert exp == 1 && next.exp == 1;
        if (base instanceof Num) {
            if (next.base instanceof Var) {
                return false;
            }
            ((Num) base).mul((Num) next.base);
        } else if (base instanceof Var) {
            if (next.base instanceof Num
                    || !next.base.toString().equals(base.toString())) {
                return false;
            }
            exp += next.exp;
        }
        return true;
    }
}
