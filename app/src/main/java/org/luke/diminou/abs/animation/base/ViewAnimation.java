package org.luke.diminou.abs.animation.base;

import android.view.View;

import org.luke.diminou.abs.utils.Platform;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ViewAnimation extends ValueAnimation {
    private static final ConcurrentHashMap<String, ConcurrentHashMap<View, ViewAnimation>> running = new ConcurrentHashMap<>();

    static {
        Platform.runBack(ViewAnimation::clear);
    }

    private static void clear() {
        Platform.sleep(10000);
        running.values().forEach(val -> {
            ArrayList<View> toClear = new ArrayList<>();
            val.keySet().forEach(view -> {
                if(!view.isAttachedToWindow()) {
                    toClear.add(view);
                }
            });
            toClear.forEach(val::remove);
        });
        clear();
    }

    private final View view;

    public ViewAnimation(long duration, View view, float from, float to) {
        super(duration, from, to);
        this.view = view;
    }

    public ViewAnimation(View view, float from, float to) {
        this(0, view, from, to);
    }

    public ViewAnimation(View view, float to) {
        this(view, Float.MIN_VALUE, to);
    }

    public ViewAnimation(long duration, View view, float to) {
        this(duration, view, Float.MIN_VALUE, to);
    }

    public View getView() {
        return view;
    }

    private float initialFrom = Float.MAX_VALUE;
    @Override
    public void init() {
        super.init();
        setStopped(false);
        if(lateFrom == null && (getFrom() == Float.MIN_VALUE || initialFrom == Float.MIN_VALUE)) {
            initialFrom = Float.MIN_VALUE;
            setFrom(getFrom(view));
        }

        ConcurrentHashMap<View, ViewAnimation> forType = running.get(getClass().getSimpleName());
        if(forType != null) {
            ViewAnimation forView = forType.get(view);
            if(forView != null && forView.isRunning() && forView != this && forView.getView() == view) {
                forView.stop();
            }
        } else {
            forType = new ConcurrentHashMap<>();
            running.put(getClass().getSimpleName(), forType);
        }

        forType.put(view, this);
    }

    @Override
    public void updateValue(float v) {
        if(isStopped()) {
            return;
        }
        if (view.isAttachedToWindow()) {
            apply(view, v);
        } else {
            apply(view, getTo());
            stop();
            if (getOnFinished() != null) {
                Platform.runLater(getOnFinished());
            }
        }
    }

    protected abstract float getFrom(View view);

    protected abstract void apply(View view, float v);
}
