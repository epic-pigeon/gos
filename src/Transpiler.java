import javafx.util.Pair;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Transpiler {
    private List<GOSCommand> commands;
    private String name;

    public Transpiler(List<GOSCommand> commands, String name) {
        this.commands = commands;
        this.name = name;
    }

    private List<CSGOCommand> scopeResultCommands(List<CSGOCommand> commands) {
        List<CSGOCommand> result = new ArrayList<>();
        for (CSGOCommand command : commands) {
            List<String> args = new ArrayList<>();
            for (String arg: command.getArguments()) {
                args.add(arg.startsWith("__internalalias_") || arg.startsWith("+__internalalias_") ? arg + "s" : arg);
            }
            result.add(new CSGOCommand(command.getName().startsWith("__internalalias_") ? command.getName() + "s" : command.getName(), command.getArguments()));
        }
        return result;
    }

    private Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>> scopeResult(
            Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>> result
    ) {
        List<Pair<String, List<CSGOCommand>>> aliases = new ArrayList<>();
        for (Pair<String, List<CSGOCommand>> alias: result.getKey()) {
            aliases.add(new Pair<>(
                    alias.getKey().startsWith("__internalalias_")||alias.getKey().startsWith("+__internalalias_")||alias.getKey().startsWith("-__internalalias_") ? alias.getKey() + "s" : alias.getKey(),
                    scopeResultCommands(alias.getValue())
            ));
        }
        return new Pair<>(
                aliases,
                scopeResultCommands(result.getValue())
        );
    }

    public Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>> transpile() {
        return transpile(0);
    }

    private Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>> transpile(int recursive) {
        List<CSGOCommand> commandResult = new ArrayList<>();
        int aliasCount = 0;
        List<Pair<String, List<CSGOCommand>>> cfgs = new ArrayList<>();
        for (GOSCommand command: commands) {
            List<String> arguments = new ArrayList<>();
            for (Argument argument: command.getArguments()) {
                if (argument instanceof StringArgument) {
                    arguments.add(((StringArgument) argument).getValue());
                } else if (argument instanceof ScopeArgument) {
                    ScopeArgument scopeArgument = (ScopeArgument) argument;
                    Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                            result = new Transpiler(scopeArgument.getCommandList(), name).transpile(recursive+1);
                    cfgs.addAll(result.getKey());
                    cfgs.add(new Pair<>(
                            "cfg_"+recursive+"_"+cfgs.size(),
                            result.getValue()
                    ));
                    arguments.add("exec " + (recursive > 0 ? "" : "__gencfg_"+name+"/") + "cfg_"+recursive+"_"+(cfgs.size()-1));
                } else if (argument instanceof SwitchScopeArgument) {
                    SwitchScopeArgument scopeArgument = (SwitchScopeArgument) argument;
                    Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                            plusResult = scopeResult(new Transpiler(scopeArgument.getPlusCommands(), name).transpile());
                    Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                            minusResult = scopeResult(new Transpiler(scopeArgument.getMinusCommands(), name).transpile());
                    cfgs.addAll(plusResult.getKey());
                    cfgs.addAll(minusResult.getKey());
                    String aliasName = "__internal"+recursive+"_"+aliasCount++;
                    cfgs.add(new Pair<>(
                            "cfg_"+recursive+"_"+cfgs.size(),
                            plusResult.getValue()
                    ));
                    cfgs.add(new Pair<>(
                            "cfg_"+recursive+"_"+cfgs.size(),
                            minusResult.getValue()
                    ));
                    commandResult.add(new CSGOCommand("alias", Arrays.asList("+"+aliasName, "exec " + (recursive > 0 ? "" : "__gencfg_"+name+"/") + "cfg_"+recursive+"_"+(cfgs.size()-2))));
                    commandResult.add(new CSGOCommand("alias", Arrays.asList("-"+aliasName, "exec " + (recursive > 0 ? "" : "__gencfg_"+name+"/") + "cfg_"+recursive+"_"+(cfgs.size()-1))));
                    arguments.add("+"+aliasName);
                }
            }
            commandResult.add(new CSGOCommand(command.getName(), arguments));
        }
        return new Pair<>(cfgs, commandResult);
    }
}
