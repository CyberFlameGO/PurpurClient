package org.purpurmc.purpur.client.mixin;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlDebugInfo;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.purpurmc.purpur.client.PurpurClient;
import org.purpurmc.purpur.client.network.BeehivePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(DebugHud.class)
public class MixinDebugHud {
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private HitResult blockHit;

    @Unique
    private long lastTime = 0;
    @Unique
    private BlockPos lastPos = null;

    @Redirect(method = "drawRightText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;getRightText()Ljava/util/List;"))
    protected List<String> drawRightText(DebugHud instance) {
        List<String> list = this.getRightText();

        if (this.client.hasReducedDebugInfo() || this.client.world == null || !PurpurClient.instance().getConfig().beeCountInDebug) {
            return list;
        }

        BlockPos pos = ((BlockHitResult) this.blockHit).getBlockPos();
        BlockState state = this.client.world.getBlockState(pos);

        if (state.getBlock() instanceof BeehiveBlock) {
            long now = System.currentTimeMillis();
            if (now > this.lastTime + 500 || !pos.equals(this.lastPos)) {
                this.lastPos = pos;
                this.lastTime = now;
                BeehivePacket.requestBeehiveData(pos);
            }
            if (BeehivePacket.numOfBees != null) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).contains("honey_level")) {
                        list.add(i + 1, "num_of_bees: " + BeehivePacket.numOfBees);
                        break;
                    }
                }
            }
        }

        return list;
    }

    @Shadow
    @SuppressWarnings("SameReturnValue")
    protected List<String> getRightText() {
        return null;
    }
}
