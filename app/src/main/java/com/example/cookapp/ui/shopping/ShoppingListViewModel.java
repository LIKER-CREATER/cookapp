package com.example.cookapp.ui.shopping;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.cookapp.database.AppDatabase;
import com.example.cookapp.database.ShoppingItem;
import com.example.cookapp.database.ShoppingItemDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShoppingListViewModel extends AndroidViewModel {
    private final ShoppingItemDao shoppingItemDao;
    private final ExecutorService executorService;
    private final LiveData<List<ShoppingItem>> allItems;

    public ShoppingListViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        shoppingItemDao = db.shoppingItemDao();
        executorService = Executors.newSingleThreadExecutor();
        allItems = shoppingItemDao.getAllItems();
    }

    public LiveData<List<ShoppingItem>> getShoppingItems() {
        return allItems;
    }

    public void insert(ShoppingItem item) {
        executorService.execute(() -> shoppingItemDao.insert(item));
    }

    public void update(ShoppingItem item) {
        executorService.execute(() -> shoppingItemDao.update(item));
    }

    public void delete(ShoppingItem item) {
        executorService.execute(() -> shoppingItemDao.delete(item));
    }

    public void clearCheckedItems() {
        executorService.execute(shoppingItemDao::deleteCheckedItems);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
} 