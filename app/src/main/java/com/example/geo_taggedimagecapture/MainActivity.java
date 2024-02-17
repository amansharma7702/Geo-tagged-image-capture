package com.example.geo_taggedimagecapture;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView textViewLocation;
    private ImageView imageView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewLocation = findViewById(R.id.textViewLocation);
        imageView = findViewById(R.id.imageView);

        // Request Location Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission has already been granted, proceed with camera operation
            dispatchTakePictureIntent();
        }

        // Capture Image Button Click
        Button btnCapture = findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(v -> dispatchTakePictureIntent());
    }

    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                textViewLocation.setText("Location: " + location.getLatitude() + ", " + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Log.e(TAG, "dispatchTakePictureIntent: No camera app available");
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imageBitmap);
                saveImage(imageBitmap);
            } else {
                Log.e(TAG, "onActivityResult: No image data received");
                Toast.makeText(this, "No image data received", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImage(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        File storageDir = getExternalFilesDir(null);
        File imageFile = new File(storageDir, imageFileName);

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            Toast.makeText(this, "Image saved: " + imageFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "saveImage: Failed to save image", e);
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            // Check if the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, proceed with the camera operation
                dispatchTakePictureIntent();
            } else {
                // Permission is denied, show a message or take appropriate action
                Toast.makeText(this, "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}