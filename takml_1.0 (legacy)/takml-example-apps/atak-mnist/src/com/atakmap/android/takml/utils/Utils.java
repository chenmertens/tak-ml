/*
 * Copyright (c) 2018, Raytheon BBN Technologies
 * All rights reserved. Distribution restricted; see copyright-and-distribution.txt.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
package com.atakmap.android.takml.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.atakmap.android.atakutils.MiscUtils;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.util.NotificationUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Created by bentoll on 12/13/17.
 */

public class Utils {
    private static final String TAG = Utils.class.getName();
    static TreeMap<Date, String> timeImageMap = null;
    //TODO move this out into OsppreState class
    private static InputStream timeKmzInputStream = null;

    public static long dateTimeStringToUnixTime(String date, String time) {
        String[] timeFields = time.split(":");
        int hour = 0;
        int minute = 0;
        if (timeFields.length != 2) {
            Log.w(TAG, "Invalid time '" + time + "', so setting last seen time to 0");
        } else {
            hour = Integer.valueOf(timeFields[0]);
            minute = Integer.valueOf(timeFields[1]);
        }

        String[] dateFields = date.split("/");
        int month = 0;
        int day = 0;
        int year = 0;
        if (dateFields.length != 3) {
            Log.w(TAG, "Invalid date '" + date + "', so setting date to default");
        } else {
            // In Java, months start at 0
            month = Integer.valueOf(dateFields[0]) - 1;
            day = Integer.valueOf(dateFields[1]);
            year = Integer.valueOf(dateFields[2]);
        }

        // ** Use the time zone where the program is currently running.
        Calendar calendar = new GregorianCalendar(year, month, day, hour, minute);
        return calendar.getTimeInMillis();
    }

    public static void toast(String str) {
        MiscUtils.toast(str);
    }

    public static void notify(Context context, String contentTitle, String contentText) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(NotificationUtil.GeneralIcon.ATAK.getID())
                .setContentTitle(contentTitle)
                .setContentText(contentText);
        Notification notification = new Notification.BigTextStyle(builder)
                .bigText(contentText)
                .build();
        notificationManager.notify(1, notification);
    }

    public static boolean validateIpv4Address(String input) {
        Pattern pattern = Pattern.compile("((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}");
        return pattern.matcher(input).matches();
    }

    public static boolean validatePort(String port) {
        if (port == null) {
            return false;
        }

        try {
            int intPort = Integer.parseInt(port);
            return intPort >= 0 && intPort <= 65535;
        } catch (NumberFormatException e){
            return false;
        }

    }

    public static void deleteShapes(MapView view, List<String> uids) {
        for (String uid : uids) {
            MapItem victim = view.getRootGroup().deepFindUID(uid);
            if (victim == null) {
                continue;
            }
            victim.getGroup().removeItem(victim);
        }
    }

    public static void deleteShape(MapView view, String uid) {
        Log.v(TAG, "Deleting shape with UUID: " + uid);
        MapItem victim = view.getRootGroup().deepFindUID(uid);
        if (victim == null) {
            Log.e(TAG, "Unable to delete shape; no shape with UID: " + uid);
            return;
        }
        victim.getGroup().removeItem(victim);
    }


// TODO
//    public static void deleteMissionPackage(String name, String uid) {
//        String filesDir = MissionPackageFileIO
//                .getMissionPackageFilesPath(FileSystemUtils.getRoot()
//                        .getAbsolutePath());
//        File dir = new File(filesDir, uid);
//        if (dir.exists())
//            FileSystemUtils.deleteDirectory(dir, false);
//    }


    public static int getScreenWidthPx() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeightPx() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static float getScreenXdpi() {
        return Resources.getSystem().getDisplayMetrics().xdpi;
    }

    public static float getScreenYdpi() {
        return Resources.getSystem().getDisplayMetrics().ydpi;
    }

    public static float getDensity() {
        return Resources.getSystem().getDisplayMetrics().density;
    }

    /**
     * Adapted from:
     * https://stackoverflow.com/questions/4275797/view-setpadding-accepts-only-in-px-is-there-anyway-to-setpadding-in-dp
     */
    public static int dpTopx(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static double kmToMiles(double km) {
        return km * 0.621371;
    }

    public static double milesToKm(double miles) {
        return miles * 1.60934;
    }

    public static String roundOffTo2DecPlaces(double val) {
        return String.format("%.2f", val);
    }

    public static void disableOverlayInteraction(boolean selectableOff, MapView mapView){
        if(selectableOff) {
            mapView.getMapTouchController().lockControls();
            mapView.getMapEventDispatcher().clearListeners("item_click");
            mapView.getMapEventDispatcher().clearListeners("map_click");
            mapView.getMapEventDispatcher().clearListeners("map_confirmed_click");
            mapView.getMapEventDispatcher().clearListeners("map_lngpress");
            mapView.getMapEventDispatcher().clearListeners("item_lngpress");
            mapView.getMapEventDispatcher().clearListeners("item_press");
            mapView.getMapEventDispatcher().clearListeners("item_release");
            mapView.getMapEventDispatcher().clearListeners("map_press");
            mapView.getMapEventDispatcher().clearListeners("map_release");

        }else{
            mapView.getMapTouchController().unlockControls();
        }
    }

    public static String truncateName(String name) {
        return (name.length() > 20) ? name.substring(0, 17) + "..." : name;
    }

    public static String getFormattedDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    // method for bitmap to base64
    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }


}
