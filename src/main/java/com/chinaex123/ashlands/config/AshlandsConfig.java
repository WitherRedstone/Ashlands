package com.chinaex123.ashlands.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AshlandsConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue SAFE_RADIUS_CHUNKS;
    public static final ModConfigSpec.IntValue TICKS_PER_RADIATION_LEVEL;
    public static final ModConfigSpec.BooleanValue STRENGTHEN_MONSTERS;
    public static final ModConfigSpec.BooleanValue STRIP_TREES_OUTSIDE_SAFE_ZONE;
    public static final ModConfigSpec.BooleanValue RADIATION_DAMAGE;
    public static final ModConfigSpec.BooleanValue NO_HOSTILE_SPAWNS_IN_SAFE_ZONE;
    public static final ModConfigSpec.BooleanValue DEFAULT_WORLD_TYPE;

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

        builder.comment("通用设置").push("General Config");
        SAFE_RADIUS_CHUNKS = builder
                .comment("初始安全区的区块半径。")
                .comment("Safe zone radius in chunks.")
                .defineInRange("safeRadiusChunks", 1, 0, 16);
        STRENGTHEN_MONSTERS = builder
                .comment("是否加强灰域世界中生成的怪物（生命、攻击、速度、护甲）。")
                .comment("Whether monsters spawned in the Ashlands are strengthened (health, attack, speed, armor).")
                .define("strengthenMonsters", true);
        STRIP_TREES_OUTSIDE_SAFE_ZONE = builder
                .comment("是否把安全区之外所有区块的树叶全部移除，只留下没有树叶的树干（整个世界都会变成枯死的森林）。")
                .comment("Whether leaves in every chunk outside the safe zone are removed, leaving only bare trunks (the whole world becomes a dead forest).")
                .define("stripTreesOutsideSafeZone", true);
        NO_HOSTILE_SPAWNS_IN_SAFE_ZONE = builder
                .comment("是否禁止敌对生物在安全区内自然生成。")
                .comment("Whether hostile mobs are prevented from spawning inside the safe zone.")
                .define("noHostileSpawnsInSafeZone", true);
        DEFAULT_WORLD_TYPE = builder
                .comment("是否将 Ashlands 设为默认世界类型")
                .comment("Set Ashlands as the default world type")
                .define("defaultWorldType", true);
        builder.pop();


        builder.comment("辐射效果").push("Radiation Effect");
        TICKS_PER_RADIATION_LEVEL = builder
                .comment("辐射每提升一级所需的游戏刻（tick）。")
                .comment("Game ticks required for radiation to gain one level.")
                .defineInRange("ticksPerRadiationLevel", 12000, 20, 864000);
        RADIATION_DAMAGE = builder
                .comment("辐射是否会对玩家造成伤害。关闭后辐射只影响饥饿值与饱和度。")
                .comment("Whether radiation deals damage to players. When false, radiation only drains hunger and saturation.")
                .define("radiationDamage", true);

        builder.comment("1 级辐射").push("Radiation level 1");
        LEVEL1_DAMAGE = builder
                .comment("每次扣血量。")
                .comment("Damage per hit.")
                .defineInRange("damage", 1.0D, 0.0D, 100.0D);
        LEVEL1_DAMAGE_INTERVAL = builder
                .comment("两次扣血之间的间隔（tick）。")
                .comment("Ticks between two damage hits.")
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
                .comment("两次扣除饥饿值/饱和度之间的间隔（tick）。")
                .comment("Ticks between two hunger/saturation drains.")
                .defineInRange("foodIntervalTicks", 200, 1, 72000);
        builder.pop();

        builder.comment("2 级辐射").push("Radiation level 2");
        LEVEL2_DAMAGE = builder
                .comment("每次扣血量。")
                .comment("Damage per hit.")
                .defineInRange("damage", 2.0D, 0.0D, 100.0D);
        LEVEL2_DAMAGE_INTERVAL = builder
                .comment("两次扣血之间的间隔（tick）。")
                .comment("Ticks between two damage hits.")
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
                .comment("两次扣除饥饿值/饱和度之间的间隔（tick）。")
                .comment("Ticks between two hunger/saturation drains.")
                .defineInRange("foodIntervalTicks", 200, 1, 72000);
        builder.pop();

        builder.comment("3 级辐射").push("Radiation level 3");
        LEVEL3_DAMAGE = builder
                .comment("每次扣血量。")
                .comment("Damage per hit.")
                .defineInRange("damage", 4.0D, 0.0D, 100.0D);
        LEVEL3_DAMAGE_INTERVAL = builder
                .comment("两次扣血之间的间隔（tick）。")
                .comment("Ticks between two damage hits.")
                .defineInRange("damageIntervalTicks", 200, 1, 72000);
        LEVEL3_LOCKS_FOOD = builder
                .comment("是否每刻强制清空饥饿值与饱和度，使任何食物都无法恢复。")
                .comment("Whether hunger and saturation are forced to zero every tick, so food can no longer restore them.")
                .define("locksFood", true);
        builder.pop();

        builder.pop();

        SPEC = builder.build();
    }
}
