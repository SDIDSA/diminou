package org.luke.diminou.abs.animation.combine;

import android.util.Log;

import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.functional.FloatConsumer;

import java.util.ArrayList;

public class ParallelAnimation extends Animation {
    private final ArrayList<Animation> animations;

    private Runnable onFinish;

    private FloatConsumer onUpdate;

    public ParallelAnimation() {
        this(200);
    }

    public ParallelAnimation(long duration) {
        super(duration);

        animations = new ArrayList<>();

        super.setOnFinished(() -> {
           for(Animation animation : animations) {
               Runnable onFinished = animation.getOnFinished();
               if(onFinished != null)
                   onFinished.run();
           }
           if(onFinish != null) onFinish.run();
        });
    }

    public ParallelAnimation setOnUpdate(FloatConsumer onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Animation> T setOnFinished(Runnable onFinished) {
        onFinish = onFinished;
        return (T) this;
    }

    public ParallelAnimation addAnimations(Animation...animations) {
        for(Animation a : animations) {
            addAnimation(a);
        }
        return this;
    }

    public ParallelAnimation addAnimation(Animation animations) {
        this.animations.add(animations);
        animations.setParent(this);
        return this;
    }

    public ParallelAnimation removeAnimation(Animation animations) {
        this.animations.remove(animations);
        return this;
    }

    public ArrayList<Animation> getAnimations() {
        return animations;
    }

    @Override
    public void init() {
        for (Animation a : animations) {
            a.init();
        }
        super.init();
    }

    @Override
    public void update(float v) {
        for (Animation a : animations) {
                a.update(v);
        }
        if(onUpdate != null) {
            try {
                onUpdate.accept(v);
            } catch (Exception e) {
                ErrorHandler.handle(e, "running parallel animation");
            }
        }
    }
}
