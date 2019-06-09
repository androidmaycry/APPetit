package com.mad.customer.UI;

import static com.mad.mylibrary.SharedClass.CUSTOMER_PATH;
import static com.mad.mylibrary.SharedClass.ROOT_UID;
import static com.mad.mylibrary.SharedClass.user;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mad.customer.R;
import com.mad.mylibrary.User;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Profile.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Profile#newInstance} factory method to
 * create an instance of this fragment.
 */

public class Profile extends Fragment {
    private OnFragmentInteractionListener mListener;
    private View view;

    public Profile() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Profile newInstance() {
        Profile fragment = new Profile();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        view.findViewById(R.id.loadingProfile).setVisibility(View.GONE);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(CUSTOMER_PATH).child(ROOT_UID);
        setHasOptionsMenu(true);
        Log.d("PATH",CUSTOMER_PATH+"/"+ROOT_UID);

        view.findViewById(R.id.button_logout).setOnClickListener(e -> {
            auth.signOut();

            Intent mainActivity = new Intent(getContext(), MainActivity.class);
            mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainActivity);
        });

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("ROOT_UID" , ROOT_UID);
                user = dataSnapshot.child("customer_info").getValue(User.class);

                ((TextView) view.findViewById(R.id.name)).setText(user.getName());
                ((TextView) view.findViewById(R.id.surname)).setText(user.getSurname());
                ((TextView) view.findViewById(R.id.mail)).setText(user.getEmail());
                ((TextView) view.findViewById(R.id.phone)).setText(user.getPhone());
                ((TextView) view.findViewById(R.id.address)).setText(user.getAddr());

                if(user.getPhotoPath() != null)
                    Glide.with(Objects.requireNonNull(view.getContext()))
                            .load(user.getPhotoPath())
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .into((ImageView)view.findViewById(R.id.profile_image));
                else
                    Glide.with(Objects.requireNonNull(view.getContext()))
                            .load(R.drawable.person)
                            .into((ImageView)view.findViewById(R.id.profile_image));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("MAIN", "Failed to read value.", error.toException());
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.add:
                Intent i = new Intent(getContext(), EditProfile.class);
                startActivity(i);
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
    public void onResume() {
        super.onResume();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(CUSTOMER_PATH).child(ROOT_UID);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("ROOT_UID" , ROOT_UID);
                user = dataSnapshot.child("customer_info").getValue(User.class);

                ((TextView) view.findViewById(R.id.name)).setText(user.getName());
                ((TextView) view.findViewById(R.id.surname)).setText(user.getSurname());
                ((TextView) view.findViewById(R.id.mail)).setText(user.getEmail());
                ((TextView) view.findViewById(R.id.phone)).setText(user.getPhone());
                ((TextView) view.findViewById(R.id.address)).setText(user.getAddr());

                if(user.getPhotoPath() != null)
                    Glide.with(Objects.requireNonNull(view.getContext()))
                            .load(user.getPhotoPath())
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .into((ImageView)view.findViewById(R.id.profile_image));
                else
                    Glide.with(Objects.requireNonNull(view.getContext()))
                            .load(R.drawable.person)
                            .into((ImageView)view.findViewById(R.id.profile_image));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("MAIN", "Failed to read value.", error.toException());
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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