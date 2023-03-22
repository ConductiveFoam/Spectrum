package de.dafuqs.spectrum.blocks.titration_barrel;

import de.dafuqs.spectrum.helpers.*;
import de.dafuqs.spectrum.recipe.*;
import de.dafuqs.spectrum.recipe.titration_barrel.*;
import de.dafuqs.spectrum.registries.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.sound.*;
import net.minecraft.state.*;
import net.minecraft.state.property.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class TitrationBarrelBlock extends HorizontalFacingBlock implements BlockEntityProvider {
	
	enum BarrelState implements StringIdentifiable {
		EMPTY,
		FILLED,
		SEALED,
		TAPPED;
		
		@Override
		public String asString() {
			return this.toString().toLowerCase(Locale.ROOT);
		}
	}
	
	public static final EnumProperty<BarrelState> BARREL_STATE = EnumProperty.of("barrel_state", BarrelState.class);
	
	public TitrationBarrelBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(BARREL_STATE, BarrelState.EMPTY));
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TitrationBarrelBlockEntity(pos, state);
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		} else {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof TitrationBarrelBlockEntity barrelEntity) {
				
				BarrelState barrelState = state.get(BARREL_STATE);
				switch (barrelState) {
					case EMPTY, FILLED -> {
						if (player.isSneaking()) {
							if (barrelState == BarrelState.FILLED) {
								tryExtractLastStack(state, world, pos, player, barrelEntity);
							}
						} else {
							// player is able to put items in
							// or seal it with a piece of colored wood
							ItemStack handStack = player.getStackInHand(hand);
							if (handStack.isEmpty()) {
								int itemCount = InventoryHelper.countItemsInInventory(barrelEntity.inventory);
								if(itemCount == TitrationBarrelBlockEntity.MAX_ITEM_COUNT) {
									player.sendMessage(Text.translatable("block.spectrum.titration_barrel.content_count_full", itemCount), false);
								} else {
									player.sendMessage(Text.translatable("block.spectrum.titration_barrel.content_count", itemCount, TitrationBarrelBlockEntity.MAX_ITEM_COUNT), false);
								}
							} else {
								if (handStack.isIn(SpectrumItemTags.COLORED_PLANKS)) {
									if(!player.isCreative()) {
										handStack.decrement(1);
									}
									sealBarrel(world, pos, state, barrelEntity, player);
								} else if (handStack.getItem() instanceof BucketItem) {
									barrelEntity.useBucket(world, pos, state, handStack, player, hand);
								} else {
									int countBefore = handStack.getCount();
									ItemStack leftoverStack = InventoryHelper.addToInventoryUpToSingleStackWithMaxTotalCount(handStack, barrelEntity.getInventory(), TitrationBarrelBlockEntity.MAX_ITEM_COUNT);
									player.setStackInHand(hand, leftoverStack);
									if (countBefore != leftoverStack.getCount()) {
										world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.8F, 0.8F + world.random.nextFloat() * 0.6F);
										if (barrelState == BarrelState.EMPTY) {
											world.setBlockState(pos, state.with(BARREL_STATE, BarrelState.FILLED));
										}
									}
								}
							}
						}
					}
					case SEALED -> {
						// player is able to query the days the barrel already ferments
						// or open it with a shift-click
						if (player.isSneaking()) {
							unsealBarrel(world, pos, state, barrelEntity);
						} else {
							if (player.isCreative() && player.getMainHandStack().isOf(SpectrumItems.PAINTBRUSH)) {
								player.sendMessage(Text.translatable("block.spectrum.titration_barrel.debug_added_day"), false);
								barrelEntity.addDayOfSealTime();
								world.playSound(null, pos, SpectrumSoundEvents.NEW_RECIPE, SoundCategory.BLOCKS, 1.0F, 1.0F);
							}
							player.sendMessage(Text.translatable("block.spectrum.titration_barrel.days_of_sealing_before_opened", barrelEntity.getSealMinecraftDays(), barrelEntity.getSealRealDays()), false);
						}
					}
					case TAPPED -> {
						// player is able to extract content until it is empty
						// reverting it to the empty state again
						if (player.isSneaking()) {
							Optional<ITitrationBarrelRecipe> recipe = world.getRecipeManager().getFirstMatch(SpectrumRecipeTypes.TITRATION_BARREL, barrelEntity.inventory, world);
							if (recipe.isPresent()) {
								player.sendMessage(Text.translatable("block.spectrum.titration_barrel.days_of_sealing_after_opened_with_extractable_amount", recipe.get().getOutput().getName().getString(), barrelEntity.getSealMinecraftDays(), barrelEntity.getSealRealDays()), false);
							} else {
								player.sendMessage(Text.translatable("block.spectrum.titration_barrel.invalid_recipe_after_opened", barrelEntity.getSealMinecraftDays(), barrelEntity.getSealRealDays()), false);
							}
						} else {
							ItemStack harvestedStack = barrelEntity.tryHarvest(world, pos, state, player.getStackInHand(hand), player);
							if (!harvestedStack.isEmpty()) {
								Support.givePlayer(player, harvestedStack);
								world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1.0F, 1.0F);
							}
						}
					}
				}
			}
			
			return ActionResult.CONSUME;
		}
	}
	
	private void tryExtractLastStack(BlockState state, World world, BlockPos pos, PlayerEntity player, TitrationBarrelBlockEntity barrelEntity) {
		Optional<ItemStack> stack = InventoryHelper.extractLastStack(barrelEntity.inventory);
		if (stack.isPresent()) {
			Support.givePlayer(player, stack.get());
			barrelEntity.markDirty();
			if (barrelEntity.inventory.isEmpty()) {
				world.setBlockState(pos, state.with(BARREL_STATE, BarrelState.EMPTY));
			}
			world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}
	}
	
	private void sealBarrel(World world, BlockPos pos, BlockState state, TitrationBarrelBlockEntity barrelEntity, PlayerEntity player) {
		// give recipe remainders
		barrelEntity.giveRecipeRemainders(player);
		
		// seal
		world.setBlockState(pos, state.with(BARREL_STATE, BarrelState.SEALED));
		barrelEntity.seal();
		world.playSound(null, pos, SoundEvents.BLOCK_BARREL_CLOSE, SoundCategory.BLOCKS, 1.0F, 1.0F);
	}
	
	private void unsealBarrel(World world, BlockPos pos, BlockState state, TitrationBarrelBlockEntity barrelEntity) {
		world.setBlockState(pos, state.with(BARREL_STATE, BarrelState.TAPPED));
		barrelEntity.tap();
		world.playSound(null, pos, SoundEvents.BLOCK_BARREL_OPEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, BARREL_STATE);
	}
	
	// drop all currently stored items
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!newState.isOf(this) && state.get(BARREL_STATE) == BarrelState.FILLED) {
			scatterContents(world, pos);
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	public static void scatterContents(@NotNull World world, BlockPos pos) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TitrationBarrelBlockEntity titrationBarrelBlockEntity) {
			ItemScatterer.spawn(world, pos, titrationBarrelBlockEntity.getInventory());
		}
	}
	
}
