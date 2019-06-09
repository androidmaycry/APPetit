package com.mad.appetit.Startup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.appetit.R;
import com.mad.mylibrary.Restaurateur;

import static com.mad.mylibrary.SharedClass.PERMISSION_GALLERY_REQUEST;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.ROOT_UID;
import static com.mad.mylibrary.SharedClass.SIGNUP;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mad.mylibrary.StarItem;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SignUp extends AppCompatActivity {
    private String mail, psw, name, addr, descr, phone, errMsg = "", currentPhotoPath = null;
    private String openingTime, closingTime;
    private double latitude, longitude;

    private Button openingTimeButton, closingTimeButton, address;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyAAzAER-HprZhx5zvmEYIjVlJfYSHj2-G8");
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        address = findViewById(R.id.button_address);
        address.setOnClickListener(l -> {
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this);
            startActivityForResult(intent, 3);
        });

        findViewById(R.id.plus).setOnClickListener(p -> editPhoto());
        findViewById(R.id.img_profile).setOnClickListener(e -> editPhoto());

        openingTimeButton = findViewById(R.id.opening_time);
        openingTimeButton.setOnClickListener(h -> setOpeningTimeDialog());

        closingTimeButton = findViewById(R.id.opening_time2);
        closingTimeButton.setOnClickListener(h -> setClosingTimeDialog());

        findViewById(R.id.button).setOnClickListener(e -> {
            if(checkFields()){
                progressDialog = new ProgressDialog(SignUp.this);
                progressDialog.setTitle("Creating profile.\nOperation may take few minutes...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                auth.createUserWithEmailAndPassword(mail, psw).addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()){
                        ROOT_UID = auth.getUid();
                        storeDatabase();
                    }
                    else {
                        //Log.w("SIGN IN", "Error: createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUp.this,"Registration failed. Try again", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                });
            }
            else{
                Toast.makeText(SignUp.this, errMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean checkFields(){
        mail = ((EditText)findViewById(R.id.mail)).getText().toString();
        psw = ((EditText)findViewById(R.id.psw)).getText().toString();
        name = ((EditText)findViewById(R.id.name)).getText().toString();
        addr = ((Button)findViewById(R.id.button_address)).getText().toString();
        descr = ((EditText)findViewById(R.id.description)).getText().toString();
        phone = ((EditText)findViewById(R.id.time_text)).getText().toString();

        if(mail.trim().length() == 0 || !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            errMsg = "Invalid Mail";
            return false;
        }

        if(psw.trim().length() < 6){
            errMsg = "Password should be at least 6 characters";
            return false;
        }

        if(name.trim().length() == 0){
            errMsg = "Fill name";
            return false;
        }

        if(addr.trim().length() == 0){
            errMsg = "Fill address";
            return false;
        }

        if(phone.trim().length() != 10){
            errMsg = "Invalid phone number";
            return false;
        }

        if(openingTime.trim().length() == 0){
            errMsg = "Fill opening time";
            return false;
        }

        if(closingTime.trim().length() == 0){
            errMsg = "Fill closing time";
            return false;
        }

        return true;
    }

    private void editPhoto(){
        AlertDialog alertDialog = new AlertDialog.Builder(SignUp.this, R.style.AlertDialogStyle).create();
        LayoutInflater factory = LayoutInflater.from(SignUp.this);
        final View view = factory.inflate(R.layout.custom_dialog, null);

        alertDialog.setOnCancelListener(dialog -> {
            alertDialog.dismiss();
        });

        view.findViewById(R.id.camera).setOnClickListener( c -> {
            cameraIntent();
            alertDialog.dismiss();
        });
        view.findViewById(R.id.gallery).setOnClickListener( g -> {
            galleryIntent();
            alertDialog.dismiss();
        });

        alertDialog.setView(view);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Camera", (dialog, which) -> {
            cameraIntent();
            dialog.dismiss();
        });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Gallery", (dialog, which) -> {
            galleryIntent();
            dialog.dismiss();
        });
        alertDialog.show();
    }

    private void cameraIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.mad.appetit",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 2);
            }
        }
    }

    private void galleryIntent(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    PERMISSION_GALLERY_REQUEST);
        }
        else{
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 1);
        }
    }

    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File( storageDir + File.separator +
                imageFileName + /* prefix */
                ".jpg"
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private String[] setTimeValue(){
        String[] cent = new String[100];
        for(int i=0; i<100; i++){
            if(i<10) {
                cent[i] = "0" +i;
            }
            else{
                cent[i] = ""+i;
            }
        }
        return cent;
    }

    private void setOpeningTimeDialog(){
        AlertDialog openingTimeDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(SignUp.this);
        final View viewOpening = inflater.inflate(R.layout.opening_time_dialog, null);

        NumberPicker hour = viewOpening.findViewById(R.id.hour_picker);
        NumberPicker min = viewOpening.findViewById(R.id.min_picker);

        openingTimeDialog.setView(viewOpening);

        openingTimeDialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK", (dialog, which) -> {
            int hourValue = hour.getValue();
            int minValue = min.getValue();

            String hourString = Integer.toString(hourValue), minString = Integer.toString(minValue);

            if(hourValue < 10)
                hourString = "0" + hourValue;
            if(minValue < 10)
                minString = "0" + minValue;

            openingTime = hourString + ":" + minString;

            openingTimeButton.setText(openingTime);
        });
        openingTimeDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"CANCEL", (dialog, which) -> {
            dialog.dismiss();
        });

        String[] hours = setTimeValue();
        hour.setDisplayedValues(hours);
        hour.setMinValue(0);
        hour.setMaxValue(23);
        hour.setValue(0);

        String[] mins = setTimeValue();
        min.setDisplayedValues(mins);
        min.setMinValue(0);
        min.setMaxValue(59);
        min.setValue(0);

        openingTimeDialog.show();
    }

    private void setClosingTimeDialog(){
        AlertDialog closingTimeDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(SignUp.this);
        final View viewClosing = inflater.inflate(R.layout.closing_time_dialog, null);

        NumberPicker hour = viewClosing.findViewById(R.id.hour_picker);
        NumberPicker min = viewClosing.findViewById(R.id.min_picker);

        closingTimeDialog.setView(viewClosing);

        closingTimeDialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK", (dialog, which) -> {
            int hourValue = hour.getValue();
            int minValue = min.getValue();
            String hourString = Integer.toString(hourValue), minString = Integer.toString(minValue);

            if(hourValue < 10)
                hourString = "0" + hourValue;
            if(minValue < 10)
                minString = "0" + minValue;

            closingTime = hourString + ":" + minString;

            closingTimeButton.setText(closingTime);
        });
        closingTimeDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"CANCEL", (dialog, which) -> {
            dialog.dismiss();
        });

        String[] hours = setTimeValue();
        hour.setDisplayedValues(hours);
        hour.setMinValue(0);
        hour.setMaxValue(23);
        hour.setValue(0);

        String[] mins = setTimeValue();
        min.setDisplayedValues(mins);
        min.setMinValue(0);
        min.setMaxValue(59);
        min.setValue(0);

        closingTimeDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_GALLERY_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission Run Time: ", "Obtained");

                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 1);
                } else {
                    Log.d("Permission Run Time: ", "Denied");

                    Toast.makeText(getApplicationContext(), "Access to media files denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if((requestCode == 1) && resultCode == RESULT_OK && null != data){
            Uri selectedImage = data.getData();

            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            currentPhotoPath = picturePath;
        }

        if((requestCode == 1 || requestCode == 2) && resultCode == RESULT_OK){
            Glide.with(getApplicationContext()).load(currentPhotoPath).into((ImageView)findViewById(R.id.img_profile));
        }

        if(requestCode == 3 && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);

            latitude = place.getLatLng().latitude;
            longitude = place.getLatLng().longitude;

            address.setText(place.getAddress());

            if(currentPhotoPath != null) {
                Glide.with(Objects.requireNonNull(this))
                        .load(currentPhotoPath)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into((ImageView) findViewById(R.id.img_profile));
            }
            else {
                Glide.with(Objects.requireNonNull(this))
                        .load(R.drawable.restaurant_home)
                        .into((ImageView) findViewById(R.id.img_profile));
            }

            Log.i("TAG", "Place: " + place.getAddress());
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            // TODO: Handle the error.
            Status status = Autocomplete.getStatusFromIntent(data);
            Log.i("TAG", status.getStatusMessage());
        } else if (resultCode == RESULT_CANCELED) {
            // The user canceled the operation.
        }
    }

    public void storeDatabase(){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + ROOT_UID);
        Map<String, Object> restMap = new HashMap<>();
        Map<String, Object> posInfoMap = new HashMap<>();

        if(currentPhotoPath != null) {
            Uri photoUri = Uri.fromFile(new File(currentPhotoPath));
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());

            ref.putFile(photoUri).continueWithTask(task -> {
                if (!task.isSuccessful()){
                    progressDialog.dismiss();
                    throw Objects.requireNonNull(task.getException());
                }
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Uri downUri = task.getResult();

                    restMap.put("info", new Restaurateur(mail, name, addr, descr,
                            openingTime + " - " + closingTime, phone, downUri.toString()));
                    myRef.updateChildren(restMap);

                    posInfoMap.put("info_pos", new LatLng(latitude, longitude));
                    myRef.updateChildren(posInfoMap);

                    restMap.clear();
                    restMap.put("stars", new StarItem(0, 0, 0));
                    myRef.updateChildren(restMap);

                    progressDialog.dismiss();

                    Intent i = new Intent();
                    setResult(SIGNUP, i);
                    finish();
                }
                else {
                    //Log.w("LOGIN", "signInWithCredential:failure", task.getException());
                    progressDialog.dismiss();
                    Snackbar.make(findViewById(R.id.email), "Authentication Failed. Try again.", Snackbar.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Snackbar.make(findViewById(R.id.email), "Authentication Failed. Try again.", Snackbar.LENGTH_SHORT).show();
            });
        }
        else{
            restMap.put("info", new Restaurateur(mail, name, addr, descr,
                    openingTime + " - " + closingTime, phone, null));
            myRef.updateChildren(restMap);

            posInfoMap.put("info_pos", new LatLng(latitude, longitude));
            myRef.updateChildren(posInfoMap);

            restMap.clear();
            restMap.put("stars", new StarItem(0, 0, 0));
            myRef.updateChildren(restMap);

            progressDialog.dismiss();

            Intent i = new Intent();
            setResult(SIGNUP, i);
            finish();
        }
    }
}