package bearmaps.proj2c;

import bearmaps.MyTrieSet;
import bearmaps.hw4.WeightedEdge;
import bearmaps.hw4.streetmap.Node;
import bearmaps.hw4.streetmap.StreetMapGraph;
import bearmaps.proj2ab.KDTree;
import bearmaps.proj2ab.Point;

import java.time.temporal.ValueRange;
import java.util.*;

/**
 * An augmented graph that is more powerful that a standard StreetMapGraph.
 * Specifically, it supports the following additional operations:
 *
 *
 * @author Alan Yao, Josh Hug, ________
 */
public class AugmentedStreetMapGraph extends StreetMapGraph {

    private MyTrieSet locations;
    private Map<Node, Point> node2point;
    private Map<Point, Node> point2node;

    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);
        locations = new MyTrieSet();
        node2point = new HashMap<>();
        point2node = new HashMap<>();
        List<Node> nodes = getNodes();
        for (Node node : nodes) {
            if (node.name() != null) {
                locations.add(cleanString(node.name()));
            }
            node2point.put(node, nodeToPoint(node));
            point2node.put(nodeToPoint(node), node);
        }
        // You might find it helpful to uncomment the line below:
        // List<Node> nodes = this.getNodes();
    }

    private static Point nodeToPoint(Node n) {
        return new Point(n.lon(), n.lat());
    }

    /**
     * For Project Part II
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    public long closest(double lon, double lat) {
        // TODO Use KdTree to run in O(logN) time

        List<Point> points = new LinkedList<>();
        for (Node n : node2point.keySet()) {
            points.add(node2point.get(n));
        }
        KDTree positions = new KDTree(points);
        Point closestPoint = positions.nearest(lon, lat);

        return point2node.get(closestPoint).id();


        /*
        double minDist = Double.POSITIVE_INFINITY;
        Node closestNode = null;
        for (Node aNode : this.getNodes()) {
            if (dist(aNode, lon, lat) < minDist) {
                minDist = dist(aNode, lon, lat);
                closestNode = aNode;
            }
        }
        return closestNode.id();
        */
    }

    private static double dist(Node n, double lon, double lat) {
        double deltaLon = n.lon() - lon,
                deltaLat = n.lat() - lat;
        return Math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon);
    }

    @Override
    public List<WeightedEdge<Long>> neighbors(Long v) {
        return super.neighbors(v);
    }

    /**
     * For Project Part III (gold points)
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        List<String> fullNames = new LinkedList<>();
        String cleanPrefix = cleanString(prefix);
        // System.out.println("Trying to find locations with clean prefix: " + cleanPrefix);
        LinkedList<String> cleanResults = (LinkedList<String>) locations.keysWithPrefix(cleanPrefix);
        for (Node n : getNodes()) {
            if (n.name() != null) {
                for (String aCleanResult : cleanResults) {
                    if (aCleanResult.equals(cleanString(n.name()))) {
                        fullNames.add(n.name());
                    }
                }
            }
        }
        // System.out.println("Full names: " + fullNames);
        return fullNames;
    }

    /**
     * For Project Part III (gold points)
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {
        List<Map<String, Object>> results = new LinkedList<>();
        LinkedList<String> cleanResults = (LinkedList<String>) locations.keysWithPrefix(cleanString(locationName));
        for (Node n : getNodes()) {
            if (n.name() != null) {
                for (String aCleanResult : cleanResults) {
                    if (aCleanResult.equals(cleanString(n.name()))){
                        Map<String, Object> subRes = new HashMap<>();
                        subRes.put("lat", n.lat());
                        subRes.put("lon", n.lon());
                        subRes.put("name", n.name());
                        subRes.put("id", n.id());
                        results.add(subRes);
                    }
                }
            }
        }
        return results;
    }


    /**
     * Useful for Part III. Do not modify.
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

}
