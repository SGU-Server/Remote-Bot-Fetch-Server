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
package com.sguserver.remotebotfetch.rule;

import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;
import carpet.api.settings.Validators;

public final class Settings {

    public static final String RBF = "RBF";

    @Rule(categories = {RBF, RuleCategory.COMMAND, RuleCategory.FEATURE, RuleCategory.SURVIVAL, RuleCategory.CREATIVE},
        validators = {Validators.CommandLevel.class})
    public static String commandGetItem = "false";

    @Rule(categories = {RBF, RuleCategory.COMMAND, RuleCategory.FEATURE, RuleCategory.SURVIVAL, RuleCategory.CREATIVE},
        validators = {Validators.CommandLevel.class})
    public static String commandGetStorageData = "false";

    @Rule(categories = {RBF, RuleCategory.COMMAND, RuleCategory.FEATURE, RuleCategory.SURVIVAL, RuleCategory.CREATIVE},
        validators = {Validators.CommandLevel.class})
    public static String commandCleanGetItemBot = "false";

    @Rule(categories = {RBF, RuleCategory.FEATURE, RuleCategory.SURVIVAL, RuleCategory.CREATIVE})
    public static String getItemBotPrefix = "bot_getitem_";

    @Rule(categories = {RBF, RuleCategory.FEATURE, RuleCategory.SURVIVAL, RuleCategory.CREATIVE},
        validators = {Validators.NonNegativeNumber.class})
    public static int getItemDelayMs = 0;

    @Rule(categories = {RBF, RuleCategory.FEATURE, RuleCategory.SURVIVAL, RuleCategory.CREATIVE},
        validators = {Validators.NonNegativeNumber.class})
    public static int getItemCooldownSeconds = 0;

    @Rule(categories = {RBF, RuleCategory.FEATURE, RuleCategory.SURVIVAL, RuleCategory.CREATIVE},
        validators = {Validators.NonNegativeNumber.class})
    public static int getStorageDataCooldownSeconds = 0;

    @Rule(categories = {RBF, RuleCategory.FEATURE, RuleCategory.SURVIVAL, RuleCategory.CREATIVE},
        validators = {Validators.NonNegativeNumber.class})
    public static int getItemMaxCount = 0;

    @Rule(categories = {RBF, RuleCategory.FEATURE, RuleCategory.SURVIVAL, RuleCategory.CREATIVE},
        validators = {Validators.NonNegativeNumber.class})
    public static int getItemMaxBots = 0;
}
