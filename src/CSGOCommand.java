import java.util.List;

public class CSGOCommand {
    private String name;
    private List<String> arguments;

    public CSGOCommand(String name, List<String> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public List<String> getArguments() {
        return arguments;
    }
}
