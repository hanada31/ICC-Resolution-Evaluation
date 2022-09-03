package net.osmand.plus.wikipedia;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.osmand.data.Amenity;

import static net.osmand.plus.wikipedia.WikiArticleHelper.WIKI_DOMAIN;
import static net.osmand.plus.wikipedia.WikiArticleHelper.WIKI_DOMAIN_COM;



public class WikipediaWebViewClient extends WebViewClient {

	private static final String PAGE_PREFIX_HTTP = "http://";
	private static final String PAGE_PREFIX_HTTPS = "https://";

	private Context context;
	private boolean nightMode;
	private WikiArticleHelper wikiArticleHelper;
	private Amenity article;

	public WikipediaWebViewClient(FragmentActivity context, Amenity article, boolean nightMode) {
		this.context = context;
		this.nightMode = nightMode;
		this.wikiArticleHelper = new WikiArticleHelper(context, nightMode);
		this.article = article;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		url = WikiArticleHelper.normalizeFileUrl(url);
		if ((url.contains(WIKI_DOMAIN) || url.contains(WIKI_DOMAIN_COM)) && article != null) {
			wikiArticleHelper.showWikiArticle(article.getLocation(), url);
		} else if (url.startsWith(PAGE_PREFIX_HTTP) || url.startsWith(PAGE_PREFIX_HTTPS)) {
			WikiArticleHelper.warnAboutExternalLoad(url, context, nightMode);
		} else {
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			context.startActivity(i);
		}
		return true;
	}

	public void stopRunningAsyncTasks() {
		if (wikiArticleHelper != null) {
			wikiArticleHelper.stopSearchAsyncTask();
		}
	}
}
