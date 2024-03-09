package expressions;

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
        // Call factors' simplify
        for (Factor factor : factors) {
            factor.simplify();
        }
        dupExpr(); // All "(Expr) ^ int" or "exp() ^ int" with int > 1 get duplicated
        // Unfold braces
        if (unfoldBracs(upperExpr)) {
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
                        BigInteger i = BigInteger.ZERO;
                        i.compareTo(exp.subtract(BigInteger.ONE)) < 0;
                        i = i.add(BigInteger.ONE)
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

    private void multFactors() {
        // TODO: need refactor
        // Multiply terms:
        HashSet<Factor> checked = new HashSet<>();
        Factor checking;

        do {
            Iterator<Factor> itr = factors.iterator();
            checking = itr.next();
            while (itr.hasNext() && checked.contains(checking)) {
                checking = itr.next();
            } // checking: the 1st unmerged term found

            boolean merged = false;
            while (itr.hasNext()) {
                merged = checking.multWith(itr.next());
                if (merged) {
                    itr.remove();
                }
            }
            checked.add(checking);
        } while (checked.size() != factors.size());
        stripFactors(); // Check for factor "+-1" or "0"
        mergeOpt();
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
            } else if (factor.getBase().toString().equals("0")) { // 0 * any = 0:
                // Delete all other factors
                Iterator<Factor> itr1 = factors.iterator();
                Factor factor1;
                while (itr1.hasNext()) {
                    factor1 = itr1.next();
                    if (!factor1.getBase().toString().equals("0")) {
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
        HashMap<String, BigInteger> varTable1 = new HashMap<>();
        HashMap<String, BigInteger> varTable2 = new HashMap<>();
        for (Factor factor : factors) {
            if (!factor.isBaseNum()) {
                varTable1.put(
                        factor.getBase().toString(),
                        factor.getIndex()
                );
            }
        }
        for (Factor factor : next.factors) {
            if (!factor.isBaseNum()) {
                varTable2.put(
                        factor.getBase().toString(),
                        factor.getIndex()
                );
            }
        }
        if (varTable1.size() != varTable2.size()) {
            return true; // unmergable
        }
        for (String key : varTable1.keySet()) {
            if (!varTable2.containsKey(key)
                    || !varTable1.get(key).equals(varTable2.get(key))) {
                return true;
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
