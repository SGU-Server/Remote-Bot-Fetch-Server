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

import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Map;

import com.sguserver.remotebotfetch.Mod;

public final class TranslationService {
    private final TranslationStorage storage = new TranslationStorage();

    public void init() {
        TranslationLoader.load(this.storage);
    }

    public Map<String, String> translations(String lang) {
        return this.storage.mergedTranslations(TranslationLanguage.normalize(lang));
    }

    public String getServerLanguage() {
        return TranslationLanguage.getServerLanguage();
    }

    public String tr(String key, Object... args) {
        return tr(getServerLanguage(), key, args);
    }

    public String tr(String lang, String key, Object... args) {
        String template = this.storage.lookup(TranslationLanguage.normalize(lang), key);
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
}
