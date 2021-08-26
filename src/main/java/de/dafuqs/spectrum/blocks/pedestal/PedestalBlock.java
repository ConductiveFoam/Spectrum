package de.dafuqs.spectrum.blocks.pedestal;

import de.dafuqs.spectrum.blocks.RedstonePoweredBlock;
import de.dafuqs.spectrum.registries.SpectrumBlockEntityRegistry;
import de.dafuqs.spectrum.registries.SpectrumBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class PedestalBlock extends BlockWithEntity implements RedstonePoweredBlock {

    private final PedestalVariant variant;
    public static final EnumProperty<RedstonePowerState> STATE = EnumProperty.of("state", RedstonePowerState.class);

    public enum PedestalVariant {
        BASIC_TOPAZ,
        BASIC_AMETHYST,
        BASIC_CITRINE,
        ALL_BASIC,
        ONYX,
        MOONSTONE
    }

    public PedestalBlock(Settings settings, PedestalVariant variant) {
        super(settings);
        this.variant = variant;
        setDefaultState(getStateManager().getDefaultState().with(STATE, RedstonePowerState.UNPOWERED));
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if(placer instanceof ServerPlayerEntity) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if(blockEntity instanceof PedestalBlockEntity) {
                ((PedestalBlockEntity) blockEntity).setOwner((ServerPlayerEntity) placer);
                blockEntity.markDirty();
            }
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(STATE);
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            this.openScreen(world, pos, player);
            return ActionResult.CONSUME;
        }
    }

    protected void openScreen(World world, BlockPos pos, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PedestalBlockEntity) {
            PedestalBlockEntity pedestalBlockEntity = (PedestalBlockEntity) blockEntity;

            if(!pedestalBlockEntity.hasOwner()) {
                pedestalBlockEntity.setOwner(player);
            }

            if(pedestalBlockEntity.isOwner(player)) {
                player.openHandledScreen((NamedScreenHandlerFactory) blockEntity);
            }
        }
    }

    public static void updateUpgrades(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if(blockEntity instanceof PedestalBlockEntity) {
            ((PedestalBlockEntity) blockEntity).updateUpgrades();
        }
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if(newState.getBlock() instanceof PedestalBlock) {
            if (!state.getBlock().equals(newState.getBlock())) {
                // pedestal is getting upgraded. Keep the blockEntity with its contents
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof PedestalBlockEntity) {
                    if (state.getBlock().equals(newState.getBlock())) {
                        PedestalVariant newVariant = ((PedestalBlock) newState.getBlock()).getVariant();
                        ((PedestalBlockEntity) blockEntity).setVariant(newVariant);
                    }
                }
            }
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PedestalBlockEntity) {
                ItemScatterer.spawn(world, pos, (Inventory)blockEntity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    /**
     * Sets pedestal to a new tier
     * while keeping the inventory and all other data
     */
    public static void upgradeToVariant(World world, BlockPos blockPos, PedestalVariant newPedestalVariant) {
        world.setBlockState(blockPos, getPedestalBlockForVariant(newPedestalVariant).getPlacementState(new AutomaticItemPlacementContext(world, blockPos, Direction.DOWN, null, Direction.UP)));
    }

    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if(world.isClient) {
            return null;
        } else {
            return checkType(type, SpectrumBlockEntityRegistry.PEDESTAL, PedestalBlockEntity::serverTick);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            if(this.isGettingPowered(world, pos)) {
                this.power(world, pos);
            } else {
                this.unPower(world, pos);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(PedestalBlock.STATE).equals(RedstonePowerState.POWERED)) {
            Vec3f vec3f = new Vec3f(0.5F, 0.5F, 0.5F);
            float xOffset = random.nextFloat();
            float zOffset = random.nextFloat();
            world.addParticle(new DustParticleEffect(vec3f, 1.0F), pos.getX() + xOffset, pos.getY() + 1, pos.getZ() + zOffset, 0.0D, 0.0D, 0.0D);
        }
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState placementState = this.getDefaultState();

        if(ctx.getWorld().getReceivedRedstonePower(ctx.getBlockPos()) > 0) {
            placementState = placementState.with(STATE, RedstonePowerState.POWERED);
        }

        return placementState;
    }

    public PedestalVariant getVariant() {
        return this.variant;
    }

    public static Block getPedestalBlockForVariant(PedestalVariant variant) {
        switch (variant) {
            case BASIC_TOPAZ -> {
                return SpectrumBlocks.PEDESTAL_BASIC_TOPAZ;
            }
            case BASIC_AMETHYST -> {
                return SpectrumBlocks.PEDESTAL_BASIC_AMETHYST;
            }
            case BASIC_CITRINE -> {
                return SpectrumBlocks.PEDESTAL_BASIC_CITRINE;
            }
            case ALL_BASIC -> {
                return SpectrumBlocks.PEDESTAL_ALL_BASIC;
            }
            case ONYX -> {
                return SpectrumBlocks.PEDESTAL_ONYX;
            }
            default -> {
                return SpectrumBlocks.PEDESTAL_MOONSTONE;
            }
        }
    }

}