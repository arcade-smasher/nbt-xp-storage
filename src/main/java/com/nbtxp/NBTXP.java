package com.nbtxp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NBTXP implements ModInitializer {
	public static final String MOD_ID = "nbtxp";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("nbtxp")
				.then(literal("amount")
					.then(literal("set")
						.then(argument("integer", IntegerArgumentType.integer(0))
							.executes(context -> {
								ServerCommandSource source = context.getSource();
								int XP_CONSUMED = IntegerArgumentType.getInteger(context, "integer");
								XPWorldState.get(source.getServer()).XP_CONSUMED(XP_CONSUMED);
								source.sendFeedback(() -> Text.translatable("command.nbtxp.amount.set", XP_CONSUMED), false);
								return 1;
							})
						)
					)
					.executes(context -> {
						ServerCommandSource source = context.getSource();
						int xp = XPWorldState.get(source.getServer()).XP_CONSUMED();
						source.sendFeedback(() -> Text.translatable("command.nbtxp.amount.current", xp), false);
						return 1;
					})
				)
				.then(literal("consumption")
					.then(literal("enable")
						.executes(context -> {
							ServerCommandSource source = context.getSource();
							XPWorldState.get(source.getServer()).ENABLE_CONSUMPTION(true);
							source.sendFeedback(() -> Text.translatable("command.nbtxp.consumption.enable"), true);
							return 1;
						})
					)
					.then(literal("disable")
						.executes(context -> {
							ServerCommandSource source = context.getSource();
							XPWorldState.get(source.getServer()).ENABLE_CONSUMPTION(false);
							source.sendFeedback(() -> Text.translatable("command.nbtxp.consumption.disable"), true);
							return 1;
						})
					)
					.executes(context -> {
						ServerCommandSource source = context.getSource();
						MutableText enabled = Text.translatable("command.nbtxp.consumption_enabled." + XPWorldState.get(source.getServer()).ENABLE_CONSUMPTION());
						source.sendFeedback(() -> Text.translatable("command.nbtxp.consumption", enabled), false);
						return 1;
					})
				)
			);
		});

		UseItemCallback.EVENT.register(this::onRightClick);
	}

	public TypedActionResult<ItemStack> onRightClick(PlayerEntity player, World world, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);

		if (!(player instanceof ServerPlayerEntity serverPlayer)) {
			return TypedActionResult.pass(stack);
		}

		if (!player.isSneaking() || !stack.isOf(Items.GLASS_BOTTLE) || !(world instanceof ServerWorld serverWorld)) {
			return TypedActionResult.pass(stack);
		}

		int xpConsumed = XPWorldState.get(serverWorld.getServer()).XP_CONSUMED();

		float playerXP = xpLevelsToPoints(serverPlayer.experienceLevel) + serverPlayer.experienceProgress * serverPlayer.getNextLevelExperience();

		if (playerXP < xpConsumed && playerXP > 0) {
			xpConsumed = MathHelper.ceil(playerXP);
		}

		boolean creativeOrSpectator = serverPlayer.isCreative() || serverPlayer.isSpectator();

		if (xpConsumed <= 0 || playerXP >= xpConsumed || creativeOrSpectator) {
			ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
			NbtCompound nbt = new NbtCompound();
			nbt.putInt("xp", xpConsumed);
			bottle.setNbt(nbt);

			serverPlayer.getInventory().offerOrDrop(bottle);

			if (!creativeOrSpectator) {
				serverPlayer.addExperience(-xpConsumed);
				if (!stack.isEmpty()) {
					stack.decrement(1);
					if (stack.isEmpty()) {
						player.setStackInHand(hand, ItemStack.EMPTY);
					}
				}
			}

			return TypedActionResult.success(stack);
		}

		return TypedActionResult.fail(stack);
	}

	private static int xpLevelsToPoints(final int level) {
		if (level == 0) {
			return 0;
		}
		if (level <= 15) {
			return sum(level, 7, 2);
		}
		if (level <= 30) {
			return 315 + sum(level - 15, 37, 5);
		}
		return 1395 + sum(level - 30, 112, 9);
	}

	private static int sum(final int n, final int a, final int d) {
		return n * (2 * a + (n - 1) * d) / 2;
	}
}
