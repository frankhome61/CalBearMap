import org.junit.Test;
import java.util.HashMap;
import java.util.Map;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestMiscellaneous {
//    @Test
//    public void testDepthTest() {
//        Rasterer rasterer = new Rasterer();
//        Map<String, Double> params = new HashMap();
//        params.put("lrlon", -122.24053369025242);
//        params.put("ullon", -122.24163047377972);
//        params.put("w", 892.0);
//        params.put("h", 875.0);
//        params.put("ullat", 37.87655856892288);
//        params.put("lrlat", 37.87548268822065);
//        rasterer.getMapRaster(params);
//        assertEquals(7, rasterer.getDepth());
//    }

//    @Test
//    public void testDepth1234() {
//        Rasterer rasterer = new Rasterer();
//        Map<String, Double> params = new HashMap();
//        params.put("lrlon", -122.20908713544797);
//        params.put("ullon", -122.3027284165759);
//        params.put("w", 305.0);
//        params.put("h", 300.0);
//        params.put("ullat", 37.88708748276975);
//        params.put("lrlat", 37.848731523430196);
//        rasterer.getMapRaster(params);
//        assertEquals(1, rasterer.getDepth());
//    }

//    @Test
//    public void testDepthTwleveImages() {
//        Rasterer rasterer = new Rasterer();
//        Map<String, Double> params = new HashMap();
//        params.put("lrlon", -122.2104604264636);
//        params.put("ullon", -122.30410170759153);
//        params.put("w", 1091.0);
//        params.put("h", 566.0);
//        params.put("ullat", 37.870213571328854);
//        params.put("lrlat", 37.8318576119893);
//        rasterer.getMapRaster(params);
//        assertEquals(2, rasterer.getDepth());
//    }

//    @Test
//    public void testDepthFromSrcTest() {
//        Rasterer rasterer = new Rasterer();
//        Map<String, Double> params = new HashMap();
//        params.put("lrlon", -122.23108224034448);
//        params.put("ullon", -122.2325986736921);
//        params.put("w", 498.0);
//        params.put("h", 691.0);
//        params.put("ullat", 37.840256238827735);
//        params.put("lrlat", 37.83815211143175);
//        rasterer.getMapRaster(params);
//        assertEquals(7, rasterer.getDepth());
//    }

//    @Test
//    public void testDepthTwleveULCorner() {
//        Rasterer rasterer = new Rasterer();
//        Map<String, Double> params = new HashMap();
//        params.put("lrlon", -122.2104604264636);
//        params.put("ullon", -122.30410170759153);
//        params.put("w", 1091.0);
//        params.put("h", 566.0);
//        params.put("ullat", 37.870213571328854);
//        params.put("lrlat", 37.8318576119893);
//        rasterer.getMapRaster(params);
//        int coord[] = rasterer.getUpperLeftXY(-122.30410170759153, 37.870213571328854);
//        int x =  coord[0];
//        int y = coord[1];
//        assertEquals(0, x);
//        assertEquals(1, y);
//    }

//    @Test
//    public void testDepthTwleveLRCorner() {
//        Rasterer rasterer = new Rasterer();
//        Map<String, Double> params = new HashMap();
//        params.put("lrlon", -122.2104604264636);
//        params.put("ullon", -122.30410170759153);
//        params.put("w", 1091.0);
//        params.put("h", 566.0);
//        params.put("ullat", 37.870213571328854);
//        params.put("lrlat", 37.8318576119893);
//        rasterer.getMapRaster(params);
//        int coord[] = rasterer.getLowerRightXY(-122.2104604264636, 37.8318576119893);
//        int x =  coord[0];
//        int y = coord[1];
//        assertEquals(3, x);
//        assertEquals(3, y);
//    }


//    @Test
//    public void testReturnedBoundryBox() {
//        Rasterer rasterer = new Rasterer();
//        Map<String, Double> params = new HashMap();
//        params.put("lrlon", -122.2104604264636);
//        params.put("ullon", -122.30410170759153);
//        params.put("w", 1091.0);
//        params.put("h", 566.0);
//        params.put("ullat", 37.870213571328854);
//        params.put("lrlat", 37.8318576119893);
//        rasterer.getMapRaster(params);
//        double[] expected = {-122.2998046875, 37.87484726881516,
//                -122.2119140625, 37.82280243352756};
//
//        double actual[] = rasterer.getBoundryBox(0, 1, 3, 3);
//        for (int i = 0; i < 4; i++) {
//            assertTrue(expected[i] == actual[i]);
//        }
//    }

    @Test
    public void testReturnedParams() {
        Rasterer rasterer = new Rasterer();
        Map<String, Double> params = new HashMap();
        params.put("lrlon", -122.2104604264636);
        params.put("ullon", -122.30410170759153);
        params.put("w", 1091.0);
        params.put("h", 566.0);
        params.put("ullat", 37.870213571328854);
        params.put("lrlat", 37.8318576119893);
        Map<String, Object> result = rasterer.getMapRaster(params);
        System.out.println(result.toString());
        System.out.println("{raster_ul_lon=-122.2998046875, depth=2, raster_lr_lon=-122.2119140625, raster_lr_lat=37.82280243352756, render_grid=[[d2_x0_y1.png, d2_x1_y1.png, d2_x2_y1.png, d2_x3_y1.png], [d2_x0_y2.png, d2_x1_y2.png, d2_x2_y2.png, d2_x3_y2.png], [d2_x0_y3.png, d2_x1_y3.png, d2_x2_y3.png, d2_x3_y3.png]], raster_ul_lat=37.87484726881516, query_success=true}");
    }

    @Test
    public void testTestHtml() {
        Rasterer rasterer = new Rasterer();
        Map<String, Double> params = new HashMap();
        params.put("lrlon", -122.24053369025242);
        params.put("ullon", -122.24163047377972);
        params.put("w", 892.0);
        params.put("h", 875.0);
        params.put("ullat", 37.87655856892288);
        params.put("lrlat", 37.87548268822065);
        Map<String, Object> result = rasterer.getMapRaster(params);

        Map<String, Object> expected = new HashMap<>();
        expected.put("raster_ul_lon", -122.24212646484375);
        expected.put("depth", 7);
        expected.put("raster_lr_lon", -122.24006652832031);
        expected.put("raster_lr_lat", 37.87538940251607);
        String[][] grid = {{"d7_x84_y28.png", "d7_x85_y28.png", "d7_x86_y28.png"},
                          {"d7_x84_y29.png", "d7_x85_y29.png", "d7_x86_y29.png"},
                          {"d7_x84_y30.png", "d7_x85_y30.png", "d7_x86_y30.png"}};
        expected.put("render_grid", grid);
        expected.put("raster_ul_lat", 37.87701580361881);
        expected.put("query_success", true);

        assertEquals(84, rasterer.getUpperLeftXY(-122.24163047377972, 37.87655856892288)[0]);
        assertEquals(28, rasterer.getUpperLeftXY(-122.24163047377972, 37.87655856892288)[1]);
        assertEquals(86, rasterer.getLowerRightXY(-122.24053369025242, 37.87548268822065)[0]);
        assertEquals(30, rasterer.getLowerRightXY(-122.24053369025242, 37.87548268822065)[1]);

        String[][] actualGrid = (String[][])result.get("render_grid");
        for (int i = 0; i < grid.length; i++) {
            assertArrayEquals(grid[i], actualGrid[i]);
        }

        assertEquals(expected.get("raster_ul_lon"), result.get("raster_ul_lon"));
        assertEquals(expected.get("depth"), result.get("depth"));
        assertEquals(expected.get("raster_lr_lon"), result.get("raster_lr_lon"));
        assertEquals(expected.get("raster_lr_lat"), result.get("raster_lr_lat"));
        assertEquals(expected.get("raster_ul_lat"), result.get("raster_ul_lat"));
        assertEquals(expected.get("query_success"), result.get("query_success"));
    }

}
