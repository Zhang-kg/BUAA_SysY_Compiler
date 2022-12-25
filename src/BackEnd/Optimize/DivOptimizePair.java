package BackEnd.Optimize;

import java.math.BigInteger;

public class DivOptimizePair {
    private BigInteger m;
    private int sh_post;
    private int l;

    public DivOptimizePair(BigInteger m, int sh_post, int l) {
        this.m = m;
        this.sh_post = sh_post;
        this.l = l;
    }

    public BigInteger getM() {
        return m;
    }

    public int getSh_post() {
        return sh_post;
    }

    public int getL() {
        return l;
    }
}
