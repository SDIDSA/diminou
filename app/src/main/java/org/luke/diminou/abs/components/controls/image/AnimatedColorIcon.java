package org.luke.diminou.abs.components.controls.image;

import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.Platform;

public class AnimatedColorIcon extends ColorIcon {
    private final int audio;
    public AnimatedColorIcon(App owner, int id, int audio) {
        super(owner, id);
        this.audio = audio;
    }

    public void start() {
        start(1);
    }

    public void start(int count) {
        owner.playSound(audio);
        Platform.runLater(() ->
        {
            if(count > 1) {
                ((AnimatedVectorDrawable)getDrawable()).registerAnimationCallback(new Animatable2.AnimationCallback() {
                    @Override
                    public void onAnimationEnd(Drawable drawable) {
                        start(count - 1);
                        ((AnimatedVectorDrawable)getDrawable()).unregisterAnimationCallback(this);
                    }
                });
            }
            ((AnimatedVectorDrawable)getDrawable()).reset();
            ((AnimatedVectorDrawable)getDrawable()).start();
        });
    }

    public void setOnFinished(Runnable post) {
        Platform.runLater(() -> {
            ((AnimatedVectorDrawable)getDrawable()).registerAnimationCallback(new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    post.run();
                }
            });
        });
    }
}
