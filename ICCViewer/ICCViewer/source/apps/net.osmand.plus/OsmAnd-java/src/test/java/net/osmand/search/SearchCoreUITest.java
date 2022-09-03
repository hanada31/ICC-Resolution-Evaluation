package net.osmand.search;

import net.osmand.OsmAndCollator;
import net.osmand.ResultMatcher;
import net.osmand.binary.BinaryMapIndexReader;
import net.osmand.data.Amenity;
import net.osmand.data.Building;
import net.osmand.data.City;
import net.osmand.data.LatLon;
import net.osmand.data.MapObject;
import net.osmand.data.Street;
import net.osmand.osm.AbstractPoiType;
import net.osmand.osm.MapPoiTypes;
import net.osmand.search.SearchUICore.SearchResultCollection;
import net.osmand.search.SearchUICore.SearchResultMatcher;
import net.osmand.search.core.SearchPhrase;
import net.osmand.search.core.SearchResult;
import net.osmand.search.core.SearchSettings;
import net.osmand.util.Algorithms;
import net.osmand.util.MapUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchCoreUITest {

	private static final String SEARCH_RESOURCES_PATH = "src/test/resources/search/";
	private static Map<String, String> enPhrases = new HashMap<>();
	private static Map<String, String> phrases = new HashMap<>();

	static {
		MapPoiTypes.setDefault(new MapPoiTypes("src/test/resources/poi_types.xml"));
		MapPoiTypes poiTypes = MapPoiTypes.getDefault();

		try {
			enPhrases = Algorithms.parseStringsXml(new File("src/test/resources/phrases/en/phrases.xml"));
			//phrases = Algorithms.parseStringsXml(new File("src/test/resources/phrases/ru/phrases.xml"));
			phrases = enPhrases;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

		poiTypes.setPoiTranslator(new MapPoiTypes.PoiTranslator() {

			@Override
			public String getTranslation(AbstractPoiType type) {
				AbstractPoiType baseLangType = type.getBaseLangType();
				if (baseLangType != null) {
					return getTranslation(baseLangType) + " (" + type.getLang().toLowerCase() + ")";
				}
				return getTranslation(type.getIconKeyName());
			}

			@Override
			public String getTranslation(String keyName) {
				String val = phrases.get("poi_" + keyName);
				if (val != null) {
					int ind = val.indexOf(';');
					if (ind > 0) {
						return val.substring(0, ind);
					}
				}
				return val;
			}

			@Override
			public String getSynonyms(AbstractPoiType type) {
				AbstractPoiType baseLangType = type.getBaseLangType();
				if (baseLangType != null) {
					return getSynonyms(baseLangType);
				}
				return getSynonyms(type.getIconKeyName());
			}


			@Override
			public String getSynonyms(String keyName) {
				String val = phrases.get("poi_" + keyName);
				if (val != null) {
					int ind = val.indexOf(';');
					if (ind > 0) {
						return val.substring(ind + 1);
					}
					return "";
				}
				return null;
			}

			@Override
			public String getEnTranslation(AbstractPoiType type) {
				AbstractPoiType baseLangType = type.getBaseLangType();
				if (baseLangType != null) {
					return getEnTranslation(baseLangType) + " (" + type.getLang().toLowerCase() + ")";
				}
				return getEnTranslation(type.getIconKeyName());
			}

			@Override
			public String getEnTranslation(String keyName) {
				if (enPhrases.isEmpty()) {
					return Algorithms.capitalizeFirstLetter(keyName.replace('_', ' '));
				}
				String val = enPhrases.get("poi_" + keyName);
				if (val != null) {
					int ind = val.indexOf(';');
					if (ind > 0) {
						return val.substring(0, ind);
					}
				}
				return val;
			}
		});
	}

	@Test
	public void testDuplicates() throws IOException {
		SearchSettings ss = new SearchSettings((SearchSettings)null);
		ss = ss.setOriginalLocation(new LatLon(0, 0));
		SearchPhrase phrase = new SearchPhrase(ss, OsmAndCollator.primaryCollator());
		SearchResultCollection cll = new SearchUICore.SearchResultCollection(phrase);
		List<SearchResult> rs = new ArrayList<>();
		SearchResult a1 = searchResult(rs, phrase, "a", 100);
		SearchResult b2 = searchResult(rs, phrase, "b", 200);
		SearchResult b1 = searchResult(rs, phrase, "b", 100);
		/*SearchResult a3 = */ searchResult(rs, phrase, "a", 100);
		cll.addSearchResults(rs, true, true);
		Assert.assertEquals(3, cll.getCurrentSearchResults().size());
		Assert.assertSame(a1, cll.getCurrentSearchResults().get(0));
		Assert.assertSame(b1, cll.getCurrentSearchResults().get(1));
		Assert.assertSame(b2, cll.getCurrentSearchResults().get(2));
	}
	
	@Test
	public void testNoResort() throws IOException {
		SearchSettings ss = new SearchSettings((SearchSettings)null);
		ss = ss.setOriginalLocation(new LatLon(0, 0));
		SearchPhrase phrase = new SearchPhrase(ss, OsmAndCollator.primaryCollator());
		SearchResultCollection cll = new SearchUICore.SearchResultCollection(phrase);
		List<SearchResult> rs = new ArrayList<>();
		SearchResult a1 = searchResult(rs, phrase, "a", 100);
		cll.addSearchResults(rs, false, true);
		rs.clear();
		
		SearchResult b2 = searchResult(rs, phrase, "b", 200);
		cll.addSearchResults(rs, false, true);
		rs.clear();
		
		SearchResult b1 = searchResult(rs, phrase, "b", 100);
		cll.addSearchResults(rs, false, true);
		rs.clear();
		
		/*SearchResult a3 = */ searchResult(rs, phrase, "a", 100);
		cll.addSearchResults(rs, false, true);
		rs.clear();
		
		Assert.assertEquals(3, cll.getCurrentSearchResults().size());
		Assert.assertSame(a1, cll.getCurrentSearchResults().get(0));
		Assert.assertSame(b2, cll.getCurrentSearchResults().get(1));
		Assert.assertSame(b1, cll.getCurrentSearchResults().get(2));
		
		
		
	}
	
	
	@Test
	public void testNoResortDuplicate() throws IOException {
		SearchSettings ss = new SearchSettings((SearchSettings)null);
		ss = ss.setOriginalLocation(new LatLon(0, 0));
		SearchPhrase phrase = new SearchPhrase(ss, OsmAndCollator.primaryCollator());
		SearchResultCollection cll = new SearchUICore.SearchResultCollection(phrase);
		List<SearchResult> rs = new ArrayList<>();
		SearchResult a1 = searchResult(rs, phrase, "a", 100);
		SearchResult b2 = searchResult(rs, phrase, "b", 200);
		SearchResult b1 = searchResult(rs, phrase, "b", 100);
		cll.addSearchResults(rs, false, true);
		rs.clear();
		/*SearchResult a3 = */ searchResult(rs, phrase, "a", 100);
		cll.addSearchResults(rs, false, true);
		rs.clear();
		
		Assert.assertEquals(3, cll.getCurrentSearchResults().size());
		Assert.assertSame(a1, cll.getCurrentSearchResults().get(0));
		Assert.assertSame(b1, cll.getCurrentSearchResults().get(1));
		Assert.assertSame(b2, cll.getCurrentSearchResults().get(2));
		
		
	}

	private SearchResult searchResult(List<SearchResult> rs, SearchPhrase phrase, String text, int dist) {
		SearchResult res = new SearchResult(phrase);
		res.localeName = text;
		double d1 = MapUtils.getDistance(0, 0, 0, 1);
		res.location = new LatLon(0, dist / d1);
		rs.add(res);
		return res;
	}

	@Test
	public void testSearchJsons() throws IOException {
		final File[] files = new File(SEARCH_RESOURCES_PATH).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".json");
			}
		});
		if (files != null) {
			for (File f : files) {
				testSearchImpl(f);
			}
		}
	}

	private void testSearchImpl(File jsonFile) throws IOException, JSONException {
		String sourceJsonText = Algorithms.getFileAsString(jsonFile);
		Assert.assertNotNull(sourceJsonText);
		Assert.assertTrue(sourceJsonText.length() > 0);

		BinaryMapIndexReaderTest reader = new BinaryMapIndexReaderTest();
		JSONObject sourceJson = new JSONObject(sourceJsonText);
		String phraseText = sourceJson.getString("phrase");
		JSONObject settingsJson = sourceJson.getJSONObject("settings");
		if (sourceJson.has("amenities")) {
			JSONArray amenitiesArr = sourceJson.getJSONArray("amenities");
			List<Amenity> amenities = new ArrayList<>();
			for (int i = 0; i < amenitiesArr.length(); i++) {
				JSONObject amenityObj = amenitiesArr.getJSONObject(i);
				amenities.add(Amenity.parseJSON(amenityObj));
			}
			reader.amenities = amenities;
		}
		if (sourceJson.has("cities")) {
			JSONArray citiesArr = sourceJson.getJSONArray("cities");
			List<City> cities = new ArrayList<>();
			List<City> initCities = new ArrayList<>();
			List<City> matchedCities = new ArrayList<>();
			List<City> streetCities = new ArrayList<>();
			for (int i = 0; i < citiesArr.length(); i++) {
				JSONObject cityObj = citiesArr.getJSONObject(i);
				final City city = City.parseJSON(cityObj);
				cities.add(city);
				if (cityObj.has("init")) {
					initCities.add(city);
				}
				if (cityObj.has("matchCity")) {
					matchedCities.add(city);
				}
				if (cityObj.has("matchStreet")) {
					streetCities.add(city);
				}
			}
			reader.cities = cities;
			reader.initCities = initCities;
			reader.matchedCities = matchedCities;
			reader.streetCities = streetCities;
		}
		List<String> results = new ArrayList<>();
		if (sourceJson.has("results")) {
			JSONArray resultsArr = sourceJson.getJSONArray("results");
			for (int i = 0; i < resultsArr.length(); i++) {
				results.add(resultsArr.getString(i));
			}
		}

		SearchSettings s = SearchSettings.parseJSON(settingsJson);
		s.setOfflineIndexes(Collections.singletonList(reader));

		SearchPhrase phrase = new SearchPhrase(s, OsmAndCollator.primaryCollator());
		phrase = phrase.generateNewPhrase(phraseText, s);

		final SearchUICore core = new SearchUICore(MapPoiTypes.getDefault(), "en", false);
		core.init();

		ResultMatcher<SearchResult> rm = new ResultMatcher<SearchResult>() {
			@Override
			public boolean publish(SearchResult object) {
				return true;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}
		};
		SearchResultMatcher matcher = new SearchResultMatcher(rm, phrase, 1, new AtomicInteger(1), -1);
		core.searchInternal(phrase, matcher);

		SearchResultCollection collection = new SearchResultCollection(phrase);
		collection.addSearchResults(matcher.getRequestResults(), true, true);
		List<SearchResult> searchResults = collection.getCurrentSearchResults();
		int i = 0;
		for (SearchResult result : searchResults) {
			String expected = results.get(i++);
			String present = result.toString();
			//System.out.println(present);
			Assert.assertEquals(expected, present);
			if (i >= results.size()) {
				break;
			}
		}
	}

	private static class BinaryMapIndexReaderTest extends BinaryMapIndexReader {

		List<Amenity> amenities = Collections.emptyList();
		List<City> cities = Collections.emptyList();
		List<City> initCities = Collections.emptyList();
		List<City> matchedCities = Collections.emptyList();
		List<City> streetCities = Collections.emptyList();

		BinaryMapIndexReaderTest() throws IOException {
			super(null, null, false);
		}

		@Override
		public List<Amenity> searchPoiByName(SearchRequest<Amenity> req) throws IOException {
			for (Amenity amenity : amenities) {
				req.publish(amenity);
			}
			return req.getSearchResults();
		}

		@Override
		public List<Amenity> searchPoi(SearchRequest<Amenity> req) throws IOException {
			for (Amenity amenity : amenities) {
				req.publish(amenity);
			}
			return req.getSearchResults();
		}

		@Override
		public List<City> getCities(SearchRequest<City> resultMatcher, int cityType) throws IOException {
			for (City city : initCities) {
				if (resultMatcher != null) {
					resultMatcher.publish(city);
				}
			}
			return initCities;
		}

		@Override
		public int preloadStreets(City c, SearchRequest<Street> resultMatcher) throws IOException {
			return 0;
		}

		@Override
		public void preloadBuildings(Street s, SearchRequest<Building> resultMatcher) throws IOException {
			// cities must be filled with streets and buildings
		}

		@Override
		public List<MapObject> searchAddressDataByName(SearchRequest<MapObject> req) throws IOException {
			for (City city : streetCities) {
				for (Street street : city.getStreets()) {
					req.publish(street);
				}
			}
			for (City city : matchedCities) {
				req.publish(city);
			}
			return req.getSearchResults();
		}

		@Override
		public String getRegionName() {
			return "Test region";
		}

		@Override
		public boolean containsPoiData(int left31x, int top31y, int right31x, int bottom31y) {
			return true;
		}

		@Override
		public boolean containsMapData() {
			return true;
		}

		@Override
		public boolean containsPoiData() {
			return true;
		}

		@Override
		public boolean containsRouteData() {
			return true;
		}

		@Override
		public boolean containsRouteData(int left31x, int top31y, int right31x, int bottom31y, int zoom) {
			return true;
		}

		@Override
		public boolean containsAddressData(int left31x, int top31y, int right31x, int bottom31y) {
			return true;
		}

		@Override
		public boolean containsMapData(int tile31x, int tile31y, int zoom) {
			return true;
		}

		@Override
		public boolean containsMapData(int left31x, int top31y, int right31x, int bottom31y, int zoom) {
			return true;
		}

		@Override
		public boolean containsAddressData() {
			return true;
		}
	}
}
