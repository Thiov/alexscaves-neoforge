package com.github.alexmodguy.alexscaves;

import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.client.config.ACClientConfig;
import com.github.alexmodguy.alexscaves.client.model.layered.ACModelLayers;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.config.ACModConfigSpec;
import com.github.alexmodguy.alexscaves.server.CommonProxy;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.block.blockentity.ACBlockEntityRegistry;
import com.github.alexmodguy.alexscaves.server.block.fluid.ACFluidRegistry;
import com.github.alexmodguy.alexscaves.server.block.poi.ACPOIRegistry;
import com.github.alexmodguy.alexscaves.server.config.ACServerConfig;
import com.github.alexmodguy.alexscaves.server.config.BiomeGenerationConfig;
import com.github.alexmodguy.alexscaves.server.entity.ACEntityDataRegistry;
import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.entity.ACFrogRegistry;
import com.github.alexmodguy.alexscaves.server.entity.util.ACAttachmentRegistry;
import com.github.alexmodguy.alexscaves.server.event.CommonEvents;
import com.github.alexmodguy.alexscaves.server.inventory.ACMenuRegistry;
import com.github.alexmodguy.alexscaves.server.item.ACArmorMaterial;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry;
import com.github.alexmodguy.alexscaves.server.level.carver.ACCarverRegistry;
import com.github.alexmodguy.alexscaves.server.level.feature.ACFeatureRegistry;
import com.github.alexmodguy.alexscaves.server.level.storage.ACWorldData;
import com.github.alexmodguy.alexscaves.server.level.structure.ACStructureRegistry;
import com.github.alexmodguy.alexscaves.server.level.structure.piece.ACStructurePieceRegistry;
import com.github.alexmodguy.alexscaves.server.level.structure.processor.ACStructureProcessorRegistry;
import com.github.alexmodguy.alexscaves.server.level.surface.ACSurfaceRuleConditionRegistry;
import com.github.alexmodguy.alexscaves.server.level.surface.ACSurfaceRules;
import com.github.alexmodguy.alexscaves.server.message.*;
import com.github.alexmodguy.alexscaves.server.misc.ACAdvancementTriggerRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACCreativeTabRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACDataComponentRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACLoadedMods;
import com.github.alexmodguy.alexscaves.server.misc.ACLootTableRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACPlayerCapes;
import com.github.alexmodguy.alexscaves.server.misc.ACPotPatternRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.server.misc.WebHelper;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.github.alexmodguy.alexscaves.server.recipe.ACRecipeRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mod(AlexsCaves.MODID)
public class AlexsCaves {
    public static final String MODID = "alexscaves";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String VERSION = "2.0.2-neoforge";

    public static CommonProxy PROXY;
    private IEventBus modEventBus;

    public static final TicketController TICKET_CONTROLLER = new TicketController(
            Identifier.fromNamespaceAndPath(MODID, "default"),
            ACWorldData::clearLoadedChunksCallback);

    public static final ACServerConfig COMMON_CONFIG;
    public static final ACClientConfig CLIENT_CONFIG;
    public static final List<String> MOD_GENERATION_CONFLICTS = new ArrayList<>();
    private static boolean worldgenBootstrapped;

    static {
        final Pair<ACServerConfig, ACModConfigSpec> serverPair = new ACModConfigSpec.Builder()
                .configure(ACServerConfig::new);
        COMMON_CONFIG = serverPair.getLeft();

        final Pair<ACClientConfig, ACModConfigSpec> clientPair = new ACModConfigSpec.Builder()
                .configure(ACClientConfig::new);
        CLIENT_CONFIG = clientPair.getLeft();
    }

