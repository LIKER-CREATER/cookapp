package com.example.cookapp;

import android.app.Application;
import android.util.Log;
import com.example.cookapp.database.AppDatabase;
import com.example.cookapp.database.CookingHistory;
import com.example.cookapp.database.Favorite;
import com.example.cookapp.database.MealPlan;
import com.example.cookapp.database.Recipe;
import com.example.cookapp.database.User;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CookApp extends Application {
    private static final String TAG = "CookApp";
    public static final List<Recipe> SAMPLE_RECIPES = Arrays.asList(

            new Recipe(
                    "白切鸡",
                    "三黄鸡1只，姜片15g，葱段20g，盐10g，料酒20ml，香油适量",
                    "1. 三黄鸡处理干净，冷水下锅，放入姜片、葱段、料酒；\n2. 大火煮沸后转小火，保持微沸状态煮 15-20 分钟，关火焖 10 分钟；\n3. 捞出鸡放入冰水中浸泡至凉透，捞出沥干；\n4. 表面刷一层香油，切块装盘，搭配盐蘸食",
                    "baiqieji"
            ),
            new Recipe(
                    "卤牛肉",
                    "牛腱子肉1000g，八角3颗，桂皮1小块，香叶3片，花椒1小把，干辣椒5个，草果1个，姜片15g，葱段20g，生抽100ml，老抽50ml，冰糖20g，盐适量",
                    "1. 牛腱子肉冷水浸泡 2 小时，中途换水，去除血水后捞出沥干；\n2. 冷水下锅，加入姜片、葱段、料酒，焯水 5 分钟后捞出洗净；\n3. 锅中加适量清水，放入所有香料、生抽、老抽、冰糖、盐，大火煮开制成卤汁；\n4. 放入牛腱子肉，大火煮沸后转小火慢炖 1.5-2 小时，关火后继续浸泡 4-6 小时使其入味；\n5. 捞出切片即可",
                    "luniurou"
            ),
            new Recipe(
                    "松鼠鳜鱼",
                    "鳜鱼1条，冬笋20g，水发香菇20g，青豌豆10g，胡萝卜20g，番茄酱50g，白糖30g，白醋20ml，盐5g，料酒15ml，淀粉1勺，食用油适量",
                    "1. 鳜鱼处理干净，鱼身两面剞花刀，用盐、料酒腌制 15 分钟;\n2. 冬笋、香菇、胡萝卜切丁;\n3. 腌制好的鳜鱼拍满淀粉，抖掉多余淀粉，入热油锅炸至定型捞出;\n4. 待油温升高，复炸至金黄色捞出装盘；\n5. 锅中留少许底油，放入冬笋、香菇、胡萝卜丁、青豌豆翻炒，加入番茄酱、白糖、白醋、适量清水，煮至浓稠；\n6. 将酱汁浇在炸好的鳜鱼上",
                    "songshuguiyu"
            ),
            new Recipe(
                    "西湖醋鱼",
                    "草鱼1条，姜末10g，白糖60g，米醋50ml，酱油75ml，湿淀粉50g，料酒15ml，盐适量",
                    "1. 草鱼处理干净，鱼身两面剞花刀，用盐、料酒腌制；\n2. 锅中加足量清水，水开后放入草鱼，大火煮 7-8 分钟至熟，捞出装盘；\n3. 锅中留适量煮鱼的汤汁，加入酱油、白糖、米醋、姜末，大火煮沸；\n4. 用湿淀粉勾芡，煮至汤汁浓稠，浇在鱼身上",
                    "xihucuyu"
            ),
            new Recipe(
                    "佛跳墙",
                    "鲍鱼6只，海参2条，鱼翅50g，瑶柱30g，花胶50g，鸽子蛋6个，鹌鹑蛋6个，火腿30g，猪肚50g，猪蹄筋50g，杏鲍菇50g，干香菇20g，姜片10g，葱段15g，料酒30ml，高汤1勺，盐适量",
                    "1. 鲍鱼、海参、鱼翅、瑶柱、花胶等食材提前泡发处理，火腿切片，猪肚、猪蹄筋焯水；\n2. 鸽子蛋、鹌鹑蛋煮熟去壳；\n3. 干香菇泡发，杏鲍菇切块；\n4. 取一个大炖盅，依次放入各种食材，加入姜片、葱段、料酒、高汤；\n5. 炖盅加盖，小火慢炖 6-8 小时，最后加盐调味",
                    "fotiaoqiang"
            ),
            new Recipe(
                    "剁椒鱼头",
                    "胖头鱼头1个，剁椒150g，姜末10g，蒜末15g，蒸鱼豉油30ml，料酒20ml，盐5g，油适量",
                    "1. 胖头鱼头处理干净，从中间劈开但不切断，用盐、料酒腌制 15 分钟；\n2. 鱼头放入盘中，铺上剁椒、姜末、蒜末；\n3. 锅中水烧开，将鱼头放入蒸锅中，大火蒸 12-15 分钟；\n4. 取出鱼头，倒掉盘中多余的汁水，淋上蒸鱼豉油；\n5. 锅中烧热油，浇在鱼头上",
                    "duojiaoyutou"
            ),
            new Recipe(
                    "黄山臭鳜鱼",
                    "鳜鱼1条，姜末10g，蒜末15g，葱末15g，干辣椒5个，生抽30ml，老抽15ml，白糖10g，料酒20ml，盐适量，食用油适量",
                    "1. 鳜鱼用淡盐水洗净，鱼身两面剞花刀，用盐、料酒腌制，在 25℃左右环境下腌制 6 天左右使其发酵出独特臭味；\n2. 锅中倒油，油热后放入鳜鱼，煎至两面金黄盛出；\n3. 锅中留少许底油，放入姜末、蒜末、干辣椒炒香，加入生抽、老抽、白糖、适量清水煮开；\n4. 放入煎好的鳜鱼，小火焖煮 10-15 分钟，至汤汁浓稠，撒上葱末即可",
                    "huangshanchouguiyu"
            ),
            new Recipe(
            "炒土豆丝",
            "土豆2个，青椒1个，蒜末适量，盐适量，油适量",
            "1. 土豆切丝，青椒切丝\n2. 热锅加油，爆香蒜末\n3. 加入土豆丝翻炒\n4. 加入青椒丝继续翻炒\n5. 加入盐调味\n6. 炒至土豆丝变软即可",
            "chaotudousi"
        ),
        new Recipe(
            "红烧排骨",
            "排骨500g，姜片适量，料酒适量，酱油适量，糖适量，八角2个，桂皮1块",
            "1. 排骨焯水去血沫\n2. 热锅加油，煸炒排骨至微黄\n3. 加入姜片、料酒、酱油、糖翻炒\n4. 加入八角、桂皮和适量水，小火炖煮\n5. 炖至排骨软烂即可",
            "hongshaopaigu"
        ),
        new Recipe(
            "口水鸡",
            "鸡腿2个，姜片适量，料酒适量，辣椒油适量，花椒油适量，蒜末适量，葱花适量",
            "1. 鸡腿焯水去血沫\n2. 加入姜片、料酒煮熟\n3. 鸡腿切块装盘\n4. 调制酱汁：辣椒油、花椒油、蒜末混合\n5. 将酱汁淋在鸡块上\n6. 最后撒上葱花即可",
            "koushuiji"
        ),
        new Recipe(
            "水煮肉片",
            "猪里脊肉300g，豆芽200g，青菜200g，干辣椒适量，花椒适量，蒜末适量，葱花适量",
            "1. 猪肉切片，用料酒、盐腌制\n2. 豆芽、青菜焯水\n3. 热锅爆香干辣椒、花椒\n4. 加入肉片快速翻炒\n5. 加入豆芽、青菜翻炒\n6. 最后撒上蒜末、葱花即可",
            "shuizhuroupian"
        ),
        new Recipe(
            "蒜蓉生菜",
            "生菜500g，蒜末适量，盐适量，油适量",
            "1. 生菜洗净切段\n2. 热锅加油，爆香蒜末\n3. 加入生菜翻炒\n4. 加入盐调味\n5. 炒至生菜变软即可",
            "suanrongshengcai"
        ),
        new Recipe(
            "土豆烧肉",
            "土豆2个，五花肉300g，姜片适量，料酒适量，酱油适量，糖适量",
            "1. 五花肉切块，土豆切块\n2. 热锅加油，煸炒五花肉至出油\n3. 加入姜片、料酒、酱油、糖翻炒\n4. 加入土豆块继续翻炒\n5. 加入适量水，小火炖煮\n6. 炖至土豆软烂即可",
            "tudoushaorou"
        ),
            new Recipe(
                    "番茄炒蛋",
                    "番茄2个,鸡蛋3个,葱花适量,盐适量,糖适量,食用油适量",
                    "1. 番茄洗净切块\n2. 鸡蛋打散加入少许盐\n3. 热锅下油，倒入鸡蛋液翻炒至凝固\n4. 加入番茄块翻炒\n5. 加入盐和糖调味\n6. 最后撒上葱花即可",
                    "fanqiechaodan"
        ),
        new Recipe(
                "宫保鸡丁",
                "鸡胸肉300g，花生米50g，干辣椒10g，花椒5g，葱段20g，姜末5g，蒜末5g，料酒15ml，生抽20ml，醋15ml，糖10g，淀粉10g，盐适量",
                "1. 鸡胸肉切丁，加入盐、料酒、淀粉抓匀腌制15分钟；\n2. 调碗汁：生抽、醋、糖、淀粉和适量清水混合均匀备用；\n3. 花生米炸至金黄酥脆捞出，干辣椒剪成小段；\n4. 热锅宽油，下鸡丁滑炒至变色盛出；\n5. 锅留底油，下花椒、干辣椒段炒出红油，放入姜末、蒜末、葱段爆香；\n6. 下鸡丁翻炒，倒入碗汁大火收汁，出锅前撒入花生米即可",
                "gongbaojiding"
        ),
        new Recipe(
                "鱼香肉丝",
                "猪里脊300g，木耳30g，胡萝卜50g，青椒50g，泡椒10g，姜末5g，蒜末5g，葱末10g，料酒10ml，生抽15ml，醋12ml，糖15g，淀粉10g，盐适量",
                "1. 猪里脊切丝，加入盐、料酒、淀粉抓匀腌制15分钟；\n2. 木耳、胡萝卜、青椒切丝备用；\n3. 调碗汁：生抽、醋、糖、淀粉和适量清水混合均匀；\n4. 热锅宽油，下肉丝滑炒至变色盛出；\n5. 锅留底油，下泡椒、姜蒜末、葱末炒出红油；\n6. 下木耳、胡萝卜丝翻炒至软，再下青椒丝、肉丝，倒入碗汁翻炒均匀即可",
                "yuxiangrousi"
        ),
        new Recipe(
                "豉汁蒸排骨",
                "肋排500g，豆豉15g，蒜末10g，姜末5g，葱花5g，料酒15ml，生抽20ml，蚝油10g，糖5g，淀粉10g，盐适量",
                "1. 肋排剁成3cm小段，冷水浸泡30分钟去血水，捞出沥干；\n2. 豆豉剁碎，与蒜末、姜末、料酒、生抽、蚝油、糖、淀粉拌匀成酱汁；\n3. 将酱汁与排骨充分抓匀，腌制30分钟以上；\n4. 排骨码入盘中，大火烧开蒸锅水，放入排骨；\n5. 中火蒸25-30分钟至排骨熟透，出锅撒上葱花即可",
                "chijizhengpaigu"
        ),
        new Recipe(
                "蜜汁叉烧",
                "梅花肉500g，叉烧酱50g，生抽20ml，老抽10ml，蜂蜜30g，蒜末5g，姜末3g，料酒10ml，糖10g，盐适量",
                "1. 梅花肉切成约2cm厚的长条，用刀背轻敲两面帮助入味；\n2. 叉烧酱、生抽、老抽、蜂蜜、蒜末、姜末、料酒、糖、盐调匀成腌料；\n3. 肉条放入腌料中充分揉匀，盖上保鲜膜冷藏腌制4小时以上，隔夜更佳；\n4. 烤箱预热200℃，烤盘铺锡纸，肉条放上，刷一层腌料汁；\n5. 送入烤箱烤15分钟，取出翻面再刷一遍腌料，继续烤10分钟；\n6. 最后表面刷薄薄一层蜂蜜，烤2分钟至表面焦糖色即可",
                "mizhi_chashao"
        ),
        new Recipe(
                "糖醋里脊",
                "猪里脊300g，鸡蛋1个，淀粉80g，面粉30g，番茄酱40g，白醋30ml，白糖40g，料酒10ml，盐适量，食用油适量",
                "1. 猪里脊切成1.5cm厚的大丁，加入盐、料酒腌制10分钟；\n2. 淀粉、面粉、鸡蛋和适量清水调成浓稠面糊，将里脊丁裹满面糊；\n3. 油温烧至六成热，下里脊丁炸至定型金黄，捞出；\n4. 油温升高至八成热，复炸里脊至外酥里嫩，捞出沥油；\n5. 锅中留少许底油，小火炒香番茄酱，加入白醋、白糖和少量清水煮沸；\n6. 用水淀粉勾芡至浓稠，下里脊丁快速翻炒均匀裹上酱汁，出锅即可",
                "tangculiji"
        ),
        new Recipe(
                "清炖狮子头",
                "五花肉400g，荸荠80g，鸡蛋1个，葱末15g，姜末5g，料酒20ml，生抽15ml，盐8g，白胡椒粉2g，淀粉15g，高汤或清水适量",
                "1. 五花肉去皮，肥瘦分开，瘦肉切成石榴粒大小，肥肉切成黄豆粒大小；\n2. 荸荠去皮切碎，挤干水分备用；\n3. 肉粒中加入葱姜末、料酒、生抽、盐、白胡椒粉、淀粉、鸡蛋，顺同一方向搅打上劲；\n4. 加入荸荠碎继续搅拌均匀，团成80g左右的大肉丸（约4个）；\n5. 砂锅内倒入高汤或清水，烧至微开，放入肉丸，小火保持微沸状态；\n6. 加盖炖2小时以上，至肉丸软糯入味，出锅撒葱花即可",
                "qingdunshizitou"
        ),
        new Recipe(
                "龙井虾仁",
                "河虾300g，龙井茶叶5g，蛋清30g，淀粉10g，料酒10ml，盐3g，白胡椒粉1g，葱段5g，姜片3g，食用油适量",
                "1. 河虾剥壳去虾线，洗净沥干，加入蛋清、淀粉、盐、白胡椒粉抓匀腌制10分钟；\n2. 龙井茶叶用80℃左右的热水冲泡，头道茶汤留用，茶叶沥干；\n3. 油温烧至四成热，下虾仁滑炒至变色刚熟，捞出沥油；\n4. 锅留底油，下葱段、姜片爆香，倒入茶汤烧开；\n5. 用水淀粉勾薄芡，下虾仁快速翻炒均匀；\n6. 关火后撒入龙井茶叶，利用余温翻炒出茶香，出锅装盘即可",
                "longjingxiaren"
        ),
        new Recipe(
                "小炒黄牛肉",
                "黄牛肉300g，蒜苗50g，泡椒15g，小米辣5g，姜末5g，蒜末8g，料酒15ml，生抽20ml，蚝油10g，淀粉10g，盐适量，食用油适量",
                "1. 黄牛肉逆纹切成薄片，加入料酒、生抽、蚝油、淀粉抓匀腌制15分钟；\n2. 蒜苗斜切成段，泡椒、小米辣切碎备用；\n3. 热锅宽油，下牛肉片大火快速滑炒至变色，立刻盛出；\n4. 锅留底油，下泡椒碎、小米辣、姜蒜末爆香出红油；\n5. 下蒜苗段翻炒至微微变软，再下牛肉片回锅；\n6. 大火快速翻炒均匀，加盐调味后立刻出锅，保持牛肉嫩滑",
                "xiaochaohuangniurou"
        )
    );

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "应用启动，开始初始化数据库");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase database = AppDatabase.getInstance(this);

            // 仅在数据库为空时插入预置数据
            if (database.recipeDao().getAllRecipes().isEmpty()) {
                Log.d(TAG, "数据库为空，开始插入预置数据");
                for (Recipe recipe : SAMPLE_RECIPES) {
                    switch (recipe.getName()) {

                        case "番茄炒蛋":
                            recipe.setCuisineType(Recipe.CuisineType.SHANDONG);
                            recipe.setCookingMethod(Recipe.CookingMethod.STIR_FRY);
                            recipe.setDifficulty(Recipe.DifficultyLevel.EASY);
                            recipe.setCookingTime(15);
                            recipe.setCalories(250);
                            break;
                        case "白切鸡":
                            recipe.setCuisineType(Recipe.CuisineType.CANTONESE);
                            recipe.setCookingMethod(Recipe.CookingMethod.BOIL);
                            recipe.setDifficulty(Recipe.DifficultyLevel.EASY);
                            recipe.setCookingTime(30);
                            recipe.setCalories(1403);
                            break;
                        case "卤牛肉":
                            recipe.setCuisineType(Recipe.CuisineType.SICHUAN);
                            recipe.setCookingMethod(Recipe.CookingMethod.STEW);
                            recipe.setDifficulty(Recipe.DifficultyLevel.MEDIUM);
                            recipe.setCookingTime(245);
                            recipe.setCalories(1163);
                            break;
                        case "松鼠鳜鱼":
                            recipe.setCuisineType(Recipe.CuisineType.JIANGSU);
                            recipe.setCookingMethod(Recipe.CookingMethod.DEEP_FRY);
                            recipe.setDifficulty(Recipe.DifficultyLevel.HARD);
                            recipe.setCookingTime(40);
                            recipe.setCalories(1800);
                            break;
                        case "西湖醋鱼":
                            recipe.setCuisineType(Recipe.CuisineType.ZHEJIANG);
                            recipe.setCookingMethod(Recipe.CookingMethod.BOIL);
                            recipe.setDifficulty(Recipe.DifficultyLevel.MEDIUM);
                            recipe.setCookingTime(30);
                            recipe.setCalories(900);
                            break;
                        case "佛跳墙":
                            recipe.setCuisineType(Recipe.CuisineType.FUJIAN);
                            recipe.setCookingMethod(Recipe.CookingMethod.STEW);
                            recipe.setDifficulty(Recipe.DifficultyLevel.HARD);
                            recipe.setCookingTime(120);
                            recipe.setCalories(6000);
                            break;
                        case "剁椒鱼头":
                            recipe.setCuisineType(Recipe.CuisineType. HUNAN);
                            recipe.setCookingMethod(Recipe.CookingMethod.STEAM);
                            recipe.setDifficulty(Recipe.DifficultyLevel.EASY);
                            recipe.setCookingTime(45);
                            recipe.setCalories(1100);
                            break;
                        case "黄山臭鳜鱼":
                            recipe.setCuisineType(Recipe.CuisineType.ANHUI);
                            recipe.setCookingMethod(Recipe.CookingMethod.STEW);
                            recipe.setDifficulty(Recipe.DifficultyLevel.MEDIUM);
                            recipe.setCookingTime(65);
                            recipe.setCalories(1100);
                            break;
                        case "炒土豆丝":
                            recipe.setCuisineType(Recipe.CuisineType.SICHUAN);
                            recipe.setCookingMethod(Recipe.CookingMethod.STIR_FRY);
                            recipe.setDifficulty(Recipe.DifficultyLevel.EASY);
                            recipe.setCookingTime(20);
                            recipe.setCalories(200);
                            break;
                        case "红烧排骨":
                            recipe.setCuisineType(Recipe.CuisineType.SHANDONG);
                            recipe.setCookingMethod(Recipe.CookingMethod.BRAISE);
                            recipe.setDifficulty(Recipe.DifficultyLevel.MEDIUM);
                            recipe.setCookingTime(45);
                            recipe.setCalories(450);
                            break;
                        case "口水鸡":
                            recipe.setCuisineType(Recipe.CuisineType.SICHUAN);
                            recipe.setCookingMethod(Recipe.CookingMethod.BOIL);
                            recipe.setDifficulty(Recipe.DifficultyLevel.MEDIUM);
                            recipe.setCookingTime(30);
                            recipe.setCalories(350);
                            break;
                        case "水煮肉片":
                            recipe.setCuisineType(Recipe.CuisineType.SICHUAN);
                            recipe.setCookingMethod(Recipe.CookingMethod.STIR_FRY);
                            recipe.setDifficulty(Recipe.DifficultyLevel.MEDIUM);
                            recipe.setCookingTime(30);
                            recipe.setCalories(400);
                            break;
                        case "蒜蓉生菜":
                            recipe.setCuisineType(Recipe.CuisineType.CANTONESE);
                            recipe.setCookingMethod(Recipe.CookingMethod.STIR_FRY);
                            recipe.setDifficulty(Recipe.DifficultyLevel.EASY);
                            recipe.setCookingTime(10);
                            recipe.setCalories(100);
                            break;
                        case "土豆烧肉":
                            recipe.setCuisineType(Recipe.CuisineType.SHANDONG);
                            recipe.setCookingMethod(Recipe.CookingMethod.BRAISE);
                            recipe.setDifficulty(Recipe.DifficultyLevel.MEDIUM);
                            recipe.setCookingTime(40);
                            recipe.setCalories(380);
                            break;
                        case "宫保鸡丁":
                            recipe.setCuisineType(Recipe.CuisineType.SICHUAN);
                            recipe.setCookingMethod(Recipe.CookingMethod.STIR_FRY);
                            recipe.setDifficulty(Recipe.DifficultyLevel.MEDIUM);
                            recipe.setCookingTime(20);
                            recipe.setCalories(480);
                            break;
                        case "鱼香肉丝":
                            recipe.setCuisineType(Recipe.CuisineType.SICHUAN);
                            recipe.setCookingMethod(Recipe.CookingMethod.STIR_FRY);
                            recipe.setDifficulty(Recipe.DifficultyLevel.MEDIUM);
                            recipe.setCookingTime(20);
                            recipe.setCalories(420);
                            break;
                        case "豉汁蒸排骨":
                            recipe.setCuisineType(Recipe.CuisineType.CANTONESE);
                            recipe.setCookingMethod(Recipe.CookingMethod.STEAM);
                            recipe.setDifficulty(Recipe.DifficultyLevel.MEDIUM);
                            recipe.setCookingTime(40);
                            recipe.setCalories(520);
                            break;
                        case "蜜汁叉烧":
                            recipe.setCuisineType(Recipe.CuisineType.CANTONESE);
                            recipe.setCookingMethod(Recipe.CookingMethod.ROAST);
                            recipe.setDifficulty(Recipe.DifficultyLevel.MEDIUM);
                            recipe.setCookingTime(90);
                            recipe.setCalories(580);
                            break;
                        case "糖醋里脊":
                            recipe.setCuisineType(Recipe.CuisineType.SHANDONG);
                            recipe.setCookingMethod(Recipe.CookingMethod.DEEP_FRY);
                            recipe.setDifficulty(Recipe.DifficultyLevel.MEDIUM);
                            recipe.setCookingTime(30);
                            recipe.setCalories(650);
                            break;
                        case "清炖狮子头":
                            recipe.setCuisineType(Recipe.CuisineType.JIANGSU);
                            recipe.setCookingMethod(Recipe.CookingMethod.STEW);
                            recipe.setDifficulty(Recipe.DifficultyLevel.HARD);
                            recipe.setCookingTime(130);
                            recipe.setCalories(700);
                            break;
                        case "龙井虾仁":
                            recipe.setCuisineType(Recipe.CuisineType.ZHEJIANG);
                            recipe.setCookingMethod(Recipe.CookingMethod.STIR_FRY);
                            recipe.setDifficulty(Recipe.DifficultyLevel.EASY);
                            recipe.setCookingTime(15);
                            recipe.setCalories(280);
                            break;
                        case "小炒黄牛肉":
                            recipe.setCuisineType(Recipe.CuisineType.HUNAN);
                            recipe.setCookingMethod(Recipe.CookingMethod.STIR_FRY);
                            recipe.setDifficulty(Recipe.DifficultyLevel.EASY);
                            recipe.setCookingTime(15);
                            recipe.setCalories(350);
                            break;
                    }
                }
                database.recipeDao().insertAll(SAMPLE_RECIPES);

                // 仅在用户表为空时添加测试用户
                if (database.userDao().getUserByUsername("test") == null) {
                    User testUser = new User("test", "123456");
                    database.userDao().insert(testUser);
                    Log.d(TAG, "测试用户添加成功");

                    long userId = 1L; // 首个用户 ID

                    // ─────────────────────────────────────────────
                    // CookingHistory：高频 + 中频 + 很久没做
                    // 菜系偏好：川菜 + 粤菜
                    // ─────────────────────────────────────────────

                    // 高频菜（cookCount 较高）
                    addHistory(database, userId, 15, "宫保鸡丁",   6,  daysAgo(3));    // 宫保鸡丁 id=15
                    addHistory(database, userId, 16, "鱼香肉丝",   5,  daysAgo(4));    // 鱼香肉丝 id=16
                    addHistory(database, userId, 11, "水煮肉片",   4,  daysAgo(5));    // 水煮肉片 id=11
                    addHistory(database, userId, 10, "口水鸡",     3,  daysAgo(7));    // 口水鸡 id=10

                    // 中频菜（久未尝试的川菜）
                    addHistory(database, userId, 3, "松鼠鳜鱼",   2,  daysAgo(20));  // 松鼠鳜鱼 id=3（苏菜，偶尔换口味）

                    // 很久没做（>30 天）
                    addHistory(database, userId, 2,  "卤牛肉",     2,  daysAgo(45));   // 卤牛肉 id=2
                    addHistory(database, userId, 1,  "白切鸡",     1,  daysAgo(60));   // 白切鸡 id=1

                    // ─────────────────────────────────────────────
                    // Favorite：6 条收藏，覆盖川菜 + 粤菜
                    // ─────────────────────────────────────────────
                    database.favoriteDao().insert(new Favorite(userId, 15));  // 宫保鸡丁（川菜）
                    database.favoriteDao().insert(new Favorite(userId, 16));  // 鱼香肉丝（川菜）
                    database.favoriteDao().insert(new Favorite(userId, 10));  // 口水鸡（川菜）
                    database.favoriteDao().insert(new Favorite(userId, 11));  // 水煮肉片（川菜）
                    database.favoriteDao().insert(new Favorite(userId, 17));  // 豉汁蒸排骨（粤菜）
                    database.favoriteDao().insert(new Favorite(userId, 2));   // 卤牛肉（川菜）

                    Log.d(TAG, "个性化测试数据（做饭历史 + 收藏）插入完成");

                    // ─────────────────────────────────────────────
                    // MealPlan：未来 3 天的部分菜单
                    // 今天是 2026-06-26（周五）
                    // 明天 2026-06-27（周六），后天 2026-06-28（周日）
                    // ─────────────────────────────────────────────
                    Date tomorrow     = dateAt(2026, Calendar.JUNE, 27, 12, 0);
                    Date dayAfterTmr  = dateAt(2026, Calendar.JUNE, 28, 18, 30);
                    Date day3         = dateAt(2026, Calendar.JUNE, 29, 19, 0);

                    MealPlan plan1 = new MealPlan(userId, 15, tomorrow,    MealPlan.MealType.LUNCH);   // 宫保鸡丁（川菜）
                    MealPlan plan2 = new MealPlan(userId, 1,  dayAfterTmr, MealPlan.MealType.DINNER); // 白切鸡（粤菜）
                    MealPlan plan3 = new MealPlan(userId, 10, day3,         MealPlan.MealType.DINNER); // 口水鸡（川菜）

                    database.mealPlanDao().insert(plan1);
                    database.mealPlanDao().insert(plan2);
                    database.mealPlanDao().insert(plan3);

                    Log.d(TAG, "个性化测试数据（菜单计划）插入完成");
                }

                List<Recipe> inserted = database.recipeDao().getAllRecipes();
                Log.d(TAG, "预置数据插入完成，共 " + inserted.size() + " 条食谱");
            } else {
                Log.d(TAG, "数据库已有数据，跳过预置数据插入");
            }
        });
        executor.shutdown();
    }

    /**
     * 辅助方法：在指定天数前创建一条 CookingHistory。
     */
    private static void addHistory(AppDatabase db, long userId, long recipeId,
                                   String name, int cookCount, Date lastCookedAt) {
        CookingHistory existing = db.cookingHistoryDao().getByUserAndRecipe(userId, recipeId);
        if (existing != null) {
            // 已存在则更新计数和时间
            existing.setCookCount(cookCount);
            existing.setLastCookedAt(lastCookedAt);
            db.cookingHistoryDao().update(existing);
        } else {
            CookingHistory h = new CookingHistory(userId, recipeId);
            h.setCookCount(cookCount);
            h.setLastCookedAt(lastCookedAt);
            db.cookingHistoryDao().insert(h);
        }
    }

    /** 今天减 N 天，返回当天中午 12:00 的 Date。 */
    private static Date daysAgo(int days) {
        Calendar c = new GregorianCalendar();
        c.add(Calendar.DAY_OF_YEAR, -days);
        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /** 构造指定年月日时的 Date。 */
    private static Date dateAt(int year, int month, int day, int hour, int minute) {
        Calendar c = new GregorianCalendar(year, month, day, hour, minute, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
}
