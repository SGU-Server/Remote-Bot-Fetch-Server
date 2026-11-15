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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

final class TranslationStorage {
    static final String DEFAULT_LANG = "en_us";

    private final Map<String, Map<String, String>> translations = new LinkedHashMap<>();

    void put(String lang, Map<String, String> values) {
        this.translations.put(lang, new HashMap<>(values));
    }

    Map<String, String> mergedTranslations(String lang) {
        Map<String, String> result = new HashMap<>(rawTranslations(DEFAULT_LANG));
        if (!DEFAULT_LANG.equals(lang)) {
            result.putAll(rawTranslations(lang));
        }
        return result;
    }

    String lookup(String lang, String key) {
        Map<String, String> primary = this.translations.get(lang);
        if (primary != null && primary.containsKey(key)) {
            return primary.get(key);
        }
        Map<String, String> fallback = this.translations.get(DEFAULT_LANG);
        return fallback != null ? fallback.get(key) : null;
    }

    private Map<String, String> rawTranslations(String lang) {
        Map<String, String> values = this.translations.get(lang);
        return values != null ? values : Collections.emptyMap();
    }
}
