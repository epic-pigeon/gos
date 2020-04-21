import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: java -jar gosc.jar <GOS script path> <CFG folder path> <CFG name>");
            System.exit(1);
        }
        String code = new String(Files.readAllBytes(Paths.get(args[0])));
        List<GOSLexem> result = new GOSLexer(code).lex();
        String name = args[2];
        new Writer(Paths.get(args[1]), name).write(new Generator(new Transpiler(result, name).transpile(), name).generate());
    }
}
