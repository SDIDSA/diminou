package org.luke.diminou.abs.components.layout.overlay.media.medialist;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.media.Media;

public class MediaHolder extends RecyclerView.ViewHolder {
    public static void clearSelected() {
        selected = null;
        MediaEntry.clearSelection();
    }

    public static Media getSelected() {
        return selected;
    }

    private static Media selected;
    private final MediaEntry entry;
    private Media data;
    private ObjectConsumer<Media> onAction;

    public MediaHolder(MediaEntry entry) {
        super(entry);
        entry.setOnClick(() -> {
            if (data == selected) {
                entry.deselect();
                selected = null;
            } else {
                entry.select();
                selected = data;
            }
            if (onAction != null && data != null) {
                try {
                    onAction.accept(data);
                } catch (Exception x) {
                    ErrorHandler.handle(x, "handle bucket selection");
                }
            }
        });
        this.entry = entry;
    }

    public void load(Media data) {
        if (selected == data && selected != this.data) {
            entry.select();
        } else if (selected != data && selected == this.data) {
            entry.deselect();
        }
        this.data = data;
        entry.load(data);
    }

    public void setOnAction(ObjectConsumer<Media> onAction) {
        this.onAction = onAction;
    }
}