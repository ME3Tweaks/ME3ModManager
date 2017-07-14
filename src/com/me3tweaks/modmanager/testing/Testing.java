package com.me3tweaks.modmanager.testing;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.objects.ThirdPartyModInfo;

/**
 * Contains test classes
 * 
 * @author Mgamerz
 *
 */
public class Testing {

	public static void Test_validateTelemetryFoldernames() throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		JSONObject dbObj = (JSONObject) parser.parse(ModManager.THIRD_PARTY_MOD_JSON);
		for (Object key : dbObj.keySet()) {
			//based on you key typel
			String keyStr = (String) key;
			JSONObject keyVal = (JSONObject) dbObj.get(keyStr);
			ThirdPartyModInfo tpmi = new ThirdPartyModInfo(keyStr, keyVal);

			String importFoldername = tpmi.getModname();//.replaceAll("'", "");
			String localModPath = ModManager.getTestingDir() + importFoldername;
			FileUtils.forceMkdir(new File(localModPath));
		}
	}
}
