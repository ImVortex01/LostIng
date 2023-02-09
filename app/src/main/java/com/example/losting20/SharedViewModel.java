package com.example.losting20;

import android.annotation.SuppressLint;
import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;

public class SharedViewModel extends AndroidViewModel {

    private MutableLiveData<FirebaseUser> user = new MutableLiveData<>();
    private final Application app;
    private static MutableLiveData<String> currentAddress = new MutableLiveData<>();
    private MutableLiveData<String> checkPermission = new MutableLiveData<>();
    private MutableLiveData<String> buttonText = new MutableLiveData<>();
    private MutableLiveData<LatLng> currentLatLng = new MutableLiveData<LatLng>();
    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null) {
                fetchAddress(locationResult.getLastLocation());
            }
        }
    };
    FusedLocationProviderClient mFusedLocationClient;
    private boolean mTrackingLocation;

    public SharedViewModel(@NonNull Application application) {
        super(application);

        this.app = application;

    }

    public MutableLiveData<LatLng> getCurrentLatLng() {return currentLatLng;}

    private void fetchAddress(Location location) {
        //...
        try {
            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
            currentLatLng.postValue(latlng);
            //...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setFusedLocationClient(FusedLocationProviderClient mFusedLocationClient) {
        this.mFusedLocationClient = mFusedLocationClient;
    }

    public MutableLiveData<String> getButtonText() {
        return buttonText;
    }

    LiveData<String> getCheckPermission() {
        return checkPermission;
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public void switchTrackingLocation() {
        if (!mTrackingLocation) {
            startTrackingLocation(true);
        } else {
            stopTrackingLocation();
        }

    }

    @SuppressLint("MissingPermission")
    void startTrackingLocation(boolean needsChecking) {
        if (needsChecking) {
            checkPermission.postValue("check");
        } else {
            mFusedLocationClient.requestLocationUpdates(
                    getLocationRequest(),
                    mLocationCallback, null
            );

            currentAddress.postValue("Carrgando...");

            mTrackingLocation = true;
            buttonText.setValue("Detener seguimiento");
        }
    }

    private void stopTrackingLocation() {
        if (mTrackingLocation) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mTrackingLocation = false;
            buttonText.setValue("Empieza a seguir la ubicacion");
        }
    }


    public LiveData<FirebaseUser> getUser() {
        return user;
    }

    public void setUser(FirebaseUser passedUser) {
        user.postValue(passedUser);
    }
}
