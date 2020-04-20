import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Writer {
    private Path path;
    private String name;

    public Writer(Path path, String name) {
        this.path = path;
        this.name = name;
    }

    public void write(Pair<String, List<Pair<String, String>>> result) throws IOException {
        path.toFile().mkdirs();
        Files.write(path.resolve(name+".cfg"), result.getKey().getBytes());
        for (int i = 0; i < result.getValue().size(); i++) {
            path.resolve("__gencfg_" + name).toFile().mkdirs();
            Files.write(path.resolve("__gencfg_" + name).resolve(result.getValue().get(i).getKey() + ".cfg"), result.getValue().get(i).getValue().getBytes());
        }
    }
}
