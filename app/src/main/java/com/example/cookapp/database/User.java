package com.example.cookapp.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String username;
    private String password;
    private String favoriteRecipeIds;
    private boolean isLoggedIn;

    public User() {
        this.isLoggedIn = false;
    }

    @Ignore
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.isLoggedIn = false;
        this.favoriteRecipeIds = "";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFavoriteRecipeIds() {
        return favoriteRecipeIds;
    }

    public void setFavoriteRecipeIds(String favoriteRecipeIds) {
        this.favoriteRecipeIds = favoriteRecipeIds;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
} 