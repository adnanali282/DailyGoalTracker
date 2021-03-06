/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.creatoro.goals.activities.settings;

import android.os.Bundle;

import com.creatoro.goals.activities.BaseActivity;
import com.creatoro.goals.activities.BaseScreen;
import com.creatoro.goals.utils.StyledResources;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import com.creatoro.goals.R;

/**
 * Activity that allows the user to view and modify the app settings.
 */
public class SettingsActivity extends BaseActivity
{
    //admob custom code
    private static final int AD_UNIT_ID = R.string.app_interstitial_ID;
    InterstitialAd mInterstitialAd;
    // End admob

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        setupActionBarColor();

        mInterstitialAd = new InterstitialAd(this);

        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(AD_UNIT_ID));

        AdRequest adRequest = new AdRequest.Builder()
                .build();

        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }
        });


    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void setupActionBarColor()
    {
        StyledResources res = new StyledResources(this);
        int color = BaseScreen.getDefaultActionBarColor(this);

        if (res.getBoolean(R.attr.useHabitColorAsPrimary))
            color = res.getColor(R.attr.aboutScreenColor);

        BaseScreen.setupActionBarColor(this, color);
    }
}
