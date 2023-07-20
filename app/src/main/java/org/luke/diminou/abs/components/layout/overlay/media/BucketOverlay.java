package org.luke.diminou.abs.components.layout.overlay.media;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.layout.overlay.PartialSlideOverlay;
import org.luke.diminou.abs.components.layout.overlay.media.bucketlist.BucketList;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.media.Bucket;

import java.util.List;

public class BucketOverlay extends PartialSlideOverlay implements Styleable {
    private final BucketList bucketList;
    private ObjectConsumer<Bucket> onBucket;

    public BucketOverlay(App owner) {
        super(owner, .5);

        bucketList = new BucketList(owner);
        bucketList.setOnAction(bucket -> {
            if (onBucket != null) {
                hide();
                onBucket.accept(bucket);
            }
        });
        list.addView(bucketList);

        applyStyle(owner.getStyle());
    }

    public void setOnBucket(ObjectConsumer<Bucket> onBucket) {
        this.onBucket = onBucket;
    }

    public void setData(List<Bucket> data) {
        bucketList.setData(data);
    }
}