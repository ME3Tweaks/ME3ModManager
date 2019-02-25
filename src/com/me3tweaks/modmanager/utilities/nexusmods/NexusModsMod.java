package com.me3tweaks.modmanager.utilities.nexusmods;

import org.json.simple.JSONObject;

public class NexusModsMod {
    public final String name;
    public final String summary;
    public final String description;
    public final String picture_url;
    public final int mod_id;
    public final String version;
    public final String author;

    public NexusModsMod(JSONObject obj) {
        name = (String) obj.get("name");
        summary = (String) obj.get("summary");
        description = (String) obj.get("description");
        picture_url = (String) obj.get("picture_url");
        mod_id = (int) obj.get("mod_id");
        version = (String) obj.get("version");
        author = (String) obj.get("author");
    }
}
