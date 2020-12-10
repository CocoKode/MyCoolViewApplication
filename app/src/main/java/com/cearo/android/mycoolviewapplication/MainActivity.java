package com.cearo.android.mycoolviewapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String[] mItems = new String[] {
        "Flip折叠", "小米运动", "健康尺", "即刻点赞", "图片加载与缩放滑动", "仿高德上拉滑动页", "登录页面"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();
    }

    private void initLayout() {
        LinearLayout mainLayout = findViewById(R.id.main_layout);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < mItems.length; i++) {
            RelativeLayout layout = new RelativeLayout(this);
            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            Button button = new Button(this);
            button.setText(mItems[i]);
            button.setTag(i);
            button.setOnClickListener(this);
            layout.addView(button, relativeParams);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
            params.weight = 1;
            mainLayout.addView(layout, params);
        }

    }

    private void startCustomViewActivity(int index) {
        Intent intent = new Intent(this, CustomViewActivity.class);
        intent.putExtra("view_index", index);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        Object tagObj = view.getTag();
        if (tagObj instanceof Integer) {
            int tag = (int) tagObj;
            if (tag == (mItems.length - 2)) {
                startActivity(new Intent(MainActivity.this, AmapActivity.class));
            } else if (tag == (mItems.length - 1)) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            } else {
                startCustomViewActivity(tag);
            }
        }
    }
}
