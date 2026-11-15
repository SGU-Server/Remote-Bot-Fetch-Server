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
package com.sguserver.remotebotfetch.lib;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public final class ChatEventCompat {
    private ChatEventCompat() {}

    public static ClickEvent runCommand(String command) {
        //#if MC>=12105
        return new ClickEvent.RunCommand(command);
        //#else
        //$$ return new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        //#endif
    }

    public static ClickEvent suggestCommand(String command) {
        //#if MC>=12105
        return new ClickEvent.SuggestCommand(command);
        //#else
        //$$ return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
        //#endif
    }

    public static HoverEvent showText(Component text) {
        //#if MC>=12105
        return new HoverEvent.ShowText(text);
        //#else
        //$$ return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
        //#endif
    }
}
