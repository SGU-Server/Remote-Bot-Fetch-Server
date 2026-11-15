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
package com.sguserver.remotebotfetch.translations;

import java.util.Map;

public final class Translations {
    private static final TranslationService SERVICE = new TranslationService();

    private Translations() {}

    public static void init() {
        SERVICE.init();
    }

    public static Map<String, String> translations(String lang) {
        return SERVICE.translations(lang);
    }

    public static String tr(String key, Object... args) {
        return SERVICE.tr(key, args);
    }

    public static String tr(FeedbackMessage message) {
        return SERVICE.tr(message);
    }

    public static String messageTr(String key, Object... args) {
        return tr("remotebotfetch.message." + key, args);
    }

    public record FeedbackMessage(String key, Object... args) {
    }
}
