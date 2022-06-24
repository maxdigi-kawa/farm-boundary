package com.kawasdk.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kawasdk.Model.MergeModel;
import com.kawasdk.R;
import com.kawasdk.Utils.AddressServiceManager;
import com.kawasdk.Utils.Common;
import com.kawasdk.Utils.FormBuilder;
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

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class fragmentEditFarmBoundries extends Fragment implements OnMapReadyCallback, MapboxMap.OnMapClickListener {
    private Common COMACT;
    //    private int PIDX = 0;
    private int ISINTERSECT = 0;
    private View VIEW;
    private MapView MAPVIEW;
    private MapboxMap MAPBOXMAP;
    String TAG = "KAWA";
    String STRID = "";
    String SELECTEDPOINTS = "";
    String PREVIOUSSELECTEDPOINTS = "";

    private List<List<LatLng>> LNGLAT = new ArrayList<>();
    private List<List<LatLng>> LNGLATEDIT = new ArrayList<>();
    List<List<Point>> LATLNGARR = new ArrayList<>();
    ArrayList POLYGONAREA = new ArrayList<>();

    private static Integer LAYERINDEX = -1;
    private static List<List<SymbolManager>> SYMBOLSET = new ArrayList<>();
    private static List<SymbolManager> NOSYMBOLSET = new ArrayList<>();
    List<String> BEFOREEDITLISTFEATURE = new ArrayList<>();
    private static Symbol SYMBOLACTIVE;
    private boolean EDITON = false;
    InterfaceKawaEvents interfaceKawaEvents;
    Integer FINALFOUNDIDX;
    Button correctBoundryBtn, saveEditBtn, completeMarkingBtn, saveDetailBtn, addDetailBtn, saveDetailnNextBtn, startOverBtn, addMoreBtn, discardEditBtn, backBtn, markAnotherBtn, exitBtn;
    ImageButton downBtn, upBtn, leftBtn, zoomOutBtn, zoomInBtn, trashBtn, rightBtn;
    LinearLayout detailsForm, thankyouLinearLayout, farmDetailsLayout, anotherndExitLayout;
    TextView totalAreaTv, totalseedsTv, addressTv;
    ImageView dootedLineFirst, dootedLineSecond;
    JSONArray farm_fields_array;
    Map<String, String> farm_fields_value = new HashMap<>();
    //    Spinner spinnerView;
    EditText[] myTextViews;
    Spinner[] spinnerViewArray;
    TextView messageBox;
    LinearLayout farm_mark_messagebox, seedsLayout, locationLayout, areaLayout;
    Handler mHandler = new Handler(Looper.getMainLooper());
    String STRSUBMIT;
    int TOTALPOLYGON;
    int CURRENTPOLYGON;
    JSONObject FILEDSOBJECT;
    JSONArray FILEDSARRAY;
    Integer LASTINDEXOFSELECTEDPOLYGON;
    String SETERRORMSG = "";
    boolean FORMVALIDATE = false;
    int activeSymbolIndex = -1;
//    int SELECTEDPOLYGON = 0;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_farm_boundries, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        super.onCreate(savedInstanceState);

        VIEW = view;
        MAPVIEW = view.findViewById(R.id.mapView);
        MAPVIEW.onCreate(savedInstanceState);
        MAPVIEW.getMapAsync(this);

        LNGLATEDIT = (List<List<LatLng>>) getArguments().getSerializable("data");
        POLYGONAREA = (ArrayList) getArguments().getSerializable("polygonarea");
        Log.e(TAG, "POLYGONAREA: " + POLYGONAREA);

        STRID = getArguments().getString("id");
        COMACT.CAMERALAT = getArguments().getDouble("lat", 0.00);
        COMACT.CAMERALNG = getArguments().getDouble("lng", 0.00);
        COMACT.MAPZOOM = getArguments().getDouble("zoom", 17.00);

        messageBox = view.findViewById(R.id.messageBox);
        farm_mark_messagebox = view.findViewById(R.id.farm_mark_messagebox);
        messageBox.setBackgroundColor(KawaMap.headerBgColor);
        messageBox.setTextColor(KawaMap.headerTextColor);

        initButtons();
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap MAPBOXMAP1) {
        onFarmsLoaded();
        SYMBOLSET = new ArrayList<>();
        MAPBOXMAP = MAPBOXMAP1;
        MAPBOXMAP.getUiSettings().setCompassEnabled(false);
        MAPBOXMAP.getUiSettings().setLogoEnabled(false);
        MAPBOXMAP.getUiSettings().setFlingVelocityAnimationEnabled(false);
        //  MAPBOXMAP.getUiSettings().setScrollGesturesEnabled(false); // Disable Scroll
        MAPBOXMAP.getUiSettings().setDoubleTapGesturesEnabled(false);
        MAPBOXMAP.setStyle(Style.SATELLITE_STREETS, style -> {
            // Log.e(TAG, "onMapReady: "+PIDX );
            LatLng latLng = new LatLng(COMACT.CAMERALAT, COMACT.CAMERALNG);
            MAPBOXMAP.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(latLng).zoom(COMACT.MAPZOOM).build()), 1000);
            COMACT.lockZoom(MAPBOXMAP);//----------
            MAPBOXMAP.addOnMapClickListener(this);
            LNGLAT = new ArrayList<>();
            //  PIDX = 0;
            if (LNGLATEDIT.size() > 0) {
                for (int i = 0; i < LNGLATEDIT.size(); i++) {
                    List<LatLng> ll = new ArrayList<>();
                    List<Point> llPts = new ArrayList<>();
                    for (int j = 0; j < LNGLATEDIT.get(i).size(); j++) {
                        double lat = LNGLATEDIT.get(i).get(j).getLatitude();
                        double lng = LNGLATEDIT.get(i).get(j).getLongitude();
                        llPts.add(Point.fromLngLat(lng, lat));
                        ll.add(new LatLng(lat, lng));
                    }
                    LNGLAT.add(ll);
                    LATLNGARR.add(llPts);
                    //   PIDX += 1;
                    COMACT.drawMapLayers(style, llPts, String.valueOf(i), "edit");
                    Log.e(TAG, "onMapReady: layer id : " + style.getLayer("polyLayerID" + i).toString());
                    int middlepolyposition = LNGLAT.get(i).size() / 2;
                    if (KawaMap.isFormEnable)
                        drawSymbolsforPolygon(middlepolyposition, i);
                    drawSymbol(style, i, 0.0f);
                    Feature multiPointFeature = Feature.fromGeometry(Polygon.fromLngLats(LATLNGARR));
                    multiPointFeature.addStringProperty("area", String.valueOf(POLYGONAREA.get(i)));
                    BEFOREEDITLISTFEATURE.add(multiPointFeature.toJson());
                }
            }
            COMACT.initMarker(getActivity(), style, MAPBOXMAP, MAPVIEW);
        });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng coordsOfPoint) {
        Log.e(TAG, "onMapClick: total polyon 1: " + LNGLATEDIT.size());
        if (EDITON && SYMBOLACTIVE == null) {
            if (LNGLATEDIT.size() > 0) {
                Log.e(TAG, "onMapClick: total polyon 2: " + LNGLATEDIT.size());
                onMaboxMapClick(coordsOfPoint);
            }
        }
        return false;
    }

    private void onMaboxMapClick(LatLng coordsOfPoint) {
        MAPBOXMAP.getStyle(style -> {
            boolean contains = false;
            Integer foundIdx = -1;

            if (coordsOfPoint != null) {
                for (int i = 0; i < LNGLATEDIT.size(); i++) {
                    contains = COMACT.checkLatLongInPolygon(coordsOfPoint, LNGLATEDIT.get(i));
                    if (contains) {
                        foundIdx = i;
                        break;
                    }
                }
            }
            Log.e(TAG, "foundIdx: " +foundIdx+"LAYERINDEX: " +LAYERINDEX);
            if (foundIdx >= 0 && LAYERINDEX != foundIdx) {
                for (int i = 0; i < LNGLATEDIT.size(); i++) {
                    float opacityL = 0.3f;
                    float opacityP = 0.3f;
                    float borderL = 0.3f;
                    if (foundIdx == i) {

                        opacityL = 1.0f;
                        opacityP = 0.6f;
                        borderL = 2f;
                    }
                    Layer lineLayer = style.getLayer("lineLayerID" + i);
                    Layer polyLayer = style.getLayer("polyLayerID" + i);
                    if (lineLayer != null && polyLayer != null) {
                        lineLayer.setProperties(PropertyFactory.lineOpacity(opacityL));
                        lineLayer.setProperties(PropertyFactory.lineWidth(borderL));
                        polyLayer.setProperties(PropertyFactory.fillOpacity(opacityP));
                    }
                }
                FINALFOUNDIDX = foundIdx;
                new Handler(Looper.getMainLooper()).postDelayed(
                        new Runnable() {
                            public void run() {
                                if (FINALFOUNDIDX != -1) {
                                    Log.e(TAG, "run: polygonIndex : " + FINALFOUNDIDX );
                                    LAYERINDEX = FINALFOUNDIDX;
                                    onFarmSelected(LAYERINDEX);
                                    trashBtn.setVisibility(View.GONE);
                                    showHideSymbol("show", false);
//                                SELECTEDPOLYGON = FINALFOUNDIDX;
                                    //Log.e("onMapClick AFTER", String.valueOf(EDITON) + " : " + String.valueOf(LAYERINDEX));
                                }

                            }
                        },
                        300);


            }
        });
    }

    private void drawSymbol(Style style, int idx, float opacity) {
        List<SymbolManager> symbolManagers = new ArrayList<>();
        for (int j = 0; j < LNGLATEDIT.get(idx).size() - 1; j++) {
            SymbolManager symbolManager = new SymbolManager(MAPVIEW, MAPBOXMAP, style);
            symbolManager.setIconAllowOverlap(true);
            symbolManager.setTextAllowOverlap(true);
            symbolManagers.add(symbolManager);
            style.addImage("symbol_blue", BitmapFactory.decodeResource(this.getResources(), R.drawable.symbol_blue));
            style.addImage("symbol_yellow", BitmapFactory.decodeResource(this.getResources(), R.drawable.symbol_yellow));
            style.addImage("symbol_active", BitmapFactory.decodeResource(this.getResources(), R.drawable.symbol_activeb));

            JsonObject objD = new JsonObject();
            objD.addProperty("sIndex", j);
            symbolManager.create(new SymbolOptions()
                    .withLatLng(new LatLng(LNGLATEDIT.get(idx).get(j).getLatitude(), LNGLATEDIT.get(idx).get(j).getLongitude()))
                    .withIconImage( j == 0 ? "symbol_active" : "symbol_blue")
                    .withIconSize(0.5f)
                    .withDraggable(true)
                    .withIconOpacity(opacity)
                    .withData(objD));

            symbolManager.addClickListener(symbol -> {
                // Log.e("EDITON - LAYERINDEX", String.valueOf(EDITON) + " - " + LAYERINDEX);
                if (EDITON && LAYERINDEX >= 0) {
                    // Log.e("getIconOpacity SYMBOL", String.valueOf(symbol.getIconOpacity()));
                    if (symbol.getIconOpacity() > 0) {
                        int flg = 0;
                        JsonObject objD1 = (JsonObject) symbol.getData();
                        int sIndex = objD1.get("sIndex").getAsInt();
                        activeSymbolIndex = sIndex;
                        Log.e(TAG, "drawSymbol: activeIndex  : " + activeSymbolIndex);
//                        if(activeSymbolIndex > 0){
//                            Log.e(TAG, "drawSymbol: ", );
//                            symbol.setIconSize(0.7f);
//                        }
//                        Log.e(TAG, "drawSymbol: SYMBOLACTIVE : " + SYMBOLACTIVE);
//                        if(SYMBOLACTIVE != null) {
//                            SYMBOLACTIVE.setIconSize(0.5f);
//                        }
                        SYMBOLACTIVE = symbol;
                        symbolManager.update(symbol);
                        onSymbolSelected();
                        SELECTEDPOINTS = "lat : " + symbol.getLatLng().getLatitude() + " lng : " + symbol.getLatLng().getLongitude();
                        COMACT.segmentEvents(getActivity(), "Point selection for editing",
                                String.valueOf(symbol.getLatLng().getLatitude()), MAPBOXMAP, String.valueOf(symbol.getLatLng().getLongitude()), "TAP_APOINT_TOEDIT");
                    }
                }
                return true;
            });

            symbolManager.addDragListener(new OnSymbolDragListener() {
                LatLng previouscoord;
                String strsubmit;

                @Override
                public void onAnnotationDragStarted(Symbol symbol) {
                    if (symbol.getIconOpacity() > 0) {
                        previouscoord = symbol.getLatLng();
                        JsonObject objD = (JsonObject) symbol.getData();
                        int sIndex = objD.get("sIndex").getAsInt();
                        activeSymbolIndex = sIndex;
//                        if(activeSymbolIndex > 0){
//                            symbol.setIconSize(0.7f);
//                        }
//                        Log.e(TAG, "onAnnotationDragStarted: SYMBOLACTIVE : " + SYMBOLACTIVE);
//                        if(SYMBOLACTIVE != null) {
//                            SYMBOLACTIVE.setIconSize(0.5f);
//                        }
                        SYMBOLACTIVE = symbol;
                        onSymbolSelected();
                    }
                }

                @Override
                public void onAnnotationDrag(Symbol symbol) {
                    if (symbol.getIconOpacity() > 0) {
                        JsonObject objD = (JsonObject) symbol.getData();
                        int sIndex = objD.get("sIndex").getAsInt();
                        if (sIndex == 0 || sIndex == LASTINDEXOFSELECTEDPOLYGON) {
                            LNGLATEDIT.get(LAYERINDEX).set(0, symbol.getLatLng());
                            LNGLATEDIT.get(LAYERINDEX).set(LASTINDEXOFSELECTEDPOLYGON, symbol.getLatLng());
                        } else
                            LNGLATEDIT.get(LAYERINDEX).set(sIndex, symbol.getLatLng());
                        Log.e(TAG, "onAnnotationDrag: size : " + LNGLATEDIT.get(LAYERINDEX).size());
                        redrawFarms();
                        if (LNGLATEDIT.get(LAYERINDEX).size() > 7) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(TAG, "run: after delay");
//                                redrawFarms();
                                }
                            }, 150);
                        } else {
                            Log.e(TAG, "run: without delay");
                            redrawFarms();
                        }
                        if (ISINTERSECT == 1) {
                            saveEditBtn.setVisibility(View.GONE);
                        } else {
//                    messageBox.setText(getResources().getString(R.string.complete_marking_save_polygon));
                            saveEditBtn.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onAnnotationDragFinished(Symbol symbol) {
                    strsubmit = "{\"lat\":" + "\"" + previouscoord.getLatitude() + "\"" + ",\"long\":" + "\"" + previouscoord.getLongitude() + "\"" + "},\"currentCoordinates\":{\"lat\":" + "\"" + symbol.getLatLng().getLatitude() + "\"" + ",\"long\":" + "\"" + symbol.getLatLng().getLongitude() + "\"" + "}";
                    COMACT.segmentEvents(getActivity(), "Point edit",
                            "User moved a point by dragging it", MAPBOXMAP, strsubmit, "DRAG_APOINT_TOEDIT");
                }
            });

        }
        SYMBOLSET.add(symbolManagers);
        if (LNGLATEDIT.get(idx).size() > 0) {
            LASTINDEXOFSELECTEDPOLYGON = LNGLATEDIT.get(idx).size() - 1;
        }
    }

    private void showAllLayers() {
        MAPBOXMAP.getStyle(style -> {
            for (int i = 0; i < LNGLATEDIT.size(); i++) {
                Layer lineLayer = style.getLayer("lineLayerID" + i);
                Layer polyLayer = style.getLayer("polyLayerID" + i);
                if (lineLayer != null && polyLayer != null) {
                    lineLayer.setProperties(PropertyFactory.lineOpacity(1.0f));
                    polyLayer.setProperties(PropertyFactory.fillOpacity(0.6f));
                }
            }
        });
    }

    private void deleteSymbol() {
        if (activeSymbolIndex == 0) {
            Toast.makeText(getContext(), "can't delete 1st point", Toast.LENGTH_SHORT).show();
            return;
        }
        MAPBOXMAP.getStyle(style -> {
            Log.e(TAG, "deleteSymbol: LATLNGEDIT : " + LNGLATEDIT.size());

            style.removeLayer("polyLayerID" + FINALFOUNDIDX);
            style.removeLayer("lineLayerID" + FINALFOUNDIDX);
            style.removeSource("polySourceID" + FINALFOUNDIDX);
            style.removeSource("lineSourceID" + FINALFOUNDIDX);
            for (SymbolManager sm:
                    SYMBOLSET.get(FINALFOUNDIDX)) {
                sm.deleteAll();
            }
//            SYMBOLSET.get(FINALFOUNDIDX).deleteAll();
            SYMBOLSET = new ArrayList<>();
            SYMBOLACTIVE = null;
            LNGLATEDIT.get(FINALFOUNDIDX).remove(activeSymbolIndex);
            trashBtn.setVisibility(View.GONE);
            if (LNGLATEDIT.size() > 0) {
                //  for (int i = 0; i < LNGLATEDIT.size(); i++) {
                int i = FINALFOUNDIDX;
                List<LatLng> ll = new ArrayList<>();
                List<Point> llPts = new ArrayList<>();
                for (int j = 0; j < LNGLATEDIT.get(i).size(); j++) {
                    double lat = LNGLATEDIT.get(i).get(j).getLatitude();
                    double lng = LNGLATEDIT.get(i).get(j).getLongitude();
                    llPts.add(Point.fromLngLat(lng, lat));
                    ll.add(new LatLng(lat, lng));
                }
                LNGLAT.add(ll);
                LATLNGARR.add(llPts);
//                    //   PIDX += 1;
                COMACT.drawMapLayers(style, llPts, String.valueOf(i), "update");
//                    Log.e(TAG, "onMapReady: layer id : " + style.getLayer("polyLayerID" + i).toString());
                int middlepolyposition = LNGLAT.get(i).size() / 2;
//                    if (KawaMap.isFormEnable)
//                        drawSymbolsforPolygon(middlepolyposition, i);
                drawSymbol(style, i, 1f);
                messageBox.setText(getResources().getString(R.string.select_point_toedit));
                messageBox.setBackgroundColor(KawaMap.headerBgColor);
//                    Feature multiPointFeature = Feature.fromGeometry(Polygon.fromLngLats(LATLNGARR));
//                    multiPointFeature.addStringProperty("area", String.valueOf(POLYGONAREA.get(i)));
//                    BEFOREEDITLISTFEATURE.add(multiPointFeature.toJson());
                //}
            }

        });


    }

    private void showHideSymbol(String type, boolean discard) {
        if (SYMBOLSET.size() > 0) {
            // Log.e("SYMBOLSET", String.valueOf(SYMBOLSET.size()));
            for (int i = 0; i < SYMBOLSET.size(); i++) {
                List<SymbolManager> symbolManagers = SYMBOLSET.get(i);
//                SymbolManager sm = SYMBOLSET.get(i);
                for (SymbolManager sm:
                        symbolManagers) {
                    if (sm != null) {
                        // Log.e("SymbolManager", "not null");
                        float opacity = 0.0f;
                        if (LAYERINDEX == i && type.equals("show"))
                            opacity = 1.0f;

                        LongSparseArray<Symbol> annotations = sm.getAnnotations();
                        if (!annotations.isEmpty()) {
                            if (annotations.size() > 0) {
                                for (int j = 0; j < annotations.size(); j++) {
                                    Symbol symbol = annotations.get(j);
                                    if (discard && LAYERINDEX == i) {
//                                    JsonObject symbolData = (JsonObject) symbol.getData();
//                                    Integer sIndex = symbolData.get("sIndex").getAsInt();
//                                    Integer lIndex = symbolData.get("lIndex").getAsInt();
                                        LatLng latLng = LNGLATEDIT.get(i).get(j);
                                        if (latLng != null)
                                            setSymbolLL(symbol, latLng);
                                    }
                                    JsonObject objD = (JsonObject) symbol.getData();
                                    int sIndex = objD.get("sIndex").getAsInt();

                                    if (opacity > 0 && symbol.getIconOpacity() <= 0 || opacity <= 0 && symbol.getIconOpacity() > 0) {
                                        symbol.setIconOpacity(opacity);
                                        if (sIndex == 0)
                                            symbol.setIconImage("symbol_active");
                                        else
                                            symbol.setIconImage("symbol_blue");
                                        symbol.setIconSize(0.5f);
                                        sm.update(symbol);
                                    }
                                }
                            }
                        }

                    }
                }

            }
        }
    }

    private void moveSymbol(String direction) {
        if (SYMBOLACTIVE != null) {
            final PointF pointF = MAPBOXMAP.getProjection().toScreenLocation(SYMBOLACTIVE.getLatLng());
            LatLng prevlatlng = SYMBOLACTIVE.getLatLng();
            final int moveBy = 5;
            float newX = pointF.x;
            float newY = pointF.y;

            switch (direction) {
                case "UP":
                    newY = newY - moveBy;

                    break;
                case "DOWN":
                    newY = newY + moveBy;
                    break;
                case "LEFT":
                    newX = newX - moveBy;
                    break;
                case "RIGHT":
                    newX = newX + moveBy;
                    break;
            }

            pointF.set(newX, newY);
            LatLng newLL = MAPBOXMAP.getProjection().fromScreenLocation(pointF);
            Log.e(TAG, "moveSymbol:" + direction + " newLL " + newLL);
            setSymbolLL(SYMBOLACTIVE, newLL);
            redrawFarms();
            String strsubmit = "{\"lat\":" + "\"" + prevlatlng.getLatitude() + "\"" + ",\"long\":" + "\"" + prevlatlng.getLongitude() + "\"" + "},\"currentCoordinates\":{\"lat\":" + "\"" + newLL.getLatitude() + "\"" + ",\"long\":" + "\"" + newLL.getLongitude() + "\"" + "},\"arrow\":\"" + direction + "\"";
            COMACT.segmentEvents(getActivity(), "Joystick edit",
                    "User moved a point by joystick", MAPBOXMAP, strsubmit, "JOYSTICK_APOINT_TOEDIT");
        }
    }

    private void setSymbolLL(Symbol symbol, LatLng latLng) {
        JsonObject objD = (JsonObject) symbol.getData();
        Integer sIndex = objD.get("sIndex").getAsInt();
        if (sIndex == 0 || sIndex == LASTINDEXOFSELECTEDPOLYGON) {
            LNGLATEDIT.get(LAYERINDEX).set(0, symbol.getLatLng());
            LNGLATEDIT.get(LAYERINDEX).set(LASTINDEXOFSELECTEDPOLYGON, symbol.getLatLng());
        } else
            LNGLATEDIT.get(LAYERINDEX).set(sIndex, symbol.getLatLng());
        symbol.setLatLng(latLng);
        SYMBOLSET.get(LAYERINDEX).get(activeSymbolIndex).update(symbol);
    }

    private void redrawFarms() {
        if (LNGLATEDIT.get(LAYERINDEX) != null) {
            MAPBOXMAP.getStyle(style -> {
                List<Point> llPts = new ArrayList<>();
                List<List<Point>> llPtsA = new ArrayList<>();
                for (int j = 0; j < LNGLATEDIT.get(LAYERINDEX).size(); j++) {
                    llPts.add(Point.fromLngLat(LNGLATEDIT.get(LAYERINDEX).get(j).getLongitude(), LNGLATEDIT.get(LAYERINDEX).get(j).getLatitude()));
                }
                llPtsA.add(llPts);
                GeoJsonSource lineSourceID = style.getSourceAs("lineSourceID" + LAYERINDEX);
                GeoJsonSource polySourceID = style.getSourceAs("polySourceID" + LAYERINDEX);
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

    private void initButtons() {
        correctBoundryBtn = VIEW.findViewById(R.id.correctBoundryBtn);
        addMoreBtn = VIEW.findViewById(R.id.addMoreBtn);
        saveEditBtn = VIEW.findViewById(R.id.saveEditBtn);
        completeMarkingBtn = VIEW.findViewById(R.id.completeMarkingBtn);
        saveDetailBtn = VIEW.findViewById(R.id.saveDetailBtn);
        addDetailBtn = VIEW.findViewById(R.id.addDetailBtn);
        saveDetailnNextBtn = VIEW.findViewById(R.id.saveDetailnNextBtn);
        discardEditBtn = VIEW.findViewById(R.id.discardEditBtn);
        startOverBtn = VIEW.findViewById(R.id.startOverBtn);
        backBtn = VIEW.findViewById(R.id.backBtn);
        upBtn = VIEW.findViewById(R.id.upBtn);
        downBtn = VIEW.findViewById(R.id.downBtn);
        leftBtn = VIEW.findViewById(R.id.leftBtn);
        rightBtn = VIEW.findViewById(R.id.rightBtn);
        zoomInBtn = VIEW.findViewById(R.id.zoomInBtn);
        zoomOutBtn = VIEW.findViewById(R.id.zoomOutBtn);
        trashBtn = VIEW.findViewById(R.id.trashBtn);
        markAnotherBtn = VIEW.findViewById(R.id.markAnotherBtn);
        exitBtn = VIEW.findViewById(R.id.exitBtn);
        detailsForm = VIEW.findViewById(R.id.detailsForm);
        thankyouLinearLayout = VIEW.findViewById(R.id.thankyouLinearLayout);
        farmDetailsLayout = VIEW.findViewById(R.id.farmDetailsLayout);
        anotherndExitLayout = VIEW.findViewById(R.id.anotherndExitLayout);
        totalAreaTv = VIEW.findViewById(R.id.totalAreaTv);
        totalseedsTv = VIEW.findViewById(R.id.totalseedsTv);
        addressTv = VIEW.findViewById(R.id.addressTv);
        completeMarkingBtn.setOnClickListener(viewV -> completeMarking());
        correctBoundryBtn.setOnClickListener(viewV -> correctBoundry());
        saveEditBtn.setOnClickListener(viewV -> saveEdits());
        saveDetailBtn.setOnClickListener(viewV -> saveDetail(viewV));
        addDetailBtn.setOnClickListener(viewV -> addFormDetail());
        saveDetailnNextBtn.setOnClickListener(viewV -> saveNGoNext());
        upBtn.setOnClickListener(viewV -> moveSymbol("UP"));
        downBtn.setOnClickListener(viewV -> moveSymbol("DOWN"));
        leftBtn.setOnClickListener(viewV -> moveSymbol("LEFT"));
        rightBtn.setOnClickListener(viewV -> moveSymbol("RIGHT"));
        zoomInBtn.setOnClickListener(viewV -> COMACT.setZoomLevel(getActivity(), 1, MAPBOXMAP));
        zoomOutBtn.setOnClickListener(viewV -> COMACT.setZoomLevel(getActivity(), -1, MAPBOXMAP));
        trashBtn.setOnClickListener(viewV -> deleteSymbol());
        addMoreBtn.setOnClickListener(viewV -> onBackPressed());
        backBtn.setOnClickListener(viewV -> onBackPressed());
        discardEditBtn.setOnClickListener(viewV -> discardEditNew());
        startOverBtn.setOnClickListener(view1 -> startOver("Start_over"));
        markAnotherBtn.setOnClickListener(view1 -> startOver("Mark_another"));
        exitBtn.setOnClickListener(view1 -> exitFunction());

        dootedLineFirst = VIEW.findViewById(R.id.dootedLineFirst);
        dootedLineSecond = VIEW.findViewById(R.id.dootedLineSecond);
        seedsLayout = VIEW.findViewById(R.id.seedsLayout);
        areaLayout = VIEW.findViewById(R.id.areaLayout);
        locationLayout = VIEW.findViewById(R.id.locationLayout);

        upBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mHandler.postDelayed(joyesticRunnable, 0); // initial call for our handler.
                return true;
            }
        });
        downBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mHandler.postDelayed(joyesticRunnable, 0); // initial call for our handler.
                return true;
            }
        });
        leftBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mHandler.postDelayed(joyesticRunnable, 0); // initial call for our handler.
                return true;
            }
        });
        rightBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mHandler.postDelayed(joyesticRunnable, 0); // initial call for our handler.
                return true;
            }
        });

        Button[] footerButtons = null;
        footerButtons = new Button[]{
                saveEditBtn,
                completeMarkingBtn,
                saveDetailBtn,
                markAnotherBtn,
                addDetailBtn,
                saveDetailnNextBtn
        };
        KawaMap.setFooterButtonColor(footerButtons);
        int orangeColor = Color.parseColor("#FD6035");
        exitBtn.setBackgroundColor(orangeColor);
        Button[] innerButtons = null;
        innerButtons = new Button[]{
                discardEditBtn,
                startOverBtn,
                backBtn,
                addMoreBtn,
                correctBoundryBtn,
        };
