package edu.kpi.iasa.ai.model;

import edu.kpi.iasa.ai.Utils;
import edu.kpi.iasa.ai.configuration.ConfiguredObstacle;
import javafx.util.Pair;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AStarWorker {

    class Location {
        public double x;
        public double y;

        public Location(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Location location = (Location) o;
            return Double.compare(location.x, x) == 0 &&
                    Double.compare(location.y, y) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "Location[" + x + ", " + y + ']';
        }
    }

    private HashMap<MapNode,Set<MapNode>> links;
    //stores all paths <next point> -> <current>
    private HashMap<MapNode,MapNode> cameFrom;
    private Map<MapNode, Double> distances;
    private HashSet<MapNode> visited;
    private Map<Location, MapNode> map;

    private Function<Point2D.Double, Location> convertFromPoint = (p) -> new Location(p.x, p.y);
    private Function<Location, Point2D.Double> convertFromLoaction = (p) -> new Point2D.Double(p.x, p.y);

    public BiFunction<MapNode,MapNode,Double> heuristicFunction =
            (current, endNode) -> Utils.length(Utils.diff(current.getLocation(), endNode.getLocation()));

    private ObstacleContainer obstacleContainer;

    public AStarWorker(ObstacleContainer obstacleContainer) {
        this.obstacleContainer = obstacleContainer;
    }

    public void init() {
        links = new HashMap<>();
        cameFrom = new HashMap<>();
        distances = new HashMap<>();
        visited = new HashSet<>();
        map = new HashMap<>();
        path = new ArrayList<>();
    }

    public void rebuildVisibilityGraph() {
        links.clear();
        fillVisibilityGraph();
    }

    /**AStar only variables*/
    //start point in world
    private ConfiguredObstacle start;
    //end point in world
    private ConfiguredObstacle end;
    //start point fixed on algorithm beginning
    private MapNode startNode;
    //end point fixed on algorithm beginning
    private MapNode endNode;
    //prioritised queue of unvisited but definitely reachable graph vertexes
    private Queue<MapNode> frontier;
    //finalized list of visited points from start to end
    private List<MapNode> path;

    //updating positions of start & end node in graph
    public void setup(ConfiguredObstacle start, ConfiguredObstacle goal){
        init();
        this.start = start;
        this.end = goal;
        startNode = createNodeOrGet(this.start.getValue().getConnectPoints().getPath().get(0));
        endNode = createNodeOrGet(this.end.getValue().getConnectPoints().getPath().get(0));
        assert startNode != null;
        assert endNode != null;
        rebuildVisibilityGraph();
    }

    public void start() {
        init();
        startNode = createNodeOrGet(this.start.getValue().getConnectPoints().getPath().get(0));
        endNode = createNodeOrGet(this.end.getValue().getConnectPoints().getPath().get(0));
        rebuildVisibilityGraph();
        frontier = initQueue();
        //enque StartNode, with distance 0
        startNode.setDistanceToStart((double) 0);
        distances.put(startNode, (double) 0);
        cameFrom.put(startNode, null);
        startNode.setPredictedDistance(heuristicFunction.apply(startNode, endNode));
        frontier.add(startNode);
    }

    public boolean iter() {
        if (!frontier.isEmpty()) {
            MapNode current = frontier.remove();

            if (!visited.contains(current) ){
                visited.add(current);
                // if last element in PQ reached
                if (current.equals(endNode)) {
                    path = reconstructPath(cameFrom, endNode);
                    finish();
                    return false;
                }

                Set<MapNode> neighbors = getNeighbors(current);
                if(neighbors == null || neighbors.size() == 0) {//if size == 0 - there are no such location in graph
                    return true; //Trying next node
                }
                for (MapNode neighbor : neighbors) {
                    if (!visited.contains(neighbor) ){

                        // calculate predicted distance to the end node
                        final double predictedDistance = heuristicFunction.apply(neighbor, endNode);

                        // 1. calculate distance to neighbor. 2. calculate dist from start node
                        final double currentToNeighborDistance = current.calculateDistance(neighbor);
                        final double totalDistance = current.getDistanceToStart() + predictedDistance + currentToNeighborDistance;

                        // check if distance smaller
                        if(!distances.containsKey(neighbor) || totalDistance < distances.get(neighbor) ){
                            // update n's distance
                            distances.put(neighbor, totalDistance);
                            // used for PriorityQueue
                            neighbor.setDistanceToStart(current.getDistanceToStart()+currentToNeighborDistance);
                            neighbor.setPredictedDistance(totalDistance);
                            // add to path tree
                            cameFrom.put(neighbor, current);

                            //enqueue
                            frontier.add(neighbor);
                        }
                    }
                }
            }
        }
        return true;
    }

    public void finish() {
        path = reconstructPath(cameFrom, endNode);
        links.remove(startNode);
        links.remove(endNode);
        map.remove(startNode);
        map.remove(endNode);
        distances.clear();
    }

    private MapNode createNodeOrGet(final Point2D.Double point) {
        final Location location = convertFromPoint.apply(point);
        MapNode node = map.get(location);
        if(node == null) {
            node = new MapNode(point);
            map.put(location, node);
        }
        return node;
    }

    private void addSingleLink(final MapNode from, final MapNode to) {
        //links.get(from).add(to);
        Set<MapNode> from_links =  links.get(from);
        if(from_links == null) {
            from_links = new HashSet<>();
            addNewLink(from, from_links);
        }
        from_links.add(to);

        //links.get(to).add(from);
        Set<MapNode> to_links =  links.get(to);
        if(to_links == null) {
            to_links = new HashSet<>();
            addNewLink(to, to_links);
        }
        to_links.add(from);
    }

    private void addNewLink(MapNode node, Set<MapNode> neighbors) {
        links.put(node, neighbors);
    }

    private PriorityQueue<MapNode> initQueue() {
        return new PriorityQueue<>(10, Comparator.comparingDouble(MapNode::getPredictedDistance));
    }

    private List<MapNode> reconstructPath(final Map<MapNode, MapNode> cameFrom, MapNode current) {
        final List<MapNode> totalPath = new ArrayList<>();

        //paths are stored in map< <point> -> <previous point> >
        while (current != null) {
            final MapNode previous = current;
            current = cameFrom.get(current);
            totalPath.add(previous);

        }
        Collections.reverse(totalPath);
        return totalPath;
    }

    private Set<MapNode> getNeighbors(final MapNode current) {
        final Set<MapNode> neighbors = links.get(current);
        return neighbors == null || neighbors.size() == 0 ? getNeighbors(current, true) : neighbors;
    }

    /**
     * Retrieving state of obstacles, points and other value, then perform raycasting and finally
     * saving results into global visibility graph {@see links}
     */
    private void fillVisibilityGraph() {
        List<Edge> allEdges = new ArrayList<>();
        List<Edge> outerEdges = new ArrayList<>();
        List<Edge> innerEdges = new ArrayList<>();
        List<Point2D.Double> points = new ArrayList<>();

        fillVisibilityParams(allEdges, outerEdges, innerEdges, points);
        //now List<Edge> allEdges contains ALL EDGES OF LEVEL except bounds of level

        //adding end/star even if they are not in bounds...
        if(startNode!= null)    points.add(startNode.getLocation());
        if(endNode!= null)      points.add(endNode.getLocation());
        points = new ArrayList<>(new HashSet<>(points)); //distinct & reorder

        //creating temp graph
        List<Pair<Integer, List<Integer>>> tempGraph = new ArrayList<>();

        for(int i = 0; i < points.size()-1; i++) { //foreach point except last
            List<Integer> links = new ArrayList<>();
            outer: for(int j = i+1; j < points.size(); j++) {  //to each point from next except all previous
                final Edge edge = new Edge(points.get(i), points.get(j));   //edge to test

                if(contains(edge, outerEdges)) {
                    //System.err.println("Adding outer edge: "+ edge);
                    links.add(j);
                    continue;   //try next link
                }
                if(contains(edge, innerEdges)) {
                    //System.err.println("skipping innerEdge:  "+edge);
                    continue;   //try next link
                }

                for (Edge e : allEdges) {//it's an edge between two different objects
                    if (edge.isIntersect(e) && !e.contains(points.get(i)) && !e.contains(points.get(j))) {
                        //System.err.println("intersecting "+edge+ " and " + e);
                        continue outer; //skip link
                    }
                }
                //System.err.println("Adding traced edge: "+ edge);
                links.add(j);

            }
            //add to graph
            tempGraph.add(new Pair<>(i,links));
        }

        //printTempGraph(tempGraph);
        saveTempGraph(points, tempGraph);
        //printGraphPreview(links);
    }

    /**
     * Filling params based on all obstacles stored in obstacleContainer
     * @param allEdges - will contain all Edges of level
     * @param outerEdges - will contain all outer Edges of obstacles
     * @param innerEdges - will contain all inner edges of obstacles (invisible out of obstacle)
     * @param points - will contain all reachable vertexes of obstacles
     */
    private void fillVisibilityParams(List<Edge> allEdges, List<Edge> outerEdges, List<Edge> innerEdges, List<Point2D.Double> points) {
        for(ConfiguredObstacle obst : obstacleContainer.getObstacles()) {
            points.addAll(obst.getValue().getConnectPoints().getPath());
            outerEdges.addAll(obst.getValue().getOuterEdges());
            innerEdges.addAll(obst.getValue().getInnerEdges());
        }
        points.removeIf((p)->{
            //remove point if it`s out of bounds
            if(!Utils.isInRange(p.x, 0.0, 1.0)
                    || !Utils.isInRange(p.y, 0.0, 1.0))
                return true;
            //removing overlapped points
            for(ConfiguredObstacle obst : obstacleContainer.getObstacles()) {
                if(obst.isInBounds(p))
                    return true;
            }
            return false;
        });
        outerEdges.removeIf((e) -> { //removing crossing edges
            for(ConfiguredObstacle obst : obstacleContainer.getObstacles()) {
                for(Edge edge: obst.getValue().getOuterEdges()) {
                    if(edge.equals(e)) continue;
                    if (edge.isIntersect(e) && !edge.contains(e.a) && !edge.contains(e.b)) {
                        //System.err.println("remove crossing "+edge+ " and " + e);
                        return  true;
                    }
                }
            }
            return false;
        });
        allEdges.addAll(outerEdges);
        allEdges.addAll(innerEdges);
    }

    /**
     * Simply converting id to id one-direct links to bothdirected links between MapNodes in global graph {@see this.links}.
     * @param points contains locations on draw zone, which indexes was used to build tempGraph
     * @param tempGraph contains one-direct links of indexes from points list
     */
    private void saveTempGraph(final List<Point2D.Double> points, final  List<Pair<Integer, List<Integer>>> tempGraph) {
        for(final Pair<Integer,List<Integer>> val: tempGraph){
            final MapNode from = createNodeOrGet(points.get(val.getKey()));
            for(final Integer toId: val.getValue()){
                final MapNode to = createNodeOrGet(points.get(toId));
                //System.out.println("<><><><>Adding "+val.getKey()+" "+toId+"\tat: "+ from.getLocation()+"\t"+to.getLocation());
                addSingleLink(from, to);
            }
        }
    }

    private boolean contains(final Edge edge, final List<Edge> edges) {
        final Edge edgeReversed = new Edge(edge.b, edge.a);
        for(Edge e: edges){
            if(e.equals(edge) || e.equals(edgeReversed))
                return true;
        }
        return false;
    }

    private Set<MapNode> getNeighbors(final MapNode current, final boolean force) {
        if(force) {
            fillVisibilityGraph();
        }
        return links.get(current);
    }

    private static void printTempGraph(final List<Pair<Integer, List<Integer>>> tempGraph) {
        for(int i = 0; i < tempGraph.size(); i++) {
            System.out.println("["+i+"->"+Arrays.toString(tempGraph.get(i).getValue().toArray())+"]");
        }
    }

    private static void printGraphPreview(HashMap<MapNode, Set<MapNode>> links) {

        Map<MapNode, Integer> mapping = new HashMap<>();
        int index = 0;
        for(MapNode key: links.keySet()) {
            mapping.put(key,index++);
        }

        for(MapNode key: links.keySet()) {
            String result = "["+mapping.get(key)+"->[";
            int printedCount = 0;
            for(MapNode node: links.get(key)) {
                result += mapping.get(node) + ", ";
                printedCount++;
            }
            if(printedCount != 0)
                result = result.substring(0,result.length()-2);
            result +="]]";
            System.out.println(result);
        }
    }

    private void clear() {
        cameFrom.clear();
        distances.clear();
        visited.clear();
    }

    public HashMap<MapNode, Set<MapNode>> getGraph() {
        return links;
    }

    public HashMap<MapNode, MapNode> getPathTree() {
        return cameFrom;
    }

    public List<MapNode> getBestPath() {
        return path;
    }
}
