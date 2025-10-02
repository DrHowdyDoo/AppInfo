package com.drhowdydoo.appinfo.model;

import android.util.LruCache;

public class LRUCache {

    private static LRUCache instance;
    private LruCache<String, BundleInfo> bundleApkCache;

    private LRUCache() {
        // Cache size in entries or bytes (here using number of entries for simplicity)
        final int maxCacheSize = 50; // number of ApkInfo objects to cache

        bundleApkCache = new LruCache<String, BundleInfo>(maxCacheSize) {
            @Override
            protected int sizeOf(String key, BundleInfo value) {
                // Optionally measure size more accurately (e.g., icon bitmap size)
                return 1; // count each ApkInfo as 1 unit
            }
        };
    }

    public static LRUCache getInstance() {
        if (instance == null) {
            instance = new LRUCache();
        }
        return instance;
    }

    public LruCache<String, BundleInfo> getBundleApkCache() {
        return bundleApkCache;
    }

    public void setBundleApkCache(LruCache<String, BundleInfo> bundleApkCache) {
        this.bundleApkCache = bundleApkCache;
    }

    public void put(String apkPath, BundleInfo bundleApkInfo) {
        if (apkPath != null && bundleApkInfo != null) {
            bundleApkCache.put(apkPath, bundleApkInfo);
        }
    }

    public BundleInfo get(String apkPath) {
        if (apkPath == null) return null;
        return bundleApkCache.get(apkPath);
    }

    public void clearCache() {
        bundleApkCache.evictAll();
    }
}
