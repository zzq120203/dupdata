package cn.ac.iie.configs;

public class Config {

    //MPP
    @FieldMeta(isOptional = false, desc = "mpp驱动地址")
    public static String mppDriver;

    @FieldMeta(isOptional = false, desc = "mpp地址")
    public static String mppUrl;

    @FieldMeta(isOptional = false, desc = "mpp用户")
    public static String mppUser;

    @FieldMeta(isOptional = false, desc = "mpp密码")
    public static String mppPwd;

    //oracle
    @FieldMeta(isOptional = false, desc = "mpp驱动地址")
    public static String oracleDriver;

    @FieldMeta(isOptional = false, desc = "mpp地址")
    public static String oracleUrl;

    @FieldMeta(isOptional = false, desc = "mpp用户")
    public static String oracleUser;

    @FieldMeta(isOptional = false, desc = "mpp密码")
    public static String oraclePwd;

    //redis
    @FieldMeta(isOptional = false, desc = "redis地址")
    public static String redisUrl;

    @FieldMeta(isOptional = false, desc = "redis认证")
    public static String redisAuthToken;


    @FieldMeta(isOptional = false, desc = "更新文本")
    public static boolean textUpData;

    @FieldMeta(isOptional = false, desc = "更新人脸")
    public static boolean faceUpData;

}
