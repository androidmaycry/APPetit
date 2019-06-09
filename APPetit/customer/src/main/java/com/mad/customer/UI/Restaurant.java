package com.mad.customer.UI;

import static com.mad.mylibrary.SharedClass.*;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mad.customer.R;
import com.mad.customer.ViewHolders.RestaurantViewHolder;
import com.mad.mylibrary.Restaurateur;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SearchView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashSet;
import java.util.LinkedList;

public class Restaurant extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Restaurateur, RestaurantViewHolder> mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private HashSet<String> cuisineType = new HashSet<>();
    private HashSet<Chip> chips = new HashSet<>();
    private ChipGroup entryChipGroup;
    private boolean icon_pop=false;
    private Menu menu;
    private boolean flag = true,favourites_selected;
    LinkedList<String> keys_favorite_restaurant;


    private FirebaseRecyclerOptions<Restaurateur> options =
            new FirebaseRecyclerOptions.Builder<Restaurateur>()
                    .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO),
                            new SnapshotParser<Restaurateur>(){
                                @NonNull
                                @Override
                                public Restaurateur parseSnapshot(@NonNull DataSnapshot snapshot) {
                                    Restaurateur searchRest;
                                    if(snapshot.child("info").child("photoUri").getValue() == null){
                                        searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                snapshot.child("info").child("name").getValue().toString(),
                                                snapshot.child("info").child("addr").getValue().toString(),
                                                snapshot.child("info").child("cuisine").getValue().toString(),
                                                snapshot.child("info").child("openingTime").getValue().toString(),
                                                snapshot.child("info").child("phone").getValue().toString(),
                                                "null");
                                    }
                                    else{
                                        searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                snapshot.child("info").child("name").getValue().toString(),
                                                snapshot.child("info").child("addr").getValue().toString(),
                                                snapshot.child("info").child("cuisine").getValue().toString(),
                                                snapshot.child("info").child("openingTime").getValue().toString(),
                                                snapshot.child("info").child("phone").getValue().toString(),
                                                snapshot.child("info").child("photoUri").getValue().toString());
                                    }
                                    return searchRest;
                                }
                            }).build();

    public Restaurant() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);
        setHasOptionsMenu(true);

        entryChipGroup = view.findViewById(R.id.chip_group);
        recyclerView = view.findViewById(R.id.restaurant_recyclerview);
        //recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        DatabaseReference fav_ref = FirebaseDatabase
                .getInstance().getReference(CUSTOMER_PATH)
                .child(ROOT_UID).child(CUSTOMER_FAVOURITE_RESTAURANT_PATH);
        fav_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                keys_favorite_restaurant = new LinkedList<>();

                for(DataSnapshot d : dataSnapshot.getChildren()){
                    keys_favorite_restaurant.add(d.getKey());
                }

                //mAdapter.stopListening();

                    mAdapter = new FirebaseRecyclerAdapter<Restaurateur, RestaurantViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position, @NonNull Restaurateur model) {
                            String key = getRef(position).getKey();
                            holder.setIsRecyclable(false);
                            holder.setData(model, position, key);

                        }

                        @NonNull
                        @Override
                        public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_item, parent, false);
                            RestaurantViewHolder resViewHolder = new RestaurantViewHolder(view, getContext());
                            resViewHolder.setFavorite(keys_favorite_restaurant);

                            return resViewHolder;
                        }
                    };
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.startListening();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        recyclerView.setAdapter(mAdapter);

        Query query = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        if(cuisineType.add(d.child("info").child("cuisine").getValue().toString())){
                            Log.d("CHIP", "building");
                            Chip chip = new Chip(view.getContext());
                            chip.setCheckable(true);
                            chip.setText(d.child("info").child("cuisine").getValue().toString());
                            chips.add(chip);
                            entryChipGroup.addView(chip);
                            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                if(isChecked && flag) {
                                    setFilter(d.child("info").child("cuisine").getValue().toString());
                                }
                            });
                        }
                    }
                }

                entryChipGroup.setOnCheckedChangeListener((chipGroup, i) -> {
                    if(chipGroup.getCheckedChipId() == View.NO_ID){
                        mAdapter.stopListening();
                        options =
                                new FirebaseRecyclerOptions.Builder<Restaurateur>()
                                        .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO),
                                                new SnapshotParser<Restaurateur>(){
                                                    @NonNull
                                                    @Override
                                                    public Restaurateur parseSnapshot(@NonNull DataSnapshot snapshot) {
                                                        Restaurateur searchRest;
                                                        if(snapshot.child("info").child("photoUri").getValue() == null){
                                                            searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                                    snapshot.child("info").child("name").getValue().toString(),
                                                                    snapshot.child("info").child("addr").getValue().toString(),
                                                                    snapshot.child("info").child("cuisine").getValue().toString(),
                                                                    snapshot.child("info").child("openingTime").getValue().toString(),
                                                                    snapshot.child("info").child("phone").getValue().toString(),
                                                                    "null");
                                                        }
                                                        else{
                                                            searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                                    snapshot.child("info").child("name").getValue().toString(),
                                                                    snapshot.child("info").child("addr").getValue().toString(),
                                                                    snapshot.child("info").child("cuisine").getValue().toString(),
                                                                    snapshot.child("info").child("openingTime").getValue().toString(),
                                                                    snapshot.child("info").child("phone").getValue().toString(),
                                                                    snapshot.child("info").child("photoUri").getValue().toString());
                                                        }
                                                        return searchRest;
                                                    }
                                                }).build();

                        mAdapter = new FirebaseRecyclerAdapter<Restaurateur, RestaurantViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position, @NonNull Restaurateur model) {
                                String key = getRef(position).getKey();
                                holder.setData(model, position, key);
                            }

                            @NonNull
                            @Override
                            public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_item,parent,false);
                                RestaurantViewHolder resViewHolder = new RestaurantViewHolder(view1,getContext());
                                resViewHolder.setFavorite(keys_favorite_restaurant);

                                return resViewHolder;
                            }

                        };

                        recyclerView.setAdapter(mAdapter);
                        mAdapter.startListening();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);
        final MenuItem searchItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        this.menu = menu;
        MenuItem heart = menu.findItem(R.id.favorite_res);
        heart.setOnMenuItemClickListener(e -> {
                    favourites_selected = !favourites_selected;
                    mAdapter.stopListening();
                    if (favourites_selected) {
                        heart.setIcon(R.drawable.heart_fill_white);
                        options = new FirebaseRecyclerOptions.Builder<Restaurateur>()
                                .setQuery(FirebaseDatabase.getInstance()
                                                .getReference(CUSTOMER_PATH)
                                                .child(ROOT_UID).child(CUSTOMER_FAVOURITE_RESTAURANT_PATH),
                                        new SnapshotParser<Restaurateur>() {
                                            @NonNull
                                            @Override
                                            public Restaurateur parseSnapshot(@NonNull DataSnapshot snapshot) {
                                                Restaurateur searchRest;
                                                if (snapshot.child("photoUri").getValue() == null) {
                                                    searchRest = new Restaurateur(snapshot.child("mail").getValue().toString(),
                                                            snapshot.child("name").getValue().toString(),
                                                            snapshot.child("addr").getValue().toString(),
                                                            snapshot.child("cuisine").getValue().toString(),
                                                            snapshot.child("openingTime").getValue().toString(),
                                                            snapshot.child("phone").getValue().toString(),
                                                            "null");
                                                } else {
                                                    searchRest = new Restaurateur(snapshot.child("mail").getValue().toString(),
                                                            snapshot.child("name").getValue().toString(),
                                                            snapshot.child("addr").getValue().toString(),
                                                            snapshot.child("cuisine").getValue().toString(),
                                                            snapshot.child("openingTime").getValue().toString(),
                                                            snapshot.child("phone").getValue().toString(),
                                                            snapshot.child("photoUri").getValue().toString());
                                                }
                                                return searchRest;
                                            }
                                        }).build();
                    } else {
                        heart.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.heart_white));
                        options =
                                new FirebaseRecyclerOptions.Builder<Restaurateur>()
                                        .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO),
                                                new SnapshotParser<Restaurateur>() {
                                                    @NonNull
                                                    @Override
                                                    public Restaurateur parseSnapshot(@NonNull DataSnapshot snapshot) {
                                                        Restaurateur searchRest;
                                                        if (snapshot.child("info").child("photoUri").getValue() == null) {
                                                            searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                                    snapshot.child("info").child("name").getValue().toString(),
                                                                    snapshot.child("info").child("addr").getValue().toString(),
                                                                    snapshot.child("info").child("cuisine").getValue().toString(),
                                                                    snapshot.child("info").child("openingTime").getValue().toString(),
                                                                    snapshot.child("info").child("phone").getValue().toString(),
                                                                    "null");
                                                        } else {
                                                            searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                                    snapshot.child("info").child("name").getValue().toString(),
                                                                    snapshot.child("info").child("addr").getValue().toString(),
                                                                    snapshot.child("info").child("cuisine").getValue().toString(),
                                                                    snapshot.child("info").child("openingTime").getValue().toString(),
                                                                    snapshot.child("info").child("phone").getValue().toString(),
                                                                    snapshot.child("info").child("photoUri").getValue().toString());
                                                        }
                                                        return searchRest;
                                                    }
                                                }).build();
                    }
                    mAdapter = new FirebaseRecyclerAdapter<Restaurateur, RestaurantViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position, @NonNull Restaurateur model) {
                            String key = getRef(position).getKey();
                            holder.setData(model, position, key);
                        }

                        @NonNull
                        @Override
                        public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_item,parent,false);
                            RestaurantViewHolder resViewHolder = new RestaurantViewHolder(view,getContext());
                            resViewHolder.setFavorite(keys_favorite_restaurant);

                            return resViewHolder;
                        }
                    };
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.startListening();
                    return false;
        });
        MenuItem pop = menu.findItem(R.id.most_popular_res);
        pop.setOnMenuItemClickListener(d->{
            icon_pop=!icon_pop;
            if(icon_pop){
                pop.setIcon(R.drawable.ic_restaurant);
                mAdapter.stopListening();
                options = new FirebaseRecyclerOptions.Builder<Restaurateur>()
                        .setQuery(FirebaseDatabase.getInstance()
                                        .getReference(RESTAURATEUR_INFO)
                                        .orderByChild("stars/sort"),
                                new SnapshotParser<Restaurateur>(){
                                    @NonNull
                                    @Override
                                    public Restaurateur parseSnapshot(@NonNull DataSnapshot snapshot) {
                                        Restaurateur searchRest;
                                        if(snapshot.child("info").child("photoUri").getValue() == null){
                                            searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                    snapshot.child("info").child("name").getValue().toString(),
                                                    snapshot.child("info").child("addr").getValue().toString(),
                                                    snapshot.child("info").child("cuisine").getValue().toString(),
                                                    snapshot.child("info").child("openingTime").getValue().toString(),
                                                    snapshot.child("info").child("phone").getValue().toString(),
                                                    "null");
                                        }
                                        else{
                                            searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                    snapshot.child("info").child("name").getValue().toString(),
                                                    snapshot.child("info").child("addr").getValue().toString(),
                                                    snapshot.child("info").child("cuisine").getValue().toString(),
                                                    snapshot.child("info").child("openingTime").getValue().toString(),
                                                    snapshot.child("info").child("phone").getValue().toString(),
                                                    snapshot.child("info").child("photoUri").getValue().toString());
                                        }
                                        return searchRest;
                                    }
                                }).build();
            }
            else{
                pop.setIcon(R.drawable.ic_chart);
                mAdapter.stopListening();
                options = new FirebaseRecyclerOptions.Builder<Restaurateur>()
                        .setQuery(FirebaseDatabase.getInstance()
                                        .getReference(RESTAURATEUR_INFO),
                                new SnapshotParser<Restaurateur>(){
                                    @NonNull
                                    @Override
                                    public Restaurateur parseSnapshot(@NonNull DataSnapshot snapshot) {
                                        Restaurateur searchRest;
                                        if(snapshot.child("info").child("photoUri").getValue() == null){
                                            searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                    snapshot.child("info").child("name").getValue().toString(),
                                                    snapshot.child("info").child("addr").getValue().toString(),
                                                    snapshot.child("info").child("cuisine").getValue().toString(),
                                                    snapshot.child("info").child("openingTime").getValue().toString(),
                                                    snapshot.child("info").child("phone").getValue().toString(),
                                                    "null");
                                        }
                                        else{
                                            searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                    snapshot.child("info").child("name").getValue().toString(),
                                                    snapshot.child("info").child("addr").getValue().toString(),
                                                    snapshot.child("info").child("cuisine").getValue().toString(),
                                                    snapshot.child("info").child("openingTime").getValue().toString(),
                                                    snapshot.child("info").child("phone").getValue().toString(),
                                                    snapshot.child("info").child("photoUri").getValue().toString());
                                        }
                                        return searchRest;
                                    }
                                }).build();

            }


            mAdapter = new FirebaseRecyclerAdapter<Restaurateur, RestaurantViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position, @NonNull Restaurateur model) {
                    String key = getRef(position).getKey();
                    holder.setData(model, position, key);
                }

                @NonNull
                @Override
                public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_item,parent,false);
                    RestaurantViewHolder resViewHolder = new RestaurantViewHolder(view,getContext());
                    resViewHolder.setFavorite(keys_favorite_restaurant);

                    return resViewHolder;
                }
            };
            recyclerView.setAdapter(mAdapter);
            mAdapter.startListening();

            return false;
        });

        searchView.setOnCloseListener(() -> {
            entryChipGroup.setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.navigation).setVisibility(View.VISIBLE);
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.stopListening();
                if(newText.length()==0){
                    entryChipGroup.setVisibility(View.GONE);
                    getActivity().findViewById(R.id.navigation).setVisibility(View.GONE);
                    options =
                            new FirebaseRecyclerOptions.Builder<Restaurateur>()
                                    .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO),
                                            snapshot -> {
                                                Restaurateur searchRest;
                                                if(snapshot.child("info").child("photoUri").getValue() == null){
                                                    searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                            snapshot.child("info").child("name").getValue().toString(),
                                                            snapshot.child("info").child("addr").getValue().toString(),
                                                            snapshot.child("info").child("cuisine").getValue().toString(),
                                                            snapshot.child("info").child("openingTime").getValue().toString(),
                                                            snapshot.child("info").child("phone").getValue().toString(),
                                                            "null");
                                                }
                                                else{
                                                    searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                            snapshot.child("info").child("name").getValue().toString(),
                                                            snapshot.child("info").child("addr").getValue().toString(),
                                                            snapshot.child("info").child("cuisine").getValue().toString(),
                                                            snapshot.child("info").child("openingTime").getValue().toString(),
                                                            snapshot.child("info").child("phone").getValue().toString(),
                                                            snapshot.child("info").child("photoUri").getValue().toString());
                                                }

                                                return searchRest;
                                            }).build();
                }
                else {
                    entryChipGroup.setVisibility(View.GONE);
                    getActivity().findViewById(R.id.navigation).setVisibility(View.GONE);
                    options =
                            new FirebaseRecyclerOptions.Builder<Restaurateur>()
                                    .setQuery(FirebaseDatabase.getInstance().getReference().child(RESTAURATEUR_INFO), snapshot -> {
                                        Restaurateur searchRest = new Restaurateur();

                                        if (snapshot.child("info").child("name").exists() && snapshot.child("info").child("name").getValue().toString().toLowerCase().contains(newText.toLowerCase())) {

                                            if (snapshot.child("info").child("photoUri").getValue() != null) {
                                                searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                        snapshot.child("info").child("name").getValue().toString(),
                                                        snapshot.child("info").child("addr").getValue().toString(),
                                                        snapshot.child("info").child("cuisine").getValue().toString(),
                                                        snapshot.child("info").child("openingTime").getValue().toString(),
                                                        snapshot.child("info").child("phone").getValue().toString(),
                                                        snapshot.child("info").child("photoUri").getValue().toString());
                                            } else {
                                                searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                                        snapshot.child("info").child("name").getValue().toString(),
                                                        snapshot.child("info").child("addr").getValue().toString(),
                                                        snapshot.child("info").child("cuisine").getValue().toString(),
                                                        snapshot.child("info").child("phone").getValue().toString(),
                                                        snapshot.child("info").child("openingTime").getValue().toString(),
                                                        "null");
                                            }
                                        }

                                        return searchRest;
                                    }).build();
                }
                mAdapter = new FirebaseRecyclerAdapter<Restaurateur, RestaurantViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position, @NonNull Restaurateur model) {
                        String key = getRef(position).getKey();
                        if(model.getName().equals("")){
                            holder.itemView.findViewById(R.id.restaurant).setLayoutParams(new FrameLayout.LayoutParams(0,0));
                            //holder.itemView.setLayoutParams(new ConstraintLayout.LayoutParams(0,0));
                        }
                        else {
                            holder.setData(model, position, key);
                        }
                    }

                    @NonNull
                    @Override
                    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_item,parent,false);
                        return new RestaurantViewHolder(view,getContext());
                    }
                };
                recyclerView.setAdapter(mAdapter);
                mAdapter.startListening();
                return false;
            }
        });




        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.search:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setFilter(String filter){
        mAdapter.stopListening();

        FirebaseRecyclerOptions<Restaurateur> options = new FirebaseRecyclerOptions.Builder<Restaurateur>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child(RESTAURATEUR_INFO), new SnapshotParser<Restaurateur>(){
                    @NonNull
                    @Override
                    public Restaurateur parseSnapshot(@NonNull DataSnapshot snapshot) {
                        Restaurateur searchRest = new Restaurateur();

                        if (snapshot.child("info").child("cuisine").exists() && snapshot.child("info").child("cuisine").getValue().toString().equals(filter)) {

                            if (snapshot.child("info").child("photoUri").getValue() != null) {
                                searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                        snapshot.child("info").child("name").getValue().toString(),
                                        snapshot.child("info").child("addr").getValue().toString(),
                                        snapshot.child("info").child("cuisine").getValue().toString(),
                                        snapshot.child("info").child("openingTime").getValue().toString(),
                                        snapshot.child("info").child("phone").getValue().toString(),
                                        snapshot.child("info").child("photoUri").getValue().toString());
                            } else {
                                searchRest = new Restaurateur(snapshot.child("info").child("mail").getValue().toString(),
                                        snapshot.child("info").child("name").getValue().toString(),
                                        snapshot.child("info").child("addr").getValue().toString(),
                                        snapshot.child("info").child("cuisine").getValue().toString(),
                                        snapshot.child("info").child("openingTime").getValue().toString(),
                                        snapshot.child("info").child("phone").getValue().toString(),
                                        "null");
                            }
                        }
                        return searchRest;
                    }
                }).build();

        mAdapter = new FirebaseRecyclerAdapter<Restaurateur, RestaurantViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position, @NonNull Restaurateur model) {
                String key = getRef(position).getKey();
                if(model.getName().equals("")){
                    holder.itemView.findViewById(R.id.restaurant).setLayoutParams(new FrameLayout.LayoutParams(0,0));
                    //holder.itemView.setLayoutParams(new ConstraintLayout.LayoutParams(0,0));
                }
                else {
                    holder.setData(model, position, key);
                }
            }

            @NonNull
            @Override
            public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_item,parent,false);
                return new RestaurantViewHolder(view,getContext());
            }
        };
        recyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mAdapter!=null) {
            mAdapter.startListening();
        }

    }
    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public interface OnFragmentInteractionListener {
    }
}

