package org.luke.diminou.abs.components.controls.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.LruCache;
import android.util.Size;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.DiskImageCache;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.media.Media;
import org.luke.diminou.data.media.Thumby;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class ImageProxy {
    private static final LruCache<String, Bitmap> memoryCache = new LruCache<>((int) (Runtime.getRuntime().maxMemory() / 1024) / 8) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount() / 1024;
        }
    };
    private static DiskImageCache diskCache;

    public static void init(App owner) {
        diskCache = new DiskImageCache(owner, "bitmaps", Bitmap.CompressFormat.PNG, 80);
    }

    private static String makeKey(String url) {
        StringBuilder sb = new StringBuilder();

        for (int i = Math.max(0, url.length() - 20); i < url.length(); i++) {
            char c = url.charAt(i);
            if (Character.isDigit(c) || Character.isLetter(c) || c == '_') {
                sb.append(Character.toLowerCase(c));
            }
        }

        return sb.toString();
    }

    public static void getImage(String url, ObjectConsumer<Bitmap> onResult) {
        String key = makeKey(url);
        AtomicReference<Bitmap> found = new AtomicReference<>(get(key));
        if (found.get() == null) {
            new Thread(() -> {
                try {
                    found.set(download(url));
                    put(key, found.get());
                } catch (Exception x) {
                    ErrorHandler.handle(x, "downloading image at " + url);
                }
                Bitmap finalFound = found.get();
                Platform.runLater(() -> {
                    try {
                        onResult.accept(finalFound);
                    } catch (Exception e) {
                        ErrorHandler.handle(e, "downloading image at " + url);
                    }
                });
            }).start();
        }else {
            try {
                onResult.accept(found.get());
            } catch (Exception e) {
                ErrorHandler.handle(e, "downloading image at " + url);
            }
        }
    }
    public static void getThumbnail(App owner, Uri uri, int size, ObjectConsumer<Bitmap> onResult, Runnable onFail) {
        new Thread(() -> {
            String url = uri.toString();
            String key = makeKey(url) + "_thumb_" + size;
            Bitmap found = get(key);
            if (found == null) {
                try {
                    found = generateThumbnail(owner, uri, size);
                    put(key, found);
                } catch (Exception x) {
                    ErrorHandler.handle(x, "generating thumbnail of " + url);
                    Platform.runLater(onFail);
                    return;
                }
            }
            Bitmap finalFound = found;
            Platform.runLater(() -> {
                try {
                    onResult.accept(finalFound);
                } catch (Exception e) {
                    ErrorHandler.handle(e, "handling thumbnail of " + url);
                    onFail.run();
                }
            });
        }).start();
    }

    private static Bitmap generateThumbnail(App owner, Uri uri, int size) throws IOException {
        Bitmap bmp;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            bmp = owner.getApplicationContext().getContentResolver()
                    .loadThumbnail(uri, new Size(size, size), null);
        } else {
            bmp = Thumby.downSample(uri, size, size);
        }
        int bmpw = bmp.getWidth();
        int bmph = bmp.getHeight();
        if (bmpw == bmph) return bmp;
        boolean hor = bmpw > bmph;
        int ds = hor ? bmph : bmpw;
        int sx = hor ? (bmpw - ds) / 2 : 0;
        int sy = hor ? 0 : (bmph - ds) / 2;
        return Bitmap.createBitmap(bmp, sx, sy, ds, ds);
    }

    public static File mediaToFile(App owner, Media media) {
        String key = makeKey(media.getUri().toString());
        try {
            return diskCache.put(key, BitmapFactory.decodeStream(owner.getContentResolver().openInputStream(media.getUri())));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Bitmap get(String key) {
        Bitmap found = memoryCache.get(key);
        if (found == null) {
            found = diskCache.getBitmap(key);
            if(found != null) {
                memoryCache.put(key, found);
            }
        }
        return found;
    }

    private static void put(String key, Bitmap bitmap) {
        if (memoryCache.get(key) == null) {
            memoryCache.put(key, bitmap);
        }

        if (diskCache.getBitmap(key) == null) {
            diskCache.put(key, bitmap);
        }

    }

    private static Bitmap download(String url) {
        try {
            return BitmapFactory.decodeStream(new URL(url.replace("http:", "https:")).openConnection().getInputStream());
        } catch (IOException e) {
            ErrorHandler.handle(e, "download image at " + url);
            return null;
        }
    }

}
