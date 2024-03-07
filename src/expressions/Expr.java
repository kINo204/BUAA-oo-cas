package expressions;

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

    public void addTerm(Term term) {
        terms.add(term);
    }

    public void addTerm(HashSet<Term> terms) {
        this.terms.addAll(terms);
    }

    public boolean simplify() {
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
        stripTerms(); // Delete term "0"
        return true; // TODO: return value here seems useless
    }

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
    public boolean mergeWith(Base next) {
        if (next instanceof Expr) {
            terms.addAll(((Expr) next).terms);
            addUpTerms();
            return true;
        }
        return false;
    }
}