//        KawaMap.setInnerButtonColor(innerButtons);
        KawaMap.setInnerButtonColor(getActivity(), innerButtons);

    }

    private Runnable joyesticRunnable = new Runnable() {
        @Override
        public void run() {
            if (upBtn.isPressed()) { // check if the button is still in its pressed state
                moveSymbol("UP");
                mHandler.postDelayed(joyesticRunnable, 100); // call for a delayed re-check of the button's state through our handler. The delay of 100ms can be changed as needed.
            }
            if (downBtn.isPressed()) { // check if the button is still in its pressed state
                moveSymbol("DOWN");
                mHandler.postDelayed(joyesticRunnable, 100); // call for a delayed re-check of the button's state through our handler. The delay of 100ms can be changed as needed.
            }
            if (leftBtn.isPressed()) { // check if the button is still in its pressed state
                moveSymbol("LEFT");
                mHandler.postDelayed(joyesticRunnable, 100); // call for a delayed re-check of the button's state through our handler. The delay of 100ms can be changed as needed.
            }
            if (rightBtn.isPressed()) { // check if the button is still in its pressed state
                moveSymbol("RIGHT");
                mHandler.postDelayed(joyesticRunnable, 100); // call for a delayed re-check of the button's state through our handler. The delay of 100ms can be changed as needed.
            }

        }
    };

    private void hideAllBtn() {
        // Log.e("Called", "hideAllBtn");
        correctBoundryBtn.setVisibility(VIEW.GONE);
        addMoreBtn.setVisibility(VIEW.GONE);
        backBtn.setVisibility(VIEW.GONE);
        discardEditBtn.setVisibility(VIEW.GONE);
        anotherndExitLayout.setVisibility(View.GONE);
        upBtn.setVisibility(VIEW.GONE);
        downBtn.setVisibility(VIEW.GONE);
        leftBtn.setVisibility(VIEW.GONE);
        rightBtn.setVisibility(VIEW.GONE);
        zoomInBtn.setVisibility(VIEW.GONE);
        zoomOutBtn.setVisibility(VIEW.GONE);
        trashBtn.setVisibility(VIEW.GONE);
        completeMarkingBtn.setVisibility(VIEW.GONE);
        saveDetailBtn.setVisibility(VIEW.GONE);
        saveEditBtn.setVisibility(VIEW.GONE);
        addMoreBtn.setVisibility(View.GONE);
        saveDetailnNextBtn.setVisibility(View.GONE);
    }

    private void onFarmsLoaded() {
        // Log.e("Called", "onFarmsLoaded");
        hideAllBtn();
        if (KawaMap.isDrawEnable) {
            completeMarking();
        } else {
            addMoreBtn.setVisibility(VIEW.VISIBLE);
            startOverBtn.setVisibility(VIEW.VISIBLE);
            if (KawaMap.isEditEnable) {
                correctBoundryBtn.setVisibility(VIEW.VISIBLE);
                completeMarkingBtn.setVisibility(VIEW.VISIBLE);
            } else if (KawaMap.isFormEnable) {
                addDetailBtn.setVisibility(VIEW.VISIBLE);
            } else {
                saveDetailBtn.setVisibility(VIEW.VISIBLE);
            }
            EDITON = false;
            SYMBOLACTIVE = null;
            LAYERINDEX = -1;
            messageBox.setText(getResources().getString(R.string.tap_correct_boundery));
        }
//        messageBox.setVisibility(View.GONE);
    }

    private void correctBoundry() {
        String strSubmit = "{\"type\":\"FeatureCollection\", \"features\":" + BEFOREEDITLISTFEATURE + "}";
        COMACT.segmentEvents(getActivity(), "Edit Farm Boundary",
                "User clicked on edit farm boundary", MAPBOXMAP, strSubmit, "EDIT_FARM_BOUNDARY");
        hideAllBtn();
        backBtn.setVisibility(VIEW.VISIBLE);
        EDITON = true;
        SYMBOLACTIVE = null;
        LAYERINDEX = -1;
        messageBox.setText(getResources().getString(R.string.select_plot_toedit));
    }

    private void onFarmSelected(int selectedindex) {
        List<List<Point>> selectedlatlngarr = new ArrayList<>();
        List<String> selectedlistfeature = new ArrayList<>();

        if (LNGLATEDIT.get(selectedindex).size() > 0) {
            List<Point> llPts = new ArrayList<>();
            List<List<Point>> llPtsA = new ArrayList<>();
            for (int j = 0; j < LNGLATEDIT.get(selectedindex).size(); j++) {
                // selectedpolygonlatlng.add(LNGLATEDIT.get(selectedindex));
                double lat = LNGLATEDIT.get(selectedindex).get(j).getLatitude();
                double lng = LNGLATEDIT.get(selectedindex).get(j).getLongitude();
                llPts.add(Point.fromLngLat(lng, lat));
            }
            selectedlatlngarr.add(llPts);
            Feature multiPointFeature = Feature.fromGeometry(Polygon.fromLngLats(selectedlatlngarr));
            multiPointFeature.addStringProperty("area", String.valueOf(POLYGONAREA.get(selectedindex)));
            selectedlistfeature.add(multiPointFeature.toJson());
            String strSubmit = "{\"type\":\"FeatureCollection\", \"features\":" + selectedlistfeature + "}";
            COMACT.segmentEvents(getActivity(), "Farm selected for editing",
                    "User has selected a farm to edit,", MAPBOXMAP, strSubmit, "SINGLE_FARM_SELECTED");
            changeBorderColor("#F7F14A", selectedindex, 0f);

        }
        hideAllBtn();
        //correctBoundryBtn.setVisibility(VIEW.VISIBLE);
        zoomInBtn.setVisibility(VIEW.VISIBLE);
        zoomOutBtn.setVisibility(VIEW.VISIBLE);
        //saveEditBtn.setVisibility(VIEW.VISIBLE);
        discardEditBtn.setVisibility(VIEW.VISIBLE);
        //saveEditBtn.setEnabled(true);
        //EDITON = true;
        messageBox.setText(getResources().getString(R.string.select_point_toedit));
    }

    private void onSymbolSelected() {
        Log.e("Called", "onSymbolSelected : activeSymboleIndex : " + activeSymbolIndex);
        if(LAYERINDEX !=-1) {
            hideAllBtn();
            zoomInBtn.setVisibility(VIEW.VISIBLE);
            zoomOutBtn.setVisibility(VIEW.VISIBLE);
            upBtn.setVisibility(VIEW.VISIBLE);
            downBtn.setVisibility(VIEW.VISIBLE);
            leftBtn.setVisibility(VIEW.VISIBLE);
            rightBtn.setVisibility(VIEW.VISIBLE);
            discardEditBtn.setVisibility(VIEW.VISIBLE);
            saveEditBtn.setVisibility(VIEW.VISIBLE);
            if (LNGLATEDIT.get(FINALFOUNDIDX).size() > 5) {
                trashBtn.setVisibility(View.VISIBLE);
            }
            EDITON = true;
            messageBox.setText(getResources().getString(R.string.drag_point_joystic));
        }
    }

    private void discardEditNew(){
        MAPBOXMAP.getStyle( style -> {
            Log.e(TAG, "deleteSymbol: LATLNGEDIT : " + LNGLATEDIT.size());

            style.removeLayer("polyLayerID" + FINALFOUNDIDX);
            style.removeLayer("lineLayerID" + FINALFOUNDIDX);
            style.removeSource("polySourceID" + FINALFOUNDIDX);
            style.removeSource("lineSourceID" + FINALFOUNDIDX);

            LNGLATEDIT.clear();
            LNGLATEDIT = new ArrayList<>();

//            SYMBOLSET.get(FINALFOUNDIDX).deleteAll();
            for (SymbolManager sm:
                    SYMBOLSET.get(FINALFOUNDIDX)) {
                sm.deleteAll();
            }
            SYMBOLSET = new ArrayList<>();
            if (LNGLAT.size() > 0) {
                //  for (int i = 0; i < LNGLATEDIT.size(); i++) {
                List<LatLng> ll = new ArrayList<>();
                List<Point> llPts = new ArrayList<>();
                for (int j = 0; j < LNGLAT.get(FINALFOUNDIDX).size(); j++) {
                    double lat = LNGLAT.get(FINALFOUNDIDX).get(j).getLatitude();
                    double lng = LNGLAT.get(FINALFOUNDIDX).get(j).getLongitude();
                    llPts.add(Point.fromLngLat(lng, lat));
                    ll.add(new LatLng(lat, lng));
                }

                LNGLATEDIT.add(ll);
                LATLNGARR.add(llPts);
                COMACT.drawMapLayers(style, llPts, String.valueOf(FINALFOUNDIDX), "edit");
                drawSymbol(style, FINALFOUNDIDX,0.0f);

                showAllLayers();
                onFarmsLoaded();
            }

        });
    }

    private void discardEdit() {
        LNGLATEDIT.clear();
        LNGLATEDIT = new ArrayList<>();

        for (int i = 0; i < LNGLAT.size(); i++) {
            List<LatLng> ll = new ArrayList<>();
            for (int j = 0; j < LNGLAT.get(i).size(); j++) {
                double lat = LNGLAT.get(i).get(j).getLatitude();
                double lng = LNGLAT.get(i).get(j).getLongitude();
                ll.add(new LatLng(lat, lng));
            }
            LNGLATEDIT.add(ll);
        }

        redrawFarms();
        showHideSymbol("hide", true);
        showAllLayers();
        onFarmsLoaded();
        Log.e(TAG, "discardEdit:" + FINALFOUNDIDX);
        Layer lineLayer = MAPBOXMAP.getStyle().getLayer("lineLayerID" + FINALFOUNDIDX);
        lineLayer.setProperties(PropertyFactory.lineWidth(0.6f));
        changeBorderColor("#000000", FINALFOUNDIDX, 0.6f);
        COMACT.segmentEvents(getActivity(), "Discard edits",
                "User clicks on Discard edits", MAPBOXMAP, "discaard", "DISCARD");
    }

    private void saveEdits() {
        List<String> aftereditlistfeature = new ArrayList<>();
        List<List<Point>> aftfereditlatlng = new ArrayList<>();

        List<Point> llPts = new ArrayList<>();
        for (int j = 0; j < LNGLATEDIT.get(FINALFOUNDIDX).size(); j++) {
            double lat = LNGLATEDIT.get(FINALFOUNDIDX).get(j).getLatitude();
            double lng = LNGLATEDIT.get(FINALFOUNDIDX).get(j).getLongitude();
            llPts.add(Point.fromLngLat(lng, lat));
        }
        aftfereditlatlng.add(llPts);

        Feature multiPointFeature = Feature.fromGeometry(Polygon.fromLngLats(aftfereditlatlng));
        multiPointFeature.addStringProperty("area", String.valueOf(POLYGONAREA.get(FINALFOUNDIDX)));
        aftereditlistfeature.add(multiPointFeature.toJson());
        Layer lineLayer = MAPBOXMAP.getStyle().getLayer("lineLayerID" + FINALFOUNDIDX);
        lineLayer.setProperties(PropertyFactory.lineWidth(0.6f));
        changeBorderColor("#000000", FINALFOUNDIDX, 0.6f);
        String strJsonWrite = "{\"type\":\"FeatureCollection\", \"features\":" + aftereditlistfeature + "}";
        COMACT.segmentEvents(getActivity(), "Save edited boundary",
                "User has Save edited boundary", MAPBOXMAP, strJsonWrite, "SAVE_EDITED_FARM");


        hideAllBtn();
        //addMoreBtn.setVisibility(VIEW.VISIBLE);
        correctBoundryBtn.setVisibility(VIEW.VISIBLE);
        completeMarkingBtn.setVisibility(VIEW.VISIBLE);
        EDITON = false;
        showHideSymbol("hide", false);
        showAllLayers();
        onFarmsLoaded();
        addMoreBtn.setVisibility(VIEW.GONE);
    }

    private void completeMarking() {
//        String strMerge = "{\"farms_fetched_at\":" + "\"" + COMACT.FARMS_FETCHED_AT + "\"" + ",\"recipe_id\":\"farm_boundaries\",\"aois\":" + String.valueOf(LNGLATEDIT) + "}";
//        JsonObject selectedFarms = JsonParser.parseString(strMerge).getAsJsonObject();
//        interfaceKawaEvents.onkawaSelect(selectedFarms);

        hideAllBtn();
        //backBtn.setVisibility(VIEW.VISIBLE);
        messageBox.setVisibility(View.GONE);
        if (KawaMap.isFormEnable)
            addDetailBtn.setVisibility(VIEW.VISIBLE);
        else
            saveDetailBtn.setVisibility(VIEW.VISIBLE);
        EDITON = false;
//        if(KawaMap.isFormEnable){
//            createformsfileds();
//        }

    }

    private void addFormDetail() {
        TOTALPOLYGON = LNGLATEDIT.size();
        int middlepolyposition = LNGLATEDIT.get(0).size() / 2;

        moveCameratoPosition(0, middlepolyposition);
        CURRENTPOLYGON = 1;
        addMoreBtn.setVisibility(View.GONE);
        startOverBtn.setVisibility(View.GONE);
        addDetailBtn.setVisibility(View.GONE);
        if (TOTALPOLYGON > 1)
            saveDetailnNextBtn.setVisibility(View.VISIBLE);
        else {
            saveDetailBtn.setVisibility(VIEW.VISIBLE);
            saveDetailnNextBtn.setVisibility(View.GONE);
        }

        createformsfileds(1);
    }

    private void saveNGoNext() {
        saveFormsDetails();
        if (FORMVALIDATE && KawaMap.isFormEnable) {
            int middlepolyposition = LNGLATEDIT.get(CURRENTPOLYGON).size() / 2;
            moveCameratoPosition(CURRENTPOLYGON, middlepolyposition);
            CURRENTPOLYGON = CURRENTPOLYGON + 1;
//        drawSymbolsforPolygon(middlepolyposition, CURRENTPOLYGON - 1);
            if (TOTALPOLYGON > CURRENTPOLYGON)
                saveDetailnNextBtn.setVisibility(View.VISIBLE);
            else {
                saveDetailBtn.setVisibility(VIEW.VISIBLE);
                saveDetailnNextBtn.setVisibility(View.GONE);
            }
            createformsfileds(CURRENTPOLYGON);
        } else {
            saveDetailnNextBtn.setVisibility(View.VISIBLE);
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle(getResources().getString(R.string.app_name));
            alertDialog.setMessage(SETERRORMSG);
            alertDialog.setIcon(R.mipmap.ic_launcher);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //saveDetailBtn.setVisibility(View.VISIBLE);

                }
            });
            alertDialog.show();
            return;
        }

    }

    private void saveDetail(View view) {
        COMACT.hideKeyboard(getActivity(), view);
        hideAllBtn();

        List<String> listFeatures = new ArrayList<>();
        for (int i = 0; i < LNGLATEDIT.size(); i++) {
            List<LatLng> ll = new ArrayList<>();
            List<Point> llPts = new ArrayList<>();
            List<List<Point>> llPtsA = new ArrayList<>();
            for (int j = 0; j < LNGLATEDIT.get(i).size(); j++) {
                double lat = LNGLATEDIT.get(i).get(j).getLatitude();
                double lng = LNGLATEDIT.get(i).get(j).getLongitude();
                llPts.add(Point.fromLngLat(lng, lat));
//                ll.add(new LatLng(lat, lng));
            }
//            LNGLAT.add(ll);
            llPtsA.add(llPts);
            if (KawaMap.isFormEnable) {
                saveFormsDetails();
                Feature multiPointFeature = Feature.fromGeometry(Polygon.fromLngLats(llPtsA));
                multiPointFeature.addStringProperty("area", String.valueOf(POLYGONAREA.get(i)));
                String strSubmit = String.valueOf(FILEDSOBJECT);
                JsonObject submitJsonObject1 = JsonParser.parseString(strSubmit).getAsJsonObject();
                multiPointFeature.addProperty("farmDetails", submitJsonObject1);
                listFeatures.add(multiPointFeature.toJson());
            } else {
                Feature multiPointFeature = Feature.fromGeometry(Polygon.fromLngLats(llPtsA));
                multiPointFeature.addStringProperty("area", String.valueOf(POLYGONAREA.get(i)));
                listFeatures.add(multiPointFeature.toJson());
            }
        }
        Log.e(TAG, "FORMVALIDATE: " + FORMVALIDATE + KawaMap.isFormEnable);
        if (FORMVALIDATE || !KawaMap.isFormEnable) {
            Log.e(TAG, "listFeatures:>> " + listFeatures);
            String strSubmit = "{\"farms_fetched_at\":" + "\"" + COMACT.FARMS_FETCHED_AT + "\"" + ",\"recipe_id\":\"farm_boundaries\",\"aois\":" + String.valueOf(listFeatures) + "}";
            JsonObject submitJsonObject = JsonParser.parseString(strSubmit).getAsJsonObject();
            String strJsonWrite = "{\"type\":\"FeatureCollection\", \"features\":" + listFeatures + "}";
            detailsForm.setVisibility(View.GONE);
            COMACT.showLoader(getActivity(), "isCircle");

            ServiceManager.getInstance().getKawaService().sumbitPoints(KawaMap.KAWA_API_KEY, COMACT.SDK_VERSION, submitJsonObject).enqueue(new Callback<MergeModel>() {
                @Override
                public void onResponse(@NonNull Call<MergeModel> call, @NonNull Response<MergeModel> response) {
                    COMACT.hideLoader();
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                // Log.e("TAG", "onResponse: " + response.body().getStatus());
                                // String strJsonWrite = String.valueOf(new Gson().toJson(response.body()));
                                startOverBtn.setVisibility(View.GONE);
                                anotherndExitLayout.setVisibility(View.VISIBLE);
                                interfaceKawaEvents.onkawaSubmit(strJsonWrite);
                                COMACT.segmentEvents(getActivity(), "Save Details",
                                        "Data saved on save details", MAPBOXMAP, strJsonWrite, "SAVE_DETAILS");
                                //thankyouLinearLayout.setVisibility(VIEW.VISIBLE);
                                //farmDetailsLayout.setVisibility(VIEW.VISIBLE);
                                //interfaceKawaEvents.onkawaSubmit(strJsonWrite);
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
                public void onFailure(@NonNull Call<MergeModel> call, @NonNull Throwable t) {
                    COMACT.hideLoader();
                    String errorBody = t.getMessage();
                    Toast.makeText(getApplicationContext(), errorBody, Toast.LENGTH_LONG).show();
                }

            });
            if (KawaMap.isFarmDetailsEnable) {
                designFarmDetails();
            }
        } else {
            saveDetailBtn.setVisibility(View.VISIBLE);
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle(getResources().getString(R.string.app_name));
            alertDialog.setMessage(SETERRORMSG);
            alertDialog.setIcon(R.mipmap.ic_launcher);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //saveDetailBtn.setVisibility(View.VISIBLE);

                }
            });
            alertDialog.show();
            return;
        }
    }

    public void saveFormsDetails() {
        FILEDSOBJECT = new JSONObject();
        FILEDSARRAY = new JSONArray();
        FORMVALIDATE = true;
        SETERRORMSG = "";
        for (int i = 0; i < farm_fields_array.length(); i++) {
            try {
                JSONObject jsonObject = farm_fields_array.getJSONObject(i);
                String field_type = jsonObject.getString("field_type");
                String field_placeholder = jsonObject.getString("field_placeholder");
                boolean is_required = jsonObject.getBoolean("is_required");
                if (field_type.equals("text") || field_type.equals("textarea") || field_type.equals("date")) {
                    if (is_required)
                        if (myTextViews[i].getText().toString().trim().isEmpty()) {
                            SETERRORMSG += "\n" + field_placeholder + " is required field";
                            FORMVALIDATE = false;
                        }
                    if (field_type.equals("date")) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                        Date objDate = dateFormat.parse(myTextViews[i].getText().toString());
                        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
                        String updatedDateFormat = dateFormat2.format(objDate);
                        Log.d("Date Format:", "Final Date:" + updatedDateFormat);
                        FILEDSOBJECT.put(String.valueOf(myTextViews[i].getTag()), updatedDateFormat);
                    } else
                        FILEDSOBJECT.put(String.valueOf(myTextViews[i].getTag()), myTextViews[i].getText().toString());
                }
                if (field_type.equals("dropdown"))
                    if (is_required)
                        if (spinnerViewArray[i].getSelectedItem().toString().isEmpty() || spinnerViewArray[i].getSelectedItem().toString().equals("Select " + field_placeholder)) {
                            SETERRORMSG += "\n" + field_placeholder + " is required field";
                            FORMVALIDATE = false;
                        } else
                            FILEDSOBJECT.put(String.valueOf(spinnerViewArray[i].getTag()), spinnerViewArray[i].getSelectedItem().toString());
            } catch (Exception e) {
            }
        }
    }

    public void drawSymbolsforPolygon(int middlepolyposition, int polygonno) {
        List<LatLng> latlng = LNGLATEDIT.get(polygonno);
        List<Point> llPts = new ArrayList<>();
        double totallat = 0, totallng = 0;
        for (int i = 0; i < latlng.size(); i++) {
            llPts.add(Point.fromLngLat(latlng.get(i).getLongitude(), latlng.get(i).getLatitude()));
            totallat = totallat + latlng.get(i).getLatitude();
            totallng = totallng + latlng.get(i).getLongitude();
        }
        double x = totallat / latlng.size();
        double y = totallng / latlng.size();

        Style style = MAPBOXMAP.getStyle();
        SymbolManager symbolManager = new SymbolManager(MAPVIEW, MAPBOXMAP, style);
        symbolManager.setIconAllowOverlap(true);
        symbolManager.setTextAllowOverlap(true);
        NOSYMBOLSET.add(symbolManager);

        style.addImage("psymbol_blue", BitmapFactory.decodeResource(this.getResources(), R.drawable.symbol_blue));
        style.addImage("symbol_active", BitmapFactory.decodeResource(this.getResources(), R.drawable.symbol_activeb));

        if (LNGLATEDIT.get(polygonno).size() > 0) {
            JsonObject objD = new JsonObject();

            objD.addProperty("polygonIndex", middlepolyposition);
            SymbolOptions sampleSymbolOptions = new SymbolOptions()
                    .withLatLng(new LatLng(x, y))
                    .withIconImage("psymbol_blue")
                    .withIconSize(0.5f)
                    .withTextColor("#fff")
                    .withTextField(String.valueOf(polygonno + 1));
            symbolManager.create(sampleSymbolOptions);
        }
    }


    public void moveCameratoPosition(int polygonno, int middlepolyposition) {
        LatLng latLng = new LatLng(LNGLATEDIT.get(polygonno).get(middlepolyposition).getLatitude(), LNGLATEDIT.get(polygonno).get(middlepolyposition).getLongitude());
        MAPBOXMAP.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(latLng).zoom(COMACT.MAPZOOM).build()), 1000);
