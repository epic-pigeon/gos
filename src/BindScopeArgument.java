import java.util.List;

public class BindScopeArgument extends Argument {
    private List<GOSLexem> plusCommands, minusCommands;

    public BindScopeArgument(List<GOSLexem> plusCommands, List<GOSLexem> minusCommands) {
        this.plusCommands = plusCommands;
        this.minusCommands = minusCommands;
    }

    public List<GOSLexem> getPlusCommands() {
        return plusCommands;
    }

    public List<GOSLexem> getMinusCommands() {
        return minusCommands;
    }

    @Override
    public String toString() {
        return "BindScopeArgument{" +
                "plusCommands=" + plusCommands +
                ", minusCommands=" + minusCommands +
                '}';
    }
}
