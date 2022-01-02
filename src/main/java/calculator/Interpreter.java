package calculator;

import java.util.HashMap;
import java.util.Map;

public class Interpreter {
    public final Map<String, Integer> environment;
    public Interpreter() {
        this.environment = new HashMap<>();
    }
    public int interpret(Ast.Expression expression) {
        if (expression instanceof Ast.BinaryExpression binaryExpression) {
            var lhs = interpret(binaryExpression.lhs());
            var rhs = interpret(binaryExpression.rhs());
            return switch (binaryExpression.operator()) {
                case ADD -> lhs + rhs;
                case SUBTRACT -> lhs - rhs;
                case MULTIPLY -> lhs * rhs;
                case DIVIDE -> lhs / rhs;
            };
        } else if (expression instanceof Ast.IntegerLiteral integer) {
            return integer.value();
        } else if (expression instanceof Ast.Identifier e) {
            return environment.get(e.name());
        } else if (expression instanceof Ast.Assignment e) {
            int value = interpret(e.expression());
            environment.put(e.name(), value);
            return value;
        } else {
            throw new RuntimeException("not reach here");
        }
    }
}
