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
package com.sguserver.remotebotfetch.mixin.util.carpet;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.component.ResolvableProfile;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import carpet.patches.EntityPlayerMPFake;

import com.sguserver.remotebotfetch.bot.FakePlayerSpawner;

// <=1.21.8:
// versions/1.21.8/src/main/java/com/sguserver/remotebotfetch/mixin/util/carpet/EntityPlayerMPFakeOfflineProfileMixin.java
@Mixin(EntityPlayerMPFake.class)
public class EntityPlayerMPFakeOfflineProfileMixin {
    @WrapOperation(method = "createFake", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/players/OldUsersConverter;convertMobOwnerIfNecessary(Lnet/minecraft/server/MinecraftServer;Ljava/lang/String;)Ljava/util/UUID;"))
    private static UUID rbf$skipOnlineLookupForOfflineProfile(MinecraftServer server, String username,
        Operation<UUID> original) {
        if (!FakePlayerSpawner.shouldForceOfflineProfile(username)) {
            return original.call(server, username);
        }
        return UUIDUtil.createOfflinePlayerUUID(username);
    }

    @WrapOperation(method = "fetchGameProfile", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/component/ResolvableProfile;resolveProfile(Lnet/minecraft/server/players/ProfileResolver;)Ljava/util/concurrent/CompletableFuture;"))
    private static CompletableFuture<GameProfile> rbf$skipOnlineSkinLookupForOfflineProfile(ResolvableProfile instance,
        net.minecraft.server.players.ProfileResolver resolver, Operation<CompletableFuture<GameProfile>> original) {
        String username = instance.partialProfile().name();
        if (username == null || !FakePlayerSpawner.shouldForceOfflineProfile(username)) {
            return original.call(instance, resolver);
        }
        return CompletableFuture.completedFuture(instance.partialProfile());
    }
}
