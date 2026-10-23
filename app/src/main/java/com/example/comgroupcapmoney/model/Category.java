package com.example.comgroupcapmoney.model;

import android.graphics.Color;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Category implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("icon")
    private String icon;

    @SerializedName("color_hex")
    private String colorHex;

    @SerializedName("type")
    private String type;

    @SerializedName("is_system")
    private boolean system;

    public Category() {}

    public Category(int id, String name, String icon, String colorHex, String type, boolean system) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.colorHex = colorHex;
        this.type = type;
        this.system = system;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getType() {
        return type;
    }

    public boolean isSystem() {
        return system;
    }

    public int getColorInt() {
        if (colorHex == null || colorHex.trim().isEmpty()) {
            return Color.GRAY;
        }
        try {
            return Color.parseColor(colorHex);
        } catch (IllegalArgumentException e) {
            return Color.GRAY;
        }
    }
}
