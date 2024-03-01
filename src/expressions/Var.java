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
    public Base cloneTree() {
        return new Var(this.var);
    }

    @Override
    public String toString() {
        return var;
    }
}
