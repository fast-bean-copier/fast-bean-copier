package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.v160.NullSelfNodeDto;
import com.github.jackieonway.copier.example.v160.NullSelfNodeDtoCopier;
import com.github.jackieonway.copier.example.v160.AutoBiNodeADto;
import com.github.jackieonway.copier.example.v160.AutoBiNodeADtoCopier;
import com.github.jackieonway.copier.example.v160.BiNodeA;
import com.github.jackieonway.copier.example.v160.BiNodeB;
import com.github.jackieonway.copier.example.v160.NullBiNodeADto;
import com.github.jackieonway.copier.example.v160.NullBiNodeADtoCopier;
import com.github.jackieonway.copier.example.v160.SelfNode;
import com.github.jackieonway.copier.example.v160.SelfNodeDto;
import com.github.jackieonway.copier.example.v160.SelfNodeDtoCopier;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class V160CycleDetectionTest {

    @Test
    public void returnNullBreaksSelfReference() {
        SelfNode source = node("root");
        source.setSelf(source);
        source.setChildren(Arrays.asList(source));

        NullSelfNodeDto result = NullSelfNodeDtoCopier.toDto(source);

        assertNotNull(result);
        assertEquals("root", result.getName());
        assertNull(result.getSelf());
        assertNotNull(result.getChildren());
        assertNull(result.getChildren().get(0));
    }

    @Test
    public void automaticCachePreservesSelfReference() {
        SelfNode source = node("root");
        source.setSelf(source);
        source.setChildren(Arrays.asList(source));

        SelfNodeDto result = SelfNodeDtoCopier.toDto(source);

        assertNotNull(result);
        assertEquals("root", result.getName());
        assertSame(result, result.getSelf());
        assertNotNull(result.getChildren());
        assertSame(result, result.getChildren().get(0));
    }

    @Test
    public void automaticCachePreservesRepeatedReferencesInListBatch() {
        SelfNode shared = node("shared");

        SelfNodeDto[] result = SelfNodeDtoCopier.toDtoArray(new SelfNode[] { shared, shared });

        assertNotNull(result);
        assertEquals(2, result.length);
        assertSame(result[0], result[1]);
    }

    @Test
    public void returnNullBreaksBidirectionalReference() {
        BiNodeA source = bidirectionalNode();

        NullBiNodeADto result = NullBiNodeADtoCopier.toDto(source);

        assertNotNull(result);
        assertEquals("a", result.getName());
        assertNotNull(result.getChild());
        assertEquals("b", result.getChild().getLabel());
        assertNull(result.getChild().getParent());
    }

    @Test
    public void automaticCachePreservesBidirectionalReference() {
        BiNodeA source = bidirectionalNode();

        AutoBiNodeADto result = AutoBiNodeADtoCopier.toDto(source);

        assertNotNull(result);
        assertEquals("a", result.getName());
        assertNotNull(result.getChild());
        assertEquals("b", result.getChild().getLabel());
        assertSame(result, result.getChild().getParent());
    }

    @Test
    public void returnNullWorksWithBiFunctionPostProcessor() {
        SelfNode source = node("root");
        source.setSelf(source);

        NullSelfNodeDto result = NullSelfNodeDtoCopier.toDto(source, null, (s, t) -> {
            t.setName(t.getName() + "_checked");
            return t;
        });

        assertNotNull(result);
        assertEquals("root_checked", result.getName());
        assertNull(result.getSelf());
    }

    private static SelfNode node(String name) {
        SelfNode node = new SelfNode();
        node.setName(name);
        return node;
    }

    private static BiNodeA bidirectionalNode() {
        BiNodeA a = new BiNodeA();
        a.setName("a");
        BiNodeB b = new BiNodeB();
        b.setLabel("b");
        a.setChild(b);
        b.setParent(a);
        return a;
    }
}
