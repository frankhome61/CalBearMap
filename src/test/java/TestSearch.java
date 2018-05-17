import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestSearch {
    private static final String OSM_DB_PATH = "../library-sp18/data/berkeley-2018.osm.xml";
    private static GraphDB graph;

    @Before
    public void setUp() throws Exception {
        graph = new GraphDB(OSM_DB_PATH);
    }

    @Test
    public void testSearch() {
        ArrayList<Map<String, Object>> r = graph.getLocations("kirala");
        ArrayList<Map<String, Object>> e = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Kirala");
        map.put("id", (long) 368199832);
        map.put("lon", -122.266771);
        map.put("lat", 37.859241);
        e.add(map);
        System.out.println(r);

        assertEquals(e.size(), r.size());
        for (int i = 0; i < e.size(); i++) {
            Map<String, Object> expected = e.get(i);
            Map<String, Object> result = r.get(i);
            assertTrue(expected.get("name").equals(result.get("name")));
            assertTrue(expected.get("id").equals(result.get("id")));
            assertTrue(expected.get("lon").equals(result.get("lon")));
            assertTrue(expected.get("lat").equals(result.get("lat")));
        }
    }

    @Test
    public void testSearch2() {
        ArrayList<Map<String, Object>> r = graph.getLocations("ez stop deli");
        ArrayList<Map<String, Object>> e = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("name", "E-Z Stop Deli");
        map.put("id", 2235910278l);
        map.put("lon", -122.267607);
        map.put("lat", 37.868756);
        e.add(map);

        assertEquals(e.size(), r.size());
        for (int i = 0; i < e.size(); i++) {
            Map<String, Object> expected = e.get(i);
            Map<String, Object> result = r.get(i);
            assertTrue(expected.get("name").equals(result.get("name")));
            assertTrue(expected.get("id").equals(result.get("id")));
            assertTrue(expected.get("lon").equals(result.get("lon")));
            assertTrue(expected.get("lat").equals(result.get("lat")));
        }
    }

    @Test
    public void testSearch3() {
        ArrayList<Map<String, Object>> r = graph.getLocations("dwight cresent  th st");
        ArrayList<Map<String, Object>> e = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Dwight Cresent & 7th St");
        map.put("id", 2060883684);
        map.put("lon", -122.2943629);
        map.put("lat", 37.860424);
        e.add(map);
        System.out.println(r);
        System.out.println(e);

        assertEquals(e.size(), r.size());
        for (int i = 0; i < e.size(); i++) {
            Map<String, Object> expected = e.get(i);
            Map<String, Object> result = r.get(i);
            assertTrue(expected.get("name").equals(result.get("name")));
            assertTrue(expected.get("id").equals(result.get("id")));
            assertTrue(expected.get("lon").equals(result.get("lon")));
            assertTrue(expected.get("lat").equals(result.get("lat")));
        }
    }

    @Test
    public void testSearchPetFood() {
        ArrayList<Map<String, Object>> r = graph.getLocations("petfood express");
        ArrayList<Map<String, Object>> e = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("name", "PetFood Express");
        map.put("id", 1669797918);
        map.put("lon", -122.2919654);
        map.put("lat", 37.8693438);
        e.add(map);
        System.out.println(r);
        System.out.println(e);

        assertEquals(e.size(), r.size());
        for (int i = 0; i < e.size(); i++) {
            Map<String, Object> expected = e.get(i);
            Map<String, Object> result = r.get(i);
            assertTrue(expected.get("name").equals(result.get("name")));
            assertTrue(expected.get("lon").equals(result.get("lon")));
            assertTrue(expected.get("lat").equals(result.get("lat")));
        }
    }

    @Test
    public void testSearchEleven() {
        ArrayList<Map<String, Object>> r = graph.getLocations("eleven");
        System.out.println(r);
        assertEquals(2, r.size());

    }



}
