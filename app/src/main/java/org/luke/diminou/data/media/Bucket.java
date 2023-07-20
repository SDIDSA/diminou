package org.luke.diminou.data.media;

import java.util.ArrayList;
import java.util.Objects;

public class Bucket {
    private final String name;
    private final ArrayList<Media> items;

    public Bucket(String name) {
        this.name = name;
        items = new ArrayList<>();
    }

    public ArrayList<Media> getItems() {
        return items;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bucket bucket = (Bucket) o;

        return Objects.equals(name, bucket.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
