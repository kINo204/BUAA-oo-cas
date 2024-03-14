package expressions;

import java.math.BigInteger;

public class Num implements Calc, Base {
    private BigInteger num;

    public Num(String num) {
        this.num = new BigInteger(num);
    }

    public Num(BigInteger num) {
        this.num = new BigInteger(num.toString());
    }

    public void add(Num num) {
        this.num = this.num.add(num.num);
    }

    public void add(BigInteger num) {
        this.num = this.num.add(num);
    }

    public void sub(BigInteger num) {
        this.num = this.num.subtract(num);
    }

    public void sub(Num num) {
        this.num = this.num.subtract(num.num);
    }

    @Override
    public boolean mergeWith(Base next) {
        if (next instanceof Num) {
            this.num = this.num.multiply(((Num) next).num);
            return true;
        }
        return false;
    }

    @Override // never used
    public boolean simplify() {
        return true;
    }

    @Override
    public Calc cloneSubTree() {
        BigInteger bigInteger = this.num;
        return new Num(bigInteger);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Num)) {
            return false;
        }
        Num num = (Num) obj;
        return this.num.compareTo(num.num) == 0;
    }

    @Override
    public String toString() {
        return String.valueOf(num);
    }
}
