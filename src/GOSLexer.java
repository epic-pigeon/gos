import java.util.ArrayList;
import java.util.List;

public class GOSLexer {
    private String code;
    private int ptr;

    public GOSLexer(String code) {
        this.code = code;
    }

    public List<GOSLexem> lex() {
        List<GOSLexem> result = new ArrayList<>();
        skipWhitespace();
        while (ptr != code.length()) {
            result.add(parseLexem());
            skipWhitespace();
        }
        return result;
    }

    private Character nextChar(int count) {
        return ptr+count < code.length() ? code.charAt(ptr+count) : null;
    }

    private Character nextChar() {
        return nextChar(0);
    }

    private Character consumeChar() {
        return ptr < code.length() ? code.charAt(ptr++) : null;
    }

    private void skipWhitespace() {
        while (nextChar() != null && Character.isWhitespace(nextChar())) consumeChar();
        if (nextChar() != null && nextChar() == '*') {
            consumeChar();
            while (nextChar() != '*') consumeChar();
            consumeChar();
            skipWhitespace();
        }
    }

    private String parseString() {
        StringBuilder result = new StringBuilder();
        char quote = consumeChar();
        assert quote == '"' || quote == '\'';
        while (nextChar() != quote) {
            char c = consumeChar();
            if (c == '\\') c = consumeChar();
            result.append(c);
        }
        consumeChar();
        return result.toString();
    }

    private boolean isIdentifierChar(char c) {
        return Character.isAlphabetic(c) || Character.isDigit(c) || c == '$' || c == '_' || c == '+' || c == '-';
    }

    private String parseIdentifier() {
        StringBuilder identifier = new StringBuilder();
        while (isIdentifierChar(nextChar())) identifier.append(consumeChar());
        if (identifier.length() == 0) throw new RuntimeException();
        return identifier.toString();
    }

    private StringArgument parseStringArgument() {
        return new StringArgument(parseString());
    }

    private Argument parseArgument() {
        if (nextChar() == null) return null;
        if (nextChar() == '\'' || nextChar() == '"') {
            return parseStringArgument();
        } else if (nextChar() == '{') {
            return parseScopeArgument();
        } else if (nextChar() == '+') {
            return parseBindScopeArgument();
        } else if (nextChar() == '#') {
            return parseSwitchScopeArgument();
        }
        return null;
    }
    private SwitchScopeArgument parseSwitchScopeArgument() {
        char c = consumeChar();
        assert c == '#';
        List<List<GOSLexem>> clauses = new ArrayList<>();
        skipWhitespace();
        clauses.add(parseScopeArgument().getCommandList());
        skipWhitespace();
        while (nextChar() != null && nextChar() == ',') {
            consumeChar();
            skipWhitespace();
            clauses.add(parseScopeArgument().getCommandList());
            skipWhitespace();
        }
        return new SwitchScopeArgument(clauses);
    }
    private ScopeArgument parseScopeArgument() {
        char c = consumeChar();
        assert c == '{';
        List<GOSLexem> commands = new ArrayList<>();
        skipWhitespace();
        while (nextChar() != '}') {
            commands.add(parseLexem());
            skipWhitespace();
        }
        consumeChar();
        return new ScopeArgument(commands);
    }
    private BindScopeArgument parseBindScopeArgument() {
        char c = consumeChar();
        assert c == '+';
        skipWhitespace();
        c = consumeChar();
        assert c == '{';
        List<GOSLexem> plusCommands = new ArrayList<>();
        skipWhitespace();
        while (nextChar() != '}') {
            plusCommands.add(parseLexem());
            skipWhitespace();
        }
        consumeChar();
        skipWhitespace();


        c = consumeChar();
        assert c == '-';
        skipWhitespace();
        c = consumeChar();
        assert c == '{';
        List<GOSLexem> minusCommands = new ArrayList<>();
        skipWhitespace();
        while (nextChar() != '}') {
            minusCommands.add(parseLexem());
            skipWhitespace();
        }
        consumeChar();
        return new BindScopeArgument(plusCommands, minusCommands);
    }

    private GOSAlias parseAlias() {
        skipWhitespace();
        String identifier = parseIdentifier();
        skipWhitespace();
        Argument argument = parseArgument();
        return new GOSAlias(identifier, argument);
    }

    private GOSLexem parseLexem() {
        if (nextChar() == ';') consumeChar();
        skipWhitespace();
        String identifier = parseIdentifier();
        if (identifier.equals("alias")) return parseAlias();
        List<Argument> arguments = new ArrayList<>();
        skipWhitespace();
        Argument argument = parseArgument();
        while (argument != null) {
            arguments.add(argument);
            skipWhitespace();
            argument = parseArgument();
        }
        return new GOSCommand(identifier, arguments);
    }
}
