package com.example.mmbuw.hellomaps;

import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMapLongClickListener,GoogleMap.OnCameraChangeListener {

    public static final String PREF_S = "Lollipop";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private EditText markerText;
    private String text;
    private LatLng position;
    private TreeSet<String> markerSet=new TreeSet<String>();
    private  ArrayList<Circle> circlesOp = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        markerText = (EditText)findViewById(R.id.markerText);
        setUpMapIfNeeded();
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraChangeListener(this);

        SharedPreferences markers = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> tempSet =markers.getStringSet("MyMapMarkers",markerSet);

        if(!tempSet.isEmpty()){
            markerSet.addAll(tempSet);
            Iterator setIter = markerSet.iterator();
            while (setIter.hasNext()) {
                String[] s = setIter.next().toString().split(";");
                 mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(s[0]),Double.parseDouble(s[1])))
                    .title(s[2])
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
         }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
    }

    @Override
    public void onMapLongClick(LatLng point) {
        position=point;
        text = markerText.getText().toString();
        mMap.addMarker(new MarkerOptions().position(point).title(text)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

        Double pointLat = point.latitude;
        Double pointLong = point.longitude;

        String textLocation = pointLat.toString()+";"+pointLong.toString()+";"+text;
        markerSet.add(textLocation);
    }

    @Override
    public void onCameraChange(CameraPosition position) {

        if (!circlesOp.isEmpty()) {
            for (int i = 0; i < circlesOp.size(); ++i) {
                circlesOp.get(i).remove();
            }
        }
        Location loc = new Location("CameraPo");
        loc.setLatitude(position.target.latitude);
        loc.setLatitude(position.target.longitude);
        if (markerSet!=null) {
            Iterator setIter = markerSet.iterator();
            while (setIter.hasNext()) {
                Location markerLoc = getLocation(setIter.next().toString());
                if (!isInside(markerLoc)) {
                    Float dist = loc.distanceTo(markerLoc);
                    CircleOptions circle = new CircleOptions()
                            .center(new LatLng(markerLoc.getLatitude(), markerLoc.getLongitude()))
                            .radius(dist)
                            .strokeColor(0xFF9932CC);

                    Circle halo = mMap.addCircle(circle);
                    circlesOp.add(halo);
                }
            }
        }
    }
    @Override protected void onStop(){
        super.onStop();
        SharedPreferences markers = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edd = markers.edit();
        edd.putStringSet("MyMapMarkers",markerSet).apply();
    }

    private Location getLocation(String s){
       Location l = new Location("temp");
        String[] split = s.split(";");
        l.setLatitude(Double.parseDouble(split[0]));
        l.setLongitude(Double.parseDouble(split[1]));
        return l;
    }

    private boolean isInside(Location loc){
        boolean test = false;
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        LatLng currentLoc = new LatLng(loc.getLatitude(),loc.getLongitude());
        if(bounds.contains(currentLoc))
            test=true;
        return test;
    }
}
