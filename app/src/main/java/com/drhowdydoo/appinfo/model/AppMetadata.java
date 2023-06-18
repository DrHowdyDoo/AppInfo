package com.drhowdydoo.appinfo.model;

public class AppMetadata {

    private String category, minSdk, targetSdk, installDt, updatedDt, source, packageName, mainClass;

    public AppMetadata(String category,
                       String minSdk,
                       String targetSdk,
                       String installDt,
                       String updatedDt,
                       String source,
                       String packageName,
                       String mainClass) {

        this.category = category;
        this.minSdk = minSdk;
        this.targetSdk = targetSdk;
        this.installDt = installDt;
        this.updatedDt = updatedDt;
        this.source = source;
        this.packageName = packageName;
        this.mainClass = mainClass;
    }

    public String getCategory() {
        if (category == null || category.isEmpty()) return "Undefined";
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMinSdk() {
        return minSdk;
    }

    public void setMinSdk(String minSdk) {
        this.minSdk = minSdk;
    }

    public String getTargetSdk() {
        return targetSdk;
    }

    public void setTargetSdk(String targetSdk) {
        this.targetSdk = targetSdk;
    }

    public String getInstallDt() {
        return installDt;
    }

    public void setInstallDt(String installDt) {
        this.installDt = installDt;
    }

    public String getUpdatedDt() {
        return updatedDt;
    }

    public void setUpdatedDt(String updatedDt) {
        this.updatedDt = updatedDt;
    }

    public String getSource() {
        if (source == null || source.isEmpty()) return "Unknown";
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getMainClass() {
        if (mainClass == null || mainClass.isEmpty()) return "None";
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }
}
