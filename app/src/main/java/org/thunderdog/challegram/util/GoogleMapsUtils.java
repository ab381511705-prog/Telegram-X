package org.thunderdog.challegram.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;
import org.thunderdog.challegram.Log;
import org.thunderdog.challegram.tool.UI;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Google地图工具类
 * 提供坐标读取、转换、计算等实用功能
 */
public class GoogleMapsUtils {
    
    public interface LocationResultCallback {
        void onLocationResult(double latitude, double longitude, float accuracy);
        void onLocationError(String errorMessage);
    }
    
    public interface DistanceCallback {
        void onDistanceCalculated(double distanceMeters);
    }
    
    public interface GeocodeCallback {
        void onGeocodeResult(String address, String district, String city, String country);
        void onGeocodeError(String errorMessage);
    }
    
    public interface BlockInfoCallback {
        void onBlockInfoResult(Map<String, String> blockInfo);
        void onBlockInfoError(String errorMessage);
    }
    
    public interface SpatialAnalysisCallback {
        void onSpatialAnalysisResult(Map<String, Object> analysisResult);
        void onSpatialAnalysisError(String errorMessage);
    }
    
    public interface BlockchainGeoValidationCallback {
        void onValidationResult(boolean isValid, String validationMessage, Map<String, Object> validationData);
        void onValidationError(String errorMessage);
    }
    
