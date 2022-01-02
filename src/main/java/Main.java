import calculator.Interpreter;

import static calculator.Ast.*;

public class Main {
    public static void main(String[] args) {
        // case1
        var e1 = add(
                subtract(
                        integer(1),
                        multiply(
                                integer(2),
                                integer(3)
                        )
                ),
                integer(4)
        );
        var interpreter = new Interpreter();

        System.out.println(interpreter.interpret(e1));
    }
}
