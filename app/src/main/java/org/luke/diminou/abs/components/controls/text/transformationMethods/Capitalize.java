package org.luke.diminou.abs.components.controls.text.transformationMethods;

import android.graphics.Rect;
import android.text.method.TransformationMethod;
import android.view.View;

public class Capitalize implements TransformationMethod {
    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return source.length() == 0 ? "" : Character.toUpperCase(source.charAt(0)) + source.subSequence(1, source.length()).toString().toLowerCase();
    }

    @Override
    public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect) {
        //do nothing
    }
}
