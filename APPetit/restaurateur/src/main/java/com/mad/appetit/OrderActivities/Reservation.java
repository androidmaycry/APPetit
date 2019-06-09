package com.mad.appetit.OrderActivities;

import static com.mad.mylibrary.SharedClass.*;
import static com.mad.mylibrary.Utilities.getDateFromTimestamp;

import com.google.firebase.database.DatabaseReference;
import com.mad.appetit.R;
import com.mad.mylibrary.OrderItem;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

class ViewHolderReservation extends RecyclerView.ViewHolder{
    private View view;
    private TextView name, addr, cell, time, price;
    private int position;

    public ViewHolderReservation(View itemView){
        super(itemView);

        this.name = itemView.findViewById(R.id.listview_name);
        this.addr = itemView.findViewById(R.id.listview_address);
        this.cell = itemView.findViewById(R.id.listview_cellphone);
        this.time = itemView.findViewById(R.id.textView_time);
        this.price = itemView.findViewById(R.id.listview_price);
        this.view = itemView;
    }

    void setData(OrderItem current, int pos){
        Query query = FirebaseDatabase.getInstance().getReference(CUSTOMER_PATH).child(current.getKey()).child("customer_info");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    name.setText(dataSnapshot.child("name").getValue(String.class));
                    addr.setText(current.getAddrCustomer());
                    cell.setText(dataSnapshot.child("phone").getValue(String.class));
                    time.setText(getDateFromTimestamp(current.getTime()));
                    price.setText(current.getTotPrice());
                    position = pos;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public View getView() {
        return view;
    }

}

public class Reservation extends Fragment {
    private FirebaseRecyclerAdapter<OrderItem, ViewHolderReservation> mAdapter;
    private RecyclerAdapterOrdered mAdapter_ordered;
    private RecyclerView.LayoutManager layoutManager;

    private static FirebaseRecyclerOptions<OrderItem> options =
            new FirebaseRecyclerOptions.Builder<OrderItem>()
                    .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + ROOT_UID
                            + "/" + RESERVATION_PATH),
                            OrderItem.class).build();

    private Reservation.OnFragmentInteractionListener mListener;
    private RecyclerView recyclerView, recyclerView_ordered;

    public Reservation() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservation, container, false);

        recyclerView = view.findViewById(R.id.ordered_list);
        mAdapter = new FirebaseRecyclerAdapter<OrderItem, ViewHolderReservation>(options) {
            @NonNull
            @Override
            public ViewHolderReservation onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.reservation_listview, viewGroup, false);

                return new ViewHolderReservation(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolderReservation holder, int position, @NonNull OrderItem model) {
                holder.setData(model, position);
                String keyOrder = getRef(position).getKey();

                holder.getView().findViewById(R.id.confirm_reservation).setOnClickListener(e -> {
                    Intent mapsIntent = new Intent(getContext(), MapsActivity.class);
                    mapsIntent.putExtra(ORDER_ID, keyOrder);
                    mapsIntent.putExtra(CUSTOMER_ID, model.getKey());
                    startActivity(mapsIntent);
                });

                holder.getView().findViewById(R.id.delete_reservation).setOnClickListener(h ->
                        removeOrder(keyOrder, model.getKey()));

                holder.getView().findViewById(R.id.open_reservation).setOnClickListener(k ->
                        viewOrder(keyOrder));
            }
        };

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    public void removeOrder(String keyOrder, String keyCustomer){
        AlertDialog reservationDialog = new AlertDialog.Builder(this.getContext()).create();
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        final View view = inflater.inflate(R.layout.reservation_dialog, null);

        view.findViewById(R.id.button_confirm).setOnClickListener(e -> {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            Query queryDel = database.getReference().child(RESTAURATEUR_INFO + "/" + ROOT_UID
                    + "/" + RESERVATION_PATH).child(keyOrder);

            queryDel.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for(DataSnapshot d : dataSnapshot.getChildren())
                            d.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("RESERVATION", "Failed to read value.", error.toException());
                }
            });

            mAdapter.notifyDataSetChanged();

            //setting status canceled of the order to customer
            DatabaseReference refCustomerOrder = FirebaseDatabase.getInstance()
                    .getReference().child(CUSTOMER_PATH + "/" + keyCustomer).child("orders").child(keyOrder);
            HashMap<String, Object> order = new HashMap<>();
            order.put("status", STATUS_DISCARDED);
            refCustomerOrder.updateChildren(order);

            reservationDialog.dismiss();
        });

        view.findViewById(R.id.button_cancel).setOnClickListener(e -> reservationDialog.dismiss());

        reservationDialog.setView(view);
        reservationDialog.setTitle("Delete Reservation?");

        reservationDialog.show();
    }

    public void viewOrder(String id){
        AlertDialog reservationDialog = new AlertDialog.Builder(this.getContext()).create();
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        final View view = inflater.inflate(R.layout.dishes_list_dialog, null);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query query = database.getReference().child(RESTAURATEUR_INFO + "/" + ROOT_UID
                + "/" + RESERVATION_PATH).child(id).child("dishes");

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
        void onFragmentInteraction(Uri uri);
    }
}