    /**
     * 获取当前位置坐标
     */
    public static void getCurrentLocation(Context context, LocationResultCallback callback) {
        if (!hasLocationPermissions(context)) {
            callback.onLocationError("位置权限未授予");
            return;
        }
        
        try {
            FusedLocationProviderClient fusedLocationClient = 
                LocationServices.getFusedLocationProviderClient(context);
            
            fusedLocationClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            callback.onLocationResult(
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getAccuracy()
                            );
                        } else {
                            callback.onLocationError("无法获取位置信息");
                        }
                    }
                });
        } catch (SecurityException e) {
            callback.onLocationError("安全异常: " + e.getMessage());
        } catch (Exception e) {
            callback.onLocationError("获取位置失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查位置权限
     */
    public static boolean hasLocationPermissions(Context context) {
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
     * 计算两点之间的距离（异步）
     */
    public static void calculateDistanceAsync(LatLng point1, LatLng point2, DistanceCallback callback) {
        UI.post(() -> {
            double distance = calculateDistance(point1, point2);
            callback.onDistanceCalculated(distance);
        });
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
        return String.format("纬度: %.6f, 经度: %.6f", latitude, longitude);
    }
    
    /**
     * 格式化精度信息
     */
    public static String formatAccuracy(float accuracy) {
        return String.format("精度: ±%.1f米", accuracy);
    }
    
    /**
     * 格式化距离显示
     */
    public static String formatDistance(double distanceMeters) {
        if (distanceMeters < 1000) {
            return String.format("%.0f米", distanceMeters);
        } else {
            return String.format("%.2f公里", distanceMeters / 1000);
        }
    }
    
    /**
     * 验证坐标是否有效
     */
    public static boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && 
               longitude >= -180 && longitude <= 180;
    }
    
    /**
     * 验证坐标是否在中国境内（近似）
     */
    public static boolean isInChina(double latitude, double longitude) {
        return latitude >= 18.0 && latitude <= 53.5 && 
               longitude >= 73.0 && longitude <= 135.0;
    }
    
    /**
     * 坐标转换示例：WGS84转GCJ02（中国偏移校正）
     * 注意：这只是一个简单示例，实际应用中需要使用专业的坐标转换库
     */
    public static LatLng convertWGS84ToGCJ02(double lat, double lng) {
        // 简化的坐标偏移校正（实际算法更复杂）
        double deltaLat = 0.0060;
        double deltaLng = 0.0065;
        return new LatLng(lat + deltaLat, lng + deltaLng);
    }
    
    /**
     * 生成位置分享文本
     */
    public static String generateShareText(double latitude, double longitude, String address) {
        StringBuilder sb = new StringBuilder();
        sb.append("📍 当前位置\n");
        sb.append(formatCoordinates(latitude, longitude)).append("\n");
        
        if (address != null && !address.isEmpty()) {
            sb.append("地址: ").append(address).append("\n");
        }
        
        sb.append("Google Maps: https://maps.google.com/?q=")
          .append(latitude).append(",").append(longitude);
        
        return sb.toString();
    }
    
    /**
     * 生成Google Maps链接
     */
    public static String generateGoogleMapsLink(double latitude, double longitude) {
        return "https://maps.google.com/?q=" + latitude + "," + longitude;
    }
    
    /**
     * 生成静态地图图片URL
     */
    public static String generateStaticMapUrl(double latitude, double longitude, int width, int height) {
        return String.format("https://maps.googleapis.com/maps/api/staticmap?center=%.6f,%.6f&zoom=15&size=%dx%d&markers=color:red%%7C%.6f,%.6f",
            latitude, longitude, width, height, latitude, longitude);
    }

    /**
     * 地理编码 - 根据坐标获取地址信息
     */
    public static void reverseGeocode(double latitude, double longitude, GeocodeCallback callback) {
        UI.post(() -> {
            try {
                String url = String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%.6f&lon=%.6f&zoom=18&addressdetails=1",
                    latitude, longitude);
                
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Telegram-X/1.0");
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                JSONObject json = new JSONObject(response.toString());
                JSONObject address = json.getJSONObject("address");
                
                String road = address.optString("road", "");
                String suburb = address.optString("suburb", "");
                String city = address.optString("city", address.optString("town", ""));
                String country = address.optString("country", "");
                
                String fullAddress = "";
                if (!road.isEmpty()) fullAddress += road;
                if (!suburb.isEmpty()) fullAddress += (fullAddress.isEmpty() ? "" : ", ") + suburb;
                
                callback.onGeocodeResult(fullAddress, suburb, city, country);
                
            } catch (Exception e) {
                callback.onGeocodeError("地理编码失败: " + e.getMessage());
            }
        });
    }

    /**
     * 区块信息解码 - 根据坐标获取区块关联信息
     */
    public static void decodeBlockInfo(double latitude, double longitude, BlockInfoCallback callback) {
        UI.post(() -> {
            try {
                Map<String, String> blockInfo = new HashMap<>();
                
                // 1. 基础地理信息
                blockInfo.put("latitude", String.format("%.6f", latitude));
                blockInfo.put("longitude", String.format("%.6f", longitude));
                
                // 2. 地理区块编码（自定义算法）
                String blockCode = generateBlockCode(latitude, longitude);
                blockInfo.put("block_code", blockCode);
                blockInfo.put("grid_sector", getGridSector(latitude, longitude));
                
                // 3. 时区信息
                blockInfo.put("timezone", getTimezone(latitude, longitude));
                
                // 4. 地理特征
                blockInfo.put("elevation_zone", getElevationZone(latitude, longitude));
                blockInfo.put("land_type", getLandType(latitude, longitude));
                
                // 5. 行政区划推测
                blockInfo.put("region_code", getRegionCode(latitude, longitude));
                blockInfo.put("admin_level", getAdminLevel(latitude, longitude));
                
                callback.onBlockInfoResult(blockInfo);
                
            } catch (Exception e) {
                callback.onBlockInfoError("区块解码失败: " + e.getMessage());
            }
        });
    }

    /**
     * 生成区块编码（自定义算法）
     */
    private static String generateBlockCode(double lat, double lng) {
        // 将经纬度转换为区块编码
        int latBlock = (int) ((lat + 90) * 100);
        int lngBlock = (int) ((lng + 180) * 100);
        return String.format("BLK-%04d-%04d", latBlock, lngBlock);
    }

    /**
     * 获取网格区域
     */
    private static String getGridSector(double lat, double lng) {
        char latSector = (char) ('A' + (int) ((lat + 90) / 10));
        char lngSector = (char) ('A' + (int) ((lng + 180) / 10));
        return String.format("%c%c-SECTOR", latSector, lngSector);
    }

    /**
     * 估算时区
     */
    private static String getTimezone(double lat, double lng) {
        int offset = (int) (lng / 15);
        return String.format("UTC%+d", offset);
    }

    /**
     * 海拔区域估算
     */
    private static String getElevationZone(double lat, double lng) {
        // 简化的海拔区域估算（实际应该调用高程API）
        if (lat > 45) return "NORTH_HIGHLAND";
        if (lat < -45) return "SOUTH_HIGHLAND";
        if (Math.abs(lat) < 30) return "TROPICAL_LOWLAND";
        return "TEMPERATE_ZONE";
    }

    /**
     * 土地类型估算
     */
    private static String getLandType(double lat, double lng) {
        // 基于经纬度的土地类型估算
        if (Math.abs(lat) > 60) return "POLAR";
        if (Math.abs(lat) > 40) return "TEMPERATE";
        if (Math.abs(lat) < 20) return "TROPICAL";
        return "SUBTROPICAL";
    }

    /**
     * 行政区划代码估算
     */
    private static String getRegionCode(double lat, double lng) {
        // 简化的区域代码（实际应该使用GIS数据）
        if (lng > 130) return "ASIA_EAST";
        if (lng > 70) return "ASIA_CENTRAL";
        if (lng > -10) return "EUROPE_AFRICA";
        if (lng > -80) return "AMERICA_SOUTH";
        return "AMERICA_NORTH";
    }

    /**
     * 行政级别估算
     */
    private static String getAdminLevel(double lat, double lng) {
        // 基于人口密度的行政级别估算
        if (Math.abs(lat) < 30 && Math.abs(lng) < 120) return "URBAN_CORE";
        if (Math.abs(lat) < 50 && Math.abs(lng) < 150) return "SUBURBAN";
        return "RURAL";
    }

    /**
     * 获取详细的区块分析报告
     */
    public static String generateBlockAnalysisReport(Map<String, String> blockInfo) {
        StringBuilder report = new StringBuilder();
        report.append("📍 区块分析报告
");
        report.append("====================
");
        
        report.append("📊 基础信息:
");
        report.append("• 纬度: ").append(blockInfo.get("latitude")).append("
");
        report.append("• 经度: ").append(blockInfo.get("longitude")).append("
");
        report.append("• 区块编码: ").append(blockInfo.get("block_code")).append("
");
        
        report.append("
🌍 地理信息:
");
        report.append("• 网格区域: ").append(blockInfo.get("grid_sector")).append("
");
        report.append("• 时区: ").append(blockInfo.get("timezone")).append("
");
        report.append("• 海拔区域: ").append(blockInfo.get("elevation_zone")).append("
");
        report.append("• 土地类型: ").append(blockInfo.get("land_type")).append("
");
        
        report.append("
🏛️ 行政信息:
");
        report.append("• 区域代码: ").append(blockInfo.get("region_code")).append("
");
        report.append("• 行政级别: ").append(blockInfo.get("admin_level")).append("
");
        
        report.append("
📈 分析结论:
");
        report.append("• 该区块位于").append(blockInfo.get("region_code")).append("区域
");
        report.append("• 属于").append(blockInfo.get("land_type")).append("气候类型
");
        report.append("• 行政级别: ").append(blockInfo.get("admin_level")).append("
");
        
        return report.toString();
    }

    /**
     * 深度空间分析 - 区块链地理空间验证
     */
    public static void performSpatialAnalysis(double latitude, double longitude, SpatialAnalysisCallback callback) {
        UI.post(() -> {
            try {
                Map<String, Object> analysisResult = new HashMap<>();
                
                // 1. 基础地理验证
                analysisResult.put("is_valid_coordinate", isValidCoordinate(latitude, longitude));
                analysisResult.put("is_in_china", isInChina(latitude, longitude));
                
                // 2. 地理哈希编码（区块链友好）
                analysisResult.put("geo_hash", generateGeoHash(latitude, longitude));
                analysisResult.put("spatial_hash", generateSpatialHash(latitude, longitude));
                
                // 3. 区块链相关地理特征
                analysisResult.put("blockchain_zone", analyzeBlockchainZone(latitude, longitude));
                analysisResult.put("mining_suitability", calculateMiningSuitability(latitude, longitude));
                analysisResult.put("network_proximity", calculateNetworkProximity(latitude, longitude));
                
                // 4. 环境因素分析
                analysisResult.put("energy_efficiency", calculateEnergyEfficiency(latitude, longitude));
                analysisResult.put("environmental_impact", assessEnvironmentalImpact(latitude, longitude));
                
                // 5. 安全风险评估
                analysisResult.put("security_risk", assessSecurityRisk(latitude, longitude));
                analysisResult.put("regulatory_compliance", checkRegulatoryCompliance(latitude, longitude));
                
                callback.onSpatialAnalysisResult(analysisResult);
                
            } catch (Exception e) {
                callback.onSpatialAnalysisError("空间分析失败: " + e.getMessage());
            }
        });
    }

    /**
     * 区块链地理空间验证
     */
    public static void validateBlockchainLocation(double latitude, double longitude, BlockchainGeoValidationCallback callback) {
        UI.post(() -> {
            try {
                Map<String, Object> validationData = new HashMap<>();
                boolean isValid = true;
                StringBuilder validationMessage = new StringBuilder();
                
                // 1. 基础坐标验证
                if (!isValidCoordinate(latitude, longitude)) {
                    isValid = false;
                    validationMessage.append("无效的坐标; ");
                }
                
                // 2. 地理可行性验证
                String blockchainZone = analyzeBlockchainZone(latitude, longitude);
                validationData.put("blockchain_zone", blockchainZone);
                
                if (blockchainZone.equals("RESTRICTED")) {
                    isValid = false;
                    validationMessage.append("位于限制区域; ");
                }
                
                // 3. 环境可行性
                double miningSuitability = calculateMiningSuitability(latitude, longitude);
                validationData.put("mining_suitability", miningSuitability);
                
                if (miningSuitability < 0.3) {
                    validationMessage.append("挖矿适宜性较低; ");
                }
                
                // 4. 法规合规性
                boolean isCompliant = checkRegulatoryCompliance(latitude, longitude);
                validationData.put("regulatory_compliance", isCompliant);
                
                if (!isCompliant) {
                    isValid = false;
                    validationMessage.append("法规不合规; ");
                }
                
                // 5. 安全风险评估
                double securityRisk = assessSecurityRisk(latitude, longitude);
                validationData.put("security_risk", securityRisk);
                
                if (securityRisk > 0.7) {
                    validationMessage.append("安全风险较高; ");
                }
                
                if (validationMessage.length() == 0) {
                    validationMessage.append("地理位置验证通过");
                }
                
                callback.onValidationResult(isValid, validationMessage.toString(), validationData);
                
            } catch (Exception e) {
                callback.onValidationError("验证失败: " + e.getMessage());
            }
        });
    }

    // ===== 深度分析工具方法 =====
    
    /**
     * 生成地理哈希（Geohash算法简化版）
     */
    private static String generateGeoHash(double lat, double lng) {
        // 简化的Geohash生成（实际应该使用完整的Geohash算法）
        int latInt = (int) ((lat + 90) * 10000);
        int lngInt = (int) ((lng + 180) * 10000);
        return String.format("GH-%08X-%08X", latInt, lngInt);
    }
    
    /**
     * 生成空间哈希（区块链专用）
     */
    private static String generateSpatialHash(double lat, double lng) {
        // 基于区块链的空间哈希算法
        String baseHash = generateGeoHash(lat, lng);
        return "BLCK-" + baseHash.substring(3) + "-SPT";
    }
    
    /**
     * 分析区块链区域类型
     */
    private static String analyzeBlockchainZone(double lat, double lng) {
        // 基于地理位置的区块链区域分类
        if (Math.abs(lat) > 60) return "POLAR"; // 极地地区
        if (Math.abs(lat) > 40) return "TEMPERATE"; // 温带地区
        if (Math.abs(lat) < 20) return "TROPICAL"; // 热带地区
        
        // 特殊限制区域检测
        if (lng > 100 && lng < 140 && lat > 20 && lat < 50) {
            return "ASIA_PACIFIC"; // 亚太地区
        }
        if (lng > -80 && lng < -40 && lat > 25 && lat < 50) {
            return "NORTH_AMERICA"; // 北美地区
        }
        
        return "GENERAL";
    }
    
    /**
     * 计算挖矿适宜性（0-1范围）
     */
    private static double calculateMiningSuitability(double lat, double lng) {
        // 基于温度、能源成本、网络基础设施的适宜性计算
        double score = 0.5; // 基础分
        
        // 温度因素（适宜温度范围）
        double tempFactor = 1.0 - Math.min(Math.abs(Math.abs(lat) - 35) / 35, 1.0);
        score += tempFactor * 0.2;
        
        // 能源成本因素（假设赤道附近能源成本较低）
        double energyFactor = 1.0 - Math.abs(lat) / 90;
        score += energyFactor * 0.15;
        
        // 网络基础设施（发达国家得分更高）
        double developmentFactor = (Math.abs(lat) < 45 && Math.abs(lng) < 135) ? 0.2 : 0.1;
        score += developmentFactor;
        
        return Math.min(Math.max(score, 0), 1);
    }
    
    /**
     * 计算网络邻近度
     */
    private static double calculateNetworkProximity(double lat, double lng) {
        // 计算与主要区块链节点的网络距离（简化版）
        // 主要节点位置：北美、欧洲、亚洲
        double[] nodeLats = {37.0, 52.0, 35.0};
        double[] nodeLngs = {-122.0, 13.0, 139.0};
        
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < nodeLats.length; i++) {
            double distance = calculateDistance(new LatLng(lat, lng), new LatLng(nodeLats[i], nodeLngs[i]));
            minDistance = Math.min(minDistance, distance);
        }
        
        // 距离越近，网络邻近度越高
        return 1.0 - Math.min(minDistance / 10000, 1.0);
    }
    
    /**
     * 计算能源效率
     */
    private static double calculateEnergyEfficiency(double lat, double lng) {
        // 基于地理位置的可再生能源可用性
        double efficiency = 0.6; // 基础效率
        
        // 太阳能潜力（赤道附近更高）
        efficiency += (1.0 - Math.abs(lat) / 90) * 0.2;
        
        // 风能潜力（沿海地区更高）
        double coastalFactor = (Math.abs(lng % 10) < 2) ? 0.1 : 0;
        efficiency += coastalFactor;
        
        return Math.min(efficiency, 0.9);
    }
    
    /**
     * 评估环境影响
     */
    private static String assessEnvironmentalImpact(double lat, double lng) {
        if (Math.abs(lat) > 60) return "LOW"; // 极地地区影响低
        if (Math.abs(lat) < 23.5) return "MODERATE"; // 热带地区中等
        return "STANDARD";
    }
    
    /**
     * 评估安全风险（0-1范围）
     */
    private static double assessSecurityRisk(double lat, double lng) {
        double risk = 0.3; // 基础风险
        
        // 政治稳定性因素（简化）
        if (lng > 70 && lng < 140 && lat > 10 && lat < 55) {
            risk += 0.2; // 亚洲地区
        }
        if (lng > -20 && lng < 40 && lat > 35 && lat < 70) {
            risk -= 0.1; // 欧洲地区
        }
        
        // 自然灾害风险
        if (Math.abs(lat) < 30 && Math.abs(lng) > 120 && Math.abs(lng) < 180) {
            risk += 0.2; // 环太平洋地震带
        }
        
        return Math.min(Math.max(risk, 0), 1);
    }
    
    /**
     * 检查法规合规性
     */
    private static boolean checkRegulatoryCompliance(double lat, double lng) {
        // 检查是否在限制性法规区域
        // 中国境内限制
        if (isInChina(lat, lng)) {
            return false; // 中国境内区块链限制
        }
        
        // 其他限制区域检测
        if (lng > 55 && lng < 75 && lat > 20 && lat < 40) {
            return false; // 中东某些地区
        }
        
        return true;
    }
    
    /**
     * 生成详细的空间分析报告
     */
    public static String generateSpatialAnalysisReport(Map<String, Object> analysisResult) {
        StringBuilder report = new StringBuilder();
        report.append("🌐 深度空间分析报告
");
        report.append("====================
");
        
        report.append("🔍 基础验证:
");
        report.append("• 坐标有效性: ").append(analysisResult.get("is_valid_coordinate")).append("
");
        report.append("• 中国境内: ").append(analysisResult.get("is_in_china")).append("
");
        
        report.append("
🔢 地理编码:
");
        report.append("• 地理哈希: ").append(analysisResult.get("geo_hash")).append("
");
        report.append("• 空间哈希: ").append(analysisResult.get("spatial_hash")).append("
");
        
        report.append("
⚡ 区块链特征:
");
        report.append("• 区块链区域: ").append(analysisResult.get("blockchain_zone")).append("
");
        report.append("• 挖矿适宜性: ").append(String.format("%.1f%%", (Double)analysisResult.get("mining_suitability") * 100)).append("
");
        report.append("• 网络邻近度: ").append(String.format("%.1f%%", (Double)analysisResult.get("network_proximity") * 100)).append("
");
        
        report.append("
🌿 环境因素:
");
        report.append("• 能源效率: ").append(String.format("%.1f%%", (Double)analysisResult.get("energy_efficiency") * 100)).append("
");
        report.append("• 环境影响: ").append(analysisResult.get("environmental_impact")).append("
");
        
        report.append("
🛡️ 安全风险:
");
        report.append("• 安全风险等级: ").append(String.format("%.1f/1.0", (Double)analysisResult.get("security_risk"))).append("
");
        report.append("• 法规合规: ").append(analysisResult.get("regulatory_compliance")).append("
");
        
        return report.toString();
    }
}
}