package com.me3tweaks.modmanager.valueparsers.bioweapon;

import java.util.ArrayList;

public class HTMLParams {
	private ArrayList<String> balanceChanges;
	private String description;
	private String loadName;
	private String weaponName;
	private String humanName;
	private String fileName;
	private boolean hasInfiniteAmmo = false;
	private boolean hasRampUp = false;
	private boolean hasHeatUp = false;
	private boolean hasKishockRecoil = false;
	private boolean hasRecoil = true;
	private boolean hasZoomRecoil = true;
	private boolean hasZoomPenalty = true;
	private boolean hasHipPenalty = true;
	private boolean hasAmmoPerShot = false;
	private boolean hasForceFireAfterCharge = false;
	private boolean hasMinCharge = false;
	private boolean hasMaxCharge = false;
	private boolean isChakramLauncher = false;
	private boolean hasHeadshotMultiplier = false;
	private boolean hasDamageOverTime = false;
	private boolean isPunisher = false;
	private boolean hasRoundsPerBurst = true;
	private boolean isTyphoon = false;
	private boolean hasAimError = true;
	private boolean hasZoomAimError = true;
	private boolean hasMinRefireTime = false;
	private boolean hasTraceRange = false;
	private boolean isGethShotgun = false;
	private boolean isArcPistol = false;
	private boolean isSilencer = false;
	private boolean hasPenetration = false;
	private boolean isVenom = false;

	public boolean isVenom() {
		return isVenom;
	}

	public void setVenom(boolean isVenom) {
		this.isVenom = isVenom;
	}

	public boolean hasMinCharge() {
		return hasMinCharge;
	}
	
	public void setMinCharge(boolean hasMinCharge) {
		this.hasMinCharge = hasMinCharge;
	}
	
	public void setMaxCharge(boolean hasMaxCharge) {
		this.hasMaxCharge = hasMaxCharge;
	}
	
	public boolean hasMaxCharge() {
		return hasMaxCharge;
	}
	

	
	
	public boolean isSilencer() {
		return isSilencer;
	}

	public void setSilencer(boolean isSilencer) {
		this.isSilencer = isSilencer;
	}
	
	public boolean isArcPistol() {
		return isArcPistol;
	}

	public void setArcPistol(boolean isArcPistol) {
		this.isArcPistol = isArcPistol;
	}

	public boolean hasPenetration() {
		return hasPenetration;
	}
	
	public void setPenetration(boolean hasPenetration) {
		this.hasPenetration = hasPenetration;
	}
	
	public boolean isGethShotgun() {
		return isGethShotgun;
	}

	public void setGethShotgun(boolean isGethShotgun) {
		this.isGethShotgun = isGethShotgun;
	}

	public boolean hasTraceRange() {
		return hasTraceRange;
	}
	
	public void setTraceRange(boolean hasTraceRange) {
		this.hasTraceRange = hasTraceRange;
	}
	
	public boolean hasMinRefireTime() {
		return hasMinRefireTime;
	}
	
	public void setMinRefireTime(boolean hasMinRefireTime) {
		this.hasMinRefireTime = hasMinRefireTime;
	}
	
	public boolean hasRoundsPerBurst() {
		return hasRoundsPerBurst;
	}
	
	public void setRoundsPerBurst(boolean hasRoundsPerBurst) {
		this.hasRoundsPerBurst = hasRoundsPerBurst;
	}
	
	public boolean hasHeadshotMultiplier() {
		return hasHeadshotMultiplier;
	}
	
	public void setHeadshotMultiplier(boolean hasHeadshotMultiplier) {
		this.hasHeadshotMultiplier = hasHeadshotMultiplier;
	}
	
	public boolean isChakramLauncher() {
		return isChakramLauncher;
	}

	public void setChakramLauncher(boolean isChakramLauncher) {
		this.isChakramLauncher = isChakramLauncher;
	}

