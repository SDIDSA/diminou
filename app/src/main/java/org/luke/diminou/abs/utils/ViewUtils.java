package org.luke.diminou.abs.utils;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.GravityInt;

import org.luke.diminou.abs.components.controls.scratches.Orientation;

public class ViewUtils {
    public static float scale = 1f;

    public static void setPaddingUnified(View view, float padding, Context context) {
        setPadding(view, padding, padding, padding, padding, context);
    }

    public static void setPadding(View view, float left, float top, float right, float bottom, Context context) {
        int dil = dipToPx(left, context);
        int dit = dipToPx(top, context);
        int dir = dipToPx(right, context);
        int dib = dipToPx(bottom, context);
        view.setPadding(dil, dit, dir, dib);
    }

    public static void spacer(View view) {
        int width = view.getLayoutParams() != null ? view.getLayoutParams().width : LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = view.getLayoutParams() != null ? view.getLayoutParams().height : LinearLayout.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.weight = 1;
        view.setLayoutParams(params);
    }

    public static View spacer(Context context, Orientation orientation) {
        try {
            boolean hor = orientation == Orientation.HORIZONTAL;
            View view = new View(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    hor ? LinearLayout.LayoutParams.WRAP_CONTENT : 1,
                    hor ? 1 : LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            view.setLayoutParams(params);
            return view;
        }catch (Exception x) {
            ErrorHandler.handle(x, "creating spacer");
            return null;
        }
    }

    public static float pxToDip(int input, Context context) {
        return pxToDip((float) input, context);
    }

    public static float pxToDip(float px, Context context) {
        return px / (context.getResources().getDisplayMetrics().density * scale);
    }

    public static int dipToPx(int input, Context context) {
        return (int) (dipToPx((float) input, context) + .5);
    }

    public static int dipToPx(float input, Context context) {
        return (int) (input * context.getResources().getDisplayMetrics().density * scale);
    }

    public static void setMarginTop(View view, Context context, float val) {
        setMargin(view, context, 0, val, 0, 0);
    }

    public static void setMarginRight(View view, Context context, float val) {
        setMargin(view, context, 0, 0, val, 0);
    }

    public static void setMarginBottom(View view, Context context, float val) {
        setMargin(view, context, 0, 0, 0, val);
    }

    public static void setMargin(View view, Context context, float left, float top, float right, float bottom) {
        try {
            ViewGroup.LayoutParams old = view.getLayoutParams();
            if (old instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams marginLayoutParams = duplicateLinearLayoutParams((LinearLayout.LayoutParams) old);
                marginLayoutParams.setMargins(dipToPx(left, context), dipToPx(top, context), dipToPx(right, context), dipToPx(bottom, context));

                marginLayoutParams.setMarginStart(dipToPx(left, context));
                marginLayoutParams.setMarginEnd(dipToPx(right, context));

                view.setLayoutParams(marginLayoutParams);
            } else {
                ViewGroup.MarginLayoutParams marginLayoutParams = duplicateViewGroupParams(old);
                marginLayoutParams.setMargins(dipToPx(left, context), dipToPx(top, context), dipToPx(right, context), dipToPx(bottom, context));

                marginLayoutParams.setMarginStart(dipToPx(left, context));
                marginLayoutParams.setMarginEnd(dipToPx(right, context));

                view.setLayoutParams(marginLayoutParams);
            }
        }catch(Exception x) {
            ErrorHandler.handle(x, "setting margin");
        }
    }

    @SuppressWarnings("deprecation")
    public static float pxToSp(float px, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= 34) {
            return TypedValue.convertPixelsToDimension(COMPLEX_UNIT_SP, px, metrics) / scale;
        } else {
            return px / (context.getResources().getDisplayMetrics().scaledDensity * scale);
        }
    }

    @SuppressWarnings("deprecation")
    public static float spToPx(float sp, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= 34) {
            return TypedValue.convertDimensionToPixels(COMPLEX_UNIT_SP, sp, metrics) * scale;
        } else {
            return sp * context.getResources().getDisplayMetrics().scaledDensity * scale;
        }
    }

    private static LinearLayout.LayoutParams duplicateLinearLayoutParams(LinearLayout.LayoutParams old) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.MarginLayoutParams.WRAP_CONTENT,
                LinearLayout.MarginLayoutParams.WRAP_CONTENT
        );

        if (old != null) {
            params.height = old.height;
            params.width = old.width;

            params.weight = old.weight;
            params.gravity = old.gravity;
            params.bottomMargin = old.bottomMargin;
            params.topMargin = old.topMargin;
            params.rightMargin = old.rightMargin;
            params.leftMargin = old.leftMargin;
        }

        return params;
    }

    public static void alignInFrame(View view, @GravityInt int gravity) {
        try {
            ViewGroup.LayoutParams old = view.getLayoutParams();
            FrameLayout.LayoutParams n = new FrameLayout.LayoutParams(
                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                    ViewGroup.MarginLayoutParams.WRAP_CONTENT
            );

            if(old != null) {
                n.width = old.width;
                n.height = old.height;
            }
            n.gravity = gravity;
            view.setLayoutParams(n);
        }catch(Exception x) {
            ErrorHandler.handle(x, "aligning child in frame");
        }
    }

    private static ViewGroup.MarginLayoutParams duplicateViewGroupParams(ViewGroup.LayoutParams old) {
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                ViewGroup.MarginLayoutParams.WRAP_CONTENT
        );

        if (old != null) {
            params.height = old.height;
            params.width = old.width;

            if (old instanceof ViewGroup.MarginLayoutParams marginedOld) {
                params.bottomMargin = marginedOld.bottomMargin;
                params.topMargin = marginedOld.topMargin;
                params.rightMargin = marginedOld.rightMargin;
                params.leftMargin = marginedOld.leftMargin;
            }
        }

        return params;
    }

    public static int dipToPx(Double aDouble, Context owner) {
        return (int) dipToPx(aDouble.floatValue(), owner);
    }
}
