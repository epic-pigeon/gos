import javafx.util.Pair;
import sun.plugin.javascript.navig.LinkArray;

import java.util.List;

public abstract class AbstractGenerator {
    protected Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>> code;
    protected String name;
    public abstract Pair<String, List<Pair<String, String>>> generate();

    public AbstractGenerator(Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>> code, String name) {
        this.code = code;
        this.name = name;
    }
}
