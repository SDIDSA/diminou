package org.luke.diminou.data.media;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Size;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.image.ImageProxy;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;

import java.io.IOException;

public class Media {
    private final Uri uri;
    private final String name;
    private final long id;

    public Media(long id, Uri uri, String name) {
        this.id = id;
        this.uri = uri;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Uri getUri() {
        return uri;
    }

    public long getId() {
        return id;
    }

    public void getThumbnail(App owner, int size, ObjectConsumer<Bitmap> onResult, Runnable onFail) {
        ImageProxy.getThumbnail(owner, uri, size, onResult, onFail);
    }
}
