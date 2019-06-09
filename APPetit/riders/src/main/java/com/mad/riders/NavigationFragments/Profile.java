package com.mad.riders.NavigationFragments;

import static com.mad.mylibrary.SharedClass.*;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.mylibrary.User;
import com.mad.riders.ProfileManagment.EditPassword;
import com.mad.riders.ProfileManagment.EditProfile;
import com.mad.riders.MainActivity;
import com.mad.riders.R;

import java.util.Objects;

public class Profile extends Fragment {

    private OnFragmentInteractionListener mListener;

    private View view;

    public Profile() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Profile newInstance(String param1, String param2) {
        Profile fragment = new Profile();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(RIDERS_PATH).child(ROOT_UID);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                User user;
                user = dataSnapshot.child("rider_info").getValue(User.class);
                ((TextView) view.findViewById(R.id.name)).setText(user.getName());
                ((TextView) view.findViewById(R.id.surname)).setText(user.getSurname());
                ((TextView) view.findViewById(R.id.mail)).setText(user.getEmail());
                ((TextView) view.findViewById(R.id.phone)).setText(user.getPhone());

                if(user.getPhotoPath() != null)
                    Glide.with(Objects.requireNonNull(view.getContext()))
                            .load(user.getPhotoPath())
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .into((ImageView)view.findViewById(R.id.profile_image));
                else
                    Glide.with(Objects.requireNonNull(view.getContext()))
                            .load(R.drawable.rider)
                            .into((ImageView)view.findViewById(R.id.profile_image));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
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
            case R.id.edit_password:
                Intent editPsw = new Intent(getContext(), EditPassword.class);
                startActivity(editPsw);
                return true;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();

                Intent mainActivity = new Intent(getContext(), MainActivity.class);
                mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
