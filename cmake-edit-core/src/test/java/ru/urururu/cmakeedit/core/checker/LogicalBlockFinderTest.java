package ru.urururu.cmakeedit.core.checker;

import org.junit.Assert;
import org.junit.Test;
import ru.urururu.cmakeedit.core.ArgumentNode;
import ru.urururu.cmakeedit.core.CommandInvocationNode;
import ru.urururu.cmakeedit.core.ConstantNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by okutane on 16/08/16.
 */
public class LogicalBlockFinderTest {
    @Test
    public void find() throws Exception {
        List<CommandInvocationNode> nodes = Arrays.asList(
                command("if", "a"),
                command("message", "1"),
                command("endif")
        );

        LogicalBlock branches = LogicalBlockFinder.findIfNodes(nodes, 0);

        Assert.assertEquals(3, branches.endPosition);
        Assert.assertEquals(
                Arrays.asList(
                        Arrays.asList(
                                nodes.get(1)
                        ),
                        Collections.emptyList()
                ), branches.bodies
        );
    }

    private CommandInvocationNode command(String commandName, String... arguments) {
        return new CommandInvocationNode(
                commandName,
                Arrays.stream(arguments).map(arg -> new ArgumentNode(Collections.singletonList(new ConstantNode(arg, null, null)), null, null)).collect(Collectors.toList()),
                Collections.emptyList(),
                null,
                null
        );
    }
}