import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    private static PriorityQueue<SearchNode> fringe;
    private static HashSet<Long> visited;
    private static SearchNode goalNode;


    private static class SearchNode implements Comparable<SearchNode> {
        private Long nodeId;
        private double backwardCost;
        private double forwardCost;
        private SearchNode prev;

        private SearchNode(Long init, double num, double heur, SearchNode pre) {
            this.nodeId = init;
            this.backwardCost = num;
            this.forwardCost = heur;
            this.prev = pre;
        }

        @Override
        public int compareTo(SearchNode n) {
            double thisPriority = this.backwardCost + this.forwardCost;
            double thatPriority = n.backwardCost + n.forwardCost;
            if (thisPriority > thatPriority) {
                return 1;
            }
            if (thisPriority < thatPriority) {
                return -1;
            }
            return 0;
        }

        public String toString() {
            return "[" + this.nodeId + "]";
        }

    }

    /**
     * Return a List of longs representing the shortest path from the node
     * closest to a start location and the node closest to the destination
     * location.
     * @param g The graph to use.
     * @param stlon The longitude of the start location.
     * @param stlat The latitude of the start location.
     * @param destlon The longitude of the destination location.
     * @param destlat The latitude of the destination location.
     * @return A list of node id's in the order visited on the shortest path.
     */
    public static List<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                          double destlon, double destlat) {
        visited = new HashSet<>();
        fringe = new PriorityQueue<>();
        long startNodeId = g.closest(stlon, stlat);
        long destNodeId = g.closest(destlon, destlat);
        SearchNode startNode = new SearchNode(startNodeId,
                0.0, g.getForwardCost(startNodeId, destNodeId), null);

        fringe.add(startNode);

        while (!fringe.isEmpty()) {
            SearchNode currNode = fringe.poll();
            visited.add(currNode.nodeId);

            if (isDestination(currNode.nodeId, destNodeId)) {
                goalNode = currNode;
                return solutionList();
            }
            for (Long nodeId: g.adjacent(currNode.nodeId)) {
                if (!visited.contains(nodeId)) {
                    double currCost = currNode.backwardCost + g.distance(nodeId, currNode.nodeId);
                    g.setCurrCost(nodeId, currCost);
                    double heur = g.getForwardCost(nodeId, destNodeId);
                    SearchNode succNode = new SearchNode(nodeId, currCost, heur, currNode);
                    fringe.add(succNode);
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * Returns the solution in an ArrayList
     * @return an ArrayList containing a list
     */
    private static ArrayList<Long> solutionList() {
        ArrayList<Long> path = new ArrayList<>();
        Stack<Long> solIterable = solutionStack();
        for (int i = solIterable.size() - 1; i >= 0; i--) {
            path.add(solIterable.get(i));
        }
        return path;
    }


    /**
     * returns the solution in a stack (reversed order)
     * @return a Stack<Long>
     */
    private static Stack<Long> solutionStack() {
        if (goalNode != null) {
            Stack<Long> sol = new Stack<>();
            sol.push(goalNode.nodeId);
            SearchNode temp = goalNode;
            while (temp.prev != null) {
                sol.push(temp.prev.nodeId);
                temp = temp.prev;
            }
            return sol;
        }
        return new Stack<>();
    }

    /**
     * Returns whether given node is already destination
     * @param sNode test node
     * @param dNode destination node
     * @return true if it's destination
     * false if not
     */
    private static boolean isDestination(long sNode, long dNode) {
        return sNode == dNode;
    }



    /**
     * Create the list of directions corresponding to a route on the graph.
     * @param g The graph to use.
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.
     * @return A list of NavigatiionDirection objects corresponding to the input
     * route.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {

        ArrayList<NavigationDirection> naviDirections = new ArrayList<>();

        //Initiate initial nodes
        long prevNodeId = route.get(0);

        String prevStreetName = g.getNodeName(prevNodeId);
        String startStreetName = prevStreetName;
        NavigationDirection currNav = new NavigationDirection();
        double currDis = 0;
        setStreetNameInfo(prevStreetName, currNav);

        // Process preceding nodes
        for (int i = 1; i < route.size(); i++) {
            long currNodeId = route.get(i);
            currDis += g.distance(currNodeId, prevNodeId);

            String currStreetName = g.getNodeName(currNodeId);
            if (g.containsStreetName(currNodeId, prevStreetName)
                    && !prevStreetName.equals("")) {
                currStreetName = prevStreetName;
            }

            if (currStreetName.equals("")) {
                currStreetName = NavigationDirection.UNKNOWN_ROAD;
            }


            // Create new NavigationDirection instance when there is
            // a street name change
            if (!currStreetName.equals(prevStreetName)) {

                setDistanceInfo(currDis, currNav);

                // Calculate direction
                int direction = getDirection(g, prevNodeId, currNodeId);
                if (startStreetName.equals(prevStreetName)) {
                    direction = 0;
                }
                setDirectionInfo(direction, currNav);

                // Add this new navigation direction to the returning list
                naviDirections.add(currNav);
                currDis = 0;

                // Create new NavigationDIrection instance, begin
                // a new round of data collection
                currNav = new NavigationDirection();
                setStreetNameInfo(currStreetName, currNav);
                prevStreetName = currStreetName;
            }
            prevNodeId = currNodeId;
        }

        long currNodeId = route.get(route.size() - 1);
        int direction = getDirection(g, currNodeId, prevNodeId);
        setDirectionInfo(direction, currNav);
        setDistanceInfo(currDis, currNav);
        naviDirections.add(currNav);
        return naviDirections;
    }

    /**
     * Sets street name information to the NavigationDirection instance
     * @param streetName street name to be changes
     * @param nav NavigationDirection instance
     */
    private static void setStreetNameInfo(String streetName, NavigationDirection nav) {
        nav.way = streetName;
    }

    /**
     * Sets street name information to the NavigationDirection instance
     * @param distance distance to be changed
     * @param nav NavigationDirection instance
     */
    private static void setDistanceInfo(double distance, NavigationDirection nav) {
        nav.distance = distance;
    }

    /**
     * Sets street name information to the NavigationDirection instance
     * @param direction direction to be changed
     * @param nav NavigationDirection instance
     */
    private static void setDirectionInfo(int direction, NavigationDirection nav) {
        nav.direction = direction;
    }

    /**
     * Gets the direction value using GraphDB.bearing method
     * @param g GraphDB instance
     * @param curr current node id
     * @param next next node id
     * @return an integer value that corresponds to one of the
     *         NavigationDirection direction constant
     */
    private static int getDirection(GraphDB g, long curr, long next) {
        double angle = g.bearing(next, curr);
        if (angle <= 15 && angle >= -15) {
            return NavigationDirection.STRAIGHT;
        }
        if (angle > 15 && angle <= 30) {
            return NavigationDirection.SLIGHT_LEFT;
        }
        if (angle < -15 && angle >= -30) {
            return NavigationDirection.SLIGHT_RIGHT;
        }
        if (angle > 30 && angle <= 100) {
            return NavigationDirection.LEFT;
        }
        if (angle < -30 && angle >= -100) {
            return NavigationDirection.RIGHT;
        }
        if (angle >= 100) {
            return NavigationDirection.SHARP_LEFT;
        }
        return NavigationDirection.SHARP_RIGHT;
    }


    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** Integer constants representing directions. */
        public static final int START = 0;
        public static final int STRAIGHT = 1;
        public static final int SLIGHT_LEFT = 2;
        public static final int SLIGHT_RIGHT = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;
        public static final int SHARP_LEFT = 6;
        public static final int SHARP_RIGHT = 7;

        /** Number of directions supported. */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        /** Default name for an unknown way. */
        public static final String UNKNOWN_ROAD = "unknown road";
        
        /** Static initializer. */
        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /** The direction a given NavigationDirection represents.*/
        int direction;
        /** The name of the way I represent. */
        String way;
        /** The distance along this way I represent. */
        double distance;

        /**
         * Create a default, anonymous NavigationDirection.
         */
        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0.0;
        }

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Takes the string representation of a navigation direction and converts it into
         * a Navigation Direction object.
         * @param dirAsString The string representation of the NavigationDirection.
         * @return A NavigationDirection object representing the input string.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                    && way.equals(((NavigationDirection) o).way)
                    && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }

}
