/*
 * Copyright (C) 2022  Luca Zanconato (<github.com/gherynos>)
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
 */

package co.naes.aurora;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class LogUtils {

    private final Logger logger;

    public static LogUtils getLogUtils(String name) {

        return new LogUtils(name);
    }

    private LogUtils(String name) {

        logger = Logger.getLogger(name);
    }

    public void logInfo(String message) {

        if (logger.isLoggable(Level.INFO)) {

            logger.info(message);
        }
    }

    public void logInfo(String format, Object... args) {

        if (logger.isLoggable(Level.INFO)) {

            logger.info(String.format(format, args));
        }
    }

    public void logFine(String message) {

        if (logger.isLoggable(Level.FINE)) {

            logger.fine(message);
        }
    }

    public void logFine(String format, Object... args) {

        if (logger.isLoggable(Level.FINE)) {

            logger.fine(String.format(format, args));
        }
    }

    public void logWarning(String message) {

        if (logger.isLoggable(Level.WARNING)) {

            logger.warning(message);
        }
    }

    public void logWarning(String format, Object... args) {

        if (logger.isLoggable(Level.WARNING)) {

            logger.warning(String.format(format, args));
        }
    }

    public void logError(Throwable ex) {

        Logger logger = Logger.getLogger(LogUtils.class.getName());

        if (logger.isLoggable(Level.SEVERE)) {

            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
