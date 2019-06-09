package com.mad.appetit.ProfileActivities;

import static com.mad.mylibrary.SharedClass.*;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.appetit.R;
import com.mad.mylibrary.Restaurateur;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EditProfile extends AppCompatActivity {
    private String name, addr, desc, mail, phone, currentPhotoPath, time;
    private String error_msg = " ";

    private String openingTime, closingTime;
    private Button address;
    private Button openingTimeButton;
    private Button closingTimeButton;
    private boolean photoChanged = false;

    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        getData();

        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyAAzAER-HprZhx5zvmEYIjVlJfYSHj2-G8");
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

        address = findViewById(R.id.button_address2);
        address.setOnClickListener(l-> {
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this);
            startActivityForResult(intent, 2);
        });

        openingTimeButton = findViewById(R.id.edit_opening_time);
        openingTimeButton.setOnClickListener(h -> setOpeningTimeDialog());

        closingTimeButton = findViewById(R.id.edit_closing_time);
        closingTimeButton.setOnClickListener(h -> setClosingTimeDialog());

        findViewById(R.id.button).setOnClickListener(e -> {
            if(checkFields()){
                storeDatabase();
            }
            else{
                Toast.makeText(getApplicationContext(), error_msg, Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.plus).setOnClickListener(p -> editPhoto());
        findViewById(R.id.img_profile).setOnClickListener(e -> editPhoto());
    }

    private boolean checkFields(){
        name = ((EditText)findViewById(R.id.name)).getText().toString();
        addr = ((Button)findViewById(R.id.button_address2)).getText().toString();
        desc = ((EditText)findViewById(R.id.description)).getText().toString();
        mail = ((EditText)findViewById(R.id.mail)).getText().toString();
        phone = ((EditText)findViewById(R.id.time_text)).getText().toString();
        time = openingTime + " - " + closingTime;

        if(name.trim().length() == 0){
            error_msg = "Fill name";
            return false;
        }

        if(addr.trim().length() == 0){
            error_msg = "Fill address";
            return false;
        }

        if(mail.trim().length() == 0 || !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            error_msg = "Invalid mail";
            return false;
        }

        if(phone.trim().length() != 10){
            error_msg = "Invalid phone number";
            return false;
        }

        if(openingTime.trim().length() == 0){
            error_msg = "Fill opening time";
            return false;
        }

        if(closingTime.trim().length() == 0){
            error_msg = "Fill closing time";
            return false;
        }

        return true;
    }

    private void getData(){
        Intent i = getIntent();

        name = i.getStringExtra(Name);
        addr = i.getStringExtra(Address);
        desc = i.getStringExtra(Description);
        mail = i.getStringExtra(Mail);
        phone = i.getStringExtra(Phone);
        currentPhotoPath = i.getStringExtra(Photo);
        time = i.getStringExtra(Time);
        openingTime = time.split("-")[0].trim();
        closingTime = time.split("-")[1].trim();

        ((EditText)findViewById(R.id.name)).setText(name);
        ((Button)findViewById(R.id.button_address2)).setText(addr);
        ((EditText)findViewById(R.id.description)).setText(desc);
        ((EditText)findViewById(R.id.mail)).setText(mail);
        ((EditText)findViewById(R.id.time_text)).setText(phone);
        ((Button)findViewById(R.id.edit_opening_time)).setText(openingTime);
        ((Button)findViewById(R.id.edit_closing_time)).setText(closingTime);

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
    }

    private void editPhoto(){
        AlertDialog alertDialog = new AlertDialog.Builder(EditProfile.this, R.style.AlertDialogStyle).create();
        LayoutInflater factory = LayoutInflater.from(EditProfile.this);
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

                photoChanged = true;

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
        LayoutInflater inflater = LayoutInflater.from(EditProfile.this);
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
        LayoutInflater inflater = LayoutInflater.from(EditProfile.this);
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
            photoChanged = true;

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

        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
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
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("TAG", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void storeDatabase(){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + ROOT_UID);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        Map<String, Object> profileMap = new HashMap<>();
        Map<String, Object> posInfoMap = new HashMap<>();

        if(photoChanged && currentPhotoPath != null) {
            Uri photoUri = Uri.fromFile(new File(currentPhotoPath));
            StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());

            ref.putFile(photoUri).continueWithTask(task -> {
                if (!task.isSuccessful()){
                    throw Objects.requireNonNull(task.getException());
                }
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Uri downUri = task.getResult();

                    profileMap.put("info", new Restaurateur(mail, name, addr, desc, time, phone, downUri.toString()));
                    myRef.updateChildren(profileMap);

                    posInfoMap.put("info_pos", new LatLng(latitude, longitude));
                    myRef.updateChildren(posInfoMap);

                    finish();
                }
            });
        }
        else{
            if(currentPhotoPath != null)
                profileMap.put("info", new Restaurateur(mail, name, addr, desc, time, phone, currentPhotoPath));
            else
                profileMap.put("info", new Restaurateur(mail, name, addr, desc, time, phone,  null));

            myRef.updateChildren(profileMap);

            posInfoMap.put("info_pos", new LatLng(latitude, longitude));
            myRef.updateChildren(posInfoMap);

            finish();
        }
    }
}