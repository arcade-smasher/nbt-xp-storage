package com.nbtxp.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

@Mixin(ExperienceBottleEntity.class)
public class ExperienceBottleEntityMixin {

    @Unique
    private NbtCompound originalItemNbt;

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)V", at = @At("TAIL"))
    private void captureOriginalItem(World world, LivingEntity owner, CallbackInfo ci) {
        if (owner == null) return;

        ItemStack stack = owner.getMainHandStack();
        if (!stack.isOf(Items.EXPERIENCE_BOTTLE) || !stack.hasNbt()) return;

        this.originalItemNbt = stack.getNbt();
    }

    @ModifyVariable(method = "onCollision", at = @At("STORE"), ordinal = 0)
    private int modifyXpAmount(int randomXPAmount) {
        if (this.originalItemNbt != null && this.originalItemNbt.contains("xp")) {
            return this.originalItemNbt.getInt("xp");
        }
        return randomXPAmount;
    }
}