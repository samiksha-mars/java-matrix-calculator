# Java Matrix Calculator

A small, dependency-free matrix library in plain Java, with a console demo that walks
through every operation and its error cases.

## Features

- Immutable `Matrix` class backed by `double[][]` (inputs and outputs are defensively copied)
- Add, subtract, scalar multiply and matrix multiply
- Transpose
- Determinant (Gaussian elimination with partial pivoting)
- Inverse (Gauss-Jordan elimination with partial pivoting)
- `Matrix.identity(n)`, `Matrix.of(...)` factory, `approxEquals(other, tolerance)`
- Clear errors:
  - `DimensionMismatchException`: incompatible shapes, or determinant/inverse of a non-square matrix
  - `SingularMatrixException`: inverting a matrix whose determinant is 0

## Tech Stack

- Java 21 (compiled with `--release 21`, runs on 21+)
- Maven (via the bundled wrapper)
- JUnit 5

## Usage

Compile, then run the demo from `target/classes`:

```bash
./mvnw -q compile
java -cp target/classes com.samiksha.matrix.MatrixDemo
```

### Using the library

```java
Matrix a = Matrix.of(
        new double[] {4, 7},
        new double[] {2, 6});

double det = a.determinant();            // 10.0
Matrix inv = a.inverse();                // [[0.6, -0.7], [-0.2, 0.4]]
Matrix id  = a.multiply(inv);            // identity (within floating-point tolerance)
Matrix t   = a.transpose();

Matrix.of(new double[] {1, 2}, new double[] {2, 4}).inverse();  // throws SingularMatrixException
```

### Example demo output (excerpt)

```text
det(A) = 9.000

inverse(A) =
[     1.444   -1.222   -0.556
     -0.778    0.889    0.222
      0.333   -0.667    0.333 ]

Error handling:
  A + W: DimensionMismatchException - Cannot add 3x3 and 2x3: shapes must match
  W x W: DimensionMismatchException - Cannot multiply 2x3 by 2x3: left columns (3) must equal right rows (2)
  det(W): DimensionMismatchException - Cannot compute determinant of non-square 2x3 matrix
  inverse([[1,2],[2,4]]): SingularMatrixException - Matrix is singular (determinant is 0) and has no inverse
```

## Project Layout

```text
src/main/java/com/samiksha/matrix/
  Matrix.java                       the matrix type and all operations
  MatrixDemo.java                   console demo (main class)
  MatrixException.java              base error
  DimensionMismatchException.java
  SingularMatrixException.java
src/test/java/com/samiksha/matrix/
  MatrixTest.java, MatrixDemoTest.java
```

## Build & Test

```bash
./mvnw test          # compile and run the JUnit 5 suite
./mvnw -q package    # build target/java-matrix-calculator-0.0.1-SNAPSHOT.jar
```
