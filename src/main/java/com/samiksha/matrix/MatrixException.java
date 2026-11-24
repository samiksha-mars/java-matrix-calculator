package com.samiksha.matrix;

/** Base type for matrix operation errors. */
public class MatrixException extends RuntimeException {
    public MatrixException(String message) {
        super(message);
    }
}
