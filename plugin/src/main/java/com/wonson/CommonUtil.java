package com.wonson;
import java.util.UUID;
public class CommonUtil {
    public static String getRandomString(){
        String s = UUID.randomUUID().toString();
        String substring = s.substring(0, 8);
        return substring;
    }
}
