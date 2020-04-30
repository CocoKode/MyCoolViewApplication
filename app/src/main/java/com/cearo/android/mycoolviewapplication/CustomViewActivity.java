package com.cearo.android.mycoolviewapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cearo.android.mycoolviewapplication.CustomViews.FlipView;
import com.cearo.android.mycoolviewapplication.CustomViews.HealthyRuler;
import com.cearo.android.mycoolviewapplication.CustomViews.hencoder.ImageLoaderAndScaleView;
import com.cearo.android.mycoolviewapplication.CustomViews.XiaomiSportView;
import com.cearo.android.mycoolviewapplication.CustomViews.ThumbUpView;
import com.cearo.android.mycoolviewapplication.CustomViews.hencoder.multiTouchView;

public class CustomViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_view);

        Intent intent = getIntent();
        if (intent != null) {
            int viewIndex = intent.getIntExtra("view_index", -1);
            if (viewIndex != -1) {
                addCustomView(viewIndex);
            }
        }
    }

    private void addCustomView(int viewIndex) {
        FrameLayout layout = findViewById(R.id.custom_layout);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        View view = null;
        switch (viewIndex) {
            case 0:
                view = new FlipView(this, null);
                break;
            case 1:
                view = new XiaomiSportView(this, null);
                break;
            case 2:
                view = new HealthyRuler(this, null);
                break;
            case 3:
                view = new ThumbUpView(this, null);
                break;
            case 4:
                view = new ImageLoaderAndScaleView(this);
                break;
            case 5:
                view = new multiTouchView(this);
            default:
                break;
        }
        if (view != null) {
            layout.addView(view, params);
        }
    }
}
