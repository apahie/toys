package calculator;

import org.javafp.parsecj.input.Input;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParsersTest {
    private Interpreter interpreter = new Interpreter();
    @Test
    public void testWhileExpression() throws Exception {
        var statements = Parsers.lines().parse(Input.of("""
                i = 0;
                while(i < 10) {
                  i = i + 1;
                }""")).getResult();
        for (var statement : statements) {
            interpreter.interpret(statement);
        }
        assertEquals(10, interpreter.getValue("i"));
    }

    @Test
    public void testFactorial() throws Exception {
        var program = Parsers.program().parse(Input.of("""
                define factorial(n) {
                  if(n < 2) {
                    1;
                  } else {
                    n * factorial(n - 1);
                  }
                }
                define main() {
                  factorial(5);
                }""")).getResult();
        assertEquals(120, interpreter.callMain(program));
    }

    @Test
    public void testForInExpression() throws Exception {
        var statements = Parsers.lines().parse(Input.of("""
                for(i in 1 to 10) {
                  i = i + 1;
                }""")).getResult();
        for (var statement : statements) {
            interpreter.interpret(statement);
        }
        assertEquals(11, interpreter.getValue("i"));
    }

    @Test
    public void testLabelledCall() throws Exception {
        var program = Parsers.program().parse(Input.of("""
                define power(n) {
                  n * n;
                }
                define main() {
                  power[n = 5];
                }""")).getResult();
        var result = interpreter.callMain(program);
        assertEquals(25, result);
    }
}
