package com.oscar.junke.bluetoothdemoapp;

import com.kontakt.sdk.android.ble.manager.ProximityManager;

/**
 * Implement this when you want to configure monitoring.
 *
 */
public interface ProximityConfigurationListener {
    void configureProximityManager(ProximityManager proximityManager);
}