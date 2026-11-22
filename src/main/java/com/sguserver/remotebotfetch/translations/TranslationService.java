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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.fabricmc.loader.api.FabricLoader;

import carpet.CarpetSettings;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sguserver.remotebotfetch.Mod;

public final class TranslationService {
    private static final String DEFAULT_LANG = "en_us";
    private static final String RESOURCE_DIR = "assets/remotebotfetchserver/lang";
    private static final Gson GSON = new Gson();

    private final Map<String, Map<String, String>> translations = new LinkedHashMap<>();

    public void init() {
        List<String> languages;
        try {
            languages = readJson(RESOURCE_DIR + "/meta/languages.json", new TypeToken<List<String>>() {}.getType());
        } catch (Exception e) {
            Mod.LOGGER.error("Failed to read language list", e);
            return;
        }
        for (String lang : languages) {
            this.translations.put(lang, new HashMap<>(loadTranslationFile(lang)));
        }
    }

    public Map<String, String> translations(String lang) {
        String normalizedLang = normalize(lang);
        Map<String, String> result = new HashMap<>(rawTranslations(DEFAULT_LANG));
        if (!DEFAULT_LANG.equals(normalizedLang)) {
            result.putAll(rawTranslations(normalizedLang));
        }
        return result;
    }

    public String getServerLanguage() {
        String language = CarpetSettings.language;
        return language == null || language.isBlank() || language.equalsIgnoreCase("none") ? DEFAULT_LANG
            : normalize(language);
    }

    public String tr(String key, Object... args) {
        return tr(getServerLanguage(), key, args);
    }

    public String tr(String lang, String key, Object... args) {
        String normalizedLang = normalize(lang);
        Map<String, String> primary = this.translations.get(normalizedLang);
        String template =
            primary != null && primary.containsKey(key) ? primary.get(key) : rawTranslations(DEFAULT_LANG).get(key);
        if (template == null) {
            return key;
        }
        try {
            return String.format(Locale.ROOT, template, args);
        } catch (IllegalFormatException e) {
            Mod.LOGGER.warn("Failed to format translation {} for {}", key, lang, e);
            return template;
        }
    }

    public String tr(Translations.FeedbackMessage message) {
        return tr("remotebotfetch.message." + message.key(), message.args());
    }

    private Map<String, String> loadTranslationFile(String lang) {
        try {
            return readJson(RESOURCE_DIR + "/" + lang + ".json", new TypeToken<Map<String, String>>() {}.getType());
        } catch (Exception e) {
            String message = "Failed to load translation of language " + lang;
            Mod.LOGGER.error(message, e);
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                throw new RuntimeException(message, e);
            }
            return Collections.emptyMap();
        }
    }

    private <T> T readJson(String path, Type type) {
        ClassLoader classLoader = TranslationService.class.getClassLoader();
        InputStream stream = classLoader != null ? classLoader.getResourceAsStream(path) : null;
        if (stream == null) {
            throw new IllegalStateException("Missing resource: " + path);
        }
        try (InputStream input = stream;
            InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> rawTranslations(String lang) {
        Map<String, String> values = this.translations.get(lang);
        return values != null ? values : Collections.emptyMap();
    }

    private static String normalize(String lang) {
        return lang.toLowerCase(Locale.ROOT);
    }
}
