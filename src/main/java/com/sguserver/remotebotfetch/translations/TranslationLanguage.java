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

import java.util.Locale;

import carpet.CarpetSettings;

final class TranslationLanguage {
    private TranslationLanguage() {}

    static String getServerLanguage() {
        String language = CarpetSettings.language;
        if (language == null || language.isBlank() || language.equalsIgnoreCase("none")) {
            return TranslationStorage.DEFAULT_LANG;
        }
        return normalize(language);
    }

    static String normalize(String lang) {
        return lang.toLowerCase(Locale.ROOT);
    }
}
