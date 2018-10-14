package com.example.ee.newsapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewsArticleAdapter {

    public class ArticleAdapter extends ArrayAdapter<NewArticle> {
        private  final String LOG_TAG = ArticleAdapter.class.getSimpleName();

        ArticleAdapter(Context context, List<NewArticle> articles) { super(context, 0, articles);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.news_list_item, parent, false);
                holder = new ViewHolder();
                holder.title =  convertView.findViewById(R.id.news_title);
                holder.sectionName =  convertView.findViewById(R.id.category_title);
                holder.date =  convertView.findViewById(R.id.date);
                holder.author =  convertView.findViewById(R.id.author);
                holder.contentText =  convertView.findViewById(R.id.content);
                holder.thumbnail =  convertView.findViewById(R.id.featured_image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            NewArticle currentArticle = getItem(position);

            if (!(currentArticle).getTitle().isEmpty())
                holder.title.setText(currentArticle.getTitle());
            if (!currentArticle.getSectionName().isEmpty())
                holder.sectionName.setText(currentArticle.getSectionName());
            if (!currentArticle.getDate().isEmpty()) {
                Date parsedDate = parseDate(currentArticle.getDate());
                if (parsedDate != null) holder.date.setText(formatDate(parsedDate));
            }
            if (!currentArticle.getAuthor().isEmpty())
                holder.author.setText(currentArticle.getAuthor());
            if (!currentArticle.getTrailText().isEmpty())
                holder.contentText.setText(currentArticle.getTrailText());
            if (currentArticle.getThumbnailBitmap() != null) {
                holder.thumbnail.setImageBitmap(currentArticle.getThumbnailBitmap());
            }
            return convertView;
        }

        private String formatDate(Date dateObj) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
            return dateFormat.format(dateObj);
        }

        private Date parseDate(String strDate) {
            //in iso8601 date, out Date object
            if (strDate == null) return null;
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            Date date = null;
            try {
                date = parser.parse(strDate);
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Error parsing date", e);
            }
            return date;
        }

        private  class ViewHolder {
            TextView title;
            TextView sectionName;
            TextView date;
            TextView contentText;
            TextView author;
            ImageView thumbnail;
        }
    }
}
