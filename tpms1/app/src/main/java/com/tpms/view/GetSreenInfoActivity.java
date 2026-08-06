package com.tpms.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import com.syt.tmps.R;

/* JADX INFO: loaded from: classes.dex */
public class GetSreenInfoActivity extends Activity {
    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        Button getBtn = (Button) findViewById(R.id.main_btn);
        final EditText widthPxText = (EditText) findViewById(R.id.main_et_width_px);
        final EditText heightPxText = (EditText) findViewById(R.id.main_et_height_px);
        final EditText densityText = (EditText) findViewById(R.id.main_et_density);
        final EditText densityDpiText = (EditText) findViewById(R.id.main_et_density_dpi);
        final EditText widthDipText = (EditText) findViewById(R.id.main_et_width_dip);
        final EditText heightDipText = (EditText) findViewById(R.id.main_et_height_dip);
        getBtn.setOnClickListener(new View.OnClickListener() { // from class: com.tpms.view.GetSreenInfoActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                WindowManager wm = GetSreenInfoActivity.this.getWindowManager();
                int widthPx = wm.getDefaultDisplay().getWidth();
                int heightPx = wm.getDefaultDisplay().getHeight();
                widthPxText.setText(widthPx + "");
                heightPxText.setText(heightPx + "");
                float density = GetSreenInfoActivity.this.getResources().getDisplayMetrics().density;
                float densityDpi = (float) GetSreenInfoActivity.this.getResources().getDisplayMetrics().densityDpi;
                densityText.setText(density + "");
                densityDpiText.setText(densityDpi + "");
                int widthDip = GetSreenInfoActivity.this.pxToDip(GetSreenInfoActivity.this, (float) widthPx);
                int heightDip = GetSreenInfoActivity.this.pxToDip(GetSreenInfoActivity.this, (float) heightPx);
                widthDipText.setText(widthDip + "");
                heightDipText.setText(heightDip + "");
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int pxToDip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((pxValue / scale) + 0.5f);
    }

    public int dipToPx(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dipValue * scale) + 0.5f);
    }
}
