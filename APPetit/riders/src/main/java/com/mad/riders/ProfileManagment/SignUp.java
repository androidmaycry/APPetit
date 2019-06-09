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
import android.graphics.Matrix;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
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
import com.google.firebase.auth.AuthResult;
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


public class SignUp extends AppCompatActivity {
    private static final String MyPREF = "User_Data";
    private static final String CheckPREF = "First Run";
    private static final String Name = "keyName";
    private static final String Address = "keyAddress";
    private static final String Description = "keyDescription";
    private static final String Email = "keyEmail";
    private static final String Phone = "keyPhone";
    private static final String Photo = "keyPhoto";
    private static final String FirstRun = "keyRun";
    private static final String DialogOpen ="keyDialog";

    private static final int PERMISSION_GALLERY_REQUEST = 1;
    private boolean dialog_open = false;

    private String name;
    private String surname;
    private String mail;
    private String phone;
    private String currentPhotoPath;
    private String psw;
    private String psw_confirm;

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

        setContentView(R.layout.activity_edit_profile);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Button confirm_reg = findViewById(R.id.confirm_registration);
        confirm_reg.setOnClickListener(e -> {
            if(checkFields()){
                auth.createUserWithEmailAndPassword(mail,psw).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SIGNIN", "createUserWithEmail:success");
                            ROOT_UID = auth.getUid();
                            uploadImage(ROOT_UID);
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Log.d("ERROR", "createUserWithEmail:failure", task.getException());
                        }
                    }
                });
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
        AlertDialog alertDialog = new AlertDialog.Builder(SignUp.this, R.style.AlertDialogStyle).create();
        LayoutInflater factory = LayoutInflater.from(SignUp.this);
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
        psw = ((EditText)findViewById(R.id.psw)).getText().toString();
        psw_confirm = ((EditText)findViewById(R.id.psw_confirm)).getText().toString();

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

        if(psw.compareTo(psw_confirm) != 0){
            error_msg = "Passwords don't match";
            return false;
        }


        return true;
    }

    private void setPhoto(String photoPath) throws IOException {
        File imgFile = new File(photoPath);

        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        myBitmap = adjustPhoto(myBitmap, photoPath);

        ((ImageView)findViewById(R.id.img_profile)).setImageBitmap(myBitmap);
    }

    private Bitmap adjustPhoto(Bitmap bitmap, String photoPath) throws IOException {
        ExifInterface ei = new ExifInterface(photoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }

        return rotatedBitmap;
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
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
            try {
                myBitmap = adjustPhoto(myBitmap, currentPhotoPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ((ImageView)findViewById(R.id.img_profile)).setImageBitmap(myBitmap);
        }
    }

    @Override
    public void onBackPressed() {
        first_check = getSharedPreferences(CheckPREF, 0);
        SharedPreferences.Editor editor = first_check.edit();
        editor.putBoolean("firsRun", true);
        editor.apply();
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(Name, ((EditText)findViewById(R.id.name)).getText().toString());
        savedInstanceState.putString(Address, ((EditText)findViewById(R.id.surname)).getText().toString());
        savedInstanceState.putString(Email, ((EditText)findViewById(R.id.mail)).getText().toString());
        savedInstanceState.putString(Phone, ((EditText)findViewById(R.id.phone2)).getText().toString());
        savedInstanceState.putString(Photo, currentPhotoPath);
        savedInstanceState.putBoolean(DialogOpen, dialog_open);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ((EditText)findViewById(R.id.name)).setText(savedInstanceState.getString(Name));
        ((EditText)findViewById(R.id.surname)).setText(savedInstanceState.getString(Address));
        ((EditText)findViewById(R.id.mail)).setText(savedInstanceState.getString(Email));
        ((EditText)findViewById(R.id.phone2)).setText(savedInstanceState.getString(Phone));
        currentPhotoPath = savedInstanceState.getString(Photo);
        if(currentPhotoPath != null){
            try {
                setPhoto(currentPhotoPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(savedInstanceState.getBoolean(DialogOpen))
            editPhoto();
    }
}