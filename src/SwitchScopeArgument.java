import java.util.List;

public class SwitchScopeArgument extends Argument {
    private List<List<GOSLexem>> clauses;

    public SwitchScopeArgument(List<List<GOSLexem>> clauses) {
        this.clauses = clauses;
    }

    public List<List<GOSLexem>> getClauses() {
        return clauses;
    }
}
