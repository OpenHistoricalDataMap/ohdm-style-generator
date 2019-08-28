package model.styling.zoom;

public class ZoomRegion {

    private ZoomLevel minZoom, maxZoom;

    private static final String REGION_PATTERN = "\\b(\\d{1,2}-\\d{1,2})\\b";
    private static final String GREATER_OR_LESS_PATTERN = "([\\u003C\\u003E]\\d{1,2})";

    public static boolean isValidZoomRegion(String string) {
        return string.matches(REGION_PATTERN) || string.matches(GREATER_OR_LESS_PATTERN);
    }

    public static ZoomRegion getEmptyZoomRegion() {
        return new ZoomRegion(null, null);
    }

    public ZoomRegion(ZoomLevel minZoom, ZoomLevel maxZoom) {
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
    }

    public ZoomRegion(String zoomRegionString) throws IllegalArgumentException {
        initialize(zoomRegionString);
    }

    private void initialize(String input) {
        if (input.matches(REGION_PATTERN)) {
            initializeFromRegionPattern(input);
        } else if (input.matches(GREATER_OR_LESS_PATTERN)) {
            initializeFromGreaterOrLessPattern(input);
        } else {
            throw new IllegalArgumentException("ZoomRegion cannot be constructed from invalid string");
        }
    }

    private void initializeFromRegionPattern(String input) {
        String[] strings = input.split("-");

        int firstInteger = Integer.parseInt(strings[0]);
        int secondInteger = Integer.parseInt(strings[1]);
        // make sure that the smaller zoom level is used as maxZoom since smaller zoom level means greater scale
        if (firstInteger > secondInteger) {
            this.minZoom = new ZoomLevel(firstInteger);
            this.maxZoom = new ZoomLevel(secondInteger);
        } else {
            this.minZoom = new ZoomLevel(secondInteger);
            this.maxZoom = new ZoomLevel(firstInteger);
        }
    }

    private void initializeFromGreaterOrLessPattern(String input) {
        String symbol = input.substring(0, 1);
        ZoomLevel zoomLevel = new ZoomLevel(Integer.parseInt(input.substring(1)));
        if (symbol.equals("<")) {
            this.maxZoom = zoomLevel;
        } else if (symbol.equals(">")) {
            this.minZoom = zoomLevel;
        }
    }

    public double getMinScaleDenominator() {
        if (minZoom == null) {
            return 0;
        } else {
            return minZoom.getScaleDenominator();
        }
    }

    public double getMaxScaleDenominator() {
        if (maxZoom == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            return maxZoom.getScaleDenominator();
        }
    }


    @Override
    public String toString() {
        return "ZoomRegion{" +
                "minZoom=" + minZoom +
                ", maxZoom=" + maxZoom +
                '}';
    }

    public String getConfigStringRepresention() {
        return minZoom.getZoomLevel() + "-" + maxZoom.getZoomLevel();
    }
}
