package drewmahrt.generalassemb.ly.twitteroauthlab;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import drewmahrt.generalassemb.ly.twitteroauthlab.models.Tweet;

/**
 * Bind tweets to views inside the recycler view
 * Created by charlie on 11/18/16.
 */

class TweetRvAdapter extends RecyclerView.Adapter<TweetRvAdapter.TweetViewHolder> {

    private List<Tweet> mTweets;

    TweetRvAdapter(List<Tweet> tweets) {
        mTweets = tweets;
    }

    @Override
    public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new TweetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TweetViewHolder holder, int position) {
        Tweet tweet = mTweets.get(position);
        holder.mTweetText.setText(tweet.getText());
        holder.mCreatedAt.setText(tweet.getCreatedAt());
        holder.mCreatedAt.setTextColor(Color.BLUE);
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    class TweetViewHolder extends RecyclerView.ViewHolder {

        TextView mTweetText, mCreatedAt;

        TweetViewHolder(View itemView) {
            super(itemView);
            mTweetText = (TextView) itemView.findViewById(android.R.id.text1);
            mCreatedAt = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }
}
