package expressions;

import tool.Debuger;

import java.util.HashSet;
import java.util.Iterator;

public class Expr implements Calc, Base {
    private HashSet<Term> terms;

    public Expr() {
        terms = new HashSet<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Iterator<Term> termIterator = terms.iterator();
        Term term = termIterator.next();
        if (term.getOptTerm() == Operator.NEG) {
            sb.append("-");
        }
        sb.append(term);
        while (termIterator.hasNext()) {
            term = termIterator.next();
            if (term.getOptTerm() == Operator.POS) {
                sb.append("+");
            } else { // neg
                sb.append("-");
            }
            sb.append(term);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Expr)) {
            return false;
        }
        Expr expr = (Expr) obj;

        if (this.terms.size() != expr.terms.size()) {
            return false;
        }

        HashSet<Term> unchecked = new HashSet<>(expr.terms);
        for (Term t1 : this.terms) {
            boolean match = false;
            for (Term t2 : unchecked) {
                if (t1.equals(t2)) {
                    match = true;
                    unchecked.remove(t2);
                    break;
                }
            }
            if (!match) {
                return false;
            }
        }
        return true;
    }

    public void addTerm(Term term) {
        terms.add(term);
    }

    public void addTerm(HashSet<Term> terms) {
        this.terms.addAll(terms);
    }

    public boolean simplify() {
        stripTerms(); // Delete term "0"

        Debuger.println("E::simplify " + this);
        // Call terms' simplify
        while (true) {
            boolean unfolded = false;
            for (Term term : terms) {
                unfolded = term.simplify(this);
                if (!unfolded) {
                    break;
                } // list of terms changed by unfolding, for-loop restart
            }
            if (unfolded) {
                break;
            }
        } // unfolding finishes here

        addUpTerms(); // Add terms together
        stripTerms(); // Delete term "0" TODO: del this?
        return true;
    }

    /**
     * Note: terms must have been simplified before this method!
     */
    private void addUpTerms() {
        // Merge terms
        HashSet<Term> checked = new HashSet<>();
        Term checking;

        do {
            Iterator<Term> itr = terms.iterator();
            checking = itr.next();
            while (itr.hasNext() && checked.contains(checking)) {
                checking = itr.next();
            } // checking is the 1st unmerged term found

            boolean merged = false;
            while (itr.hasNext()) {
                merged = checking.addUpWith(itr.next());
                if (merged) {
                    itr.remove();
                }
            }
            checked.add(checking);
        } while (checked.size() != terms.size());
    }

    private void stripTerms() {
        // Delete term "0"
        Iterator<Term> termIterator = terms.iterator();
        Term term;
        while (termIterator.hasNext()) {
            term = termIterator.next();
            if (term.toString().equals("0") && terms.size() > 1) {
                termIterator.remove();
            }
        }
    }

    @Override
    public Calc cloneSubTree() {
        Expr expr = new Expr();
        for (Term term : terms) {
            expr.addTerm((Term) term.cloneSubTree());
        }
        return expr;
    }

    public HashSet<Term> getTerms() {
        return terms;
    }

    public void substituteTerm(Term substitutedTerm, HashSet<Term> srcTerms) {
        terms.addAll(srcTerms);
        if (substitutedTerm.getOptFact() != substitutedTerm.getOptTerm()) {
            for (Term t : srcTerms) {
                t.reverseOptTerm();
            }
        }
        terms.remove(substitutedTerm);
    }

    @Override
    public boolean mergeWith(Calc next) {
        if (!(next instanceof Expr)) {
            return false;
        }
        terms.addAll(((Expr) next).terms);
        return true;
    }

    /**
     * @return the current expressions' diff expression.
     */
    @Override
    public Calc diff() {
        Expr expr = new Expr();
        for (Term term : terms) {
            // Diff each term and add them together to form a new Expr.
            expr.mergeWith(term.diff());
        }
        return expr;
    }
}
