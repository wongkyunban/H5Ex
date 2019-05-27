package com.wong.h5ex;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // 请求权限代码
    private static int REQUEST_PERMISSION_CODE = 1000;

    // 要申请的权限
    private String[] permissions = {Manifest.permission.INTERNET};
    private AlertDialog dialog;
    // WebView容器
    private FrameLayout contentContainer;
    // 记录访问的URL
    private List<String> urlList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentContainer = (FrameLayout) findViewById(R.id.web_view_main);
        // 当API版本大于23，判断是否已获取权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            int granted = ContextCompat.checkSelfPermission(this, permissions[0]);
            // GRANTED：已授权  DENIED：被拒绝
            if (granted != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户去申请权限
                showDialogTipUserRequestPermission();
            }
        }
        addWeb("http://www.hao123.cn");
    }

    // 提示用户该请求权限的弹出框
    private void showDialogTipUserRequestPermission() {

        new AlertDialog.Builder(this)
                .setTitle("网络权限不可用")
                .setMessage("由于访问互联网需要网络权限；\n否则，您将无法正常使用App")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRequestPermission();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    // 开始请求权限
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE);

    }

    // 用户权限申请的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // 判断用户是否点击了不再提醒。(检测该权限是否还可以申请)
                    boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                    if (!b) {
                        // 用户还是想用我的APP的
                        // 提示用户去应用设置界面手动开启权限
                        showDialogTipUserGoToAppSetting();
                    } else
                        finish();
                } else {
                    Toast.makeText(this, "网络权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 提示用户去应用设置界面手动开启权限

    private void showDialogTipUserGoToAppSetting() {

        dialog = new AlertDialog.Builder(this)
                .setTitle("网络权限不可用")
                .setMessage("请在-应用设置-权限-中，允许App使用网络权限来请求网络数据")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用设置界面
                        goToAppSetting();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    // 跳转到当前应用的设置界面
    private void goToAppSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 1111);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1111) {

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 检查该权限是否已经获取
                int i = ContextCompat.checkSelfPermission(this, permissions[0]);
                // GRANTED：已授权  DENIED：被拒绝
                if (i != PackageManager.PERMISSION_GRANTED) {
                    // 提示用户应该去应用设置界面手动开启权限
                    showDialogTipUserGoToAppSetting();
                } else {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 添加webview
     *
     * @param url
     */
    private void addWeb(String url) {  // 重点在这里，每次都新的URL都会创建一个WebView实例，添加到容器中
        WebView mWeb = new WebView(MainActivity.this);
        WebSettings webSettings = mWeb.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        mWeb.setWebChromeClient(new WebChromeClient());
        mWeb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (!urlList.contains(url)) {
                    addWeb(url);
                    urlList.add(url);
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, request);
                }

            }
        });
        mWeb.loadUrl(url);
        contentContainer.addView(mWeb); // 添加到容器中
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        mWeb.setLayoutParams(params);
    }


    // 返回处理时移除容器的最顶的视图（即当前页面视图）
    @Override
    public void onBackPressed() { // 点击返回按钮事件
        int childCount = contentContainer.getChildCount();
        if (childCount > 1) {
            contentContainer.removeViewAt(childCount - 1);
            urlList.remove(urlList.size() - 1);
        } else {
            super.onBackPressed();
        }

    }
}
