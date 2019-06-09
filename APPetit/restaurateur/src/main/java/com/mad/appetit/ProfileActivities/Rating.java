package com.mad.appetit.ProfileActivities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

import com.mad.appetit.R;
import com.mad.mylibrary.ReviewItem;

import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.ROOT_UID;

class RatingViewHolder extends RecyclerView.ViewHolder {
    private TextView name, comment;
    private RatingBar ratingBar;
    private ImageView img;
    private View view;

    public RatingViewHolder(@NonNull View itemView) {
        super(itemView);
        this.view = itemView;
        this.name = itemView.findViewById(R.id.rating_item_name);
        this.comment  = itemView.findViewById(R.id.rating_item_comment);
        this.ratingBar = itemView.findViewById(R.id.ratingbaritem);
        this.img = itemView.findViewById(R.id.rating_item_img);
    }

    public View getView() {
        return view;
    }

    public void setData (ReviewItem ri){
        name.setText(ri.getName());

        ratingBar.setRating(ri.getStars());

        if(ri.getComment() != null)
            comment.setText(ri.getComment());
        else
            comment.setVisibility(View.GONE);

        if(!ri.getImg().isEmpty() && ri.getImg() != null && !ri.getImg().equals("null"))
            Glide.with(view.getContext()).load(ri.getImg()).into(img);
    }
}

public class Rating extends Fragment {

    private OnFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<ReviewItem, RatingViewHolder> mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private static FirebaseRecyclerOptions<ReviewItem> options =
            new FirebaseRecyclerOptions.Builder<ReviewItem>()
                    .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO).child(ROOT_UID).child("review"),
                            ReviewItem.class).build();

    public Rating() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rating, container, false);

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
