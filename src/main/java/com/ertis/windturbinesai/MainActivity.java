package com.ertis.windturbinesai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private int cont;
    private String currentPhotoPath;
    private Button startButton;

    private Mount mount;
    private int[] bbox;
    private int[] size;
    private ArrayList<String> images;
    private ArrayList<String> defects;

    private Python pyInterface;
    private PyObject pyObject;
    private PyObject pyObject2;

    private CheckBox vertical;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1000);
        }

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(getApplicationContext()));
        }

        startButton = findViewById(R.id.startButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        vertical = findViewById(R.id.verticalCheckbox);

        mount = new Mount();
        bbox = new int[4];
        size = new int[2];
        images = new ArrayList<>();
        defects = new ArrayList<>();
        cont = 1;

        pyInterface = Python.getInstance();
        pyObject = pyInterface.getModule("inference");
        pyObject2 = pyInterface.getModule("inference2");
    }

    // Creates an unique image file name
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Backup_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Takes a picture and creates the file
    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,"com.ertis.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void showDialog(String title, String msg, boolean finish) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setIcon(R.drawable.ic_launcher_foreground);
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        if(finish) {
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), "Confirmed", Toast.LENGTH_SHORT).show();
                    try {
                        endActivity();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), "Future action confirmed", Toast.LENGTH_SHORT).show();
                    takePicture();
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), "Workflow canceled", Toast.LENGTH_SHORT).show();
                    try {
                        mount.toBase();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    cont = 1;
                    bbox = new int[4];
                    images = new ArrayList<>();
                    defects = new ArrayList<>();
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            });
        }
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            PyObject prediction = pyObject.callAttr("predict", currentPhotoPath);
            String result = prediction.toString(); // ['label', [width, height], [startX, startY, endX, endY]]
            if (result.contains("blade")) {
                PyObject prediction2 = pyObject2.callAttr("predict", currentPhotoPath);

                if(!prediction2.toString().equals("good")) {
                    images.add(currentPhotoPath);
                    defects.add(prediction2.toString());
                }

                List<PyObject> pyList = prediction.asList();

                if (size[0] == 0 || size[1] == 0) {
                    String fullSizeString = pyList.get(1).toString();
                    String sizeString = fullSizeString.substring(1, fullSizeString.length()-1);
                    String[] sizeParts = sizeString.split(",");
                    for (int i = 0; i < sizeParts.length; i++) {
                        size[i] = Integer.parseInt(sizeParts[i].trim());
                    }
                    mount.setCenterX(size);
                }

                if(bbox[0] == 0 && bbox[1] == 0 && bbox[2] == 0 && bbox[3] == 0) {
                    String fullBboxString = pyList.get(2).toString();
                    String bboxString = fullBboxString.substring(1, fullBboxString.length()-1);
                    String[] bboxParts = bboxString.split(",");
                    for (int j = 0; j < bboxParts.length; j++) {
                        bbox[j] = Integer.parseInt(bboxParts[j].trim());
                    }
                }

                try {
                    if (!vertical.isChecked()) {
                        cont = mount.moveMount(bbox);
                    } else {
                        cont++;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(cont <= 7) {
                    String title = "Manual movement may be required";
                    String msg = "Before taking the next photo, move the mount vertically to continue analyzing the blade.";
                    showDialog(title, msg, false);
                } else {
                    String title = "Finished process";
                    String msg = "End of allowed movement of the mount.";
                    showDialog(title, msg, true);
                }
            } else {
                try {
                    endActivity();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void endActivity() throws InterruptedException {
        mount.toBase();
        cont = 1;
        bbox = new int[4];
        ArrayList<String> auxImages = images;
        ArrayList<String> auxDefects = defects;
        images = new ArrayList<>();
        defects = new ArrayList<>();
        Intent i = new Intent(this, DamagesActivity.class);
        i.putExtra("images", auxImages);
        i.putExtra("defects", auxDefects);
        startActivity(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mount.disconnect();
    }
}