//        MAPBOXMAP.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 20,20,20,100), 1000);

    }
     /*

     public void moveCameratoPosition(int polygonno, int middlepolyposition) {
        double polygoncenterlat,polygoncenterlng;
        List<LatLng> latlng = LNGLATEDIT.get(polygonno - 1);
        List<Point> llPts = new ArrayList<>();
        for (int i = 0; i < latlng.size(); i++) {
            llPts.add(Point.fromLngLat(latlng.get(i).getLongitude(), latlng.get(i).getLatitude()));
        }
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(llPts.get(0).latitude(), llPts.get(0).longitude()))
                .include(new LatLng(llPts.get(middlepolyposition).latitude(), llPts.get(middlepolyposition).longitude()))
                .build();

        polygoncenterlat =  latLngBounds.getCenter().getLatitude();
        polygoncenterlng =  latLngBounds.getCenter().getLongitude();

        LatLng latLng = new LatLng(polygoncenterlat, polygoncenterlng);
        MAPBOXMAP.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(latLng).zoom(COMACT.MAPZOOM).build()), 1000);
//        MAPBOXMAP.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 20,20,20,100), 1000);

    }
     */

    public void designFarmDetails() {
        float totalArea = 0.0f;
        for (int i = 0; i < POLYGONAREA.size(); i++) {
            totalArea = totalArea + ((float) POLYGONAREA.get(i));
        }
        if (KawaMap.isOtherFarmDetailsEnable) {
            float hectares = (float) (totalArea / 2.47105);
            float seedrequired = hectares * 17;
            String hectaresStr = String.format("%.2f", hectares);
            String seedrequiredStr = String.format("%.2f", seedrequired);
            totalAreaTv.setText(hectaresStr + " " + getResources().getString(R.string.hectares));
            totalseedsTv.setText(seedrequiredStr + " KG");
            getAddress();
        } else {
            String areaStr = String.format("%.2f", totalArea);
            totalAreaTv.setText(areaStr + " Acres");
            totalseedsTv.setVisibility(View.GONE);
            addressTv.setVisibility(View.GONE);
            dootedLineFirst.setVisibility(View.GONE);
            dootedLineSecond.setVisibility(View.GONE);
            seedsLayout.setVisibility(View.GONE);
            locationLayout.setVisibility(View.GONE);

        }
        if (KawaMap.isFormEnable && KawaMap.isOtherFarmDetailsEnable == false) {
            areaLayout.setVisibility(View.GONE);
            totalseedsTv.setVisibility(View.GONE);
            addressTv.setVisibility(View.GONE);
            dootedLineFirst.setVisibility(View.GONE);
            dootedLineSecond.setVisibility(View.GONE);
            seedsLayout.setVisibility(View.GONE);
            locationLayout.setVisibility(View.GONE);
            ImageView[] myImageView = new ImageView[TOTALPOLYGON]; // create an empty array;
            TextView[] myAreaTextViews = new TextView[TOTALPOLYGON]; // create an empty array;
            TextView[] myAreaLabel = new TextView[TOTALPOLYGON]; // create an empty array;
            LinearLayout[] myAreaLayout = new LinearLayout[TOTALPOLYGON]; // create an empty array;
            ImageView[] dootedLineView = new ImageView[TOTALPOLYGON]; // create an empty array;
            LinearLayout.LayoutParams editTextLayoutParams = new LinearLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            editTextLayoutParams.setMargins(20, 0, 20, 0);
            for (int i = 0; i < TOTALPOLYGON; i++) {
                final LinearLayout rowLinearLayoutView = new LinearLayout(getActivity());
                rowLinearLayoutView.setPadding(20, 10, 10, 10);
                rowLinearLayoutView.setOrientation(LinearLayout.HORIZONTAL);
                rowLinearLayoutView.setLayoutParams(editTextLayoutParams);

                final ImageView rowImageView = new ImageView(getActivity());
                rowImageView.setPadding(0, 0, 20, 0);
                rowImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_area));

                final ImageView rowDottedImageView = new ImageView(getActivity());
                rowDottedImageView.setPadding(0, 10, 20, 10);
                rowDottedImageView.setImageDrawable(getResources().getDrawable(R.drawable.dottedline));
                rowDottedImageView.setMinimumHeight(40);

                final TextView rowAreaTextView = new TextView(getActivity());
                rowAreaTextView.setTextColor(Color.WHITE);
                rowAreaTextView.setTextSize(14f);
                rowAreaTextView.setLayoutParams(editTextLayoutParams);
//                rowAreaTextView.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
//                rowAreaTextView.setPadding(150, 0, 0, 0);
                Float dtotalArea = Float.valueOf(POLYGONAREA.get(i).toString());
                String areaStr = String.format("%.2f", dtotalArea);
                rowAreaTextView.setText(areaStr + " Acres");
                rowAreaTextView.setGravity(Gravity.END);
                rowAreaTextView.setGravity(Gravity.RIGHT);

                final TextView rowAreaLabelView = new TextView(getActivity());
                rowAreaLabelView.setTextColor(Color.WHITE);
                rowAreaLabelView.setTextSize(14f);
                int polygonno = i + 1;
                rowAreaLabelView.setText("Area of Farm " + polygonno);
                rowLinearLayoutView.addView(rowImageView);
                rowLinearLayoutView.addView(rowAreaLabelView);
                rowLinearLayoutView.addView(rowAreaTextView);
                farmDetailsLayout.addView(rowLinearLayoutView);
//                Log.e(TAG, "TOTALPOLYGON:<<< "+TOTALPOLYGON + "i>>" + i );
                if (i != TOTALPOLYGON - 1)
                    farmDetailsLayout.addView(rowDottedImageView);

                myAreaTextViews[i] = rowAreaTextView;
                myAreaLabel[i] = rowAreaLabelView;
                myAreaLayout[i] = rowLinearLayoutView;
                myImageView[i] = rowImageView;
                dootedLineView[i] = rowDottedImageView;
            }
        }
        startOverBtn.setVisibility(View.GONE);
        messageBox.setVisibility(View.GONE);
        farmDetailsLayout.setVisibility(VIEW.VISIBLE);
        anotherndExitLayout.setVisibility(View.VISIBLE);
        farm_mark_messagebox.setVisibility(View.VISIBLE);

    }

    private void startOver(String eventname) {
        if (eventname.equals("Mark_another")) {
            COMACT.segmentEvents(getActivity(), "Mark another Farm",
                    "User clicked on Mark another Farm", MAPBOXMAP, "", "MARK_ANOTHER_PLOTS");
        } else {
            COMACT.segmentEvents(getActivity(), "Start Over",
                    "user clicked on Start over", MAPBOXMAP, "", "START_OVER");
        }

        fragmentFarmLocation fragmentFarmLocation = new fragmentFarmLocation();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.kawaMapView, fragmentFarmLocation);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void exitFunction() {

        COMACT.segmentEvents(getActivity(), "Exit",
                "User has exited farm boundary", MAPBOXMAP, "exit", "EXIT_SDK");
        COMACT.destroyStaticVariables();
        interfaceKawaEvents.onkawaDestroy();
    }

    private void createformsfileds(int polygon_no) {
        try {
            farm_fields_array = new JSONArray();
            detailsForm.removeAllViews();
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            if (obj != null) {
                farm_fields_array = obj.getJSONArray("farm_fields");

                if (farm_fields_array != null && farm_fields_array.length() > 0) {
                    detailsForm.setVisibility(VIEW.VISIBLE);
                    myTextViews = new EditText[farm_fields_array.length()]; // create an empty array;
                    spinnerViewArray = new Spinner[farm_fields_array.length()]; // create an empty array;

                    TextView titleTextView = FormBuilder.textFiled(getContext(), "Farm " + polygon_no + " Details", 22f, Color.WHITE, "bold");
                    TextView subtitleTextView = FormBuilder.textFiled(getContext(), "Add details for farm " + polygon_no, 14f, Color.parseColor("#C0C0C0"), "normal");

                    detailsForm.addView(titleTextView);
                    detailsForm.addView(subtitleTextView);

                    LinearLayout.LayoutParams editTextLayoutParams = new LinearLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    editTextLayoutParams.setMargins(20, 10, 20, 10);

                    for (int i = 0; i < farm_fields_array.length(); i++) {
                        JSONObject jsonObject = farm_fields_array.getJSONObject(i);
                        String field_type = jsonObject.getString("field_type");
                        if (field_type.equals("text")) {
                            EditText editTextView = FormBuilder.editFiled(getActivity(), jsonObject, editTextLayoutParams);
                            detailsForm.addView(editTextView);
                            myTextViews[i] = editTextView;

                        } else if (field_type.equals("dropdown")) {
                            Spinner spinnerView = FormBuilder.spinnerFiled(getActivity(), jsonObject, editTextLayoutParams);
                            detailsForm.addView(spinnerView);
                            spinnerViewArray[i] = spinnerView;
                        } else if (field_type.equals("date")) {
                            EditText dateTextView = FormBuilder.dateFiled(getActivity(), jsonObject, editTextLayoutParams);
                            detailsForm.addView(dateTextView);
                            myTextViews[i] = dateTextView;
                        } else if (field_type.equals("textarea")) {
                            EditText textareaView = FormBuilder.textArea(getActivity(), jsonObject, editTextLayoutParams);
                            detailsForm.addView(textareaView);
                            myTextViews[i] = textareaView;
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "Invalid JSON", Toast.LENGTH_LONG).show();
                    detailsForm.setVisibility(VIEW.GONE);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            detailsForm.setVisibility(VIEW.GONE);
        }
    }


    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream inputStream = getActivity().getAssets().open("kawa_form_fields.json");
            // Log.e("TAG", "loadJSONFromAsset: " + inputStream);
            if (inputStream != null) {
                int size = inputStream.available();
                byte[] buffer = new byte[size];
                inputStream.read(buffer);
                inputStream.close();
                json = new String(buffer, "UTF-8");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Toast.makeText(getContext(), getResources().getString(R.string.json_file_not_found), Toast.LENGTH_LONG).show();
            Log.e("TAG", "loadJSONFromAsset: " + ex);
            return "";
        }
        return json;
    }

    public void getAddress() {

        LatLng mapLatLng;
        double mapLat, mapLong;
        if (POLYGONAREA.size() == 1) {
            // Log.e(TAG, "getAddress:IF " + POLYGONAREA);
            mapLat = LNGLATEDIT.get(0).get(0).getLatitude();
            mapLong = LNGLATEDIT.get(0).get(0).getLongitude();
        } else {
            // Log.e(TAG, "getAddress:else " + POLYGONAREA);
            mapLatLng = MAPBOXMAP.getCameraPosition().target;
            mapLat = mapLatLng.getLatitude();
            mapLong = mapLatLng.getLongitude();
        }


        COMACT.showLoader(getActivity(), "isCircle");

        AddressServiceManager.getInstance().getKawaService().getAddress("json", String.valueOf(mapLat), String.valueOf(mapLong))
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                JSONObject jsonObj = new JSONObject(response.body().toString());
                                addressTv.setText(jsonObj.getString("display_name"));

                            } else {
                                if (response.errorBody() != null) {
                                    JSONObject jsonObj = new JSONObject(response.errorBody().string());
//                                    // Log.e("RESP", jsonObj.getString("error"));
                                    Toast.makeText(getApplicationContext(), jsonObj.getString("error"), Toast.LENGTH_LONG).show();
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        farmDetailsLayout.setVisibility(VIEW.VISIBLE);
                        anotherndExitLayout.setVisibility(View.VISIBLE);
                        startOverBtn.setVisibility(View.GONE);
                        messageBox.setVisibility(View.GONE);
                        farm_mark_messagebox.setVisibility(View.VISIBLE);
                        COMACT.hideLoader();
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        COMACT.hideLoader();
                        farmDetailsLayout.setVisibility(VIEW.VISIBLE);
                        String errorBody = t.getMessage();
                        Toast.makeText(getApplicationContext(), errorBody, Toast.LENGTH_LONG).show();
                    }
                });


    }

    private void checkForIntersections() {
        Point lnPoint1, lnPoint2, lnPoint3, lnPoint4;
        int intersectionCheckLimit = LNGLATEDIT.get(FINALFOUNDIDX).size() - 2;
        ISINTERSECT = 0;
        changeBorderColor("#F7F14A", FINALFOUNDIDX, 0f);
        messageBox.setText(getResources().getString(R.string.drag_point_joystic));
        messageBox.setBackgroundColor(KawaMap.headerBgColor);
        if (LNGLATEDIT.get(0).size() > 2) {
            for (int j = 0; j < intersectionCheckLimit; j++) {
                lnPoint1 = Point.fromLngLat(LNGLATEDIT.get(FINALFOUNDIDX).get(j).getLongitude(), LNGLATEDIT.get(FINALFOUNDIDX).get(j).getLatitude());
                lnPoint2 = Point.fromLngLat(LNGLATEDIT.get(FINALFOUNDIDX).get(j + 1).getLongitude(), LNGLATEDIT.get(FINALFOUNDIDX).get(j + 1).getLatitude());
                for (int k = 0; k <= intersectionCheckLimit; k++) {
                    if (k != j && k != (j + 1) && (k + 1) != j && (k + 1) != (j + 1)) {
                        lnPoint3 = Point.fromLngLat(LNGLATEDIT.get(FINALFOUNDIDX).get(k).getLongitude(), LNGLATEDIT.get(FINALFOUNDIDX).get(k).getLatitude());
                        lnPoint4 = Point.fromLngLat(LNGLATEDIT.get(FINALFOUNDIDX).get(k + 1).getLongitude(), LNGLATEDIT.get(FINALFOUNDIDX).get(k + 1).getLatitude());
                        if (lineIntersect(lnPoint1, lnPoint2, lnPoint3, lnPoint4)) {
                            changeBorderColor("#ff0000", FINALFOUNDIDX, 0f);
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

    private void changeBorderColor(String borderColorCode, int selectedidx, float opacity) {
        Style style = MAPBOXMAP.getStyle();

        Log.e(TAG, "changeBorderColor:fun " + selectedidx);
        Layer lineLayer = style.getLayer("lineLayerID" + selectedidx);
        Layer polyLayer = style.getLayer("polyLayerID" + selectedidx);
        Integer borderColor = Color.parseColor(borderColorCode);
        lineLayer.setProperties(PropertyFactory.lineColor(borderColor));
        polyLayer.setProperties(PropertyFactory.fillOpacity(opacity));
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


    public void onBackPressed() {
        COMACT.segmentEvents(getActivity(), "Add more plots",
                "Data saved on add more plots", MAPBOXMAP, "", "ADD_MORE_PLOTS");
        getActivity().getSupportFragmentManager().popBackStack();
    }
}