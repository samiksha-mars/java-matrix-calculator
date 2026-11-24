package com.samiksha.matrix;

import java.io.PrintStream;

/** Small console walkthrough of every {@link Matrix} operation, including the error cases. */
public final class MatrixDemo {

    private MatrixDemo() {
    }

    public static void main(String[] args) {
        run(System.out);
    }

    static void run(PrintStream out) {
        Matrix a = Matrix.of(
                new double[] {4, 7, 2},
                new double[] {3, 6, 1},
                new double[] {2, 5, 3});
        Matrix b = Matrix.of(
                new double[] {1, 0, 2},
                new double[] {0, 1, 0},
                new double[] {3, 0, 1});
        Matrix wide = Matrix.of(
                new double[] {1, 2, 3},
                new double[] {4, 5, 6});
        Matrix singular = Matrix.of(
                new double[] {1, 2},
                new double[] {2, 4});

        section(out, "A", a);
        section(out, "B", b);
        section(out, "A + B", a.add(b));
        section(out, "A - B", a.subtract(b));
        section(out, "A x B", a.multiply(b));
        section(out, "2 x A", a.multiply(2));
        section(out, "W (2x3)", wide);
        section(out, "transpose(W)", wide.transpose());
        out.printf("det(A) = %.3f%n%n", a.determinant());
        Matrix inverse = a.inverse();
        section(out, "inverse(A)", inverse);
        section(out, "A x inverse(A)", a.multiply(inverse));

        out.println("Error handling:");
        attempt(out, "A + W", () -> a.add(wide));
        attempt(out, "W x W", () -> wide.multiply(wide));
        attempt(out, "det(W)", wide::determinant);
        attempt(out, "inverse([[1,2],[2,4]])", singular::inverse);
    }

    private static void section(PrintStream out, String title, Matrix m) {
        out.println(title + " =");
        out.println(m);
        out.println();
    }

    private static void attempt(PrintStream out, String label, Runnable op) {
        try {
            op.run();
            out.println("  " + label + ": ok");
        } catch (MatrixException e) {
            out.println("  " + label + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
