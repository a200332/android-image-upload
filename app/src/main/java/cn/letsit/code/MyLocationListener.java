package cn.letsit.code;

import android.location.LocationListener;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MyLocationListener implements LocationListener {

    Context mContent;
    public MyLocationListener(Context context) {
        mContent = context;
    }
    @Override
    public void onLocationChanged(Location location) {

        location.getLatitude();
        location.getLongitude();

        String Text = "My current location is :" +
                "Latitude = " + location.getLatitude() +
                " Longitude = " + location.getLongitude();
        //figure out a way to make it display through a text view
     //   mLocationTextView.setText(Text);
        System.out.println(Text);

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(mContent, "Gps Disabled", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(mContent, "Gps Enabled", Toast.LENGTH_SHORT);


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

}