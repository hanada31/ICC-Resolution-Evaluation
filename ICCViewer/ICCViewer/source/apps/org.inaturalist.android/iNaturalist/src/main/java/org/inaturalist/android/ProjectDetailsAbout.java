package org.inaturalist.android;

import com.evernote.android.state.State;
import com.flurry.android.FlurryAgent;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.livefront.bridge.Bridge;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

public class ProjectDetailsAbout extends AppCompatActivity {
    public static final String KEY_PROJECT = "project";
    @State(AndroidStateBundlers.BetterJSONObjectBundler.class) public BetterJSONObject mProject;

    @Override
	protected void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, INaturalistApp.getAppContext().getString(R.string.flurry_api_key));
		FlurryAgent.logEvent(this.getClass().getSimpleName());
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bridge.restoreInstanceState(this, savedInstanceState);

        setContentView(R.layout.project_details_about);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setLogo(R.drawable.ic_arrow_back);
        actionBar.setTitle(R.string.about_project);

        TextView title = (TextView) findViewById(R.id.project_title);
        TextView projectDescription = (TextView) findViewById(R.id.project_description);
        TextView projectTerms = (TextView) findViewById(R.id.project_terms);
        TextView projectRules = (TextView) findViewById(R.id.project_rules);
        ViewGroup projectTermsContainer = (ViewGroup) findViewById(R.id.terms_container);
        ViewGroup projectRulesContainer = (ViewGroup) findViewById(R.id.rules_container);

        final Intent intent = getIntent();
        if (savedInstanceState == null) {
            mProject = (BetterJSONObject) intent.getSerializableExtra(KEY_PROJECT);
        }

        title.setText(mProject.getString("title"));
        String description = mProject.getString("description");
        description = description.replace("\n", "\n<br>");
        projectDescription.setText(Html.fromHtml(description));
        Linkify.addLinks(projectDescription, Linkify.ALL);
        projectDescription.setMovementMethod(LinkMovementMethod.getInstance());

        String terms = mProject.getString("terms");
        if ((terms != null) && (terms.length() > 0)) {
            projectTermsContainer.setVisibility(View.VISIBLE);
            projectTerms.setText(Html.fromHtml(terms));
            Linkify.addLinks(projectTerms, Linkify.ALL);
            projectTerms.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            projectTermsContainer.setVisibility(View.GONE);
        }

        String rules = mProject.getString("project_observation_rule_terms");
        if ((rules != null) && (rules.length() > 0)) {
            projectRulesContainer.setVisibility(View.VISIBLE);
            String[] rulesSplit = rules.split("\\|");
            String rulesFinal = StringUtils.join(rulesSplit, "<br/>&#8226; ");
            rulesFinal = "&#8226; " + rulesFinal;
            projectRules.setText(Html.fromHtml(rulesFinal));
            Linkify.addLinks(projectRules, Linkify.ALL);
            projectRules.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            projectRulesContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bridge.saveInstanceState(this, outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
