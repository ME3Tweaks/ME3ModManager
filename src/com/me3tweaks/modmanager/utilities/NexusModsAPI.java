package com.me3tweaks.modmanager.utilities;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.utilities.nexusmods.NexusModsFileInfo;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.fluent.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;

public class NexusModsAPI {
    private static String API_ENDPOINT_BASE = "https://api.nexusmods.com/v1/";
    private static String PERSONAL_API_KEY = "";
    private static String SSO_API_KEY = "";
    private static String GAME_NAME = "masseffect3";

    public static NexusModsFileInfo GetModInfoByMD5(String md5) {
        try {
            ModManager.debugLogger.writeMessage("Looking up mod info on nexusmods via MD5: "+md5);
            if (PERSONAL_API_KEY.equals("")) { LoadAPIKeyFromDisk(); }
            String md5SeachResult = Request.Get(API_ENDPOINT_BASE + "games/" + GAME_NAME + "/mods/md5_search/" + md5 + ".json")
                    .addHeader("apikey", PERSONAL_API_KEY)
                    .setHeader("User-Agent", "Mass Effect 3 Mod Manager/Build " + ModManager.BUILD_NUMBER + " (Java) on " + System.getProperty("os.name"))
                    .execute().returnContent().asString();

            JSONParser parser = new JSONParser();
            JSONArray results = (JSONArray) parser.parse(md5SeachResult);
            if (results.size() != 1) {
                return null; //More or less than one result.
            }
            return new NexusModsFileInfo((JSONObject)((JSONObject)results.get(0)).get("file_details"));
        } catch (Exception e) {
            ModManager.debugLogger.writeError("Error getting mod info by MD5: "+e.getMessage());
        }
        return null;
    }


    /**
     * Loads your personal API key from disk. Do not commit this file to a repository.
     */
    public static void LoadAPIKeyFromDisk() {
        try {
            PERSONAL_API_KEY = FileUtils.readFileToString(new File("NM_PersonalAPIKey.txt"), "UTF-8");
            ModManager.debugLogger.writeMessage("Loaded NexusMods API key from disk");
        } catch (Exception e) {
            ModManager.debugLogger.writeError("Could not read personal API key from disk");
        }
    }
}
