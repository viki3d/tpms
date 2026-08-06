package com.tpms.widget;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.syt.tmps.R;

/* JADX INFO: loaded from: classes.dex */
public class PAlertDialog {
    private static String TAG = "PAlertDialog";

    public static AlertDialog showDiolg(Context context, String txt) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.progress_dialog, (ViewGroup) null);
        TextView tv = (TextView) view.findViewById(R.id.tv_txt_wait);
        if (tv != null && !TextUtils.isEmpty(txt)) {
            tv.setText(txt);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.create();
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.tpms.widget.PAlertDialog.1
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialog) {
                Log.i(PAlertDialog.TAG, "onCancel");
            }
        });
        AlertDialog dlg = builder.show();
        return dlg;
    }

    public static ProgressDialog showProgress(Context context, String str1, String str2) {
        return ProgressDialog.show(context, str1, str2, true, false);
    }
}
