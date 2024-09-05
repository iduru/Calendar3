package com.example.calendar3;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 画面向きを縦に設定
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ViewPager2 viewPager = findViewById(R.id.my_view_pager);
        TabLayout tabLayout = findViewById(R.id.my_tab_layout);

        viewPager.setAdapter(new MyVPAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("メニュー");
                    break;
                case 1:
                    tab.setText("カレンダー");
                    break;
            }
        }).attach();
    }
}
