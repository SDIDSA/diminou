package org.luke.diminou.abs.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class DiskImageCache {

    private final Bitmap.CompressFormat mCompressFormat;
    private final int mCompressQuality;
    private final File diskCacheDir;

    public DiskImageCache(Context context, String uniqueName,
                          Bitmap.CompressFormat compressFormat, int quality) {
        diskCacheDir = getDiskCacheDir(context, uniqueName);
        mCompressFormat = compressFormat;
        mCompressQuality = quality;

        if (!diskCacheDir.exists() || !diskCacheDir.isDirectory()) {
            if (!diskCacheDir.mkdir()) {
                ErrorHandler.handle(new RuntimeException("failed to create cache dir"), "init disk cache");
            }
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        final String cachePath = context.getCacheDir().getPath();
        return new File(cachePath + File.separator + uniqueName);
    }

    public File put(String key, Bitmap data) {
        File saveTo = keyToFile(key);
        try {
            data.compress(mCompressFormat, mCompressQuality, new FileOutputStream(saveTo));
        } catch (FileNotFoundException e) {
            ErrorHandler.handle(e, "caching image with key [ " + key + " ]");
        }
        return saveTo;
    }

    public Bitmap getBitmap(String key) {
        File readFrom = keyToFile(key);
        if(readFrom.exists() && readFrom.isFile()) {
            return BitmapFactory.decodeFile(readFrom.getAbsolutePath());
        }
        return null;
    }

    private File keyToFile(String key) {
        return new File(diskCacheDir.getAbsolutePath() + File.separator + key);
    }

}