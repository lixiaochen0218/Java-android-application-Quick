package com.example.alex.quick;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 123;
    private static final String RESTAURANT="restaurant";
    private static final String CAFE="cafe";
    private static final String STORE="store";
    private final String LOG_TAG ="TestApp";
    private TextView txtLocation;

    ImageView imageResaurant;
    ImageView imageCafe;
    ImageView imageStore;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location location;
    private String currentLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();



        imageResaurant=(ImageView)findViewById(R.id.imageRestaurant);
        imageResaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    intentToPlacesActivity(RESTAURANT);
            }
        });
        imageCafe=(ImageView)findViewById(R.id.imageCafe);
        imageCafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToPlacesActivity(CAFE);
            }
        });
        imageStore=(ImageView)findViewById(R.id.imageStore);
        imageStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToPlacesActivity(STORE);
            }
        });
    }



    public void createApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        onStart();
    }
    private void intentToPlacesActivity(String type) {
        Intent intent = new Intent(this, PlacesActivity.class);
        if(currentLocation!=null){
            intent.putExtra("currentLocation",currentLocation);
            intent.putExtra("type",type);
            startActivity(intent);
        }else{
            Toast.makeText(this,"Location: Null",Toast.LENGTH_SHORT);
            Snackbar.make(findViewById(R.id.constrainLayout), "Location: Null", Snackbar.LENGTH_LONG)
                    .show();
        }

    }

    public void checkPermissions(){
        System.out.println("checkPermisssion");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("askPermisssion");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);

        }else{
            createApiClient();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createApiClient();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Toast.makeText(this,"please grant permission to ues location",Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        if(mGoogleApiClient != null){
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10); // Update location every second

//        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if(location!=null){
//            System.out.println("last location get"+location.toString());
//        }else {
//            System.out.println("last location NULL");
//        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                System.out.println("getLocation");
                if(location==null){
                    Toast.makeText(MainActivity.this.getApplicationContext(),"Wrong GPS info",Toast.LENGTH_LONG);
                    return;
                }else{
                    MainActivity.this.location = location;
                    currentLocation=location.getLatitude()+","+location.getLongitude();
                    System.out.println("location = [" + location.toString() + "]");
                }

//                currentLocation=location.getLatitude()+","+location.getLongitude();
//                currentLat=location.getLatitude();
//                currentLng=location.getLongitude();
            }
        });


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }

}
