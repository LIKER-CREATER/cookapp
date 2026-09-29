package com.example.cookapp.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "shopping_items",
        foreignKeys = @ForeignKey(entity = Recipe.class,
                parentColumns = "id",
                childColumns = "recipeId",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.NO_ACTION),
        indices = {@Index("recipeId")})
public class ShoppingItem {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private Long recipeId;
    private String name;
    private int quantity;
    private String unit;
    private boolean isChecked;

    public ShoppingItem() {
        this.isChecked = false;
    }

    @Ignore
    public ShoppingItem(Long recipeId, String name, int quantity, String unit) {
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.isChecked = false;
    }

    @Ignore
    public ShoppingItem(Long recipeId, String name, int quantity) {
        this(recipeId, name, quantity, "");
    }

    @Ignore
    public ShoppingItem(String name, int quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.isChecked = false;
    }

    @Ignore
    public ShoppingItem(String name, int quantity) {
        this(name, quantity, "");
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
} 