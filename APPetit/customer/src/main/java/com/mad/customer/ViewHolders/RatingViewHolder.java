package com.mad.customer.ViewHolders;

import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mad.customer.R;
import com.mad.mylibrary.ReviewItem;

import static com.mad.mylibrary.SharedClass.CUSTOMER_PATH;
import static com.mad.mylibrary.SharedClass.RESERVATION_PATH;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.ROOT_UID;

public class RatingViewHolder extends RecyclerView.ViewHolder {
    private TextView name, comment;
    private RatingBar ratingBar;
    private ImageView img;
    private View view;


    public RatingViewHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
        name = itemView.findViewById(R.id.rating_item_name);
        comment  = itemView.findViewById(R.id.rating_item_comment);
        ratingBar = itemView.findViewById(R.id.ratingbaritem);
        img = itemView.findViewById(R.id.rating_item_img);
    }
    public View getView() {
        return view;
    }

    public void setData (ReviewItem ri) {
        name.setText(ri.getName());
        if (ri.getComment() != null) {
            comment.setText(ri.getComment());
        } else {
            comment.setVisibility(View.GONE);
        }

        Query query = FirebaseDatabase.getInstance().getReference(CUSTOMER_PATH).child(ri.getUser_key()).child("customer_info").child("photoPath");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if (dataSnapshot.exists()){
                     Glide.with(itemView).load(dataSnapshot.getValue()).into(img);
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         }
        );
        ratingBar.setRating(ri.getStars());
    }
}
