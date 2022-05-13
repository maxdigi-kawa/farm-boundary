package com.kawasdk.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kawasdk.Model.DeviveBounderyModel;
import com.kawasdk.Model.LocationModel.LocationModel;
import com.kawasdk.R;
import com.kawasdk.Utils.Common;
import com.kawasdk.Utils.GoogleServiceManager;
import com.kawasdk.Utils.InterfaceKawaEvents;
import com.kawasdk.Utils.KawaMap;
import com.kawasdk.Utils.LocationAdapter;
import com.kawasdk.Utils.PlaceSearchAdapter;
import com.kawasdk.Utils.ServiceManager;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class fragmentFarmLocation extends Fragment implements OnMapReadyCallback, PlaceSearchAdapter.PlaceSearchItemClickListener, LocationAdapter.LocationItemClickListener, MapboxMap.OnMapClickListener {

    Intent intent;
    private Common common;
    private MapboxMap mapboxMap;
    private MapView mapview;
    private Button dropdownBtn;
    private AppCompatButton GET_FARMSBtn;
    private ImageButton zoomOutBtn, zoomInBtn, dropPinFab;
    private EditText searchTxt;
    private TextView messageBox;
    private ActivityResultLauncher<Intent> SEARCHRESULT;
    private int firstTimecnt = 0;
    private ActivityResultLauncher<String> MPERMISSIONRESULT;
    private ImageView buttonSearchClear;
    private RecyclerView place_recyclerView, locationlist_recyclerView;
    private LocationAdapter locationAdapter;
    private PlaceSearchAdapter placeSearchAdapter;
    private ArrayList<String> PLACEARRAY;
    private ArrayList<String> PLACEIDARRAY;
    private ArrayList<LatLng> PLACELATLNGARRAY;
    private ArrayList<String> LATLNGARRAY;
    private LatLng PLACELATLNG;
    private LatLng PLACELNGLAT;
    private LinearLayout powerdByIV;
    private ArrayList<String> cityname = new ArrayList<String>();
    private boolean searchEnable = true;
    private List<List<LatLng>> LNGLAT = new ArrayList<>();
    private List<Point> llPts = new ArrayList<>();
    private List<LocationModel> LocationList;
    // 434573882bf2d7079548eeb5344cd61e82131e76 kawa smartlook
    // 81ab38327bb3cbabb3f67fca628c0849d034aec0 maxdigi smartlook
    private int clicckCount = 0;
    private LinearLayout linearLayoutProgressbar;
    private boolean PROGRESSBAR = true;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        InterfaceKawaEvents interfaceKawaEvents = (InterfaceKawaEvents) context;
        interfaceKawaEvents.initKawaMap(KawaMap.isValidKawaAPiKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        common = new Common(getResources().getString(R.string.mapbox_api_key));
        Mapbox.getInstance(getContext(), common.MAPBOX_ACCESS_TOKEN);
        common.showLoader(getContext(), "isScanner");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        common.setLocale((Activity) getContext()); // Change Langurage
        return inflater.inflate(R.layout.select_farm_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        super.onCreate(savedInstanceState);
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
        //revisit     X_API_KEY = getResources().getString(R.string.kawa_api_key);

        mapview = view.findViewById(R.id.mapView);
        //mapiew.onCreate(savedInstanceState); // revisit
        mapview.getMapAsync(this);
//
        zoomInBtn = view.findViewById(R.id.zoomInBtn);
        zoomOutBtn = view.findViewById(R.id.zoomOutBtn);
        GET_FARMSBtn = view.findViewById(R.id.GET_FARMSBtn);
        dropPinFab = view.findViewById(R.id.goCurrentLocBtn);
        searchTxt = view.findViewById(R.id.searchTxt);
        buttonSearchClear = view.findViewById(R.id.button_search_clear);
        messageBox = view.findViewById(R.id.messageBox);
        dropdownBtn = view.findViewById(R.id.dropdownBtn);

//        messageBox.setBackgroundColor(KawaMap.headerBgColor);
//        messageBox.setTextColor(KawaMap.headerTextColor);
//        GET_FARMSBtn.setTextColor(KawaMap.footerBtnTextColor);
//        GET_FARMSBtn.setBackgroundColor(KawaMap.footerBtnBgColor);

        PLACEARRAY = new ArrayList<>();
        PLACEIDARRAY = new ArrayList<>();
        LATLNGARRAY = new ArrayList<>();
        PLACELATLNGARRAY = new ArrayList<>();
        place_recyclerView = view.findViewById(R.id.place_recyclerView);
        powerdByIV = view.findViewById(R.id.powerdByIV);
        locationlist_recyclerView = view.findViewById(R.id.locationlist_recyclerView);
        linearLayoutProgressbar = view.findViewById(R.id.linearLayoutProgressbar);

        place_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        placeSearchAdapter = new PlaceSearchAdapter(getActivity(), PLACEARRAY);
        placeSearchAdapter.setClickListener(this);
        place_recyclerView.setAdapter(placeSearchAdapter);
        powerdByIV.setVisibility(View.GONE);
        if (KawaMap.isFlyToLocationEnable) {
            getAllLocation();
            locationlist_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            locationAdapter = new LocationAdapter(getActivity(), cityname);
            locationAdapter.setClickListener(this);
            locationlist_recyclerView.setAdapter(locationAdapter);
        }
//
        dropdownBtn.setOnClickListener(viewV -> flytoLocation());


        searchTxt.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                buttonSearchClear.setVisibility(View.VISIBLE);
                if (searchEnable) {
                    try {
                        makeGeocodeSearch(searchTxt.getText().toString().trim());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (searchEnable) {
                    try {
                        makeGeocodeSearch(searchTxt.getText().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }


            }
        });

        GET_FARMSBtn.setOnClickListener(viewV -> getAllFarms());
        zoomInBtn.setOnClickListener(viewV -> common.setZoomLevel(getActivity(),1, mapboxMap));
        zoomOutBtn.setOnClickListener(viewV -> common.setZoomLevel(getActivity(),-1, mapboxMap));
        searchTxt.setOnClickListener(viewV -> searchView());
        buttonSearchClear.setOnClickListener(viewV -> clearsearchView());
        MPERMISSIONRESULT = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        // Log.e("PERMISSIONSTATUS", String.valueOf(result));
                        if (result) {
                            // Log.e("TAG", "onActivityResult: PERMISSION GRANTED");

                            Style loadedMapStyle = mapboxMap.getStyle();
                            //mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().zoom(Common.MAPZOOM).build()), 100);
                            LocationComponent locationComponent = mapboxMap.getLocationComponent();
                            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(getActivity(), loadedMapStyle).build());
                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // Log.e("TAG", "onActivityResult: PERMISSION IF");
                                Toast.makeText(getActivity(), R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
                                return;
                            }
                            locationComponent.setLocationComponentEnabled(true);
                            // locationComponent.setCameraMode(CameraMode.TRACKING);
                            // Log.e("commonZOOM", String.valueOf(common.MAPZOOM));
                            locationComponent.setCameraMode(
                                    CameraMode.TRACKING_GPS,
                                    750L,
                                    16.0,
                                    null,
                                    null,
                                    null);
                            //locationComponent.getLastKnownLocation().getLongitude()
                        } else {
                            // Log.e("TAG", "onActivityResult: PERMISSION DENIED");
                            Toast.makeText(getActivity(), R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    //
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        this.mapboxMap.getUiSettings().setCompassEnabled(false);
        this.mapboxMap.getUiSettings().setLogoEnabled(false);
        this.mapboxMap.getUiSettings().setFlingVelocityAnimationEnabled(false);
        this.mapboxMap.setStyle(Style.SATELLITE_STREETS, style -> {
            common.hideLoader();
            common.initMarker(getContext(), style, this.mapboxMap, mapview);
            this.mapboxMap.addOnMapClickListener(this);
             dropPinFab.setOnClickListener(viewV -> getCurrentLocation());
            MPERMISSIONRESULT.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        });
    }


    private void getCurrentLocation() {
        MPERMISSIONRESULT.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        common.segmentEvents(getActivity(), "Fetching the current location", "GPS location saved", mapboxMap, "", "CURRENT_LOC");
    }

    public void getAllFarms() {
        common.showLoader(getActivity(),"isScanner");
        ServiceManager.getInstance().getKawaService().GET_FARMSs(KawaMap.KAWA_API_KEY, Common.SDK_VERSION, getCornerLatLng())
                .enqueue(new Callback<DeviveBounderyModel>() {
                    @Override
                    public void onResponse(@NonNull Call<DeviveBounderyModel> call, @NonNull Response<DeviveBounderyModel> response) {
                        common.hideLoader();
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                CameraPosition cameraPosition = mapboxMap.getCameraPosition();
                                common.MAPZOOM = cameraPosition.zoom;

                                common.segmentEvents(getActivity(), "Get farm boundaries", "Response saved on successfully getting farm boundaries", mapboxMap, response.body().getId(), "GET_FARMS");
                                Bundle farms_bundle = new Bundle();
                                farms_bundle.putString("id", response.body().getId());
                                farms_bundle.putDouble("lat", common.CAMERALAT);
                                farms_bundle.putDouble("lng", common.CAMERALNG);
                                farms_bundle.putDouble("zoom", common.MAPZOOM);

                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.kawaMapView, fragmentShowAllFarms.class,farms_bundle);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();


                            } else {
                                if (response.errorBody() != null) {
                                    JSONObject jsonObj = new JSONObject(response.errorBody().string());
                                    // Log.e("RESP", jsonObj.getString("error"));
                                    String errorMsg = jsonObj.getString("error");
                                    // Log.e("getAllFarms", errorMsg);
                                    common.segmentEvents(getActivity(), "Get farm boundaries", "Response saved on failing to get farm boundaries", mapboxMap, errorMsg, "GET_FARMS");

                                    if (errorMsg.equals("Could not validate request: the area chosen is too large, please choose coordinates enclosing a smaller area"))
                                        Toast.makeText(getContext(), getResources().getString(R.string.error_large_area), Toast.LENGTH_LONG).show();
                                    else if (errorMsg.equals("Could not validate request: The coordinates given are out of bounds for the recipe farm_boundaries, please check developers.kawa.space for the coordinate bounds for the recipe"))
                                        Toast.makeText(getContext(), getResources().getString(R.string.error_farmnot_available), Toast.LENGTH_LONG).show();
                                    else
                                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<DeviveBounderyModel> call, Throwable t) {
                        common.hideLoader();
                        String errorBody = t.getMessage();
                        // Log.e("TAG", "onFailure: " + errorBody);
                        Toast.makeText(getContext(), errorBody, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private JsonObject getCornerLatLng() {
        VisibleRegion vRegion = mapboxMap.getProjection().getVisibleRegion();
        String jString = "{ \"recipe_id\": \"farm_boundaries\", \"aoi\": { \"type\": \"Feature\", \"geometry\": { \"type\": \"Polygon\", \"coordinates\": " +
                "[[[" + vRegion.farLeft.getLongitude() + ", " + vRegion.farLeft.getLatitude() + "], " +
                "[" + vRegion.nearLeft.getLongitude() + ", " + vRegion.nearLeft.getLatitude() + "], " +
                "[" + vRegion.nearRight.getLongitude() + ", " + vRegion.nearRight.getLatitude() + "], " +
                "[" + vRegion.farRight.getLongitude() + ", " + vRegion.farRight.getLatitude() + "], " +
                "[" + vRegion.farLeft.getLongitude() + "," + vRegion.farLeft.getLatitude() + "]]] } } }";

        JsonObject jsonObject = JsonParser.parseString(jString).getAsJsonObject();
        // Log.e("jsonObject:", String.valueOf(jsonObject));
        return jsonObject;
    }

    private void flytoLocation() {
        if (PROGRESSBAR) {
            linearLayoutProgressbar.setVisibility(View.VISIBLE);
            PROGRESSBAR = false;
            powerdByIV.setVisibility(View.GONE);
            place_recyclerView.setVisibility(View.GONE);
            dropdownBtn.setVisibility(View.GONE);
            new Handler(Looper.getMainLooper()).postDelayed(
                    new Runnable() {
                        public void run() {
                            if (cityname.size() >= 1) {
                                linearLayoutProgressbar.setVisibility(View.GONE);
                                locationlist_recyclerView.setVisibility(View.VISIBLE);
                            }
                        }
                    },
                    1000);
        } else {
            place_recyclerView.setVisibility(View.GONE);
            dropdownBtn.setVisibility(View.GONE);
            locationlist_recyclerView.setVisibility(View.VISIBLE);

        }

        ViewGroup.LayoutParams params = locationlist_recyclerView.getLayoutParams();
        // Log.e("cityname", "flytoLocation: " + cityname.size());
        if (cityname.size() >= 6)
            params.height = 600;
        else if (cityname.size() >= 4)
            params.height = 400;
        else
            params.height = 200;
        locationlist_recyclerView.setLayoutParams(params);

    }

    private void searchView() {

        searchEnable = true;
        // Log.e("TAG", "searchView:<> ");
        place_recyclerView.setVisibility(View.VISIBLE);
        if (PLACEIDARRAY.size() > 0)
            powerdByIV.setVisibility(View.VISIBLE);
        locationlist_recyclerView.setVisibility(View.GONE);
        dropdownBtn.setVisibility(View.GONE);
    }

    private void clearsearchView() {
        searchTxt.setText("");
        searchEnable = true;
        buttonSearchClear.setVisibility(View.GONE);
        place_recyclerView.setVisibility(View.GONE);
        powerdByIV.setVisibility(View.GONE);
        locationlist_recyclerView.setVisibility(View.GONE);
        if (KawaMap.isFlyToLocationEnable)
            dropdownBtn.setVisibility(View.VISIBLE);
        PLACEARRAY.clear(); // clear list
        PLACEIDARRAY.clear();
        PLACELATLNGARRAY.clear(); // clear list
        placeSearchAdapter.setItems(PLACEARRAY);
    }

   /* private void displaySerachRegion(Intent data) {
        GET_FARMSBtn.setVisibility(View.VISIBLE);
//        searchTxt.setText(String.valueOf(mapboxMap.getProjection().getVisibleRegion().latLngBounds));
        searchTxt.setTextColor(Color.BLACK);
        if (mapboxMap != null) {
            Style style = mapboxMap.getStyle();
            if (style != null) {
                CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
                double lat = ((Point) selectedCarmenFeature.geometry()).latitude();
                double lng = ((Point) selectedCarmenFeature.geometry()).longitude();
                LatLng latLng = new LatLng(lat, lng);

                common.CAMERALAT = lat;
                common.CAMERALNG = lng;
                // Log.e("TAG", lat + " : displaySerachRegion: " + lng);

                GeoJsonSource markerSorceID = style.getSourceAs("markerSorceID");
                markerSorceID.setGeoJson(Point.fromLngLat(lat, lng));

                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(latLng).zoom(common.MAPZOOM).build()), 1000);
            }
        }
    }
*/
    @Override
    public void onplacesearchItemClick(View view, int position) { // search seggestion selection
        //Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_LONG).show();
        // Log.e("TAG", "PLACELATLNGARRAY: " + PLACELATLNGARRAY);
        if (PLACEIDARRAY.size() > 0)
            getLatLngUsingGoogleAPI(PLACEIDARRAY.get(position));
        else if (PLACELATLNGARRAY.size() > 0) {
            animateCameraToNewPosition(PLACELATLNGARRAY.get(position));
            new Handler(Looper.getMainLooper()).postDelayed(
                    new Runnable() {
                        public void run() {
                            common.segmentEvents(getActivity(), "Fetching a desired location", PLACEARRAY.get(position), mapboxMap, searchTxt.getText().toString(), "SEARCH");
                        }
                    },
                    1000);
            searchEnable = false;
            searchTxt.setText(PLACEARRAY.get(position));

        } else {
            Toast.makeText(getContext(), "Invalid Latitude or Longitude", Toast.LENGTH_LONG).show();
            //searchTxt.setText("");
        }
        place_recyclerView.setVisibility(View.GONE);
        powerdByIV.setVisibility(View.GONE);
        if (KawaMap.isFlyToLocationEnable)
            dropdownBtn.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(
                new Runnable() {
                    public void run() {
                        PLACEARRAY.clear(); // clear list
                        PLACEIDARRAY.clear(); // clear list
                        PLACELATLNGARRAY.clear(); // clear list
                        placeSearchAdapter.setItems(PLACEARRAY);
                        common.hideKeyboard(getActivity(), view);
                    }
                },
                1000);
//        searchTxt.setText(String.valueOf(mapboxMap.getProjection().getVisibleRegion().latLngBounds));
    }

    @Override
    public void onLocationItemClick1(View view, int position) { // flytoLocation value seggestion
        clicckCount++;
        Style style = mapboxMap.getStyle();
        List<List<Double>> latlng = LocationList.get(position).getRecipeBoundsPolygon().getCoordinates().get(0);
        List<Point> llPts = new ArrayList<>();
        for (int i = 0; i < latlng.size(); i++) {
            llPts.add(Point.fromLngLat(latlng.get(i).get(0), latlng.get(i).get(1)));
        }
        style.addSource(new GeoJsonSource("lineSourceID" + LocationList.get(position).getId() + clicckCount, FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(LineString.fromLngLats(llPts))})));
        style.addLayer(new LineLayer("lineLayerID" + LocationList.get(position).getId() + clicckCount, "lineSourceID" + LocationList.get(position).getId() + clicckCount).withProperties(
                PropertyFactory.lineWidth(4f),
                PropertyFactory.lineColor(Color.parseColor("#000000")),
                PropertyFactory.lineOpacity(0.8f)
        ));
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(llPts.get(0).latitude(), llPts.get(0).longitude()))
                .include(new LatLng(llPts.get(2).latitude(), llPts.get(2).longitude()))
                .build();

        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 20), 1000);
        locationlist_recyclerView.setVisibility(View.GONE);
        if (KawaMap.isFlyToLocationEnable)
            dropdownBtn.setVisibility(View.VISIBLE);
        // Log.e("onLocationItemClick1", String.valueOf(llPts));
        searchEnable = false;
        searchTxt.setText("Location #" + (position + 1));
//        searchTxt.setText(String.valueOf(mapboxMap.getProjection().getVisibleRegion().latLngBounds));
    }

    @Override
    public boolean onMapClick(@NonNull LatLng coordsOfPoint) {
        if (KawaMap.isFlyToLocationEnable)
            dropdownBtn.setVisibility(View.VISIBLE);
        place_recyclerView.setVisibility(View.GONE);
        powerdByIV.setVisibility(View.GONE);
        locationlist_recyclerView.setVisibility(View.GONE);
        return false;
    }

    private boolean latCoordinateIsValid(double value) {
        return (value >= -90 && value <= 90);

    }

    private boolean longCoordinateIsValid(double value) {
        return (value >= -180 && value <= 180);


    }

    public boolean isAlpha(String s) {
        return s != null && s.matches(".*[a-zA-Z]+.*");
    }

    private void makeGeocodeSearch(String searchStr) throws ParseException {
        // Log.e("TAG", "makeGeocodeSearch: ");
        if (searchStr.length() >= 3) {

            place_recyclerView.setVisibility(View.VISIBLE);
            powerdByIV.setVisibility(View.VISIBLE);
            dropdownBtn.setVisibility(View.GONE);
            Pattern pattern = Pattern.compile(".*[a-zA-Z]+.*");
            Matcher matcher = pattern.matcher(searchStr);
            Pattern numPattern = Pattern.compile(".*[0-9]+.*");
            Matcher noMatcher = numPattern.matcher(searchStr);
            Log.e("TAG", "makeGeocodeSearch:>><< " + searchStr.matches("^[0-9]*$"));
            if (matcher.matches()) {
                // searchUsingAddress(searchStr);
                Log.e("TAG", "makeGeocodeSearch:If ");
                getLocationGoogleAPI(searchStr);
            } else if (searchStr.matches("^[0-9]*$")) {
                getLocationGoogleAPI(searchStr);
                Log.e("TAG", "makeGeocodeSearch:else-If ");
            } else {
                Log.e("TAG", "makeGeocodeSearch:else ");
                searchUsingLatLng(searchStr);
            }

        } else {
            place_recyclerView.setVisibility(View.GONE);
            powerdByIV.setVisibility(View.GONE);
            if (KawaMap.isFlyToLocationEnable)
                dropdownBtn.setVisibility(View.VISIBLE);
        }
    }

    public void searchUsingAddress(String searchStr) {
        PLACEARRAY.clear(); // clear list
        PLACELATLNGARRAY.clear(); // clear list
        placeSearchAdapter.setItems(PLACEARRAY);
        MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(common.MAPBOX_ACCESS_TOKEN)
                .query(searchStr)
                .build();

        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                List<CarmenFeature> results = response.body().features();
                if (results.size() > 0) {
                    PLACEARRAY.clear(); // clear list
                    PLACELATLNGARRAY.clear(); // clear list
                    for (int i = 0; i < results.size(); i++) {
                        Log.d("TAG", "onResponse: " + results.get(0).placeName());
                        PLACEARRAY.add(results.get(i).placeName());
                        double lat = results.get(i).center().coordinates().get(1);
                        double lng = results.get(i).center().coordinates().get(0);
                        PLACELATLNG = (new LatLng(Double.valueOf(lat),
                                Double.valueOf(lng)));
                        PLACELATLNGARRAY.add(PLACELATLNG);
                        placeSearchAdapter.setItems(PLACEARRAY);
                    }
                } else {
                    Log.d("TAG", "onResponse: No result found");
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public void searchUsingLatLng(String searchStr) {
//        // Log.e("TAG", "searchUsingLatLng: "+searchStr );
        List<String> placeList = Arrays.asList(searchStr.split("\\,"));

        // if (placeList.get(0).matches("-?\\d+")) {
        String placelistFirstObj = placeList.get(0).trim();
        if (placelistFirstObj.matches("\\d+(?:\\.\\d+)?") || placelistFirstObj.matches("-?\\d+")) {
//
            if (placeList.size() == 2) {

                PLACEARRAY.clear(); // clear list
                PLACELATLNGARRAY.clear(); // clear list
                placeSearchAdapter.setItems(PLACEARRAY);
                PLACEARRAY.add("Lat : " + placeList.get(0) + " Long : " + placeList.get(1));
                PLACEARRAY.add("Long : " + placeList.get(0) + " Lat : " + placeList.get(1));

                if (placeList.get(1).equals(" ")) {
                    // Log.e("TAG", "Space : " + PLACEARRAY);
                } else {
                    // Log.e("TAG", "PLACEARRAY >> : " + PLACEARRAY);

                    //String placelistSecondObj = placeList.get(1).replace(" ","");
                    String placelistSecondObj = placeList.get(1).trim();
                    if (placelistSecondObj.matches("\\d+(?:\\.\\d+)?") || placelistSecondObj.matches("-?\\d+")) {
                        // Log.e("TAG", "placeList: " + placeList);
//
                        try {
                            double lat = Double.parseDouble(placeList.get(0));
                            double lng = Double.parseDouble(placeList.get(1));
                            LatLng latLng = null;
                            if (latCoordinateIsValid(Double.valueOf(lat))
                                    && longCoordinateIsValid(Double.valueOf(lng))) {
                                PLACELATLNG = (new LatLng(Double.valueOf(lat),
                                        Double.valueOf(lng)));

                                if (Double.valueOf(placeList.get(1)) <= 90) {
                                    PLACELNGLAT = (new LatLng(Double.valueOf(lng),
                                            Double.valueOf(lat)));
//24.98 98.89
                                }
                                PLACELATLNGARRAY.add(PLACELATLNG);
                                PLACELATLNGARRAY.add(PLACELNGLAT);
                                placeSearchAdapter.setItems(PLACEARRAY);
                                latLng = (new LatLng(Double.valueOf(placeList.get(0)), Double.valueOf(placeList.get(1))));
                                // Log.e("TAG", "latLng: " + latLng);
                                // Log.e("TAG", "PLACELATLNGARRAY>>: " + PLACELATLNGARRAY);

                                MapboxGeocoding client = MapboxGeocoding.builder()
                                        .accessToken(common.MAPBOX_ACCESS_TOKEN)
                                        .query(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()))
                                        .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
                                        .mode(GeocodingCriteria.MODE_PLACES)
                                        .build();
                                LatLng finalLatLng = latLng;
                                client.enqueueCall(new Callback<GeocodingResponse>() {
                                    @Override
                                    public void onResponse(Call<GeocodingResponse> call,
                                                           Response<GeocodingResponse> response) {
                                        if (response.body() != null) {
                                            List<CarmenFeature> results = response.body().features();
                                            if (results.size() > 0) {
                                                CarmenFeature feature = results.get(0);
                                                //  animateCameraToNewPosition(finalLatLng);
                                            } else {
                                                // Toast.makeText(getContext(), "Place Not Found", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                                        // Log.e("Geocoding Failure: ", throwable.getMessage());
                                    }
                                });
                            } else {
                                // Toast.makeText(getContext(), "Invalid Mark", Toast.LENGTH_LONG).show();
                            }
                        } catch (ServicesException servicesException) {
                            // Log.e("Error geocoding: ", servicesException.toString());
                            servicesException.printStackTrace();
                        }
                    } else {
                        //Toast.makeText(getContext(), "Number Only", Toast.LENGTH_LONG).show();
                    }
                }
            }

            // Log.e("TAG", "makeGeocodeearch: " + PLACEARRAY);

        }
    }

    public void getLocationGoogleAPI(String searchStr) {
        // common.showLoader("isCircle","0");
        String latlng = common.CAMERALAT + "," + common.CAMERALNG;
        GoogleServiceManager.getInstance().getKawaService().getGoogleLocations(searchStr, latlng, "50000", "AIzaSyD4USXe5X90MW3JnWrffNBl822ym99Q8hs").enqueue(new Callback<JsonObject>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)

            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                //  common.hideLoader();
                try {
                    JsonArray googlePlacesArray = response.body().getAsJsonArray("predictions");
                    PLACEARRAY.clear(); // clear list
                    PLACEIDARRAY.clear(); // clear list
                    PLACEARRAY = new ArrayList<>();
                    PLACEIDARRAY = new ArrayList<>();

                    placeSearchAdapter.setItems(PLACEARRAY);
                    for (int i = 0; googlePlacesArray.size() > i; i++) {
                        PLACEARRAY.add(String.valueOf(googlePlacesArray.get(i).getAsJsonObject().get("description").getAsString()));
                        PLACEIDARRAY.add(String.valueOf(googlePlacesArray.get(i).getAsJsonObject().get("place_id").getAsString()));
                    }
                    placeSearchAdapter.setItems(PLACEARRAY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                //           common.hideLoader();
                String errorBody = t.getMessage();
                // Log.e("TAG", "onFailure: " + errorBody);
                Toast.makeText(getContext(), errorBody, Toast.LENGTH_LONG).show();
                //Toast.makeText(getContext(), getResources().getString(R.string.Error_General), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getLatLngUsingGoogleAPI(String searchStr) {
        common.showLoader(getActivity(),"isCircle");

        GoogleServiceManager.getInstance().getKawaService().getLatLngLocations("geometry", searchStr, "AIzaSyD4USXe5X90MW3JnWrffNBl822ym99Q8hs").enqueue(new Callback<JsonObject>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)

            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                common.hideLoader();
                LatLng newLatLng;
                try {
                    JsonObject googlePlacesArray = response.body().getAsJsonObject("result");
                    Log.e("TAG", "onResponse:>>>> " + googlePlacesArray);
                    if (PLACEIDARRAY.size() > 0) {
                        newLatLng = new LatLng(googlePlacesArray.get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lat").getAsDouble(), googlePlacesArray.get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lng").getAsDouble());
                        animateCameraToNewPosition(newLatLng);
                    } else {
                        Toast.makeText(getContext(), "Invalid Latitude or Longitude", Toast.LENGTH_LONG).show();
                        //searchTxt.setText("");
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                common.hideLoader();
                String errorBody = t.getMessage();
                // Log.e("TAG", "onFailure: " + errorBody);
                Toast.makeText(getContext(), errorBody, Toast.LENGTH_LONG).show();
                Toast.makeText(getContext(), getResources().getString(R.string.Error_General), Toast.LENGTH_LONG).show();
            }
        });
    }


    public void getAllLocation() {
        common.showLoader(getActivity(),"isCircle");
        ServiceManager.getInstance().getKawaService().getLocations()
                .enqueue(new Callback<List<LocationModel>>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)

                    @Override
                    public void onResponse(@NonNull Call<List<LocationModel>> call, @NonNull Response<List<LocationModel>> response) {

                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                LocationList = response.body();
                                for (int i = 0; i < LocationList.size(); i++) {
                                    cityname.add("Location #" + (i + 1));
                                }
                                dropdownBtn.setVisibility(View.VISIBLE);
                                // Log.e("getAllLocation:", String.valueOf(LNGLAT));
                            } else {
                                if (response.errorBody() != null) {
                                    JSONObject jsonObj = new JSONObject(response.errorBody().string());
                                    String errorMsg = jsonObj.getString("error");
                                    // Log.e("getAllLocation:error", errorMsg);
                                    Toast.makeText(getContext(), getResources().getString(R.string.Error_General), Toast.LENGTH_LONG).show();
                                }
                                common.hideLoader();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            common.hideLoader();
                        }

                    }

                    @Override
                    public void onFailure(Call<List<LocationModel>> call, Throwable t) {
                        common.hideLoader();
                        String errorBody = t.getMessage();
                        // Log.e("getAllLocation", "onFailure: " + errorBody);
                        Toast.makeText(getContext(), getResources().getString(R.string.Error_General), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void animateCameraToNewPosition(LatLng latLng) {
        common.MAPZOOM = 17.0;
        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(common.MAPZOOM)
                        .build()), 1000);
    }
//
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }
//

    @Override
    public void onStart() {
        super.onStart();
        mapview.onStart();

    }

    //
    @Override
    public void onResume() {
        super.onResume();
        mapview.onResume();
//        if (firstTimecnt == 0) {
//            // do something
//            // Log.e("onStart_if", String.valueOf(firstTimecnt));
//            firstTimecnt = 2;
//        } else if (firstTimecnt == 2) {
//            // Log.e("onStart_else", String.valueOf(firstTimecnt));
//            //MPERMISSIONRESULT.launch(Manifest.permission.ACCESS_FINE_LOCATION);
//            new Handler(Looper.getMainLooper()).postDelayed(
//                    new Runnable() {
//                        public void run() {
//                            Fragment frag = new fragmentFarmLocation();
//                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                            fragmentManager.beginTransaction().replace(R.id.kawaMapView, frag).commit();
//                        }
//                    },
//                    1000);
//        }
    }

    //
//
//
    @Override
    public void onPause() {
        super.onPause();
        mapview.onPause();
    }

    //
    @Override
    public void onStop() {
        super.onStop();
        mapview.onStop();
    }

    //
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapview.onSaveInstanceState(outState);
    }

    //
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapview.onDestroy();
//        MAPVIEW =null;
//        mapboxMap = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapview.onLowMemory();
    }

}