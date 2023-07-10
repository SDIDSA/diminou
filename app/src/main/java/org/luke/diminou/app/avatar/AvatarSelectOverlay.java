package org.luke.diminou.app.avatar;

import android.graphics.Color;
import android.view.Gravity;
import org.luke.diminou.abs.components.layout.StackPane;
import android.widget.ScrollView;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.shape.Rectangle;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.components.layout.overlay.PartialSlideOverlay;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.abs.utils.ViewUtils;

public class AvatarSelectOverlay extends PartialSlideOverlay {
    private final Label text;
    private final Runnable onDone;
    public AvatarSelectOverlay(App owner, Runnable onDone) {
        super(owner, .6);
        this.onDone = onDone;

        VBox root = new VBox(owner);
        root.setSpacing(20);
        root.setPadding(10);
        root.setGravity(Gravity.TOP | Gravity.CENTER);

        text = new Label(owner, "select_avatar");
        text.setFont(new Font(18));

        root.addView(text);

        root.addView(category(Avatar.shapes(), false));
        root.addView(category(Avatar.micahs(), true));
        root.addView(category(Avatar.smiles(), true));
        root.addView(category(Avatar.fulls(), true));
        root.addView(category(Avatar.avatars(), false));

        ScrollView sv = new ScrollView(owner);
        sv.addView(root);
        list.addView(sv);

        applyStyle(owner.getStyle());
    }

    private StackPane category(Avatar[] avatars, boolean locked) {
        StackPane fl = new StackPane(owner);

        VBox res = new VBox(owner);
        res.setSpacing(10);

        for(int i = 0; i < (locked ? 3 : 5); i++) {
            HBox hbx = new HBox(owner);
            res.addView(hbx);
            for(int j = 0; j < 4; j++) {
                if(j != 0) hbx.addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
                AvatarDisplay ad = new AvatarDisplay(owner);
                Avatar a = avatars[i * 4 + j];
                ad.setValue(a);
                hbx.addView(ad);

                ad.setOnClick(() -> Store.setAvatar(a.name(), s -> {hide(); onDone.run();}));
            }
        }
        fl.addView(res);

        if(locked) {
            res.setScaleX(.85f);
            res.setScaleY(.85f);
            res.setAlpha(.3f);
            Rectangle overlay = new Rectangle(owner);
            overlay.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            overlay.setRadius(7);
            overlay.setFill(Color.BLACK);
            overlay.setAlpha(.3f);
            overlay.setOnClickListener(e -> {
                //ABSORB
            });

            ColoredIcon lock = new ColoredIcon(owner, Style::getTextNormal, R.drawable.lock);
            lock.setSize(82);
            lock.center();

            fl.addView(overlay);
            fl.addView(lock);
        }

        return fl;
    }

    @Override
    public void applyStyle(Style style) {
        if(text == null) return;
        super.applyStyle(style);

        text.setFill(style.getTextNormal());
    }
}
