package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private static boolean isAutoScanning = false;
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        // ចុះឈ្មោះ Keybind អក្សរ G លើ Keyboard
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autocore.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.autocore"
        ));

        // Event រត់រៀងរាល់ Tick ក្នុង Client
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // ចុច [G] ម្ដងបើក ចុច [G] ម្ដងទៀតបិទ
            while (toggleKey.wasPressed()) {
                isAutoScanning = !isAutoScanning;
                if (isAutoScanning) {
                    client.player.sendMessage(Text.literal("§a[AutoCore] បានបើក Auto Scanner! (ចុច G ម្ដងទៀតដើម្បីបិទ)"), true);
                } else {
                    client.player.sendMessage(Text.literal("§c[AutoCore] បានបិទ Auto Scanner!"), true);
                }
            }

            // ប្រសិនបើបើក Scanner វានឹងស្កេនរៀងរាល់ 10 Ticks (0.5 វិនាទី)
            if (isAutoScanning) {
                tickCounter++;
                if (tickCounter >= 10) { 
                    tickCounter = 0;
                    scanAndUnlockVault(client);
                }
            }
        });
    }

    private void scanAndUnlockVault(net.minecraft.client.MinecraftClient client) {
        BlockPos playerPos = client.player.getBlockPos();
        int radius = 5; // កាំនៃការស្កេន ៥ Blocks ជុំវិញខ្លួន

        for (BlockPos pos : BlockPos.iterate(playerPos.add(-radius, -radius, -radius), playerPos.add(radius, radius, radius))) {
            if (client.world.getBlockState(pos).isOf(Blocks.VAULT)) {
                BlockEntity blockEntity = client.world.getBlockEntity(pos);

                if (blockEntity instanceof VaultBlockEntity vault) {
                    var displayItem = vault.getClientData().getDisplayItem();

                    // ស្កេនរកមើល Vault ណាដែលបង្ហាញរូប Heavy Core
                    if (displayItem != null && displayItem.isOf(Items.HEAVY_CORE)) {
                        BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
                        
                        if (client.interactionManager != null) {
                            // ចាក់យក Item ដោយស្វ័យប្រវត្តិ (នៅតែបន្តបើក Scanner ដដែលរហូតដល់ចុច G បិទ)
                            client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hitResult);
                            client.player.sendMessage(Text.literal("§a[AutoCore] កំពុងចាក់ Vault ដែលមាន Heavy Core!"), true);
                            break;
                        }
                    }
                }
            }
        }
    }
}
