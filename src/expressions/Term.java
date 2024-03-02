package expressions;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Term implements Calc {
    private HashSet<Factor> factors;
    private OptPosNeg optTerm;

    private OptPosNeg optFact;

    public Term() {
        factors = new HashSet<>();
        optTerm = OptPosNeg.POS;
        optFact = OptPosNeg.POS;
    }

    public void setOptTerm(OptPosNeg optPosNeg) {
        this.optTerm = optPosNeg;
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
        // Call factors to simplify
        for (Factor factor : factors) {
            factor.simplify();
        } // Lower level simplification finishes here.

        // All "(Expr) ^ int" with int > 1 get duplicated:
        dupBracedExpr();
        // Unfold braces
        if (unfoldBracs(upperExpr)) {
            return false;
        }
        multFactors();
        stripFactors(); // Check for factor "+-1" or "0"
        mergeOpt();
        return true;
    }

    private boolean unfoldBracs(Expr upperExpr) {
        // stripFactors();
        for (Factor factor : factors) {
            if (factor.isBaseExpr()) {
                // get all other factors
                HashSet<Factor> remain = new HashSet<>();
                for (Factor factor1 : factors) {
                    if (factor1 != factor) {
                        remain.add((Factor) factor1.cloneTree());
                    }
                }
                for (Term term : ((Expr) factor.getBase()).getTerms()) {
                    // Add them to all lower level terms' factors:
                    HashSet<Factor> tmp = new HashSet<>();
                    for (Factor factor1 : remain) {
                        tmp.add((Factor) factor1.cloneTree());
                    }
                    term.addFactor(tmp);
                }
                upperExpr.substituteTerm(this, ((Expr) factor.getBase()).getTerms());
                return true;
            }
        } // All braces below this level have been unfolded.
        return false;
    }

    private void dupBracedExpr() {
        while (true) {
            boolean dupMotion = false; // TODO: is this init correct?
            for (Factor factor : factors) {
                if (factor.isBaseExpr()) {
                    dupMotion = factor.getExp() > 1;
                    int exp = factor.getExp();
                    factor.setExp(1);
                    for (int i = 0; i < exp - 1; i++) {
                        factors.add((Factor) factor.cloneTree());
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
                merged = checking.mergeWith(itr.next());
                if (merged) {
                    itr.remove();
                }
            }
            checked.add(checking);
        } while (checked.size() != factors.size());
    }

    public void mergeOpt() {
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
        if (optFact == OptPosNeg.NEG) {
            reverseOptFact();
            reverseOptTerm();
        }
    }

    private void reverseOptFact() {
        if (optFact == OptPosNeg.POS) {
            optFact = OptPosNeg.NEG;
        } else {
            optFact = OptPosNeg.POS;
        }
    }

    public void reverseOptTerm() {
        if (optTerm == OptPosNeg.POS) {
            optTerm = OptPosNeg.NEG;
        } else {
            optTerm = OptPosNeg.POS;
        }
    }

    public Calc cloneTree() {
        Term term = new Term();
        term.setOptTerm(optTerm);
        for (Factor factor : factors) {
            term.addFactor((Factor) factor.cloneTree());
        }
        return term;
    }

    public boolean mergeWith(Term next) {
        if (mergePrep(next)) {
            return false;
        }
        for (Factor factor : factors) {
            if (factor.isBaseNum()) {
                for (Factor factor1 : next.factors) {
                    if (factor1.isBaseNum()) {
                        // Both this and next have the num factor:
                        if (optTerm == next.optTerm) {
                            ((Num) factor.getBase()).add((Num) factor1.getBase());
                        } else {
                            ((Num) factor.getBase()).sub((Num) factor1.getBase());
                        }
                        mergeOpt();
                        stripFactors();
                        return true;
                    }
                }
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
        newFact.setExp(1);
        factors.add(newFact);
        mergeOpt();
        stripFactors();
        return true;
    }

    private boolean mergePrep(Term next) {
        HashMap<String, Integer> varTable1 = new HashMap<>();
        HashMap<String, Integer> varTable2 = new HashMap<>();
        for (Factor factor : factors) {
            if (factor.isBaseVar()) {
                varTable1.put(
                        factor.getBase().toString(),
                        factor.getExp()
                );
            }
        }
        for (Factor factor : next.factors) {
            if (factor.isBaseVar()) {
                varTable2.put(
                        factor.getBase().toString(),
                        factor.getExp()
                );
            }
        }
        if (varTable1.isEmpty() && !varTable2.isEmpty()) {
            return true;
        }
        for (String key : varTable1.keySet()) {
            if (!varTable2.containsKey(key)
                    || !varTable1.get(key).equals(varTable2.get(key))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Iterator<Factor> factorIterator = factors.iterator();
        Factor factor = factorIterator.next();
        if (optFact == OptPosNeg.NEG) {
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

    public OptPosNeg getOptTerm() {
        return optTerm;
    }

    public OptPosNeg getOptFact() {
        return optFact;
    }

    public void setOptFact(OptPosNeg optPosNeg) {
        optFact = optPosNeg;
    }
}
