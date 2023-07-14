package org.luke.diminou.abs.components.layout.overlay.media.medialist;

import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.media.Media;

import java.util.List;

public class MediaList extends RecyclerView {
    private final MediaAdapter adapter;

    private ObjectConsumer<Media> onAction;
    public MediaList(App owner) {
        super(owner);

        setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        //ViewUtils.spacer(owner, this);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(true);

        GridLayoutManager lm = new GridLayoutManager(owner, 4);
        lm.setSmoothScrollbarEnabled(true);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        setLayoutManager(lm);

        int size = owner.getScreenWidth() - ViewUtils.dipToPx(50, owner);
        Log.i("size", size + "");
        adapter = new MediaAdapter(owner, size);
        adapter.setOnAction(media -> {
            if(onAction != null) {
                onAction.accept(media);
            }
        });
        setAdapter(adapter);

        GradientDrawable clip = new GradientDrawable();
        clip.setCornerRadius(ViewUtils.dipToPx(7, owner));
        setBackground(clip);
        setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        setClipToOutline(true);

        setClipToPadding(false);
    }

    public void setOnAction(ObjectConsumer<Media> onAction) {
        this.onAction = onAction;
    }

    public void setData(List<Media> data) {
        adapter.setData(data);
    }
}