import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    private String[][] renderGrid;
    private double rasterUlLon;
    private double rasterUlLat;
    private double rasterLrLon;
    private double rasterLrLat;
    private int depth;
    private double queryLonDpp;
    private boolean querySuccess;
    private double blockLonDpp;
    private double blockLatDpp;


    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "renderGrid"   : String[][], the files to display. <br>
     * "rasterUlLon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "rasterUlLat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "rasterLrLon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "rasterLrLat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image;
     *                    can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "querySuccess" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        System.out.println(params);
        Map<String, Object> results;

        // Parse query variables
        double lrlon = params.get("lrlon");
        double lrlat = params.get("lrlat");
        double ullon = params.get("ullon");
        double ullat = params.get("ullat");
        double queryWidth = params.get("w");
        double queryHeight = params.get("h");

        //Process query variables
        this.queryLonDpp = getLonDpp(lrlon, ullon, queryWidth);
        this.depth = getDepth();
        int ulX = getUpperLeftXY(ullon, ullat)[0];
        int ulY = getUpperLeftXY(ullon, ullat)[1];
        int lrX = getLowerRightXY(lrlon, lrlat)[0];
        int lrY = getLowerRightXY(lrlon, lrlat)[1];
        this.renderGrid = getGrid(ulX, ulY, lrX, lrY);
        double[] boundries = getBoundryBox(ulX, ulY, lrX, lrY);
        this.rasterUlLon = boundries[0];
        this.rasterUlLat = boundries[1];
        this.rasterLrLon = boundries[2];
        this.rasterLrLat = boundries[3];

        //Finalize the query process
        if (ullon > MapServer.ROOT_LRLON || ullat < MapServer.ROOT_LRLAT
                || lrlon < MapServer.ROOT_ULLON || lrlat > MapServer.ROOT_ULLAT) {
            this.querySuccess = false;
        } else {
            this.querySuccess = true;
        }
        results = getReturnedParams();
        return results;
    }

    /**
     * Returns the longitudinal distance per pixel of a given area
     * @param lowerRightLon lower right longitude of the given area
     * @param upperLeftLon upper left longitude of the given area
     * @param width width of the given area
     * @return a double which evaluates to the LonDPP
     */
    private double getLonDpp(double lowerRightLon, double upperLeftLon, double width) {
        return (lowerRightLon - upperLeftLon) / width;
    }

    /**
     * Returns the longitudinal distance per pixel of given depth
     * @param lowerRightLon lower right longitude of the given area
     * @param upperLeftLon upper left longitude of the given area
     * @param width width of the given area
     * @param inputDepth depth of the map area
     * @return a double which evaluates to the LonDPP
     */
    private double getLonDpp(double lowerRightLon, double upperLeftLon,
                             double width, int inputDepth) {
        return (lowerRightLon - upperLeftLon) / (width * Math.pow(2, inputDepth));
    }

    /**
     * Returns the latitudinal distance per pixel of a given area
     * @param lowerRightLat lower right latitude of the given area
     * @param upperLeftLat upper left latitude of the given area
     * @param height height of the given area
     * @return a double which evaluates to the Latitude distance per pixel
     */
    private double getLatDpp(double lowerRightLat, double upperLeftLat, double height) {
        return (upperLeftLat - lowerRightLat) / height;
    }

    /**
     * Returns the latitudinal distance per pixel of a given depth
     * @param lowerRightLat lower right latitude of the given area
     * @param upperLeftLat upper left latitude of the given area
     * @param height height of the given area
     * @param inputDepth depth of the map
     * @return a double which evaluates to the Latitude distance per pixel
     */
    private double getLatDpp(double lowerRightLat, double upperLeftLat,
                             double height, int inputDepth) {
        return (upperLeftLat - lowerRightLat) / (height * Math.pow(2, inputDepth));
    }

    /**
     * Given a query, returns the depth of the map that the backend should return
     * @return an integer that represents the depth of the returning map
     */
    private int getDepth() {
        int level = 0;
        double currLonDpp = getLonDpp(MapServer.ROOT_LRLON, MapServer.ROOT_ULLON,
                MapServer.TILE_SIZE);
        double currLatDpp = getLatDpp(MapServer.ROOT_LRLAT, MapServer.ROOT_ULLAT,
                MapServer.TILE_SIZE);
        while (currLonDpp > this.queryLonDpp) {
            level++;
            if (level >= 7) {
                this.blockLonDpp = getLonDpp(MapServer.ROOT_LRLON, MapServer.ROOT_ULLON,
                        MapServer.TILE_SIZE, 7);
                this.blockLatDpp = getLatDpp(MapServer.ROOT_LRLAT, MapServer.ROOT_ULLAT,
                        MapServer.TILE_SIZE, 7);
                return 7;
            }
            currLonDpp = currLonDpp / 2;
            currLatDpp = currLatDpp / 2;
        }
        this.blockLonDpp = getLonDpp(MapServer.ROOT_LRLON, MapServer.ROOT_ULLON,
                MapServer.TILE_SIZE, level);
        this.blockLatDpp = getLatDpp(MapServer.ROOT_LRLAT, MapServer.ROOT_ULLAT,
                MapServer.TILE_SIZE, level);
        return level;
    }

    /**
     * Given a query upper-left longitude and latitude,
     * returns the x, y index of the map image file
     * @param queryULLon query upper-left longitude
     * @param queryULLat query upper-left latitdue
     * @return two integers that represents the
     * upper-left corner map image file in format of "dD_xj_yk"
     */
    public int[] getUpperLeftXY(double queryULLon, double queryULLat) {
        if (queryULLon < MapServer.ROOT_ULLON) {
            queryULLon = MapServer.ROOT_ULLON;
        }
        if (queryULLat > MapServer.ROOT_ULLAT) {
            queryULLat = MapServer.ROOT_ULLAT;
        }
        int disToLeftSideInPixel
                = (int) Math.ceil((queryULLon - MapServer.ROOT_ULLON) / this.blockLonDpp);
        int disToUpSideInPixel
                = (int) Math.ceil((MapServer.ROOT_ULLAT - queryULLat) / this.blockLatDpp);

        int x = disToLeftSideInPixel / MapServer.TILE_SIZE;

        int y = disToUpSideInPixel / MapServer.TILE_SIZE;

        int[] coord = {x, y};
        return coord;
    }

    /**
     * Given a query lower-right longitude and latitude,
     * returns the x, y index of the map image file
     * @param queryLRLon query lower- right longitude
     * @param queryLRLat query lower-right latitdue
     * @return two integers that represents the lower-right
     * corner map image file in format of "dD_xj_yk"
     */
    public int[] getLowerRightXY(double queryLRLon, double queryLRLat) {
        if (queryLRLon > MapServer.ROOT_LRLON) {
            queryLRLon = MapServer.ROOT_LRLON;
        }
        if (queryLRLat < MapServer.ROOT_LRLAT) {
            queryLRLat = MapServer.ROOT_LRLAT;
        }
        int disToLeftSideInPixel
                = (int) Math.ceil((queryLRLon - MapServer.ROOT_ULLON) / this.blockLonDpp);
        int disToUpSideInPixel
                = (int) Math.ceil((MapServer.ROOT_ULLAT - queryLRLat) / this.blockLatDpp);
        int x = 0, y = 0;

        if (disToLeftSideInPixel != 0) {
            x = (disToLeftSideInPixel - 1) / MapServer.TILE_SIZE;
        }
        if (disToUpSideInPixel != 0) {
            y = (disToUpSideInPixel - 1) / MapServer.TILE_SIZE;
        }
        int[] coord = {x, y};
        return coord;
    }

    /**
     * Given the index of lower-right corner and upper-left corners, returns
     * the corresponding string grid that represents all required .png files
     * @param upperLeftX x index of the upper-left corner
     * @param upperLeftY y index of the upper-left corner
     * @param lowerRightX x index of the lower-right corner
     * @param lowerRightY y index of the lower-right corner
     * @return
     */
    private String[][] getGrid(int upperLeftX, int upperLeftY, int lowerRightX, int lowerRightY) {
        int width = lowerRightX - upperLeftX + 1;
        int height = lowerRightY - upperLeftY + 1;
        String[][] grid = new String[height][width];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grid[j][i] = "d" + this.depth + "_x"
                        + (i + upperLeftX) + "_y" + (upperLeftY + j) + ".png";
            }
        }
        return grid;
    }

    /**
     * Returns an double type array that contains the boundary lon & lat information
     * @param upperLeftX x index of the upper-left corner
     * @param upperLeftY y index of the upper-left corner
     * @param lowerRightX x index of the lower-right corner
     * @param lowerRightY y index of the lower-right corner
     * @return
     */
    private double[] getBoundryBox(int upperLeftX, int upperLeftY,
                                  int lowerRightX, int lowerRightY) {
        int numBlockRow = (int) Math.pow(2, this.depth);
        double rasUlLon = MapServer.ROOT_ULLON
                + upperLeftX * MapServer.TILE_SIZE * this.blockLonDpp;
        double rasUlLat = MapServer.ROOT_ULLAT
                - upperLeftY * MapServer.TILE_SIZE * this.blockLatDpp;
        double rasLrLon = MapServer.ROOT_LRLON
                - (numBlockRow - lowerRightX - 1) * MapServer.TILE_SIZE * this.blockLonDpp;
        double rasLrLat = MapServer.ROOT_LRLAT
                + (numBlockRow - lowerRightY - 1) * MapServer.TILE_SIZE * this.blockLatDpp;
        double[] boundry = {rasUlLon, rasUlLat, rasLrLon, rasLrLat};
        return boundry;
    }

    /**
     * Produce the final parameters that is to be passed into MapServer.java
     * @return a HashMap that contains all required parameters
     */
    private Map<String, Object> getReturnedParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("raster_ul_lon", this.rasterUlLon);
        params.put("raster_ul_lat", this.rasterUlLat);
        params.put("raster_lr_lon", this.rasterLrLon);
        params.put("raster_lr_lat", this.rasterLrLat);
        params.put("render_grid", this.renderGrid);
        params.put("query_success", this.querySuccess);
        params.put("depth", this.depth);
        return params;
    }

}
