package com.example.alex.quick;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PlacesActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String TAG = "Place_Activity";

    private String currentLocation;
    private String type;
    private Location location;
    private ArrayList<Place> place_list = null;
    private HashMap<Integer,Place> place_map = null;
    //Google Map
    private MarkerOptions currentMarker;
    private MarkerOptions placeMarker;
    private GoogleMap googleMap;
    private boolean mapReady=true;

    private SlidingUpPanelLayout mLayout;
    private ImageView placeImage;
    private TextView placeTitle;

    //Searchng
    private SearchView searchView;
    private SearchManager searchManager;

    //panel
    private TextView placeType;
    private TextView placeAddress;
    private TextView placeOpen;
    private TextView placeDistance;

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_places);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        queue = Volley.newRequestQueue(this);

        currentLocation=getIntent().getStringExtra("currentLocation");
        type= getIntent().getStringExtra("type");
        if(type!=null){
            getSupportActionBar().setTitle(type);
        }
        getPlacesJSON();

        placeType =(TextView)findViewById(R.id.placeType);
        placeAddress=(TextView)findViewById(R.id.placeAddress);
        placeOpen =(TextView)findViewById(R.id.placeOpen) ;
        placeDistance=(TextView)findViewById(R.id.placeDistance);


        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
               // Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
            }
        });

        mLayout.setEnabled(false);
        mLayout.setAnchorPoint(0.6f);
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        placeImage = (ImageView)findViewById(R.id.placeImage);
        placeTitle = (TextView)findViewById(R.id.placeTitle);
        timezone();


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_places, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.normal:
                System.out.println("item = [main_menu normal]");
                if(mapReady) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    Snackbar.make(findViewById(R.id.sliding_layout), "Normal map", Snackbar.LENGTH_LONG)
                            .show();
                }
                return true;
            case R.id.satellite:
                System.out.println("item = [main_menu satellite]");
                if(mapReady) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    Snackbar.make(findViewById(R.id.sliding_layout), "Satellite map", Snackbar.LENGTH_LONG)
                            .show();
                }
                return true;
            case R.id.hybrid:
                System.out.println("item = [main_menu hybrid]");
                if(mapReady) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    Snackbar.make(findViewById(R.id.sliding_layout), "Hybrid map", Snackbar.LENGTH_LONG)
                            .show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getPlacesJSON(){
        place_list = new ArrayList<Place>();
        String URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+currentLocation+"&rankby=distance&types="+type+"&key=AIzaSyBECdnBe-LP4YUxygPWgDmEeDqOEx_Rv08";

        //RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        praseJSON(response); // prase json object
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void getSearchPlaceJSON(String search){
        place_list = new ArrayList<Place>();
        String URL=null;
        if(search!=null){
            URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+currentLocation+"&radius=500&keyword="+search+"&key=AIzaSyBECdnBe-LP4YUxygPWgDmEeDqOEx_Rv08";
        }else{
           return;
        }
        //RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        praseJSON(response); // prase json object
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void praseJSON(JSONObject response) {
        try {
            JSONArray array=response.getJSONArray("results");
            for(int i = 0 ; i < array.length() ; i++){
                double lat = array.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                double lng = array.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                String id = array.getJSONObject(i).getString("id");
                String name = array.getJSONObject(i).getString("name");
                String place_id = array.getJSONObject(i).getString("place_id");
                String price_level = array.getJSONObject(i).has("price_level")?array.getJSONObject(i).getString("price_level"):"unknown";
                String rating = array.getJSONObject(i).has("rating")?array.getJSONObject(i).getString("rating"):"3";
                String photo_reference = array.getJSONObject(i).has("photos")?array.getJSONObject(i).getJSONArray("photos").getJSONObject(0).getString("photo_reference"):null;
                String type = array.getJSONObject(i).getJSONArray("types").get(0).toString();
                System.out.println("type11111111111 = [" + price_level + "]");
                //JSONObject aa=array.getJSONObject(i);
                String vicinity = array.getJSONObject(i).getString("vicinity");
                Boolean isOpen = array.getJSONObject(i).has("opening_hours")?array.getJSONObject(i).getJSONObject("opening_hours").getBoolean("open_now"):true;

                place_list.add(new Place(lat,lng,id,name,place_id,null,rating,photo_reference,vicinity,type,isOpen));
                System.out.println("response = [" + place_list.size() + "]");

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        initialMap();
    }

    private void initialMap() {

        if(googleMap!=null){
            googleMap.clear();
            System.out.println("map cleared");
        }
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(PlacesActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        place_map = new HashMap<>();
        System.out.println("googleMap = ready");
        mapReady=true;
        this.googleMap=googleMap;
        String[] latLng = currentLocation.split(",");
        LatLng currentLocation=new LatLng(Double.parseDouble(latLng[0]),Double.parseDouble(latLng[1]));

        for(Place place : place_list){
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(place.getLatitude(),place.getLongitude())).title(place.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker1));
            Marker marker = googleMap.addMarker(markerOptions);
            place_map.put(Integer.parseInt(marker.getId().substring(1)),place);
        }

        CameraPosition target= CameraPosition.builder().target(currentLocation).zoom(16).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));

        //placeMarker = new MarkerOptions().position(placeLocation).title(place.getName());
        currentMarker = new MarkerOptions().position(currentLocation).title("Me").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2));
        Marker markerMe =googleMap.addMarker(currentMarker);
        place_map.put(Integer.parseInt(markerMe.getId().substring(1)),null);
        googleMap.setOnMarkerClickListener(this);

        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
