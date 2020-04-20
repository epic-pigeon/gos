import javafx.scene.SceneAntialiasing;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Generator extends AbstractGenerator {
    public Generator(Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>> code, String name) {
        super(code, name);
    }

    @Override
    public Pair<String, List<Pair<String, String>>> generate() {
        StringBuilder result = new StringBuilder();
        List<Pair<String, String>> files = new ArrayList<>();
        for (int i = 0; i < code.getKey().size(); i++) {
            Pair<String, List<CSGOCommand>> alias = code.getKey().get(i);
            files.add(new Pair<>(alias.getKey(), commandsToLine(alias.getValue())));
        }
        for (CSGOCommand command: code.getValue()) result.append(commandToString(command)).append("\n");
        return new Pair<>(result.toString(), files);
    }

    private String commandToString(CSGOCommand command) {
        StringBuilder result = new StringBuilder(command.getName());
        for (String arg: command.getArguments()) result.append(" \"").append(arg).append('"');
        return result.toString();
    }

    private String commandsToLine(List<CSGOCommand> commands) {
        StringBuilder result = new StringBuilder();
        for (CSGOCommand command: commands) {
            result.append(commandToString(command)).append("; ");
        }
        return result.toString();
    }
}
