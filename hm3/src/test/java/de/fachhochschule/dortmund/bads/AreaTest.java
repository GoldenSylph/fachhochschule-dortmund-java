package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.model.Area;

class AreaTest {

	@Test
    void pathViaPointAdjacency() {
        Area a = new Area();
        Map<Area.Point, Set<Area.Point>> adj = new HashMap<>();

        Area.Point p00 = new Area.Point(0, 0);
        Area.Point p10 = new Area.Point(1, 0);
        Area.Point p20 = new Area.Point(2, 0);

        adj.put(p00, Set.of(p10));
        adj.put(p10, Set.of(p00, p20));
        adj.put(p20, Set.of(p10));

        a.setGraph(adj);
        a.setStart(0, 0);

        var path = a.findPath(2, 0);
        assertEquals(3, path.size());
        assertEquals(p00, path.get(0));
        assertEquals(p10, path.get(1));
        assertEquals(p20, path.get(2));

        // sanity: all nodes present in internal graph
        assertTrue(path.containsAll(Set.of(p00, p10, p20)));
    }
}