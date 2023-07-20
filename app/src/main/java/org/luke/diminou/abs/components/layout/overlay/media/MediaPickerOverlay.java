package org.luke.diminou.abs.components.layout.overlay.media;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.graphics.Insets;


import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.button.Button;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.overlay.PartialSlideOverlay;
import org.luke.diminou.abs.components.layout.overlay.media.medialist.MediaHolder;
import org.luke.diminou.abs.components.layout.overlay.media.medialist.MediaList;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Permissions;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.media.Bucket;
import org.luke.diminou.data.media.Media;

import java.util.ArrayList;
import java.util.List;

public class MediaPickerOverlay extends PartialSlideOverlay {

    private final Button done;
    private final Button bucket;
    private final ColorIcon arrow;
    private final BucketOverlay bucketOverlay;
    private MediaList mediaList;
    private List<Bucket> buckets;
    private Bucket selected;
    private ObjectConsumer<Media> onMedia;

    public MediaPickerOverlay(App owner) {
        super(owner, .7);

        done = new Button(owner, "done");
        done.setFont(new Font(16));
        ViewUtils.setMargin(done, owner, 10, 10, 10, 10);

        HBox top = new HBox(owner);
        top.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        top.setGravity(Gravity.CENTER);
        ViewUtils.setMargin(top, owner, 10, 10, 10, 10);

        bucketOverlay = new BucketOverlay(owner);

        bucket = new Button(owner, "Select Album");
        bucket.setOnClick(() -> {
            if (buckets == null) {
                Toast.makeText(owner, "Unresolved permissions", Toast.LENGTH_SHORT).show();
            } else {
                int[] l = new int[2];
                bucket.getLocationOnScreen(l);
                int y = l[1];
                int h = bucket.getHeight();
                int maxY = y + h + ViewUtils.dipToPx(15, owner);

                double perc = (double) maxY / owner.getScreenHeight();

                bucketOverlay.setHeightFactor(1 - perc);
                bucketOverlay.setData(buckets);
                bucketOverlay.setOnBucket(res -> {
                    selected = res;
                    bucket.setKey(res.getName());
                    mediaList.setData(selected.getItems());
                });
                bucketOverlay.show();
            }
        });

        arrow = new ColorIcon(owner, R.drawable.right_arrow);
        arrow.setRotation(90);
        arrow.setSize(16);
        arrow.setFocusable(false);
        arrow.setClickable(false);

        ViewUtils.setMarginLeft(arrow, owner, 15);

        bucket.addPostLabel(arrow);

        top.addView(bucket);

        addOnShowing(this::readImages);

        mediaList = new MediaList(owner);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.weight = 1;
        mediaList.setLayoutParams(params);
        mediaList.setOnAction(media -> {
            if (onMedia != null) {
                done.setDisabled(false);
            }
        });
        ViewUtils.setMargin(mediaList, owner, 10, 0, 10, 0);

        list.addView(top);
        list.addView(mediaList);
        list.addView(done);

        done.setOnClick(() -> {
            try {
                onMedia.accept(MediaHolder.getSelected());
            } catch (Exception e) {
                ErrorHandler.handle(e, "handling media picker result");
            }
            hide();
        });

        addOnShowing(() -> {
            MediaHolder.clearSelected();
            done.setDisabled(true);
        });

        applyStyle(owner.getStyle());
    }

    public void setOnMedia(ObjectConsumer<Media> onMedia) {
        this.onMedia = onMedia;
    }

    private void readImages() {
        owner.requirePermissions(this::loadImages, Permissions.mediaPermissions());
    }

    private void loadImages() {
        if(buckets == null) {
            buckets = listBuckets();
            bucket.setKey(selected.getName());
            mediaList.setData(selected.getItems());
        }
    }

    private List<Bucket> listBuckets() {
        ArrayList<Bucket> res = new ArrayList<>();

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED
        };

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String BUCKET_ORDER_BY = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        Cursor cursor = owner.getContentResolver().query(images, projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                BUCKET_ORDER_BY        // Ordering
        );

        Bucket all = new Bucket("All Photos");
        res.add(all);

        selected = all;

        if (cursor.moveToFirst()) {

            int idCol = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            int nameCol = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
            int bucketCol = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            do {

                long id = cursor.getLong(idCol);
                String name = cursor.getString(nameCol);
                String bucket = cursor.getString(bucketCol);

                Bucket b = bucketFromName(res, bucket);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                Media media = new Media(id, contentUri, name);

                all.getItems().add(media);
                b.getItems().add(media);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return res;
    }

    private Bucket bucketFromName(ArrayList<Bucket> list, String name) {
        Bucket res = null;

        for (Bucket b : list) {
            if (b.getName().equals(name)) {
                res = b;
                break;
            }
        }

        if (res == null) {
            res = new Bucket(name);
            list.add(res);
        }

        return res;
    }

    @Override
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (done == null) return;
        bucket.setBackgroundColor(style.getBackgroundPrimary());
        bucket.setTextFill(style.getTextNormal());
        bucket.setBorderColor(style.getTextMuted());
        arrow.setFill(style.getTextNormal());
        done.setTextFill(Color.WHITE);
        done.setBackgroundColor(style.getSecondaryButtonBack());
    }
}
