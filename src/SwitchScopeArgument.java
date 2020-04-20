import java.util.List;

public class SwitchScopeArgument extends Argument {
    private List<GOSCommand> plusCommands, minusCommands;

    public SwitchScopeArgument(List<GOSCommand> plusCommands, List<GOSCommand> minusCommands) {
        this.plusCommands = plusCommands;
        this.minusCommands = minusCommands;
    }

    public List<GOSCommand> getPlusCommands() {
        return plusCommands;
    }

    public List<GOSCommand> getMinusCommands() {
        return minusCommands;
    }

    @Override
    public String toString() {
        return "SwitchScopeArgument{" +
                "plusCommands=" + plusCommands +
                ", minusCommands=" + minusCommands +
                '}';
    }
}
