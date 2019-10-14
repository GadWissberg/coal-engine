package com.gadarts.engine.utils;

public final class C {

    public static final String PLAYER_STRAFE_FORCE_NAME = "strafe";

    public final class LevelKeys {

        public static final String ACTORS = "actors";
        public static final String HASHMAP = "hashMap";
        public static final String LINES = "lines";
        public static final String SRC = "src";
        public static final String DST = "dst";
        public static final String X = "x";
        public static final String Y = "y";
        public static final String SOLID = "solid";
        public static final String FRONT_SECTOR_ID = "frontSectorId";
        public static final String BACK_SECTOR_ID = "backSectorId";
        public static final String HOR_OFFSET = "horizontalOffset";
        public static final String VER_OFFSET = "verticalOffset";
        public static final String FRONT_TEXTURE = "frontTexture";
        public static final String BACK_TEXTURE = "backTexture";
        public static final String ID = "id";
        public static final String SECTORS = "sectors";
        public static final String SUB_SECTORS = "subSectors";
        public static final String FLOOR_ALTITUDE = "floorAltitude";
        public static final String CEIL_ALTITUDE = "ceilAltitude";
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
        public static final String POINTS = "points";
        public static final String CONTAINER_ID = "containerId";
        public static final String ELEMENTS = "elements";
        public static final String PROPERTIES = "properties";
        public static final String LEFT = "left";
        public static final String BOTTOM = "bottom";
        public static final String TOP = "top";
        public static final String RIGHT = "right";
        public static final String CURRENT_FLOOR_ALTITUDE = "currentFloorAltitude";
        public static final String CURRENT_CEILING_ALTITUDE = "currentCeilingAltitude";
        public static final String CURRENT_SECTOR_ID = "currentSectorId";
        public static final String TYPE = "type";
        public static final String FLOOR_TEXTURE = "floorTexture";
        public static final String CEILING_TEXTURE = "ceilingTexture";
        public static final String MIDDLE = "middle";
        public static final String NAME = "name";
        public static final String OPACITY = "opacity";
        public static final String DIRECTION = "direction";
    }

    public static final class ShaderRelated {
        public static final class RegionAttributes {
            public static final class RegionUvAttributes {
                public static final int ATTRIBUTE_USAGE = 512;
                public static final String ATTRIBUTE_ALIAS = "a_region_uv";
            }

            public static final class RegionSizeAttributes {
                public static final int ATTRIBUTE_USAGE = 1024;
                public static final String ATTRIBUTE_ALIAS = "a_region_size";
            }

        }

        public static final class FragmentShaderKeys {
            public static final String COLOR_MULTIPLIER = "u_colorMultiplier";
            public static final String BLUR_DIRECTION = "u_dir";
            public static final String BLUR_RESOLUTION = "u_resolution";
            public static final String BLUR_RADIUS = "u_radius";

        }


    }

    public static final int SPATIAL_CELL_SIZE = 8;

    public static final class Camera {

        public static final float LANDING_SPEED = 3f;
        public static final float WALKING_SPEED = 1.5f;
        public static final float LANDING_MIN_Z = 1.4f;
        public static final float WALKING_MIN_Z = 1.5f;
        public static final float TILT_MAX_DOT_PROD = 0.03f;
        public static final float TILT_DEGREES_DELTA = 15f;
        public static final float ZOOM_SPEED = 0.2f;
        public static final float EYES_TO_TOP_OFFSET = 0.2f;
    }

    public static final class ScreenFade {
        public static final float PICKUP_SCREEN_FADE_PACE = 0.05f;

    }

    public final static class Errors {
        public final static class CodeRelated {
            public static final String PLAYER_NOT_INITIALIZED = "Failed to load game! Did you remember to initialize " +
                    "the player?";
        }

        public final static class LevelRelated {
            private static final String FAILED_TO_LOAD_MAP = "Failed to load map: %s - ";
            public static final String NEG_COORDINATE = FAILED_TO_LOAD_MAP + "%s #%d has a negative coordinate.";
            public static final String ACTOR_IS_OUTSIDE = FAILED_TO_LOAD_MAP + "Actor #%d is not inside a sector.";
            public static final String TWO_ELEMENTS_WITH_SAME_ID = FAILED_TO_LOAD_MAP + "There are more than " +
                    "one %s with the ID: %d.";
            public static final String CEILING_IS_NOT_HIGHER_THAN_FLOOR = FAILED_TO_LOAD_MAP + "Sector #%d doesn't " +
                    "have a ceiling altitude value higher than its floor altitude value.";
            public static final String NEGATIVE_ALTITUDE = FAILED_TO_LOAD_MAP + "Sector #%d has one or more negative " +
                    "altitudes values.";
            public static final String NO_PLAYER_WAS_FOUND = FAILED_TO_LOAD_MAP + "Map has no players.";
            public static final String LINE_WITH_EQUAL_VERTICES = FAILED_TO_LOAD_MAP + "Line #%d has vertices with equal " +
                    "vertices";
        }
    }
}
