package com.tpms.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.syt.tmps.R;

/* JADX INFO: loaded from: classes.dex */
public class ClickToast {
    private boolean closeBtnEvent = false;
    private String guid = "";
    private Context mContext;
    private WindowManager.LayoutParams mParams;
    private int mStartX;
    private int mStartY;
    private Button mTvAddress;
    private View mView;
    private WindowManager mWM;

    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String id) {
        this.guid = id;
    }

    public void initToast(Context context, View groupview, String txt) {
        this.mContext = context;
        this.mWM = (WindowManager) context.getSystemService("window");
        this.mParams = new WindowManager.LayoutParams();
        this.mParams.height = -2;
        this.mParams.width = -1;
        this.mParams.x = 0;
        this.mParams.y = 0;
        this.mParams.format = -3;
        this.mParams.type = 2003;
        this.mParams.flags = 136;
        this.mParams.gravity = 49;
        this.mView = groupview;
        if (this.closeBtnEvent) {
            this.mView.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() { // from class: com.tpms.widget.ClickToast.1
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    ClickToast.this.hideCustomToast();
                }
            });
        }
        TextView tv = (TextView) this.mView.findViewById(R.id.txt_view);
        tv.setText(txt);
        this.mView.setLayoutParams(this.mParams);
        this.mView.setOnTouchListener(new View.OnTouchListener() { // from class: com.tpms.widget.ClickToast.2
            int lastX;
            int lastY;
            int paramX;
            int paramY;

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == 0) {
                    this.lastX = (int) event.getRawX();
                    this.lastY = (int) event.getRawY();
                    this.paramX = ClickToast.this.mParams.x;
                    this.paramY = ClickToast.this.mParams.y;
                    return true;
                }
                if (action == 2) {
                    int dx = ((int) event.getRawX()) - this.lastX;
                    int dy = ((int) event.getRawY()) - this.lastY;
                    ClickToast.this.mParams.x = this.paramX + dx;
                    ClickToast.this.mParams.y = this.paramY + dy;
                    ClickToast.this.mWM.updateViewLayout(ClickToast.this.mView, ClickToast.this.mParams);
                    return true;
                }
                return true;
            }
        });
    }

    public static ClickToast makeToast(Context cont, View view, String txt) {
        ClickToast toast = new ClickToast();
        toast.initToast(cont, view, txt);
        return toast;
    }

    public static ClickToast makeToast(Context cont, int layoutid, String txt) {
        ClickToast toast = new ClickToast();
        toast.closeBtnEvent = true;
        View view = LayoutInflater.from(cont).inflate(layoutid, (ViewGroup) null);
        toast.initToast(cont, view, txt);
        return toast;
    }

    public void show() {
        this.mWM.addView(this.mView, this.mParams);
    }

    public void hideCustomToast() {
        if (this.mView != null) {
            if (this.mView.getParent() != null) {
                this.mWM.removeView(this.mView);
            }
            this.mView = null;
        }
    }
}
