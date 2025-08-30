package org.thunderdog.challegram.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.thunderdog.challegram.BaseActivity;
import org.thunderdog.challegram.Log;
import org.thunderdog.challegram.R;
import org.thunderdog.challegram.tool.UI;
import org.thunderdog.challegram.unsorted.Settings;

import me.vkryl.core.lambda.CancellableRunnable;

/**
 * Google地图坐标信息读取工具类
 * 提供位置获取、坐标转换、地址解析等功能
 */
public class GoogleMapsCoordinateReader {
    
    public interface CoordinateCallback {
        void onCoordinateResult(double latitude, double longitude, float accuracy);
        void onCoordinateError(int errorCode, String errorMessage);
    }
    
    public interface AddressCallback {
        void onAddressResult(String address, double latitude, double longitude);
        void onAddressError(int errorCode, String errorMessage);
    }
    
    private static final int LOCATION_REQUEST_INTERVAL = 10000; // 10秒
    private static final int LOCATION_REQUEST_FASTEST_INTERVAL = 5000; // 5秒
    private static final float LOCATION_MIN_DISTANCE = 10.0f; // 10米
    
    private final Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private CancellableRunnable timeoutRunnable;
    
    public GoogleMapsCoordinateReader(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }
    
    /**
     * 获取当前位置坐标
     */
    public void getCurrentLocation(CoordinateCallback callback) {
        if (!checkLocationPermissions()) {
            callback.onCoordinateError(ERROR_PERMISSION_DENIED, 
                context.getString(R.string.LocationPermissionDenied));
            return;
        }
        
        try {
            fusedLocationClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            callback.onCoordinateResult(
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getAccuracy()
                            );
                        } else {
                            // 如果没有最后位置，请求新的位置更新
                            requestLocationUpdates(callback);
                        }
                    }
                });
        } catch (SecurityException e) {
            callback.onCoordinateError(ERROR_SECURITY_EXCEPTION, e.getMessage());
        }
    }
    
    /**
     * 请求持续的位置更新
     */
    private void requestLocationUpdates(final CoordinateCallback callback) {
        if (!checkLocationPermissions()) {
            callback.onCoordinateError(ERROR_PERMISSION_DENIED, 
                context.getString(R.string.LocationPermissionDenied));
            return;
        }
        
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(LOCATION_MIN_DISTANCE);
        
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        stopLocationUpdates();
                        callback.onCoordinateResult(
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getAccuracy()
                        );
                    }
                }
            }
        };
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, 
                locationCallback, 
                Looper.getMainLooper()
            );
            
            // 设置超时
            timeoutRunnable = new CancellableRunnable() {
                @Override
                public void act() {
                    stopLocationUpdates();
                    callback.onCoordinateError(ERROR_TIMEOUT, 
                        context.getString(R.string.LocationTimeout));
                }
            };
            UI.post(timeoutRunnable, 30000); // 30秒超时
            
        } catch (SecurityException e) {
            callback.onCoordinateError(ERROR_SECURITY_EXCEPTION, e.getMessage());
        }
    }
    
    /**
     * 停止位置更新
     */
    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
        if (timeoutRunnable != null) {
            timeoutRunnable.cancel();
            timeoutRunnable = null;
        }
    }
    
    /**
     * 检查位置权限
     */
    private boolean checkLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                   context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    
    /**
     * 计算两个坐标点之间的距离（米）
     */
    public static double calculateDistance(LatLng point1, LatLng point2) {
        float[] results = new float[1];
        Location.distanceBetween(
            point1.latitude, point1.longitude,
            point2.latitude, point2.longitude,
            results
        );
        return results[0];
    }
    
    /**
     * 将Location转换为LatLng
     */
    public static LatLng locationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
    
    /**
     * 将LatLng转换为Location
     */
    public static Location latLngToLocation(LatLng latLng) {
        Location location = new Location("GoogleMaps");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }
    
    /**
     * 格式化坐标显示
     */
    public static String formatCoordinates(double latitude, double longitude) {
        return String.format("Lat: %.6f, Lng: %.6f", latitude, longitude);
    }
    
    /**
     * 格式化精度信息
     */
    public static String formatAccuracy(float accuracy) {
        return String.format("±%.1fm", accuracy);
    }
    
    // 错误代码
    public static final int ERROR_PERMISSION_DENIED = 1;
    public static final int ERROR_SECURITY_EXCEPTION = 2;
    public static final int ERROR_TIMEOUT = 3;
    public static final int ERROR_LOCATION_UNAVAILABLE = 4;
    
    /**
     * 销毁资源
     */
    public void destroy() {
        stopLocationUpdates();
        fusedLocationClient = null;
    }
}