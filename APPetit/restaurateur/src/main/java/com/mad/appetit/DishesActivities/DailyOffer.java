package com.mad.appetit.DishesActivities;

import static com.mad.mylibrary.SharedClass.*;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mad.appetit.R;
import com.mad.mylibrary.DishItem;

class ViewHolderDailyOffer extends RecyclerView.ViewHolder{
    private ImageView dishPhoto;
    private TextView dishName, dishDesc, dishPrice, dishQuantity;
    private DishItem current;
    private int position;

    ViewHolderDailyOffer(View itemView){
        super(itemView);

        dishName = itemView.findViewById(R.id.dish_name);
        dishDesc = itemView.findViewById(R.id.dish_desc);
        dishPrice = itemView.findViewById(R.id.dish_price);
        dishQuantity = itemView.findViewById(R.id.dish_quant);
        dishPhoto = itemView.findViewById(R.id.dish_image);
    }

    void setData(DishItem current, int position){
        this.dishName.setText(current.getName());
        this.dishDesc.setText(current.getDesc());
        this.dishPrice.setText(current.getPrice() + " €");
        this.dishQuantity.setText(String.valueOf(current.getQuantity()));

        if(current.getPhoto() != null)
            Glide.with(itemView.getContext()).load(current.getPhoto()).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(dishPhoto);
        else
            Glide.with(itemView.getContext()).load(R.drawable.hamburger).into(dishPhoto);

        this.position = position;
        this.current = current;
    }
}

public class DailyOffer extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseRecyclerAdapter<DishItem, ViewHolderDailyOffer> mAdapter;
    private OnFragmentInteractionListener mListener;

    private FirebaseRecyclerOptions<DishItem> options =
            new FirebaseRecyclerOptions.Builder<DishItem>()
                    .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" +
                            ROOT_UID + "/" + DISHES_PATH),
                            DishItem.class).build();

    public DailyOffer() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dailyoffer, container, false);

        recyclerView = view.findViewById(R.id.dish_list);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new FirebaseRecyclerAdapter<DishItem, ViewHolderDailyOffer>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolderDailyOffer holder, int position, @NonNull DishItem model) {
                holder.setData(model, position);
            }

            @NonNull
            @Override
            public ViewHolderDailyOffer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.dailyoffer_listview, parent, false);

                view.findViewById(R.id.delete_offer).setOnClickListener(e -> {
                    deleteDish(((TextView)view.findViewById(R.id.dish_name)).getText().toString());
                });

                view.findViewById(R.id.edit_offer).setOnClickListener(h ->
                        editDailyOffer(((TextView)view.findViewById(R.id.dish_name)).getText().toString()));

                return new ViewHolderDailyOffer(view);
            }
        };

        recyclerView.setAdapter(mAdapter);

        return view;
    }

    public void editDailyOffer(String dishName){
        Intent editOffer = new Intent(getContext(), EditOffer.class);
        editOffer.putExtra(EDIT_EXISTING_DISH, dishName);
        startActivity(editOffer);
    }

    public void deleteDish(String dishName){
        AlertDialog reservationDialog = new AlertDialog.Builder(this.getContext()).create();
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        final View view = inflater.inflate(R.layout.reservation_dialog, null);

        view.findViewById(R.id.button_confirm).setOnClickListener(e -> {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            Query query = myRef.child(RESTAURATEUR_INFO + "/" + ROOT_UID + "/" + DISHES_PATH).orderByChild("name").equalTo(dishName);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for(DataSnapshot d : dataSnapshot.getChildren()) {
                            d.getRef().removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("DAILY OFFER", "Failed to read value.", error.toException());
                }
            });

            mAdapter.notifyDataSetChanged();

            reservationDialog.dismiss();
        });
        view.findViewById(R.id.button_cancel).setOnClickListener(e -> reservationDialog.dismiss());

        reservationDialog.setView(view);
        reservationDialog.setTitle("Delete Dish?");

        reservationDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_daily, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.add:
                Intent edit_profile = new Intent(getContext(), EditOffer.class);
                startActivityForResult(edit_profile, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}