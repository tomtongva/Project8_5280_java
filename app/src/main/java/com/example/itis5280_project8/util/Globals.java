package com.example.itis5280_project8.util;

import java.util.HashMap;
import java.util.Map;

public class Globals {
    //public static final String URL = "https://project4-5280-server.herokuapp.com";
    public static final String URL = "http://192.168.86.32:8080";

    public static final String BlueCatsToken = "06e8c088-fae4-419c-aeb6-c56e8def1c42";

    public static Map<String, String> blueCatsToRegionMap = new HashMap<String, String>();
    static {
        blueCatsToRegionMap.put("B101", "grocery");
        blueCatsToRegionMap.put("B201", "produce");
        blueCatsToRegionMap.put("B301", "lifestyle");
    }
}
