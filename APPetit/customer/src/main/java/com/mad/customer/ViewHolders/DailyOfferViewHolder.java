package com.mad.customer.ViewHolders;

import android.os.StrictMode;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mad.customer.Items.DishItem;
import com.mad.customer.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DailyOfferViewHolder extends RecyclerView.ViewHolder{
    private ImageView dishPhoto;
    private TextView dishName, dishDesc, dishPrice, dishQuantity;
    private View view;


    public DailyOfferViewHolder(View itemView) {
        super(itemView);
        view = itemView;
        dishName = itemView.findViewById(R.id.dish_name);
        dishDesc = itemView.findViewById(R.id.dish_desc);
        dishPrice = itemView.findViewById(R.id.dish_price);
        dishPhoto = itemView.findViewById(R.id.dish_image);

    }

    public void setData(DishItem current, int position) {
        InputStream inputStream = null;

        this.dishName.setText(current.getName());
        this.dishDesc.setText(current.getDesc());
        this.dishPrice.setText(current.getPrice() + " €");
        if (current.getPhotoUri() != null) {
            Glide.with(view.getContext()).load(current.getPhotoUri()).override(80,80).into(dishPhoto);
        }
    }

    public View getView() {
        return view;
    }

}
