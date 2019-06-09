package com.mad.riders.ProfileManagment;

import static com.mad.mylibrary.SharedClass.*;

import com.mad.mylibrary.User;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mad.riders.R;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class EditProfile extends AppCompatActivity {

    private static final int PERMISSION_GALLERY_REQUEST = 1;
    private boolean dialog_open = false;

    private String name;
    private String surname;
    private String mail;
    private String phone;
    private String currentPhotoPath;

    private String error_msg;
    private Uri url;

    private SharedPreferences user_data, first_check;

    private FirebaseStorage storage;
    FirebaseDatabase database;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();

        setContentView(R.layout.fragment_edit_profile);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Button confirm_reg = findViewById(R.id.confirm_registration);
        confirm_reg.setOnClickListener(e -> {
            if(checkFields()){
                uploadImage(ROOT_UID);
            }
            else{
                Toast.makeText(getApplicationContext(), error_msg, Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.plus).setOnClickListener(p -> editPhoto());
        findViewById(R.id.img_profile).setOnClickListener(e -> editPhoto());
    }

    private void uploadImage(String UID) {
        if(currentPhotoPath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/").child("riders/").child(UID);
            File file = null;
            Uri imageUri = Uri.fromFile(new File(currentPhotoPath));

            ref.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downUri = task.getResult();
                        Log.d("URL", "onComplete: Url: "+ downUri.toString());

                        Map<String, Object> new_user = new HashMap<String, Object>();
                        new_user.put("rider_info",new User("gallottino",name, surname
                                ,mail,phone,null,downUri.toString()));
                        new_user.put("available",true);
                        DatabaseReference myRef = database.getReference(RIDERS_PATH +"/"+UID);
                        myRef.updateChildren(new_user);
                        setResult(1);
                        finish();
                    }
                }
            });
        }else{

            Map<String, Object> new_user = new HashMap<String, Object>();
            new_user.put("rider_info",new User("gallottino",name, surname
                    ,mail,phone,null,null));
            new_user.put("available",true);
            DatabaseReference myRef = database.getReference(RIDERS_PATH +"/"+UID);
            myRef.updateChildren(new_user);
            setResult(1);
            finish();
        }

    }

    private void editPhoto(){
        AlertDialog alertDialog = new AlertDialog.Builder(EditProfile.this, R.style.AlertDialogStyle).create();
        LayoutInflater factory = LayoutInflater.from(EditProfile.this);
        final View view = factory.inflate(R.layout.custom_dialog, null);

        dialog_open = true;

        alertDialog.setOnCancelListener(dialog -> {
            dialog_open = false;
            alertDialog.dismiss();
        });

        view.findViewById(R.id.camera).setOnClickListener( c -> {
            cameraIntent();
            dialog_open = false;
            alertDialog.dismiss();
        });
        view.findViewById(R.id.gallery).setOnClickListener( g -> {
            galleryIntent();
            dialog_open = false;
            alertDialog.dismiss();
        });
        alertDialog.setView(view);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Camera", (dialog, which) -> {
            cameraIntent();
            dialog_open = false;
            dialog.dismiss();
        });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Gallery", (dialog, which) -> {
            galleryIntent();
            dialog_open = false;
            dialog.dismiss();
        });
        alertDialog.show();
    }

    private void cameraIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("FILE: ","error creating file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.mad.riders.fileprovider",
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

    private boolean checkFields(){
        name = ((EditText)findViewById(R.id.name)).getText().toString();
        surname = ((EditText)findViewById(R.id.surname)).getText().toString();
        mail = ((EditText)findViewById(R.id.mail)).getText().toString();
        phone = ((EditText)findViewById(R.id.phone2)).getText().toString();

        if(name.trim().length() == 0){
            error_msg = "Insert name";
            return false;
        }

        if(surname.trim().length() == 0){
            error_msg = "Insert address";
            return false;
        }

        if(mail.trim().length() == 0 || !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            error_msg = "Insert e-mail";
            return false;
        }

        if(phone.trim().length() == 0){
            error_msg = "Insert phone number";
            return false;
        }

        return true;
    }

    private void setPhoto(String photoPath) throws IOException {
        File imgFile = new File(photoPath);

        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        ((ImageView)findViewById(R.id.img_profile)).setImageBitmap(myBitmap);
    }



    private File createImageFile() throws IOException {
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

        if(requestCode == 1 && resultCode == RESULT_OK && null != data){
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            //Log.d("Photo path: ", picturePath);
            currentPhotoPath = picturePath;
        }

        if((requestCode == 1 || requestCode == 2) && resultCode == RESULT_OK){
            File imgFile = new File(currentPhotoPath);
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ((ImageView)findViewById(R.id.img_profile)).setImageBitmap(myBitmap);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(Name, ((EditText)findViewById(R.id.name)).getText().toString());
        savedInstanceState.putString(Address, ((EditText)findViewById(R.id.surname)).getText().toString());
        savedInstanceState.putString(Mail, ((EditText)findViewById(R.id.mail)).getText().toString());
        savedInstanceState.putString(Phone, ((EditText)findViewById(R.id.phone2)).getText().toString());
        savedInstanceState.putString(Photo, currentPhotoPath);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ((EditText)findViewById(R.id.name)).setText(savedInstanceState.getString(Name));
        ((EditText)findViewById(R.id.surname)).setText(savedInstanceState.getString(Address));
        ((EditText)findViewById(R.id.mail)).setText(savedInstanceState.getString(Mail));
        ((EditText)findViewById(R.id.phone2)).setText(savedInstanceState.getString(Phone));
        currentPhotoPath = savedInstanceState.getString(Photo);
        if(currentPhotoPath != null){
            try {
                setPhoto(currentPhotoPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}