// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.network;

import android.net.Uri;

import com.mopub.common.ClientMetadata;
import com.mopub.common.MoPub;
import com.mopub.common.privacy.AdvertisingId;
import com.mopub.common.privacy.MoPubIdentifier;
import com.mopub.volley.toolbox.HurlStack;

import static com.mopub.common.Constants.TAS_AUTHORIZED;
import static com.mopub.common.Constants.TAS_DENIED;

/**
 * Url Rewriter that replaces MoPub templates for Google Advertising ID and Do Not Track settings
 * when a request is queued for dispatch by the HurlStack in Volley.
 */
public class PlayServicesUrlRewriter implements HurlStack.UrlRewriter {
    public static final String IFA_TEMPLATE = "mp_tmpl_advertising_id";
    public static final String DO_NOT_TRACK_TEMPLATE = "mp_tmpl_do_not_track";
    public static final String MOPUB_ID_TEMPLATE = "mp_tmpl_mopub_id";
    public static final String TAS_TEMPLATE = "mp_tmpl_tas";

    public PlayServicesUrlRewriter() {
    }

    @Override
    public String rewriteUrl(final String url) {
        ClientMetadata clientMetadata = ClientMetadata.getInstance();
        if (clientMetadata == null) {
            return url;
        }
        MoPubIdentifier identifier = clientMetadata.getMoPubIdentifier();
        AdvertisingId info = identifier.getAdvertisingInfo();
        String toReturn = url.replace(DO_NOT_TRACK_TEMPLATE, info.isDoNotTrack() ? "1" : "0");
        toReturn = toReturn.replace(TAS_TEMPLATE, info.isDoNotTrack() ? TAS_DENIED : TAS_AUTHORIZED);

        if (MoPub.canCollectPersonalInformation() && !info.isDoNotTrack()) {
            toReturn = toReturn.replace(IFA_TEMPLATE, Uri.encode(info.getIdentifier(true)));
        } else {
            final String ifaFullTemplate = "&ifa=" + IFA_TEMPLATE;
            toReturn = toReturn.replace(ifaFullTemplate, "");
        }

        toReturn = toReturn.replace(MOPUB_ID_TEMPLATE,  Uri.encode(info.getIdentifier(false)));
        return toReturn;
    }
}
