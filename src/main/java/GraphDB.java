import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;



/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {

    Map<Long, Node> nodes = new LinkedHashMap<>();
    private HashSet<Node> unconnected = new HashSet<>();
    private HashMap<String, ArrayList<Long>> locations = new HashMap<>();
    private HashMap<Long, Node> locationNodes = new HashMap<>();
    private TrieSet trie = new TrieSet(128);


    private final double BINNUM = 10d;
    private Map<Double, HashSet<Node>> partitionedNodes = new HashMap<>();
    /**
     * Helper Node class that stores each node in the given XML file
     * id: node id
     * lat: latitude of the node
     * lon: longitude of the node
     * adj: the adjacency list of the node, listing all adjacent nodes
     */
    class Node {
        private long id;
        private double lat;
        private double lon;
        private double distToStart;
        private String wayName;
        private String locationName;
        private ArrayList<Node> adj;
        private HashSet<String> wayNames;

        /**
         * Constructor of the Node class
         * @param inputId value of node id, parsed from OSM XML file
         * @param inputLat latitude of the node, parsed from OSM XML file
         * @param inputLon longitude of the node, parsed from OSM XML fild
         */
        Node(long inputId, double inputLat, double inputLon) {
            this.id = inputId;
            this.lat = inputLat;
            this.lon = inputLon;
            this.adj = new ArrayList<>();
            this.wayName = "";
            this.wayNames = new HashSet<>();
            this.locationName = null;
            this.distToStart = Double.MAX_VALUE;
        }

        Node(long inputId, double inputLat, double inputLon, String name) {
            this.id = inputId;
            this.lat = inputLat;
            this.lon = inputLon;
            this.adj = new ArrayList<>();
            this.wayName = "";
            this.wayNames = new HashSet<>();
            this.locationName = name;
            this.distToStart = Double.MAX_VALUE;
        }
        public String toString() {
            return "[" + this.id + ", " + this.lon + ", " + this.lat + "]";
        }
    }

    private class Pair {
        private Node pNode;
        private double speed;
        Pair(Node n, double s) {
            pNode = n;
            speed = s;
        }
        Node getNode() {
            return pNode;
        }
        double getSpeed() {
            return speed;
        }
    }

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Adds a node using given parameters (which are in String format)
     * Parses the string first, and construct the node using parsed
     * long values, also adds the node to an unconnected HashSet
     * @param id node id
     * @param lat latitude of the node
     * @param lon longitude of the node
     */
    void addNode(String id, String lat, String lon) {
        long nodeId = Long.parseLong(id);
        double longitude = Double.parseDouble(lon);
        double latitude = Double.parseDouble(lat);
        Node newNode = new Node(nodeId, latitude, longitude);
        long longId = Long.parseLong(id);
        this.nodes.put(longId, newNode);
        this.unconnected.add(newNode);
    }

    void addStreetName(String id, String n) {
        long nodeId = Long.parseLong(id);
        this.nodes.get(nodeId).wayName = n;
    }

    void addStreetNames(String id, String n) {
        long nodeId = Long.parseLong(id);
        this.nodes.get(nodeId).wayNames.add(n);
    }

    /**
     * Add location name to the specified node
     * @param id node id
     * @param n name to be added into the node
     */
    void addLocationName(String id, String n) {
        long nodeId = Long.parseLong(id);
        this.nodes.get(nodeId).locationName = n;
    }

    /**
     * Add location name to the location map
     * The map maps a node id to a list of corresponding nodes
     * @param loca cleaned version of location name (lower case, a-z only)
     * @param id node id
     * @param actualName full length location name
     */
    void addLocations(String loca, String id, String actualName) {
        long nodeId = Long.parseLong(id);
        if (locations.containsKey(loca)) {
            ArrayList<Long> matchings = locations.get(loca);
            matchings.add(nodeId);
            trie.put(loca, actualName);
        } else {
            ArrayList<Long> newPlace = new ArrayList<>();
            newPlace.add(nodeId);
            locations.put(loca, newPlace);
            trie.put(loca, actualName);
        }
    }

    void addLocationNode(String inputId, String inputLat, String inputLon, String inputName) {
        long nodeId = Long.parseLong(inputId);
        double latitude = Double.parseDouble(inputLat);
        double longitude = Double.parseDouble(inputLon);
        Node n = new Node(nodeId, latitude, longitude, inputName);
        this.locationNodes.put(nodeId, n);
    }

    /**
     * Returns true if a node contains the specified street name
     * @param id node id
     * @param n specified street name
     * @return true if the node contains that street name
     * false otherwise
     */
    boolean containsStreetName(long id, String n) {
        long nodeId = id;
        return this.nodes.get(nodeId).wayNames.contains(n);
    }


    /**
     * Connects vertex v to vertex w by adding one vertex to
     * the other vertex's adjacency list
     * @param vid the String format id of vertex V
     * @param wid the String format id of vertex W
     */
    void addEdge(String vid, String wid) {
        long longVid = Long.parseLong(vid);
        long longWid = Long.parseLong(wid);
        Node v = nodes.get(longVid);
        Node w = nodes.get(longWid);
        this.unconnected.remove(v);
        this.unconnected.remove(w);
        ArrayList<Node> vAdj = v.adj;
        ArrayList<Node> wAdj = w.adj;
        vAdj.add(w);
        wAdj.add(v);
    }

    void addEdgeWithSpeed(String vid, String wid, String speed) {
        long longVid = Long.parseLong(vid);
        long longWid = Long.parseLong(wid);
        double sp = parseSpeed(speed);
    }


    ArrayList<Map<String, Object>> getLocations(String queryLocation) {
        String queryCleaned = cleanStringAlter(queryLocation);
        if (!locations.containsKey(queryCleaned)) {
            return new ArrayList<>();
        }
        ArrayList<Map<String, Object>> returnedList = new ArrayList<>();
        ArrayList<Long> matches = locations.get(queryCleaned);
        for (Long id: matches) {
            Map<String, Object> matchingLocationMap = new HashMap<>();
            Node n = locationNodes.get(id);
            if (queryCleaned.equals(cleanStringAlter(n.locationName))) {
                matchingLocationMap.put("lat", n.lat);
                matchingLocationMap.put("lon", n.lon);
                matchingLocationMap.put("name", n.locationName);
                matchingLocationMap.put("id", n.id);
                returnedList.add(matchingLocationMap);
            }
        }
        return returnedList;
    }

    /**
     * Given prefix, returns a complete list that contains the matching
     * locations
     * @param prefix String prefix
     * @return a list of string
     */
    ArrayList<String> getLocationsByPrefix(String prefix) {
        return trie.getAll(prefix);
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanStringAlter(String s) {
        String temp = s.toLowerCase();
        String toBeReturned = "";
        for (int i = 0; i < temp.length(); i++) {
            char c = temp.charAt(i);
            if (c >= 'a' && c <= 'z' || c == ' ') {
                toBeReturned += c;
            }
        }
        return toBeReturned;
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        Iterable<Long> ids = vertices();
        Set<Long> set = (Set) ids;
        for (Node n: this.unconnected) {
            this.nodes.remove(n.id);
            //removeNodeFromPartitioned(n);
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        Set<Long> vertices = this.nodes.keySet();
        return vertices;
    }

    /**
     * Returns ids of all vertices adjacent to v.
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        ArrayList<Long> adjacency = new ArrayList<>();
        Node vNode = nodes.get(v);
        if (vNode == null) {
            return adjacency;
        }
        ArrayList<Node> adjacenyNodes = vNode.adj;
        for (Node n: adjacenyNodes) {
            adjacency.add(n.id);
        }
        return adjacency;
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);
        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        double dist = Double.MAX_VALUE;
        Node closestNode = null;
        for (long id: this.nodes.keySet()) {
            Node n = this.nodes.get(id);
            double newDist = distance(n.lon, n.lat, lon, lat);
            if (newDist < dist) {
                dist = newDist;
                closestNode = n;
            }
        }
        return closestNode.id;
    }

    /**
     * Gets the longitude of a vertex.
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        if (!this.nodes.containsKey(v)) {
            return -1;
        }
        Node n = this.nodes.get(v);
        return n.lon;
    }

    /**
     * Gets the latitude of a vertex.
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        if (!this.nodes.containsKey(v)) {
            return -1;
        }
        Node n = this.nodes.get(v);
        return n.lat;
    }

    /**
     * Given a string format speed value,
     * returns the double type value
     * @param speedString speed value, in the format "xx mph"
     * @return a double value containing only xx
     */
    double parseSpeed(String speedString) {
        double n = 0;
        for (int i = 0; i < speedString.length(); i++) {
            char a = speedString.charAt(i);
            if (Character.isDigit(a)) {
                n = 10 * n + Character.digit(a, 10);
            }
        }
        return n;
    }

    void setCurrCost(Long id, double dist) {
        Node n = nodes.get(id);
        n.distToStart = dist;
    }

    double getForwardCost(Long nodeId, Long destNodeId) {
        Node n = nodes.get(nodeId);
        Node d = nodes.get(destNodeId);
        double stlon = n.lon;
        double stlat = n.lat;
        double destlon = d.lon;
        double destlat = d.lat;
        return distance(stlon, stlat, destlon, destlat);
    }

    double getBackWardCost(Long id) {
        Node n = nodes.get(id);
        return n.distToStart;
    }




    ///////////////// Spacial Hashing Methods////////////////////
    /**
     * Given the longitude and latitude, figure out which
     * map block it belongs to
     * @param lon longitude
     * @param lat latitude
     * @return a bin number
     */
    double binNumber(double lon, double lat) {
        double value = lon + lat;
        return (double) Math.round(value * BINNUM) / BINNUM;
    }

    /**
     * Given a node, figure out which map block it belongs to
     * @param n Node n
     * @return a bin number that represents a map block
     */
    double binNumber(Node n) {
        double lon = n.lon;
        double lat = n.lat;
        double value = lon + lat;
        return (double) Math.round(value * BINNUM) / BINNUM;
    }

    /**
     * Partition the map into BINNUM * BINNUM
     * (currently set to 1000 * 1000) number of blocks
     */
    void initiatePartition(String ullon, String ullat, String lrlon, String lrlat) {
        double ulon = Double.parseDouble(ullon);
        double ulat = Double.parseDouble(ullat);
        double llon = Double.parseDouble(lrlon);
        double llat = Double.parseDouble(lrlat);
        for (double i = ulon - 0.5; i < llon + 0.5; i += 1 / BINNUM) {
            for (double j = llat - 0.5; j < ulat + 0.5; j += 1 / BINNUM) {
                HashSet<Node> newNodeSet = new HashSet<>();
                double binNum = binNumber(i, j);
                this.partitionedNodes.put(binNum, newNodeSet);
            }
        }
    }

    /**
     * Partitions a given node to a set, whereas
     * the set is a
     * @param n
     */
    void partitionNode(Node n) {
        double binNum = binNumber(n);
        HashSet<Node> nodeSet = this.partitionedNodes.get(binNum);
        nodeSet.add(n);
    }

    /**
     * To be called in clean(), remove all unconnected nodes
     * from partitionedNodes as well, since partitionedNodes
     * and nodes essentially contains same elements
     * @param n
     */
    void removeNodeFromPartitioned(Node n) {
        double binNumber = binNumber(n);
        Set<Node> currNodeSet = this.partitionedNodes.get(binNumber);
        currNodeSet.remove(n);
    }

    long alternateClosest(double lon, double lat) {
        double binNum = binNumber(lon, lat);
        HashSet<Node> nodeSet = this.partitionedNodes.get(binNum);
        double dist = Double.MAX_VALUE;
        Node closestNode = null;
        for (Node n: nodeSet) {
            double newDist = distance(n.lon, n.lat, lon, lat);
            if (newDist < dist) {
                dist = newDist;
                closestNode = n;
            }
        }
        return closestNode.id;
    }

    String getNodeName(Long id) {
        Node n = this.nodes.get(id);
        if (n == null || n.wayName.equals("")) {
            return "";
        }
        return n.wayName;
    }

    double getNodeDistanceToStart(Long id) {
        return this.nodes.get(id).distToStart;
    }
}
