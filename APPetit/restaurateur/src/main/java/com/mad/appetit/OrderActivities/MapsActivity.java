package com.mad.appetit.OrderActivities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mad.appetit.R;
import com.mad.mylibrary.OrderItem;
import com.mad.mylibrary.OrderRiderItem;
import com.mad.mylibrary.Restaurateur;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static com.mad.mylibrary.SharedClass.ACCEPTED_ORDER_PATH;
import static com.mad.mylibrary.SharedClass.CUSTOMER_ID;
import static com.mad.mylibrary.SharedClass.CUSTOMER_PATH;
import static com.mad.mylibrary.SharedClass.ORDER_ID;
import static com.mad.mylibrary.SharedClass.RESERVATION_PATH;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.RIDERS_ORDER;
import static com.mad.mylibrary.SharedClass.RIDERS_PATH;
import static com.mad.mylibrary.SharedClass.ROOT_UID;
import static com.mad.mylibrary.SharedClass.STATUS_DELIVERING;
import static com.mad.mylibrary.Utilities.updateInfoDish;

public class MapsActivity extends AppCompatActivity implements MapsFragment.OnFragmentInteractionListener,
        ListRiderFragment.OnFragmentInteractionListener{

    private HashMap<String, String> ridersMap;
    private TreeMap<Double, String> distanceMap;

    private static boolean mapsFragVisible = false;
    private boolean mLocationPermissionGranted;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getLocationPermission();
        if (checkMapServices() && mLocationPermissionGranted) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_maps_container,
                    new MapsFragment()).commit();
            mapsFragVisible = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.near_rider:
                chooseNearestRider();
                return true;
            case R.id.list_riders:
                if (mapsFragVisible) {
                    mapsFragVisible = false;
                    item.setIcon(R.drawable.icon_map);

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_maps_container,
                            new ListRiderFragment()).commit();
                }
                else {
                    mapsFragVisible = true;
                    item.setIcon(R.drawable.showlist_map);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_maps_container,
                            new MapsFragment()).commit();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkMapServices(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }

        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setNegativeButton("No", (dialog, id) -> finish())
                .setPositiveButton("Yes", (dialog, id) -> {
                    Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS:
                getLocationPermission();
                if (checkMapServices() && mLocationPermissionGranted) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_maps_container,
                            new MapsFragment()).commit();
                    mapsFragVisible = true;
                }
                else
                    finish();
                break;
            default:
                finish();
                break;
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    if (checkMapServices()) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_maps_container,
                                new MapsFragment()).commit();
                        mapsFragVisible = true;
                    }
                }
                else // Request is cancelled, the result arrays are empty.
                    finish();
                break;
            default:
                finish();
                break;
        }
    }

    private void chooseNearestRider(){
        AlertDialog choiceDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.reservation_dialog, null);

        view.findViewById(R.id.button_confirm).setOnClickListener(e -> {
                    choiceDialog.dismiss();
                    selectRider(distanceMap.firstEntry().getValue(),
                            getIntent().getStringExtra(ORDER_ID),
                            getIntent().getStringExtra(CUSTOMER_ID));
                });

        view.findViewById(R.id.button_cancel).setOnClickListener(e -> choiceDialog.dismiss());

        choiceDialog.setView(view);
        choiceDialog.setTitle("Do you want to choose automatically the nearest rider?");
        choiceDialog.show();
    }

    public void selectRider(String riderId, String orderId, String customerId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query queryDel = database.getReference().child(RESTAURATEUR_INFO + "/" + ROOT_UID
                + "/" + RESERVATION_PATH).child(orderId);

        queryDel.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    DatabaseReference acceptOrder = database.getReference(RESTAURATEUR_INFO + "/" + ROOT_UID
                            + "/" + ACCEPTED_ORDER_PATH);
                    Map<String, Object> orderMap = new HashMap<>();
                    OrderItem orderItem = dataSnapshot.getValue(OrderItem.class);

                    updateInfoDish(orderItem.getDishes());

                    //removing order from RESERVATION_PATH and storing it into ACCEPTED_ORDER_PATH
                    orderMap.put(Objects.requireNonNull(acceptOrder.push().getKey()), orderItem);
                    dataSnapshot.getRef().removeValue();
                    acceptOrder.updateChildren(orderMap);

                    //choosing the selected rider (riderId)
                    Query queryRider = database.getReference(RIDERS_PATH);
                    queryRider.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                String keyRider = "", name = "";

                                for(DataSnapshot d : dataSnapshot.getChildren()){
                                    if(d.getKey().equals(riderId)){
                                        keyRider = d.getKey();
                                        name = d.child("rider_info").child("name").getValue(String.class);
                                        break;
                                    }
                                }

                                //getting address of restaurant to fill OrderRiderItem class
                                DatabaseReference getAddrRestaurant = database.getReference(RESTAURATEUR_INFO + "/" + ROOT_UID + "/info");
                                String finalKeyRider = keyRider;
                                String finalName = name;
                                getAddrRestaurant.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            Restaurateur restaurateur = dataSnapshot.getValue(Restaurateur.class);

                                            orderMap.clear();
                                            orderMap.put(orderId, new OrderRiderItem(ROOT_UID, customerId, orderItem.getAddrCustomer(), restaurateur.getAddr(), orderItem.getTime(), orderItem.getTotPrice()));
                                            DatabaseReference addOrderToRider = database.getReference(RIDERS_PATH + "/" + finalKeyRider + RIDERS_ORDER);
                                            addOrderToRider.updateChildren(orderMap);

                                            //setting to 'false' the availability of that rider
                                            DatabaseReference setFalse = database.getReference(RIDERS_PATH + "/" + finalKeyRider + "/available");
                                            setFalse.setValue(false);

                                            //setting STATUS_DELIVERING of the order to customer
                                            DatabaseReference refCustomerOrder = FirebaseDatabase.getInstance()
                                                    .getReference().child(CUSTOMER_PATH + "/" + customerId).child("orders").child(orderId);
                                            HashMap<String, Object> order = new HashMap<>();
                                            order.put("status", STATUS_DELIVERING);
                                            refCustomerOrder.updateChildren(order);

                                            Toast.makeText(getApplicationContext(), "Order assigned to rider " + finalName, Toast.LENGTH_LONG).show();

                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.w("RESERVATION", "Failed to read value.", databaseError.toException());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w("RESERVATION", "Failed to read value.", databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("RESERVATION", "Failed to read value.", error.toException());
            }
        });
    }

    public void saveRidersList(HashMap<String, String> ridersMap){
        this.ridersMap = ridersMap;
    }

    public void saveDistanceMap(TreeMap<Double, String> distanceMap){
        this.distanceMap = distanceMap;
    }

    public TreeMap<Double, String> getDistanceMap(){
        return this.distanceMap;
    }

    public HashMap<String, String> getRidersMap(){
        return this.ridersMap;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}