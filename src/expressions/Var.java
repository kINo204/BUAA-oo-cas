package expressions;

public class Var implements Calc, Base {
    private String var;

    public Var(String var) {
        this.var = var;
    }

    @Override // never used
    public boolean simplify() {
        return true;
    }

    @Override
    public Base cloneSubTree() {
        return new Var(this.var);
    }

    @Override
    public String toString() {
        return var;
    }

    @Override
    public boolean mergeWith(Calc next) {
        return next instanceof Var && toString().equals(next.toString());
    }

    @Override // unused
    public Calc diff() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Var)) {
            return false;
        }
        Var var = (Var) obj;
        return this.var.equals(var.var);
    }
}
