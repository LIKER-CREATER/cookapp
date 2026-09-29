package com.example.cookapp.database;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Recipe.class, Favorite.class, ShoppingItem.class, User.class, MealPlan.class, CookingHistory.class}, version = 11)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String TAG = "AppDatabase";
    private static volatile AppDatabase INSTANCE;
    private static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    public abstract RecipeDao recipeDao();
    public abstract ShoppingItemDao shoppingItemDao();
    public abstract UserDao userDao();
    public abstract FavoriteDao favoriteDao();
    public abstract MealPlanDao mealPlanDao();
    public abstract CookingHistoryDao cookingHistoryDao();
    
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 删除旧表
            database.execSQL("DROP TABLE IF EXISTS favorites");
            database.execSQL("DROP TABLE IF EXISTS users");
            database.execSQL("DROP TABLE IF EXISTS shopping_items");
            
            // 创建新表
            database.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "username TEXT, " +
                    "password TEXT, " +
                    "favoriteRecipeIds TEXT)");
                    
            database.execSQL("CREATE TABLE IF NOT EXISTS favorites (" +
                    "userId INTEGER NOT NULL, " +
                    "recipeId INTEGER NOT NULL, " +
                    "PRIMARY KEY(userId, recipeId), " +
                    "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE)");
                    
            database.execSQL("CREATE TABLE IF NOT EXISTS shopping_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT, " +
                    "quantity INTEGER NOT NULL, " +
                    "isChecked INTEGER NOT NULL, " +
                    "recipeId INTEGER, " +
                    "FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE)");
                    
            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_favorites_userId ON favorites (userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_favorites_recipeId ON favorites (recipeId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_shopping_items_recipeId ON shopping_items (recipeId)");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 修改shopping_items表，将quantity字段类型从TEXT改为INTEGER
            database.execSQL("CREATE TABLE IF NOT EXISTS shopping_items_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT, " +
                    "quantity INTEGER NOT NULL, " +
                    "isChecked INTEGER NOT NULL, " +
                    "recipeId INTEGER, " +
                    "FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE)");

            // 复制数据
            database.execSQL("INSERT INTO shopping_items_new (id, name, quantity, isChecked, recipeId) " +
                    "SELECT id, name, CAST(quantity AS INTEGER), isChecked, recipeId FROM shopping_items");

            // 删除旧表
            database.execSQL("DROP TABLE shopping_items");

            // 重命名新表
            database.execSQL("ALTER TABLE shopping_items_new RENAME TO shopping_items");

            // 重新创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_shopping_items_recipeId ON shopping_items (recipeId)");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 添加 isLoggedIn 列到 users 表
            database.execSQL("ALTER TABLE users ADD COLUMN isLoggedIn INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 重新创建shopping_items表，确保quantity字段为INTEGER类型
            database.execSQL("CREATE TABLE IF NOT EXISTS shopping_items_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT, " +
                    "quantity INTEGER NOT NULL, " +
                    "isChecked INTEGER NOT NULL, " +
                    "recipeId INTEGER, " +
                    "FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE)");

            // 复制数据
            database.execSQL("INSERT INTO shopping_items_new (id, name, quantity, isChecked, recipeId) " +
                    "SELECT id, name, CAST(quantity AS INTEGER), isChecked, recipeId FROM shopping_items");

            // 删除旧表
            database.execSQL("DROP TABLE shopping_items");

            // 重命名新表
            database.execSQL("ALTER TABLE shopping_items_new RENAME TO shopping_items");

            // 重新创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_shopping_items_recipeId ON shopping_items (recipeId)");
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 重新创建shopping_items表，添加外键约束
            database.execSQL("CREATE TABLE IF NOT EXISTS shopping_items_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT, " +
                    "quantity INTEGER NOT NULL, " +
                    "isChecked INTEGER NOT NULL, " +
                    "recipeId INTEGER, " +
                    "FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE ON UPDATE NO ACTION)");

            // 复制数据
            database.execSQL("INSERT INTO shopping_items_new (id, name, quantity, isChecked, recipeId) " +
                    "SELECT id, name, quantity, isChecked, recipeId FROM shopping_items");

            // 删除旧表
            database.execSQL("DROP TABLE shopping_items");

            // 重命名新表
            database.execSQL("ALTER TABLE shopping_items_new RENAME TO shopping_items");

            // 重新创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_shopping_items_recipeId ON shopping_items (recipeId)");
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 重新创建shopping_items表，使recipeId可以为空
            database.execSQL("CREATE TABLE IF NOT EXISTS shopping_items_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT, " +
                    "quantity INTEGER NOT NULL, " +
                    "isChecked INTEGER NOT NULL, " +
                    "recipeId INTEGER, " +
                    "FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE ON UPDATE NO ACTION)");

            // 复制数据
            database.execSQL("INSERT INTO shopping_items_new (id, name, quantity, isChecked, recipeId) " +
                    "SELECT id, name, quantity, isChecked, recipeId FROM shopping_items");

            // 删除旧表
            database.execSQL("DROP TABLE shopping_items");

            // 重命名新表
            database.execSQL("ALTER TABLE shopping_items_new RENAME TO shopping_items");

            // 重新创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_shopping_items_recipeId ON shopping_items (recipeId)");
        }
    };

    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 重新创建users表
            database.execSQL("CREATE TABLE IF NOT EXISTS users_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "username TEXT, " +
                    "password TEXT, " +
                    "favoriteRecipeIds TEXT, " +
                    "isLoggedIn INTEGER NOT NULL DEFAULT 0)");

            // 复制数据
            database.execSQL("INSERT INTO users_new (id, username, password, favoriteRecipeIds, isLoggedIn) " +
                    "SELECT id, username, password, favoriteRecipeIds, isLoggedIn FROM users");

            // 删除旧表
            database.execSQL("DROP TABLE users");

            // 重命名新表
            database.execSQL("ALTER TABLE users_new RENAME TO users");

            // 删除旧的favorites表
            database.execSQL("DROP TABLE IF EXISTS favorites");

            // 创建新的favorites表，使用联合主键
            database.execSQL("CREATE TABLE IF NOT EXISTS favorites (" +
                    "userId INTEGER NOT NULL, " +
                    "recipeId INTEGER NOT NULL, " +
                    "PRIMARY KEY(userId, recipeId), " +
                    "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE)");

            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_favorites_userId ON favorites (userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_favorites_recipeId ON favorites (recipeId)");
        }
    };
    
    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS meal_plans (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "userId INTEGER NOT NULL, " +
                    "recipeId INTEGER NOT NULL, " +
                    "planDate INTEGER, " +
                    "mealType TEXT, " +
                    "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE)");

            database.execSQL("CREATE INDEX IF NOT EXISTS index_meal_plans_userId ON meal_plans (userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_meal_plans_recipeId ON meal_plans (recipeId)");

            database.execSQL("CREATE TABLE IF NOT EXISTS cooking_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "userId INTEGER NOT NULL, " +
                    "recipeId INTEGER NOT NULL, " +
                    "cookCount INTEGER NOT NULL DEFAULT 0, " +
                    "lastCookedAt INTEGER, " +
                    "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE, " +
                    "UNIQUE(userId, recipeId))");

            database.execSQL("CREATE INDEX IF NOT EXISTS index_cooking_history_userId ON cooking_history (userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_cooking_history_recipeId ON cooking_history (recipeId)");
        }
    };

    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE INDEX IF NOT EXISTS index_meal_plans_planDate ON meal_plans (planDate)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_cooking_history_userId_recipeId ON cooking_history (userId, recipeId)");
        }
    };

    private static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS shopping_items_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT, " +
                    "quantity INTEGER NOT NULL, " +
                    "unit TEXT, " +
                    "isChecked INTEGER NOT NULL, " +
                    "recipeId INTEGER, " +
                    "FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE ON UPDATE NO ACTION)");

            database.execSQL("INSERT INTO shopping_items_new (id, name, quantity, unit, isChecked, recipeId) " +
                    "SELECT id, name, quantity, '', isChecked, recipeId FROM shopping_items");

            database.execSQL("DROP TABLE shopping_items");
            database.execSQL("ALTER TABLE shopping_items_new RENAME TO shopping_items");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_shopping_items_recipeId ON shopping_items (recipeId)");
        }
    };

    // MIGRATION_10_11: add unit TEXT to shopping_items (correct version above at line 258)
    
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    Log.d(TAG, "创建新的数据库实例");
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "cookapp_database")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                                         MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
} 