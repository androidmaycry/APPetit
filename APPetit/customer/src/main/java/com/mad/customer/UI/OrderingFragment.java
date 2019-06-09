package com.mad.customer.UI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.mad.customer.Items.DishItem;
import com.mad.customer.R;
import com.mad.customer.ViewHolders.DailyOfferViewHolder;
import com.mad.mylibrary.Restaurateur;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.mad.mylibrary.SharedClass.RESTAURATEUR_INFO;


public class OrderingFragment extends Fragment {

    //Strings of ordered Items
    ArrayList<String> keys = new ArrayList<String>();
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<String> nums = new ArrayList<String>();
    ArrayList<String> prices = new ArrayList<String>();

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<DishItem, DailyOfferViewHolder> mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    public OrderingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_ordering, container, false);
        recyclerView = view.findViewById(R.id.dish_recyclerview);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        String key = ((TabApp)getActivity()).getKey();
        Restaurateur item = ((TabApp)getActivity()).getItem();

        FirebaseRecyclerOptions<DishItem> options =
                new FirebaseRecyclerOptions.Builder<DishItem>()
                        .setQuery(FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + key + "/dishes"),
                                new SnapshotParser<DishItem>(){
                                    @NonNull
                                    @Override
                                    public DishItem parseSnapshot(@NonNull DataSnapshot snapshot) {
                                        DishItem dishItem;
                                        if(snapshot.child("photo").getValue() != null){
                                            dishItem = new DishItem(snapshot.child("name").getValue().toString(),
                                                    snapshot.child("desc").getValue().toString(),
                                                    Float.parseFloat(snapshot.child("price").getValue().toString()),
                                                    Integer.parseInt(snapshot.child("quantity").getValue().toString()),
                                                    snapshot.child("photo").getValue().toString());
                                        }
                                        else{
                                            dishItem = new DishItem(snapshot.child("name").getValue().toString(),
                                                    snapshot.child("desc").getValue().toString(),
                                                    Float.parseFloat(snapshot.child("price").getValue().toString()),
                                                    Integer.parseInt(snapshot.child("quantity").getValue().toString()),
                                                    null);
                                        }
                                        return dishItem;
                                    }
                                }).build();

        mAdapter = new FirebaseRecyclerAdapter<DishItem, DailyOfferViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull DailyOfferViewHolder holder, int position, @NonNull DishItem model) {
                holder.setData(model, position);
                TextView numView = holder.getView().findViewById(R.id.dish_num);
                String dish_key = getRef(position).getKey();
                if(keys.contains(dish_key)){
                    int pos = keys.indexOf(dish_key);
                    String value_num = nums.get(pos);
                    numView.setText(value_num);
                    getActivity().invalidateOptionsMenu();
                }
                else{
                    numView.setText("0");
                    getActivity().invalidateOptionsMenu();
                }
                holder.getView().findViewById(R.id.add_dish).setOnClickListener(a->{
                    Integer num = Integer.parseInt((numView).getText().toString());
                    num++;
                    if(num>model.getQuantity()){
                        Toast.makeText(holder.getView().getContext(), "Maximum quantity exceeded", Toast.LENGTH_LONG).show();
                    }
                    else if (num>99){
                        Toast.makeText(holder.getView().getContext(), "Contact us to get more than this quantity", Toast.LENGTH_LONG).show();
                    }
                    else{
                        numView.setText(num.toString());
                        AddDish(dish_key, model.getName(),Float.toString(model.getPrice()),"add");
                        getActivity().invalidateOptionsMenu();
                    }
                    if(!keys.isEmpty()){
                        view.findViewById(R.id.next).setBackgroundColor(Color.parseColor("#5aad54"));
                    }
                });
                holder.getView().findViewById(R.id.delete_dish).setOnClickListener(b->{

                    Integer num = Integer.parseInt((numView).getText().toString());
                    num--;
                    if(num<0){
                        Toast.makeText(holder.getView().getContext(), "Please select the right quantity", Toast.LENGTH_LONG).show();
                    }
                    else{
                        numView.setText(num.toString());
                        AddDish(dish_key, model.getName(),Float.toString(model.getPrice()),"remove");
                        getActivity().invalidateOptionsMenu();
                    }
                    if(keys.isEmpty()){
                        view.findViewById(R.id.next).setBackgroundColor(Color.parseColor("#c1c1c1"));
                    }
                });
            }

            @NonNull
            @Override
            public DailyOfferViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
                final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_item,parent,false);
                return new DailyOfferViewHolder(view);
            }
        };
        recyclerView.setAdapter(mAdapter);

        //Ordine scelto, pulsante per andare avanti
        view.findViewById(R.id.next).setOnClickListener(w->{
            if (keys.size()==0){
                Toast.makeText(view.getContext(), "Inserire un piatto.", Toast.LENGTH_LONG);
            }
            else{
                Intent intent = new Intent(getContext(), Confirm.class);
                intent.putExtra("key", key);
                intent.putExtra("raddr", item.getAddr());
                intent.putExtra("rname", item.getName());
                intent.putExtra("photo", item.getPhotoUri());
                intent.putStringArrayListExtra("keys", (ArrayList<String>) keys);
                intent.putStringArrayListExtra("names", (ArrayList<String>) names);
                intent.putStringArrayListExtra("prices", (ArrayList<String>) prices);
                intent.putStringArrayListExtra("nums", (ArrayList<String>) nums);
                startActivityForResult(intent, 0);
            }
        });

        return view;

    }



    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }
    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    public void AddDish(String key, String name, String price, String mode){
        if(keys.contains(key) && mode.equals("add")){
            int i = keys.indexOf(key);
            Integer num = Integer.parseInt(nums.get(i))+1;
            nums.set(i, num.toString());
        }
        else if(keys.contains(key) && mode.equals("remove")){
            int i = keys.indexOf(key);
            Integer num = Integer.parseInt(nums.get(i))-1;
            if(num.equals(0)){
                keys.remove(i);
                nums.remove(i);
                names.remove(i);
                prices.remove(i);
            }
            else {
                nums.set(i, num.toString());
            }
        }
        else{
            keys.add(key);
            nums.add("1");
            names.add(name);
            prices.add(price);
        }
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(keys.size()!=0) {
            MenuItem menuItem = (MenuItem) menu.findItem(R.id.action_custom_button);
            TextView cart = menuItem.getActionView().findViewById(R.id.money);
            String snum = getQuantity(nums);
            String tot = calcoloTotale(prices, nums);
            cart.setText(snum+" | "+tot+"€");
        }
        super.onPrepareOptionsMenu(menu);
    }

    public String getQuantity (ArrayList<String> nums){
        int num =0;
        for (String a : nums){
            num += Integer.parseInt(a);
        }
        String snum = Integer.toString(num);
        return snum;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ordering, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    private String calcoloTotale (ArrayList<String> prices, ArrayList<String> nums){
        float tot=0;
        for(int i=0; i<prices.size(); i++){
            float price = Float.parseFloat(prices.get(i));
            float num = Float.parseFloat(nums.get(i));
            tot=tot+(price*num);
        }
        return Float.toString(tot);
    }
}
