import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        String code = new String(Files.readAllBytes(Paths.get("mycfg.gos")));
        List<GOSCommand> result = new GOSLexer(code).lex();
        String name = "kar";
        new Writer(Paths.get("test"), name).write(new Generator(new Transpiler(result, name).transpile(), name).generate());
    }
}
