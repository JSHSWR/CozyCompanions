package com.coenn.cozycompanions;

import com.coenn.cozycompanions.client.screen.VillagerDialogueScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CozyCompanions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class VillagerBondEvents {

    @SubscribeEvent
    public static void onRightClickVillager(PlayerInteractEvent.EntityInteractSpecific event) {
        // Only run on client
        if (!event.getLevel().isClientSide()) return;

        // Only when sneaking
        if (!event.getEntity().isShiftKeyDown()) return;

        if (!(event.getTarget() instanceof Villager villager)) return;

        // Open the cozy dialogue screen
        Minecraft.getInstance().setScreen(new VillagerDialogueScreen(villager));

        // Prevent the trading UI from opening when we trigger our interaction
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}