	public boolean hasZoomRecoil() {
		return hasZoomRecoil;
	}
	
	public void setZoomRecoil(boolean hasZoomRecoil) {
		this.hasZoomRecoil = hasZoomRecoil;
	}
	
	public boolean hasForceFireAfterCharge() {
		return hasForceFireAfterCharge;
	}
	
	public void setForceFireAfterCharge(boolean hasForceFireAfterCharge) {
		this.hasForceFireAfterCharge = hasForceFireAfterCharge;
	}
	
	public void setRecoil(boolean hasRecoil) {
		this.hasRecoil = hasRecoil;
	}
	
	public boolean hasRecoil() {
		return hasRecoil;
	}
	
	public void setZoomPenalty(boolean hasZoomPenalty) {
		this.hasZoomPenalty = hasZoomPenalty;
	}
	
	public boolean hasZoomPenalty() {
		return hasZoomPenalty;
	}
	
	public boolean hasHipPenalty() {
		return hasHipPenalty;
	}
	

	public void setHipPenalty(boolean hasHipPenalty) {
		this.hasHipPenalty = hasHipPenalty;
	}
	

	
	public boolean hasKishockRecoil() {
		return hasKishockRecoil;
	}
	public void setKishockRecoil(boolean hasKishockRecoil) {
		this.hasKishockRecoil = hasKishockRecoil;
	}
	public boolean hasHeatUp() {
		return hasHeatUp;
	}
	public void setHeatUp(boolean hasHeatUp) {
		this.hasHeatUp = hasHeatUp;
	}
	public boolean hasRampUp() {
		return hasRampUp;
	}
	public void setRampUp(boolean hasRampUp) {
		this.hasRampUp = hasRampUp;
	}
	public ArrayList<String> getBalanceChanges() {
		return balanceChanges;
	}
	public void setBalanceChanges(ArrayList<String> balanceChanges) {
		this.balanceChanges = balanceChanges;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLoadName() {
		return loadName;
	}
	public void setLoadName(String loadName) {
		this.loadName = loadName;
	}
	public String getWeaponName() {
		return weaponName;
	}
	public void setWeaponName(String weaponName) {
		this.weaponName = weaponName;
	}
	public String getHumanName() {
		return humanName;
	}
	public void setHumanName(String humanName) {
		this.humanName = humanName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public boolean hasInfiniteAmmo() {
		return hasInfiniteAmmo;
	}
	public void setInfiniteAmmo(boolean hasInfiniteAmmo) {
		this.hasInfiniteAmmo = hasInfiniteAmmo;
	}
	public void addBalanceChange(String string) {
		if (balanceChanges == null) {
			balanceChanges = new ArrayList<String>();
		}
		balanceChanges.add(string);
	}

	public void setAmmoPerShot(boolean hasAmmoPerShot) {
		this.hasAmmoPerShot = hasAmmoPerShot;
	}
	
	public boolean hasAmmoPerShot() {
		return this.hasAmmoPerShot;
	}

	public boolean hasDamageOverTime() {
		return hasDamageOverTime;
	}
	
	public void setDamageOverTime(boolean hasDamageOverTime) {
		this.hasDamageOverTime = hasDamageOverTime;
	}

	public void setPunisher(boolean isPunisher) {
		this.isPunisher = isPunisher;
	}

	public boolean isPunisher() {
		return isPunisher;
	}

	public boolean isTyphoon() {
		return isTyphoon;
	}
	
	public void setTyphoon(boolean isTyphoon) {
		this.isTyphoon = isTyphoon;
	}
	
	public void setAimError(boolean hasAimError) {
		this.hasAimError  = hasAimError;
	}

	public boolean hasAimError() {
		return hasAimError;
	}

	public boolean hasZoomAimError() {
		return hasZoomAimError ;
	}
	
	public void setZoomAimError(boolean hasZoomAimError) {
		this.hasZoomAimError  = hasZoomAimError;
	}
}
