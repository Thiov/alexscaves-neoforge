package com.github.alexmodguy.alexscaves.server.block.blockentity;

import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class GingerbarrelBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private Component customName;
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        
        protected void onOpen(Level level, BlockPos blockPos, BlockState blockState) {
            GingerbarrelBlockEntity.this.playSound(blockState, ACSoundRegistry.METAL_BARREL_LID.get());
            GingerbarrelBlockEntity.this.updateBlockState(blockState, true);
        }

        
        protected void onClose(Level level, BlockPos blockPos, BlockState blockState) {
            GingerbarrelBlockEntity.this.playSound(blockState, ACSoundRegistry.METAL_BARREL_LID.get());
            GingerbarrelBlockEntity.this.updateBlockState(blockState, false);
        }

        
        protected void openerCountChanged(Level level, BlockPos blockPos, BlockState blockState, int openCount, int previousOpenCount) {
        }

        
        public boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof ChestMenu chestMenu) {
                Container container = chestMenu.getContainer();
                return container == GingerbarrelBlockEntity.this;
            }
            return false;
        }
    };

    public GingerbarrelBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ACBlockEntityRegistry.GINGERBARREL.get(), blockPos, blockState);
    }

    
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.storeNullable("CustomName", ComponentSerialization.CODEC, this.customName);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items);
        }
    }

    
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        this.customName = tag.read("CustomName", ComponentSerialization.CODEC).orElse(null);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items);
        }
    }

    
    public int getContainerSize() {
        return 9;
    }

    
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    
    protected Component getDefaultName() {
        return Component.translatable("block.alexscaves.gingerbarrel");
    }

    public void setCustomName(Component name) {
        this.customName = name;
    }

    
    public Component getName() {
        return this.customName != null ? this.customName : this.getDefaultName();
    }

    
    public Component getDisplayName() {
        return this.getName();
    }

    
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x1, containerId, inventory, this, 1);
    }

    
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState(), 0.0D);
        }
    }

    
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    private void updateBlockState(BlockState blockState, boolean open) {
        Level level = this.getLevel();
        if (level != null) {
            level.setBlock(this.getBlockPos(), blockState.setValue(BarrelBlock.OPEN, open), 3);
        }
    }

    private void playSound(BlockState blockState, SoundEvent soundEvent) {
        Level level = this.getLevel();
        if (level == null) {
            return;
        }
        Vec3i vec3i = blockState.getValue(BarrelBlock.FACING).getUnitVec3i();
        double x = this.worldPosition.getX() + 0.5D + vec3i.getX() / 2.0D;
        double y = this.worldPosition.getY() + 0.5D + vec3i.getY() / 2.0D;
        double z = this.worldPosition.getZ() + 0.5D + vec3i.getZ() / 2.0D;
        level.playSound(null, x, y, z, soundEvent, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }
}
