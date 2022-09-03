package net.osmand.router;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import net.osmand.binary.BinaryMapIndexReader;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@RunWith(Parameterized.class)
public class RouteTestingTest {
	private TestEntry te;


    public RouteTestingTest(String name, TestEntry te) {
        this.te = te;
    }

    @BeforeClass
    public static void setUp() throws Exception {
        RouteResultPreparation.PRINT_TO_CONSOLE_ROUTE_INFORMATION_TO_TEST = true;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() throws IOException {
        String fileName = "/test_routing.json";
        Reader reader = new InputStreamReader(RouteTestingTest.class.getResourceAsStream(fileName));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        TestEntry[] testEntries = gson.fromJson(reader, TestEntry[].class);
        ArrayList<Object[]> arrayList = new ArrayList<>();
        for(TestEntry te : testEntries) {
        	if(te.isIgnore()) {
        		continue;
        	}
        	arrayList.add(new Object[] {te.getTestName(), te});
        }
        reader.close();
        return arrayList;

    }

    @Test
	public void testRouting() throws Exception {
		String fl = "src/test/resources/Routing_test.obf";
		RandomAccessFile raf = new RandomAccessFile(fl, "r");
		RoutePlannerFrontEnd fe = new RoutePlannerFrontEnd();

		BinaryMapIndexReader[] binaryMapIndexReaders = { new BinaryMapIndexReader(raf, new File(fl)) };
		RoutingConfiguration.Builder builder = RoutingConfiguration.getDefault();
		Map<String, String> params = te.getParams();
		RoutingConfiguration config = builder.build(params.containsKey("vehicle") ? params.get("vehicle") : "car",
				RoutingConfiguration.DEFAULT_MEMORY_LIMIT * 3, params);
		RoutingContext ctx = fe.buildRoutingContext(config, null, binaryMapIndexReaders,
				RoutePlannerFrontEnd.RouteCalculationMode.NORMAL);
		ctx.leftSideNavigation = false;
		List<RouteSegmentResult> routeSegments = fe.searchRoute(ctx, te.getStartPoint(), te.getEndPoint(), te.getTransitPoint());
		Set<Long> reachedSegments = new TreeSet<Long>();
		Assert.assertNotNull(routeSegments);
		int prevSegment = -1;
		for (int i = 0; i <= routeSegments.size(); i++) {
			if (i == routeSegments.size() || routeSegments.get(i).getTurnType() != null) {
				if (prevSegment >= 0) {
					String name = routeSegments.get(prevSegment).getDescription();
					long segmentId = routeSegments.get(prevSegment).getObject().getId() >> (RouteResultPreparation.SHIFT_ID);
					System.out.println("segmentId: " + segmentId + " description: " + name);
				}
				prevSegment = i;
			}
			if (i < routeSegments.size()) {
				reachedSegments.add(routeSegments.get(i).getObject().getId() >> (RouteResultPreparation.SHIFT_ID));
			}
		}
		Map<Long, String> expectedResults = te.getExpectedResults();
		Iterator<Entry<Long, String>> it = expectedResults.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Long, String> es = it.next();
			if (es.getValue().equals("false")) {
				Assert.assertTrue("Expected segment " + (es.getKey()) + " was wrongly reached in route segments "
						+ reachedSegments.toString(), !reachedSegments.contains(es.getKey()));
			} else {
				Assert.assertTrue("Expected segment " + (es.getKey()) + " weren't reached in route segments "
						+ reachedSegments.toString(), reachedSegments.contains(es.getKey()));
			}
		}

	}



}
