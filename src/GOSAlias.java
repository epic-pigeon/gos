public class GOSAlias extends GOSLexem {
    private String name;
    private Argument scope;

    public GOSAlias(String name, Argument scope) {
        this.name = name;
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public Argument getScope() {
        return scope;
    }
}
