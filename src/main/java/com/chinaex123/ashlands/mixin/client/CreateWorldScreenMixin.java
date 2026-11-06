package com.chinaex123.ashlands.mixin.client;

import com.chinaex123.ashlands.config.AshlandsConfig;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 创建世界界面的 Mixin。
 * <p>
 * 在预设列表刷新完成后，若配置启用了默认世界类型，
 * 则将世界类型自动设置为本模组的预设。
 */
@Mixin(WorldCreationUiState.class)
public class CreateWorldScreenMixin {

    /**
     * 在刷新预设列表后设置默认世界类型。
     * <p>
     * 仅当配置项启用时生效：从注册表查找空岛世界预设，
     * 找到后将其设为当前选中的世界类型。
     *
     * @param ci 回调信息
     */
    @Inject(method = "updatePresetLists", at = @At("TAIL"))
    private void ashlands$setDefaultWorldType(CallbackInfo ci) {
        if (AshlandsConfig.DEFAULT_WORLD_TYPE.get()) {
            WorldCreationUiState state = (WorldCreationUiState) (Object) this;
            var lookup = state.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET);
            ResourceKey<WorldPreset> key = ResourceKey.create(Registries.WORLD_PRESET, ResourceLocation.fromNamespaceAndPath("ashlands", "ashlands"));
            lookup.get(key).ifPresent(holder -> {
                WorldCreationUiState.WorldTypeEntry entry = new WorldCreationUiState.WorldTypeEntry(holder);
                state.setWorldType(entry);
            });
        }
    }
}
