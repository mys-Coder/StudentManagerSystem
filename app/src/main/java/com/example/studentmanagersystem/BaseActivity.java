package com.example.studentmanagersystem;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

// 所有页面的基类：统一适配 Android 15+ 边到边显示
// 自动在顶部留出状态栏高度、底部留出导航栏高度，并把系统栏图标设为深色
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 页面整体是浅色背景：状态栏 / 导航栏图标统一用深色，避免白底白图标看不见
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        if (!fitSystemBars()) return; // 登录页自己处理
        View content = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.statusBars()
                            | WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, bars.top, 0, bars.bottom);
            return insets;
        });
    }

    // 登录页需要壁纸全屏铺到系统栏后面，返回 false 自行安排内部边距
    protected boolean fitSystemBars() {
        return true;
    }
}
