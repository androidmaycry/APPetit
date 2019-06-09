package com.mad.mylibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.mad.mylibrary.SharedClass.DISHES_PATH;
import static com.mad.mylibrary.SharedClass.RESERVATION_PATH;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;
import static com.mad.mylibrary.SharedClass.ROOT_UID;

public class Utilities {

    public static File reizeImageFileWithGlide(String path) throws ExecutionException, InterruptedException, IOException {
        File imgFile = new File(path);
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        Bitmap resized = Bitmap.createScaledBitmap(myBitmap,
                (int) (myBitmap.getWidth() * 0.8),
                (int)(myBitmap.getHeight()*0.8),
                true);

        File file = new File("prova.png");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileOutputStream fos = new FileOutputStream(file);
        resized.compress(Bitmap.CompressFormat.PNG, 10,bos);
        fos.write(bos.toByteArray());
        fos.flush();
        fos.close();

        return file;
    }

    public static void updateInfoDish(final HashMap<String, Integer> dishes){
        Query getDishes = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + ROOT_UID
                + "/" + RESERVATION_PATH);

        getDishes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot d : dataSnapshot.getChildren()){
                        final DishItem dishItem = d.getValue(DishItem.class);

                        if(dishes.containsKey(dishItem.getName())){
                            String keyDish = d.getKey();

                            Query updateDish = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + ROOT_UID
                                    + "/" + RESERVATION_PATH).child(keyDish);
                            updateDish.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        DishItem newDishItem = dataSnapshot.getValue(DishItem.class);

                                        newDishItem.setQuantity(newDishItem.getQuantity() - dishes.get(dishItem.getName()));
                                        newDishItem.setFrequency(newDishItem.getFrequency() + dishes.get(dishItem.getName()));

                                        Map<String, Object> dishMap = new HashMap<>();
                                        DatabaseReference dishRef = FirebaseDatabase.getInstance().getReference(
                                                RESTAURATEUR_INFO + "/" + ROOT_UID + "/" + DISHES_PATH);
                                        dishMap.put(dataSnapshot.getKey(), newDishItem);
                                        dishRef.updateChildren(dishMap);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static String getDateFromTimestamp(Long timestamp){
        Date d = new Date(timestamp);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        int hourValue = c.get(Calendar.HOUR);
        int minValue =c.get(Calendar.MINUTE);
        String hourString = Integer.toString(hourValue), minString = Integer.toString(minValue);

        if(hourValue < 10)
            hourString = "0" + hourValue;
        if(minValue < 10)
            minString = "0" + minValue;

        return hourString + ":" + minString;
    }
}
