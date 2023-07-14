package org.luke.diminou.abs.utils;

import android.Manifest;
import android.os.Build;

public class Permissions {

    public static String[] mediaPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[] {
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO};
        }else {
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    public static int permissionRequestCode() {
        return (int) (Math.random() * 2048) + 1;
    }
}
