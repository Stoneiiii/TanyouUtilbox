package com.tanyou.toolservice.util;

public enum TanyouSvrAndroidVersionEnum {

    android14("android-14.0.0_r1"),
    android13("android-13.0.0_r1"),
    android12("android-12.0.0_r1");

    private String svrAndroidVersion;
    TanyouSvrAndroidVersionEnum(String svrAndroidVersion) {
        this.svrAndroidVersion = svrAndroidVersion;
    }

    public static String getTanyouAndroidVersion(String androidVersion) {
        for (TanyouSvrAndroidVersionEnum tanyouSvrAndroidVersionEnum : values()) {
            if (tanyouSvrAndroidVersionEnum.name().equals("android" + androidVersion))
                return tanyouSvrAndroidVersionEnum.svrAndroidVersion;
        }
        return null;
    }


}
