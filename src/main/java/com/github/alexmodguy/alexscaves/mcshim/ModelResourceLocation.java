package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.client.resources.model.*;

import net.minecraft.resources.Identifier;

import java.util.Objects;

public class ModelResourceLocation {
    private final Identifier id;
    private final String variant;

    public ModelResourceLocation(Identifier id, String variant) {
        this.id = id;
        this.variant = variant;
    }

    public static ModelResourceLocation vanilla(String path, String variant) {
        return new ModelResourceLocation(Identifier.withDefaultNamespace(path), variant);
    }

    public Identifier id() {
        return id;
    }

    public String variant() {
        return variant;
    }

    
    public String toString() {
        return id + "#" + variant;
    }

    
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ModelResourceLocation other)) {
            return false;
        }
        return Objects.equals(id, other.id) && Objects.equals(variant, other.variant);
    }

    
    public int hashCode() {
        return Objects.hash(id, variant);
    }
}
