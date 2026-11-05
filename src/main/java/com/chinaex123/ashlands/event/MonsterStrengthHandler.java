package com.chinaex123.ashlands.event;

import com.chinaex123.ashlands.Ashlands;
import com.chinaex123.ashlands.config.AshlandsConfig;
import com.chinaex123.ashlands.util.AshlandsWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = Ashlands.MODID)
public final class MonsterStrengthHandler {

    private static final ResourceLocation HEALTH_ID =
            ResourceLocation.fromNamespaceAndPath(Ashlands.MODID, "monster_health");
    private static final ResourceLocation ATTACK_ID =
            ResourceLocation.fromNamespaceAndPath(Ashlands.MODID, "monster_attack");
    private static final ResourceLocation SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(Ashlands.MODID, "monster_speed");
    private static final ResourceLocation ARMOR_ID =
            ResourceLocation.fromNamespaceAndPath(Ashlands.MODID, "monster_armor");

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!AshlandsConfig.STRENGTHEN_MONSTERS.get()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level) || !AshlandsWorld.isAshlandsWorld(level)) {
            return;
        }
        if (!(event.getEntity() instanceof Monster monster)) {
            return;
        }

        addModifier(monster, Attributes.MAX_HEALTH, HEALTH_ID, 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addModifier(monster, Attributes.ATTACK_DAMAGE, ATTACK_ID, 0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addModifier(monster, Attributes.MOVEMENT_SPEED, SPEED_ID, 0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addModifier(monster, Attributes.ARMOR, ARMOR_ID, 4.0D, AttributeModifier.Operation.ADD_VALUE);

        // 生命上限提高后补满血量，避免生成时只有一半血。
        monster.setHealth(monster.getMaxHealth());
    }

    private static void addModifier(Mob mob, net.minecraft.core.Holder<Attribute> attribute,
                                    ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance == null || instance.getModifier(id) != null) {
            return;
        }
        instance.addPermanentModifier(new AttributeModifier(id, amount, operation));
    }
}
