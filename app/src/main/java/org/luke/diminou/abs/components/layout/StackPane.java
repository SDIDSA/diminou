package org.luke.diminou.abs.components.layout;

import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.ErrorHandler;

public class StackPane extends FrameLayout {
    public StackPane(App owner) {
        super(owner);
    }

    @Override
    public void addView(View child) {
        if(Thread.currentThread() != Looper.getMainLooper().getThread() && isAttachedToWindow())
            ErrorHandler.handle(new RuntimeException("modifying ui from the wrong thread"), "adding view to stackPane");
        super.addView(child);
    }

    @Override
    public void removeView(View view) {
        if(Thread.currentThread() != Looper.getMainLooper().getThread() && isAttachedToWindow())
            ErrorHandler.handle(new RuntimeException("modifying ui from the wrong thread"), "adding view to stackPane");
        super.removeView(view);
    }

    @Override
    public void removeAllViews() {
        if(Thread.currentThread() != Looper.getMainLooper().getThread() && isAttachedToWindow())
            ErrorHandler.handle(new RuntimeException("modifying ui from the wrong thread"), "adding view to stackPane");
        super.removeAllViews();
    }

    @Override
    public void addView(View child, int index) {
        if(Thread.currentThread() != Looper.getMainLooper().getThread() && isAttachedToWindow())
            ErrorHandler.handle(new RuntimeException("modifying ui from the wrong thread"), "adding view to stackPane");
        super.addView(child, index);
    }
}
