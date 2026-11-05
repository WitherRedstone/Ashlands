package com.chinaex123.ashlands.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AshlandsConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue SAFE_RADIUS_CHUNKS;
    public static final ModConfigSpec.DoubleValue MINUTES_PER_RADIATION_LEVEL;
    public static final ModConfigSpec.BooleanValue STRENGTHEN_MONSTERS;
    public static final ModConfigSpec.BooleanValue STRIP_TREES_OUTSIDE_SAFE_ZONE;
    public static final ModConfigSpec.BooleanValue RADIATION_DAMAGE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        SAFE_RADIUS_CHUNKS = builder
                .comment("安全区半径（区块）。1 表示以出生点所在区块为中心的 3x3 共 9 个区块；0 只有出生点区块；2 为 5x5，以此类推（最大 16，即 33x33）。")
                .comment("Safe zone radius in chunks. 1 means a 3x3 area (9 chunks) centered on the spawn chunk; 0 means only the spawn chunk; 2 means 5x5, and so on (max 16 = 33x33).")
                .comment("光幕位置、辐射判定边界、树叶保留范围都以此为准, 修改后需要重启游戏/服务器。")
                .comment("The curtain position, the radiation boundary and the leaf-preserved area all follow this value. Changing it requires a game/server restart.")
                .defineInRange("safeRadiusChunks", 1, 0, 16);
        MINUTES_PER_RADIATION_LEVEL = builder
                .comment("辐射每提升一级所需的时间（分钟）。辐射最高 3 级。")
                .comment("Minutes required for radiation to gain one level. Radiation caps at level 3.")
                .defineInRange("minutesPerRadiationLevel", 10.0D, 0.1D, 720.0D);
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

        SPEC = builder.build();
    }
}
