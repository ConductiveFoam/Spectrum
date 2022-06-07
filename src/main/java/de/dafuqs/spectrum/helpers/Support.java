package de.dafuqs.spectrum.helpers;

import de.dafuqs.spectrum.SpectrumCommon;
import de.dafuqs.spectrum.progression.ClientAdvancements;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.TagKey;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Support {
	
	public static final List<Vec3d> VECTORS_4 = new ArrayList<>() {{
		add(new Vec3d(1.0D, 0, 0.0D));
		add(new Vec3d(0.0D, 0, 1.0D));
		add(new Vec3d(-1.0, 0, 0.0D));
		add(new Vec3d(0.0D, 0, -1.0D));
	}};
	public static final List<Vec3d> VECTORS_8 = new ArrayList<>() {{
		add(new Vec3d(1.0D, 0, 0.0D));
		add(new Vec3d(0.7D, 0, 0.7D));
		add(new Vec3d(0.0D, 0, 1.0D));
		add(new Vec3d(-0.7D, 0, 0.7D));
		add(new Vec3d(-1.0D, 0, 0.0D));
		add(new Vec3d(-0.7D, 0, -0.7D));
		add(new Vec3d(0.0D, 0, -1.0D));
		add(new Vec3d(0.7D, 0, -0.7D));
	}};
	// Like eight, just turned clockwise
	public static final List<Vec3d> VECTORS_8_OFFSET = new ArrayList<>() {{
		add(new Vec3d(0.75D, 0, 0.5D));
		add(new Vec3d(0.5D, 0, 0.75D));
		add(new Vec3d(-0.5D, 0, 0.75D));
		add(new Vec3d(-0.75D, 0, 0.5D));
		add(new Vec3d(-0.75D, 0, 0.5D));
		add(new Vec3d(-0.5D, 0, -0.75D));
		add(new Vec3d(0.5D, 0, -0.75D));
		add(new Vec3d(0.75D, 0, -0.5D));
	}};
	public static final List<Vec3d> VECTORS_16 = new ArrayList<>() {{
		add(new Vec3d(1.0D, 0, 0.0D));
		add(new Vec3d(0.75D, 0, 0.5D));
		add(new Vec3d(0.7D, 0, 0.7D));
		add(new Vec3d(0.5D, 0, 0.75D));
		add(new Vec3d(0.0D, 0, 1.0D));
		add(new Vec3d(-0.5D, 0, 0.75D));
		add(new Vec3d(-0.7D, 0, 0.7D));
		add(new Vec3d(-0.75D, 0, 0.5D));
		add(new Vec3d(-1.0D, 0, 0.0D));
		add(new Vec3d(-0.75D, 0, 0.5D));
		add(new Vec3d(-0.7D, 0, -0.7D));
		add(new Vec3d(-0.5D, 0, -0.75D));
		add(new Vec3d(0.0D, 0, -1.0D));
		add(new Vec3d(0.5D, 0, -0.75D));
		add(new Vec3d(0.7D, 0, -0.7D));
		add(new Vec3d(0.75D, 0, -0.5D));
	}};
	private static final Identifier PROGRESSION_FINISHED_ADVANCEMENT_IDENTIFIER = new Identifier(SpectrumCommon.MOD_ID, "lategame/finish_progression");
	private static final DecimalFormat df = new DecimalFormat("0.00");
	
	public static @NotNull Optional<TagKey<Block>> getFirstMatchingBlockTag(@NotNull BlockState blockState, @NotNull List<TagKey<Block>> tags) {
		return blockState.streamTags().filter(tags::contains).findFirst();
	}
	
	public static String getShortenedNumberString(double number) {
		if (number > 1000000000D) {
			return df.format(number / 1000000000D) + "G";
		} else if (number > 1000000D) {
			return df.format(number / 1000000D) + "M";
		} else if (number > 1000D) {
			return df.format(number / 1000D) + "K";
		} else {
			return df.format(number);
		}
	}
	
	public static String getShortenedNumberString(long number) {
		if (number > 1000000000L) {
			return df.format(number / 1000000000D) + "G";
		} else if (number > 1000000L) {
			return df.format(number / 1000000D) + "M";
		} else if (number > 1000L) {
			return df.format(number / 1000D) + "K";
		} else {
			return df.format(number);
		}
	}
	
	/**
	 * Adds a stack to the players inventory.
	 * If there is not enough room drop it on the ground instead
	 *
	 * @param playerEntity The player to give the stack to
	 * @param itemStack    The item stack
	 */
	public static void givePlayer(PlayerEntity playerEntity, ItemStack itemStack) {
		boolean insertInventorySuccess = playerEntity.getInventory().insertStack(itemStack);
		ItemEntity itemEntity;
		if (insertInventorySuccess && itemStack.isEmpty()) {
			itemStack.setCount(1);
			itemEntity = playerEntity.dropItem(itemStack, false);
			if (itemEntity != null) {
				itemEntity.setDespawnImmediately();
			}
			
			playerEntity.world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((playerEntity.getRandom().nextFloat() - playerEntity.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
			playerEntity.currentScreenHandler.sendContentUpdates();
		} else {
			itemEntity = playerEntity.dropItem(itemStack, false);
			if (itemEntity != null) {
				itemEntity.resetPickupDelay();
				itemEntity.setOwner(playerEntity.getUuid());
			}
		}
	}
	
	public static int getIntFromDecimalWithChance(double d, @NotNull Random random) {
		boolean roundUp = (random.nextFloat() < d % 1);
		if (roundUp) {
			return ((int) d) + 1;
		} else {
			return (int) d;
		}
	}
	
	public static BlockPos directionalOffset(BlockPos origin, Vec3i offset, @NotNull Direction horizontalFacing) {
		switch (horizontalFacing) {
			case NORTH -> {
				return origin.add(offset.getZ(), offset.getY(), -offset.getX());
			}
			case EAST -> {
				return origin.add(offset.getX(), offset.getY(), offset.getZ());
			}
			case SOUTH -> {
				return origin.add(offset.getZ(), offset.getY(), offset.getX());
			}
			case WEST -> {
				return origin.add(-offset.getX(), offset.getY(), offset.getZ());
			}
			default -> {
				SpectrumCommon.logWarning("Called directionalOffset with facing" + horizontalFacing + " this is not supported.");
				return origin;
			}
		}
	}
	
	public static void grantAdvancementCriterion(@NotNull ServerPlayerEntity serverPlayerEntity, Identifier advancementIdentifier, String criterion) {
		ServerAdvancementLoader sal = SpectrumCommon.minecraftServer.getAdvancementLoader();
		PlayerAdvancementTracker tracker = serverPlayerEntity.getAdvancementTracker();
		
		Advancement advancement = sal.get(advancementIdentifier);
		if (advancement == null) {
			SpectrumCommon.logError("Trying to grant a criterion \"" + criterion + "\" for an advancement that does not exist: " + advancementIdentifier);
		} else {
			if (!tracker.getProgress(advancement).isDone()) {
				tracker.grantCriterion(advancement, criterion);
			}
		}
	}
	
	public static void grantAdvancementCriterion(@NotNull ServerPlayerEntity serverPlayerEntity, String advancementString, String criterion) {
		grantAdvancementCriterion(serverPlayerEntity, new Identifier(SpectrumCommon.MOD_ID, advancementString), criterion);
	}
	
	public static boolean hasAdvancement(PlayerEntity playerEntity, Identifier advancementIdentifier) {
		if (playerEntity == null) {
			return false;
		} else if (advancementIdentifier == null) {
			return true;
		}
		
		if (playerEntity instanceof ServerPlayerEntity) {
			Advancement advancement = SpectrumCommon.minecraftServer.getAdvancementLoader().get(advancementIdentifier);
			if (advancement == null) {
				SpectrumCommon.logError("Player " + playerEntity.getName() + " was getting an advancement check for an advancement that does not exist: " + advancementIdentifier);
				return false;
			} else {
				return ((ServerPlayerEntity) playerEntity).getAdvancementTracker().getProgress(advancement).isDone();
			}
			// we cannot test for "net.minecraft.client.network.ClientPlayerEntity" there because that will get obfuscated
			// to "net.minecraft.class_xxxxx" in compiled versions => works in dev env, breaks in prod
		} else if (playerEntity.getClass().getCanonicalName().startsWith("net.minecraft")) {
			return hasAdvancementClient(advancementIdentifier);
		} else {
			// thank you, Kibe FakePlayerEntity
			// it neither is a ServerPlayerEntity, nor a ClientPlayerEntity
			return false;
		}
	}
	
	@Environment(EnvType.CLIENT)
	public static boolean hasAdvancementClient(Identifier advancementIdentifier) {
		return ClientAdvancements.hasDone(advancementIdentifier);
	}
	
	public static @NotNull String getReadableDimensionString(@NotNull String dimensionKeyString) {
		switch (dimensionKeyString) {
			case "minecraft:overworld":
				return "Overworld";
			case "minecraft:nether":
				return "Nether";
			case "minecraft:end":
				return "End";
			default:
				if (dimensionKeyString.contains(":")) {
					return dimensionKeyString.substring(0, dimensionKeyString.indexOf(":"));
				} else {
					return dimensionKeyString;
				}
		}
	}
	
	@Contract(pure = true)
	public static Direction directionFromRotation(@NotNull BlockRotation blockRotation) {
		switch (blockRotation) {
			case NONE -> {
				return Direction.EAST;
			}
			case CLOCKWISE_90 -> {
				return Direction.SOUTH;
			}
			case CLOCKWISE_180 -> {
				return Direction.WEST;
			}
			default -> {
				return Direction.NORTH;
			}
		}
	}
	
	public static boolean hasPlayerFinishedMod(PlayerEntity player) {
		return Support.hasAdvancement(player, PROGRESSION_FINISHED_ADVANCEMENT_IDENTIFIER);
	}
	
	public static Optional<BlockPos> getNexReplaceableBlockPosUpDown(World world, BlockPos blockPos, int maxUpDown) {
		if (world.getBlockState(blockPos).getMaterial().isReplaceable()) {
			// search down
			for (int i = 0; i < maxUpDown; i++) {
				if (!world.getBlockState(blockPos.down(i + 1)).getMaterial().isReplaceable()) {
					return Optional.of(blockPos.down(i));
				}
			}
		} else {
			// search up
			for (int i = 0; i < maxUpDown; i++) {
				if (!world.getBlockState(blockPos.up(i + 1)).getMaterial().isReplaceable()) {
					return Optional.of(blockPos.up(i));
				}
			}
		}
		return Optional.empty();
	}
	
}