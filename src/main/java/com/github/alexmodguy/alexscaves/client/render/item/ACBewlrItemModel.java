package com.github.alexmodguy.alexscaves.client.render.item;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Custom item-model type {@code alexscaves:bewlr}. Mirrors vanilla {@code SpecialModelWrapper} (a base
 * model supplies the per-context display transforms via {@link ModelRenderProperties#applyToLayer}, and a
 * {@link net.minecraft.client.renderer.special.SpecialModelRenderer} draws the geometry) — but builds a
 * context-carrying {@link ACBewlrArg} in {@link #update} (the one place the {@link ItemDisplayContext} is
 * available) instead of the context-blind {@code SpecialModelRenderer.extractArgument}, so AC's
 * context-dependent {@code renderByItem} works unchanged. Registered into {@code ItemModels.ID_MAPPER}
 * by {@code mixin.client.ItemModelsMixin}.
 */
public class ACBewlrItemModel implements ItemModel {

    private final ACBewlrRenderer renderer;
    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;
    private final Supplier<Vector3fc[]> extents;

    public ACBewlrItemModel(ACBewlrRenderer renderer, ModelRenderProperties properties, Matrix4fc transformation) {
        this.renderer = renderer;
        this.properties = properties;
        this.transformation = transformation;
        this.extents = Suppliers.memoize(() -> {
            Set<Vector3fc> results = new HashSet<>();
            renderer.getExtents(results::add);
            return results.toArray(new Vector3fc[0]);
        });
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver,
            ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        output.appendModelIdentityElement(this);
        output.appendModelIdentityElement(displayContext);
        // AC's renders are animated and depend on live client state + display context; never cache them.
        output.setAnimated();
        LayerRenderState layer = output.newLayer();
        if (item.hasFoil()) {
            layer.setFoilType(FoilType.STANDARD);
            output.appendModelIdentityElement(FoilType.STANDARD);
        }
        ACBewlrArg arg = new ACBewlrArg(item, displayContext);
        layer.setExtents(this.extents);
        layer.setLocalTransform(this.transformation);
        layer.setupSpecialModel(this.renderer, arg);
        this.properties.applyToLayer(layer, displayContext);
    }

    public record Unbaked(Identifier base) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                Identifier.CODEC.fieldOf("base").forGetter(Unbaked::base)
            ).apply(i, Unbaked::new)
        );

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.base);
        }

        @Override
        public ItemModel bake(BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(this.base);
            TextureSlots textureSlots = model.getTopTextureSlots();
            ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, model, textureSlots);
            return new ACBewlrItemModel(new ACBewlrRenderer(this.base.getPath()), properties, transformation);
        }
    }
}
