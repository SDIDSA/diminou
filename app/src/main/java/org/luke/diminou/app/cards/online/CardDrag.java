package org.luke.diminou.app.cards.online;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.View;
import android.widget.ImageView;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.avatar.AvatarDisplay;

public class CardDrag extends View.DragShadowBuilder {

    private final App owner;
    private static Bitmap avatar;

    public CardDrag(App owner, ImageView iv) {
        super(iv);
        this.owner = owner;

        avatar = owner.getBitmapFromView(iv);
    }

    @Override
    public void onProvideShadowMetrics (Point size, Point touch) {
        int pxsize = ViewUtils.dipToPx(AvatarDisplay.preSize, owner);
        size.set(pxsize, pxsize);
        touch.set(pxsize / 2, pxsize / 2);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Paint p = new Paint();

        float scale = 1f;
        int alpha = 255;

        int oSize = canvas.getWidth();
        int size = (int) (oSize * scale);

        int offset = (int) (oSize / 2f - size / 2f);

        int radius = ViewUtils.dipToPx(7, owner);

        p.setColor(owner.getStyle().get().getBackgroundPrimary());
        p.setAlpha(alpha);
        canvas.drawRoundRect(offset,offset,size,size, radius, radius, p);

        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(avatar, offset, offset, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(owner.getStyle().get().getTextMuted());
        p.setAlpha(alpha);
        p.setStrokeWidth(ViewUtils.dipToPx(2, owner));
        canvas.drawRoundRect(offset,offset,size,size, radius, radius, p);
    }
}
