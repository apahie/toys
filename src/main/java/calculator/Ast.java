package calculator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Ast {
    public static BinaryExpression add(Expression lhs, Expression rhs) {
        return new BinaryExpression(Operator.ADD, lhs, rhs);
    }
    public static BinaryExpression subtract(Expression lhs, Expression rhs) {
        return new BinaryExpression(Operator.SUBTRACT, lhs, rhs);
    }
    public static BinaryExpression multiply(Expression lhs, Expression rhs) {
        return new BinaryExpression(Operator.MULTIPLY, lhs, rhs);
    }
    public static BinaryExpression divide(Expression lhs, Expression rhs) {
        return new BinaryExpression(Operator.DIVIDE, lhs, rhs);
    }
    public static IntegerLiteral integer(int value) {
        return new IntegerLiteral(value);
    }

    public static Identifier identifier(String name) {
        return new Identifier(name);
    }
    public static Assignment assignment(String name, Expression expression) {
        return new Assignment(name, expression);
    }

    public static BlockExpression Block(Expression... elements) {
        return new BlockExpression(Arrays.asList(elements));
    }

    public static WhileExpression While(Expression condition, Expression body) {
        return new WhileExpression(condition, body);
    }
    public static IfExpression If(Expression condition, Expression thenClause, Optional<Expression> elseClause) {
        return new IfExpression(condition, thenClause, elseClause);
    }
    public static IfExpression If(Expression condition, Expression thenClause) {
        return If(condition, thenClause, Optional.empty());
    }

    public static BinaryExpression lessThan(Expression lhs, Expression rhs) {
        return new BinaryExpression(Operator.LESS_THAN, lhs, rhs);
    }
    public static BinaryExpression lessOrEqual(Expression lhs, Expression rhs) {
        return new BinaryExpression(Operator.LESS_OR_EQUAL, lhs, rhs);
    }
    public static BinaryExpression greaterThan(Expression lhs, Expression rhs) {
        return new BinaryExpression(Operator.GREATER_THAN, lhs, rhs);
    }
    public static BinaryExpression greaterOrEqual(Expression lhs, Expression rhs) {
        return new BinaryExpression(Operator.GREATER_OR_EQUAL, lhs, rhs);
    }
    public static BinaryExpression equalEqual(Expression lhs, Expression rhs) {
        return new BinaryExpression(Operator.EQUAL_EQUAL, lhs, rhs);
    }

    public static Println Println(Expression arg) {
        return new Println(arg);
    }

    public static FunctionDefinition DefineFunction(String name, List<String> args, Expression body) {
        return new FunctionDefinition(name, args, body);
    }
    public static FunctionCall call(String name, Expression... args) {
        return new FunctionCall(name, Arrays.asList(args));
    }

    sealed public interface Expression permits Assignment, BinaryExpression, BlockExpression, FunctionCall, Identifier, IfExpression, IntegerLiteral, LabelledCall, Println, WhileExpression {}

    public record BinaryExpression(Operator operator, Expression lhs, Expression rhs) implements Expression {}
    public record IntegerLiteral(int value) implements Expression {}

    public record Identifier(String name) implements Expression {}
    public record Assignment(String name, Expression expression) implements Expression {}

    public record BlockExpression(List<Expression> elements) implements Expression {}
    public record WhileExpression(Expression condition, Expression body) implements Expression {}
    public record IfExpression(Expression condition, Expression thenClause, Optional<Expression> elseClause)
        implements Expression {}
    public record Environment(Map<String, Integer> bindings, Optional<Environment> next) {
        public Optional<Map<String, Integer>> findBinding(String name) {
            if (bindings.get(name) != null) {
                return Optional.of(bindings);
            }
            if (next.isPresent()) {
                return next.get().findBinding(name);
            }
            return Optional.empty();
        }
    }

    sealed public interface TopLevel permits GlobalVariableDefinition, FunctionDefinition {}

    public record Program(List<TopLevel> definitions) {}

    public record Println(Expression arg) implements Expression {}

    public record GlobalVariableDefinition(String name, Expression expression) implements TopLevel {}
    public record FunctionDefinition(String name, List<String> args, Expression body) implements TopLevel {}
    public record FunctionCall(String name, List<Expression> args) implements Expression {}

    public record LabelledParameter(String name, Expression parameter) {}
    public record LabelledCall(String name, List<LabelledParameter> args) implements Expression {}
}
