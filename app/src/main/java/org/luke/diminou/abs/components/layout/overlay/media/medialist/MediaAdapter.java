package org.luke.diminou.abs.components.layout.overlay.media.medialist;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.media.Media;

import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaHolder> {
    private final App owner;
    private List<Media> data;

    private ObjectConsumer<Media> onAction;

    private final int size;

    public MediaAdapter(App owner, int size) {
        this.owner = owner;
        this.data = new ArrayList<>();
        this.size = size;
    }

    public void setOnAction(ObjectConsumer<Media> onAction) {
        this.onAction = onAction;
    }

    @NonNull
    @Override
    public MediaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MediaHolder holder = new MediaHolder(new MediaEntry(owner, size / 4));
        holder.setOnAction(media -> {
            if (onAction != null) {
                onAction.accept(media);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MediaHolder holder, int position) {
        Media media = data.get(position);
        holder.load(media);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Media> data) {
        this.data = data;
        notifyDataSetChanged();
    }
}