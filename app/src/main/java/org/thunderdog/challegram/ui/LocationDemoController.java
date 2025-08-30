package org.thunderdog.challegram.ui;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.thunderdog.challegram.R;
import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.navigation.ViewController;
import org.thunderdog.challegram.tool.Screen;
import org.thunderdog.challegram.tool.UI;
import org.thunderdog.challegram.util.GoogleMapsUtils;
import org.thunderdog.challegram.widget.ShadowView;

import me.vkryl.android.widget.FrameLayoutFix;

import java.util.Map;

/**
 * 位置坐标演示控制器
 * 展示如何使用Google Maps坐标读取功能
 */
public class LocationDemoController extends ViewController<LocationDemoController.Args> {
    
    public static class Args {
        // 可添加参数
    }
    
    private TextView coordinatesTextView;
    private TextView accuracyTextView;
    private TextView blockInfoTextView;
    private Button getLocationButton;
    private Button copyButton;
    private Button openMapsButton;
    private Button shareButton;
    private Button analyzeBlockButton;
    
    public LocationDemoController (Context context, @Nullable Args args) {
        super(context, args);
    }
    
    @Override
    public int getId () {
        return R.id.controller_location_demo;
    }
    
    @Override
    protected View onCreateView (Context context) {
        // 创建主布局
        FrameLayoutFix contentView = new FrameLayoutFix(context);
        contentView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        contentView.setBackgroundColor(0xFFF5F5F5);
        
        // 创建信息面板
        LinearLayout infoPanel = new LinearLayout(context);
        infoPanel.setOrientation(LinearLayout.VERTICAL);
        infoPanel.setLayoutParams(new FrameLayoutFix.LayoutParams(
            FrameLayoutFix.LayoutParams.MATCH_PARENT,
            FrameLayoutFix.LayoutParams.WRAP_CONTENT
        ));
        infoPanel.setPadding(Screen.dp(24f), Screen.dp(24f), Screen.dp(24f), Screen.dp(24f));
        
        // 标题
        TextView titleTextView = new TextView(context);
        titleTextView.setText(Lang.getString(R.string.LocationDemoTitle));
        titleTextView.setTextSize(20);
        titleTextView.setTextColor(0xFF333333);
        infoPanel.addView(titleTextView);
        
        // 副标题
        TextView subtitleTextView = new TextView(context);
        subtitleTextView.setText(Lang.getString(R.string.LocationDemoSubtitle));
        subtitleTextView.setTextSize(14);
        subtitleTextView.setTextColor(0xFF666666);
        subtitleTextView.setPadding(0, Screen.dp(8f), 0, Screen.dp(16f));
        infoPanel.addView(subtitleTextView);
        
        // 坐标信息显示
        coordinatesTextView = new TextView(context);
        coordinatesTextView.setText(Lang.getString(R.string.WaitingForLocation));
        coordinatesTextView.setTextSize(16);
        coordinatesTextView.setTextColor(0xFF333333);
        coordinatesTextView.setPadding(Screen.dp(16f), Screen.dp(16f), Screen.dp(16f), Screen.dp(16f));
        coordinatesTextView.setBackgroundResource(R.drawable.bg_round_8);
        infoPanel.addView(coordinatesTextView);
        
        // 精度信息显示
        accuracyTextView = new TextView(context);
        accuracyTextView.setTextSize(14);
        accuracyTextView.setTextColor(0xFF888888);
        accuracyTextView.setPadding(0, Screen.dp(8f), 0, Screen.dp(8f));
        infoPanel.addView(accuracyTextView);

        // 区块信息显示
        blockInfoTextView = new TextView(context);
        blockInfoTextView.setTextSize(12);
        blockInfoTextView.setTextColor(0xFF666666);
        blockInfoTextView.setPadding(Screen.dp(16f), Screen.dp(12f), Screen.dp(16f), Screen.dp(12f));
        blockInfoTextView.setBackgroundResource(R.drawable.bg_round_8);
        blockInfoTextView.setVisibility(View.GONE);
        infoPanel.addView(blockInfoTextView);
        
        // 按钮容器
        LinearLayout buttonContainer = new LinearLayout(context);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        
        // 获取位置按钮
        getLocationButton = new Button(context);
        getLocationButton.setText(Lang.getString(R.string.GetCurrentLocation));
        getLocationButton.setOnClickListener(v -> requestLocation());
        getLocationButton.setPadding(Screen.dp(8f), 0, Screen.dp(8f), 0);
        
        // 复制按钮
        copyButton = new Button(context);
        copyButton.setText(Lang.getString(R.string.CopyLocation));
        copyButton.setOnClickListener(v -> copyCoordinates());
        copyButton.setPadding(Screen.dp(8f), 0, Screen.dp(8f), 0);
        
        // 打开地图按钮
        openMapsButton = new Button(context);
        openMapsButton.setText(Lang.getString(R.string.OpenInGoogleMaps));
        openMapsButton.setOnClickListener(v -> openInMaps());
        openMapsButton.setPadding(Screen.dp(8f), 0, Screen.dp(8f), 0);
        
        // 分享按钮
        shareButton = new Button(context);
        shareButton.setText(Lang.getString(R.string.ShareThisLocation));
        shareButton.setOnClickListener(v -> shareLocation());
        shareButton.setPadding(Screen.dp(8f), 0, Screen.dp(8f), 0);
        
        // 区块分析按钮
        analyzeBlockButton = new Button(context);
        analyzeBlockButton.setText(Lang.getString(R.string.AnalyzeBlock));
        analyzeBlockButton.setOnClickListener(v -> analyzeBlock());
        analyzeBlockButton.setPadding(Screen.dp(8f), 0, Screen.dp(8f), 0);
        
        // 深度分析按钮
        Button deepAnalysisButton = new Button(context);
        deepAnalysisButton.setText(Lang.getString(R.string.DeepSpatialAnalysis));
        deepAnalysisButton.setOnClickListener(v -> performDeepAnalysis());
        deepAnalysisButton.setPadding(Screen.dp(8f), 0, Screen.dp(8f), 0);
        
        // 区块链验证按钮
        Button blockchainValidationButton = new Button(context);
        blockchainValidationButton.setText(Lang.getString(R.string.BlockchainValidation));
        blockchainValidationButton.setOnClickListener(v -> validateBlockchainLocation());
        blockchainValidationButton.setPadding(Screen.dp(8f), 0, Screen.dp(8f), 0);
        
        buttonContainer.addView(getLocationButton);
        buttonContainer.addView(copyButton);
        buttonContainer.addView(openMapsButton);
        buttonContainer.addView(shareButton);
        buttonContainer.addView(analyzeBlockButton);
        buttonContainer.addView(deepAnalysisButton);
        buttonContainer.addView(blockchainValidationButton);
        
        infoPanel.addView(buttonContainer);
        
        contentView.addView(infoPanel);
        
        // 初始化按钮状态
        updateButtonStates(false);
        
        return contentView;
    }
    
