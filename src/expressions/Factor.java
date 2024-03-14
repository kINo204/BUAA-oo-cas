package expressions;

import tool.BigIntegerMultiLooper;
import tool.Debuger;

import java.math.BigInteger;
import java.util.ArrayList;

public class Factor implements Calc {

    private Base base;
    private BigInteger index;

    public void setBase(Base base) {
        this.base = base;
    }

    public void setIndex(BigInteger exp) {
        this.index = exp;
    }

    /*
    equals() compare two SIMPLIFIED objects.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Factor)) {
            return false;
        }
        Factor factor = (Factor) obj;

        assert !(base instanceof Expr);
        if (base instanceof Num) {
            assert index.equals(BigInteger.ONE);
            return this.base.equals(factor.base);
        } else if (base instanceof Var) {
            return
                    this.base.equals(factor.base)
                    && this.index.equals(factor.index);
        } else if (base instanceof Exp) {
            assert index.equals(BigInteger.ONE);
            return this.base.equals(factor.base);
        }
        return false;
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
        if (!index.equals(BigInteger.ONE)) {
            sb.append("^");
            sb.append(index);
        }

        return sb.toString();
    }

    public boolean simplify() {
        Debuger.println("F::simplify " + this);
        // Exponent == 0: -> 1
        if (index.equals(BigInteger.ZERO)) {
            index = BigInteger.ONE;
            base = new Num("1");
            return true;
        }
        // Exponent >= 1:
        base.simplify();
        if (index.compareTo(BigInteger.ONE) > 0) { // expo > 1
            if (base instanceof Num) { // Calculate "num ^ expo" if expo > 1
                Num ori = (Num) base.cloneSubTree();
                for (
                    BigInteger i = BigInteger.ZERO;                     // i == 0;
                    i.compareTo(index.subtract(BigInteger.ONE)) < 0;    // i < index - 1;
                    i = i.add(BigInteger.ONE)                           // i++;
                ) {
                    base.mergeWith(ori);                                // base *= ori_base;
                }
                index = BigInteger.ONE;                                 // index = 1;
            } else { // If base is a complex structure
                if (base instanceof Exp) {
                    powExp();
                } else if (base instanceof Expr) {
                    powExpr();
                }
            }

        }
        return true; // TODO: the 2 return values seem useless
    }

    private void powExpr() {
        Expr newExpr = new Expr();
        Expr oriExpr = (Expr) base;
        int layerNum = oriExpr.getTerms().size();
        BigIntegerMultiLooper looper = new BigIntegerMultiLooper(
                index.add(BigInteger.ONE), layerNum
        );
        while (looper.hasNext()) {
            execPow(looper, layerNum, oriExpr, newExpr);
            looper.step();
        }
        execPow(looper, layerNum, oriExpr, newExpr);
        newExpr.simplify();
        this.base = newExpr;
        this.index = BigInteger.ONE;
    }

    private void execPow(BigIntegerMultiLooper looper, int layerNum, Expr oriExpr, Expr newExpr) {
        ArrayList<BigInteger> loopVars;
        loopVars = looper.peek();
        // For each n1 + n2 + ... ni = n:
        if (sumOf(loopVars).compareTo(index) == 0) {
            // Prefix of the expanded term
            BigInteger preVal = fctorial(index);
            for (int i = 0; i < layerNum; i++) {
                preVal = preVal.divide(fctorial(loopVars.get(i)));
            }
            Factor f = new Factor();
            f.setBase(new Num(preVal));
            f.setIndex(BigInteger.ONE);
            Term t = new Term(); // One of the terms of the final expanded Expr.
            t.addFactor(f);

            // Latter factors
            ArrayList<Term> terms = new ArrayList<>(oriExpr.getTerms());
            for (int i = 0; i < layerNum; i++) {
                for (Factor factor : terms.get(i).getFactors()) {
                    Factor factor1 = new Factor();
                    factor1.setBase((Base) factor.base.cloneSubTree());
                    factor1.setIndex(factor.getIndex().multiply(loopVars.get(i)));
                    t.addFactor(factor1);
                }
                if (terms.get(i).getOptTerm() != terms.get(i).getOptFact()) {
                    if (loopVars.get(i).divideAndRemainder(
                            new BigInteger("2"))[1].equals(BigInteger.ONE)) {
                        t.reverseOptTerm();
                    }
                }
            }
            newExpr.addTerm(t);
        }
    }

    private BigInteger sumOf(ArrayList<BigInteger> nums) {
        BigInteger bigInteger = BigInteger.ZERO;
        for (BigInteger n : nums) {
            bigInteger = bigInteger.add(n);
        }
        return bigInteger;
    }

    private BigInteger fctorial(BigInteger num) {
        if (num.compareTo(BigInteger.ZERO) == 0) {
            return BigInteger.ONE;
        }
        BigInteger bigInteger = new BigInteger("1");
        for (
            BigInteger i = BigInteger.ONE;  // i = 1;
            i.compareTo(num) <= 0;          // i <= num;
            i = i.add(BigInteger.ONE)       // i++
        ) {
            bigInteger = bigInteger.multiply(i);
        }
        return bigInteger;
    }

    private void powExp() {
        // exp(a + b) ^ c = exp((a + b) * c)
        Num num = new Num(index);

        Term term = new Term();

        Factor factIndex = new Factor();
        factIndex.setBase((Base) num.cloneSubTree());
        factIndex.setIndex(BigInteger.ONE);
        term.addFactor(factIndex);

        Factor factExpr = new Factor();
        factExpr.setBase(((Exp) base).getExpr());
        factExpr.setIndex(BigInteger.ONE);
        term.addFactor(factExpr);

        Expr expr = new Expr();
        expr.addTerm(term);
        expr.simplify();

        ((Exp) base).setExpr(expr);
        this.index = BigInteger.ONE;
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

    public BigInteger getIndex() {
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
            index = index.add(next.index);
        }
        return true;
    }

    public boolean isBaseExp() {
        return base instanceof Exp;
    }
}
