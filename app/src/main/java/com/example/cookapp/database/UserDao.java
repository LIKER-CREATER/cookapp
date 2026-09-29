package com.example.cookapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);
    
    @Update
    void update(User user);
    
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);
    
    @Query("SELECT * FROM users WHERE username = :username")
    User getUserByUsername(String username);
    
    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserById(int userId);
    
    @Query("UPDATE users SET password = :newPassword WHERE username = :username")
    void updatePassword(String username, String newPassword);
    
    @Delete
    void delete(User user);
    
    @Query("DELETE FROM users")
    void deleteAllUsers();

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    User getCurrentUser();

    @Query("UPDATE users SET isLoggedIn = 0")
    void logoutAll();

    @Query("UPDATE users SET isLoggedIn = 0 WHERE id = :userId")
    void logout(long userId);
} 