package com.example.ee.newsapp;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

    public class NewsArticleLoader extends AsyncTaskLoader<List<NewArticle>> {
        private final String mQueryUrl;

        public NewsArticleLoader(Context context, String queryUrl) {
            super(context);
            mQueryUrl = queryUrl;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public List<NewArticle> loadInBackground() {
            if (mQueryUrl == null) return null;
            return QueryUtils.fetchData(mQueryUrl);
        }
}
