package com.mad.customer.UI;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.mad.customer.R;
import com.mad.customer.ViewHolders.RatingViewHolder;
import com.mad.mylibrary.Restaurateur;
import com.mad.mylibrary.ReviewItem;


import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;


public class RatingFragment extends Fragment {

    private static String key;
    private Restaurateur item;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<ReviewItem, RatingViewHolder> mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    public RatingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rating, container, false);
        key = ((TabApp)getActivity()).getKey();
        item = ((TabApp)getActivity()).getItem();

        FirebaseRecyclerOptions<ReviewItem> options =
                new FirebaseRecyclerOptions.Builder<ReviewItem>()
                        .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO).child(key).child("review"),
                                ReviewItem.class).build();
        recyclerView = view.findViewById(R.id.rating_recyclerview);

        mAdapter = new FirebaseRecyclerAdapter<ReviewItem, RatingViewHolder>(options) {
            @NonNull
            @Override
            public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rating_item, viewGroup, false);
                return new RatingViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull RatingViewHolder holder, int position, @NonNull ReviewItem model) {
                holder.setData(model);
            }
        };

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(layoutManager);
        return view;
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

}
