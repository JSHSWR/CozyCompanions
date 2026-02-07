// temp commit test
package com.coenn.cozycompanions;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

/**
 * The main mod class for Cozy Companions.
 * This class is automatically loaded by Forge as it is annotated with {@link Mod}.
 * The mod id must match the id defined in the mods.toml file.
 */
@Mod(CozyCompanions.MODID)
public final class CozyCompanions {
    /** The mod ID used in registries and other references. */
    public static final String MODID = "cozycompanions";
    /** A logger for this mod. */
    private static final Logger LOGGER = LogUtils.getLogger();
    /** A DeferredRegister for registering blocks under this mod's namespace. */
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    /** A DeferredRegister for registering items under this mod's namespace. */
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    /** A DeferredRegister for registering creative mode tabs under this mod's namespace. */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    /** Example block registration with the path "example_block". */
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block",
        () -> new Block(BlockBehaviour.Properties.of()
            .setId(BLOCKS.key("example_block"))
            .mapColor(MapColor.STONE)
        )
    );
    /** Example block item registration which corresponds to {@link #EXAMPLE_BLOCK}. */
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block",
        () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties().setId(ITEMS.key("example_block")))
    );

    /** Example food item registration with simple nutrition values. */
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item",
        () -> new Item(new Item.Properties()
            .setId(ITEMS.key("example_item"))
            .food(new FoodProperties.Builder()
                .alwaysEdible()
                .nutrition(1)
                .saturationModifier(2f)
                .build()
            )
        )
    );

    /** Example creative tab which appears after the combat tab. */
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                // Add the example item to the tab. For custom tabs, this method is preferred over the event.
                output.accept(EXAMPLE_ITEM.get());
            }).build());

    /**
     * Constructs the mod instance.
     * @param context The mod loading context provided by Forge.
     */
    public CozyCompanions(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();
        // Register the common setup method for modloading.
        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);
        // Register deferred registries to the mod event bus.
        BLOCKS.register(modBusGroup);
        ITEMS.register(modBusGroup);
        CREATIVE_MODE_TABS.register(modBusGroup);
        // Register item to a creative tab via event bus.
        BuildCreativeModeTabContentsEvent.BUS.addListener(CozyCompanions::addCreative);
        // Register our mod's configuration specification so that Forge can create and load the config file.
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /**
     * Common setup event. This runs on both client and server.
     * @param event The setup event.
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        // Log a message to indicate that common setup is running.
        LOGGER.info("Cozy Companions: common setup initializing");
    }

    /**
     * Adds items to the building blocks creative tab when the BuildCreativeModeTabContentsEvent is fired.
     * @param event The build creative tab event.
     */
    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(EXAMPLE_BLOCK_ITEM);
    }

    /**
     * Client-specific event subscriptions.
     * Use this nested static class to subscribe to client-only events.
     */
    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        /**
         * Runs during the client setup event.
         * @param event The client setup event.
         */
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Cozy Companions: client setup initializing");
            // Print the player's name to the log as a simple confirmation feature.
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}