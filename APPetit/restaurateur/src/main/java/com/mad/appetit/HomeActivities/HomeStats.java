package com.mad.appetit.HomeActivities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mad.appetit.R;
import com.mad.mylibrary.DishItem;
import com.mad.mylibrary.OrderItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import static com.mad.mylibrary.SharedClass.DISHES_PATH;
import static com.mad.mylibrary.Utilities.getDateFromTimestamp;
import static com.mad.mylibrary.SharedClass.ACCEPTED_ORDER_PATH;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.ROOT_UID;

class ViewHolderDailyOfferTiming extends RecyclerView.ViewHolder{
    private ImageView dishPhoto;
    private TextView dishName, dishDesc, dishPrice, dishQuantity;
    private DishItem current;
    private int position;

    ViewHolderDailyOfferTiming(View itemView){
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

public class HomeStats extends Fragment {
    private OnFragmentInteractionListener mListener;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseRecyclerAdapter<DishItem, ViewHolderDailyOfferTiming> mAdapter;

    private static FirebaseRecyclerOptions<DishItem> options;

    public HomeStats() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_stats, container, false);

        recyclerView = view.findViewById(R.id.dish_list);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        Query checkBestTime = FirebaseDatabase.getInstance().getReference().child(RESTAURATEUR_INFO + "/" +
                ROOT_UID + "/" + ACCEPTED_ORDER_PATH);
        checkBestTime.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    TreeMap<String, Integer> timeOrderMap = new TreeMap<>(); //key = hour, value = number of order at that hour
                    HashMap<String, ArrayList<String>> timeDishesMap = new HashMap<>(); //key = hour, value = dishes name bought at that hour

                    for(DataSnapshot d : dataSnapshot.getChildren()){
                        OrderItem orderItem = d.getValue(OrderItem.class);
                        String orderTime = getDateFromTimestamp(orderItem.getTime());
                        String hour = orderTime.split(":")[0];
                        Integer value = timeOrderMap.get(hour);

                        if(value != null){
                            timeOrderMap.put(hour, ++value);
                            ArrayList dishes = timeDishesMap.get(hour);
                            for(String key : orderItem.getDishes().keySet())
                                if(!dishes.contains(key))
                                    dishes.add(key);
                            timeDishesMap.put(hour, dishes);
                        }
                        else{
                            timeOrderMap.put(hour, 0);
                            ArrayList dishes = new ArrayList();
                            for(String key : orderItem.getDishes().keySet())
                                if(!dishes.contains(key))
                                    dishes.add(key);
                            timeDishesMap.put(hour, dishes);
                        }
                    }

                    String bestTime = timeOrderMap.firstEntry().getKey();

                    ((TextView)(view.findViewById(R.id.analytics_text))).setText(getAnalyticsText(bestTime));
                    ArrayList<String> mostTimeDishes = timeDishesMap.get(bestTime);

                    Query getMostTimeDishes = FirebaseDatabase.getInstance().getReference().child(RESTAURATEUR_INFO + "/" +
                            ROOT_UID + "/" + DISHES_PATH);
                    options = new FirebaseRecyclerOptions.Builder<DishItem>()
                            .setQuery(getMostTimeDishes, snapshot -> {
                                DishItem dishItem = snapshot.getValue(DishItem.class);
                                String dishName = dishItem.getName();

                                for(String name : mostTimeDishes) {
                                    if(name.toLowerCase().equals(dishName.toLowerCase())){
                                        return dishItem;
                                    }
                                }

                                return new DishItem();
                            }).build();

                    mAdapter = new FirebaseRecyclerAdapter<DishItem, ViewHolderDailyOfferTiming>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ViewHolderDailyOfferTiming holder, int position, @NonNull DishItem model) {
                            if(model.getName().equals(""))
                                holder.itemView.setVisibility(View.GONE);
                            else
                                holder.setData(model, position);
                        }

                        @NonNull
                        @Override
                        public ViewHolderDailyOfferTiming onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.dailyoffer_listview, parent, false);

                            view.findViewById(R.id.delete_offer).setVisibility(View.GONE);
                            view.findViewById(R.id.edit_offer).setVisibility(View.GONE);

                            return new ViewHolderDailyOfferTiming(view);
                        }
                    };

                    recyclerView.setAdapter(mAdapter);
                    mAdapter.startListening();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.favourite_dishes:
                if(HomeStats.this.isVisible())
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new Home()).commit();
                return true;
            case R.id.advanced_stats:
                if(!HomeStats.this.isVisible())
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new HomeStats()).commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getAnalyticsText(String bestTime){
        Integer nexTime = Integer.parseInt(bestTime)+1;
        String nextTimeString;

        if(nexTime < 9)
            nextTimeString = "0" + nexTime;
        else
            nextTimeString = nexTime.toString();

        return "Time slot with more orders is: " + bestTime + ":00" + " - " + nextTimeString + ":00";
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAdapter != null)
            mAdapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        //mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAdapter != null)
            mAdapter.stopListening();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
