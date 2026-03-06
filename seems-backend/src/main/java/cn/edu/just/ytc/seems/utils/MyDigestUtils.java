package cn.edu.just.ytc.seems.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class MyDigestUtils {
    public static String strEncrypt(String str) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(str);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, encodedPassword);
    }
}
