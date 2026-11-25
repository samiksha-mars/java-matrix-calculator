package com.samiksha.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MatrixTest {

    private static final double TOL = 1e-9;

    private static Matrix m(double[]... rows) {
        return Matrix.of(rows);
    }

    private static double[] r(double... values) {
        return values;
    }

    @Nested
    class Construction {

        @Test
        void rejectsEmptyAndRaggedInput() {
            assertThrows(IllegalArgumentException.class, () -> new Matrix(new double[0][0]));
            assertThrows(IllegalArgumentException.class, () -> m(r(1, 2), r(3)));
        }

        @Test
        void copiesInputSoMatrixIsImmutable() {
            double[][] raw = {{1, 2}, {3, 4}};
            Matrix matrix = new Matrix(raw);
            raw[0][0] = 99;
            matrix.toArray()[0][1] = 99;
            assertEquals(1, matrix.get(0, 0));
            assertEquals(2, matrix.get(0, 1));
        }

        @Test
        void identityHasOnesOnDiagonal() {
            assertEquals(m(r(1, 0, 0), r(0, 1, 0), r(0, 0, 1)), Matrix.identity(3));
            assertThrows(IllegalArgumentException.class, () -> Matrix.identity(0));
        }
    }

    @Nested
    class Arithmetic {

        @Test
        void addAndSubtract() {
            Matrix a = m(r(1, 2), r(3, 4));
            Matrix b = m(r(5, 6), r(7, 8));
            assertEquals(m(r(6, 8), r(10, 12)), a.add(b));
            assertEquals(m(r(-4, -4), r(-4, -4)), a.subtract(b));
        }

        @Test
        void addRejectsShapeMismatch() {
            Matrix a = m(r(1, 2), r(3, 4));
            Matrix b = m(r(1, 2, 3));
            DimensionMismatchException e = assertThrows(DimensionMismatchException.class, () -> a.add(b));
            assertTrue(e.getMessage().contains("2x2"));
            assertTrue(e.getMessage().contains("1x3"));
            assertThrows(DimensionMismatchException.class, () -> a.subtract(b));
        }

        @Test
        void multiplyNonSquareMatrices() {
            Matrix a = m(r(1, 2, 3), r(4, 5, 6));
            Matrix b = m(r(7, 8), r(9, 10), r(11, 12));
            assertEquals(m(r(58, 64), r(139, 154)), a.multiply(b));
        }

        @Test
        void multiplyRejectsIncompatibleShapes() {
            Matrix a = m(r(1, 2, 3), r(4, 5, 6));
            assertThrows(DimensionMismatchException.class, () -> a.multiply(a));
        }

        @Test
        void multiplyByIdentityIsNoOp() {
            Matrix a = m(r(2, -1), r(0.5, 3));
            assertEquals(a, a.multiply(Matrix.identity(2)));
        }

        @Test
        void scalarMultiply() {
            assertEquals(m(r(2, 4), r(-6, 0)), m(r(1, 2), r(-3, 0)).multiply(2));
        }

        @Test
        void transposeSwapsRowsAndColumns() {
            Matrix a = m(r(1, 2, 3), r(4, 5, 6));
            Matrix t = a.transpose();
            assertEquals(3, t.rows());
            assertEquals(2, t.cols());
            assertEquals(m(r(1, 4), r(2, 5), r(3, 6)), t);
            assertEquals(a, t.transpose());
        }
    }

    @Nested
    class Determinant {

        @Test
        void oneByOneAndTwoByTwo() {
            assertEquals(7, m(r(7)).determinant(), TOL);
            assertEquals(-2, m(r(1, 2), r(3, 4)).determinant(), TOL);
        }

        @Test
        void threeByThree() {
            assertEquals(-306, m(r(6, 1, 1), r(4, -2, 5), r(2, 8, 7)).determinant(), TOL);
        }

        @Test
        void requiresRowSwapWhenLeadingEntryIsZero() {
            assertEquals(-1, m(r(0, 1), r(1, 0)).determinant(), TOL);
        }

        @Test
        void singularMatrixHasZeroDeterminant() {
            assertEquals(0, m(r(1, 2, 3), r(4, 5, 6), r(7, 8, 9)).determinant(), 1e-9);
        }

        @Test
        void nonSquareIsRejected() {
            assertThrows(DimensionMismatchException.class, () -> m(r(1, 2, 3), r(4, 5, 6)).determinant());
        }
    }

    @Nested
    class Inverse {

        @Test
        void twoByTwoInverse() {
            Matrix inv = m(r(4, 7), r(2, 6)).inverse();
            assertTrue(inv.approxEquals(m(r(0.6, -0.7), r(-0.2, 0.4)), TOL), inv::toString);
        }

        @Test
        void productWithInverseIsIdentity() {
            Matrix a = m(r(4, 7, 2), r(3, 6, 1), r(2, 5, 3));
            assertTrue(a.multiply(a.inverse()).approxEquals(Matrix.identity(3), TOL));
            assertTrue(a.inverse().multiply(a).approxEquals(Matrix.identity(3), TOL));
        }

        @Test
        void inverseWithZeroPivotNeedsRowSwap() {
            Matrix a = m(r(0, 1), r(1, 0));
            assertEquals(a, a.inverse());
        }

        @Test
        void singularMatrixThrows() {
            assertThrows(SingularMatrixException.class, () -> m(r(1, 2), r(2, 4)).inverse());
            assertThrows(SingularMatrixException.class, () -> m(r(0, 0), r(0, 0)).inverse());
        }

        @Test
        void nonSquareThrowsDimensionMismatch() {
            assertThrows(DimensionMismatchException.class, () -> m(r(1, 2, 3)).inverse());
        }
    }

    @Nested
    class Equality {

        @Test
        void approxEqualsRespectsTolerance() {
            Matrix a = m(r(1, 2));
            assertTrue(a.approxEquals(m(r(1.0000001, 2)), 1e-6));
            assertFalse(a.approxEquals(m(r(1.1, 2)), 1e-6));
            assertFalse(a.approxEquals(m(r(1), r(2)), 1e-6));
        }

        @Test
        void equalsAndHashCode() {
            assertEquals(m(r(1, 2)), m(r(1, 2)));
            assertEquals(m(r(1, 2)).hashCode(), m(r(1, 2)).hashCode());
            assertNotEquals(m(r(1, 2)), m(r(2, 1)));
        }

        @Test
        void negativeZeroEqualsZero() {
            Matrix negated = m(r(0, 2)).multiply(-1);
            assertEquals(m(r(0, -2)), negated);
            assertEquals(m(r(0, -2)).hashCode(), negated.hashCode());
            assertEquals(0.0, negated.get(0, 0));
        }
    }
}
