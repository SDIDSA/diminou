package org.luke.diminou.abs.components.layout.overlay.media.bucketlist;

import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.media.Bucket;

import java.util.List;

public class BucketList extends RecyclerView {
    private final BucketAdapter adapter;

    private ObjectConsumer<Bucket> onAction;
    public BucketList(App owner) {
        super(owner);

        setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(true);

        LinearLayoutManager lm = new LinearLayoutManager(owner);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        setLayoutManager(lm);

        adapter = new BucketAdapter(owner);
        adapter.setOnAction(bucket -> {
            if(onAction != null) {
                onAction.accept(bucket);
            }
        });
        setAdapter(adapter);

        setClipToPadding(false);
    }

    public void setOnAction(ObjectConsumer<Bucket> onAction) {
        this.onAction = onAction;
    }

    public void setData(List<Bucket> data) {
        adapter.setData(data);
    }
}