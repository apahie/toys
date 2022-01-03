package calculator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static calculator.Ast.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InterpreterTest {
    private Interpreter interpreter = new Interpreter();
    @Test
    public void testFactorial() {
        List<Ast.TopLevel> topLevels = List.of(
                // define main() {
                //   fact(5);
                // }
                DefineFunction("main", List.of(),
                        Block(call("fact", integer(5)))
                ),
                // define factorial(n) {
                //   if(n < 2) {
                //     1;
                //   } else {
                //     n * fact(n - 1);
                //   }
                // }
                DefineFunction("fact", List.of("n"),
                        Block(
                                If(
                                        lessThan(identifier("n"), integer(2)),
                                        integer(1),
                                        Optional.of(
                                                multiply(
                                                        identifier("n"),
                                                        call("fact",
                                                                subtract(identifier("n"), integer(1))
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
        int result = interpreter.callMain(new Ast.Program(topLevels));
        assertEquals(120, result);
    }
}
