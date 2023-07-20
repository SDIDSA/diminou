package org.luke.diminou.abs.components.layout.overlay.media.bucketlist;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.media.Bucket;

public class BucketHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final BucketEntry entry;
    private Bucket data;

    private ObjectConsumer<Bucket> onAction;

    public BucketHolder(BucketEntry entry) {
        super(entry);
        entry.setOnClickListener(this);
        this.entry = entry;
    }

    public void load(Bucket data) {
        this.data = data;
        entry.load(data);
    }

    @Override
    public void onClick(View view) {
        if(onAction != null && data != null) {
            try {
                onAction.accept(data);
            } catch (Exception x) {
                ErrorHandler.handle(x , "handle bucket selection");
            }
        }
    }

    public void setOnAction(ObjectConsumer<Bucket> onAction) {
        this.onAction = onAction;
    }
}
