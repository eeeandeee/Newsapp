package com.example.ee.newsapp;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewArticle>> {
    private static final int LOADER_ID = 1;
    private static final String SEARCH_QUERY_KEY = "query";
    private TextView mEmptyStateView;
    private NewsArticleAdapter mAdapter;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmptyStateView =  findViewById(R.id.empty_view);
        mProgressBar =  findViewById(R.id.progress_bar);
        mAdapter = new NewsArticleAdapter(this, new ArrayList<NewArticle>());

        ListView articleListView =  findViewById(R.id.list);
        articleListView.setEmptyView(mEmptyStateView);
        articleListView.setAdapter(mAdapter);
        articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        mAdapter.getItem(position).getWebUrl()
                ));
                if (browserIntent.resolveActivity(view.getContext().getPackageManager()) != null)
                    view.getContext().startActivity(browserIntent);
            }
        });

        if (getIntent() != null) handleIntent(getIntent());
    }

    //helpers
    private void handleIntent(Intent intent) {
        final String queryAction = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Bundle bundle = new Bundle();
            bundle.putString(SEARCH_QUERY_KEY, query);
            lookupArticles(bundle);
        } else if (Intent.ACTION_MAIN.equals(queryAction)) {
            lookupArticles(null);
        }
    }

    private void lookupArticles(Bundle bundle) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            mProgressBar.setVisibility(View.VISIBLE);
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(LOADER_ID, bundle, this);
        } else {
            mEmptyStateView.setText(getString(R.string.no_internet));
        }
    }

    private String createUri(Bundle bundle) {
        String queryString;
        if (bundle != null) queryString = bundle.getString(SEARCH_QUERY_KEY);
        else queryString = "null";

        final String QUERY_URL = "https://content.guardianapis.com/search";
        final String ARG_QUERY = "q";
        final String ARG_ORDER = "order-By";
        final String ARG_API = "api-key";
        final String API_KEY = "2b1f8e05-8a41-4e5c-ac30-b98cebde15e0";
        final String ARG_SHOW_FIELDS = "show-fields";
        final String ARG_FIELDS_BYLINE = "byline";
        final String ARG_FIELDS_TRAILTEXT = "trailText";
        final String ARG_FIELDS_THUMBNAILS = "thumbnail";
        final String FIELDS_SEPARATOR = ",";

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        Uri baseUri = Uri.parse(QUERY_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter(ARG_QUERY, queryString);
        uriBuilder.appendQueryParameter(ARG_ORDER, orderBy);

        final boolean byline = sharedPrefs.getBoolean(getString(R.string.settings_use_byline_key), true);
        final boolean trailText = sharedPrefs.getBoolean(getString(R.string.settings_use_trailText_key), true);
        final boolean thumbnails = sharedPrefs.getBoolean(getString(R.string.settings_use_thumbnail_key), true);
        StringBuilder fieldsBuilder = new StringBuilder();
        if (byline) fieldsBuilder.append(ARG_FIELDS_BYLINE + FIELDS_SEPARATOR);
        if (trailText) fieldsBuilder.append(ARG_FIELDS_TRAILTEXT + FIELDS_SEPARATOR);
        if (thumbnails) fieldsBuilder.append(ARG_FIELDS_THUMBNAILS + FIELDS_SEPARATOR);
        if (fieldsBuilder.length() > 0) {
            fieldsBuilder.deleteCharAt(fieldsBuilder.length() - 1);
            uriBuilder.appendQueryParameter(ARG_SHOW_FIELDS, fieldsBuilder.toString());
        }
        uriBuilder.appendQueryParameter(ARG_API, API_KEY);
        return uriBuilder.toString();

    }





    //loader stuff
    @Override
    public Loader<List<NewArticle>>onCreateLoader(int id, Bundle args) {
        return new NewsArticleLoader(this, createUri(args));
    }

    @Override
    public void onLoadFinished(Loader<List<NewArticle>> loader, List<NewArticle> data) {
        mProgressBar.setVisibility(View.GONE);
        mEmptyStateView.setText(getString(R.string.no_results));
        mAdapter.clear();
        if (data != null && !data.isEmpty()) mAdapter.addAll(data);
    }

    @Override
    public void onLoaderReset(Loader<List<NewArticle>> loader) {
        mAdapter.clear();
    }

    //intents
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }
}
