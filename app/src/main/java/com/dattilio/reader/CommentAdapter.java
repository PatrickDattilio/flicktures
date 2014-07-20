package com.dattilio.reader;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.googlecode.flickrjandroid.photos.comments.Comment;

/**
 * Created by Pmoney on 7/19/2014.
 */
public class CommentAdapter extends CursorAdapter {
    private LayoutInflater mInflater;

    public CommentAdapter(Context context, Cursor c) {
        super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.list_item_comment, parent, false);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.author = (TextView) view.findViewById(R.id.list_item_comment_author);
        viewHolder.comment = (TextView) view.findViewById(R.id.list_item_comment_text);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        Comment comment = new Comment();
        comment.setId(cursor.getString(CommentQuery.ID));
        comment.setAuthor(cursor.getString(CommentQuery.AUTHOR));
        comment.setAuthorName(cursor.getString(CommentQuery.AUTHOR_NAME));
        comment.setText(cursor.getString(CommentQuery.TEXT));
        viewHolder.comment.setText(comment.getText());
        String authorName = (comment.getAuthorName() != null && !comment.getAuthorName().equals("")) ? comment.getAuthorName() : comment.getAuthor();
        viewHolder.author.setText(authorName);

    }

    /*
        Interface for comment query columns
     */
    private interface CommentQuery {
        static final int ID = 0;
        static final int PHOTO_ID = 1;
        static final int AUTHOR = 2;
        static final int AUTHOR_NAME = 3;
        static final int TEXT = 4;
    }

    private class ViewHolder {
        public TextView author;
        public TextView comment;
    }
}
