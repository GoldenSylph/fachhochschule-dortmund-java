package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.util.*;

public class Area {
	// graph stored as adjacency list: node Point -> set of neighbour Points
	private Map<Point, Set<Point>> graph;
	// set of nodes that are marked as points of interest
	private int startX = 0, startY = 0;

	public record Point(int x, int y) {
	}

	// Dijkstra on the graph: returns shortest path as list of Points from start -> target
	// edge weight = Euclidean distance between points
	public List<Point> findPath(int startXParam, int startYParam, int targetX, int targetY) {
		if (graph == null)
			return List.of();
		Point startPoint = new Point(startXParam, startYParam);
		Point targetPoint = new Point(targetX, targetY);
		if (!graph.containsKey(startPoint) || !graph.containsKey(targetPoint))
			return List.of();

		// distances and previous node map
		Map<Point, Double> dist = new HashMap<>();
		Map<Point, Point> previous = new HashMap<>();
		for (Point node : graph.keySet()) {
			dist.put(node, Double.POSITIVE_INFINITY);
		}
		dist.put(startPoint, 0.0);

		PriorityQueue<Point> queue = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
		queue.add(startPoint);

		while (!queue.isEmpty()) {
			Point current = queue.poll();
			if (current == null)
				break;
			if (current.equals(targetPoint))
				break;
			double currentDist = dist.getOrDefault(current, Double.POSITIVE_INFINITY);
			for (Point neighbor : graph.getOrDefault(current, Collections.emptySet())) {
				double weight = euclideanDistance(current, neighbor);
				double newDist = currentDist + weight;
				if (newDist < dist.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
					dist.put(neighbor, newDist);
					previous.put(neighbor, current);
					// reinsert neighbor to update its priority
					queue.remove(neighbor);
					queue.add(neighbor);
				}
			}
		}

		if (!previous.containsKey(targetPoint) && !startPoint.equals(targetPoint))
			return List.of();

		LinkedList<Point> path = new LinkedList<>();
		for (Point at = targetPoint; at != null; at = previous.get(at)) {
			path.addFirst(at);
			if (at.equals(startPoint))
				break;
		}
		return path;
	}

	public List<Point> findPath(int targetX, int targetY) {
		return findPath(this.startX, this.startY, targetX, targetY);
	}
	
	public List<Point> findPath(Point targetPoint) {
		return findPath(this.startX, this.startY, targetPoint.x(), targetPoint.y());
	}
	
	public List<Point> findPath(Point startPoint, Point targetPoint) {
		return findPath(startPoint.x(), startPoint.y(), targetPoint.x(), targetPoint.y());
	}

	private static double euclideanDistance(Point a, Point b) {
		double dx = (double) a.x() - b.x();
		double dy = (double) a.y() - b.y();
		return Math.hypot(dx, dy);
	}

	public void setGraph(Map<Point, Set<Point>> adjacency) {
		if (adjacency == null) {
			this.graph = null;
			return;
		}
		Map<Point, Set<Point>> copy = new HashMap<>();
		for (Map.Entry<Point, Set<Point>> entry : adjacency.entrySet()) {
			copy.put(entry.getKey(), entry.getValue() == null ? Set.of() : new HashSet<>(entry.getValue()));
		}
		this.graph = copy;
	}

	public Map<Point, Set<Point>> getAdjacencyMap() {
		return Collections.unmodifiableMap(graph);
	}
	
	public void setStart(int startXCoord, int startYCoord) {
		this.startX = startXCoord;
		this.startY = startYCoord;
	}

	public int getStartX() {
		return startX;
	}

	public int getStartY() {
		return startY;
	}
}