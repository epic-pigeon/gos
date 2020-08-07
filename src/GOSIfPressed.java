public class GOSIfPressed extends GOSLexem {
    private String button;
    private ScopeArgument then, otherwise;

    public GOSIfPressed(String button, ScopeArgument then, ScopeArgument otherwise) {
        this.button = button;
        this.then = then;
        this.otherwise = otherwise;
    }

    public String getButton() {
        return button;
    }

    public ScopeArgument getThen() {
        return then;
    }

    public ScopeArgument getOtherwise() {
        return otherwise;
    }
}
