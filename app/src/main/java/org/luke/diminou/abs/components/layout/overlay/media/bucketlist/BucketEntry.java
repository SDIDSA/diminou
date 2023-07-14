package org.luke.diminou.abs.components.layout.overlay.media.bucketlist;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.image.Image;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.media.Bucket;
import org.luke.diminou.data.property.Property;

public class BucketEntry extends HBox implements Styleable {
    private final Label name;
    private final Label count;
    private final Image thumb;

    public BucketEntry(App owner) {
        super(owner);
        ViewUtils.setPaddingUnified(this, 10, owner);
        setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.CENTER);

        setFocusable(true);
        setClickable(true);

        VBox text = new VBox(owner);
        text.setSpacing(6);

        name = new Label(owner, "");
        name.setFont(new Font(16));
        count = new Label(owner, "");
        count.setFont(new Font(14));

        text.addView(name);
        text.addView(count);

        thumb = new Image(owner);
        thumb.setSize(64);
        thumb.setCornerRadius(7);
        thumb.setFocusable(false);
        thumb.setClickable(false);

        addView(text);
        addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        addView(thumb);

        setBackground(new GradientDrawable());

        applyStyle(owner.getStyle());
    }

    public void load(Bucket data) {
        name.setText(data.getName());
        count.setText(String.valueOf(data.getItems().size()).concat(" items"));

        if (data.getItems().size() > 0)
            data.getItems().get(0)
                    .getThumbnail(
                            getOwner(),
                            ViewUtils.dipToPx(64, getOwner()),
                            thumb::setImageBitmap,
                            () -> thumb.setImageResource(R.drawable.problem)
                    );
    }

    @Override
    public void applyStyle(Style style) {
        name.setTextColor(style.getTextNormal());
        count.setTextColor(style.getTextMuted());

        setOnTouchListener((view, action) -> {
            switch (action.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setBackgroundColor(App.adjustAlpha(style.getTextNormal(), .2f));
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                case MotionEvent.ACTION_CANCEL:
                    setBackgroundColor(Color.TRANSPARENT);
                    break;
            }
            return true;
        });
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}