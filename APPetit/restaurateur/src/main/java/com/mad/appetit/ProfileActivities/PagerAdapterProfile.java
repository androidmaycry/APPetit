package com.mad.appetit.ProfileActivities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mad.appetit.R;
import com.mad.appetit.Startup.MainActivity;
import com.mad.mylibrary.Restaurateur;
import com.mad.mylibrary.ReviewItem;

import java.util.Objects;

import static com.mad.mylibrary.SharedClass.Address;
import static com.mad.mylibrary.SharedClass.Description;
import static com.mad.mylibrary.SharedClass.Mail;
import static com.mad.mylibrary.SharedClass.Name;
import static com.mad.mylibrary.SharedClass.Phone;
import static com.mad.mylibrary.SharedClass.Photo;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.ROOT_UID;
import static com.mad.mylibrary.SharedClass.Time;

class PagerAdapter extends FragmentPagerAdapter {
    private int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new Profile();
            case 1:
                return new Rating();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}

public class PagerAdapterProfile extends Fragment {
    private String name, addr, descr, mail, phone, photoUri, time;
    private OnFragmentInteractionListener mListener;
    private PagerAdapter pagerAdapter;
    private TabLayout tab;
    private ViewPager viewPager;

    public PagerAdapterProfile() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_pager_adapter_profile, container, false);

        Query getRestaurantInfo = FirebaseDatabase.getInstance().getReference().child(RESTAURATEUR_INFO + "/" + ROOT_UID).child("info");
        getRestaurantInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Restaurateur restaurateur = dataSnapshot.getValue(Restaurateur.class);

                    name = restaurateur.getName();
                    addr = restaurateur.getAddr();
                    descr = restaurateur.getCuisine();
                    mail = restaurateur.getMail();
                    phone = restaurateur.getPhone();
                    photoUri = restaurateur.getPhotoUri();
                    time = restaurateur.getOpeningTime();

                    ((TextView)view.findViewById(R.id.name)).setText(name);

                    if(photoUri != null) {
                        Glide.with(Objects.requireNonNull(view.getContext()))
                                .load(photoUri)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .into((ImageView) view.findViewById(R.id.profile_image));
                    }
                    else {
                        Glide.with(Objects.requireNonNull(view.getContext()))
                                .load(R.drawable.restaurant_white)
                                .into((ImageView) view.findViewById(R.id.profile_image));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("PAGER ADAPTER PROFILE", "Failed to read value.", error.toException());
            }
        });

        Query getGlobalRating = FirebaseDatabase.getInstance().getReference().child(RESTAURATEUR_INFO + "/" + ROOT_UID).child("review");
        getGlobalRating.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    float totRating = 0;
                    long nReviews = dataSnapshot.getChildrenCount();

                    for(DataSnapshot d : dataSnapshot.getChildren()){
                        ReviewItem reviewItem = d.getValue(ReviewItem.class);
                        totRating += reviewItem.getStars();
                    }

                    totRating = totRating/nReviews;
                    ((RatingBar)view.findViewById(R.id.ratingbaritem)).setRating(totRating);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("PAGER ADAPTER PROFILE", "Failed to read value.", databaseError.toException());
            }
        });

        tab = view.findViewById(R.id.tablayout);
        viewPager = view.findViewById(R.id.view_pager_id);

        tab.addTab(tab.newTab().setText("Profile"));
        tab.addTab(tab.newTab().setText("Rating"));

        pagerAdapter = new PagerAdapter(getChildFragmentManager(), tab.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tab));
        tab.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.edit_profile:
                Intent editProfile = new Intent(getContext(), EditProfile.class);

                editProfile.putExtra(Name, name);
                editProfile.putExtra(Description, descr);
                editProfile.putExtra(Address, addr);
                editProfile.putExtra(Mail, mail);
                editProfile.putExtra(Phone, phone);
                editProfile.putExtra(Photo, photoUri);
                editProfile.putExtra(Time, time);

                startActivity(editProfile);
                return true;

            case R.id.edit_password:
                Intent editPsw = new Intent(getContext(), EditPassword.class);
                startActivity(editPsw);
                return true;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                ROOT_UID = "";

                Intent mainActivity = new Intent(getContext(), MainActivity.class);
                mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainActivity);
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
