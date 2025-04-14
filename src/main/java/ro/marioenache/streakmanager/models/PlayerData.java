package ro.marioenache.streakmanager.models;

import java.util.UUID;

public class PlayerData {
    private UUID uuid;
    private int streak;
    private String name;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.streak = 0;
        this.name = "";
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}