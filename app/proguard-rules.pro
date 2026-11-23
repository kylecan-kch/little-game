# Saved games persist Status.name and restore it with Status.valueOf.
# R8 must not rename the constants or existing saves stop loading.
-keepclassmembers enum com.gamecollection.engine2048.Status {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepnames enum com.gamecollection.engine2048.Status
