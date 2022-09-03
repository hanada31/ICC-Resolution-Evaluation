package net.osmand;

import android.content.Context;
import android.os.StatFs;
import android.support.annotation.NonNull;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.osmand.plus.OsmandApplication;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

public class PicassoUtils {

	private static final String PICASSO_CACHE = "picasso-cache";
	private static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB
	private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
	private static PicassoUtils INSTANCE;

	private Cache diskCache;
	private LruCache memoryCache;

	private Map<String, Boolean> cached = new HashMap<>();
	
	private PicassoUtils(OsmandApplication app){
		File cacheDir = createDefaultCacheDir(app);

		diskCache = new Cache(cacheDir, calculateDiskCacheSize(cacheDir));
		memoryCache = new LruCache(app);

		Picasso picasso = new Picasso.Builder(app)
				.downloader(new OkHttp3Downloader(new OkHttpClient.Builder().cache(diskCache).build()))
				.memoryCache(memoryCache)
				.build();

		Picasso.setSingletonInstance(picasso);

	}

	public static PicassoUtils getPicasso(@NonNull OsmandApplication app) {
		if(INSTANCE == null) {
			INSTANCE = new PicassoUtils(app);
		}
		return INSTANCE;
	}

	public void clearAllPicassoCache() {
		if (memoryCache != null) {
			memoryCache.clear();
		}
		if (diskCache != null) {
			try {
				diskCache.evictAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		cached.clear();
	}

	public Boolean isURLLoaded(@NonNull String key) {
		return cached.get(key);
	}

	public void setResultLoaded(@NonNull String key, boolean val) {
		cached.put(key, val);
	}

	public void clearCachedMap() {
		cached.clear();
	}

	public long getDiskCacheSizeBytes() throws IOException {
		return diskCache.size();
	}

	private static File createDefaultCacheDir(Context context) {
		File cache = new File(context.getApplicationContext().getCacheDir(), PICASSO_CACHE);
		if (!cache.exists()) {
			//noinspection ResultOfMethodCallIgnored
			cache.mkdirs();
		}
		return cache;
	}

	private static long calculateDiskCacheSize(File dir) {
		long size = MIN_DISK_CACHE_SIZE;

		try {
			StatFs statFs = new StatFs(dir.getAbsolutePath());
			//noinspection deprecation
			long blockCount =
					SDK_INT < JELLY_BEAN_MR2 ? (long) statFs.getBlockCount() : statFs.getBlockCountLong();
			//noinspection deprecation
			long blockSize =
					SDK_INT < JELLY_BEAN_MR2 ? (long) statFs.getBlockSize() : statFs.getBlockSizeLong();
			long available = blockCount * blockSize;
			// Target 2% of the total space.
			size = available / 50;
		} catch (IllegalArgumentException ignored) {
		}

		// Bound inside min/max size for disk cache.
		return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE);
	}
}