//        Snackbar.make(findViewById(R.id.sliding_layout), "Get "+(place_map.size()-1)+" results!", Snackbar.LENGTH_LONG)
//                .show();
        Toast.makeText(this,"Get "+(place_map.size()-1)+" results!",Toast.LENGTH_SHORT);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        int index = Integer.parseInt(marker.getId().toString().substring(1));
        System.out.println("marker = [" + marker.getId() + "]");
        if(place_map.get(index)==null) {
            mLayout.setEnabled(false);
            placeImage.setImageResource(R.drawable.blank);
            return false;
        }
        Place place =  place_map.get(index);
        placeType.setText(place.getType());
        placeAddress.setText(place.getVicinity().isEmpty()?"unknown":place.getVicinity());
        placeOpen.setText(place.getIsOpen()?"Opening":"Closed");
        setStars(place.getRating());
        System.out.println("marker = [ " +place.getName()+ " ]");
        placeWalkingDistance(index);
        placeImage(index);
        geolocation(index);
        return false;
    }

    public void placeWalkingDistance(final int index){
        //RequestQueue queue = Volley.newRequestQueue(this);
        Place place=place_map.get(index);
        String url ="https://maps.googleapis.com/maps/api/directions/json?origin="+currentLocation+"&destination=place_id:"+place.getPlace_id()+"&mode=walking&key=AIzaSyBECdnBe-LP4YUxygPWgDmEeDqOEx_Rv08";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d("TAG", response.toString());
                        try {
                            JSONArray array=response.getJSONArray("routes");
                            String length = array.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text");
                            String time = array.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text");
                            System.out.println("walking response = [" + length + time + "]");
                            if((Integer.parseInt(time.charAt(0)+""))>=2){
                                placeDrivingDistance(index);
                            }else{
                                placeDistance.setText(" "+length+"  walking: "+time);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //initialMap();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });
        queue.add(jsonObjectRequest);
    }

    public void placeDrivingDistance(int index){
        //RequestQueue queue = Volley.newRequestQueue(this);
        Place place=place_map.get(index);
        String url ="https://maps.googleapis.com/maps/api/directions/json?origin="+currentLocation+"&destination=place_id:"+place.getPlace_id()+"&mode=driving&key=AIzaSyBECdnBe-LP4YUxygPWgDmEeDqOEx_Rv08";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d("TAG", response.toString());
                        try {
                            JSONArray array=response.getJSONArray("routes");
                            String length = array.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text");
                            String time = array.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text");
                            System.out.println("driving response = [" + length + time + "]");
                            placeDistance.setText(" "+length+"  driving: "+time);
                            ImageView walkingImg =(ImageView)findViewById(R.id.walkingImg);
                            walkingImg.setImageResource(R.drawable.ic_time_to_leave_black_36dp);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //initialMap();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });
        queue.add(jsonObjectRequest);
    }

    public void placeImage(int index){
        Place place = place_map.get(index);
        String photo_reference = place.getPhoto_reference();
        String url ="https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="+photo_reference+"&sensor=false&key=AIzaSyBECdnBe-LP4YUxygPWgDmEeDqOEx_Rv08";
        //RequestQueue queue = Volley.newRequestQueue(this);
        final ImageRequest imageRequest=new ImageRequest (url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {

                popularDate(response,true);

            }
        },0,0, ImageView.ScaleType.CENTER_INSIDE,null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Bitmap image=null;
                popularDate(null,false);

            }
        });
        queue.add(imageRequest);
        placeTitle.setText(place.getName());
    }

    private void popularDate(Bitmap response, Boolean image) {
        mLayout.setEnabled(true);
        if(image){
            placeImage.setImageBitmap(response);
        }else{
            placeImage.setImageResource(R.drawable.map);
        }
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
    }

    @Override
    public boolean onClose() {
        System.out.println("close searching");
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        System.out.println("query = [" + query + "]");
        query.trim().replaceAll("[^a-zA-Z ]", "").toLowerCase();
        googleMap.clear();
        getSearchPlaceJSON(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //System.out.println("newText = [" + newText + "]");

        return false;
    }

    public void geolocation(int index){
        Place place = place_map.get(index);

        String url ="https://maps.googleapis.com/maps/api/elevation/json?locations="+place.getLatitude()+","+place.getLongitude()+"&key=AIzaSyBECdnBe-LP4YUxygPWgDmEeDqOEx_Rv08";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d("TAG", response.toString());
                        try {
                            JSONArray jsonArray=response.getJSONArray("results");

                            String elevation = jsonArray.getJSONObject(0).getString("elevation");


                            System.out.println("elevation  = [" + elevation + "]");
                            Snackbar.make(findViewById(R.id.sliding_layout),elevation , Snackbar.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });
        queue.add(jsonObjectRequest);
    }

    public void timezone(){
        //RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://maps.googleapis.com/maps/api/timezone/json?location="+currentLocation+"&timestamp=1458000000&key=AIzaSyBECdnBe-LP4YUxygPWgDmEeDqOEx_Rv08";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d("TAG", response.toString());
                        try {
                            String timeZoneId = response.getString("timeZoneId");
                            String timeZoneName = response.getString("timeZoneName");
                            System.out.println("timeZoneId  = [" + timeZoneId + timeZoneName + "]");
                            Snackbar.make(findViewById(R.id.sliding_layout),timeZoneName , Snackbar.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });
        queue.add(jsonObjectRequest);
    }

    public void setStars(String rating){
        ImageView ratingImg =(ImageView)findViewById(R.id.rating);
        double star = Double.parseDouble(rating);
        if(star>4.4){
            ratingImg.setImageResource(R.drawable.star5);
        }else if(star>3.4){
            ratingImg.setImageResource(R.drawable.star4);
        }else if(star>2.4){
            ratingImg.setImageResource(R.drawable.star3);
        }else if(star>1.4){
            ratingImg.setImageResource(R.drawable.star2);
        }else{
            ratingImg.setImageResource(R.drawable.star1);
        }
    }
}
