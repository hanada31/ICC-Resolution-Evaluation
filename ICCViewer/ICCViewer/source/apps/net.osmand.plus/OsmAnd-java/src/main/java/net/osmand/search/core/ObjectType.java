package net.osmand.search.core;

public enum ObjectType {
	// ADDRESS
	CITY(true), VILLAGE(true), POSTCODE(true), STREET(true), HOUSE(true), STREET_INTERSECTION(true),
	// POI
	POI_TYPE(false), POI(true),
	// LOCATION
	LOCATION(true), PARTIAL_LOCATION(false),
	// UI OBJECTS
	FAVORITE(true), FAVORITE_GROUP(false), WPT(true), RECENT_OBJ(true),

	// ONLINE SEARCH
	ONLINE_SEARCH(true),
	
	REGION(true),

	SEARCH_STARTED(false),
	FILTER_FINISHED(false),
	SEARCH_FINISHED(false),
	SEARCH_API_FINISHED(false),
	SEARCH_API_REGION_FINISHED(false),
	UNKNOWN_NAME_FILTER(false);

	private boolean hasLocation;
	private ObjectType(boolean location) {
		this.hasLocation = location;
	}
	public boolean hasLocation() {
		return hasLocation;
	}
	
	public static boolean isAddress(ObjectType t) {
		return t == CITY || t == VILLAGE || t == POSTCODE || t == STREET || t == HOUSE || t == STREET_INTERSECTION;
	}

	public static boolean isTopVisible(ObjectType t) {
		return t == POI_TYPE || t == FAVORITE || t == FAVORITE_GROUP || t == WPT || t == LOCATION || t == PARTIAL_LOCATION;
	}

	public static ObjectType getExclusiveSearchType(ObjectType t) {
		if (t == FAVORITE_GROUP) {
			return FAVORITE;
		}
		return null;
	}

	public static double getTypeWeight(ObjectType t) {
		if (t == null) {
			return 1.0;
		}
		switch (t) {
			case CITY:
				return 1.0;
			case VILLAGE:
				return 1.0;
			case POSTCODE:
				return 1.0;
			case STREET:
				return 2.0;
			case HOUSE:
				return 3.0;
			case STREET_INTERSECTION:
				return 3.0;
			case POI:
				return 2.0;
			default:
				return 1.0;
		}
	}
}
