package calculator;

import org.javafp.data.IList;
import org.javafp.data.Unit;
import org.javafp.parsecj.Combinators;
import org.javafp.parsecj.Parser;

import java.util.List;
import java.util.function.BinaryOperator;

import static org.javafp.parsecj.Text.*;
import static calculator.Ast.*;

public class Parsers {
    public static final Parser<Character, Unit> SPACING = wspace.map(__1 -> Unit.unit).
            or(regex("(?m)//.*$").map(__1 -> Unit.unit));
    public static final Parser<Character, Unit> SPACINGS = SPACING.many().map(__1 -> Unit.unit);
    public static final Parser<Character, Unit> IF = string("if").then(SPACINGS);
    public static final Parser<Character, Unit> ELSE = string("else").then(SPACINGS);
    public static final Parser<Character, Unit> WHILE = string("while").then(SPACINGS);
    public static final Parser<Character, Unit> PLUS = string("+").then(SPACINGS);
    public static final Parser<Character, Unit> MINUS = string("-").then(SPACINGS);
    public static final Parser<Character, Unit> ASTER = string("*").then(SPACINGS);
    public static final Parser<Character, Unit> SLASH = string("/").then(SPACINGS);
    public static final Parser<Character, Unit> LT = string("<").then(SPACINGS);
    public static final Parser<Character, Unit> LT_EQ = string("<=").then(SPACINGS);
    public static final Parser<Character, Unit> GT = string(">").then(SPACINGS);
    public static final Parser<Character, Unit> GT_EQ = string(">=").then(SPACINGS);
    public static final Parser<Character, Unit> EQEQ = string("==").then(SPACINGS);
    public static final Parser<Character, Unit> NOT_EQ = string("!=").then(SPACINGS);
    public static final Parser<Character, Unit> EQ = string("=").then(SPACINGS);
    public static final Parser<Character, Unit> GLOBAL = string("global").then(SPACINGS);
    public static final Parser<Character, Unit> DEFINE = string("define").then(SPACINGS);
    public static final Parser<Character, Unit> RETURN = string("return").then(SPACINGS);
    public static final Parser<Character, Unit> PRINTLN = string("println").then(SPACINGS);
    public static final Parser<Character, Unit> TRUE = string("true").then(SPACINGS);
    public static final Parser<Character, Unit> FALSE = string("false").then(SPACINGS);
    public static final Parser<Character, Unit> COMMA = string(",").then(SPACINGS);
    public static final Parser<Character, Unit> LPAREN = string("(").then(SPACINGS);
    public static final Parser<Character, Unit> RPAREN = string(")").then(SPACINGS);
    public static final Parser<Character, Unit> LBRACE = string("{").then(SPACINGS);
    public static final Parser<Character, Unit> RBRACE = string("}").then(SPACINGS);
    public static final Parser<Character, Unit> LBRACKET = string("[").then(SPACINGS);
    public static final Parser<Character, Unit> RBRACKET = string("]").then(SPACINGS);
    public static final Parser<Character, Unit> SEMI_COLON = string(";").then(SPACINGS);
    public static final Parser<Character, String> IDENT = regex("[a-zA-Z_][a-zA-Z0-9_]*").bind(name -> SPACINGS.map(__ -> name));
    public static final Parser<Character, Unit> FOR = string("for").then(SPACINGS);
    public static final Parser<Character, Unit> IN = string("in").then(SPACINGS);
    public static final Parser<Character, Unit> TO = string("to").then(SPACINGS);

    public static Parser<Character, Ast.IntegerLiteral> integer = intr.map(Ast::integer).bind(v -> SPACINGS.map(__ -> v));

    // program <- topLevelDefinition*;
    public static Parser<Character, Program> program() {
        return SPACINGS.bind(_1 -> topLevelDefinition().many().map(IList::toList).map(Program::new));
    }

    // lines <- line+ EOF;
    public static Parser<Character, List<Expression>> lines() {
        return line().many1().bind(s -> Combinators.<Character>eof().map(__ -> s.toList()));
    }

    // topLevelDefinition <- globalVariableDefinition / functionDefinition;
    public static Parser<Character, Ast.TopLevel> topLevelDefinition() {
        return globalVariableDefinition().map(g -> (TopLevel) g).or(functionDefinition().map(f -> (TopLevel) f));
    }

    // functionDefinition <-
    //     "define" identifier
    //     "(" (identifier ("," identifier)*)? ")"
    //     blockExpression;
    public static Parser<Character, FunctionDefinition> functionDefinition() {
        var defName = DEFINE.then(IDENT);
        var defArgs = IDENT.sepBy(COMMA).between(LPAREN, RPAREN);
        return defName.bind(name ->
                defArgs.bind(args ->
                        blockExpression().map(body -> new FunctionDefinition(name, args.toList(), body))
                )
        );
    }

    // globalVariableDefinition <- "global" identifier "=" expression;
    public static Parser<Character, GlobalVariableDefinition> globalVariableDefinition() {
        var defGlobal = GLOBAL.then(IDENT);
        var defInitializer = EQ.then(expression());
        return defGlobal.bind(name ->
                defInitializer.bind(expression ->
                        SEMI_COLON.map(_1 -> new GlobalVariableDefinition(name, expression))
                )
        );
    }

    // line <- println / whileExpression / ifExpression / assignment /expressionLine / blockExpression;
    public static Parser<Character, Expression> line() {
        return println().or(whileExpression())
                .or(ifExpression())
                .or(assignment())
                .or(expressionLine())
                .or(blockExpression())
                .or(forInExpression());
    }

