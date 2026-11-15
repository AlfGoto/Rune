package com.alfgoto.rune.prototype.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** THROWAWAY (issue #138): the prototype JSON stays well formed. */
class ProtoTreeDataTest {

    @Test
    void parentsComeBeforeChildrenAndGetTheNamespace() {
        Set<String> seen = new HashSet<>();
        var tree = ProtoTreeData.build("Test");
        assertEquals(1 + 2 * (1 + 4), tree.size());
        tree.forEach((path, body) -> {
            JsonObject json = new Gson().fromJson(ProtoTreeData.withNamespace(body, "ns"), JsonObject.class);
            if (json.has("parent")) {
                String parent = json.get("parent").getAsString();
                assertTrue(parent.startsWith("ns:"));
                assertTrue(seen.contains(parent.substring(3)), path + " comes before its parent");
            }
            assertTrue(json.getAsJsonObject("criteria").has(ProtoTreeData.CRITERION));
            seen.add(path);
        });
    }
}
