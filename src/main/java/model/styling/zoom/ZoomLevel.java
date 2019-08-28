package model.styling.zoom;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Data from <a href=https://wiki.openstreetmap.org/wiki/Zoom_levels>OSM Wiki Zoom Levels</a>
 */
public class ZoomLevel {

    private static final Map<Integer, Double> ZOOM_LEVELS;

    static {
        Map<Integer, Double> map = new HashMap<>();
        map.put(0, 500e6);
        map.put(1, 250e6);
        map.put(2, 150e6);
        map.put(3, 70e6);
        map.put(4, 35e6);
        map.put(5, 15e6);
        map.put(6, 10e6);
        map.put(7, 4e6);
        map.put(8, 2e6);
        map.put(9, 1e6);
        map.put(10, 500e3);
        map.put(11, 250e3);
        map.put(12, 150e3);
        map.put(13, 70e3);
        map.put(14, 35e3);
        map.put(15, 15e3);
        map.put(16, 8e3);
        map.put(17, 4e3);
        map.put(18, 2e3);
        map.put(19, 1e3);
        ZOOM_LEVELS = Collections.unmodifiableMap(map);
    }

    private int zoomLevel;

    public ZoomLevel(int zoomLevel) {
        if (zoomLevel < 0 || zoomLevel >= ZOOM_LEVELS.size()) {
            throw new IllegalArgumentException("Zoom level must be between 0 and " + (ZOOM_LEVELS.size() - 1) + ".");
        }

        this.zoomLevel = zoomLevel;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public double getScaleDenominator() {
        return ZOOM_LEVELS.get(zoomLevel);
    }

    @Override
    public String toString() {
        return "ZoomLevel{" +
                "zoomLevel=" + zoomLevel +
                '}';
    }

}
