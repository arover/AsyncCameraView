package com.arover.camera;

/**
 * @author minstrel
 *         created at 19/07/2017 11:16
 */


import android.support.v4.util.ArrayMap;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A collection class that automatically groups {@link Size}s by their {@link AspectRatio}s.
 */
class SizeMap {

    private final ArrayMap<AspectRatio, SortedSet<Size>> mRatios = new ArrayMap<>();

    /**
     * Add a new {@link Size} to this collection.
     *
     * @param size The size to add.
     * @return {@code true} if it is added, {@code false} if it already exists and is not added.
     */
    public boolean add(Size size) {
        for (AspectRatio ratio : mRatios.keySet()) {
            if (ratio.about(size)) {
                final SortedSet<Size> sizes = mRatios.get(ratio);
                if (sizes.contains(size)) {
                    return false;
                } else {
                    sizes.add(size);
                    return true;
                }
            }
        }
        // None of the existing ratio matches the provided size; add a new key
        SortedSet<Size> sizes = new TreeSet<>();
        sizes.add(size);
        mRatios.put(AspectRatio.compute(size.getWidth(), size.getHeight()), sizes);
        return true;
    }

    Set<AspectRatio> ratios() {
        return mRatios.keySet();
    }

    SortedSet<Size> sizes(AspectRatio ratio) {
        SortedSet<Size> map = mRatios.get(ratio);
        if(map==null){
            return new TreeSet<>();
        }
        return map;
    }

    void clear() {
        mRatios.clear();
    }

    boolean isEmpty() {
        return mRatios.isEmpty();
    }

    public void remove(AspectRatio ratio) {
        mRatios.remove(ratio);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("");
        for(Map.Entry<AspectRatio, SortedSet<Size>> item : mRatios.entrySet()){
            for(Size size: item.getValue()){
                builder.append(item.getKey().toString()).append("-").append(size.toString()).append("\n");
            }
        }
        return builder.toString();
    }
}