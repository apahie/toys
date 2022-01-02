package calculator;

public enum Operator {
    ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("/");
    private String name;

    Operator(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