    private void requestLocation () {
        if (!GoogleMapsUtils.hasLocationPermissions(getContext())) {
            Toast.makeText(getContext(), 
                Lang.getString(R.string.LocationPermissionRequired), 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        getLocationButton.setEnabled(false);
        getLocationButton.setText(Lang.getString(R.string.GettingLocation));
        
        GoogleMapsUtils.getCurrentLocation(getContext(), new GoogleMapsUtils.LocationResultCallback() {
            @Override
            public void onLocationResult(double latitude, double longitude, float accuracy) {
                UI.post(() -> {
                    // 更新UI显示
                    coordinatesTextView.setText(GoogleMapsUtils.formatCoordinates(latitude, longitude));
                    accuracyTextView.setText(GoogleMapsUtils.formatAccuracy(accuracy));
                    
                    getLocationButton.setEnabled(true);
                    getLocationButton.setText(Lang.getString(R.string.GetCurrentLocation));
                    
                    // 启用操作按钮
                    updateButtonStates(true);
                    
                    // 显示成功提示
                    Toast.makeText(getContext(), 
                        Lang.getString(R.string.LocationSuccess), 
                        Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onLocationError(String errorMessage) {
                UI.post(() -> {
                    coordinatesTextView.setText(errorMessage);
                    accuracyTextView.setText("");
                    
                    getLocationButton.setEnabled(true);
                    getLocationButton.setText(Lang.getString(R.string.GetCurrentLocation));
                    
                    // 禁用操作按钮
                    updateButtonStates(false);
                    
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void copyCoordinates() {
        // 从坐标文本中提取坐标
        String coordinates = coordinatesTextView.getText().toString();
        if (coordinates.contains("纬度") && coordinates.contains("经度")) {
            // 提取坐标值
            String[] parts = coordinates.split(",");
            if (parts.length >= 2) {
                String latPart = parts[0].replace("纬度:", "").trim();
                String lngPart = parts[1].replace("经度:", "").trim();
                
                String coords = latPart + "," + lngPart;
                android.content.ClipboardManager clipboard = 
                    (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setText(coords);
                    Toast.makeText(getContext(), "坐标已复制", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getContext(), "没有可复制的坐标", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openInMaps() {
        String coordinates = coordinatesTextView.getText().toString();
        if (coordinates.contains("纬度") && coordinates.contains("经度")) {
            String[] parts = coordinates.split(",");
            if (parts.length >= 2) {
                String latPart = parts[0].replace("纬度:", "").trim();
                String lngPart = parts[1].replace("经度:", "").trim();
                
                try {
                    double latitude = Double.parseDouble(latPart);
                    double longitude = Double.parseDouble(lngPart);
                    
                    String uri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude;
                    android.content.Intent intent = new Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri));
                    getContext().startActivity(intent);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "坐标格式错误", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getContext(), "没有可用的坐标", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void shareLocation() {
        String coordinates = coordinatesTextView.getText().toString();
        if (coordinates.contains("纬度") && coordinates.contains("经度")) {
            String[] parts = coordinates.split(",");
            if (parts.length >= 2) {
                String latPart = parts[0].replace("纬度:", "").trim();
                String lngPart = parts[1].replace("经度:", "").trim();
                
                String shareText = "📍 当前位置
" +
                                 "纬度: " + latPart + ", 经度: " + lngPart + "
" +
                                 "Google Maps: https://maps.google.com/?q=" + latPart + "," + lngPart;
                
                android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
                getContext().startActivity(android.content.Intent.createChooser(shareIntent, "分享位置"));
            }
        } else {
            Toast.makeText(getContext(), "没有可分享的坐标", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateButtonStates(boolean hasValidLocation) {
        copyButton.setEnabled(hasValidLocation);
        openMapsButton.setEnabled(hasValidLocation);
        shareButton.setEnabled(hasValidLocation);
        analyzeBlockButton.setEnabled(hasValidLocation);
        deepAnalysisButton.setEnabled(hasValidLocation);
    }

    private void performDeepAnalysis() {
        String coordinates = coordinatesTextView.getText().toString();
        if (coordinates.contains("纬度") && coordinates.contains("经度")) {
            String[] parts = coordinates.split(",");
            if (parts.length >= 2) {
                String latPart = parts[0].replace("纬度:", "").trim();
                String lngPart = parts[1].replace("经度:", "").trim();
                
                try {
                    double latitude = Double.parseDouble(latPart);
                    double longitude = Double.parseDouble(lngPart);
                    
                    // 显示加载状态
                    blockInfoTextView.setVisibility(View.VISIBLE);
                    blockInfoTextView.setText("正在进行深度空间分析...");
                    
                    GoogleMapsUtils.performSpatialAnalysis(latitude, longitude, 
                        new GoogleMapsUtils.SpatialAnalysisCallback() {
                            @Override
                            public void onSpatialAnalysisResult(Map<String, Object> analysisResult) {
                                UI.post(() -> {
                                    String analysisReport = GoogleMapsUtils.generateSpatialAnalysisReport(analysisResult);
                                    blockInfoTextView.setText(analysisReport);
                                    Toast.makeText(getContext(), 
                                        "深度空间分析完成", 
                                        Toast.LENGTH_SHORT).show();
                                });
                            }
                            
                            @Override
                            public void onSpatialAnalysisError(String errorMessage) {
                                UI.post(() -> {
                                    blockInfoTextView.setText("深度分析失败: " + errorMessage);
                                    Toast.makeText(getContext(), 
                                        errorMessage, 
                                        Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "坐标格式错误", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getContext(), "没有可用的坐标", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateBlockchainLocation() {
        String coordinates = coordinatesTextView.getText().toString();
        if (coordinates.contains("纬度") && coordinates.contains("经度")) {
            String[] parts = coordinates.split(",");
            if (parts.length >= 2) {
                String latPart = parts[0].replace("纬度:", "").trim();
                String lngPart = parts[1].replace("经度:", "").trim();
                
                try {
                    double latitude = Double.parseDouble(latPart);
                    double longitude = Double.parseDouble(lngPart);
                    
                    // 显示加载状态
                    blockInfoTextView.setVisibility(View.VISIBLE);
                    blockInfoTextView.setText("正在进行区块链地理位置验证...");
                    
                    GoogleMapsUtils.validateBlockchainLocation(latitude, longitude, 
                        new GoogleMapsUtils.BlockchainGeoValidationCallback() {
                            @Override
                            public void onValidationResult(boolean isValid, String validationMessage, Map<String, Object> validationData) {
                                UI.post(() -> {
                                    StringBuilder result = new StringBuilder();
                                    result.append("🔐 区块链地理位置验证
");
                                    result.append("====================
");
                                    result.append("验证结果: ").append(isValid ? "✅ 通过" : "❌ 失败").append("
");
                                    result.append("验证信息: ").append(validationMessage).append("

");
                                    
                                    result.append("📊 验证数据:
");
                                    for (Map.Entry<String, Object> entry : validationData.entrySet()) {
                                        result.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append("
");
                                    }
                                    
                                    blockInfoTextView.setText(result.toString());
                                    Toast.makeText(getContext(), 
                                        isValid ? "验证通过" : "验证失败", 
                                        Toast.LENGTH_SHORT).show();
                                });
                            }
                            
                            @Override
                            public void onValidationError(String errorMessage) {
                                UI.post(() -> {
                                    blockInfoTextView.setText("验证失败: " + errorMessage);
                                    Toast.makeText(getContext(), 
                                        errorMessage, 
                                        Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "坐标格式错误", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getContext(), "没有可用的坐标", Toast.LENGTH_SHORT).show();
        }
    }

    private void analyzeBlock() {
        String coordinates = coordinatesTextView.getText().toString();
        if (coordinates.contains("纬度") && coordinates.contains("经度")) {
            String[] parts = coordinates.split(",");
            if (parts.length >= 2) {
                String latPart = parts[0].replace("纬度:", "").trim();
                String lngPart = parts[1].replace("经度:", "").trim();
                
                try {
                    double latitude = Double.parseDouble(latPart);
                    double longitude = Double.parseDouble(lngPart);
                    
                    // 显示加载状态
                    blockInfoTextView.setVisibility(View.VISIBLE);
                    blockInfoTextView.setText("正在分析区块信息...");
                    
                    GoogleMapsUtils.decodeBlockInfo(latitude, longitude, 
                        new GoogleMapsUtils.BlockInfoCallback() {
                            @Override
                            public void onBlockInfoResult(Map<String, String> blockInfo) {
                                UI.post(() -> {
                                    String analysisReport = GoogleMapsUtils.generateBlockAnalysisReport(blockInfo);
                                    blockInfoTextView.setText(analysisReport);
                                    Toast.makeText(getContext(), 
                                        "区块分析完成", 
                                        Toast.LENGTH_SHORT).show();
                                });
                            }
                            
                            @Override
                            public void onBlockInfoError(String errorMessage) {
                                UI.post(() -> {
                                    blockInfoTextView.setText("区块分析失败: " + errorMessage);
                                    Toast.makeText(getContext(), 
                                        errorMessage, 
                                        Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "坐标格式错误", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getContext(), "没有可用的坐标", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void destroy () {
        super.destroy();
        // 清理资源
    }
    
    @Override
    public void onFocus () {
        super.onFocus();
        setTitle(Lang.getString(R.string.LocationDemoTitle));
    }
}