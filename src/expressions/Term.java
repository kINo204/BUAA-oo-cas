package expressions;

import tool.Debuger;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Term implements Calc {
    private HashSet<Factor> factors;
    private Operator optTerm;

    private Operator optFact;

    public Term() {
        factors = new HashSet<>();
        optTerm = Operator.POS;
        optFact = Operator.POS;
    }

    public HashSet<Factor> getFactors() {
        return factors;
    }

    public void setOptTerm(Operator operator) {
        this.optTerm = operator;
    }

    public void addFactor(Factor factor) {
        factors.add(factor);
    }

    public void addFactor(HashSet<Factor> factors) {
        this.factors.addAll(factors);
    }

    public boolean simplify() { // never used
        return true;
    }

    public boolean simplify(Expr upperExpr) {
        Debuger.println("T::simplify " + this);
        this.stripFactors(); // optimize: reduce factors number before simplifying.

        for (Factor factor : factors) {
            factor.simplify();
        }

        // Unfold braces
        if (unfoldBracs(upperExpr)) {
            // TODO: can this term create a new expression and use it? optimize?
            // if successfully unfolded, the current Term will
            // be replaced in the original Expr, so return and restart
            // with updated Expr.terms
            return false;
        }
        multFactors();
        return true;
    }

    private void dupExpr() {
        while (true) {
            boolean dupMotion = false;
            for (Factor factor : factors) {
                if (factor.isBaseExpr() || factor.isBaseExp()) {
                    dupMotion = factor.getIndex().compareTo(BigInteger.ONE) > 0;
                    BigInteger exp = factor.getIndex();
                    factor.setIndex(BigInteger.ONE);
                    for (
                        BigInteger i = BigInteger.ZERO;                 // i = 0;
                        i.compareTo(exp.subtract(BigInteger.ONE)) < 0;  // i < exp - 1;
                        i = i.add(BigInteger.ONE)                       // i++;
                    ) {
                        factors.add((Factor) factor.cloneSubTree());
                    }
                    if (dupMotion) {
                        break;
                    }
                }
            }
            if (!dupMotion) {
                break;
            }
        }
    }

    private boolean unfoldBracs(Expr upperExpr) {
        // stripFactors();
        for (Factor factor : factors) {
            if (factor.isBaseExpr()) {
                // get all other factors
                HashSet<Factor> remain = new HashSet<>();
                for (Factor f : factors) {
                    if (f != factor) {
                        remain.add((Factor) f.cloneSubTree());
                    }
                }
                // Add them to all lower level terms' factors.
                for (Term term : ((Expr) factor.getBase()).getTerms()) {
                    // Create a cloned subtree for each term.
                    HashSet<Factor> cloneOfRemain = new HashSet<>();
                    for (Factor f : remain) {
                        cloneOfRemain.add((Factor) f.cloneSubTree());
                    }
                    term.addFactor(cloneOfRemain);
                }
                upperExpr.substituteTerm(this, ((Expr) factor.getBase()).getTerms());
                return true;
            }
        } // All braces below this level have been unfolded.
        return false;
    }

    /*
    multFactors() mult a Term's factors together to merge them.
    The method deals with simple factor structures:
        2 ^ 3
        var ^ 9
        exp(...)
     */
    private void multFactors() {
        HashSet<Factor> checkedFactors = new HashSet<>();
        Factor checking;
        while (checkedFactors.size() != factors.size()) {
            Iterator<Factor> itr = factors.iterator();
            // checking = the 1st unmerged factor found
            checking = itr.next();
            while (itr.hasNext() && checkedFactors.contains(checking)) {
                checking = itr.next();
            }
            // Search for ALL factors able to mult with `checking` and mult.
            // 2 * 4
            while (itr.hasNext()) {
                if (checking.multWith(itr.next())) {    // = 8 * 4
                    itr.remove();                       // = 8
                }
            }
            checkedFactors.add(checking);
        }

        this.stripFactors(); // TODO: del this?
        this.mergeOpt();
    }

    private void stripFactors() {
        // Check for factor "+-1" or "0" TODO: CHECK THIS
        Iterator<Factor> itr = factors.iterator();
        Factor factor;
        while (itr.hasNext()) {
            factor = itr.next();
            // 1 * x = x, (-1) * x = - x
            if (factor.isBaseNum() && factor.getBase().toString().equals("1")
                    && factors.size() > 1) {
                itr.remove();
            } else if (factor.isBaseNum() && factor.getBase().toString().equals("-1")
                    && factors.size() > 1) {
                reverseOptFact();
                itr.remove();
            } else if (factor.getBase().toString().equals("0")
                        && !factor.getIndex().equals(BigInteger.ZERO)) { // 0 * any = 0:
                // Delete all other factors
                Iterator<Factor> itr1 = factors.iterator();
                Factor factor1;
                while (itr1.hasNext()) {
                    factor1 = itr1.next();
                    if (factor1 != factor) {
                        itr1.remove();
                    }
                }
                break;
            }
        }
    }

    private void mergeOpt() {
        // Signal merge: num -> fact
        for (Factor f : factors) {
            if (f.isBaseNum() && f.getBase().toString().charAt(0) == '-') {
                Num num = new Num(f.getBase().toString().substring(1));
                f.setBase(num);
                reverseOptFact();
                break;
            }
        }
        // Signal merge: fact -> term
        if (optFact == Operator.NEG) {
            reverseOptFact();
            reverseOptTerm();
        }
    }

    private void reverseOptFact() {
        if (optFact == Operator.POS) {
            optFact = Operator.NEG;
        } else {
            optFact = Operator.POS;
        }
    }

    public void reverseOptTerm() {
        if (optTerm == Operator.POS) {
            optTerm = Operator.NEG;
        } else {
            optTerm = Operator.POS;
        }
    }

    public Calc cloneSubTree() {
        Term term = new Term();
        term.setOptTerm(optTerm);
        for (Factor factor : factors) {
            term.addFactor((Factor) factor.cloneSubTree());
        }
        return term;
    }

    public boolean addUpWith(Term next) {
        if (addUpPrep(next)) {
            return false;
        }
        for (Factor factor : factors) {
            if (factor.isBaseNum()) {
                for (Factor f : next.factors) {
                    if (f.isBaseNum()) {
                        // Both this and next have the num factor:
                        if (optTerm == next.optTerm) {
                            ((Num) factor.getBase()).add((Num) f.getBase());
                        } else {
                            ((Num) factor.getBase()).sub((Num) f.getBase());
                        }
                        mergeOpt();
                        stripFactors();
                        return true;
                    }
                }
                // next has no num factor, but this has one:
                if (optTerm == next.optTerm) {
                    ((Num) factor.getBase()).add(new Num("1"));
                } else {
                    ((Num) factor.getBase()).sub(new Num("1"));
                }
                mergeOpt();
                stripFactors();
                return true;
            }
        }
        // Reaching here means no num factor in this.factors. Add a new num-fact:
        Num num = new Num("1"); // no num fact: num is 1
        BigInteger nextNum = null;
        for (Factor factor : next.factors) {
            if (factor.isBaseNum()) {
                // next has the num factor:
                nextNum = new BigInteger(factor.getBase().toString());
                break;
            }
        }
        if (nextNum == null) {
            // None of this and next has a num factor:
            nextNum = new BigInteger("1");
        }
        if (optTerm == next.optTerm) {
            num.add(nextNum);
        } else {
            num.sub(nextNum);
        }
        Factor newFact = new Factor();
        newFact.setBase(num);
        newFact.setIndex(BigInteger.ONE);
        factors.add(newFact);
        mergeOpt();
        stripFactors();
        return true;
    }

    private boolean addUpPrep(Term next) {
        HashMap<Base, BigInteger> varTable1 = new HashMap<>();
        HashMap<Base, BigInteger> varTable2 = new HashMap<>();
        for (Factor factor : factors) {
            if (!factor.isBaseNum()) {
                varTable1.put(
                        factor.getBase(),
                        factor.getIndex()
                );
            }
        }
        for (Factor factor : next.factors) {
            if (!factor.isBaseNum()) {
                varTable2.put(
                        factor.getBase(),
                        factor.getIndex()
                );
            }
        }
        if (varTable1.size() != varTable2.size()) {
            return true; // unmergable
        }
        HashSet<Base> unchecked = new HashSet<>(varTable2.keySet());
        for (Base base1 : varTable1.keySet()) {
            boolean match = false;
            for (Base base2 : unchecked) {
                if (base1.equals(base2)) {
                    match = true;
                    if (!(varTable1.get(base1).
                            equals(varTable2.get(base2)))) {
                        return true;
                    }
                    unchecked.remove(base2);
                    break;
                }
            }
            if (!match) {
                return true; // a var not found
            }
        }
        return false;
    }

    public boolean singleFactor() {
        return factors.size() == 1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Iterator<Factor> factorIterator = factors.iterator();
        Factor factor = factorIterator.next();
        if (optFact == Operator.NEG) {
            sb.append("-");
        }
        sb.append(factor);
        while (factorIterator.hasNext()) {
            sb.append("*");
            factor = factorIterator.next();
            sb.append(factor);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Term)) {
            return false;
        }
        Term term = (Term) obj;

        // Must be the same signal.
        if ((this.optTerm == this.optFact) != (term.optTerm == term.optFact)) {
            return false;
        }
        // Must be the same size.
        if (this.factors.size() != term.factors.size()) {
            return false;
        }
        // 1-1 compare between factors.
        HashSet<Factor> unchecked = new HashSet<>(term.factors);
        for (Factor f1 : this.factors) {
            boolean match = false;
            for (Factor f2 : unchecked) {
                if (f1.equals(f2)) {
                    match = true;
                    unchecked.remove(f2);
                    break;
                }
            }
            if (!match) {
                return false;
            }
        }
        return true;
    }

    public Operator getOptTerm() {
        return optTerm;
    }

    public Operator getOptFact() {
        return optFact;
    }

    public void setOptFact(Operator operator) {
        optFact = operator;
    }
}