    public static Parser<Character, Expression> println() {
        return PRINTLN.bind(_1 ->
                expression().between(LPAREN, RPAREN).bind(param ->
                        SEMI_COLON.map(_2 -> (Expression) new Println(param))
                )
        ).attempt();
    }

//    public static Parser<Character, Expression> println() {
//        return PRINTLN.bind(_1 ->
//                expression().between(LPAREN, RPAREN).bind(paaram ->
//                        SEMI_COLON.map(_2 -> (Expression) new Println(param))
//                )
//        );
//    }

    public static Parser<Character, Expression> ifExpression() {
        var condition = IF.then(expression().between(LPAREN, RPAREN));
        return condition.bind(c ->
                line().bind(thenCLause ->
                        ELSE.then(line()).optionalOpt().map(elseClauseOpt ->
                                (Expression) new IfExpression(c, thenCLause, elseClauseOpt)
                        )
                )
        ).attempt();
    }

    public static Parser<Character, Expression> whileExpression() {
        var condition = WHILE.then(expression().between(LPAREN, RPAREN));
        return condition.bind(c -> line().map(body -> (Expression) new WhileExpression(c, body))).attempt();
    }

    public static Parser<Character, Expression> blockExpression() {
        return LBRACE.bind(__ -> line().many().bind(expressions -> RBRACE.map(___ -> new BlockExpression(expressions.stream().toList()))));
    }

    public static Parser<Character, Expression> assignment() {
        return IDENT.bind(name ->
                EQ.then(expression().bind(e -> SEMI_COLON.map(__ -> (Expression) new Assignment(name, e))))
        ).attempt();
    }

    public static Parser<Character, Expression> expressionLine() {
        return expression().bind(e -> SEMI_COLON.map(__ -> e)).attempt();
    }

    public static Parser<Character, Expression> expression() {
        return comparative();
    }

    public static Parser<Character, Expression> comparative() {
        Parser<Character, BinaryOperator<Expression>> lt = LT.attempt().map(op -> Ast::lessThan);
        Parser<Character, BinaryOperator<Expression>> gt = GT.attempt().map(op -> Ast::greaterThan);
        Parser<Character, BinaryOperator<Expression>> lte = LT_EQ.attempt().map(op -> Ast::lessOrEqual);
        Parser<Character, BinaryOperator<Expression>> gte = GT_EQ.attempt().map(op -> Ast::greaterOrEqual);
        Parser<Character, BinaryOperator<Expression>> eq = EQEQ.attempt().map(op -> Ast::equalEqual);
        Parser<Character, BinaryOperator<Expression>> neq = NOT_EQ.attempt().map(op -> Ast::equalEqual);
        return additive().chainl1(lte.or(gte).or(neq).or(lt).or(gt).or(eq));
    }

    public static Parser<Character, Expression> additive() {
        Parser<Character, BinaryOperator<Expression>> add = PLUS.map(op -> Ast::add);
        Parser<Character, BinaryOperator<Expression>> sub = MINUS.map(op -> Ast::subtract);
        return multitive().chainl1(add.or(sub));
    }

    public static Parser<Character, Expression> multitive() {
        Parser<Character, BinaryOperator<Expression>> mul = ASTER.map(op -> Ast::multiply);
        Parser<Character, BinaryOperator<Expression>> div = SLASH.map(op -> Ast::divide);
        return primary().chainl1(mul.or(div));
    }

    // primary <- "(" expression ")"
    //         / integer
    //         / functionCall
    //         / labelledCall
    //         / identifier;
    public static Parser<Character, Expression> primary() {
        return LPAREN.bind(_1 -> expression().bind(v -> RPAREN.map(_2 -> v)))
                .or(integer)
                .or(functionCall())
                .or(labelledCall())
//                .or(arrayLiteral())
//                .or(boolLiteral())
                .or(identifier());
    }

    public static Parser<Character, FunctionCall> functionCall() {
        return IDENT.bind(name ->
                expression().sepBy(COMMA).between(LPAREN, RPAREN).map(params -> new FunctionCall(name, params.toList()))
        ).attempt();
    }

    // labelledParameter <- identifier "=" expression;
    public static Parser<Character, LabelledCall> labelledCall() {
        return IDENT.bind(name ->
                // labelledCall <- identifier
                IDENT.bind(label ->
                                EQ.then(expression().map(param -> new LabelledParameter(label, param)))
                        )
                        .sepBy(COMMA)
                        .between(LBRACKET, RBRACKET)
                        .map(params -> new LabelledCall(name, params.toList()))
        ).attempt();
    }

    public static Parser<Character, Identifier> identifier() {
        return IDENT.map(Identifier::new);
    }

    // forInExpression <- "for" "(" ループ変数 "in" 開始値 "to" 終了値 ")" ループ本体;
    public static Parser<Character, Expression> forInExpression() {
        return FOR.then(LPAREN.then(IDENT).bind(name ->
                IN.then(expression()).bind(from ->
                        TO.then(expression()).bind(to ->
                                RPAREN.then(line()).map(body ->
                                        (Expression) Block(
                                                Ast.assignment(name, from),
                                                While(lessThan(Ast.identifier(name), to),
                                                        Block(body, Ast.assignment(
                                                                name,
                                                                add(Ast.identifier(name), integer(1)))
                                                        )
                                                )
                                        )
                                )
                        )
                )
        )).attempt();
    }
}
