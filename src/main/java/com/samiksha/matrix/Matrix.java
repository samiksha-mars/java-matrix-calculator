package com.samiksha.matrix;

import java.util.Arrays;

/** An immutable, dense matrix of doubles. */
public final class Matrix {

    /** Values whose magnitude is below this are treated as zero when pivoting. */
    public static final double EPSILON = 1e-10;

    private final int rows;
    private final int cols;
    private final double[][] data;

    /** Creates a matrix from row arrays; the input is copied. */
    public Matrix(double[][] values) {
        if (values == null || values.length == 0 || values[0] == null || values[0].length == 0) {
            throw new IllegalArgumentException("Matrix must have at least one row and one column");
        }
        this.rows = values.length;
        this.cols = values[0].length;
        this.data = new double[rows][];
        for (int r = 0; r < rows; r++) {
            if (values[r] == null || values[r].length != cols) {
                throw new IllegalArgumentException("All rows must have " + cols + " columns (row " + r + " differs)");
            }
            double[] row = values[r].clone();
            for (int c = 0; c < cols; c++) {
                row[c] += 0.0; // -0.0 + 0.0 == +0.0, so equals/hashCode treat all zeros alike
            }
            this.data[r] = row;
        }
    }

    /** Convenience factory: {@code Matrix.of(new double[]{1, 2}, new double[]{3, 4})}. */
    public static Matrix of(double[]... rows) {
        return new Matrix(rows);
    }

    public static Matrix identity(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Size must be positive: " + n);
        }
        double[][] values = new double[n][n];
        for (int i = 0; i < n; i++) {
            values[i][i] = 1;
        }
        return new Matrix(values);
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public boolean isSquare() {
        return rows == cols;
    }

    public double get(int row, int col) {
        return data[row][col];
    }

    public double[][] toArray() {
        double[][] copy = new double[rows][];
        for (int r = 0; r < rows; r++) {
            copy[r] = data[r].clone();
        }
        return copy;
    }

    public Matrix add(Matrix other) {
        requireSameShape(other, "add");
        double[][] result = new double[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                result[r][c] = data[r][c] + other.data[r][c];
            }
        }
        return new Matrix(result);
    }

    public Matrix subtract(Matrix other) {
        requireSameShape(other, "subtract");
        double[][] result = new double[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                result[r][c] = data[r][c] - other.data[r][c];
            }
        }
        return new Matrix(result);
    }

    public Matrix multiply(double scalar) {
        double[][] result = new double[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                result[r][c] = data[r][c] * scalar;
            }
        }
        return new Matrix(result);
    }

    public Matrix multiply(Matrix other) {
        if (cols != other.rows) {
            throw new DimensionMismatchException("Cannot multiply " + shape() + " by " + other.shape()
                    + ": left columns (" + cols + ") must equal right rows (" + other.rows + ")");
        }
        double[][] result = new double[rows][other.cols];
        for (int r = 0; r < rows; r++) {
            for (int k = 0; k < cols; k++) {
                double value = data[r][k];
                for (int c = 0; c < other.cols; c++) {
                    result[r][c] += value * other.data[k][c];
                }
            }
        }
        return new Matrix(result);
    }

    public Matrix transpose() {
        double[][] result = new double[cols][rows];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                result[c][r] = data[r][c];
            }
        }
        return new Matrix(result);
    }

    /** Determinant via Gaussian elimination with partial pivoting. */
    public double determinant() {
        requireSquare("determinant");
        double[][] a = toArray();
        double det = 1;
        for (int col = 0; col < rows; col++) {
            int pivot = pivotRow(a, col);
            if (Math.abs(a[pivot][col]) < EPSILON) {
                return 0;
            }
            if (pivot != col) {
                swap(a, pivot, col);
                det = -det;
            }
            det *= a[col][col];
            for (int r = col + 1; r < rows; r++) {
                double factor = a[r][col] / a[col][col];
                for (int c = col; c < rows; c++) {
                    a[r][c] -= factor * a[col][c];
                }
            }
        }
        return det;
    }

    /** Inverse via Gauss-Jordan elimination on {@code [A | I]}. */
    public Matrix inverse() {
        requireSquare("inverse");
        int n = rows;
        double[][] a = toArray();
        double[][] inv = identity(n).toArray();
        for (int col = 0; col < n; col++) {
            int pivot = pivotRow(a, col);
            if (Math.abs(a[pivot][col]) < EPSILON) {
                throw new SingularMatrixException();
            }
            swap(a, pivot, col);
            swap(inv, pivot, col);

            double scale = a[col][col];
            for (int c = 0; c < n; c++) {
                a[col][c] /= scale;
                inv[col][c] /= scale;
            }
            for (int r = 0; r < n; r++) {
                if (r == col) {
                    continue;
                }
                double factor = a[r][col];
                if (factor == 0) {
                    continue;
                }
                for (int c = 0; c < n; c++) {
                    a[r][c] -= factor * a[col][c];
                    inv[r][c] -= factor * inv[col][c];
                }
            }
        }
        return new Matrix(inv);
    }

    /** Element-wise comparison within {@code tolerance}. */
    public boolean approxEquals(Matrix other, double tolerance) {
        if (other == null || rows != other.rows || cols != other.cols) {
            return false;
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (Math.abs(data[r][c] - other.data[r][c]) > tolerance) {
                    return false;
                }
            }
        }
        return true;
    }

    public String shape() {
        return rows + "x" + cols;
    }

    private int pivotRow(double[][] a, int col) {
        int best = col;
        for (int r = col + 1; r < a.length; r++) {
            if (Math.abs(a[r][col]) > Math.abs(a[best][col])) {
                best = r;
            }
        }
        return best;
    }

    private static void swap(double[][] a, int i, int j) {
        if (i != j) {
            double[] tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
    }

    private void requireSameShape(Matrix other, String operation) {
        if (rows != other.rows || cols != other.cols) {
            throw new DimensionMismatchException("Cannot " + operation + " " + shape() + " and " + other.shape()
                    + ": shapes must match");
        }
    }

    private void requireSquare(String operation) {
        if (!isSquare()) {
            throw new DimensionMismatchException("Cannot compute " + operation + " of non-square " + shape() + " matrix");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Matrix m && rows == m.rows && cols == m.cols && Arrays.deepEquals(data, m.data);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            sb.append(r == 0 ? "[ " : "  ");
            for (int c = 0; c < cols; c++) {
                double v = data[r][c];
                sb.append(String.format("%9.3f", Math.abs(v) < EPSILON ? 0.0 : v)); // avoid printing -0.000
            }
            sb.append(r == rows - 1 ? " ]" : "\n");
        }
        return sb.toString();
    }
}
