package com.ertis.windturbinesai;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class DamagesActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> images;
    private ArrayList<String> defects;
    private ArrayList<Damage> damages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_damages);

        damages = new ArrayList<>();
        listView = findViewById(R.id.listView);

        images = getIntent().getExtras().getStringArrayList("images");
        defects = getIntent().getExtras().getStringArrayList("defects");

        for (int i = 0; i < defects.size(); i++) {
            if(defects.get(i).equals("crack")) {
                defects.set(i,"Crack");
            } else if(defects.get(i).equals("erosion")) {
                defects.set(i,"Erosion");
            } else if(defects.get(i).equals("mechanicaldamage")) {
                defects.set(i,"Mechanical damage");
            } else if(defects.get(i).equals("paintoff")) {
                defects.set(i,"Paint off");
            } else if(defects.get(i).equals("scratch")) {
                defects.set(i,"Scratch");
            }
        }

        for (int i = 0; i < images.size(); i++) {
            damages.add(new Damage(BitmapFactory.decodeFile(images.get(i)),defects.get(i)));
        }

        DamageAdapter damageAdapter = new DamageAdapter(this,R.layout.list_row,damages);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Damage d = (Damage) parent.getItemAtPosition(position);
                Bitmap bmp = d.getImage();

                try {
                    // Write file
                    String filename = "bitmap.png";
                    FileOutputStream stream = openFileOutput(filename, Context.MODE_PRIVATE);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();

                    // Pop intent
                    Intent i = new Intent(DamagesActivity.this,InfoActivity.class);
                    i.putExtra("name", d.getName());
                    i.putExtra("image", filename);
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        listView.setAdapter(damageAdapter);
    }
}