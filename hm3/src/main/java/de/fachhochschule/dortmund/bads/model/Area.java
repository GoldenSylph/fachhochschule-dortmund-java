package de.fachhochschule.dortmund.bads.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Area {
	private static final Logger LOGGER = LogManager.getLogger();
	
	// graph stored as adjacency list: node Point -> set of neighbour Points
	private Map<Point, Set<Point>> graph;
	// set of nodes that are marked as points of interest
	private int startX = 0, startY = 0;

	public record Point(int x, int y) {
	}

	// Dijkstra on the graph: returns shortest path as list of Points from start -> target
	// edge weight = Euclidean distance between points
	public List<Point> findPath(int startXParam, int startYParam, int targetX, int targetY) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Finding path from ({}, {}) to ({}, {})", startXParam, startYParam, targetX, targetY);
		}
		
		if (graph == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Graph is null, cannot find path");
			}
			return List.of();
		}
		
		Point startPoint = new Point(startXParam, startYParam);
		Point targetPoint = new Point(targetX, targetY);
		
		if (!graph.containsKey(startPoint) || !graph.containsKey(targetPoint)) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Start point {} or target point {} not found in graph", startPoint, targetPoint);
			}
			return List.of();
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Starting Dijkstra algorithm with {} nodes in graph", graph.size());
		}

		// distances and previous node map
		Map<Point, Double> dist = new HashMap<>();
		Map<Point, Point> previous = new HashMap<>();
		for (Point node : graph.keySet()) {
			dist.put(node, Double.POSITIVE_INFINITY);
		}
		dist.put(startPoint, 0.0);

		PriorityQueue<Point> queue = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
		queue.add(startPoint);

		int visitedNodes = 0;
		while (!queue.isEmpty()) {
			Point current = queue.poll();
			if (current == null)
				break;
			
			visitedNodes++;
			if (current.equals(targetPoint)) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Target reached after visiting {} nodes", visitedNodes);
				}
				break;
			}
			
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

		if (!previous.containsKey(targetPoint) && !startPoint.equals(targetPoint)) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No path found from {} to {}", startPoint, targetPoint);
			}
			return List.of();
		}

		LinkedList<Point> path = new LinkedList<>();
		for (Point at = targetPoint; at != null; at = previous.get(at)) {
			path.addFirst(at);
			if (at.equals(startPoint))
				break;
		}
		
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Path found from {} to {} with {} steps, total distance: {}", 
				startPoint, targetPoint, path.size(), String.format("%.2f", dist.get(targetPoint)));
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Path: {}", path);
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
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Graph set to null");
			}
			return;
		}
		Map<Point, Set<Point>> copy = new HashMap<>();
		for (Map.Entry<Point, Set<Point>> entry : adjacency.entrySet()) {
			copy.put(entry.getKey(), entry.getValue() == null ? Set.of() : new HashSet<>(entry.getValue()));
		}
		this.graph = copy;
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Graph initialized with {} nodes", this.graph.size());
		}
		if (LOGGER.isDebugEnabled()) {
			int totalEdges = this.graph.values().stream().mapToInt(Set::size).sum();
			LOGGER.debug("Graph contains {} total edges", totalEdges);
		}
	}

	public Map<Point, Set<Point>> getAdjacencyMap() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adjacency map requested, graph has {} nodes", 
				graph != null ? graph.size() : 0);
		}
		if (graph == null) {
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(graph);
	}
	
	public void setStart(int startXCoord, int startYCoord) {
		Point oldStart = new Point(this.startX, this.startY);
		this.startX = startXCoord;
		this.startY = startYCoord;
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Start position changed from {} to ({}, {})", 
				oldStart, startXCoord, startYCoord);
		}
	}

	public int getStartX() {
		return startX;
	}

	public int getStartY() {
		return startY;
	}
}