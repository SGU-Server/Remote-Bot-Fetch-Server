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
import java.util.List;
import java.util.Map;

import net.fabricmc.loader.api.FabricLoader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sguserver.remotebotfetch.Mod;

final class TranslationLoader {
    private static final String RESOURCE_DIR = "assets/remotebotfetchserver/lang";
    private static final Gson GSON = new Gson();

    private TranslationLoader() {}

    static void load(TranslationStorage storage) {
        List<String> languages;
        try {
            languages = readJson(RESOURCE_DIR + "/meta/languages.json", new TypeToken<List<String>>() {}.getType());
        } catch (Exception e) {
            Mod.LOGGER.error("Failed to read language list", e);
            return;
        }
        for (String lang : languages) {
            storage.put(lang, loadTranslationFile(lang));
        }
    }

    private static Map<String, String> loadTranslationFile(String lang) {
        try {
            return readJson(RESOURCE_DIR + "/" + lang + ".json", new TypeToken<Map<String, String>>() {}.getType());
        } catch (Exception e) {
            String msg = "Failed to load translation of language " + lang;
            Mod.LOGGER.error(msg, e);
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                throw new RuntimeException(msg, e);
            }
            return Collections.emptyMap();
        }
    }

    private static <T> T readJson(String path, Type type) {
        ClassLoader classLoader = TranslationLoader.class.getClassLoader();
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
}
