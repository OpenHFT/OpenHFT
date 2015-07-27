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

package net.openhft.lang.thread;

import net.openhft.lang.model.constraints.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: peter.lawrey Date: 18/08/13 Time: 11:37
 */
public class NamedThreadFactory implements ThreadFactory {
    private final AtomicInteger id = new AtomicInteger();
    private final String name;
    private final Boolean daemon;

    public NamedThreadFactory(@NotNull String name) {
        this(name, null);
    }

    public NamedThreadFactory(@NotNull String name, Boolean daemon) {
        this.name = name;
        this.daemon = daemon;
    }

    @NotNull
    @Override
    public Thread newThread(@NotNull Runnable r) {
        int id = this.id.getAndIncrement();
        String nameN = id == 0 ? name : (name + '-' + id);
        Thread t = new Thread(r, nameN);
        if (daemon != null)
            t.setDaemon(daemon);
        return t;
    }
}
