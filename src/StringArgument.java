public class StringArgument extends Argument {
    private String value;

    public StringArgument(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "StringArgument{" +
                "value='" + value + '\'' +
                '}';
    }
}
