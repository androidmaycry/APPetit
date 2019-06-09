package com.mad.customer.ViewHolders;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hsalf.smilerating.SmileRating;
import com.mad.customer.Items.OrderCustomerItem;
import com.mad.customer.R;
import com.mad.mylibrary.ReviewItem;
import com.mad.mylibrary.StarItem;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static com.mad.mylibrary.SharedClass.CUSTOMER_PATH;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.ROOT_UID;
import static com.mad.mylibrary.SharedClass.STATUS_DELIVERED;
import static com.mad.mylibrary.SharedClass.STATUS_DELIVERING;
import static com.mad.mylibrary.SharedClass.STATUS_DISCARDED;
import static com.mad.mylibrary.SharedClass.STATUS_UNKNOWN;
import static com.mad.mylibrary.SharedClass.user;

public class OrderViewHolder extends RecyclerView.ViewHolder{
    private TextView name, date, delivery, total;
    private ImageView img;
    View view;

    public OrderViewHolder(View itemView){
        super(itemView);
        view = itemView;
        name = itemView.findViewById(R.id.order_res_name);
        date = itemView.findViewById(R.id.order_date);
        delivery = itemView.findViewById(R.id.order_status);
        total = itemView.findViewById(R.id.order_tot);
        img = itemView.findViewById(R.id.order_image);
    }

    public void setData(OrderCustomerItem current, int position, String orderKey){
        //Set time in correct format
        Date d = new Date(current.getTime());
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        String date = day+"/"+month+"/"+year;
        this.date.setText(date);
        //Set delivery
        switch (current.getStatus()){
            case STATUS_UNKNOWN:
                delivery.setText("Order sent");
                break;
            case STATUS_DELIVERED:
                delivery.setText("Order delivered");
                delivery.setTextColor(Color.parseColor("#59cc33"));
                break;
            case STATUS_DISCARDED:
                delivery.setText("Order refused");
                delivery.setTextColor(Color.parseColor("#cc3333"));
                break;
            case STATUS_DELIVERING:
                delivery.setText("Delivering...");
                delivery.setTextColor(Color.parseColor("#ffb847"));
                break;
        }

        total.setText(current.getTotPrice()+" €");
        Query query = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO).child(current.getKey()).child("info");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name.setText((String)dataSnapshot.child("name").getValue());
                if(dataSnapshot.child("photoUri").exists()){
                    Glide.with(itemView).load(dataSnapshot.child("photoUri").getValue()).into(img);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Button rate = ((Button)view.findViewById(R.id.order_rate_button));
        if(current.isRated()){
            rate.setVisibility(View.GONE);
        }
        else {
            rate.setOnClickListener(a->{
                showAlertDialogDelivered(current.getKey(), orderKey);
            });
        }


    }

    public View getView (){
        return view;
    }

    private void showAlertDialogDelivered (String resKey, String orderKey){
        Query query = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO).child(resKey).child("info");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AlertDialog alertDialog = new AlertDialog.Builder(view.getContext()).create();
                LayoutInflater factory = LayoutInflater.from(view.getContext());
                final View view = factory.inflate(R.layout.rating_dialog, null);

                alertDialog.setView(view);
                if(dataSnapshot.child("photoUri").exists()){
                    Glide.with(view).load(dataSnapshot.child("photoUri").getValue()).into((ImageView) view.findViewById(R.id.dialog_rating_icon));
                }
                SmileRating smileRating = (SmileRating) view.findViewById(R.id.dialog_rating_rating_bar);
                //Button confirm pressed
                view.findViewById(R.id.dialog_rating_button_positive).setOnClickListener(a->{
                    if(smileRating.getRating()!=0) {
                        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + resKey).child("review");
                        DatabaseReference myRef2 = FirebaseDatabase.getInstance().getReference(CUSTOMER_PATH).child(ROOT_UID).child("orders").child(orderKey);
                        HashMap <String, Object> rated = new HashMap<>();
                        HashMap<String, Object> review = new HashMap<>();
                        String comment = ((EditText)view.findViewById(R.id.dialog_rating_feedback)).getText().toString();
                        updateRestaurantStars(resKey, smileRating.getRating());
                        if(!comment.isEmpty()){
                            rated.put("rated", true);
                            myRef2.updateChildren(rated);
                            review.put(myRef.push().getKey(), new ReviewItem(smileRating.getRating(), comment, ROOT_UID, user.getPhotoPath(), user.getName()));
                            myRef.updateChildren(review);
                        }
                        else{
                            rated.put("rated", true);
                            myRef2.updateChildren(rated);
                            review.put(myRef.push().getKey(), new ReviewItem(smileRating.getRating(), null, ROOT_UID, user.getPhotoPath(), user.getName()));
                            myRef.updateChildren(review);
                        }
                        Toast.makeText(view.getContext(), "Thanks for your review!", Toast.LENGTH_LONG).show();
                        alertDialog.dismiss();
                    }
                    else {
                        Toast.makeText(view.getContext(), "You forgot to rate!", Toast.LENGTH_LONG).show();
                    }
                });
                view.findViewById(R.id.dialog_rating_button_negative).setOnClickListener(b->{
                    alertDialog.dismiss();
                });
                alertDialog.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void updateRestaurantStars (String resKey, int stars) {
        Query query = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO).child(resKey).child("stars");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> star = new HashMap<>();
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + resKey);
                if(dataSnapshot.exists()){
                    int s = ((Long)dataSnapshot.child("tot_stars").getValue()).intValue();
                    int p = ((Long)dataSnapshot.child("tot_review").getValue()).intValue();
                    star.put("stars", new StarItem(s+stars, p+1, -s-stars));
                    myRef.updateChildren(star);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

