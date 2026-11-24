package com.samiksha.matrix;

/** Thrown when inverting a matrix whose determinant is (numerically) zero. */
public class SingularMatrixException extends MatrixException {
    public SingularMatrixException() {
        super("Matrix is singular (determinant is 0) and has no inverse");
    }
}
