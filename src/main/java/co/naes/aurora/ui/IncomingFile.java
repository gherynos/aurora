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

package co.naes.aurora.ui;

public class IncomingFile {

    private final String fileId;

    private final String sender;

    private final int missing;

    private final int total;

    public IncomingFile(String fileId, String sender, int missing, int total) {

        this.fileId = fileId;
        this.sender = sender;
        this.missing = missing;
        this.total = total;
    }

    public Object[] asRow() {

        return new Object[]{sender, fileId, total - missing, total};
    }
}
