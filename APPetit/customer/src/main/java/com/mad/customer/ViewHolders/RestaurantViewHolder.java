package com.mad.customer.ViewHolders;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mad.customer.UI.OrderingFragment;
import com.mad.customer.R;
import com.mad.customer.UI.TabApp;
import com.mad.mylibrary.Restaurateur;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import static com.mad.mylibrary.SharedClass.CUSTOMER_FAVOURITE_RESTAURANT_PATH;
import static com.mad.mylibrary.SharedClass.CUSTOMER_PATH;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.ROOT_UID;

public class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private TextView name;
    private TextView addr;
    private TextView cuisine;
    private TextView opening;
    private ImageView img;
    private Restaurateur current;
    private String key;
    private Context context;
    private boolean favorite_bool,favorite_visible;
    private LinkedList<String> list_favorite;
    private RatingBar ratingBar;
    private TextView star_value;

    public RestaurantViewHolder(View itemView, Context context){
        super(itemView);
        name = itemView.findViewById(R.id.restaurant_name);
        addr = itemView.findViewById(R.id.listview_address);
        cuisine = itemView.findViewById(R.id.listview_cuisine);
        img = itemView.findViewById(R.id.restaurant_image);
        opening = itemView.findViewById(R.id.listview_opening);
        this.context = context;
        favorite_bool = false;
        favorite_visible = false;
        ratingBar = itemView.findViewById(R.id.ratingBaritem);
        star_value = itemView.findViewById(R.id.textView14);
        itemView.setOnClickListener(this);
    }

    public void setData (Restaurateur current, int position, String key){
        this.name.setText(current.getName());
        this.addr.setText(current.getAddr());
        this.cuisine.setText(current.getCuisine());
        this.opening.setText(current.getOpeningTime());
        if(!current.getPhotoUri().equals("null")) {
            Glide.with(itemView).load(current.getPhotoUri()).into(this.img);
        }
        //Opening --> controllo se il ristorante è aperto o chiuso
        int open_h = Integer.parseInt(current.getOpeningTime().split(" - ")[0].split(":")[0]);
        int open_m = Integer.parseInt(current.getOpeningTime().split(" - ")[0].split(":")[1]);
        int close_h = Integer.parseInt(current.getOpeningTime().split(" - ")[1].split(":")[0]);
        int close_m = Integer.parseInt(current.getOpeningTime().split(" - ")[1].split(":")[1]);
        Long opening = getDate(open_h, open_m,0, (long) 0);
        Long closing = getDate(close_h, close_m,1, opening);

        if(System.currentTimeMillis()<=closing & System.currentTimeMillis()>=opening){
            ImageView closed = (ImageView) itemView.findViewById(R.id.imageView4);
            closed.setVisibility(View.GONE);
        }
        else{
            Log.d("TAG","ristorante chiuso");
            itemView.setOnClickListener(null);
        }
        this.current = current;
        this.key = key;
        Drawable d;
        ImageView favorite = itemView.findViewById(R.id.star_favorite);
        if(favorite_visible) {

            for(String key_res : list_favorite){
                if(key_res.compareTo(key) == 0){
                    favorite_bool = true;
                    ImageView start = itemView.findViewById(R.id.star_favorite);
                    start.setImageResource(R.drawable.heart_fill);
                }
            }

            favorite.setOnClickListener(e -> {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(CUSTOMER_PATH).child(ROOT_UID).child(CUSTOMER_FAVOURITE_RESTAURANT_PATH);
                if (favorite_bool) {
                    ref.child(key).removeValue();
                    ImageView start = itemView.findViewById(R.id.star_favorite);
                    start.setImageResource(R.drawable.heart);
                    favorite_bool = false;

                    Toast.makeText(context, "Removed from favorite",
                            Toast.LENGTH_SHORT).show();
                } else {
                    HashMap<String, Object> favorite_restaurant = new HashMap<String, Object>();
                    favorite_restaurant.put(key, current);

                    ref.updateChildren(favorite_restaurant);
                    ImageView start = itemView.findViewById(R.id.star_favorite);
                    start.setImageResource(R.drawable.heart_fill);
                    favorite_bool = true;

                    Toast.makeText(context, "Added to favourite",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            favorite.setVisibility(View.INVISIBLE);
        }
        Query query = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO).child(key).child("stars");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    if(((Long)dataSnapshot.child("tot_review").getValue()).intValue()==0){
                        ratingBar.setRating(0);
                        star_value.setVisibility(View.GONE);
                    }
                    else {
                        float s = ((Long) dataSnapshot.child("tot_stars").getValue()).floatValue();
                        float p = ((Long) dataSnapshot.child("tot_review").getValue()).floatValue();
                        ratingBar.setRating(s / p);
                        star_value.setText(String.format("%.2f", s / p));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), TabApp.class);
        intent.putExtra("res_item", current);
        intent.putExtra("key", this.key);
        view.getContext().startActivity(intent);
    }

    public void setFavorite(LinkedList<String> favorite){
        list_favorite = favorite;
        favorite_visible = true;
    }
    private Long getDate (int hour, int min, int mode, Long prev) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        Date date = cal.getTime();

        if (mode==1 && date.getTime()<prev){
            cal.set(Calendar.DATE,cal.get(Calendar.DATE)+1);
            date = cal.getTime();
        }
        return date.getTime();
    }
}
