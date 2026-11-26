/*
 * Copyright (C) 2025  Remote-Bot-Fetch-Server contributors
 * https://github.com/SGU-Server/Remote-Bot-Fetch-Server

 * Remote Bot Fetch Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.

 * Remote Bot Fetch Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Remote Bot Fetch Server.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.sguserver.remotebotfetch.menu;

import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import carpet.patches.EntityPlayerMPFake;

import com.sguserver.remotebotfetch.Mod;
import com.sguserver.remotebotfetch.lib.MessageComponent;

import org.jspecify.annotations.Nullable;

public final class RbfBotInventoryMenu extends InventoryMenu {
    public static final int PANEL_WIDTH = 176;
    public static final int BOT_X = PANEL_WIDTH + 8;
    public static final int WIDTH = BOT_X + PANEL_WIDTH;
    public static final int OPEN_BUTTON = 127;
    public static final int HIDE_BOT_BUTTON = 126;
    public static final int SHOW_BOT_BUTTON = 125;
    public static final int NEXT_BOT_PAGE = 124;
    public static final int PREVIOUS_BOT_PAGE = 123;
    public static final int BOTS_PER_PAGE = 4;
    public static final String TITLE_KEY = "remote-bot-fetch.gui.botInventoryTitle";

    private static final int PLAYER_CRAFTING_END = 5;
    private static final int PLAYER_STORAGE_START = 9;
    private static final int PLAYER_STORAGE_END = 45;
    private static final int BOT_START = 46;
    private static final int BOT_STORAGE_END = BOT_START + 36;
    private static final int BOT_SIZE = 41;
    private static final int ARMOR_ROW_Y = 18;
    private static final int PLACEHOLDER_ROW_Y = 36;
    private static final Container PLACEHOLDER_CONTAINER = new SimpleContainer(1);

    public static final MenuType<RbfBotInventoryMenu> TYPE = Registry.register(BuiltInRegistries.MENU,
        //#if MC>=12111
        net.minecraft.resources.Identifier.fromNamespaceAndPath(Mod.MOD_ID, "bot_inventory"),
        //#else
        //$$ net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Mod.MOD_ID, "bot_inventory"),
        //#endif
        new MenuType<>(RbfBotInventoryMenu::new, FeatureFlags.VANILLA_SET));

    private final Player viewer;
    private final Level openedLevel;
    private final @Nullable Level botOpenedLevel;
    private final @Nullable EntityPlayerMPFake bot;
    private final List<EntityPlayerMPFake> choices;
    private boolean botVisible = true;
    private int botPage;

    public static void register() {
        // Initializing this class registers TYPE before the built-in registries freeze.
    }

    public RbfBotInventoryMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, null, List.of());
    }

    RbfBotInventoryMenu(int syncId, Inventory playerInventory, @Nullable EntityPlayerMPFake bot,
        List<EntityPlayerMPFake> choices) {
        super(playerInventory, bot != null, playerInventory.player);
        this.containerId = syncId;
        this.viewer = playerInventory.player;
        this.openedLevel = this.viewer.level();
        this.bot = bot;
        this.botOpenedLevel = bot == null ? null : bot.level();
        this.choices = List.copyOf(choices);
        this.botPage = bot == null ? 0 : Math.max(0, choices.indexOf(bot)) / BOTS_PER_PAGE;
        this.addBotSlots(bot == null ? new SimpleContainer(BOT_SIZE) : bot.getInventory(),
            bot == null ? this.viewer : bot);
    }

    @Override
    public MenuType<?> getType() {
        // Vanilla clients do not know the custom menu type id. Client-only mods can register extra
        // menu types, which shifts the numeric ids and makes the client open the wrong screen.
        // Reporting the built-in crafting type keeps the packet id identical on both sides, and the
        // client recognizes our menu by TITLE_KEY in BotInventoryMenuScreensMixin.
        return MenuType.CRAFTING;
    }

    private void addBotSlots(Container inventory, LivingEntity owner) {
        // Keep GCA's six chest rows. The action controls in the first two rows are left blank.
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new BotSlot(inventory, 9 + row * 9 + column, 8 + column * 18, 54 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new BotSlot(inventory, column, 8 + column * 18, 108));
        }
        this.addSlot(
            new BotArmorSlot(inventory, owner, EquipmentSlot.HEAD, 39, 26, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET));
        this.addSlot(
            new BotArmorSlot(inventory, owner, EquipmentSlot.CHEST, 38, 44, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE));
        this.addSlot(
            new BotArmorSlot(inventory, owner, EquipmentSlot.LEGS, 37, 62, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS));
        this.addSlot(
            new BotArmorSlot(inventory, owner, EquipmentSlot.FEET, 36, 80, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS));
        this.addSlot(new BotSlot(inventory, 40, 134, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return owner.canUseSlot(EquipmentSlot.OFFHAND);
            }

            @Override
            public void setByPlayer(ItemStack stack, ItemStack previous) {
                owner.onEquipItem(EquipmentSlot.OFFHAND, previous, stack);
                super.setByPlayer(stack, previous);
            }

            @Override
            //#if MC>=12111
            public net.minecraft.resources.Identifier getNoItemIcon() {
                return InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
            }
            //#else
            //#if MC>=12104
            //$$ public net.minecraft.resources.ResourceLocation getNoItemIcon() {
            //$$     return InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
            //$$ }
            //#else
            //$$ public com.mojang.datafixers.util.Pair<net.minecraft.resources.ResourceLocation, net.minecraft.resources.ResourceLocation> getNoItemIcon() {
            //$$     return com.mojang.datafixers.util.Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            //$$ }
            //#endif
            //#endif
        });
        // GCA keeps every cell of the six row grid visible. The unused cells of the armour row and the
        // empty row below it become inert slots so the panel matches that layout without offering any
        // fake player controls.
        for (int column = 0; column < 9; column++) {
            boolean used = (column >= 1 && column <= 4) || column == 7;
            if (!used) {
                this.addSlot(new PlaceholderSlot(8 + column * 18, ARMOR_ROW_Y));
            }
            this.addSlot(new PlaceholderSlot(8 + column * 18, PLACEHOLDER_ROW_Y));
        }
    }

    private class PlaceholderSlot extends Slot {
        PlaceholderSlot(int x, int y) {
            super(PLACEHOLDER_CONTAINER, 0, BOT_X + x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean isActive() {
            return RbfBotInventoryMenu.this.botVisible;
        }
    }

    private class BotSlot extends Slot {
        BotSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, BOT_X + x, y);
        }

        @Override
        public boolean isActive() {
            return RbfBotInventoryMenu.this.botVisible;
        }
    }

    private class BotArmorSlot extends ArmorSlot {
        //#if MC>=12111
        BotArmorSlot(Container inventory, LivingEntity owner, EquipmentSlot slot, int index, int x,
            net.minecraft.resources.Identifier icon) {
            super(inventory, owner, slot, index, BOT_X + x, 18, icon);
        }
        //#else
        //$$ BotArmorSlot(Container inventory, LivingEntity owner, EquipmentSlot slot, int index, int x,
        //$$     net.minecraft.resources.ResourceLocation icon) {
        //$$     super(inventory, owner, slot, index, BOT_X + x, 18, icon);
        //$$ }
        //#endif

        @Override
        public boolean isActive() {
            return RbfBotInventoryMenu.this.botVisible && super.isActive();
        }
    }

    public void setBotVisible(boolean visible) {
        this.botVisible = visible;
    }

    public boolean isBotVisible() {
        return this.botVisible;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer viewer) || this.bot == null || !this.stillValid(viewer)) {
            return false;
        }
        if (id == HIDE_BOT_BUTTON || id == SHOW_BOT_BUTTON) {
            this.setBotVisible(id == SHOW_BOT_BUTTON);
            return true;
        }
        if (!this.botVisible) {
            return false;
        }
        if (id == PREVIOUS_BOT_PAGE || id == NEXT_BOT_PAGE) {
            int lastPage = (this.choices.size() - 1) / BOTS_PER_PAGE;
            this.botPage = Math.clamp(this.botPage + (id == NEXT_BOT_PAGE ? 1 : -1), 0, lastPage);
            return true;
        }
        int index = this.botPage * BOTS_PER_PAGE + id;
        return id >= 0 && id < BOTS_PER_PAGE && index < this.choices.size()
            && BotInventorySessions.select(viewer, this.choices.get(index), this.choices);
    }

    public static boolean canView(Player player, EntityPlayerMPFake bot) {
        return !player.isSpectator() && player.isAlive() && bot.isAlive();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.bot == null || (player == this.viewer && player.level() == this.openedLevel
            && this.bot.level() == this.botOpenedLevel && canView(player, this.bot));
    }

    @Override
    //#if MC>=260100
    public void clicked(int slot, int button, net.minecraft.world.inventory.ContainerInput input, Player player) {
        //#else
        //$$ public void clicked(int slot, int button, net.minecraft.world.inventory.ClickType input, Player player) {
        //#endif
        // The client keeps an empty mirror of the fake player slots, so it only predicts clicks on the
        // player inventory and lets the server own every fake player click.
        if (!this.stillValid(player) || (!this.botVisible && slot >= BOT_START)) {
            return;
        }
        if (this.bot == null && slot >= BOT_START) {
            return;
        }
        //#if MC>=260100
        if (input == net.minecraft.world.inventory.ContainerInput.CLONE
            || (input == net.minecraft.world.inventory.ContainerInput.QUICK_CRAFT && getQuickcraftType(button) == 2)) {
            //#else
            //$$ if (input == net.minecraft.world.inventory.ClickType.CLONE
            //$$     || (input == net.minecraft.world.inventory.ClickType.QUICK_CRAFT && getQuickcraftType(button) == 2)) {
            //#endif
            return;
        }
        super.clicked(slot, button, input, player);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return (this.botVisible || slot.index < BOT_START) && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (this.bot == null || index < 0 || index >= this.slots.size() || !this.stillValid(player)) {
            return ItemStack.EMPTY;
        }
        if (index < BOT_START && (!this.botVisible || index < PLAYER_CRAFTING_END)) {
            return super.quickMoveStack(player, index);
        }
        if (!this.botVisible) {
            return ItemStack.EMPTY;
        }
        Slot source = this.getSlot(index);
        if (!source.hasItem() || !source.mayPickup(player)) {
            return ItemStack.EMPTY;
        }

        boolean fromBot = index >= BOT_START;
        int targetStart = fromBot ? PLAYER_STORAGE_START : BOT_START;
        int targetEnd = fromBot ? PLAYER_STORAGE_END : BOT_STORAGE_END;
        ItemStack stack = source.getItem();
        ItemStack original = stack.copy();
        if (!this.moveItemStackTo(stack, targetStart, targetEnd, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            source.setByPlayer(ItemStack.EMPTY, original);
        } else {
            source.setChanged();
        }
        source.onTake(player, stack);
        return original;
    }

    @Override
    public void removed(Player player) {
        if (this.bot == null || !(player instanceof ServerPlayer serverPlayer)) {
            super.removed(player);
            return;
        }
        try {
            // Restore the cursor before logout saves the inventory or death drops it.
            ItemStack carried = this.getCarried();
            this.setCarried(ItemStack.EMPTY);
            //#if MC>=260300
            player.getInventory().placeItemBackInInventory(carried, net.minecraft.util.Prediction.SERVER_ONLY);
            //#else
            //$$ player.getInventory().placeItemBackInInventory(carried);
            //#endif
            super.removed(player);
            if (!this.stillValid(player) && !serverPlayer.hasDisconnected()) {
                new MessageComponent("rbfBotInventory.closed", this.bot.getName().getString())
                    .sendSystemMessage(serverPlayer);
            }
        } finally {
            BotInventorySessions.release(this.bot, this);
        }
    }
}
