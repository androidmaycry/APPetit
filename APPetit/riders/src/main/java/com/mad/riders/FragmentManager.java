package com.mad.riders;


import static com.mad.mylibrary.SharedClass.ROOT_UID;
import static com.mad.mylibrary.SharedClass.RIDERS_PATH;

import com.mad.riders.NavigationFragments.Home;
import com.mad.riders.NavigationFragments.Orders;
import com.mad.riders.NavigationFragments.Profile;
import com.mad.riders.Services.LocationService;


import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


public class FragmentManager extends AppCompatActivity implements
        Orders.OnFragmentInteractionListener,
        Home.OnFragmentInteractionListener,
        Profile.OnFragmentInteractionListener{

    public boolean value;
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private boolean mLocationPermissionGranted = false;

    private BottomNavigationView navigation;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item ->{

        switch (item.getItemId()) {
            case R.id.navigation_home:
                if(!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof Home)) {
                    checkBadge();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Home()).commit();
                }
                return true;

            case R.id.navigation_profile:
                if(!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof Profile)) {
                Bundle bundle = new Bundle();
                bundle.putString("UID",ROOT_UID);
                Profile profile = new Profile();
                profile.setArguments(bundle);
                checkBadge();

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profile).commit();
                }
                return true;

            case R.id.navigation_reservation:
                if(!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof Orders)) {
                    Bundle bundle2 = new Bundle();
                    bundle2.putString("UID", ROOT_UID);
                    if (value)
                        bundle2.putString("STATUS", "true");
                    else
                        bundle2.putString("STATUS", "false");
                    Orders orders = new Orders();
                    orders.setArguments(bundle2);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, orders).commit();
                    hideBadgeView();
                }
                return true;
        }
        return false;
    };
    private View notificationBadge;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //TODO: MAP PERMISSION
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                // TODO:
            } else {
                getLocationPermission();
            }
        }

        startLocationService();

        value = true;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference status = database.getReference(RIDERS_PATH+"/"+ROOT_UID+"/available/");

        status.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                value = (boolean)dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        checkBadge();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new Home()).commit();
            addBadgeView();
            hideBadgeView();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStop() {
        super.onStop();
        stopService(serviceIntent);
    }

    private void checkBadge(){
        Query query = FirebaseDatabase.getInstance().getReference(RIDERS_PATH+"/"+ROOT_UID+"/pending/");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    long count = dataSnapshot.getChildrenCount();

                    ((TextView)notificationBadge.findViewById(R.id.count_badge)).setText(Long.toString(count));
                    refreshBadgeView();
                }
                else{
                    hideBadgeView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addBadgeView() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(2);

        notificationBadge = LayoutInflater.from(this).inflate(R.layout.notification_badge, menuView, false);

        itemView.addView(notificationBadge);
    }

    private void refreshBadgeView() {
        notificationBadge.setVisibility(VISIBLE);
    }

    private void hideBadgeView(){
        notificationBadge.setVisibility(INVISIBLE);
    }

    //Functions for permissions check
    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    public boolean isServicesOK(){
        Log.d("TAG", "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(FragmentManager.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d("TAG", "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d("TAG", "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(FragmentManager.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            //getChatrooms();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            serviceIntent = new Intent(this, LocationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                FragmentManager.this.startForegroundService(serviceIntent);
            }
            else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.example.rider_map.services.LocationService".equals(service.service.getClassName())) {
                Log.d("DEBUG", "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d("DEBUG", "isLocationServiceRunning: location service is not running.");
        return false;
    }

}
