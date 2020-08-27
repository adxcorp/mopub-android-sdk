// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.nativeads;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.common.Preconditions;
import com.mopub.common.VisibleForTesting;
import com.mopub.common.logging.MoPubLog;

import java.lang.ref.WeakReference;

import static com.mopub.common.logging.MoPubLog.SdkLogEvent.CUSTOM;

/**
 * Use {@link com.mopub.nativeads.MoPubAdAdapter} or
 * {@link com.mopub.nativeads.MoPubStreamAdPlacer} instead
 */
public final class AdapterHelper {
    /**
     * Preferably an Activity Context.
     */
    @NonNull private final WeakReference<Context> mContext;
    @NonNull private final Context mApplicationContext;
    private final int mStart;
    private final int mInterval;

    public AdapterHelper(@NonNull final Context context, final int start, final int interval) {
        Preconditions.checkNotNull(context, "Context cannot be null.");
        Preconditions.checkArgument(start >= 0, "start position must be non-negative");
        Preconditions.checkArgument(interval >= 2, "interval must be at least 2");

        mContext = new WeakReference<>(context);
        mApplicationContext = context.getApplicationContext();
        mStart = start;
        mInterval = interval;
    }

    @NonNull
    public View getAdView(@Nullable final View convertView,
            @Nullable final ViewGroup parent,
            @Nullable final NativeAd nativeAd,
            @Nullable final ViewBinder viewBinder) {
        final Context context = mContext.get();
        if (context == null) {
            MoPubLog.log(CUSTOM, "Weak reference to Context in"
                    + " AdapterHelper became null. Returning empty view.");
            return new View(mApplicationContext);
        }

        return NativeAdViewHelper.getAdView(
                convertView,
                parent,
                context,
                nativeAd
        );
    }

    @NonNull
    public View getAdView(@Nullable final View convertView,
            @Nullable final ViewGroup parent,
            @Nullable final NativeAd nativeAd) {
        return getAdView(convertView, parent, nativeAd, null);
    }

    // Total number of content rows + ad rows
    public int shiftedCount(final int originalCount) {
        return originalCount + numberOfAdsThatCouldFitWithContent(originalCount);
    }

    // Shifted position of content in the backing list
    public int shiftedPosition(final int position) {
        return position - numberOfAdsSeenUpToPosition(position);
    }

    public boolean isAdPosition(final int position) {
        if (position < mStart) {
            return false;
        }

        return ((position - mStart) % mInterval == 0);
    }

    private int numberOfAdsSeenUpToPosition(final int position) {
        // This method takes a position from a list of content and ads mixed together
        // and calculates the number of ads seen up to that point

        if (position <= mStart) {
            return 0;
        }

        // Add 1 to the result since we start with an ad at start position and round down
        return (int) Math.floor((double) (position - mStart) / mInterval) + 1;
    }

    private int numberOfAdsThatCouldFitWithContent(final int contentRowCount) {
        // This method is passed the number of content rows from the backing list
        // and calculates how many ads could fit in with the content

        if (contentRowCount <= mStart) {
            return 0;
        }

        final int spacesBetweenAds = mInterval - 1;
        if ((contentRowCount - mStart) % spacesBetweenAds == 0) {
            // Don't add 1 to result since we never include an ad at the last position in the list
            return (contentRowCount - mStart) / spacesBetweenAds;
        } else {
            // Add 1 to the result since we start with an ad at start position and round down
            return (int) Math.floor((double) (contentRowCount - mStart) / spacesBetweenAds) + 1;
        }
    }

    // Testing
    @Deprecated
    @VisibleForTesting
    void clearContext() {
        mContext.clear();
    }
}
