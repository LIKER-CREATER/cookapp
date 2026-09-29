package com.example.cookapp.util;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.cookapp.R;

import java.io.File;

/**
 * 统一的食谱图片加载工具。
 * 加载顺序：1. drawable 资源（内置示例图） -> 2. 内部存储文件（用户新增图） -> 3. 占位图
 */
public class RecipeImageLoader {
    private static final String TAG = "RecipeImageLoader";
    private static final String IMAGE_DIR = "recipe_images";

    public static void load(Context context, ImageView imageView, String imageName) {
        if (context == null || imageView == null || imageName == null || imageName.isEmpty()) {
            Log.w(TAG, "load: 参数无效，使用占位图");
            imageView.setImageResource(R.drawable.placeholder_image);
            return;
        }

        String rawName = imageName;
        // 去除可能的后缀
        String resourceName = rawName.replace(".jpg", "").replace(".png", "");

        // 1. 尝试 drawable 资源（内置示例图，如 mapo_tofu）
        int drawableResId = context.getResources().getIdentifier(
                resourceName, "drawable", context.getPackageName());
        if (drawableResId != 0) {
            Log.d(TAG, "命中 drawable 资源: " + resourceName);
            Glide.with(context)
                    .load(drawableResId)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(imageView);
            return;
        }

        // 2. 尝试内部存储文件（用户新增图，如 recipe_1720000000000.jpg）
        File imageFile = new File(context.getFilesDir(), IMAGE_DIR + "/" + rawName);
        if (imageFile.exists()) {
            Log.d(TAG, "命中文件: " + imageFile.getAbsolutePath());
            Glide.with(context)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(imageView);
            return;
        }

        // 3. 两者都失败，显示占位图
        Log.w(TAG, "drawable 和文件均未命中，使用占位图，imageName=" + rawName);
        imageView.setImageResource(R.drawable.placeholder_image);
    }
}
