package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.util.*;

public class Area {
	// graph stored as adjacency list: node key -> set of neighbour node keys
	private Map<Long, Set<Long>> graph;
	// set of nodes that are marked as restaurants (packed keys)
	private Set<Long> restaurants;
	private int startX = 0, startY = 0;

	public record Point(int x, int y) {
	}

	// BFS on the graph: returns shortest path as list of Points from start ->
	// target
	public List<Point> findPath(int sx, int sy, int targetX, int targetY) {
		if (graph == null)
			return List.of();
		long s = key(sx, sy), t = key(targetX, targetY);
		if (!graph.containsKey(s) || !graph.containsKey(t))
			return List.of();

		Deque<Long> q = new ArrayDeque<>();
		Map<Long, Long> parent = new HashMap<>();
		q.add(s);
		parent.put(s, null);

		while (!q.isEmpty()) {
			Long cur = q.poll();
			if (cur == null)
				break;
			if (cur.longValue() == t)
				break;
			for (Long nb : graph.getOrDefault(cur, Collections.emptySet())) {
				if (parent.containsKey(nb))
					continue;
				parent.put(nb, cur);
				q.add(nb);
			}
		}

		if (!parent.containsKey(t))
			return List.of();

		LinkedList<Point> path = new LinkedList<>();
		for (Long cur = t; cur != null; cur = parent.get(cur)) {
			path.addFirst(new Point(xFromKey(cur), yFromKey(cur)));
		}
		return path;
	}

	public List<Point> findPath(int targetX, int targetY) {
		return findPath(this.startX, this.startY, targetX, targetY);
	}

	public static long key(int x, int y) {
		return ((long) x << 32) | (y & 0xffffffffL);
	}

	public static int xFromKey(long k) {
		return (int) (k >> 32);
	}

	public static int yFromKey(long k) {
		return (int) k;
	}

	public boolean isRestaurant(int x, int y) {
		return restaurants != null && restaurants.contains(Long.valueOf(key(x, y)));
	}

	public void addRestaurant(int x, int y) {
		if (restaurants == null)
			restaurants = new HashSet<>();
		restaurants.add(Long.valueOf(key(x, y)));
	}

	public void removeRestaurant(int x, int y) {
		if (restaurants != null)
			restaurants.remove(Long.valueOf(key(x, y)));
	}

	// replace restaurant set with a defensive copy of the provided packed-key set.
	public void setRestaurants(Set<Long> restaurantsSet) {
		this.restaurants = restaurantsSet == null ? null : new HashSet<>(restaurantsSet);
	}

	public Set<Point> getRestaurants() {
		if (restaurants == null)
			return Collections.emptySet();
		Set<Point> out = new HashSet<>();
		for (Long k : restaurants)
			out.add(new Point(xFromKey(k), yFromKey(k)));
		return out;
	}

	public void setGraph(Map<Long, Set<Long>> adjacency) {
		if (adjacency == null) {
			this.graph = null;
			return;
		}
		Map<Long, Set<Long>> copy = new HashMap<>();
		for (Map.Entry<Long, Set<Long>> e : adjacency.entrySet()) {
			copy.put(e.getKey(), e.getValue() == null ? Set.of() : new HashSet<>(e.getValue()));
		}
		this.graph = copy;
	}

	public void setStart(int x, int y) {
		this.startX = x;
		this.startY = y;
	}

	public int getStartX() {
		return startX;
	}

	public int getStartY() {
		return startY;
	}
}