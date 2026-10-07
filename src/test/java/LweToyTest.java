import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LweToyTest {

    private static final int Q = 97;
    private static final int N = 4;
    private static final int M = 20;
    private static final int ERROR_BOUND = 1;

    private LweToy lwe;
    private Modular mod;

    @BeforeEach
    void setUp() {
        lwe = new LweToy(Q, N, M, ERROR_BOUND);
        mod = new Modular(Q);
    }

    // ---------- Constructor ----------

    @Test
    void constructorRejectsInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> new LweToy(1, N, M, ERROR_BOUND));  // q < 2
        assertThrows(IllegalArgumentException.class, () -> new LweToy(Q, 0, M, ERROR_BOUND));  // n < 1
        assertThrows(IllegalArgumentException.class, () -> new LweToy(Q, N, 0, ERROR_BOUND));  // m < 1
        assertThrows(IllegalArgumentException.class, () -> new LweToy(Q, N, M, -1));          // errorBound < 0
    }

    @Test
    void constructorAcceptsZeroErrorBound() {
        assertDoesNotThrow(() -> new LweToy(Q, N, M, 0));
    }

    // ---------- keyGen: estructura ----------

    @Test
    void keyGenHasCorrectDimensions() {
        LweToy.KeyPair kp = lwe.keyGen();
        int[] s = kp.sk().s();
        int[][] A = kp.pk().A();
        int[] b = kp.pk().b();

        assertEquals(N, s.length, "s debe tener n elementos");
        assertEquals(M, A.length, "A debe tener m filas");
        for (int i = 0; i < M; i++) {
            assertEquals(N, A[i].length, "la fila " + i + " de A debe tener n columnas");
        }
        assertEquals(M, b.length, "b debe tener m elementos");
    }

    @Test
    void keyGenValuesAreInRange() {
        LweToy.KeyPair kp = lwe.keyGen();

        assertAllInRange(kp.sk().s(), "s");
        for (int[] row : kp.pk().A()) {
            assertAllInRange(row, "A");
        }
        assertAllInRange(kp.pk().b(), "b");
    }

    // ---------- keyGen: la relación b = A·s + e ----------

    @Test
    void publicKeyIsAsPlusSmallError() {
        LweToy.KeyPair kp = lwe.keyGen();
        int[] As = mod.matrixXVector(kp.pk().A(), kp.sk().s());
        int[] b = kp.pk().b();

        for (int i = 0; i < M; i++) {
            int error = mod.center(b[i] - As[i]);
            assertTrue(error >= -ERROR_BOUND && error <= ERROR_BOUND,
                    "error en la fila " + i + " fuera de rango: " + error);
        }
    }

    @Test
    void zeroErrorBoundGivesExactSystem() {
        LweToy noError = new LweToy(Q, N, M, 0);
        LweToy.KeyPair kp = noError.keyGen();
        int[] As = mod.matrixXVector(kp.pk().A(), kp.sk().s());

        assertArrayEquals(As, kp.pk().b(), "sin error, b debe ser exactamente A·s");
    }

    @Test
    void errorDistributionIsUniformAndSymmetric() {
        // Contamos cuántas veces sale cada error en muchas claves.
        // Índice 0 -> error -1, índice 1 -> error 0, índice 2 -> error +1
        int keyPairs = 1000;
        int[] counts = new int[2 * ERROR_BOUND + 1];

        for (int k = 0; k < keyPairs; k++) {
            LweToy.KeyPair kp = lwe.keyGen();
            int[] As = mod.matrixXVector(kp.pk().A(), kp.sk().s());
            int[] b = kp.pk().b();
            for (int i = 0; i < M; i++) {
                int error = mod.center(b[i] - As[i]);
                counts[error + ERROR_BOUND]++;
            }
        }

        int total = keyPairs * M;
        for (int i = 0; i < counts.length; i++) {
            double fraction = (double) counts[i] / total;
            int errorValue = i - ERROR_BOUND;
            assertTrue(fraction > 0.30 && fraction < 0.37,
                    "el error " + errorValue + " aparece en un " + (fraction * 100) + " % de los casos");
        }
    }

    @Test
    void keyGenProducesDifferentKeysEachTime() {
        // No es una prueba de seguridad, solo detecta errores graves
        // (por ejemplo, un generador que devuelva siempre lo mismo).
        int[] s1 = lwe.keyGen().sk().s();
        int[] s2 = lwe.keyGen().sk().s();
        int[] s3 = lwe.keyGen().sk().s();

        boolean allEqual = java.util.Arrays.equals(s1, s2) && java.util.Arrays.equals(s2, s3);
        assertFalse(allEqual, "tres claves secretas seguidas son idénticas");
    }

    // ---------- Utilidades ----------

    private void assertAllInRange(int[] v, String name) {
        for (int i = 0; i < v.length; i++) {
            assertTrue(v[i] >= 0 && v[i] < Q,
                    name + "[" + i + "] = " + v[i] + " fuera del rango [0, " + (Q - 1) + "]");
        }
    }
}