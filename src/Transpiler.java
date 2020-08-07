import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

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

    private static List<Pair<String, GOSIfPressed>> ifs = new ArrayList<>();

    private List<GOSLexem> commands;
    private String name;

    public Transpiler(List<GOSLexem> commands, String name) {
        this.commands = commands;
        this.name = name;
    }
    public Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>> transpile() {
        return transpile(false);
    }
    public Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>> transpile(boolean top) {
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
                                result = new Transpiler(scopeArgument.getCommandList(), name).transpile();
                        cfgs.addAll(result.getKey());
                        cfgs.add(new Pair<>(
                                getNextCfgName(),
                                result.getValue()
                        ));
                        arguments.add("exec " + ("__gencfg_" + name + "/") + getPrevCfgName());
                    } else if (argument instanceof BindScopeArgument) {
                        BindScopeArgument scopeArgument = (BindScopeArgument) argument;
                        Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                                plusResult = (new Transpiler(scopeArgument.getPlusCommands(), name).transpile());
                        Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                                minusResult = (new Transpiler(scopeArgument.getMinusCommands(), name).transpile());
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
                        commandResult.add(new CSGOCommand("alias", Arrays.asList("+" + aliasName, "exec " + ("__gencfg_" + name + "/") + "cfg" + (count - 2))));
                        commandResult.add(new CSGOCommand("alias", Arrays.asList("-" + aliasName, "exec " + ("__gencfg_" + name + "/") + getPrevCfgName())));
                        arguments.add("+" + aliasName);
                    } else if (argument instanceof SwitchScopeArgument) {
                        SwitchScopeArgument switchScopeArgument = (SwitchScopeArgument) argument;
                        String aliasName = getNextAliasName();
                        List<String> cfgNames = new ArrayList<>();
                        for (int i = 0; i < switchScopeArgument.getClauses().size(); i++) cfgNames.add(getNextCfgName());
                        for (int i = 0; i < switchScopeArgument.getClauses().size(); i++) {
                            Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                                    result = new Transpiler(switchScopeArgument.getClauses().get(i), name).transpile();
                            cfgs.addAll(result.getKey());
                            List<CSGOCommand> file = result.getValue();
                            file.add(new CSGOCommand("alias", Arrays.asList(aliasName, "exec " + ("__gencfg_" + name + "/") +
                                    cfgNames.get(i == switchScopeArgument.getClauses().size()-1 ? 0 : i+1))));
                            cfgs.add(new Pair<>(
                                    cfgNames.get(i),
                                    file
                            ));
                        }
                        commandResult.add(new CSGOCommand("alias", Arrays.asList(aliasName, "exec " + ("__gencfg_" + name + "/") + cfgNames.get(0))));
                        arguments.add(aliasName);
                    }
                }
                commandResult.add(new CSGOCommand(command.getName(), arguments));
            } else if (lexem instanceof GOSAlias) {
                GOSAlias alias = (GOSAlias) lexem;
                if (alias.getScope() instanceof ScopeArgument) {
                    ScopeArgument scopeArgument = (ScopeArgument) alias.getScope();
                    Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                            result = new Transpiler(scopeArgument.getCommandList(), name).transpile();
                    cfgs.addAll(result.getKey());
                    cfgs.add(new Pair<>(
                            getNextCfgName(),
                            result.getValue()
                    ));
                    commandResult.add(new CSGOCommand("alias", Arrays.asList(alias.getName(), "exec " + ("__gencfg_" + name + "/") + getPrevCfgName())));
                } else if (alias.getScope() instanceof BindScopeArgument) {
                    BindScopeArgument scopeArgument = (BindScopeArgument) alias.getScope();
                    Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                            plusResult = (new Transpiler(scopeArgument.getPlusCommands(), name).transpile());
                    Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                            minusResult = (new Transpiler(scopeArgument.getMinusCommands(), name).transpile());
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
                    commandResult.add(new CSGOCommand("alias", Arrays.asList("+" + alias.getName(), "exec " + ("__gencfg_" + name + "/") + "cfg" + (count - 2))));
                    commandResult.add(new CSGOCommand("alias", Arrays.asList("-" + alias.getName(), "exec " + ("__gencfg_" + name + "/") + getPrevCfgName())));
                } else if (alias.getScope() instanceof SwitchScopeArgument) {
                    SwitchScopeArgument switchScopeArgument = (SwitchScopeArgument) alias.getScope();
                    String aliasName = alias.getName();
                    List<String> cfgNames = new ArrayList<>();
                    for (int i = 0; i < switchScopeArgument.getClauses().size(); i++) cfgNames.add(getNextCfgName());
                    for (int i = 0; i < switchScopeArgument.getClauses().size(); i++) {
                        Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                                result = new Transpiler(switchScopeArgument.getClauses().get(i), name).transpile();
                        cfgs.addAll(result.getKey());
                        List<CSGOCommand> file = result.getValue();
                        file.add(new CSGOCommand("alias", Arrays.asList(aliasName, "exec " + ("__gencfg_" + name + "/") +
                                cfgNames.get(i == switchScopeArgument.getClauses().size()-1 ? 0 : i+1))));
                        cfgs.add(new Pair<>(
                                cfgNames.get(i),
                                file
                        ));
                    }
                    commandResult.add(new CSGOCommand("alias", Arrays.asList(aliasName, "exec " + ("__gencfg_" + name + "/") + cfgNames.get(0))));
                } else throw new RuntimeException("Alias second argument can only be a scope");
            } else if (lexem instanceof GOSIfPressed) {
                String aliasName = getNextAliasName();
                ifs.add(new Pair<>(aliasName, (GOSIfPressed) lexem));
                commandResult.add(new CSGOCommand(aliasName, new ArrayList<>()));
            }
        }
        if (top) {
            List<Pair<String, List<Pair<String, GOSIfPressed>>>> ifsSorted = new ArrayList<>();
            for (Pair<String, GOSIfPressed> anIf: ifs) {
                AtomicBoolean flag = new AtomicBoolean(false);
                ifsSorted.forEach(pair -> {
                    if (!flag.get() && pair.getKey().equals(anIf.getValue().getButton())) {
                        pair.getValue().add(anIf);
                        flag.set(true);
                    }
                });
                if (!flag.get()) {
                    ifsSorted.add(new Pair<>(anIf.getValue().getButton(), Arrays.asList(anIf)));
                }
            }
            List<CSGOCommand> initCommands = new ArrayList<>();
            for (Pair<String, List<Pair<String, GOSIfPressed>>> buttonListPair: ifsSorted) {
                String button = buttonListPair.getKey();
                List<Pair<String, GOSIfPressed>> ifs = buttonListPair.getValue();
                String aliasPlusCfgName = getNextCfgName();
                List<CSGOCommand> aliasesPlus = new ArrayList<>();
                for (Pair<String, GOSIfPressed> theIf: ifs) {
                    String cfgName = getNextCfgName();
                    Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                            ifResult = new Transpiler(theIf.getValue().getThen().getCommandList(), name).transpile();
                    cfgs.addAll(ifResult.getKey());
                    cfgs.add(new Pair<>(cfgName, ifResult.getValue()));
                    aliasesPlus.add(new CSGOCommand("alias", Arrays.asList(theIf.getKey(), "exec " + ("__gencfg_" + name + "/") + cfgName)));
                }
                cfgs.add(new Pair<>(aliasPlusCfgName, aliasesPlus));
                String aliasMinusCfgName = getNextCfgName();
                List<CSGOCommand> aliasesMinus = new ArrayList<>();
                for (Pair<String, GOSIfPressed> theIf: ifs) {
                    String cfgName = getNextCfgName();
                    Pair<List<Pair<String, List<CSGOCommand>>>, List<CSGOCommand>>
                            ifResult = new Transpiler(theIf.getValue().getOtherwise().getCommandList(), name).transpile();
                    cfgs.addAll(ifResult.getKey());
                    cfgs.add(new Pair<>(cfgName, ifResult.getValue()));
                    aliasesMinus.add(new CSGOCommand("alias", Arrays.asList(theIf.getKey(), "exec " + ("__gencfg_" + name + "/") + cfgName)));
                }
                cfgs.add(new Pair<>(aliasMinusCfgName, aliasesMinus));
                String aliasName = getNextAliasName();
                initCommands.add(new CSGOCommand("alias", Arrays.asList("+"+aliasName, "exec " + ("__gencfg_" + name + "/") + aliasPlusCfgName)));
                initCommands.add(new CSGOCommand("alias", Arrays.asList("-"+aliasName, "exec " + ("__gencfg_" + name + "/") + aliasMinusCfgName)));
                initCommands.add(new CSGOCommand("bind", Arrays.asList(button, "+"+aliasName)));
                initCommands.add(new CSGOCommand("exec", Arrays.asList(("__gencfg_" + name + "/") + aliasMinusCfgName)));
                List<CSGOCommand> newResult = new ArrayList<>();
                newResult.addAll(initCommands);
                newResult.addAll(commandResult);
                commandResult = newResult;
            }
        }
        return new Pair<>(cfgs, commandResult);
    }
}
