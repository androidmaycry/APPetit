package com.mad.appetit.OrderActivities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mad.appetit.R;
import com.mad.mylibrary.OrderItem;

import java.util.ArrayList;

import static com.mad.mylibrary.SharedClass.ACCEPTED_ORDER_PATH;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.ROOT_UID;

public class Order extends Fragment {

    private OnFragmentInteractionListener mListener;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerAdapterOrdered mAdapter_ordered;
    private RecyclerView recyclerView_accepted, recyclerView_ordered;
    private FirebaseRecyclerAdapter<OrderItem, ViewHolderReservation> mAdapter_accepted;
    private static FirebaseRecyclerOptions<OrderItem> options =
            new FirebaseRecyclerOptions.Builder<OrderItem>()
                    .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + ROOT_UID
                                    + "/" + ACCEPTED_ORDER_PATH),
                            OrderItem.class).build();

    public Order() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        recyclerView_accepted = view.findViewById(R.id.reservation_list_accepted);
        mAdapter_accepted = new FirebaseRecyclerAdapter<OrderItem, ViewHolderReservation>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolderReservation holder, int position, @NonNull OrderItem model) {
                holder.setData(model, position);

                holder.getView().findViewById(R.id.confirm_reservation).setVisibility(View.INVISIBLE);
                holder.getView().findViewById(R.id.delete_reservation).setVisibility(View.INVISIBLE);

                holder.getView().findViewById(R.id.open_reservation).setOnClickListener(k ->
                        viewOrder(getRef(position).getKey()));
            }

            @NonNull
            @Override
            public ViewHolderReservation onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.reservation_listview, parent, false);

                return new ViewHolderReservation(view);
            }
        };

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView_accepted.setAdapter(mAdapter_accepted);
        recyclerView_accepted.setLayoutManager(layoutManager);

        return view;
    }

    public void viewOrder(String id){
        AlertDialog reservationDialog = new AlertDialog.Builder(this.getContext()).create();
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        final View view = inflater.inflate(R.layout.dishes_list_dialog, null);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query query = database.getReference().child(RESTAURATEUR_INFO + "/" + ROOT_UID
                + "/" + ACCEPTED_ORDER_PATH).child(id).child("dishes");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ArrayList<String> dishes = new ArrayList<>();

                    for(DataSnapshot d : dataSnapshot.getChildren()){
                        dishes.add(d.getKey() + " - Quantity: " + d.getValue(Integer.class));
                    }

                    recyclerView_ordered = view.findViewById(R.id.ordered_list);
                    mAdapter_ordered = new RecyclerAdapterOrdered(reservationDialog.getContext(), dishes);
                    layoutManager = new LinearLayoutManager(reservationDialog.getContext());
                    recyclerView_ordered.setAdapter(mAdapter_ordered);
                    recyclerView_ordered.setLayoutManager(layoutManager);

                    view.findViewById(R.id.back).setOnClickListener(e -> reservationDialog.dismiss());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("RESERVATION", "Failed to read value.", error.toException());
            }
        });

        reservationDialog.setView(view);
        reservationDialog.setTitle("Order");

        reservationDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter_accepted.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter_accepted.stopListening();
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
