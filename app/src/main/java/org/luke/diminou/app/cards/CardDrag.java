package org.luke.diminou.app.cards;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.avatar.Avatar;
import org.luke.diminou.app.avatar.AvatarDisplay;

public class CardDrag extends View.DragShadowBuilder {

    private final App owner;
    private static int avatar;

    public CardDrag(App owner, PlayerCard card) {
        super(card);
        this.owner = owner;

        avatar = Avatar.valueOf(card.getAvatar()).getRes();
    }

    // Define a callback that sends the drag shadow dimensions and touch point
// back to the system.
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
        canvas.drawBitmap(getBitmap(avatar, size), offset, offset, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(owner.getStyle().get().getTextMuted());
        p.setAlpha(alpha);
        p.setStrokeWidth(ViewUtils.dipToPx(2, owner));
        canvas.drawRoundRect(offset,offset,size,size, radius, radius, p);


    }

    private Bitmap getBitmap(int drawableRes, int size) {
        Drawable drawable = ResourcesCompat.getDrawable(owner.getResources(), drawableRes, null);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        assert drawable != null;
        drawable.setBounds(0, 0, size, size);
        drawable.draw(canvas);

        return bitmap;
    }
}
