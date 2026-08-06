package com.tpms.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.syt.tmps.R;

/* JADX INFO: loaded from: classes.dex */
public class CDialog extends Dialog {
    LayoutInflater inflater;
    View mView;

    public CDialog(Context context, View view) {
        super(context, R.style.DialogStyle);
        this.inflater = LayoutInflater.from(context);
        initView(view);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    private void initView(View view) {
        if (view == null) {
            return;
        }
        this.mView = view;
        View btn = this.mView.findViewById(R.id.close_btn);
        setContentView(view);
        if (btn == null) {
            return;
        }
        btn.setOnClickListener(new View.OnClickListener() { // from class: com.tpms.widget.CDialog.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                CDialog.this.dismiss();
            }
        });
    }

    public CDialog(Context context, int layid) {
        this(context, (View) null);
        this.inflater = LayoutInflater.from(context);
        View view = this.inflater.inflate(layid, (ViewGroup) null);
        initView(view);
    }

    public void show(String txt) {
        TextView tv = (TextView) this.mView.findViewById(R.id.txt_view);
        tv.setText(txt);
        super.show();
    }
}
