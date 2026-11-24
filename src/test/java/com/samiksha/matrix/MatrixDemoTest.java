package com.samiksha.matrix;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MatrixDemoTest {

    @Test
    void demoRunsAndReportsErrorCases() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        MatrixDemo.run(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        String output = buffer.toString(StandardCharsets.UTF_8);

        assertTrue(output.contains("det(A) = 9.000"));
        assertTrue(output.contains("DimensionMismatchException"));
        assertTrue(output.contains("SingularMatrixException"));
    }
}
