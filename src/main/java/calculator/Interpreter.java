package calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Interpreter {
    public Ast.Environment variableEnvironment;
    public final Map<String, Ast.FunctionDefinition> functionEnvironment;
    public Interpreter() {
        this.variableEnvironment = newEnvironment(Optional.empty());
        this.functionEnvironment = new HashMap<>();
    }

    private static Ast.Environment newEnvironment(Optional<Ast.Environment> next) {
        return new Ast.Environment(new HashMap<>(), next);
    }

    public Integer getValue(String name) {
        return variableEnvironment.bindings().get(name);
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
                case LESS_THAN -> lhs < rhs ? 1 : 0;
                case LESS_OR_EQUAL -> lhs <= rhs ? 1 : 0;
                case GREATER_THAN -> lhs > rhs ? 1 : 0;
                case GREATER_OR_EQUAL -> lhs >= rhs ? 1 : 0;
                case EQUAL_EQUAL -> lhs == rhs ? 1 : 0;
                case NOT_EQUAL -> lhs != rhs ? 1 : 0;
            };
        } else if (expression instanceof Ast.IntegerLiteral integer) {
            return integer.value();
        } else if (expression instanceof Ast.Identifier identifier) {
            var bindingsOpt = variableEnvironment.findBinding(identifier.name());
            return bindingsOpt.get().get(identifier.name());
        } else if (expression instanceof Ast.Assignment assignment) {
            int value = interpret(assignment.expression());
            variableEnvironment.bindings().put(assignment.name(), value);
            return value;
        } else if (expression instanceof Ast.IfExpression ifExpression) {
            int condition = interpret(ifExpression.condition());
            if (condition != 0) {
                return interpret(ifExpression.thenClause());
            } else {
                var elseClauseOpt = ifExpression.elseClause();
                return elseClauseOpt.map(this::interpret).orElse(1);
            }
        } else if (expression instanceof Ast.WhileExpression whileExpression) {
            while (true) {
                int condition = interpret(whileExpression.condition());
                if (condition != 0) {
                    interpret(whileExpression.body());
                } else {
                    break;
                }
            }
            return 1;
        } else if (expression instanceof Ast.BlockExpression block) {
            int value = 0;
            for (var e : block.elements()) {
                value = interpret(e);
            }
            return value;
        } else if (expression instanceof Ast.FunctionCall functionCall) {
            var definition = functionEnvironment.get(functionCall.name());
            if (definition == null) {
                throw new RuntimeException("Function " + functionCall.name() + " is not found");
            }

            var actualParams = functionCall.args();
            var formalParams = definition.args();
            var body = definition.body();
            var values = actualParams.stream()
                    .map(a -> interpret(a))
                    .collect(Collectors.toUnmodifiableList());
            var backup = variableEnvironment;
            variableEnvironment = newEnvironment(Optional.of(variableEnvironment));
            int i = 0;
            for (var formalParamName : formalParams) {
                variableEnvironment.bindings().put(formalParamName, values.get(i));
                i++;
            }
            var result = interpret(body);
            variableEnvironment = backup;
            return result;
        } else if (expression instanceof Ast.LabelledCall labelledCall) {
            var definition = functionEnvironment.get(labelledCall.name());
            if (definition == null) {
                throw new RuntimeException("Function " + labelledCall.name() + "  is not found");
            }
            var labels = labelledCall.args();
            var mapping = new HashMap<String, Ast.Expression>();
            for (var label : labels) {
                mapping.put(label.name(), label.parameter());
            }
            var formalParams = definition.args();
            var actualParams = new ArrayList<Ast.Expression>();
            for (var param : formalParams) {
                actualParams.add(mapping.get(param));
            }

            var body = definition.body();
            var values = actualParams.stream()
                    .map(a -> interpret(a))
                    .collect(Collectors.toList());
            var backup = variableEnvironment;
            variableEnvironment = newEnvironment(Optional.of(variableEnvironment));
            int i = 0;
            for (var formalParamName : formalParams) {
                variableEnvironment.bindings().put(formalParamName, values.get(i));
                i++;
            }
            var result = interpret(body);
            variableEnvironment = backup;
            return result;
        } else {
            throw new RuntimeException("not reach here");
        }
    }

    public int callMain(Ast.Program program) {
        var topLevels = program.definitions();
        for (var topLevel : topLevels) {
            if (topLevel instanceof Ast.FunctionDefinition functionDefinition) {
                functionEnvironment.put(functionDefinition.name(), functionDefinition);
            } else if (topLevel instanceof Ast.GlobalVariableDefinition globalVariableDefinition) {
                variableEnvironment.bindings().put(
                        globalVariableDefinition.name(),
                        interpret(globalVariableDefinition.expression())
                );
            } else {
                throw new RuntimeException("not reach here");
            }
        }
        var mainFunction = functionEnvironment.get("main");
        if (mainFunction != null) {
            return interpret(mainFunction.body());
        } else {
            throw new LanguageException("This program doesn't have main() function");
        }
    }
}
