package com.me3tweaks.modmanager.valueparsers.mpstorepack;

import java.util.ArrayList;

import com.me3tweaks.modmanager.valueparsers.ValueParserLib;

public class PackMetadata {
	//(nID=0, PackName="starter",Title=705015, SubTitle=, Description=705016, 
	//CreditCost=0, srPromoString=703324, PerPlayerMax=1, 
	//ImageData=(ImageLocation=EStoreImageLocation_Local, ImageReference="GUI_MPImages.StoreItems.Store001"), 
	//RevealIntroTextureRef="GUI_MPImages.Creates.box", 
	//RevealIntroHoloTextureRef="GUI_MPImages.Creates.boxHolo", RevealIntroSound=MPRevealCrate1)
	
	private String packname;
	private String title;
	private String description;
	private int cost;
	private String promotext;
	private int maxPurchases = 0;
	private String image;
	private int srTitle, srSubtitle, srDescription, srPromoString;

	public PackMetadata(String metadataStr) {
		packname = ValueParserLib.getStringProperty(metadataStr, "PackName", true);
		srTitle = ValueParserLib.getIntProperty(metadataStr, "Title");
		srSubtitle = ValueParserLib.getIntProperty(metadataStr, "SubTitle");
		srDescription = ValueParserLib.getIntProperty(metadataStr, "Description");
		srPromoString = ValueParserLib.getIntProperty(metadataStr, "srPromoString");
		maxPurchases = ValueParserLib.getIntProperty(metadataStr, "PerPlayerMax");
		cost = ValueParserLib.getIntProperty(metadataStr, "CreditCost");
		description = CardParser.livetlkMap.get(srDescription);
	}

	public String getPackname() {
		return packname;
	}

	public void setPackname(String packname) {
		this.packname = packname;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public String getPromotext() {
		return promotext;
	}

	public void setPromotext(String promotext) {
		this.promotext = promotext;
	}

	public int getMaxPurchases() {
		return maxPurchases;
	}

	public void setMaxPurchases(int maxPurchases) {
		this.maxPurchases = maxPurchases;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public int getSrTitle() {
		return srTitle;
	}

	public void setSrTitle(int srTitle) {
		this.srTitle = srTitle;
	}

	public int getSrSubtitle() {
		return srSubtitle;
	}

	public void setSrSubtitle(int srSubtitle) {
		this.srSubtitle = srSubtitle;
	}

	public int getSrDescription() {
		return srDescription;
	}

	public void setSrDescription(int srDescription) {
		this.srDescription = srDescription;
	}

	public int getSrPromoString() {
		return srPromoString;
	}

	public void setSrPromoString(int srPromoString) {
		this.srPromoString = srPromoString;
	}
}
