import javafx.util.Pair;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Transpiler {
    private static int count = 0;
    private static String getNextCfgName() {
        return "cfg"+count++;
    }
    private static String getPrevCfgName() {
        return "cfg"+(count-1);
    }
    private static int aliasCount = 0;
    private static String getNextAliasName() {
        return "__internal"+aliasCount++;
    }
    private static String getPrevAliasName() {
        return "__internal"+(aliasCount-1);
    }


    private List<GOSLexem> commands;
    private String name;

    public Transpiler(List<GOSLexem> commands, String name) {
        this.commands = commands;
        this.name = name;
    }

    public Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>> transpile() {
        return transpile(false);
    }

    private Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>> transpile(boolean recursive) {
        List<CSGOCommand> commandResult = new ArrayList<>();
        List<Pair<String, List<CSGOCommand>>> cfgs = new ArrayList<>();
        for (GOSLexem lexem: commands) {
            if (lexem instanceof GOSCommand) {
                GOSCommand command = (GOSCommand) lexem;
                List<String> arguments = new ArrayList<>();
                for (Argument argument : command.getArguments()) {
                    if (argument instanceof StringArgument) {
                        arguments.add(((StringArgument) argument).getValue());
                    } else if (argument instanceof ScopeArgument) {
                        ScopeArgument scopeArgument = (ScopeArgument) argument;
                        Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                                result = new Transpiler(scopeArgument.getCommandList(), name).transpile(true);
                        cfgs.addAll(result.getKey());
                        cfgs.add(new Pair<>(
                                getNextCfgName(),
                                result.getValue()
                        ));
                        arguments.add("exec " + (recursive ? "" : "__gencfg_" + name + "/") + getPrevCfgName());
                    } else if (argument instanceof SwitchScopeArgument) {
                        SwitchScopeArgument scopeArgument = (SwitchScopeArgument) argument;
                        Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                                plusResult = (new Transpiler(scopeArgument.getPlusCommands(), name).transpile(true));
                        Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                                minusResult = (new Transpiler(scopeArgument.getMinusCommands(), name).transpile(true));
                        cfgs.addAll(plusResult.getKey());
                        cfgs.addAll(minusResult.getKey());
                        String aliasName = getNextAliasName();
                        cfgs.add(new Pair<>(
                                getNextCfgName(),
                                plusResult.getValue()
                        ));
                        cfgs.add(new Pair<>(
                                getNextCfgName(),
                                minusResult.getValue()
                        ));
                        commandResult.add(new CSGOCommand("alias", Arrays.asList("+" + aliasName, "exec " + (recursive ? "" : "__gencfg_" + name + "/") + "cfg" + (count - 2))));
                        commandResult.add(new CSGOCommand("alias", Arrays.asList("-" + aliasName, "exec " + (recursive ? "" : "__gencfg_" + name + "/") + getPrevCfgName())));
                        arguments.add("+" + aliasName);
                    }
                }
                commandResult.add(new CSGOCommand(command.getName(), arguments));
            } else if (lexem instanceof GOSAlias) {
                GOSAlias alias = (GOSAlias) lexem;
                if (alias.getScope() instanceof ScopeArgument) {
                    ScopeArgument scopeArgument = (ScopeArgument) alias.getScope();
                    Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                            result = new Transpiler(scopeArgument.getCommandList(), name).transpile(true);
                    cfgs.addAll(result.getKey());
                    cfgs.add(new Pair<>(
                            getNextCfgName(),
                            result.getValue()
                    ));
                    commandResult.add(new CSGOCommand("alias", Arrays.asList(alias.getName(), "exec " + (recursive ? "" : "__gencfg_" + name + "/") + getPrevCfgName())));
                } else if (alias.getScope() instanceof SwitchScopeArgument) {
                    SwitchScopeArgument scopeArgument = (SwitchScopeArgument) alias.getScope();
                    Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                            plusResult = (new Transpiler(scopeArgument.getPlusCommands(), name).transpile(true));
                    Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                            minusResult = (new Transpiler(scopeArgument.getMinusCommands(), name).transpile(true));
                    cfgs.addAll(plusResult.getKey());
                    cfgs.addAll(minusResult.getKey());
                    cfgs.add(new Pair<>(
                            getNextCfgName(),
                            plusResult.getValue()
                    ));
                    cfgs.add(new Pair<>(
                            getNextCfgName(),
                            minusResult.getValue()
                    ));
                    commandResult.add(new CSGOCommand("alias", Arrays.asList("+" + alias.getName(), "exec " + (recursive ? "" : "__gencfg_" + name + "/") + "cfg" + (count - 2))));
                    commandResult.add(new CSGOCommand("alias", Arrays.asList("-" + alias.getName(), "exec " + (recursive ? "" : "__gencfg_" + name + "/") + getPrevCfgName())));
                }
            }
        }
        return new Pair<>(cfgs, commandResult);
    }
}
