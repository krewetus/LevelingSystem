package net.owlery.statsystem.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;
import java.util.HashSet;
import java.util.Set;

public class PlayerStatCapability {
    private Set<String> unlockedTitles = new HashSet<>();
    private Set<String> unlockedJobs = new HashSet<>();
    private String equippedTitle = "Noob";
    private String equippedJob = "None";
    private int usedPoints = 0;
    private int availablePoints = 0;
    private int healthStat = 0;
    private int strengthStat = 0;
    private int agilityStat = 0;
    private int pointsPurchased = 0;
    private int charismaStat = 0;

    public PlayerStatCapability() {
        // Ensure default titles and jobs are unlocked
        unlockedTitles.add("Noob");
        unlockedJobs.add("None");
    }

    public Set<String> getUnlockedTitles() { return unlockedTitles; }
    public Set<String> getUnlockedJobs() { return unlockedJobs; }
    public String getEquippedTitle() { return equippedTitle; }
    public String getEquippedJob() { return equippedJob; }
    public int getUsedPoints() { return usedPoints; }
    public int getAvailablePoints() { return availablePoints; }
    public int getHealthStat() { return healthStat; }
    public int getStrengthStat() { return strengthStat; }
    public int getAgilityStat() { return agilityStat; }
    public int getPointsPurchased() { return pointsPurchased; }
    public int getCharismaStat() { return charismaStat; }

    public void unlockTitle(String title) { unlockedTitles.add(title); }
    public void unlockJob(String job) { unlockedJobs.add(job); }

    public void equipTitle(String title) {
        if (unlockedTitles.contains(title)) this.equippedTitle = title;
    }
    public void equipJob(String job) {
        if (unlockedJobs.contains(job)) this.equippedJob = job;
    }

    public void setUsedPoints(int value) { usedPoints = value; }
    public void setAvailablePoints(int points) {
        this.availablePoints = Math.max(0, points); // Prevent negative points
    }
    public void setHealthStat(int value) { healthStat = Math.max(0, Math.min(100, value)); }
    public void setStrengthStat(int value) { strengthStat = Math.max(0, Math.min(100, value)); }
    public void setAgilityStat(int value) { agilityStat = Math.max(0, Math.min(100, value)); }
    public void setPointsPurchased(int value) { pointsPurchased = value; }
    public void setCharismaStat(int value) { charismaStat = Math.max(0, Math.min(100, value)); }

    public void usePoint() {
        if (this.availablePoints > 0) {
            this.availablePoints--;
            this.usedPoints++;
        }
    }

    public void buyPoint() {
        this.pointsPurchased++;
        this.availablePoints++;
    }

    public void saveNBTData(CompoundTag tag) {
        tag.putString("EquippedTitle", equippedTitle);
        tag.putString("EquippedJob", equippedJob);
        ListTag titleList = new ListTag();
        for (String t : unlockedTitles) titleList.add(StringTag.valueOf(t));
        tag.put("UnlockedTitles", titleList);
        ListTag jobList = new ListTag();
        for (String j : unlockedJobs) jobList.add(StringTag.valueOf(j));
        tag.put("UnlockedJobs", jobList);
        tag.putInt("UsedPoints", usedPoints);
        tag.putInt("AvailablePoints", availablePoints);
        tag.putInt("HealthStat", healthStat);
        tag.putInt("StrengthStat", strengthStat);
        tag.putInt("AgilityStat", agilityStat);
        tag.putInt("PointsPurchased", pointsPurchased);
        tag.putInt("CharismaStat", charismaStat);
    }
    public void loadNBTData(CompoundTag tag) {
        // Always ensure unlocked sets have defaults first
        unlockedTitles = new HashSet<>();
        unlockedJobs = new HashSet<>();
        ListTag titleList = tag.getList("UnlockedTitles", 8);
        for (int i = 0; i < titleList.size(); i++) unlockedTitles.add(titleList.getString(i));
        ListTag jobList = tag.getList("UnlockedJobs", 8);
        for (int i = 0; i < jobList.size(); i++) unlockedJobs.add(jobList.getString(i));
        ensureDefaults();

        // Now load equipped, defaulting only if missing or not unlocked
        String loadedTitle = tag.contains("EquippedTitle") ? tag.getString("EquippedTitle") : "Noob";
        String loadedJob = tag.contains("EquippedJob") ? tag.getString("EquippedJob") : "None";
        equippedTitle = unlockedTitles.contains(loadedTitle) ? loadedTitle : "Noob";
        equippedJob = unlockedJobs.contains(loadedJob) ? loadedJob : "None";

        usedPoints = tag.contains("UsedPoints") ? tag.getInt("UsedPoints") : 0;
        availablePoints = tag.contains("AvailablePoints") ? tag.getInt("AvailablePoints") : 0;
        healthStat = tag.contains("HealthStat") ? tag.getInt("HealthStat") : 0;
        strengthStat = tag.contains("StrengthStat") ? tag.getInt("StrengthStat") : 0;
        agilityStat = tag.contains("AgilityStat") ? tag.getInt("AgilityStat") : 0;
        pointsPurchased = tag.contains("PointsPurchased") ? tag.getInt("PointsPurchased") : 0;
        charismaStat = tag.contains("CharismaStat") ? tag.getInt("CharismaStat") : 0;
    }

    // Utility: ensure defaults always unlocked
    public void ensureDefaults() {
        unlockedTitles.add("Noob");
        unlockedJobs.add("None");
        if (equippedTitle == null) equippedTitle = "Noob";
        if (equippedJob == null) equippedJob = "None";
    }
} 