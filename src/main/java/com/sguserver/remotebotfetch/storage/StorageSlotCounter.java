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
package com.sguserver.remotebotfetch.storage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import com.sguserver.remotebotfetch.lib.Utils;

import org.jspecify.annotations.Nullable;

public final class StorageSlotCounter {
    private StorageSlotCounter() {}

    public static @Nullable Result count(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        if (!Utils.isShulkerBox(stack)) {
            return new Result(stack.getItem(), stack.getCount(), true);
        }

        ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        if (container == null) {
            return new Result(Items.SHULKER_BOX, stack.getCount(), false);
        }

        Item mergedItem = null;
        int mergedCount = 0;
        for (ItemStack inner : Utils.nonItemCopyList(container)) {
            Result innerResult = count(inner);
            if (innerResult == null) {
                continue;
            }
            if (mergedItem != null && mergedItem != innerResult.item()) {
                return null;
            }
            mergedItem = innerResult.item();
            mergedCount += innerResult.count();
        }

        if (mergedItem == null) {
            return new Result(Items.SHULKER_BOX, stack.getCount(), false);
        }
        int count = mergedCount + (mergedItem == Items.SHULKER_BOX ? 1 : 0);
        return new Result(mergedItem, count * stack.getCount(), false);
    }

    public static Item normalize(Item item) {
        return Utils.isShulkerBox(item) ? Items.SHULKER_BOX : item;
    }

    public record Result(Item item, int count, boolean noShulkerBox) {
        public boolean matches(Item target) {
            return this.item == target && this.count > 0;
        }
    }
}
