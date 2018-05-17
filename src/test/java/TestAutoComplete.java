import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;



public class TestAutoComplete {
    private static final String RESULTS_FILE = "autocomplete_result.txt";
    private static final String OSM_DB_PATH = "../library-sp18/data/berkeley-2018.osm.xml";
    private static GraphDB graph;

    @Before
    public void setUp() throws Exception {
        graph = new GraphDB(OSM_DB_PATH);

    }
    @Test
    public void testAutoCompleteWithOrder() throws Exception {
        ArrayList<String> expected = locationsFromFile();
        ArrayList<String> actual = graph.getLocationsByPrefix("c");
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            String expect = expected.get(i);
            String res = actual.get(i);
            assertTrue(expect.equals(res));
        }
    }

    @Test
    public void testAutoCompleteWithoutOrder() throws Exception {
        ArrayList<String> expected = locationsFromFile();
        ArrayList<String> actual = graph.getLocationsByPrefix("c");

        HashSet<String> expMap = new HashSet<>(expected);
        HashSet<String> actMap = new HashSet<>(actual);
        System.out.println(expMap);
        System.out.println(actMap);

        int size = expected.size();
        for (int i = 0; i < size; i++) {
            String toBeTested = expected.get(i);
            if (expMap.contains(toBeTested) && actMap.contains(toBeTested)) {
                expMap.remove(toBeTested);
                actMap.remove(toBeTested);
            }
        }
        assertTrue(expMap.isEmpty() && actMap.isEmpty());
    }


    private ArrayList<String> locationsFromFile() throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(RESULTS_FILE), Charset.defaultCharset());
        ArrayList<String> returnedList = new ArrayList<>();
        String locations = lines.get(0);
        String[] locationArray = locations.split(", ");
        for (String s: locationArray) {
            if ((!s.equals("Choice Meat Market Wholesale Retail Packaging"))
                    && (!s.equals("Restaurant Supplies"))) {
                returnedList.add(s);
            }
        }
        returnedList.add("Choice Meat Market Wholesale Retail Packaging, Restaurant Supplies");
        return returnedList;
    }
}
