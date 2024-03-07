package expressions;

public class Factor implements Calc {

    private Base base;
    private int index;

    public void setBase(Base base) {
        this.base = base;
    }

    public void setIndex(int exp) {
        this.index = exp;
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
        if (index != 1) {
            sb.append("^");
            sb.append(index);
        }

        return sb.toString();
    }

    public boolean simplify() {
        // Exponent == 0:
        if (index == 0) {
            index = 1;
            base = new Num("1");
            return true;
        }

        // Exponent != 0:
        base.simplify(); // No operation for Num & Var
        if (base instanceof Num) {
            if (index > 1) {
                Num ori = (Num) base.cloneSubTree();
                for (int i = 0; i < index - 1; i++) {
                    ((Num) base).mergeWith(ori);
                }
                index = 1;
            }
        }
        return true; // TODO: the 2 return values seem useless
    }

    public Calc cloneSubTree() {
        Factor factor = new Factor();
        factor.setBase((Base) base.cloneSubTree());
        factor.setIndex(index);
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

    public int getIndex() {
        return index;
    }

    public Base getBase() {
        return base;
    }

    public boolean multWith(Factor next) {
        assert !(next.base instanceof Expr);

        boolean merged = base.mergeWith(next.base);
        if (!merged) {
            return false;
        }
        if (base instanceof Var) {
            index += next.index;
        }
        return true;
    }

    public boolean isBaseExp() {
        return base instanceof Exp;
    }
}
