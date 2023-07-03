package org.luke.diminou.abs.animation.combine;

import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.utils.Platform;

import java.util.ArrayList;

public class SequenceAnimation extends Animation {
    private final ArrayList<Animation> animations;
    private Animation running = null;
    private long delay = 0;
    private Thread runner;

    public SequenceAnimation(long duration) {
        super(duration);
        animations = new ArrayList<>();
    }

    public SequenceAnimation setDelay(long delay) {
        this.delay = delay;
        return this;
    }

    public SequenceAnimation addAnimation(Animation animation) {
        animation.setDuration(getDuration());
        this.animations.add(animation);
        return this;
    }

    public ArrayList<Animation> getAnimations() {
        return animations;
    }

    @Override
    public void init() {
        for(Animation animation : animations) {
            animation.init();
        }
        super.init();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Animation> T start() {
        runner = new Thread(() -> {
            for (int i = 0; i < animations.size() - 1 && !Thread.currentThread().isInterrupted(); i++) {
                Animation current = animations.get(i);
                Animation next = animations.get(i + 1);
                current.start();
                running = current;
                Platform.sleep((long) ((getDuration() + delay) * timeScale));
                next.start();
                running = next;
            }
        }, "sequence_animation_thread");
        runner.start();
        return (T) this;
    }

    @Override
    public void stop() {
        if (runner != null && runner.isAlive())
            runner.interrupt();

        if (running != null)
            running.stop();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Animation> T setInterpolator(Interpolator interpolator) {
        for (Animation animation : animations) {
            animation.setInterpolator(interpolator);
        }
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Animation> T setAutoReverse(boolean autoReverse) {
        for (Animation animation : animations) {
            animation.setAutoReverse(autoReverse);
        }
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Animation> T setCycleCount(int cycleCount) {
        for (Animation animation : animations) {
            animation.setCycleCount(cycleCount);
        }
        return (T) this;
    }

    @Override
    public void update(float v) {
        //IGNORE
    }

}
