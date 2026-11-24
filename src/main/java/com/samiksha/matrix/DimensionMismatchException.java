package com.samiksha.matrix;

/** Thrown when an operation is applied to matrices of incompatible shapes. */
public class DimensionMismatchException extends MatrixException {
    public DimensionMismatchException(String message) {
        super(message);
    }
}
