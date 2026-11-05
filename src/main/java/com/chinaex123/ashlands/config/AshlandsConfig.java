package com.chinaex123.ashlands.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AshlandsConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue SAFE_RADIUS_CHUNKS;
    public static final ModConfigSpec.IntValue TICKS_PER_RADIATION_LEVEL;
    public static final ModConfigSpec.BooleanValue STRENGTHEN_MONSTERS;
    public static final ModConfigSpec.BooleanValue STRIP_TREES_OUTSIDE_SAFE_ZONE;
    public static final ModConfigSpec.BooleanValue RADIATION_DAMAGE;

    public static final ModConfigSpec.DoubleValue LEVEL1_DAMAGE;
    public static final ModConfigSpec.IntValue LEVEL1_DAMAGE_INTERVAL;
    public static final ModConfigSpec.IntValue LEVEL1_FOOD_DRAIN;
    public static final ModConfigSpec.DoubleValue LEVEL1_SATURATION_DRAIN;
    public static final ModConfigSpec.IntValue LEVEL1_FOOD_INTERVAL;

    public static final ModConfigSpec.DoubleValue LEVEL2_DAMAGE;
    public static final ModConfigSpec.IntValue LEVEL2_DAMAGE_INTERVAL;
    public static final ModConfigSpec.IntValue LEVEL2_FOOD_DRAIN;
    public static final ModConfigSpec.DoubleValue LEVEL2_SATURATION_DRAIN;
    public static final ModConfigSpec.IntValue LEVEL2_FOOD_INTERVAL;

    public static final ModConfigSpec.DoubleValue LEVEL3_DAMAGE;
    public static final ModConfigSpec.IntValue LEVEL3_DAMAGE_INTERVAL;
    public static final ModConfigSpec.BooleanValue LEVEL3_LOCKS_FOOD;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        SAFE_RADIUS_CHUNKS = builder
                .comment("安全区半径（区块）。1 表示以出生点所在区块为中心的 3x3 共 9 个区块；0 只有出生点区块；2 为 5x5，以此类推（最大 16，即 33x33）。")
                .comment("Safe zone radius in chunks. 1 means a 3x3 area (9 chunks) centered on the spawn chunk; 0 means only the spawn chunk; 2 means 5x5, and so on (max 16 = 33x33).")
                .comment("光幕位置、辐射判定边界、树叶保留范围都以此为准, 修改后需要重启游戏/服务器。")
                .comment("The curtain position, the radiation boundary and the leaf-preserved area all follow this value. Changing it requires a game/server restart.")
                .defineInRange("safeRadiusChunks", 1, 0, 16);
        TICKS_PER_RADIATION_LEVEL = builder
                .comment("辐射每提升一级所需的游戏刻。20 刻 = 1 秒，12000 刻 = 10 分钟。辐射最高 3 级。")
                .comment("Game ticks required for radiation to gain one level. 20 ticks = 1 second, 12000 ticks = 10 minutes. Radiation caps at level 3.")
                .defineInRange("ticksPerRadiationLevel", 12000, 20, 864000);
        STRENGTHEN_MONSTERS = builder
                .comment("是否加强灰域世界中生成的怪物（生命、攻击、速度、护甲）。")
                .comment("Whether monsters spawned in the Ashlands are strengthened (health, attack, speed, armor).")
                .define("strengthenMonsters", true);
        STRIP_TREES_OUTSIDE_SAFE_ZONE = builder
                .comment("是否把安全区之外所有区块的树叶全部移除，只留下没有树叶的树干（整个世界都会变成枯死的森林）。")
                .comment("Whether leaves in every chunk outside the safe zone are removed, leaving only bare trunks (the whole world becomes a dead forest).")
                .define("stripTreesOutsideSafeZone", true);
        RADIATION_DAMAGE = builder
                .comment("辐射是否会对玩家造成伤害。关闭后辐射只影响饥饿值与饱和度。")
                .comment("Whether radiation deals damage to players. When false, radiation only drains hunger and saturation.")
                .define("radiationDamage", true);

        builder.comment("1 级辐射（刚越过光幕时）").comment("Radiation level 1 (right after crossing the curtain)").push("radiationLevel1");
        LEVEL1_DAMAGE = builder
                .comment("每次扣血量。2 点 = 1 颗心，默认 1.0 即半颗心。")
                .comment("Damage per hit. 2 points = 1 heart; the default 1.0 is half a heart.")
                .defineInRange("damage", 1.0D, 0.0D, 100.0D);
        LEVEL1_DAMAGE_INTERVAL = builder
                .comment("两次扣血之间的间隔（游戏刻）。20 刻 = 1 秒，默认 1200 刻 = 60 秒。")
                .comment("Ticks between two damage hits. 20 ticks = 1 second; the default 1200 ticks = 60 seconds.")
                .defineInRange("damageIntervalTicks", 1200, 1, 72000);
        LEVEL1_FOOD_DRAIN = builder
                .comment("每次扣除的饥饿值点数（鸡腿）。")
                .comment("Hunger points drained each time.")
                .defineInRange("foodDrain", 1, 0, 20);
        LEVEL1_SATURATION_DRAIN = builder
                .comment("每次扣除的饱和度。")
                .comment("Saturation drained each time.")
                .defineInRange("saturationDrain", 1.0D, 0.0D, 20.0D);
        LEVEL1_FOOD_INTERVAL = builder
                .comment("两次扣除饥饿值/饱和度之间的间隔（游戏刻）。20 刻 = 1 秒，默认 200 刻 = 10 秒。")
                .comment("Ticks between two hunger/saturation drains. 20 ticks = 1 second; the default 200 ticks = 10 seconds.")
                .defineInRange("foodIntervalTicks", 200, 1, 72000);
        builder.pop();

        builder.comment("2 级辐射").comment("Radiation level 2").push("radiationLevel2");
        LEVEL2_DAMAGE = builder
                .comment("每次扣血量。2 点 = 1 颗心，默认 2.0 即 1 颗心。")
                .comment("Damage per hit. 2 points = 1 heart; the default 2.0 is 1 heart.")
                .defineInRange("damage", 2.0D, 0.0D, 100.0D);
        LEVEL2_DAMAGE_INTERVAL = builder
                .comment("两次扣血之间的间隔（游戏刻）。20 刻 = 1 秒，默认 600 刻 = 30 秒。")
                .comment("Ticks between two damage hits. 20 ticks = 1 second; the default 600 ticks = 30 seconds.")
                .defineInRange("damageIntervalTicks", 600, 1, 72000);
        LEVEL2_FOOD_DRAIN = builder
                .comment("每次扣除的饥饿值点数（鸡腿）。")
                .comment("Hunger points drained each time.")
                .defineInRange("foodDrain", 3, 0, 20);
        LEVEL2_SATURATION_DRAIN = builder
                .comment("每次扣除的饱和度。")
                .comment("Saturation drained each time.")
                .defineInRange("saturationDrain", 6.0D, 0.0D, 20.0D);
        LEVEL2_FOOD_INTERVAL = builder
                .comment("两次扣除饥饿值/饱和度之间的间隔（游戏刻）。20 刻 = 1 秒，默认 200 刻 = 10 秒。")
                .comment("Ticks between two hunger/saturation drains. 20 ticks = 1 second; the default 200 ticks = 10 seconds.")
                .defineInRange("foodIntervalTicks", 200, 1, 72000);
        builder.pop();

        builder.comment("3 级辐射").comment("Radiation level 3").push("radiationLevel3");
        LEVEL3_DAMAGE = builder
                .comment("每次扣血量。2 点 = 1 颗心，默认 4.0 即 2 颗心。")
                .comment("Damage per hit. 2 points = 1 heart; the default 4.0 is 2 hearts.")
                .defineInRange("damage", 4.0D, 0.0D, 100.0D);
        LEVEL3_DAMAGE_INTERVAL = builder
                .comment("两次扣血之间的间隔（游戏刻）。20 刻 = 1 秒，默认 200 刻 = 10 秒。")
                .comment("Ticks between two damage hits. 20 ticks = 1 second; the default 200 ticks = 10 seconds.")
                .defineInRange("damageIntervalTicks", 200, 1, 72000);
        LEVEL3_LOCKS_FOOD = builder
                .comment("是否每刻强制清空饥饿值与饱和度，使任何食物都无法恢复。")
                .comment("Whether hunger and saturation are forced to zero every tick, so food can no longer restore them.")
                .define("locksFood", true);
        builder.pop();

        SPEC = builder.build();
    }
}
