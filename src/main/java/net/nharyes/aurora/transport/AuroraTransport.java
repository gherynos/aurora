/*
 * Copyright (C) 2020-2024  Luca Zanconato (<github.com/gherynos>)
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

package net.nharyes.aurora.transport;

import net.nharyes.aurora.AuroraException;
import net.nharyes.aurora.msg.key.OutKeyMessage;
import net.nharyes.aurora.msg.OutMessage;

public interface AuroraTransport {

    void setIncomingMessageHandler(IncomingMessageHandler messageHandler);

    void sendKeyMessage(OutKeyMessage key) throws AuroraException;

    void sendMessage(OutMessage<?> message) throws AuroraException;

    void checkForMessages() throws AuroraException;

    boolean requiresArmoredMessages();
}