    public AlexsCaves(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        this.modEventBus = modEventBus;
        PROXY = dist.isClient() ? new ClientProxy() : new CommonProxy();

        // Mod-bus lifecycle / event listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::loadComplete);
        modEventBus.addListener(com.github.alexmodguy.alexscaves.server.ACNetworking::registerPayloads);
        modEventBus.addListener(this::registerLayerDefinitions);
        modEventBus.addListener(this::registerTicketControllers);

        // Game-bus listeners
        NeoForge.EVENT_BUS.register(new CommonEvents());
        NeoForge.EVENT_BUS.addListener(ACEffectRegistry::registerBrewingRecipes);

        // ENTITY_FALLBACK: only needed if ACEntityRegistry.initializeAttributes / spawnPlacements are
        // NOT annotated @SubscribeEvent (ACEntityRegistry is @EventBusSubscriber(bus = MOD)). If you add
        // @SubscribeEvent to those two methods, delete these two lines.
        // modEventBus.addListener(ACEntityRegistry::initializeAttributes);
        // modEventBus.addListener(ACEntityRegistry::spawnPlacements);

        // DeferredRegister registration (all 31)
        ACBlockRegistry.DEF_REG.register(modEventBus);
        ACBlockEntityRegistry.DEF_REG.register(modEventBus);
        ACFluidRegistry.FLUID_TYPE_DEF_REG.register(modEventBus);
        ACFluidRegistry.FLUID_DEF_REG.register(modEventBus);
        ACItemRegistry.DEF_REG.register(modEventBus);
        ACArmorMaterial.ARMOR_MATERIALS.register(modEventBus);
        ACParticleRegistry.DEF_REG.register(modEventBus);
        ACEntityRegistry.DEF_REG.register(modEventBus);
        ACEntityDataRegistry.DEF_REG.register(modEventBus);
        // ACAttachmentRegistry: no DeferredRegister — magnetic data is a WeakHashMap helper (see spec 7).
        ACFrogRegistry.DEF_REG.register(modEventBus);
        ACPOIRegistry.DEF_REG.register(modEventBus);
        ACFeatureRegistry.DEF_REG.register(modEventBus);
        ACCarverRegistry.DEF_REG.register(modEventBus);
        ACSurfaceRuleConditionRegistry.DEF_REG.register(modEventBus);
        ACSoundRegistry.DEF_REG.register(modEventBus);
        ACStructureRegistry.DEF_REG.register(modEventBus);
        ACStructurePieceRegistry.DEF_REG.register(modEventBus);
        ACStructureProcessorRegistry.DEF_REG.register(modEventBus);
        ACEffectRegistry.DEF_REG.register(modEventBus);
        ACEffectRegistry.POTION_DEF_REG.register(modEventBus);
        ACMenuRegistry.DEF_REG.register(modEventBus);
        ACRecipeRegistry.TYPE_DEF_REG.register(modEventBus);
        ACRecipeRegistry.DEF_REG.register(modEventBus);
        ACAdvancementTriggerRegistry.DEF_REG.register(modEventBus);
        ACLootTableRegistry.GLOBAL_LOOT_MODIFIER_DEF_REG.register(modEventBus);
        ACLootTableRegistry.LOOT_FUNCTION_DEF_REG.register(modEventBus);
        ACCreativeTabRegistry.DEF_REG.register(modEventBus);
        ACDataComponentRegistry.init(modEventBus);
        ACPotPatternRegistry.init();

        PROXY.commonInit(modEventBus);
        // clientInit MUST run here in the constructor — it registers the mod-bus listeners for the client
        // setup/registration events (EntityRenderersEvent.RegisterRenderers, RegisterClientExtensionsEvent,
        // ModelEvent, RegisterGuiLayersEvent, the fluid client extensions, ...). Deferring it via
        // FMLClientSetupEvent.enqueueWork (as before) added those listeners AFTER the registration events had
        // already fired, so registerRenderers NEVER ran → NO AC entity renderer was registered → every AC
        // entity (mobs, projectiles, weapon effects, ...) rendered invisibly. commonInit (above) is already
        // wired here for the same reason. On a dedicated server PROXY is CommonProxy and clientInit is a no-op.
        PROXY.clientInit(modEventBus);
        ACBiomeRegistry.init();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        PROXY.initPathfinding();
        event.enqueueWork(() -> {
            ACSurfaceRules.setup();
            ACPlayerCapes.setup();
            ACEffectRegistry.setup();
            ACBlockRegistry.setup();
            ACItemRegistry.setup();
            ACBlockEntityRegistry.expandVanillaDefinitions();
        });
        BiomeGenerationConfig.reloadConfig();
        readModIncompatibilities();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // clientInit moved to the mod constructor (see comment there) — registering its listeners here, even
        // via enqueueWork, is too late for the client registration events that fire during this phase.
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
        event.enqueueWork(ACFluidRegistry::postInit);
        event.enqueueWork(ACLoadedMods::afterAllModsLoaded);
    }

    private void registerLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        ACModelLayers.register(event);
    }

    private void registerTicketControllers(final RegisterTicketControllersEvent event) {
        event.register(TICKET_CONTROLLER);
        event.register(com.github.alexmodguy.alexscaves.server.entity.item.NuclearExplosionEntity.TICKET_CONTROLLER);
        event.register(com.github.alexmodguy.alexscaves.server.entity.item.BeholderEyeEntity.TICKET_CONTROLLER);
        event.register(com.github.alexmodguy.alexscaves.server.item.OccultGemItem.TICKET_CONTROLLER);
        event.register(com.github.alexmodguy.alexscaves.server.item.RemoteDetonatorItem.TICKET_CONTROLLER);
    }

    public static void setProxy(CommonProxy proxy) {
        PROXY = proxy;
    }

    public static <MSG extends CustomPacketPayload> void sendMSGToServer(MSG message) {
        net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(message);
    }

    public static <MSG extends CustomPacketPayload> void sendMSGToAll(MSG message) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }
        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            sendNonLocal(message, player);
        }
    }

    public static <MSG extends CustomPacketPayload> void sendNonLocal(MSG msg, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, msg);
    }

    private static void readModIncompatibilities() {
        MOD_GENERATION_CONFLICTS.clear();
        BufferedReader urlContents = WebHelper.getURLContents(
                "https://raw.githubusercontent.com/AlexModGuy/AlexsCaves/main/src/main/resources/assets/alexscaves/warning/mod_generation_conflicts.txt",
                "assets/alexscaves/warning/mod_generation_conflicts.txt");
        if (urlContents == null) {
            LOGGER.warn("Failed to load mod conflicts");
            return;
        }
        try {
            String line;
            while ((line = urlContents.readLine()) != null) {
                MOD_GENERATION_CONFLICTS.add(line);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load mod conflicts", e);
        }
    }
}
