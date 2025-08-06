package com.main.manage;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AdminLoginInfo {
    private AdminLoginInfo() {

    }

    private static int currentCount;
    private static Map<String, String> mapAdmin;

    public static synchronized void newAdmin() {
        ++currentCount;
    }

    public static synchronized int getCurrentCount() {
        return currentCount;
    }

    public static synchronized String NewAdminLogin(String adminName) {
        if (mapAdmin == null) {
            mapAdmin = new HashMap<>();
        }
        try {
            String time = ReturnCard.LineAESEncrypt(ReturnCard.GetSingleton().GetReturnRodom(6));//这个就是把时间戳经过处理得到期望格式的时间
            if (mapAdmin.containsKey(adminName)) {
                mapAdmin.remove(adminName);
            }
            mapAdmin.put(adminName, time);
            return mapAdmin.get(adminName);
        } catch (Exception e) {
            return "";
        }

    }

    public static synchronized String GetAdminLoginInfo(String adminName) {

        return mapAdmin.get(adminName);

    }
}
