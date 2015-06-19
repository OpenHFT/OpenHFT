/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.lang.model;

import net.openhft.lang.io.serialization.BytesMarshallable;

/**
 * User: peter.lawrey
 * Date: 06/10/13
 * Time: 16:59
 */
public interface MinimalInterface extends BytesMarshallable, Copyable<MinimalInterface>, Byteable {
    void flag(boolean flag);

    boolean flag();

    void byte$(byte b);

    byte byte$();

    void short$(short s);

    short short$();

    void char$(char ch);

    char char$();

    void int$(int i);

    int int$();

    void float$(float f);

    float float$();

    void long$(long l);

    long long$();

    void double$(double d);

    double double$();
}
