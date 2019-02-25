package com.me3tweaks.modmanager.utilities.nexusmods;

import org.json.simple.JSONObject;

public class NexusModsFileInfo {
    public final long file_id;
    public final String name;
    public final String version;
    public final long size;
    public final String changeloghtml;
    public final String md5;
    public final String mod_version;
    public final String file_name;

    /**
     * Information about a file on NexusMods. Note this is not a mod, but rather a file download.
     * @param jsonObj
     */
    public NexusModsFileInfo(JSONObject jsonObj) {
        file_id = (long) jsonObj.get("file_id");
        name = (String) jsonObj.get("name");
        version = (String) jsonObj.get("version");
        size = (long) jsonObj.get("size");
        file_name = (String) jsonObj.get("file_name");
        mod_version = (String) jsonObj.get("mod_version");
        md5 = (String) jsonObj.get("md5");
        changeloghtml = (String) jsonObj.get("changelog_html");
    }
}
