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
        if (term.getOptTerm() == OptPosNeg.NEG) {
            sb.append("-");
        }
        sb.append(term);
        while (termIterator.hasNext()) {
            term = termIterator.next();
            if (term.getOptTerm() == OptPosNeg.POS) {
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

    public boolean simplify() {
        // Call term simplify which will unfold braces
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
            } // unfolding finishes here
        }

        // Factors should have been simplified when reaching here.
        // TODO: need refactor
        // Add/sub factors:
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
                merged = checking.mergeWith(itr.next());
                if (merged) {
                    itr.remove();
                }
            }
            checked.add(checking);
        } while (checked.size() != terms.size());

        // Delete term "0"
        Iterator<Term> termIterator = terms.iterator();
        Term term;
        while (termIterator.hasNext()) {
            term = termIterator.next();
            if (term.toString().equals("0") && terms.size() > 1) {
                termIterator.remove();
            }
        }

        return true; // TODO: return value here seems useless
    }

    @Override
    public Calc cloneTree() {
        Expr expr = new Expr();
        for (Term term : terms) {
            expr.addTerm((Term) term.cloneTree());
        }
        return expr;
    }

    public HashSet<Term> getTerms() {
        return terms;
    }

    public void substituteTerm(Term substitutedTerm, HashSet<Term> srcTerms) {
        terms.addAll(srcTerms);
        substitutedTerm.mergeOpt();
        if (substitutedTerm.getOptTerm() == OptPosNeg.NEG) {
            for (Term t : srcTerms) {
                t.reverseOptTerm();
            }
        }
        terms.remove(substitutedTerm);
    }
}
