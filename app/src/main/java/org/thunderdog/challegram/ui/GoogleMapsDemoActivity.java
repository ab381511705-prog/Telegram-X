package org.thunderdog.challegram.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.thunderdog.challegram.BaseActivity;
import org.thunderdog.challegram.R;
import org.thunderdog.challegram.component.base.SettingView;
import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.navigation.ViewController;
import org.thunderdog.challegram.tool.UI;
import org.thunderdog.challegram.util.GoogleMapsCoordinateReader;
import org.thunderdog.challegram.widget.ShadowView;

import me.vkryl.android.AnimatorUtils;

/**
 * Google地图坐标演示Activity
 * 展示如何获取和显示位置坐标信息
 */
public class GoogleMapsDemoActivity extends ViewController<GoogleMapsDemoActivity.Args> 
    implements OnMapReadyCallback, GoogleMapsCoordinateReader.CoordinateCallback {
    
    public static class Args {
        // 可添加参数
    }
    
    private MapView mapView;
    private GoogleMap googleMap;
    private GoogleMapsCoordinateReader coordinateReader;
    
    private SettingView locationInfoView;
    private Button getLocationButton;
    private TextView coordinatesTextView;
    private TextView accuracyTextView;
    
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    public GoogleMapsDemoActivity (Context context) {
        super(context, null);
    }
    
    @Override
    public int getId () {
        return R.id.controller_google_maps_demo;
    }
    
    @Override
    protected View onCreateView (Context context) {
        coordinateReader = new GoogleMapsCoordinateReader(context);
        
        // 创建主布局
        FrameLayoutFix contentView = new FrameLayoutFix(context);
        contentView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        
        // 创建地图视图
        mapView = new MapView(context);
        mapView.setLayoutParams(new FrameLayoutFix.LayoutParams(
            FrameLayoutFix.LayoutParams.MATCH_PARENT,
            FrameLayoutFix.LayoutParams.MATCH_PARENT
        ));
        mapView.onCreate(null);
        mapView.getMapAsync(this);
        contentView.addView(mapView);
        
        // 创建信息面板
        LinearLayout infoPanel = new LinearLayout(context);
        infoPanel.setOrientation(LinearLayout.VERTICAL);
        infoPanel.setLayoutParams(new FrameLayoutFix.LayoutParams(
            FrameLayoutFix.LayoutParams.MATCH_PARENT,
            FrameLayoutFix.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        ));
        infoPanel.setBackgroundColor(0xAA000000);
        infoPanel.setPadding(Screen.dp(16f), Screen.dp(16f), Screen.dp(16f), Screen.dp(16f));
        
        // 坐标信息显示
        coordinatesTextView = new TextView(context);
        coordinatesTextView.setTextColor(0xFFFFFFFF);
        coordinatesTextView.setTextSize(16);
        coordinatesTextView.setText(Lang.getString(R.string.WaitingForLocation));
        infoPanel.addView(coordinatesTextView);
        
        // 精度信息显示
        accuracyTextView = new TextView(context);
        accuracyTextView.setTextColor(0xCCFFFFFF);
        accuracyTextView.setTextSize(14);
        infoPanel.addView(accuracyTextView);
        
        // 获取位置按钮
        getLocationButton = new Button(context);
        getLocationButton.setText(Lang.getString(R.string.GetCurrentLocation));
        getLocationButton.setOnClickListener(v -> requestLocation());
        infoPanel.addView(getLocationButton);
        
        contentView.addView(infoPanel);
        
        return contentView;
    }
    
    @Override
    public void onMapReady (GoogleMap googleMap) {
        this.googleMap = googleMap;
        
        // 配置地图
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        
        // 请求位置权限
        if (checkLocationPermissions()) {
            enableLocationFeatures();
        } else {
            requestLocationPermissions();
        }
    }
    
    private void requestLocation () {
        if (!checkLocationPermissions()) {
            requestLocationPermissions();
            return;
        }
        
        getLocationButton.setEnabled(false);
        getLocationButton.setText(Lang.getString(R.string.GettingLocation));
        
        coordinateReader.getCurrentLocation(this);
    }
    
    @Override
    public void onCoordinateResult (double latitude, double longitude, float accuracy) {
        UI.post(() -> {
            // 更新UI显示
            coordinatesTextView.setText(GoogleMapsCoordinateReader.formatCoordinates(latitude, longitude));
            accuracyTextView.setText(GoogleMapsCoordinateReader.formatAccuracy(accuracy));
            
            getLocationButton.setEnabled(true);
            getLocationButton.setText(Lang.getString(R.string.GetCurrentLocation));
            
            // 在地图上显示位置
            if (googleMap != null) {
                LatLng location = new LatLng(latitude, longitude);
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(Lang.getString(R.string.CurrentLocation))
                    .snippet(GoogleMapsCoordinateReader.formatAccuracy(accuracy))
                );
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
            }
        });
    }
    
    @Override
    public void onCoordinateError (int errorCode, String errorMessage) {
        UI.post(() -> {
            coordinatesTextView.setText(errorMessage);
            accuracyTextView.setText("");
            
            getLocationButton.setEnabled(true);
            getLocationButton.setText(Lang.getString(R.string.GetCurrentLocation));
            
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        });
    }
    
    private boolean checkLocationPermissions () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                   getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    
    private void requestLocationPermissions () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            };
            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    
    private void enableLocationFeatures () {
        if (googleMap != null && checkLocationPermissions()) {
            try {
                googleMap.setMyLocationEnabled(true);
                getLocationButton.setEnabled(true);
            } catch (SecurityException e) {
                Toast.makeText(getContext(), R.string.LocationPermissionDenied, Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationFeatures();
            } else {
                Toast.makeText(getContext(), R.string.LocationPermissionDenied, Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void destroy () {
        super.destroy();
        if (coordinateReader != null) {
            coordinateReader.destroy();
            coordinateReader = null;
        }
        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
        }
    }
    
    @Override
    public void onResume () {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }
    
    @Override
    public void onPause () {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
    
    @Override
    public void onLowMemory () {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
    
    @Override
    public void onSaveInstanceState (@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }
}