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
package com.sguserver.remotebotfetch.storage.data;

import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.sguserver.remotebotfetch.storage.StorageSlotCounter;
import com.sguserver.remotebotfetch.storage.StorageSlotCounter.Result;

final class StorageItemStackProcessor {
    private final Map<Item, Storage.ItemCount> items;

    private StorageItemStackProcessor(Map<Item, Storage.ItemCount> items) {
        this.items = items;
    }

    static void processStack(ItemStack stack, Map<Item, Storage.ItemCount> items) {
        StorageItemStackProcessor processor = new StorageItemStackProcessor(items);
        processor.addSlot(stack);
    }

    private void addSlot(ItemStack itemStack) {
        Result result = StorageSlotCounter.count(itemStack);
        if (result == null) {
            return;
        }
        Storage.ItemCount itemCount = this.items.computeIfAbsent(result.item(), ignored -> new Storage.ItemCount());
        itemCount.add(result.count());
    }
}
