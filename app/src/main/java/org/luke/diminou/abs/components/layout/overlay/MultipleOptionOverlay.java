package org.luke.diminou.abs.components.layout.overlay;

import android.view.Gravity;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.button.Button;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.ViewUtils;

import java.util.ArrayList;
import java.util.function.Function;

public class MultipleOptionOverlay extends PartialSlideOverlay {
    protected final VBox root;
    private final Label text;
    private final ArrayList<Button> buttons;

    private final Function<String, Boolean> isSelected;

    public MultipleOptionOverlay(App owner, String header, Function<String, Boolean> isSelected) {
        super(owner, -2);

        this.isSelected = isSelected;

        root = new VBox(owner);
        root.setSpacing(10);
        root.setPadding(20);
        root.setGravity(Gravity.TOP | Gravity.CENTER);

        text = new Label(owner, header);
        text.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        text.setFont(new Font(20));
        text.setLineSpacing(10);
        ViewUtils.setMarginBottom(text, owner, 20);

        buttons = new ArrayList<>();

        root.addView(text);

        list.addView(root);

        applyStyle(owner.getStyle().get());
    }

    public void addButton(String text, Runnable onClick) {
        Button button = new Button(owner, text);
        root.addView(button);
        buttons.add(button);
        button.setOnClick(onClick);

        applyStyle(owner.getStyle().get());
    }

    @Override
    public void applyStyle(Style style) {
        if(root == null) return;
        super.applyStyle(style);

        text.setFill(style.getTextNormal());

        for (Button button : buttons) {
            if(isSelected.apply(button.getKey())) {
                button.setFill(style.getBackgroundTertiary());
                button.setTextFill(style.getTextNormal());
            }else {
                button.setFill(style.getBackgroundPrimary());
                button.setTextFill(style.getTextNormal());
            }
        }
    }
}
