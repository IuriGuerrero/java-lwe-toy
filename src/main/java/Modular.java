public final class Modular {

    private final int q;

    public Modular(int q) {
        if(q < 2) {
            throw new IllegalArgumentException("q can't be lower than 2");
        }
        this.q = q;
    }

    public int reduce(long x) {
        return (int) Math.floorMod(x, (long) q);
    }

    public int center(int x) {
        int reduced = reduce(x);
        if(reduced <= q/2) {
            return reduced;
        }
        return reduced - q;
    }

    public int scalarProduct(int[] a, int[] b) {
        if(a.length != b.length) {
            throw new IllegalArgumentException("Different lengths: " + a.length + " and " + b.length);
        }
        long res = 0;
        for(int i = 0; i < a.length; i++) {
            res += ((long) a[i]) * b[i];
        }
        return reduce(res);
    }

    public int[] matrixXVector(int[][] A, int[] v) {
        // scalarProduct already checks the essential condition
        int[] res = new int[A.length];
        for(int i = 0; i < A.length; i++) {
            res[i] = scalarProduct(A[i], v);
        }
        return res;
    }

}
