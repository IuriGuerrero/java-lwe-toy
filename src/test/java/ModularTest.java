import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModularTest {

    private static final int Q = 97;
    private Modular mod;

    @BeforeEach
    void setUp() {
        mod = new Modular(Q);
    }

    // ---------- Constructor ----------

    @Test
    void constructorRejectsInvalidQ() {
        assertThrows(IllegalArgumentException.class, () -> new Modular(1));
        assertThrows(IllegalArgumentException.class, () -> new Modular(0));
        assertThrows(IllegalArgumentException.class, () -> new Modular(-5));
    }

    @Test
    void constructorAcceptsMinimumQ() {
        assertDoesNotThrow(() -> new Modular(2));
    }

    // ---------- reduce ----------

    @Test
    void reduceKeepsValuesInRange() {
        assertEquals(0, mod.reduce(0));
        assertEquals(5, mod.reduce(5));
        assertEquals(96, mod.reduce(96));
    }

    @Test
    void reduceWrapsAround() {
        assertEquals(0, mod.reduce(97));
        assertEquals(3, mod.reduce(100));
        assertEquals(0, mod.reduce(194));
    }

    @Test
    void reduceHandlesNegatives() {
        assertEquals(96, mod.reduce(-1));
        assertEquals(95, mod.reduce(-2));
        assertEquals(0, mod.reduce(-97));
    }

    @Test
    void reduceHandlesLargeLongValues() {
        long big = 97L * 1_000_000_000L + 7;
        assertEquals(7, mod.reduce(big));
    }

    // ---------- center ----------

    @Test
    void centerBoundaryValues() {
        // Los dos valores a cada lado del límite q/2 = 48
        assertEquals(48, mod.center(48));
        assertEquals(-48, mod.center(49));
    }

    @Test
    void centerTypicalValues() {
        assertEquals(0, mod.center(0));
        assertEquals(1, mod.center(1));
        assertEquals(-1, mod.center(96));
    }

    @Test
    void centerAcceptsUnreducedInput() {
        assertEquals(-1, mod.center(-1));
        assertEquals(3, mod.center(100));
    }

    @Test
    void centerPropertiesHoldForManyValues() {
        // Propiedades que deben cumplirse para cualquier entrada:
        // 1) el resultado está en el rango simétrico [-48, 48]
        // 2) el resultado es equivalente a la entrada módulo q
        for (int x = -1000; x <= 1000; x++) {
            int c = mod.center(x);
            assertTrue(c >= -Q / 2 && c <= Q / 2, "center(" + x + ") = " + c + " fuera de rango");
            assertEquals(mod.reduce(x), mod.reduce(c), "center(" + x + ") no es equivalente módulo q");
        }
    }

    // ---------- scalarProduct ----------

    @Test
    void scalarProductBasic() {
        assertEquals(32, mod.scalarProduct(new int[]{1, 2, 3}, new int[]{4, 5, 6}));
    }

    @Test
    void scalarProductWrapsAround() {
        assertEquals(53, mod.scalarProduct(new int[]{50, 50}, new int[]{2, 1}));
    }

    @Test
    void scalarProductEmptyVectorsIsZero() {
        assertEquals(0, mod.scalarProduct(new int[]{}, new int[]{}));
    }

    @Test
    void scalarProductRejectsDifferentLengths() {
        assertThrows(IllegalArgumentException.class,
                () -> mod.scalarProduct(new int[]{1, 2}, new int[]{1, 2, 3}));
        assertThrows(IllegalArgumentException.class,
                () -> mod.scalarProduct(new int[]{1, 2, 3}, new int[]{1, 2}));
    }

    // ---------- matrixXVector ----------

    @Test
    void matrixXVectorNonSquare() {
        // Matriz 3x2: el resultado debe tener 3 elementos (uno por fila)
        int[][] A = {{1, 2}, {3, 4}, {5, 6}};
        int[] v = {1, 1};
        assertArrayEquals(new int[]{3, 7, 11}, mod.matrixXVector(A, v));
    }

    @Test
    void matrixXVectorWrapsAround() {
        int[][] A = {{50, 50}, {96, 0}};
        int[] v = {2, 1};
        assertArrayEquals(new int[]{53, 95}, mod.matrixXVector(A, v));
    }

    @Test
    void matrixXVectorRejectsWrongColumnCount() {
        int[][] A = {{1, 2, 3}, {4, 5, 6}};
        int[] v = {1, 1};
        assertThrows(IllegalArgumentException.class, () -> mod.matrixXVector(A, v));
    }
}