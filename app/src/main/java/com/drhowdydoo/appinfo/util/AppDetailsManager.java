package com.drhowdydoo.appinfo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Base64;

import com.drhowdydoo.appinfo.model.StringCount;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AppDetailsManager {

    private final Context context;
    private final PackageInfo packageInfo;

    public AppDetailsManager(Context context, PackageInfo packageInfo) {
        this.context = context;
        this.packageInfo = packageInfo;
    }

    public Drawable getIcon(boolean isApk, String apkAbsolutePath) {
        if (!isApk)
            return context.getPackageManager().getApplicationIcon(packageInfo.applicationInfo);
        else {
            packageInfo.applicationInfo.sourceDir = apkAbsolutePath;
            packageInfo.applicationInfo.publicSourceDir = apkAbsolutePath;
            return packageInfo.applicationInfo.loadIcon(context.getPackageManager());
        }
    }

    public String getCategory() {
        CharSequence category = ApplicationInfo.getCategoryTitle(context, packageInfo.applicationInfo.category);
        return category != null ? category.toString() : "Undefined";
    }

    public String getInstalledDate() {
        long installDate = packageInfo.firstInstallTime;
        return installDate > 0 ? DateFormat.getDateInstance().format(installDate) : "Not installed";
    }

    public String getUpdatedDate() {
        long lastUpdateTime = packageInfo.lastUpdateTime;
        return lastUpdateTime > 0 ? DateFormat.getDateInstance().format(lastUpdateTime) : "Not Updated";
    }

    public String getMainClass() {
        String className = packageInfo.applicationInfo.className;
        return className != null ? className : "None";
    }

    public String getTheme(){
        String theme = "NOT FOUND 😅";
        try {
            theme =  context.getPackageManager().getResourcesForApplication(packageInfo.applicationInfo).getResourceEntryName(packageInfo.applicationInfo.theme);
            if (theme == null || theme.isEmpty()) return "NOT FOUND 😅";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return theme;
    }

    @SuppressLint("ResourceType")
    public com.drhowdydoo.appinfo.model.Color getColors() {
        try {
            Resources resources = context.getPackageManager().getResourcesForApplication(packageInfo.applicationInfo);
            Resources.Theme theme = resources.newTheme();
            theme.applyStyle(packageInfo.applicationInfo.theme, true);
            TypedArray colorPrimaryStyledAttr = theme.obtainStyledAttributes(getAttr("colorPrimary", android.R.attr.colorPrimary, resources));
            TypedArray colorSecondaryStyledAttr = theme.obtainStyledAttributes(getAttr("colorSecondary", android.R.attr.colorSecondary, resources));

            int colorPrimary = colorPrimaryStyledAttr.getColor(0, colorPrimaryStyledAttr.getColor(1, Color.WHITE));
            int colorSecondary = colorSecondaryStyledAttr.getColor(0, colorSecondaryStyledAttr.getColor(1, Color.WHITE));
            colorPrimaryStyledAttr.recycle();
            colorSecondaryStyledAttr.recycle();
            return new com.drhowdydoo.appinfo.model.Color(colorPrimary, colorSecondary);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new com.drhowdydoo.appinfo.model.Color(0, 0);
    }

    private int[] getAttr(String colorName, int colorId, Resources resources) {
        final int[] attrs = new int[]{
                resources.getIdentifier(colorName, "attr", packageInfo.packageName),
                colorId
        };
        return attrs;
    }

    public String getInstallSource() {
        String installSource = "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                installSource = context.getPackageManager().getInstallSourceInfo(packageInfo.packageName).getInstallingPackageName();
            } else {
                installSource = context.getPackageManager().getInstallerPackageName(packageInfo.packageName);
            }
            installSource = installSource != null ? Utilities.sourcePackageMap.getOrDefault(installSource, installSource) : "Unknown";
        } catch (PackageManager.NameNotFoundException | IllegalArgumentException exception) {
            installSource = "Unknown";
        }

        return installSource;
    }

    public String getMinSdk() {
        int minSdk = packageInfo.applicationInfo.minSdkVersion;
        String androidName = Utilities.androidVersions.getOrDefault(minSdk, "Unknown");
        return "API " + minSdk + "\n" + androidName;
    }

    public String getTargetSdk() {
        int targetSdk = packageInfo.applicationInfo.targetSdkVersion;
        String androidName = Utilities.androidVersions.getOrDefault(targetSdk, "Unknown");
        return "API " + targetSdk + "\n" + androidName;
    }

    public StringCount getPermissions() {
        String[] requestedPermissions = packageInfo.requestedPermissions;
        if (requestedPermissions == null) return new StringCount("N/A");
        StringBuilder permissions = new StringBuilder();
        for (String requestedPermission : requestedPermissions) {
            permissions.append(requestedPermission).append("\n");
        }
        return new StringCount(permissions.toString().trim(), requestedPermissions.length);
    }

    public StringCount getActivities() {
        ActivityInfo[] activities = packageInfo.activities;
        if (activities == null) return new StringCount("NONE");
        StringBuilder activityList = new StringBuilder();
        for (ActivityInfo activity : activities) {
            activityList.append(activity.name).append("\n");
        }
        return new StringCount(activityList.toString().trim(), activities.length);
    }

    public StringCount getBroadcastReceivers() {
        ActivityInfo[] receiversList = packageInfo.receivers;
        if (receiversList == null) return new StringCount("NONE");
        StringBuilder receivers = new StringBuilder();
        for (ActivityInfo receiver : receiversList) {
            receivers.append(receiver.name).append("\n");
        }
        return new StringCount(receivers.toString().trim(), receiversList.length);
    }

    public StringCount getServices() {
        ServiceInfo[] serviceInfos = packageInfo.services;
        if (serviceInfos == null) return new StringCount("NONE");
        StringBuilder services = new StringBuilder();
        for (ServiceInfo service : serviceInfos) {
            services.append(service.name).append("\n");
        }
        return new StringCount(services.toString().trim(), serviceInfos.length);
    }

    public StringCount getProviders() {
        ProviderInfo[] providerInfos = packageInfo.providers;
        if (providerInfos == null) return new StringCount("NONE");
        StringBuilder providers = new StringBuilder();
        for (ProviderInfo provider : providerInfos) {
            providers.append(provider.name).append("\n");
        }
        return new StringCount(providers.toString().trim(), providerInfos.length);
    }

    public StringCount getFeatures() {
        FeatureInfo[] featureInfos = packageInfo.reqFeatures;
        if (featureInfos == null) return new StringCount("NO FEATURES REQUESTED");
        StringBuilder features = new StringBuilder();
        for (FeatureInfo feature : featureInfos) {
            features.append(feature.name).append("\n");
        }
        return new StringCount(features.toString().trim(), featureInfos.length);
    }

    public String getDataDirPath() {
        return packageInfo.applicationInfo.dataDir;
    }

    public String getSourceDirPath() {
        return packageInfo.applicationInfo.sourceDir;
    }

    public String getNativeLibraryPath() {
        return packageInfo.applicationInfo.nativeLibraryDir;
    }

    public Optional<Map<String, String>> getSignatures() {
        Map<String, String> signatures = new HashMap<>(2);
        StringBuilder certificates = new StringBuilder();
        Signature[] apkContentsSigners;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (packageInfo.signingInfo == null) return Optional.empty();
            if (packageInfo.signingInfo.hasMultipleSigners()) {
                apkContentsSigners = packageInfo.signingInfo.getApkContentsSigners();
            } else {
                apkContentsSigners = packageInfo.signingInfo.getSigningCertificateHistory();
            }
        } else {
            apkContentsSigners = packageInfo.signatures;
            if (apkContentsSigners == null) return Optional.empty();
        }

        StringBuilder signingKeys = new StringBuilder();
        for (Signature apkContentsSigner : apkContentsSigners) {
            final byte[] rawCert = apkContentsSigner.toByteArray();
            InputStream certStream = new ByteArrayInputStream(rawCert);

            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                MessageDigest hash = MessageDigest.getInstance("SHA");
                MessageDigest sha1 = MessageDigest.getInstance("SHA1");
                MessageDigest sha256 = MessageDigest.getInstance("SHA256");

                md5.update(rawCert);
                hash.update(rawCert);
                sha1.update(rawCert);
                sha256.update(rawCert);

                String hashKey = Base64.encodeToString(hash.digest(), Base64.DEFAULT).trim();
                String md5Hash = hashToHex(md5.digest());
                String sha1Hash = hashToHex(sha1.digest());
                String sha256Hash = hashToHex(sha256.digest());

                signingKeys.append("Hash : ").append(hashKey).append("\n")
                           .append("MD5 : ").append(md5Hash).append("\n")
                           .append("SHA1 : ").append(sha1Hash).append("\n")
                           .append("SHA256 : ").append(sha256Hash).append("\n\n");

                CertificateFactory certFactory = CertificateFactory.getInstance("X509");
                X509Certificate x509Cert = (X509Certificate) certFactory.generateCertificate(certStream);
                certificates.append("Certificate serial number : ").append(x509Cert.getSerialNumber()).append("\n")
                        .append("Signature Algorithm : ").append(x509Cert.getSigAlgName()).append("\n");

                certificates.append(transformIssuerDNString(x509Cert.getIssuerDN().getName())).append("\n");

            } catch (NoSuchAlgorithmException | CertificateException e) {
                e.printStackTrace();
            }
        }

        signatures.put("signing_keys", signingKeys.toString().trim());
        signatures.put("certificates", certificates.toString().trim());

        return Optional.of(signatures);
    }

    private String hashToHex(byte[] digest) {
        StringBuilder toRet = new StringBuilder();
        for (byte b : digest) {
            String hex = String.format("%02x", b);
            toRet.append(hex).append(":");
        }
        toRet.setLength(toRet.length() - 1); // Remove the trailing ":"
        return toRet.toString();
    }

    private String transformIssuerDNString(String issuerDNString) {
        String[] pairs = issuerDNString.split(",");
        StringBuilder transformedString = new StringBuilder();

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String attribute = keyValue[0];
                String value = keyValue[1].replace("\\", "");
                transformedString.append(expandSignatureAbbreviation(attribute)).append(" : ").append(value).append("\n");
            }
        }

        return transformedString.toString();
    }

    private String expandSignatureAbbreviation(String abbreviation) {
        switch (abbreviation) {
            case "EMAILADDRESS":
                return "Email Address";
            case "CN":
                return "Common Name";
            case "OU":
                return "Organizational Unit";
            case "O":
                return "Organization";
            case "L":
                return "Locality";
            case "ST":
                return "State";
            case "C":
                return "Country";
        }
        return abbreviation;
    }

}
