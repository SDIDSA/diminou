package org.luke.diminou.abs.components.controls.text.transformationMethods;

import android.graphics.Rect;
import android.text.method.TransformationMethod;
import android.view.View;

public class AllCaps implements TransformationMethod {
    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return source.toString().toUpperCase();
    }

    @Override
    public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect) {
        //do nothing
    }
}
