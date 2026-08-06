package com.tpms.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.syt.tmps.R;

/* JADX INFO: loaded from: classes.dex */
public class SupportMultipleScreensUtil {
    public static final int BASE_SCREEN_HEIGHT = 600;
    public static final float BASE_SCREEN_HEIGHT_FLOAT = 600.0f;
    public static final int BASE_SCREEN_WIDTH = 1024;
    public static final float BASE_SCREEN_WIDTH_FLOAT = 1024.0f;
    public static float scale = 1.0f;

    public static void init(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        int widthPixels = displayMetrics.widthPixels;
        scale = widthPixels / 1024.0f;
    }

    public static int getScaleValue(int value) {
        return (int) Math.ceil(scale * value);
    }

    public static void scale(View view) {
        if (view != null) {
            if (view instanceof ViewGroup) {
                scaleViewGroup((ViewGroup) view);
            } else {
                scaleView(view);
            }
        }
    }

    private static void scaleViewGroup(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ViewGroup) {
                scaleViewGroup((ViewGroup) view);
            }
            scaleView(view);
        }
    }

    private static void scaleView(View view) {
        Object isScale = view.getTag(R.id.is_scale_size_tag);
        if (!(isScale instanceof Boolean) || !((Boolean) isScale).booleanValue()) {
            if (view instanceof TextView) {
                scaleTextView((TextView) view);
            } else {
                scaleViewSize(view);
            }
            view.setTag(R.id.is_scale_size_tag, true);
        }
    }

    public static void scaleViewSize(View view) {
        if (view != null) {
            int paddingLeft = getScaleValue(view.getPaddingLeft());
            int paddingTop = getScaleValue(view.getPaddingTop());
            int paddingRight = getScaleValue(view.getPaddingRight());
            int paddingBottom = getScaleValue(view.getPaddingBottom());
            view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams != null) {
                if (layoutParams.width > 0) {
                    layoutParams.width = getScaleValue(layoutParams.width);
                }
                if (layoutParams.height > 0) {
                    layoutParams.height = getScaleValue(layoutParams.height);
                }
                if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                    int topMargin = getScaleValue(marginLayoutParams.topMargin);
                    int leftMargin = getScaleValue(marginLayoutParams.leftMargin);
                    int bottomMargin = getScaleValue(marginLayoutParams.bottomMargin);
                    int rightMargin = getScaleValue(marginLayoutParams.rightMargin);
                    marginLayoutParams.topMargin = topMargin;
                    marginLayoutParams.leftMargin = leftMargin;
                    marginLayoutParams.bottomMargin = bottomMargin;
                    marginLayoutParams.rightMargin = rightMargin;
                }
            }
            view.setLayoutParams(layoutParams);
        }
    }

    private static void setTextViewCompoundDrawables(TextView textView, Drawable leftDrawable, Drawable topDrawable, Drawable rightDrawable, Drawable bottomDrawable) {
        if (leftDrawable != null) {
            scaleDrawableBounds(leftDrawable);
        }
        if (rightDrawable != null) {
            scaleDrawableBounds(rightDrawable);
        }
        if (topDrawable != null) {
            scaleDrawableBounds(topDrawable);
        }
        if (bottomDrawable != null) {
            scaleDrawableBounds(bottomDrawable);
        }
        textView.setCompoundDrawables(leftDrawable, topDrawable, rightDrawable, bottomDrawable);
    }

    public static Drawable scaleDrawableBounds(Drawable drawable) {
        int right = getScaleValue(drawable.getIntrinsicWidth());
        int bottom = getScaleValue(drawable.getIntrinsicHeight());
        drawable.setBounds(0, 0, right, bottom);
        return drawable;
    }

    public static void scaleTextView(TextView textView) {
        if (textView != null) {
            scaleViewSize(textView);
            Object isScale = textView.getTag(R.id.is_scale_font_tag);
            if (!(isScale instanceof Boolean) || !((Boolean) isScale).booleanValue()) {
                float size = textView.getTextSize();
                textView.setTextSize(0, size * scale);
            }
            Drawable[] drawables = textView.getCompoundDrawables();
            Drawable leftDrawable = drawables[0];
            Drawable topDrawable = drawables[1];
            Drawable rightDrawable = drawables[2];
            Drawable bottomDrawable = drawables[3];
            setTextViewCompoundDrawables(textView, leftDrawable, topDrawable, rightDrawable, bottomDrawable);
            int compoundDrawablePadding = getScaleValue(textView.getCompoundDrawablePadding());
            textView.setCompoundDrawablePadding(compoundDrawablePadding);
        }
    }
}
