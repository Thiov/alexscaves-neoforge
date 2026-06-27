package com.github.alexmodguy.alexscaves.server.entity.item;

import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class MovingMetalBlockEntity extends AbstractMovingBlockEntity {

    public MovingMetalBlockEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    
    public boolean movesEntities() {
        return true;
    }

    
    public boolean canBeCollidedWith(Entity entity) {
        return true;
    }

}
