package com.mad.appetit.OrderActivities;

import static com.mad.mylibrary.SharedClass.*;

import com.google.android.gms.maps.model.Marker;
import com.mad.appetit.R;
import com.mad.mylibrary.OrderItem;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mad.mylibrary.OrderRiderItem;
import com.mad.mylibrary.Restaurateur;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static com.mad.mylibrary.SharedClass.RESERVATION_PATH;
import static com.mad.mylibrary.Utilities.updateInfoDish;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

class Position {
    public Double latitude, longitude;

    public Position() {

    }

    public Position(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}

class Haversine {

    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

    public static double distance(double startLat, double startLong,
                                  double endLat, double endLong) {

        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversineFormula(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversineFormula(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // <-- d
    }

    public static double haversineFormula(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

}

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = true;
    private static final int DEFAULT_ZOOM = 15;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private PlaceDetectionClient mPlaceDetectionClient;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085); //default location

    private double longitude, latitude;
    private Query queryRiderPos;
    private ValueEventListener riderPosListener;

    private String restaurantName;
    private HashMap<String, Position> posMap;
    private HashMap<String, String> riderName;
    private TreeMap<Double, String> distanceMap;
    private HashMap <String, Marker> markerMap = new HashMap<>();
    private HashSet<String> riderKey = new HashSet<>();

    private OnFragmentInteractionListener mListener;

    public MapsFragment() {
        // Required empty public constructor
    }

    public static MapsFragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this.getContext(), null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        updateLocationUI();

        try{
            if(mLocationPermissionGranted){
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this.getActivity(), task -> {
                    if(task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = (Location) task.getResult();

                        Query getRestaurantInfo = FirebaseDatabase.getInstance().getReference()
                                .child(RESTAURATEUR_INFO + "/" + ROOT_UID);
                        getRestaurantInfo.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    Position position = dataSnapshot.child("info_pos").getValue(Position.class);
                                    restaurantName = dataSnapshot.child("info").child("name").getValue(String.class);

                                    latitude = position.getLatitude();
                                    longitude = position.getLongitude();

                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(latitude, longitude), DEFAULT_ZOOM));

                                    mMap.addCircle(new CircleOptions()
                                            .center(new LatLng(latitude, longitude))
                                            .radius(10000)
                                            .strokeColor(0xFFBC7362)
                                            .fillColor(0x32FFC8C8));

                                    setRidersOnMaps();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.w("MAPS FRAGMENT", "Failed to read value.", databaseError.toException());
                            }
                        });

                    }
                    else {
                        Log.d("TAG", "Current location is null. Using defaults.");
                        Log.e("TAG","Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        }
        catch(SecurityException e){
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void setRidersOnMaps(){
        queryRiderPos = FirebaseDatabase.getInstance().getReference(RIDERS_PATH);
        queryRiderPos.addValueEventListener(riderPosListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    distanceMap = new TreeMap<>();
                    posMap = new HashMap<>();
                    riderName = new HashMap<>();

                    for(DataSnapshot d : dataSnapshot.getChildren()){
                        if((boolean)d.child("available").getValue()){
                            riderName.put(d.getKey(), d.child("rider_info").child("name").getValue(String.class));
                            posMap.put(d.getKey(), d.child("rider_pos").getValue(Position.class));
                            distanceMap.put(Haversine.distance(latitude, longitude, posMap.get(d.getKey()).latitude, posMap.get(d.getKey()).longitude), d.getKey());
                        }
                    }

                    if(distanceMap.isEmpty()){
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                        builder.setMessage("No riders available. Retry later!")
                                .setCancelable(false)
                                .setNeutralButton("Ok", (dialog, id) -> getActivity().finish());

                        final AlertDialog alert = builder.create();
                        alert.show();
                    }
                    else{
                        boolean first = true;
                        for (Map.Entry<Double, String> entry : distanceMap.entrySet()) {
                            if(!riderKey.contains(entry.getValue())) {
                                riderKey.add(entry.getValue());
                                if (first) {
                                    first = false;

                                    Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(posMap.get(entry.getValue()).latitude, posMap.get(entry.getValue()).longitude))
                                            .title(riderName.get(entry.getValue()))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.nearest_icon))
                                            .snippet(new DecimalFormat("#.##").format(entry.getKey()) + " km"));
                                    m.setTag(entry.getValue());
                                    markerMap.put(entry.getValue(), m);
                                } else {
                                    Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(posMap.get(entry.getValue()).latitude, posMap.get(entry.getValue()).longitude))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_rider))
                                            .title(riderName.get(entry.getValue()))
                                            .snippet(new DecimalFormat("#.##").format(entry.getKey()) + " km"));
                                    m.setTag(entry.getValue());
                                    markerMap.put(entry.getValue(), m);
                                }
                            }
                            else{
                                if(first){
                                    first = false;

                                    markerMap.get(entry.getValue()).setPosition(new LatLng(posMap.get(entry.getValue()).latitude, posMap.get(entry.getValue()).longitude));
                                    markerMap.get(entry.getValue()).setSnippet(new DecimalFormat("#.##").format(entry.getKey()) + " km");
                                    markerMap.get(entry.getValue()).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.nearest_icon));
                                }
                                else{
                                    markerMap.get(entry.getValue()).setPosition(new LatLng(posMap.get(entry.getValue()).latitude, posMap.get(entry.getValue()).longitude));
                                    markerMap.get(entry.getValue()).setSnippet(new DecimalFormat("#.##").format(entry.getKey()) + " km");
                                    markerMap.get(entry.getValue()).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_rider));
                                }
                            }
                        }

                        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                .title(restaurantName));

                        mMap.setOnInfoWindowClickListener(marker ->
                                selectRider(marker.getTag().toString(),
                                        getActivity().getIntent().getStringExtra(ORDER_ID),
                                        getActivity().getIntent().getStringExtra(CUSTOMER_ID)));

                        ((MapsActivity) getActivity()).saveDistanceMap(distanceMap);
                        ((MapsActivity) getActivity()).saveRidersList(riderName);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("MAPS FRAGMENT", "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void selectRider(String riderId, String orderId, String customerId) {
        AlertDialog reservationDialog = new AlertDialog.Builder(this.getContext()).create();
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        final View view = inflater.inflate(R.layout.reservation_dialog, null);

        view.findViewById(R.id.button_confirm).setOnClickListener(e -> {
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

                        // choosing the selected rider (riderId)
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
                                    DatabaseReference getAddrRestaurant = database.getReference(RESTAURATEUR_INFO + "/" + ROOT_UID
                                            + "/info");
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

                                                //setting to 'false' boolean variable of rider
                                                DatabaseReference setFalse = database.getReference(RIDERS_PATH + "/" + finalKeyRider + "/available");
                                                setFalse.setValue(false);

                                                //setting status delivering of the order to customer
                                                DatabaseReference refCustomerOrder = FirebaseDatabase.getInstance()
                                                        .getReference().child(CUSTOMER_PATH + "/" + customerId).child("orders").child(orderId);
                                                HashMap<String, Object> order = new HashMap<>();
                                                order.put("status", STATUS_DELIVERING);
                                                refCustomerOrder.updateChildren(order);

                                                reservationDialog.dismiss();
                                                Toast.makeText(getContext(), "Order assigned to rider " + finalName, Toast.LENGTH_LONG).show();

                                                getActivity().finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.w("MAPS FRAGMENT", "Failed to read value.", databaseError.toException());
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.w("MAPS FRAGMENT", "Failed to read value.", databaseError.toException());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("MAPS FRAGMENT", "Failed to read value.", error.toException());
                }
            });
        });

        view.findViewById(R.id.button_cancel).setOnClickListener(e -> reservationDialog.dismiss());

        reservationDialog.setView(view);
        reservationDialog.setTitle("Are you sure to select this rider?\n");

        reservationDialog.show();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    @Override
    public void onPause() {
        queryRiderPos.removeEventListener(riderPosListener);
        super.onPause();
    }

    @Override
    public void onStop() {
        queryRiderPos.removeEventListener(riderPosListener);
        super.onStop();
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
