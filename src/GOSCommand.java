import java.util.List;

public class GOSCommand extends GOSLexem {
    private String name;
    private List<Argument> arguments;

    public GOSCommand(String name, List<Argument> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "GOSCommand{" +
                "name='" + name + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
