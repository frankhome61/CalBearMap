import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Queue;

/**
 * Inspired by the Trie implementation from CS61B 2017 Iteration
 * @source CS 61B Spring 2017 Trie Lecture Slides <a href=
 * "https://docs.google.com/presentation/d/1ZDobmVNNZIayrgDUZzZVM-t7yr6ZZCkWBhC6aEjtC04/edit"></a>
 */
public class TrieSet {

    private static int r;
    /**
     * Helper Node class that stores the actual information
     */
    private class Node {
        char nodeName;
        boolean exists;
        HashSet<String> values;
        Node[] links;

        /**
         * Default constructor of the Node class
         */
        public Node() {
            links = new Node[r];
            exists = false;
            values = new HashSet<>();
        }
    }
    private Node root;

    /**
     * Constructor of the TrieSet
     * @param num number of elements in Node.links
     */
    public TrieSet(int num) {
        this.r = num;
        this.root = new Node();
    }


    /**
     * Puts the key into the Trie
     * @param key key to be stored
     */
    public void put(String key, String value) {
        put(root, key, 0, (char) 00, value);
    }

    /**
     * Helper method for the put() method.
     * @param x The node to put the character
     * @param key key to be put into the trie
     * @param d the depth of the current Trie branch
     * @param name the node name
     * @return root of the branch at depth d
     */
    private Node put(Node x, String key, int d, char name, String value) {
        if (x == null) {
            x = new Node();
            x.nodeName = name;
        }

        if (d == key.length()) {
            x.exists = true;
            x.values.add(value);
            return x;
        }

        char c = key.charAt(d);
        x.links[c] = put(x.links[c], key, d + 1, c, value);
        return x;
    }

    /**
     * Returns a list of all possible words that corresponds to the prefix
     * @param prefix query prefix
     * @return a list of words that matches the prefix
     */
    public ArrayList<String> getAll(String prefix) {
        Queue<String> results = new LinkedList<>();
        Node x = get(root, prefix, 0);
        getHelper(x, new StringBuilder(prefix), results);
        ArrayList<String> res = new ArrayList<>(results);
        return res;
    }

    /**
     * Helper Method for get()
     * @param x current node
     * @param prefix prefix to be searched
     * @param results results to be returned by get()
     */
    private void getHelper(Node x, StringBuilder prefix, Queue<String> results) {
        if (x == null) {
            return;
        }
        if (x.exists) {
            for (String s: x.values) {
                results.add(s);
            }
        }
        for (char c = 0; c < r; c++) {
            prefix.append(c);
            getHelper(x.links[c], prefix, results);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }


    /**
     * Helper method for getting specific key
     * @param x current node
     * @param key search key
     * @param d search depth
     * @return the current node
     */
    private Node get(Node x, String key, int d) {
        if (x == null) {
            return null;
        }
        if (d == key.length()) {
            return x;
        }
        char c = key.charAt(d);
        return get(x.links[c], key, d + 1);
    }
}
