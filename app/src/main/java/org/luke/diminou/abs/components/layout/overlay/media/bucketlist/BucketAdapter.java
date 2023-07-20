package org.luke.diminou.abs.components.layout.overlay.media.bucketlist;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.media.Bucket;

import java.util.ArrayList;
import java.util.List;

public class BucketAdapter extends RecyclerView.Adapter<BucketHolder> {
    private final App owner;
    private List<Bucket> data;

    private ObjectConsumer<Bucket> onAction;

    public BucketAdapter(App owner) {
        this.owner = owner;
        this.data = new ArrayList<>();
    }

    public void setOnAction(ObjectConsumer<Bucket> onAction) {
        this.onAction = onAction;
    }

    @NonNull
    @Override
    public BucketHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BucketHolder holder = new BucketHolder(new BucketEntry(owner));
        holder.setOnAction(code -> {
            if (onAction != null) {
                onAction.accept(code);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull BucketHolder holder, int position) {
        Bucket code = data.get(position);
        holder.load(code);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Bucket> data) {
        this.data = data;
        notifyDataSetChanged();
    }
}