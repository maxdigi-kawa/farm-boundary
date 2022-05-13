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

public class fragmentEditFarmBoundries extends Fragment implements OnMapReadyCallback, MapboxMap.OnMapClickListener {
    private Common common;
    private MapView mapview;
    private MapboxMap mapboxMap;
    private String TAG = "KAWA";
    private String STRID = "";
    private String SELECTEDPOINTS = "";
    private String PREVIOUSSELECTEDPOINTS = "";

    private List<List<LatLng>> LNGLAT = new ArrayList<>();
    private List<List<LatLng>> LNGLATEDIT = new ArrayList<>();
    private List<List<Point>> LATLNGARR = new ArrayList<>();
    private ArrayList POLYGONAREA = new ArrayList<>();

    private Integer LAYERINDEX;
    private List<SymbolManager> SYMBOLSET = new ArrayList<>();
    private List<SymbolManager> NOSYMBOLSET = new ArrayList<>();
    private List<String> BEFOREEDITLISTFEATURE = new ArrayList<>();
    private Symbol SYMBOLACTIVE;
    private boolean EDITON = false;
    private Integer FINALFOUNDIDX;
    private Button correctBoundryBtn, saveEditBtn, completeMarkingBtn, saveDetailBtn, addDetailBtn, saveDetailnNextBtn, startOverBtn, addMoreBtn, discardEditBtn, backBtn, markAnotherBtn, exitBtn;
    private ImageButton downBtn, upBtn, leftBtn, zoomOutBtn, zoomInBtn, rightBtn;
    private LinearLayout detailsForm, thankyouLinearLayout, farmDetailsLayout, anotherndExitLayout;
    private TextView totalAreaTv, totalseedsTv, addressTv;
    private ImageView dootedLineFirst, dootedLineSecond;
    private JSONArray farm_fields_array;
    private Map<String, String> farm_fields_value = new HashMap<>();
    //    Spinner spinnerView;
    private EditText[] myTextViews;
    private Spinner[] spinnerViewArray;
    private TextView messageBox;
    private LinearLayout farm_mark_messagebox, seedsLayout, locationLayout, areaLayout;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private String STRSUBMIT;
    private int TOTALPOLYGON;
    private int CURRENTPOLYGON;
    private JSONObject FILEDSOBJECT;
    private JSONArray FILEDSARRAY;
    private Integer LASTINDEXOFSELECTEDPOLYGON;
    private String SETERRORMSG = "";
    private boolean FORMVALIDATE = false;
    SymbolManager symbolManager;
    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        common = new Common(getResources().getString(R.string.mapbox_api_key));
        Mapbox.getInstance(getContext(), common.MAPBOX_ACCESS_TOKEN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_farm_boundries, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        super.onCreate(savedInstanceState);

        mapview = view.findViewById(R.id.mapView);
        mapview.getMapAsync(this);
        LNGLATEDIT = (List<List<LatLng>>) getArguments().getSerializable("data");
        POLYGONAREA = (ArrayList) getArguments().getSerializable("polygonarea");
        Log.e(TAG, "POLYGONAREA: " + POLYGONAREA);

        STRID = getArguments().getString("id");
        common.CAMERALAT = getArguments().getDouble("lat", 0.00);
        common.CAMERALNG = getArguments().getDouble("lng", 0.00);
        common.MAPZOOM = getArguments().getDouble("zoom", 17.00);
        messageBox = view.findViewById(R.id.messageBox);
        farm_mark_messagebox = view.findViewById(R.id.farm_mark_messagebox);
//        messageBox.setBackgroundColor(KawaMap.headerBgColor);
//        messageBox.setTextColor(KawaMap.headerTextColor);

        initButtons();
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap1) {
        onFarmsLoaded();
        SYMBOLSET = new ArrayList<>();
        mapboxMap = mapboxMap1;
        mapboxMap.getUiSettings().setCompassEnabled(false);
        mapboxMap.getUiSettings().setLogoEnabled(false);
        mapboxMap.getUiSettings().setFlingVelocityAnimationEnabled(false);
        mapboxMap.getUiSettings().setDoubleTapGesturesEnabled(false);
        mapboxMap.setStyle(Style.SATELLITE_STREETS, style -> {

            LatLng latLng = new LatLng(common.CAMERALAT, common.CAMERALNG);
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(latLng).zoom(common.MAPZOOM).build()), 1000);
            common.lockZoom(mapboxMap);//----------
            mapboxMap.addOnMapClickListener(this);
            LNGLAT = new ArrayList<>();
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
                    common.drawMapLayers(style, llPts, String.valueOf(i), "edit");
                    int middlepolyposition = LNGLAT.get(i).size() / 2;
                    if (KawaMap.isFormEnable)
                        drawSymbolsforPolygon(middlepolyposition, i);
                    //drawSymbol(style, i);
                    Feature multiPointFeature = Feature.fromGeometry(Polygon.fromLngLats(LATLNGARR));
                    multiPointFeature.addStringProperty("area", String.valueOf(POLYGONAREA.get(i)));
                    BEFOREEDITLISTFEATURE.add(multiPointFeature.toJson());
                }
            }
            common.initMarker(getContext(), style, mapboxMap, mapview);
        });
    }

    //
    @Override
    public boolean onMapClick(@NonNull LatLng coordsOfPoint) {

        if (EDITON && SYMBOLACTIVE == null) {
            if (LNGLATEDIT.size() > 0) {
                onMaboxMapClick(coordsOfPoint);
            }
        }
        return false;
    }

    private void onMaboxMapClick(LatLng coordsOfPoint) {
        mapboxMap.getStyle(style -> {
            boolean contains = false;
            Integer foundIdx = -1;

            if (coordsOfPoint != null) {
                for (int i = 0; i < LNGLATEDIT.size(); i++) {
                    contains = common.checkLatLongInPolygon(coordsOfPoint, LNGLATEDIT.get(i));
                    if (contains) {
                        foundIdx = i;
                        break;
                    }
                }
            }
            if (foundIdx >= 0 && LAYERINDEX != foundIdx) {
                for (int i = 0; i < LNGLATEDIT.size(); i++) {
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
                FINALFOUNDIDX = foundIdx;

                new Handler(Looper.getMainLooper()).postDelayed(
                        new Runnable() {
                            public void run() {
                                LAYERINDEX = FINALFOUNDIDX;
                                onFarmSelected(LAYERINDEX);
                                showHideSymbol("show", false);
                                //Log.e("onMapClick AFTER", String.valueOf(EDITON) + " : " + String.valueOf(LAYERINDEX));
                            }
                        },
                        300);
            }
        });
    }

    /*private void drawSymbol(Style style, int idx) {
        SymbolManager symbolManager = new SymbolManager(mapview, mapboxMap, style);
        symbolManager.setIconAllowOverlap(true);
        symbolManager.setTextAllowOverlap(true);
        SYMBOLSET.add(symbolManager);

        style.addImage("symbol_blue", BitmapFactory.decodeResource(this.getResources(), R.drawable.symbol_blue));
        style.addImage("symbol_yellow", BitmapFactory.decodeResource(this.getResources(), R.drawable.symbol_yellow));
        if (LNGLATEDIT.get(idx).size() > 0) {
            LASTINDEXOFSELECTEDPOLYGON = LNGLATEDIT.get(idx).size() - 1;

            for (int j = 0; j < LNGLATEDIT.get(idx).size() - 1; j++) {
                JsonObject objD = new JsonObject();
                //objD.addProperty("lIndex", idx);
                objD.addProperty("sIndex", j);
                symbolManager.create(new SymbolOptions()
                        .withLatLng(new LatLng(LNGLATEDIT.get(idx).get(j).getLatitude(), LNGLATEDIT.get(idx).get(j).getLongitude()))
                        .withIconImage("symbol_blue")
                        .withIconSize(0.5f)
                        .withDraggable(false)
                        .withIconOpacity(0.0f)
                        .withData(objD)
                );
            }
        }

        symbolManager.addClickListener(symbol -> {
            // Log.e("EDITON - LAYERINDEX", String.valueOf(EDITON) + " - " + LAYERINDEX);
            if (EDITON && LAYERINDEX >= 0) {
                // Log.e("getIconOpacity SYMBOL", String.valueOf(symbol.getIconOpacity()));
                if (symbol.getIconOpacity() > 0) {
                    int flg = 0;
                    if (SYMBOLACTIVE != null) {

                        if (!symbol.equals(SYMBOLACTIVE)) {
                            // Log.e("NOT EQUAL", "PREV");
                            SYMBOLACTIVE.setDraggable(false);
                            SYMBOLACTIVE.setIconImage("symbol_blue");
                            SYMBOLACTIVE.setIconSize(0.3F);
                            symbolManager.update(SYMBOLACTIVE);
                            flg = 1;
                        }
                    } else {
                        // Log.e("PREV SYMBOL", "NOT FOUND");
                        flg = 1;
                    }

                    if (flg == 1) {
                        SYMBOLACTIVE = symbol;
                        symbol.setDraggable(true);
                        symbol.setIconImage("symbol_yellow");
                        symbol.setIconSize(0.5f);
                        symbolManager.update(symbol);
                        onSymbolSelected();
                        SELECTEDPOINTS = "lat : " + symbol.getLatLng().getLatitude() + " lng : " + symbol.getLatLng().getLongitude();
                        common.segmentEvents(getContext(), "Point selection for editing",
                                String.valueOf(symbol.getLatLng().getLatitude()), mapboxMap, String.valueOf(symbol.getLatLng().getLongitude()), "TAP_APOINT_TOEDIT");

                    }
                }
            }
            return true;
        });

        symbolManager.addDragListener(new OnSymbolDragListener() {
            LatLng previouscoord;
            String strsubmit;

            @Override
            public void onAnnotationDragStarted(Symbol symbol) {
                previouscoord = symbol.getLatLng();
            }

            @Override
            public void onAnnotationDrag(Symbol symbol) {
                JsonObject objD = (JsonObject) symbol.getData();
                int sIndex = objD.get("sIndex").getAsInt();
                if (sIndex == 0 || sIndex == LASTINDEXOFSELECTEDPOLYGON) {
                    LNGLATEDIT.get(LAYERINDEX).set(0, symbol.getLatLng());
                    LNGLATEDIT.get(LAYERINDEX).set(LASTINDEXOFSELECTEDPOLYGON, symbol.getLatLng());
                } else
                    LNGLATEDIT.get(LAYERINDEX).set(sIndex, symbol.getLatLng());
                redrawFarms();
            }

            @Override
            public void onAnnotationDragFinished(Symbol symbol) {
                strsubmit = "{\"lat\":" + "\"" + previouscoord.getLatitude() + "\"" + ",\"long\":" + "\"" + previouscoord.getLongitude() + "\"" + "},\"currentCoordinates\":{\"lat\":" + "\"" + symbol.getLatLng().getLatitude() + "\"" + ",\"long\":" + "\"" + symbol.getLatLng().getLongitude() + "\"" + "}";
                common.segmentEvents(getContext(), "Point edit",
                        "User moved a point by dragging it", mapboxMap, strsubmit, "DRAG_APOINT_TOEDIT");
            }
        });
    }
*/
    private void showAllLayers() {
        mapboxMap.getStyle(style -> {
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

    private void showHideSymbol(String type, boolean discard) {
        if (SYMBOLSET.size() > 0) {
            // Log.e("SYMBOLSET", String.valueOf(SYMBOLSET.size()));
            for (int i = 0; i < SYMBOLSET.size(); i++) {
                SymbolManager sm = SYMBOLSET.get(i);
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
                                    JsonObject symbolData = (JsonObject) symbol.getData();
                                    //Integer sIndex = symbolData.get("sIndex").getAsInt();
                                    //Integer lIndex = symbolData.get("lIndex").getAsInt();
                                    LatLng latLng = LNGLATEDIT.get(i).get(j);
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

    private void moveSymbol(String direction) {
        if (SYMBOLACTIVE != null) {
            final PointF pointF = mapboxMap.getProjection().toScreenLocation(SYMBOLACTIVE.getLatLng());
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
            LatLng newLL = mapboxMap.getProjection().fromScreenLocation(pointF);
            Log.e(TAG, "moveSymbol:" + direction + " newLL " + newLL);
            setSymbolLL(SYMBOLACTIVE, newLL);
            redrawFarms();
            String strsubmit = "{\"lat\":" + "\"" + prevlatlng.getLatitude() + "\"" + ",\"long\":" + "\"" + prevlatlng.getLongitude() + "\"" + "},\"currentCoordinates\":{\"lat\":" + "\"" + newLL.getLatitude() + "\"" + ",\"long\":" + "\"" + newLL.getLongitude() + "\"" + "},\"arrow\":\"" + direction + "\"";
            common.segmentEvents(getContext(), "Joystick edit",
                    "User moved a point by joystick", mapboxMap, strsubmit, "JOYSTICK_APOINT_TOEDIT");
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
        SYMBOLSET.get(LAYERINDEX).update(symbol);
    }

    private void redrawFarms() {
        if (LNGLATEDIT.get(LAYERINDEX) != null) {
            mapboxMap.getStyle(style -> {
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
        }
    }

    private void initButtons() {
        correctBoundryBtn = getView().findViewById(R.id.correctBoundryBtn);
        addMoreBtn = getView().findViewById(R.id.addMoreBtn);
        saveEditBtn = getView().findViewById(R.id.saveEditBtn);
        completeMarkingBtn = getView().findViewById(R.id.completeMarkingBtn);
        saveDetailBtn = getView().findViewById(R.id.saveDetailBtn);
        addDetailBtn = getView().findViewById(R.id.addDetailBtn);
        saveDetailnNextBtn = getView().findViewById(R.id.saveDetailnNextBtn);
        discardEditBtn = getView().findViewById(R.id.discardEditBtn);
        startOverBtn = getView().findViewById(R.id.startOverBtn);
        backBtn = getView().findViewById(R.id.backBtn);
        upBtn = getView().findViewById(R.id.upBtn);
        downBtn = getView().findViewById(R.id.downBtn);
        leftBtn = getView().findViewById(R.id.leftBtn);
        rightBtn = getView().findViewById(R.id.rightBtn);
        zoomInBtn = getView().findViewById(R.id.zoomInBtn);
        zoomOutBtn = getView().findViewById(R.id.zoomOutBtn);
        markAnotherBtn = getView().findViewById(R.id.markAnotherBtn);
        exitBtn = getView().findViewById(R.id.exitBtn);
        detailsForm = getView().findViewById(R.id.detailsForm);
        thankyouLinearLayout = getView().findViewById(R.id.thankyouLinearLayout);
        farmDetailsLayout = getView().findViewById(R.id.farmDetailsLayout);
        anotherndExitLayout = getView().findViewById(R.id.anotherndExitLayout);
        totalAreaTv = getView().findViewById(R.id.totalAreaTv);
        totalseedsTv = getView().findViewById(R.id.totalseedsTv);
        addressTv = getView().findViewById(R.id.addressTv);
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
        zoomInBtn.setOnClickListener(viewV -> common.setZoomLevel(getContext(), 1, mapboxMap));
        zoomOutBtn.setOnClickListener(viewV -> common.setZoomLevel(getContext(), -1, mapboxMap));
        addMoreBtn.setOnClickListener(viewV -> onBackPressed());
        backBtn.setOnClickListener(viewV -> onBackPressed());
        discardEditBtn.setOnClickListener(viewV -> discardEdit());
        startOverBtn.setOnClickListener(view1 -> startOver("Start_over"));
        markAnotherBtn.setOnClickListener(view1 -> startOver("Mark_another"));
        exitBtn.setOnClickListener(view1 -> exitFunction());

        dootedLineFirst = getView().findViewById(R.id.dootedLineFirst);
        dootedLineSecond = getView().findViewById(R.id.dootedLineSecond);
        seedsLayout = getView().findViewById(R.id.seedsLayout);
        areaLayout = getView().findViewById(R.id.areaLayout);
        locationLayout = getView().findViewById(R.id.locationLayout);

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
//        KawaMap.setFooterButtonColor(footerButtons);
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
//        KawaMap.setInnerButtonColor(getContext(),innerButtons);

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
        correctBoundryBtn.setVisibility(View.GONE);
        addMoreBtn.setVisibility(View.GONE);
        backBtn.setVisibility(View.GONE);
        discardEditBtn.setVisibility(View.GONE);
        anotherndExitLayout.setVisibility(View.GONE);
        upBtn.setVisibility(View.GONE);
        downBtn.setVisibility(View.GONE);
        leftBtn.setVisibility(View.GONE);
        rightBtn.setVisibility(View.GONE);
        zoomInBtn.setVisibility(View.GONE);
        zoomOutBtn.setVisibility(View.GONE);
        completeMarkingBtn.setVisibility(View.GONE);
        saveDetailBtn.setVisibility(View.GONE);
        saveEditBtn.setVisibility(View.GONE);
        addMoreBtn.setVisibility(View.GONE);
        saveDetailnNextBtn.setVisibility(View.GONE);
    }

    //
    private void onFarmsLoaded() {
        // Log.e("Called", "onFarmsLoaded");
        hideAllBtn();
        if (KawaMap.isDrawEnable){
            completeMarking();
        }else {
        addMoreBtn.setVisibility(View.VISIBLE);
        startOverBtn.setVisibility(View.VISIBLE);
        if (KawaMap.isEditEnable) {
            correctBoundryBtn.setVisibility(View.VISIBLE);
            completeMarkingBtn.setVisibility(View.VISIBLE);
        } else if (KawaMap.isFormEnable) {
            addDetailBtn.setVisibility(View.VISIBLE);
        } else {
            saveDetailBtn.setVisibility(View.VISIBLE);
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
        common.segmentEvents(getContext(), "Edit Farm Boundary",
                "User clicked on edit farm boundary", mapboxMap, strSubmit, "EDIT_FARM_BOUNDARY");
        hideAllBtn();
        backBtn.setVisibility(View.VISIBLE);
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
            common.segmentEvents(getContext(), "Farm selected for editing",
                    "User has selected a farm to edit,", mapboxMap, strSubmit, "SINGLE_FARM_SELECTED");

        }
        hideAllBtn();
        //correctBoundryBtn.setVisibility(VIEW.VISIBLE);
        zoomInBtn.setVisibility(View.VISIBLE);
        zoomOutBtn.setVisibility(View.VISIBLE);
        //saveEditBtn.setVisibility(View.VISIBLE);
        discardEditBtn.setVisibility(View.VISIBLE);
        //saveEditBtn.setEnabled(true);
        //EDITON = true;
        messageBox.setText(getResources().getString(R.string.select_point_toedit));
    }

    private void onSymbolSelected() {
        // Log.e("Called", "onSymbolSelected");
        hideAllBtn();
        zoomInBtn.setVisibility(View.VISIBLE);
        zoomOutBtn.setVisibility(View.VISIBLE);
        upBtn.setVisibility(View.VISIBLE);
        downBtn.setVisibility(View.VISIBLE);
        leftBtn.setVisibility(View.VISIBLE);
        rightBtn.setVisibility(View.VISIBLE);
        discardEditBtn.setVisibility(View.VISIBLE);
        saveEditBtn.setVisibility(View.VISIBLE);
        EDITON = true;
        messageBox.setText(getResources().getString(R.string.drag_point_joystic));
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
        common.segmentEvents(getContext(), "Discard edits",
                "User clicks on Discard edits", mapboxMap, "discaard", "DISCARD");
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
        String strJsonWrite = "{\"type\":\"FeatureCollection\", \"features\":" + aftereditlistfeature + "}";
        common.segmentEvents(getContext(), "Save edited boundary",
                "User has Save edited boundary", mapboxMap, strJsonWrite, "SAVE_EDITED_FARM");


        hideAllBtn();
        //addMoreBtn.setVisibility(VIEW.VISIBLE);
        correctBoundryBtn.setVisibility(View.VISIBLE);
        completeMarkingBtn.setVisibility(View.VISIBLE);
        EDITON = false;
        showHideSymbol("hide", false);
        showAllLayers();
        onFarmsLoaded();
        addMoreBtn.setVisibility(View.GONE);
    }

    private void completeMarking() {

        hideAllBtn();
        //backBtn.setVisibility(VIEW.VISIBLE);
        messageBox.setVisibility(View.GONE);
        if (KawaMap.isFormEnable)
            addDetailBtn.setVisibility(View.VISIBLE);
        else
            saveDetailBtn.setVisibility(View.VISIBLE);
        EDITON = false;

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
            saveDetailBtn.setVisibility(View.VISIBLE);
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
                saveDetailBtn.setVisibility(View.VISIBLE);
                saveDetailnNextBtn.setVisibility(View.GONE);
            }
            createformsfileds(CURRENTPOLYGON);
        } else {
            saveDetailnNextBtn.setVisibility(View.VISIBLE);
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
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
        common.hideKeyboard(getActivity(), view);
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
            String strSubmit = "{\"farms_fetched_at\":" + "\"" + common.FARMS_FETCHED_AT + "\"" + ",\"recipe_id\":\"farm_boundaries\",\"aois\":" + String.valueOf(listFeatures) + "}";
            JsonObject submitJsonObject = JsonParser.parseString(strSubmit).getAsJsonObject();
            String strJsonWrite = "{\"type\":\"FeatureCollection\", \"features\":" + listFeatures + "}";
            detailsForm.setVisibility(View.GONE);
            common.showLoader(getContext(), "isCircle");

            ServiceManager.getInstance().getKawaService().sumbitPoints(KawaMap.KAWA_API_KEY, common.SDK_VERSION, submitJsonObject).enqueue(new Callback<MergeModel>() {
                @Override
                public void onResponse(@NonNull Call<MergeModel> call, @NonNull Response<MergeModel> response) {
                    common.hideLoader();
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                // Log.e("TAG", "onResponse: " + response.body().getStatus());
                                // String strJsonWrite = String.valueOf(new Gson().toJson(response.body()));
                                startOverBtn.setVisibility(View.GONE);
                                anotherndExitLayout.setVisibility(View.VISIBLE);
                                InterfaceKawaEvents interfaceKawaEvents = (InterfaceKawaEvents) getContext();
                                interfaceKawaEvents.onkawaSubmit(strJsonWrite);
                                common.segmentEvents(getContext(), "Save Details",
                                        "Data saved on save details", mapboxMap, strJsonWrite, "SAVE_DETAILS");
                                //thankyouLinearLayout.setVisibility(VIEW.VISIBLE);
                                //farmDetailsLayout.setVisibility(VIEW.VISIBLE);
                                //interfaceKawaEvents.onkawaSubmit(strJsonWrite);
                            }
                        } else {
                            common.hideLoader();
                            if (response.errorBody() != null) {
                                JSONObject jsonObj = new JSONObject(response.errorBody().string());
                                Toast.makeText(getContext(), jsonObj.getString("error"), Toast.LENGTH_LONG).show();// this will tell you why your api doesnt work most of time
                            }
                        }
                    } catch (Exception e) {
                        common.hideLoader();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MergeModel> call, @NonNull Throwable t) {
                    common.hideLoader();
                      String errorBody = t.getMessage();
                    Toast.makeText(getContext(), errorBody, Toast.LENGTH_LONG).show();
                }

            });
            if (KawaMap.isFarmDetailsEnable) {
                designFarmDetails();
            }
        } else {
            saveDetailBtn.setVisibility(View.VISIBLE);
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
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

        Style style = mapboxMap.getStyle();
        symbolManager = new SymbolManager(mapview, mapboxMap, style);
        symbolManager.setIconAllowOverlap(true);
        symbolManager.setTextAllowOverlap(true);
        NOSYMBOLSET.add(symbolManager);

        style.addImage("psymbol_blue", BitmapFactory.decodeResource(this.getResources(), R.drawable.symbol_blue));
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
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(latLng).zoom(common.MAPZOOM).build()), 1000);
//        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 20,20,20,100), 1000);

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
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(latLng).zoom(common.MAPZOOM).build()), 1000);
//        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 20,20,20,100), 1000);

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
                final LinearLayout rowLinearLayoutView = new LinearLayout(getContext());
                rowLinearLayoutView.setPadding(20, 10, 10, 10);
                rowLinearLayoutView.setOrientation(LinearLayout.HORIZONTAL);
                rowLinearLayoutView.setLayoutParams(editTextLayoutParams);

                final ImageView rowImageView = new ImageView(getContext());
                rowImageView.setPadding(0, 0, 20, 0);
                rowImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_area));

                final ImageView rowDottedImageView = new ImageView(getContext());
                rowDottedImageView.setPadding(0, 10, 20, 10);
                rowDottedImageView.setImageDrawable(getResources().getDrawable(R.drawable.dottedline));
                rowDottedImageView.setMinimumHeight(40);

                final TextView rowAreaTextView = new TextView(getContext());
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

                final TextView rowAreaLabelView = new TextView(getContext());
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
        farmDetailsLayout.setVisibility(View.VISIBLE);
        anotherndExitLayout.setVisibility(View.VISIBLE);
        farm_mark_messagebox.setVisibility(View.VISIBLE);

    }

    private void startOver(String eventname) {
        if (eventname.equals("Mark_another")) {
            common.segmentEvents(getContext(), "Mark another Farm",
                    "User clicked on Mark another Farm", mapboxMap, "", "MARK_ANOTHER_PLOTS");
        } else {
            common.segmentEvents(getContext(), "Start Over",
                    "user clicked on Start over", mapboxMap, "", "START_OVER");
        }
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.kawaMapView, fragmentFarmLocation.class, null);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void exitFunction() {

        common.segmentEvents(getContext(), "Exit",
                "User has exited farm boundary", mapboxMap, "exit", "EXIT_SDK");
        common.destroyStaticVariables();
        InterfaceKawaEvents interfaceKawaEvents = (InterfaceKawaEvents) getContext();
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
                    detailsForm.setVisibility(View.VISIBLE);
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
                            EditText editTextView = FormBuilder.editFiled(getContext(), jsonObject, editTextLayoutParams);
                            detailsForm.addView(editTextView);
                            myTextViews[i] = editTextView;

                        } else if (field_type.equals("dropdown")) {
                            Spinner spinnerView = FormBuilder.spinnerFiled(getContext(), jsonObject, editTextLayoutParams);
                            detailsForm.addView(spinnerView);
                            spinnerViewArray[i] = spinnerView;
                        } else if (field_type.equals("date")) {
                            EditText dateTextView = FormBuilder.dateFiled(getContext(), jsonObject, editTextLayoutParams);
                            detailsForm.addView(dateTextView);
                            myTextViews[i] = dateTextView;
                        } else if (field_type.equals("textarea")) {
                            EditText textareaView = FormBuilder.textArea(getContext(), jsonObject, editTextLayoutParams);
                            detailsForm.addView(textareaView);
                            myTextViews[i] = textareaView;
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Invalid JSON", Toast.LENGTH_LONG).show();
                    detailsForm.setVisibility(View.GONE);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            detailsForm.setVisibility(View.GONE);
        }
    }


    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream inputStream = getContext().getAssets().open("kawa_form_fields.json");
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
            mapLatLng = mapboxMap.getCameraPosition().target;
            mapLat = mapLatLng.getLatitude();
            mapLong = mapLatLng.getLongitude();
        }


        common.showLoader(getContext(), "isCircle");

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
                                    Toast.makeText(getContext(), jsonObj.getString("error"), Toast.LENGTH_LONG).show();
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        farmDetailsLayout.setVisibility(View.VISIBLE);
                        anotherndExitLayout.setVisibility(View.VISIBLE);
                        startOverBtn.setVisibility(View.GONE);
                        messageBox.setVisibility(View.GONE);
                        farm_mark_messagebox.setVisibility(View.VISIBLE);
                        common.hideLoader();
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        common.hideLoader();
                        farmDetailsLayout.setVisibility(View.VISIBLE);
                        String errorBody = t.getMessage();
                        Toast.makeText(getContext(), errorBody, Toast.LENGTH_LONG).show();
                    }
                });


    }

    @Override
    public void onStart() {
        super.onStart();
        mapview.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapview.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapview.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapview.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapview.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapview.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapview.onLowMemory();
    }

    //
//
    public void onBackPressed() {
        common.segmentEvents(getContext(), "Add more plots",
                "Data saved on add more plots", mapboxMap, "", "ADD_MORE_PLOTS");
        getActivity().getSupportFragmentManager().popBackStack(); //TODO:
    }
}