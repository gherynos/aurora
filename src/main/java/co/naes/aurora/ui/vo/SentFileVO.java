/*
 * Copyright (C) 2020  Luca Zanconato (<luca.zanconato@naes.co>)
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

package co.naes.aurora.ui.vo;

import co.naes.aurora.Identifier;

import java.sql.Timestamp;

public class SentFileVO {

    private final String fileId;

    private final String path;

    private final Identifier recipient;

    private final Timestamp completed;

    public SentFileVO(String fileId, String path, Identifier recipient, Timestamp completed) {

        this.fileId = fileId;
        this.path = path;
        this.recipient = recipient;
        this.completed = completed;
    }

    public Object[] asRow() {

        return new Object[]{completed, recipient, fileId, path};
    }
}
