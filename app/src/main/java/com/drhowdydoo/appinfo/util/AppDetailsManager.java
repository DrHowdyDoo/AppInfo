package com.drhowdydoo.appinfo.util;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Base64;

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

    public Drawable getIcon(boolean isApk, String apkPath) {
        if (!isApk) return context.getPackageManager().getApplicationIcon(packageInfo.applicationInfo);
        else {
            packageInfo.applicationInfo.sourceDir       = apkPath;
            packageInfo.applicationInfo.publicSourceDir = apkPath;
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
            installSource = "N/A";
        }

        return installSource;
    }

    public String getPermissions() {
        if (packageInfo.requestedPermissions == null) return "N/A";
        StringBuilder permissions = new StringBuilder();
        for (String requestedPermission : packageInfo.requestedPermissions) {
            permissions.append(requestedPermission).append("\n");
        }
        return permissions.toString();
    }

    public String getActivities() {
        if (packageInfo.activities == null) return "N/A";
        StringBuilder activities = new StringBuilder();
        for (ActivityInfo activity : packageInfo.activities) {
            activities.append(activity.name).append("\n");
        }
        return activities.toString();
    }

    public String getBroadcastReceivers() {
        if (packageInfo.receivers == null) return "N/A";
        StringBuilder receivers = new StringBuilder();
        for (ActivityInfo receiver : packageInfo.receivers) {
            receivers.append(receiver.name).append("\n");
        }
        return receivers.toString();
    }

    public String getServices() {
        if (packageInfo.services == null) return "N/A";
        StringBuilder services = new StringBuilder();
        for (ServiceInfo service : packageInfo.services) {
            services.append(service.name).append("\n");
        }
        return services.toString();
    }

    public String getProviders() {
        if (packageInfo.providers == null) return "N/A";
        StringBuilder providers = new StringBuilder();
        for (ProviderInfo provider : packageInfo.providers) {
            providers.append(provider.name).append("\n");
        }
        return providers.toString();
    }

    public String getFeatures() {
        if (packageInfo.reqFeatures == null) return "N/A";
        StringBuilder features = new StringBuilder();
        for (FeatureInfo feature : packageInfo.reqFeatures) {
            features.append(feature.name).append("\n");
        }
        return features.toString();
    }

    public Optional<Map<String,String>> getSignatures() {
        Map<String,String> signatures = new HashMap<>(2);
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

                signingKeys.append("Hash :").append("\t").append(hashKey).append("\n")
                        .append("MD5 :").append("\t").append(md5Hash).append("\n")
                        .append("SHA1 :").append("\t").append(sha1Hash).append("\n")
                        .append("SHA256 :").append("\t").append(sha256Hash).append("\n");

                signatures.put("signing_keys",signingKeys.toString());

                CertificateFactory certFactory = CertificateFactory.getInstance("X509");
                X509Certificate x509Cert = (X509Certificate) certFactory.generateCertificate(certStream);
                certificates.append("Certificate serial number : ").append(x509Cert.getSerialNumber()).append("\n")
                        .append("Signature Algorithm : ").append(x509Cert.getSigAlgName()).append("\n");

                certificates.append(transformIssuerDNString(x509Cert.getIssuerDN().getName()));
                signatures.put("certificates",certificates.toString());

            } catch (NoSuchAlgorithmException | CertificateException e) {
                e.printStackTrace();
            }
        }

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
        String[] pairs = issuerDNString.split(", ");
        StringBuilder transformedString = new StringBuilder();

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String attribute = keyValue[0];
                String value = keyValue[1];
                transformedString.append(expandSignatureAbbreviation(attribute)).append(" : ").append(value).append("\n");
            }
        }

        return transformedString.toString();
    }

    private String expandSignatureAbbreviation(String abbreviation) {
        switch (abbreviation){
            case "EMAILADDRESS" : return "Email Address : ";
            case "CN" :  return "Common Name : ";
            case "OU" : return "Organizational Unit : ";
            case "O" : return "Organization : ";
            case "L" : return "Locality : ";
            case "ST" : return "State : ";
            case "C" : return "Country : ";
        }
        return abbreviation;
    }

}
