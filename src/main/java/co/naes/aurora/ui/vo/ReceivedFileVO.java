/*
 * Copyright (C) 2020  Luca Zanconato (<github.com/gherynos>)
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

public class ReceivedFileVO {

    private final String fileId;

    private final String path;

    private final Identifier sender;

    private final Timestamp completed;

    public ReceivedFileVO(String fileId, String path, Identifier sender, Timestamp completed) {

        this.fileId = fileId;
        this.path = path;
        this.sender = sender;
        this.completed = completed;
    }

    public Object[] asRow() {

        return new Object[]{completed, sender, fileId, path};
    }
}
