package com.oscar.junke.bluetoothdemoapp;

import java.util.List;

/**
 * Interface for listening to the BeaconService.
 * Should be attached in such a way that we are sure we have retrieved the
 * interesting beacons from the server before we start listening
 */
public interface BeaconListener {

    void onBeaconsUpdated(List<String> beacons);


}
