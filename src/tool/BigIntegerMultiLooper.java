package tool;

import java.math.BigInteger;
import java.util.ArrayList;

public class BigIntegerMultiLooper {
    private final BigInteger range;
    private final int layerNum;
    private final ArrayList<BigInteger> list;

    public BigIntegerMultiLooper(BigInteger range, int layerNum) {
        this.range = range;
        this.layerNum = layerNum;
        list = new ArrayList<>();
        for (int i = 0; i < layerNum; i++) {
            list.add(BigInteger.ZERO);
        }
    }

    public void step() {
        step(0);
    }

    public void step(int startLayer) {
        list.set(startLayer, list.get(startLayer).add(BigInteger.ONE)); // list[i]++
        if (list.get(startLayer).compareTo(range) == 0) {      // if reach max range:
            list.set(startLayer, BigInteger.ZERO);       // wrap;
            if (startLayer < layerNum - 1) {             // and carry if available
                this.step(startLayer + 1);
            }
        }
    }

    public boolean hasNext() {
        for (BigInteger bigInteger : list) {
            if (bigInteger.compareTo(range.subtract(BigInteger.ONE)) < 0) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<BigInteger> peek() {
        return list;
    }
}
