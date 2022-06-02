package com.kawasdk.Fragment;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.collection.LongSparseArray;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kawasdk.Model.Boundary;
import com.kawasdk.Model.MergeModel;
import com.kawasdk.Model.PolygonModel;
import com.kawasdk.Model.ResponseKawa;
import com.kawasdk.R;
import com.kawasdk.Utils.Common;
import com.kawasdk.Utils.InterfaceKawaEvents;
import com.kawasdk.Utils.KawaMap;
import com.kawasdk.Utils.ServiceManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolDragListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeasurement;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class fragmentShowAllFarms extends Fragment implements OnMapReadyCallback, MapboxMap.OnMapClickListener {
    private Common COMACT;
    private MapboxMap MAPBOXMAP;
    private MapView MAPVIEW;
    String STRID = "";
    private List<Integer> POLYSELECTED = new ArrayList<>();
    private List<List<LatLng>> LNGLAT = new ArrayList<>();
    Button startOverBtn, drawBounderyBtn;
    AppCompatButton combinePlotBtn, saveBounderyBtn, resetBtn, clearBtn;
    TextView messageBox;
    LinearLayout clearNExitLayout;
    InterfaceKawaEvents interfaceKawaEvents;
    Float AREA;
    private ArrayList AREAARRAY = new ArrayList();
    ArrayList POLYGONAREA = new ArrayList<>();
    int DRAWENABLE = 0;
    private static List<SymbolManager> SYMBOLSET = new ArrayList<>();
    private List<List<LatLng>> DRAWLNGLAT = new ArrayList<>();
    private List<List<LatLng>> TEMPDRAWLNGLAT = new ArrayList<>();
    private boolean EDITON = false;
    private static Symbol SYMBOLACTIVE;
    List<LatLng> LLARRAY = new ArrayList<>();
    List<Point> LATLNGPTS = new ArrayList<>();
    LatLng FIRSTPOINTOFPOLY = new LatLng();
    Integer LASTINDEXOFSELECTEDPOLYGON;
    int PIDX = 0;
    SymbolManager symbolManager;
    int ISINTERSECT = 0;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        interfaceKawaEvents = (InterfaceKawaEvents) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        COMACT = new Common(getActivity());
        Mapbox.getInstance(getActivity(), COMACT.MAPBOX_ACCESS_TOKEN);
        COMACT.showLoader(getActivity(),"isScanner");
        KawaMap.isDrawEnable = false;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.show_all_farms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        super.onCreate(savedInstanceState);

        // X_API_KEY = getResources().getString(R.string.kawa_api_key);

        MAPVIEW = view.findViewById(R.id.mapView);
        MAPVIEW.onCreate(savedInstanceState);
        MAPVIEW.getMapAsync(this);
        STRID = getArguments().getString("id");
        COMACT.CAMERALAT = getArguments().getDouble("lat", 0.00);
        COMACT.CAMERALNG = getArguments().getDouble("lng", 0.00);
        COMACT.MAPZOOM = getArguments().getDouble("zoom", 16.0);

        messageBox = view.findViewById(R.id.messageBox);
        messageBox.setBackgroundColor(KawaMap.headerBgColor);
        messageBox.setTextColor(KawaMap.headerTextColor);
        combinePlotBtn = view.findViewById(R.id.combinePlotBtn);
        combinePlotBtn.setTextColor(KawaMap.footerBtnTextColor);
//        combinePlotBtn.setBackground(KawaMap.footerBtnBgColor);
        combinePlotBtn.setVisibility(View.GONE);
        startOverBtn = view.findViewById(R.id.startOverBtn);
        drawBounderyBtn = view.findViewById(R.id.drawBounderyBtn);
        saveBounderyBtn = view.findViewById(R.id.saveBounderyBtn);
        saveBounderyBtn.setTextColor(KawaMap.footerBtnTextColor);
        resetBtn = view.findViewById(R.id.resetBtn);
        clearBtn = view.findViewById(R.id.clearBtn);
        clearNExitLayout = view.findViewById(R.id.clearNExitLayout);
        //clearBtn.setBackgroundColor(KawaMap.footerBtnBgColor);
        //  int orangeColor = Color.parseColor("#FD6035");
        //  resetBtn.setBackgroundColor(orangeColor);
        Button[] innerButtons = null;
        innerButtons = new Button[]{startOverBtn, drawBounderyBtn};
        KawaMap.setInnerButtonColor(getActivity(), innerButtons);
        Button[] footerButtons = null;
        footerButtons = new Button[]{
                saveBounderyBtn,
                combinePlotBtn
        };
        KawaMap.setFooterButtonColor(footerButtons);

        startOverBtn.setOnClickListener(view1 -> startOver());
        drawBounderyBtn.setOnClickListener(view1 -> DrawBounderyBtnClick());
        clearBtn.setOnClickListener(view1 -> clearBoundery());
        saveBounderyBtn.setOnClickListener(view1 -> saveBoundery());
        resetBtn.setOnClickListener(view1 -> resetBoundery());

        combinePlotBtn.setOnClickListener(viewV -> {
            try {
                getMergedCordinates();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MAPBOXMAP = mapboxMap;
        SYMBOLSET = new ArrayList<>();
        MAPBOXMAP.getUiSettings().setCompassEnabled(false);
        MAPBOXMAP.getUiSettings().setLogoEnabled(false);
        MAPBOXMAP.getUiSettings().setFlingVelocityAnimationEnabled(false);
        //    MAPBOXMAP.getUiSettings().setScrollGesturesEnabled(false); // Disable Scroll
        //MAPBOXMAP.setMinZoomPreference(COMACT.MAPZOOM);
        MAPBOXMAP.setStyle(Style.SATELLITE_STREETS, style -> {
            MAPBOXMAP.addOnMapClickListener(this);
            LatLng latLng = new LatLng(COMACT.CAMERALAT, COMACT.CAMERALNG);
            MAPBOXMAP.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(latLng).zoom(COMACT.MAPZOOM).build()), 1000);
            COMACT.initMarker(getActivity(),style, MAPBOXMAP, MAPVIEW);

            new android.os.Handler(Looper.getMainLooper()).postDelayed(
                    new Runnable() {
                        public void run() {
                            COMACT.MAPZOOM = COMACT.MAPZOOM - 2;
                            COMACT.lockZoom(MAPBOXMAP);
                        }
                    },
                    1000);
            getAllCordinates(style);
        });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng coordsOfPoint) {
        if (DRAWENABLE == 1) {
            drawBoundery(coordsOfPoint);
        } else if (DRAWENABLE == 4) {
//            EDITON = true;
//            if (SYMBOLACTIVE == null) {
//                if (DRAWLNGLAT.size() > 0) {
//                     onMaboxMapClick(coordsOfPoint);
//                }
//            }
        } else {
            if (LNGLAT.size() > 0) {
                MAPBOXMAP.getStyle(style -> {
                    for (int i = 0; i < LNGLAT.size(); i++) {
                        boolean contains;
                        contains = COMACT.checkLatLongInPolygon(coordsOfPoint, LNGLAT.get(i));
                        if (contains) {
                            Layer lineLayer = style.getLayer("lineLayerID" + i);
                            if (lineLayer != null) {
                                int flg = 0;
                                combinePlotBtn.setVisibility(View.VISIBLE);

                                if (POLYSELECTED.size() > 0) {
                                    if (POLYSELECTED.contains(i)) {
                                        flg = 1;
                                        POLYSELECTED.remove((Integer) i);
                                        COMACT.segmentEvents(getActivity(), "Farm boundary Selection",
                                                "deselect", MAPBOXMAP, "null", "FARMS_SELECTION");
                                    }
                                }

                                if (flg == 0) {
                                    POLYSELECTED.add(i);
                                    lineLayer.setProperties(PropertyFactory.lineOpacity(1f));
                                    COMACT.segmentEvents(getActivity(), "Farm boundary Selection",
                                            "select", MAPBOXMAP, getSelectedLatLng(), "FARMS_SELECTION");

                                } else {
                                    lineLayer.setProperties(PropertyFactory.lineOpacity(0f));
                                }
                            }
                            break;
                        }
                    }
                });
                int selSize = POLYSELECTED.size();
                String msgPre = "s";
                if (selSize == 1) {
                    msgPre = "";
                }
                messageBox.setText(selSize + " " + getResources().getString(R.string.farm) + msgPre + " " + getResources().getString(R.string.selected));
            }
        }

        return false;
    }

    private void getAllCordinates(Style style) {
        //STRID = "6383c4cd-7889-44ee-8a57-42ff3c01d824";
        LNGLAT.clear();
        DRAWLNGLAT.clear();
        LLARRAY.clear();
        PIDX = 0;
        DRAWENABLE = 0;

        ServiceManager.getInstance().getKawaService().status(KawaMap.KAWA_API_KEY, COMACT.SDK_VERSION, STRID).enqueue(new Callback<PolygonModel>() {
            @Override
            public void onResponse(@NonNull Call<PolygonModel> call, @NonNull Response<PolygonModel> response) {

                try {
                    if (response.isSuccessful()) {
                        COMACT.hideLoader();
                        if (response.body() != null) {

                            COMACT.segmentEvents(getActivity(), "Farm Boundary Response",
                                    "Farm Boundary Response", MAPBOXMAP, String.valueOf(new Gson().toJson(response.body())), "GET_ALL_POLYGON_DATA");
                            COMACT.FARMS_FETCHED_AT = response.body().getData().GET_FARMSs_fetched_at();
                            List<Boundary> newListBoundry = response.body().getData().getBoundaries();
                            if (newListBoundry.size() > 0) {

                                if (POLYSELECTED.size() > 0) {
                                    combinePlotBtn.setVisibility(View.VISIBLE);
                                }

                                for (int i = 0; i < newListBoundry.size(); i++) {
                                    List<List<Double>> cordinates = newListBoundry.get(i).getGeojson().getCoordinates().get(0);
                                    AREA = newListBoundry.get(i).getProperties().getArea();
                                    AREAARRAY.add(AREA);
                                    List<Point> llPts = new ArrayList<>();
                                    List<List<Point>> llPtsA = new ArrayList<>();
                                    List<LatLng> ll = new ArrayList<>();

                                    for (int j = 0; j < cordinates.size(); j++) {
                                        llPts.add(Point.fromLngLat(cordinates.get(j).get(0), cordinates.get(j).get(1)));
                                        ll.add(new LatLng(cordinates.get(j).get(1), cordinates.get(j).get(0)));
                                    }

                                    llPtsA.add(llPts);
                                    LNGLAT.add(ll);
                                    COMACT.drawMapLayers(style, llPts, String.valueOf(i), "list");
                                    if (POLYSELECTED.contains(i)) {
                                        Layer lineLayer = style.getLayer("lineLayerID" + i);
                                        if (lineLayer != null) {
                                            lineLayer.setProperties(PropertyFactory.lineOpacity(1f));
                                        }
                                    }
                                }
                            } else {
                                startOverBtn.setVisibility(View.GONE);
                                messageBox.setVisibility(View.GONE);
                                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                                alertDialog.setTitle(getResources().getString(R.string.app_name));
                                alertDialog.setMessage(getResources().getString(R.string.no_farm_detected));
                                alertDialog.setIcon(R.mipmap.ic_launcher);
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.start_over), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        startOver();
                                    }
                                });
                                alertDialog.show();
                                //Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_farm_detected), Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        COMACT.hideLoader();
                        if (response.errorBody() != null) {
                            JSONObject jsonObj = new JSONObject(response.errorBody().string());
                            Toast.makeText(getApplicationContext(), jsonObj.getString("error"), Toast.LENGTH_LONG).show();// this will tell you why your api doesnt work most of time
                        }
                    }
                } catch (Exception e) {
                    COMACT.hideLoader();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PolygonModel> call, @NonNull Throwable t) {
                COMACT.hideLoader();
                String errorBody = t.getMessage();
                Toast.makeText(getApplicationContext(), errorBody, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getMergedCordinates() throws JSONException {
        if (POLYSELECTED.size() > 0) {
            // if (COMACT.APP_PHASE.equals("1") || COMACT.APP_PHASE.equals("3")|| COMACT.APP_PHASE.equals("4")) {
            if (KawaMap.isMergeEnable) {
                apiCallForMergeSelectedPolygon();
            } else {
                getCoordinatesOfSelectedPolygon(false);
            }
        } else {
            Toast.makeText(getActivity(), R.string.select_farm_to_merge, Toast.LENGTH_LONG).show();
        }
    }

    private void gotoEditPolygon(List<List<LatLng>> mergedCord) {
        CameraPosition cameraPosition = MAPBOXMAP.getCameraPosition();
        COMACT.MAPZOOM = cameraPosition.zoom;
        fragmentEditFarmBoundries fragmentEditFarmBoundries = new fragmentEditFarmBoundries();
        Bundle farms_bundle = new Bundle();
        farms_bundle.putString("id", STRID);
        farms_bundle.putDouble("lat", COMACT.CAMERALAT);
        farms_bundle.putDouble("lng", COMACT.CAMERALNG);
        farms_bundle.putDouble("zoom", COMACT.MAPZOOM);

        farms_bundle.putSerializable("data", (Serializable) mergedCord);
        farms_bundle.putSerializable("polygonarea", (Serializable) POLYGONAREA);
        fragmentEditFarmBoundries.setArguments(farms_bundle);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.kawaMapView, fragmentEditFarmBoundries);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private String getSelectedLatLng() {
        List<String> listFeatures = new ArrayList<>();
        for (int i = 0; i < POLYSELECTED.size(); i++) {
            List<List<Point>> llPtsA = new ArrayList<>();
            List<Point> llPts = new ArrayList<>();
            POLYGONAREA = new ArrayList<>();
            for (int j = 0; j < LNGLAT.get(POLYSELECTED.get(i)).size(); j++) {
                List<LatLng> ll = LNGLAT.get(POLYSELECTED.get(i));
                llPts.add(Point.fromLngLat(ll.get(j).getLongitude(), ll.get(j).getLatitude()));
                POLYGONAREA.add(AREAARRAY.get(POLYSELECTED.get(i)));
            }
            llPtsA.add(llPts);
            Feature multiPointFeature = Feature.fromGeometry(Polygon.fromLngLats(llPtsA));
            listFeatures.add(multiPointFeature.toJson());
        }

        String strMerge = String.valueOf(listFeatures);
        return strMerge;
    }

    private void startOver() {

        COMACT.segmentEvents(getActivity(), "Start Over",
                "user clicked on Start over", MAPBOXMAP, "", "START_OVER");
        fragmentFarmLocation fragmentFarmLocation = new fragmentFarmLocation();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.kawaMapView, fragmentFarmLocation);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void apiCallForMergeSelectedPolygon() {
        List<String> listFeatures = new ArrayList<>();
        POLYGONAREA = new ArrayList<>();
        for (int i = 0; i < POLYSELECTED.size(); i++) {
            List<List<Point>> llPtsA = new ArrayList<>();
            List<Point> llPts = new ArrayList<>();

            for (int j = 0; j < LNGLAT.get(POLYSELECTED.get(i)).size(); j++) {
                List<LatLng> ll = LNGLAT.get(POLYSELECTED.get(i));
                llPts.add(Point.fromLngLat(ll.get(j).getLongitude(), ll.get(j).getLatitude()));
            }
            //POLYGONAREA.add(AREAARRAY.get(POLYSELECTED.get(i)));
            llPtsA.add(llPts);
            Feature multiPointFeature = Feature.fromGeometry(Polygon.fromLngLats(llPtsA));
            listFeatures.add(multiPointFeature.toJson());
        }

        String strMerge = "{\"farms_fetched_at\":" + "\"" + COMACT.FARMS_FETCHED_AT + "\"" + ",\"recipe_id\":\"farm_boundaries\",\"aois\":" + String.valueOf(listFeatures) + "}";
        JsonObject selectedFarms = JsonParser.parseString(strMerge).getAsJsonObject();
        interfaceKawaEvents.onkawaSelect(selectedFarms);
        //Phase second
        COMACT.showLoader(getActivity(),"isCircle");
        ServiceManager.getInstance().getKawaService().getMergedPoints(KawaMap.KAWA_API_KEY, COMACT.SDK_VERSION, selectedFarms).enqueue(new Callback<MergeModel>() {
            @Override
            public void onResponse(@NonNull Call<MergeModel> call, @NonNull Response<MergeModel> response) {
                COMACT.hideLoader();
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            List<ResponseKawa> responseKawa = response.body().getResponse();
                            List<List<LatLng>> lngLat = new ArrayList<>();

                            if (responseKawa.size() > 0) {
                                for (int i = 0; i < responseKawa.size(); i++) {
                                    List<LatLng> ll = new ArrayList<>();
                                    List<List<Float>> cordinates = responseKawa.get(i).getGeometry().getCoordinates().get(0);
                                    if (cordinates.size() > 0) {
                                        for (int j = 0; j < cordinates.size(); j++) {
                                            ll.add(new LatLng(cordinates.get(j).get(1), cordinates.get(j).get(0)));
                                        }
                                        lngLat.add(ll);
                                    }
                                    POLYGONAREA.add(responseKawa.get(i).getProperties().getArea());
                                }
                                COMACT.segmentEvents(getActivity(), "Save Selection",
                                        String.valueOf(selectedFarms), MAPBOXMAP, String.valueOf(new Gson().toJson(response.body())), "SAVE_ON_SUCCESS");
                                gotoEditPolygon(lngLat);

                            }
                        }
                    } else {
                        COMACT.hideLoader();
                        //assert response.errorBody() != null;
                        if (response.errorBody() != null) {
                            JSONObject jsonObj = new JSONObject(response.errorBody().string());
                            COMACT.segmentEvents(getActivity(), "Save Selection",
                                    String.valueOf(selectedFarms), MAPBOXMAP, jsonObj.getString("error"), "TYPESAVEFAIL");
                            Toast.makeText(getApplicationContext(), jsonObj.getString("error"), Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    COMACT.hideLoader();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MergeModel> call, @NonNull Throwable t) {
                COMACT.hideLoader();
                String errorBody = t.getMessage();
                Toast.makeText(getApplicationContext(), errorBody, Toast.LENGTH_LONG).show();
                //Toast.makeText(getActivity(), "onResponse:Failure " + errorBody, Toast.LENGTH_LONG).show(); // this will tell you why your api doesnt work most of time
            }
        });
    }

    private void getCoordinatesOfSelectedPolygon(boolean drawMode) {
        if (drawMode) {
            List<Point> llPts = new ArrayList<>();
            List<List<Point>> llPtsA = new ArrayList<>();
            for (int j = 0; j < DRAWLNGLAT.get(0).size(); j++) {
                llPts.add(Point.fromLngLat(DRAWLNGLAT.get(0).get(j).getLongitude(), DRAWLNGLAT.get(0).get(j).getLatitude()));
            }
            llPtsA.add(llPts);
            Feature multiPointFeature = Feature.fromGeometry(Polygon.fromLngLats(llPtsA));
            double drawPolyAreaSM = TurfMeasurement.area(multiPointFeature);
            float drawPolyAreaAcre = (float) (drawPolyAreaSM * 0.000247105);
            POLYGONAREA.add(drawPolyAreaAcre);
            gotoEditPolygon(DRAWLNGLAT);
        } else {
            List<List<LatLng>> lngLat = new ArrayList<>();
            POLYGONAREA = new ArrayList<>();
            for (int i = 0; i < POLYSELECTED.size(); i++) {
                lngLat.add(LNGLAT.get(POLYSELECTED.get(i)));
                POLYGONAREA.add(AREAARRAY.get(POLYSELECTED.get(i)));
            }
            gotoEditPolygon(lngLat); // Will be removed once service call is used
        }

    }

    private void hideAllBtn() {
        drawBounderyBtn.setVisibility(View.GONE);
//        startOverBtn.setVisibility(View.GONE);
        combinePlotBtn.setVisibility(View.GONE);
        resetBtn.setVisibility(View.GONE);
        clearBtn.setVisibility(View.GONE);
        clearNExitLayout.setVisibility(View.GONE);
        saveBounderyBtn.setVisibility(View.GONE);
    }

    private void resetBoundery() {
        changeBorderColor("#F7F14A");
        messageBox.setBackgroundColor(KawaMap.headerBgColor);
        messageBox.setText(getResources().getString(R.string.start_selecting_label));
        hideShowSymbol("hide", false);
        hideShowPolygon(0.6f);
        DRAWENABLE = 0;
        hideAllBtn();
        drawBounderyBtn.setVisibility(View.VISIBLE);
        saveBounderyBtn.setVisibility(View.GONE);
        DRAWLNGLAT.clear();
        DRAWLNGLAT = new ArrayList<>();
        LLARRAY.clear();
        LLARRAY = new ArrayList<>();
        FIRSTPOINTOFPOLY = new LatLng();
    }

    private void clearBoundery() {
        changeBorderColor("#F7F14A");
        hideAllBtn();
        DRAWENABLE = 1;
        clearNExitLayout.setVisibility(View.VISIBLE);
        resetBtn.setVisibility(View.VISIBLE);
        clearBtn.setVisibility(View.VISIBLE);
        hideShowPolygon(0f);
        hideShowSymbol("hide", false);
        saveBounderyBtn.setVisibility(View.GONE);
        // resetBtn.setVisibility(View.VISIBLE);
        DRAWLNGLAT.clear();
        DRAWLNGLAT = new ArrayList<>();
        LLARRAY.clear();
        LLARRAY = new ArrayList<>();
        FIRSTPOINTOFPOLY = new LatLng();
        messageBox.setText("Click on map and start drawing");
        messageBox.setBackgroundColor(KawaMap.headerBgColor);
    }

    private void saveBoundery() {
        KawaMap.isDrawEnable = true;
        getCoordinatesOfSelectedPolygon(true);
//        Toast.makeText(getContext(),"working on it",Toast.LENGTH_LONG).show();
    }

    private void DrawBounderyBtnClick() {
        hideAllBtn();
        clearBtn.setVisibility(View.VISIBLE);
        resetBtn.setVisibility(View.VISIBLE);
        clearNExitLayout.setVisibility(View.VISIBLE);
        POLYSELECTED.clear();
        DRAWLNGLAT.clear();
        DRAWLNGLAT = new ArrayList<>();
        messageBox.setText("Click on map and start drawing");
        DRAWENABLE = 1;
        hideShowPolygon(0f);
    }

    private void drawBoundery(LatLng coordsOfPoint) {
        double lat = coordsOfPoint.getLatitude();
        double lng = coordsOfPoint.getLongitude();

        if (DRAWLNGLAT.size() == 0) {
            FIRSTPOINTOFPOLY = (new LatLng(lat, lng));
            messageBox.setText(getResources().getString(R.string.click_to_add_another_point));
        }

        double distanceMeters = coordsOfPoint.distanceTo((LatLng) FIRSTPOINTOFPOLY);
        if (distanceMeters > 10) {
            LLARRAY.add(new LatLng(coordsOfPoint.getLatitude(), coordsOfPoint.getLongitude()));
            LATLNGPTS.add(Point.fromLngLat(coordsOfPoint.getLatitude(), coordsOfPoint.getLongitude()));
        } else {
            LLARRAY.add(new LatLng(FIRSTPOINTOFPOLY));
        }

        DRAWLNGLAT.clear();
        DRAWLNGLAT = new ArrayList<>();
        DRAWLNGLAT.add(LLARRAY);
        if (DRAWLNGLAT.get(0).size() == 3) {
            messageBox.setText(getResources().getString(R.string.touch_first_point));
            firstPointActive();
        }
        if (DRAWLNGLAT.size() > 0) {
            if (distanceMeters > 10 || DRAWLNGLAT.get(0).size() <= 1) {
                drawSymbol();
            } else {
                DRAWENABLE = 4;
            }
            COMACT.drawMapLayers(MAPBOXMAP.getStyle(), LATLNGPTS, String.valueOf("DP" + PIDX), "TranStyle");
            PIDX += 1;
            redrawFarms();
            if (ISINTERSECT == 0 && DRAWENABLE == 4) {
                saveBounderyBtn.setVisibility(View.VISIBLE);
                messageBox.setText(getResources().getString(R.string.complete_marking_save_polygon));
            }
        }

    }

    private void firstPointActive() {
        if (SYMBOLSET.size() > 0) {
            for (int i = 0; i < SYMBOLSET.size() - 1; i++) {
                SymbolManager sm = SYMBOLSET.get(i);
                if (sm != null) {
                    LongSparseArray<Symbol> annotations = sm.getAnnotations();
                    if (!annotations.isEmpty()) {
                        if (annotations.size() > 0) {
                            for (int j = 0; j < annotations.size(); j++) {
                                Symbol symbol = annotations.get(j);
                                symbol.setIconImage("symbol_active");
                                SYMBOLSET.get(i).update(symbol);
                            }
                        }
                    }

                }
            }
        }


    }

    private void drawSymbol() {
        Style style = MAPBOXMAP.getStyle();
        symbolManager = new SymbolManager(MAPVIEW, MAPBOXMAP, style);
        symbolManager.setIconAllowOverlap(true);
        symbolManager.setTextAllowOverlap(true);
        SYMBOLSET.add(symbolManager);
        style.addImage("symbol_blue", BitmapFactory.decodeResource(this.getResources(), R.drawable.symbol_blue));
        style.addImage("symbol_yellow", BitmapFactory.decodeResource(this.getResources(), R.drawable.symbol_yellow));
        style.addImage("symbol_active", BitmapFactory.decodeResource(this.getResources(), R.drawable.symbol_activeb));

        JsonObject objD = new JsonObject();
        //objD.addProperty("lIndex", idx);
        //if(symbolManager )
        int j = DRAWLNGLAT.get(0).size() - 1;
        LASTINDEXOFSELECTEDPOLYGON = DRAWLNGLAT.get(0).size();

        objD.addProperty("sIndex", j);
//        boolean draggable = true;
//        String icon_name = "symbol_blue";
//        if (j == 0)
//            icon_name = "symbol_active";
//        else
//            draggable = true;
        symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(DRAWLNGLAT.get(0).get(j).getLatitude(), DRAWLNGLAT.get(0).get(j).getLongitude()))
                .withIconImage("symbol_blue")
                .withIconSize(0.5f)
                .withDraggable(true)
                .withIconOpacity(0.8f)
                .withData(objD)
        );
//        symbolManager.addClickListener(symbol -> {
//            JsonObject objS = (JsonObject) symbol.getData();
//            int symbolIndex = objS.get("sIndex").getAsInt();
//            if ( symbolIndex == 0) {
//            }
//
//            /*if (EDITON) {
//                if (symbol.getIconOpacity() > 0) {
//                    int flg = 0;
//                    if (SYMBOLACTIVE != null) {
//
//                        if (!symbol.equals(SYMBOLACTIVE)) {
//                            SYMBOLACTIVE.setDraggable(true);
//                            SYMBOLACTIVE.setIconImage("symbol_blue");
//                            SYMBOLACTIVE.setIconSize(0.3F);
//                            symbolManager.update(SYMBOLACTIVE);
//                            flg = 1;
//                        }
//                    } else {
//                        flg = 1;
//                    }
//
//                    if (flg == 1) {
//                        SYMBOLACTIVE = symbol;
//                        symbol.setDraggable(true);
//                        symbol.setIconImage("symbol_yellow");
//                        symbol.setIconSize(0.5f);
//                        symbolManager.update(symbol);
//                        // onSymbolSelected();
//
//                    }
//                }
//            }*/
//            return true;
//        });

        symbolManager.addDragListener(new OnSymbolDragListener() {
            @Override
            public void onAnnotationDragStarted(Symbol symbol) {
                JsonObject objD = (JsonObject) symbol.getData();
                int sIndex = objD.get("sIndex").getAsInt();
                if (sIndex == 0 && DRAWLNGLAT.get(0).size() > 2) {
                    onMapClick(symbol.getLatLng());
                }
            }

            @Override
            public void onAnnotationDrag(Symbol symbol) {
                JsonObject objD = (JsonObject) symbol.getData();
                int sIndex = objD.get("sIndex").getAsInt();
                if (sIndex == 0) {
                    FIRSTPOINTOFPOLY = symbol.getLatLng();
                    DRAWLNGLAT.get(0).set(0, symbol.getLatLng());
                    if (DRAWENABLE == 4)
                        DRAWLNGLAT.get(0).set(LASTINDEXOFSELECTEDPOLYGON, symbol.getLatLng());
                } else {
                    DRAWLNGLAT.get(0).set(sIndex, symbol.getLatLng());
                }
                redrawFarms();
                 if(ISINTERSECT == 0 && DRAWENABLE != 4){
                    messageBox.setText(getResources().getString(R.string.touch_first_point));
                }
                if (ISINTERSECT == 0 && DRAWENABLE == 4) {
                    messageBox.setText(getResources().getString(R.string.complete_marking_save_polygon));
                }
            }

            @Override
            public void onAnnotationDragFinished(Symbol symbol) {
                JsonObject objD = (JsonObject) symbol.getData();
                Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.borders_cant_intersect), Toast.LENGTH_SHORT);

                if (ISINTERSECT == 1) {
                    toast.show();
                    saveBounderyBtn.setVisibility(View.GONE);
                }
                else if (DRAWENABLE == 4) {
                    messageBox.setText(getResources().getString(R.string.complete_marking_save_polygon));
                    saveBounderyBtn.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    /* private void onMaboxMapClick(LatLng coordsOfPoint) {
         MAPBOXMAP.getStyle(style -> {
             boolean contains = false;
             Integer foundIdx = -1;

             if (coordsOfPoint != null) {
                 for (int i = 0; i < DRAWLNGLAT.size(); i++) {
                     contains = COMACT.checkLatLongInPolygon(coordsOfPoint, DRAWLNGLAT.get(i));
                     if (contains) {
                         foundIdx = i;
                         break;
                     }
                 }
             }
             for (int i = 0; i < DRAWLNGLAT.size(); i++) {
                 float opacityL = 0.3f;
                 float opacityP = 0.3f;
                 if (foundIdx == i) {

                     opacityL = 1.0f;
                     opacityP = 0.6f;
                 }
                 Layer lineLayer = style.getLayer("lineLayerID" + i);
                 Layer polyLayer = style.getLayer("polyLayerID" + i);
                 if (lineLayer != null && polyLayer != null) {
                     lineLayer.setProperties(PropertyFactory.lineOpacity(opacityL));
                     polyLayer.setProperties(PropertyFactory.fillOpacity(opacityP));
                 }
             }
             new Handler(Looper.getMainLooper()).postDelayed(
                     new Runnable() {
                         public void run() {

                             hideShowSymbol("show", false);
                         }
                     },
                     300);

         });
     }
 */
    private void hideShowSymbol(String type, boolean discard) {
        if (SYMBOLSET.size() > 0) {
            for (int i = 0; i < SYMBOLSET.size(); i++) {
                SymbolManager sm = SYMBOLSET.get(i);
                if (sm != null) {
                    float opacity = 0.0f;
                    if (type.equals("show"))
                        opacity = 1.0f;

                    LongSparseArray<Symbol> annotations = sm.getAnnotations();
                    if (!annotations.isEmpty()) {
                        if (annotations.size() > 0) {
                            for (int j = 0; j < annotations.size(); j++) {
                                Symbol symbol = annotations.get(j);

                                if (discard) {
                                    JsonObject symbolData = (JsonObject) symbol.getData();
                                    //Integer sIndex = symbolData.get("sIndex").getAsInt();
                                    //Integer lIndex = symbolData.get("lIndex").getAsInt();
                                    LatLng latLng = DRAWLNGLAT.get(i).get(j);
                                    if (latLng != null)
                                        setSymbolLL(symbol, latLng);
                                }

                                if (opacity > 0 && symbol.getIconOpacity() <= 0 || opacity <= 0 && symbol.getIconOpacity() > 0) {
                                    symbol.setIconOpacity(opacity);
                                    symbol.setDraggable(false);
                                    symbol.setIconImage("symbol_blue");
                                    symbol.setIconSize(0.3f);
                                    SYMBOLSET.get(i).update(symbol);
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private void hideShowPolygon(float opacityP) {
        Style style = MAPBOXMAP.getStyle();
        for (int i = 0; i < LNGLAT.size(); i++) {
            Layer lineLayer = style.getLayer("lineLayerID" + i);
            Layer polyLayer = style.getLayer("polyLayerID" + i);

            lineLayer.setProperties(PropertyFactory.lineOpacity(0f));
            polyLayer.setProperties(PropertyFactory.fillOpacity(opacityP));
        }
        for (int j = 0; j < PIDX; j++) {
            Layer lineLayer = style.getLayer("lineLayerIDDP" + j);
            Layer polyLayer = style.getLayer("polyLayerIDDP" + j);

            lineLayer.setProperties(PropertyFactory.lineOpacity(0f));
            polyLayer.setProperties(PropertyFactory.fillOpacity(0f));

        }

    }


    private void setSymbolLL(Symbol symbol, LatLng latLng) {
        JsonObject objD = (JsonObject) symbol.getData();
        Integer sIndex = objD.get("sIndex").getAsInt();
        DRAWLNGLAT.get(0).set(sIndex, symbol.getLatLng());
        symbol.setLatLng(latLng);
        SYMBOLSET.get(0).update(symbol);
    }

    private void redrawFarms() {
        if (DRAWLNGLAT.get(0) != null) {
            MAPBOXMAP.getStyle(style -> {
                List<Point> llPts = new ArrayList<>();
                List<List<Point>> llPtsA = new ArrayList<>();
                for (int j = 0; j < DRAWLNGLAT.get(0).size(); j++) {
                    llPts.add(Point.fromLngLat(DRAWLNGLAT.get(0).get(j).getLongitude(), DRAWLNGLAT.get(0).get(j).getLatitude()));
                    Layer lineLayer = style.getLayer("lineLayerIDDP" + j);
                    Layer polyLayer = style.getLayer("polyLayerIDDP" + j);
                    if (lineLayer != null && polyLayer != null) {
                        lineLayer.setProperties(PropertyFactory.lineOpacity(1.0f));
                        polyLayer.setProperties(PropertyFactory.fillOpacity(0f));
                    }

                }
                llPtsA.add(llPts);
                GeoJsonSource lineSourceID = style.getSourceAs("lineSourceIDDP" + 0);
                GeoJsonSource polySourceID = style.getSourceAs("polySourceIDDP" + 0);

                if (lineSourceID != null) {
                    lineSourceID.setGeoJson(FeatureCollection.fromJson(""));
                    polySourceID.setGeoJson(FeatureCollection.fromJson(""));
                    lineSourceID.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(llPts)));
                    polySourceID.setGeoJson(Feature.fromGeometry(Polygon.fromLngLats(llPtsA)));
                }
            });
            checkForIntersections();

        }

    }

    private void checkForIntersections() {
        Point lnPoint1, lnPoint2, lnPoint3, lnPoint4;
        int intersectionCheckLimit = DRAWLNGLAT.get(0).size() - 2;
        ISINTERSECT = 0;
        changeBorderColor("#F7F14A");
        //messageBox.setText(getResources().getString(R.string.complete_marking_save_polygon));
        messageBox.setBackgroundColor(KawaMap.headerBgColor);
        if (DRAWLNGLAT.get(0).size() > 2) {
            for (int j = 0; j < intersectionCheckLimit; j++) {
                lnPoint1 = Point.fromLngLat(DRAWLNGLAT.get(0).get(j).getLongitude(), DRAWLNGLAT.get(0).get(j).getLatitude());
                lnPoint2 = Point.fromLngLat(DRAWLNGLAT.get(0).get(j + 1).getLongitude(), DRAWLNGLAT.get(0).get(j + 1).getLatitude());
                for (int k = 0; k <= intersectionCheckLimit; k++) {
                    if (k != j && k != (j + 1) && (k + 1) != j && (k + 1) != (j + 1)) {
                        lnPoint3 = Point.fromLngLat(DRAWLNGLAT.get(0).get(k).getLongitude(), DRAWLNGLAT.get(0).get(k).getLatitude());
                        lnPoint4 = Point.fromLngLat(DRAWLNGLAT.get(0).get(k + 1).getLongitude(), DRAWLNGLAT.get(0).get(k + 1).getLatitude());
                        if (lineIntersect(lnPoint1, lnPoint2, lnPoint3, lnPoint4)) {
                            changeBorderColor("#ff0000");
                            messageBox.setText(getResources().getString(R.string.borders_cant_intersect));
                            messageBox.setBackgroundColor(getResources().getColor(R.color.mapboxRed));
                            ISINTERSECT = 1;
                            break;
                        }
//                        else {
////                            changeBorderColor("#F7F14A");
//                           //  messageBox.setText(getResources().getString(R.string.complete_marking_save_polygon));
//                        }
                    }
                }
                if (ISINTERSECT == 1) {
                    break;
                }
            }
        }
    }

    private boolean lineIntersect(Point lnPoint1, Point lnPoint2, Point lnPoint3, Point lnPoint4) {
        double x1 = lnPoint1.latitude();
        double y1 = lnPoint1.longitude();
        double x2 = lnPoint2.latitude();
        double y2 = lnPoint2.longitude();
        double x3 = lnPoint3.latitude();
        double y3 = lnPoint3.longitude();
        double x4 = lnPoint4.latitude();
        double y4 = lnPoint4.longitude();
        ///below if is to ignore if line 1 endpoint and line 2 start point is same
        if (x1 != x4 && y1 != y4) {
            double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
            if (denom == 0.0) { // Lines are parallel.
                //retrn null;
            }
            double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
            double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
            if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f)
                return true;
            else
                return false;
        }
        return false;
    }


    private void changeBorderColor(String borderColorCode) {
        Style style = MAPBOXMAP.getStyle();
        for (int k = 0; k < PIDX; k++) {
            Layer lineLayer = style.getLayer("lineLayerIDDP" + k);
            Layer polyLayer = style.getLayer("polyLayerIDDP" + k);
            Integer borderColor = Color.parseColor(borderColorCode);
            lineLayer.setProperties(PropertyFactory.lineColor(borderColor));
//            polyLayer.setProperties(PropertyFactory.fillOpacity(0f));

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MAPVIEW.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        MAPVIEW.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        MAPVIEW.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        MAPVIEW.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        MAPVIEW.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MAPVIEW.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        MAPVIEW.onLowMemory();
    }
}