package com.drhowdydoo.appinfo.util;

import java.util.concurrent.TimeUnit;

public class TimeFormatter {

    public static String format(long timeInMilliseconds, String prefix) {

        long time = TimeUnit.MILLISECONDS.toSeconds(timeInMilliseconds);

        if (time >= TimeUnit.DAYS.toSeconds(365)) {
            return prefix + " more than a year ago";
        } else if (time >= TimeUnit.DAYS.toSeconds(30)) {
            return prefix + " " + time / TimeUnit.DAYS.toSeconds(30) + " months ago";
        } else if (time >= TimeUnit.DAYS.toSeconds(7)) {
            return prefix + " " + time / TimeUnit.DAYS.toSeconds(7) + " wk. ago";
        } else if (time >= TimeUnit.DAYS.toSeconds(1)) {
            if (time >= TimeUnit.DAYS.toSeconds(2)) {
                return prefix + " " + time / TimeUnit.DAYS.toSeconds(1) + " days ago";
            } else {
                return prefix + " Yesterday";
            }
        } else if (time >= TimeUnit.HOURS.toSeconds(1)) {
            return prefix + " " + time / TimeUnit.HOURS.toSeconds(1) + " hr. ago";
        } else if (time >= TimeUnit.MINUTES.toSeconds(1)) {
            return prefix + " " + time / TimeUnit.MINUTES.toSeconds(1) + " min ago";
        } else if (time >= 1) {
            return prefix + " " + time + " sec ago";
        } else {
            return prefix + " Just now";
        }
    }


}
