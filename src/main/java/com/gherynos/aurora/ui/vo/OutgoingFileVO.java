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

package com.gherynos.aurora.ui.vo;

import com.gherynos.aurora.Identifier;

public class OutgoingFileVO {

    private final String fileId;

    private final Identifier recipient;

    private final int sent;

    private final int toSend;

    private final int total;

    public OutgoingFileVO(String fileId, Identifier recipient, int sent, int toSend, int total) {

        this.fileId = fileId;
        this.recipient = recipient;
        this.sent = sent;
        this.toSend = toSend;
        this.total = total;
    }

    public Object[] asRow() {

        return new Object[]{fileId, recipient, total - (sent + toSend), toSend, total};
    }
}
