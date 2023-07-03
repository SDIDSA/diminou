package org.luke.diminou.abs.components.controls.input;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.functional.StringConsumer;
import org.luke.diminou.data.property.StringProperty;

public class InputUtils {
    public static void setChangeListener(EditText input, StringConsumer onChange) {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence ov, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence nv, int i, int i1, int i2) {
                try {
                    onChange.accept(String.valueOf(nv));
                } catch (Exception x) {
                    ErrorHandler.handle(x, "set change listener on input");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    public static void bindToProperty(EditText input, StringProperty property) {
        setChangeListener(input, property::set);
    }
}
