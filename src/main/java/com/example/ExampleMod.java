package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
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

    private static boolean enabled = false;

    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {

        toggleKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.autocore.toggle",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_G,
                        "category.autocore"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null || client.world == null) {
                return;
            }

            // G = ON / OFF
            while (toggleKey.wasPressed()) {

                enabled = !enabled;

                if (enabled) {
                    client.player.sendMessage(
                            Text.literal("§a[AutoCore] Auto Scanner ON"),
                            true
                    );
                } else {
                    client.player.sendMessage(
                            Text.literal("§c[AutoCore] Auto Scanner OFF"),
                            true
                    );
                }
            }

            if (!enabled) {
                tickCounter = 0;
                return;
            }

            // Scan every 10 ticks = 0.5 second
            tickCounter++;

            if (tickCounter >= 10) {
                tickCounter = 0;
                scanVaults(client);
            }
        });
    }

    private void scanVaults(net.minecraft.client.MinecraftClient client) {

        BlockPos playerPos = client.player.getBlockPos();

        int radius = 5;

        BlockPos min = playerPos.add(
                -radius,
                -radius,
                -radius
        );

        BlockPos max = playerPos.add(
                radius,
                radius,
                radius
        );

        for (BlockPos pos : BlockPos.iterate(min, max)) {

            if (!client.world.getBlockState(pos).isOf(Blocks.VAULT)) {
                continue;
            }

            BlockEntity blockEntity =
                    client.world.getBlockEntity(pos);

            if (!(blockEntity instanceof VaultBlockEntity vault)) {
                continue;
            }

            ItemStack displayItem =
                    vault.getClientData().getDisplayItem();

            if (displayItem == null || displayItem.isEmpty()) {
                continue;
            }

            if (!displayItem.isOf(Items.HEAVY_CORE)) {
                continue;
            }

            BlockHitResult hitResult =
                    new BlockHitResult(
                            Vec3d.ofCenter(pos),
                            Direction.UP,
                            pos,
                            false
                    );

            if (client.interactionManager != null) {

                client.interactionManager.interactBlock(
                        client.player,
                        Hand.MAIN_HAND,
                        hitResult
                );

                client.player.sendMessage(
                        Text.literal(
                                "§a[AutoCore] Vault with Heavy Core found!"
                        ),
                        true
                );

                return;
            }
        }
    }
}
