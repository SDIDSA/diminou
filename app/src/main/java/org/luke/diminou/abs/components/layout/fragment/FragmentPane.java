package org.luke.diminou.abs.components.layout.fragment;

import android.widget.FrameLayout;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.position.TranslateXAnimation;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.ViewUtils;

public class FragmentPane extends FrameLayout {
    private final App owner;
    Class<? extends Fragment> type;
    Animation running = null;
    private Fragment loaded;

    public FragmentPane(App owner, Class<? extends Fragment> type) {
        super(owner);
        this.owner = owner;
        this.type = type;
        setClipChildren(false);
        Fragment.clearCache(type);
    }
    public void nextInto(Class<? extends Fragment> pageType) {
        navigateInto(pageType, 1);
    }

    public void previousInto(Class<? extends Fragment> pageType) {
        navigateInto(pageType, -1);
    }

    private void navigateInto(Class<? extends Fragment> fragmentType, int direction) {
        owner.hideKeyboard();
        if (isRunning())
            return;

        new Thread(() -> {
            Fragment old = loaded;
            if (old != null && fragmentType.isInstance(old)) return;
            Fragment nw = Fragment.getInstance(owner, fragmentType);
            if (nw == null) return;

            loaded = nw;

            Platform.runLater(() -> {
                float fromX = old == null ? 0 : (owner.getScreenWidth() * direction) / (direction == 1 ? 1f : 2f);
                float toX = -(owner.getScreenWidth() * direction) / (direction == 1 ? 2f : 1f);
                loaded.setTranslationX(fromX);
                removeView(loaded);
                addView(loaded, direction == -1 ? 0 : old == null ? 0 : 1);
                loaded.setup(direction == 1);
                ParallelAnimation trans = new ParallelAnimation(400)
                        .addAnimation(new TranslateXAnimation(loaded, fromX, 0))
                        .setInterpolator(Interpolator.EASE_OUT);
                if (old != null) {
                    old.destroy(direction == 1);
                    trans.addAnimation(new TranslateXAnimation(old, 0, toX));
                    trans.setOnFinished(() -> removeView(old));
                }
                running = trans;
                running.start();
            });
        }).start();
    }

    public boolean isRunning() {
        return running != null && running.isRunning();
    }
}
