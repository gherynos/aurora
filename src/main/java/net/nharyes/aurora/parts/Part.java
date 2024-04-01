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

package net.nharyes.aurora.parts;

import java.security.InvalidParameterException;

@SuppressWarnings("PMD.ShortClassName")
public class Part {

    private final PartId id;

    private final int total;

    private final long totalSize;

    private final byte[] data;

    public Part(PartId id, int total, long totalSize, byte[] data) {

        if (data.length > Splitter.getPartSize(totalSize, total)) {

            throw new InvalidParameterException("Part content bigger than expected");
        }

        this.id = id;
        this.total = total;
        this.totalSize = totalSize;
        this.data = data.clone();
    }

    public PartId getId() {

        return id;
    }

    public int getTotal() {

        return total;
    }

    public long getTotalSize() {

        return totalSize;
    }

    public byte[] getData() {

        return data.clone();
    }

    public int getPartSize() {

        return Splitter.getPartSize(totalSize, total);
    }
}
