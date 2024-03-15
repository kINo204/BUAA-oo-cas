package expressions;

public interface Calc {

    public boolean simplify();

    boolean mergeWith(Calc next);

    Calc diff();

    public Calc cloneSubTree();

}
