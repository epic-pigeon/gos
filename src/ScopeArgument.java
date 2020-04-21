import java.util.List;

public class ScopeArgument extends Argument {
    private List<GOSLexem> commandList;

    public ScopeArgument(List<GOSLexem> commandList) {
        this.commandList = commandList;
    }

    public List<GOSLexem> getCommandList() {
        return commandList;
    }

    @Override
    public String toString() {
        return "ScopeArgument{" +
                "commandList=" + commandList +
                '}';
    }
}
