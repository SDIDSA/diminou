package org.luke.diminou.app.pages.settings;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.widget.LinearLayout;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.button.Button;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.controls.text.transformationMethods.Capitalize;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.abs.utils.functional.StringConsumer;
import org.luke.diminou.abs.utils.functional.StringSupplier;
import org.luke.diminou.data.property.Property;

public class Setting extends Button implements Styleable {
    private final App owner;
    private final Label value;

    private final String key;
    private final StringSupplier get;
    private final StringConsumer set;
    private SettingOverlay overlay;

    private final String[] options;

    private final boolean reset;

    @SuppressLint("RtlHardcoded")
    public Setting(App owner, String key, StringSupplier get, StringConsumer set, boolean reset, String... options) {
        super(owner, key);
        this.reset = reset;
        this.owner = owner;
        this.key = key;
        this.get = get;
        this.set = set;
        this.options = options;
        setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewUtils.dipToPx(56, owner)));
        ViewUtils.setPadding(this, 10, 10, 10, 10, owner);

        content.setGravity(Gravity.CENTER);

        value = new Label(owner, get.get());
        value.setTransformationMethod(new Capitalize());
        addPostLabel(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        addPostLabel(value);

        overlay = setOverlay();

        setOnClick(() -> overlay.show());

        setFont(new Font(14));

        applyStyle(owner.getStyle());
    }

    public SettingOverlay setOverlay() {
        return new SettingOverlay(owner, key, get, v -> {
            boolean success = false;
            while(!success) {
                try {
                    set.accept(v);
                    success = true;
                }catch(Exception x) {
                    ErrorHandler.handle(x, "setting " + key + " to " + v);
                }
            }
            value.setKey(v);
            Page.clearCache();
            if(reset)
                resetOverlay(() -> owner.loadPage(Settings.class, () -> overlay.show()));
        }, options);
    }

    public void resetOverlay(Runnable onDone) {
        overlay.addOnHidden(() -> {
            overlay = setOverlay();
            if (onDone != null)
                onDone.run();
        });
        overlay.hide();
    }

    @Override
    public void applyStyle(Style style) {
        setFill(style.getBackgroundPrimary());
        setTextFill(style.getTextNormal());

        value.setFill(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
