package ru.urururu.cmakeedit.core.checker;

import org.junit.Assert;
import org.junit.Test;
import ru.urururu.cmakeedit.core.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by okutane on 16/08/16.
 */
public class BranchingStrategyTest {
    @Test
    public void getBranches() throws Exception {
        BranchingStrategy branching = new BranchingStrategy();

        List<CommandInvocationNode> nodes = Arrays.asList(
                command("if", "a"),
                command("message", "1"),
                command("endif")
        );

        BranchingInfo branches = branching.getBranches(new SimulationState(nodes, 0));

        Assert.assertEquals(3, branches.mergePoint);
        Assert.assertEquals(
                Arrays.asList(
                        Arrays.asList(
                                nodes.get(1)
                        ),
                        Collections.emptyList()
                ), branches.branches
        );
    }

    private CommandInvocationNode command(String commandName, String... arguments) {
        return new CommandInvocationNode(
                commandName,
                Arrays.stream(arguments).map(arg -> new ArgumentNode(arg, Collections.emptyList(), null, null)).collect(Collectors.toList()),
                Collections.emptyList(),
                null,
                null
        );
    }
}