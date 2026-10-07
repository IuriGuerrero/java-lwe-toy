import java.security.SecureRandom;

public final class LweToy {

    public record PublicKey(int[][] A, int[] b) {}
    public record SecretKey(int[] s) {}
    public record KeyPair(PublicKey pk, SecretKey sk) {}

    private final int q, n, m, errorBound;
    private final Modular mod;
    private final SecureRandom rng = new SecureRandom();

    public LweToy(int q, int n, int m, int errorBound) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be at least 1 (actual value: " + n + ")");
        }
        if (m < 1) {
            throw new IllegalArgumentException("m must be at least 1 (actual value: " + m + ")");
        }
        if (errorBound < 0) {
            throw new IllegalArgumentException("errorBound must be non-negative (actual value: " + errorBound + ")");
        }
        this.mod = new Modular(q); // Modular ya valida que q >= 2
        this.q = q;
        this.n = n;
        this.m = m;
        this.errorBound = errorBound;
    }

    /**
     * Devuelve un vector de 'length' números aleatorios uniformes entre 0 y q-1.
     * Se usa para generar el secreto s.
     */
    private int[] randomVector(int length) {
        int[] res = new int[length];
        for(int i = 0; i < length; i++) {
            res[i] = rng.nextInt(q);
        }
        return res;
    }

    /**
     * Devuelve una matriz de 'rows' filas y 'cols' columnas con números
     * aleatorios uniformes entre 0 y q-1. Se usa para generar A.
     */
    private int[][] randomMatrix(int rows, int cols) {
        int[][] res = new int[rows][];
        for(int i = 0; i < rows; i++) {
            res[i] = randomVector(cols);
        }
        return res;
    }

    /**
     * Devuelve un vector de 'length' errores pequeños, cada uno entre
     * -errorBound y errorBound, ya reducidos módulo q (entre 0 y q-1).
     */
    private int[] errorVector(int length) {
        int[] res = new int[length];
        for(int i = 0; i < length; i++) {
            res[i] = mod.reduce(rng.nextInt(-errorBound, errorBound + 1));
        }
        return res;
    }

    /**
     * Genera un par de claves:
     *   s = vector secreto de n elementos
     *   A = matriz pública de m filas y n columnas
     *   e = vector de m errores pequeños
     *   b = A·s + e  (reduciendo cada posición módulo q)
     */
    public KeyPair keyGen() {
        int[] s = randomVector(n);
        SecretKey sk = new SecretKey(s);

        int[][] A = randomMatrix(m, n);
        int[] e = errorVector(m);
        int[] b = mod.addVectors(mod.matrixXVector(A, s), e);
        PublicKey pk = new PublicKey(A, b);

        return new KeyPair(pk, sk);
    }
}