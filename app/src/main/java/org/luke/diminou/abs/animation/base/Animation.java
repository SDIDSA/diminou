package org.luke.diminou.abs.animation.base;

import android.view.View;

import androidx.annotation.NonNull;

import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.combine.SequenceAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.easing.Linear;
import org.luke.diminou.abs.utils.Platform;

public abstract class Animation {
    public static final int INDEFINITE = -1;

    public static final float timeScale = 1f;

    private float fps = 120f;
    private Interpolator interpolator = new Linear();
    private long duration;
    private long lastUpdate;

    private Runnable onFinished;
    private float progress;
    private Thread th;

    private boolean autoReverse = false;
    private int cycleCount = 1;

    private ParallelAnimation parent;

    protected Animation(long duration) {
        this.duration = (long) (duration * timeScale);
    }

    protected Animation() {
        this(0);
    }

    public void init() {

    }

    public void start(long after) {
        Platform.runBack(() -> {
            Platform.sleep(after);
            stop();
            init();
            start(1);
        });
    }

    public void start() {
        stop();
        init();
        start(1);
    }

    protected void start(int rep) {
        th = new Thread("animation_thread_" + getClass().getSimpleName()) {
            public void run() {
                final long start = System.nanoTime();
                while (!Thread.currentThread().isInterrupted()) {
                    long now = System.nanoTime();
                    if (now - lastUpdate >= 0x3b9aca00 / fps) {
                        progress = (now - start) / (float) (duration * 0xf4240);
                        boolean reversed = (rep % 2 == 0 && autoReverse);
                        float to = reversed ? 0 : 1;
                        final float fp = Math.min(Math.max(reversed ? 1 - progress : progress, 0), 1);
                        if ((reversed && fp <= 0) || (!reversed && fp >= 1.0f)) {
                            Platform.runLater(() -> preUpdate(interpolator.interpolate(to)));
                            if (onFinished != null) {
                                Platform.runLater(onFinished);
                            }
                            if (rep < cycleCount || cycleCount == INDEFINITE) {
                                Animation.this.start(rep + 1);
                            }
                            return;
                        } else {
                            Platform.runLater(() -> preUpdate(interpolator.interpolate(fp)));
                        }
                        lastUpdate = now;
                    }else {
                        Platform.sleep(1);
                    }
                }
            }
        };
        th.start();
    }

    private boolean stopped = false;

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void stop() {
        if (th != null && th.isAlive()) {
            th.interrupt();
        }else if(parent != null && parent.isRunning()){
            setStopped(true);
        }
    }

    public void setParent(ParallelAnimation parent) {
        this.parent = parent;
    }

    public boolean isRunning() {
        return (th != null && th.isAlive()) || (parent != null && parent.isRunning());
    }

    @SuppressWarnings("unchecked")
    public <T extends Animation> T setFps(float fps) {
        this.fps = fps;
        return (T) this;
    }

    public long getDuration() {
        return (long) (duration / timeScale);
    }

    @SuppressWarnings("unchecked")
    public <T extends Animation> T setDuration(long duration) {
        this.duration = (long) (duration * timeScale);
        return (T) this;
    }

    public Runnable getOnFinished() {
        return onFinished;
    }

    @SuppressWarnings("unchecked")
    public <T extends Animation> T setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Animation> T setCycleCount(int cycleCount) {
        this.cycleCount = cycleCount;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Animation> T setAutoReverse(boolean autoReverse) {
        this.autoReverse = autoReverse;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Animation> T setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
        return (T) this;
    }

    private void preUpdate(float v) {
        update(v);
    }

    public abstract void update(float v);

    private String toString(int indent) {
        StringBuilder sb = new StringBuilder();

        sb.append('\n');
        for (int i = 0; i < indent; i++) {
            sb.append('\t');
        }
        sb.append(getClass().getSimpleName());

        if (this instanceof ParallelAnimation pa) {
            View target = null;

            for (Animation sa : pa.getAnimations()) {
                if (sa instanceof ViewAnimation) {
                    target = ((ViewAnimation) sa).getView();
                }
            }

            if (target != null) {
                sb.append("\ttarget : ").append(target.getClass().getSimpleName());
            }

            for (Animation sa : pa.getAnimations()) {
                sb.append(sa.toString(indent + 1));
            }
        }

        if (this instanceof SequenceAnimation pa) {
            for (Animation sa : pa.getAnimations()) {
                sb.append(sa.toString(indent + 1));
            }
        }
        return sb.toString();
    }

    @NonNull
    @Override
    public String toString() {
        return toString(0);
    }
}
