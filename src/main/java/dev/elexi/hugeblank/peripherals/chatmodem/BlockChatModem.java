package dev.elexi.hugeblank.peripherals.chatmodem;

import dan200.computercraft.shared.peripheral.modem.ModemShapes;
import dan200.computercraft.shared.util.WaterloggableHelpers;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static dan200.computercraft.shared.util.WaterloggableHelpers.*;
import static net.minecraft.state.property.Properties.WATERLOGGED;

public class BlockChatModem extends Block implements Waterloggable, BlockEntityProvider {

    public static final DirectionProperty FACING = Properties.FACING;
    public static final BooleanProperty ON = BooleanProperty.of( "on" );
    public static final BooleanProperty PAIRED = BooleanProperty.of( "paired" );
    private boolean creative;

    public BlockChatModem(Settings settings, boolean creative) {
        super(settings);
        setDefaultState( getStateManager().getDefaultState()
            .with( FACING, Direction.NORTH )
            .with( ON, false )
            .with( PAIRED, false)
            .with( WATERLOGGED, false ) );
        this.creative = creative;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        BlockEntityType<ChatModemBlockEntity> use;
        if (this.creative) {
            use = ChatModemBlockEntity.creativeChatModem;
        } else {
            use = ChatModemBlockEntity.normalChatModem;
        }
        return new ChatModemBlockEntity(use, pos, state, this.creative);
    }

    @Override
    @Deprecated
    public ActionResult onUse(BlockState blockState_1, World world_1, BlockPos blockPos_1, PlayerEntity playerEntity_1, Hand hand_1, BlockHitResult blockHitResult_1) {
        if (hand_1 == Hand.MAIN_HAND && !world_1.isClient) {
            BlockEntity be = world_1.getBlockEntity(blockPos_1);
            if (be instanceof ChatModemBlockEntity) {
                ChatModemBlockEntity chatmodem = (ChatModemBlockEntity) be;
                chatmodem.onBlockInteraction(playerEntity_1);
                return ActionResult.SUCCESS;
            };
        }
        return ActionResult.FAIL;
    }

    @Override
    @Deprecated
    public void neighborUpdate(BlockState blockState_1, World world_1, BlockPos blockPos_1, Block block_1, BlockPos blockPos_2, boolean boolean_1) {
        super.neighborUpdate(blockState_1, world_1, blockPos_1, block_1, blockPos_2, boolean_1);
        BlockEntity be = world_1.getBlockEntity(blockPos_1);
        if (be instanceof ChatModemBlockEntity) {
            ChatModemBlockEntity chatmodem = (ChatModemBlockEntity) be;
            chatmodem.markDirty();
        }
    }

    @Override
    @Deprecated
    public final void onStateReplaced(@Nonnull BlockState block, @Nonnull World world, @Nonnull BlockPos pos, BlockState replace, boolean bool )
    {
        if( block.getBlock() == replace.getBlock() ) return;

        BlockEntity tile = world.getBlockEntity( pos );
        super.onStateReplaced( block, world, pos, replace, bool );
        world.removeBlockEntity( pos );
        if( tile instanceof ChatModemBlockEntity) ((ChatModemBlockEntity) tile).destroy();
    }

    @Override
    protected void appendProperties( StateManager.Builder<Block, BlockState> builder )
    {
        builder.add( FACING, ON, PAIRED, WATERLOGGED );
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getOutlineShape(BlockState blockState, BlockView world, BlockPos pos, ShapeContext position )
    {
        return ModemShapes.getBounds( blockState.get( FACING ) );
    }

    @Nonnull
    @Override
    @Deprecated
    public FluidState getFluidState(BlockState state )
    {
        return WaterloggableHelpers.getFluidState(state);
    }

    @Nonnull
    @Deprecated
    public BlockState getStateForNeighborUpdate(BlockState state, Direction side, BlockState otherState, World world, BlockPos pos, BlockPos otherPos )
    {
        updateShape( state, world, pos );
        return side == state.get( FACING ) && !state.canPlaceAt( world, pos )
                ? state.getFluidState().getBlockState()
                : state;
    }

    @Override
    @Deprecated
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos )
    {
        Direction facing = state.get( FACING );
        BlockPos offsetPos = pos.offset( facing );
        BlockState offsetState = world.getBlockState( offsetPos );
        return Block.isFaceFullSquare( offsetState.getCollisionShape( world, offsetPos ), facing.getOpposite() );
    }

    @Nullable
    @Override
    public BlockState getPlacementState( ItemPlacementContext placement )
    {
        return getDefaultState()
                .with( FACING, placement.getSide().getOpposite() )
                .with( WATERLOGGED, getFluidStateForPlacement( placement ) );
    }
}
