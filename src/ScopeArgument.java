import java.util.List;

public class ScopeArgument extends Argument {
    private List<GOSCommand> commandList;

    public ScopeArgument(List<GOSCommand> commandList) {
        this.commandList = commandList;
    }

    public List<GOSCommand> getCommandList() {
        return commandList;
    }

    @Override
    public String toString() {
        return "ScopeArgument{" +
                "commandList=" + commandList +
                '}';
    }
}
