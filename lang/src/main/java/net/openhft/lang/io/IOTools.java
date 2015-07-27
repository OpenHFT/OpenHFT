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

package net.openhft.lang.io;

import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.model.constraints.Nullable;
import org.slf4j.LoggerFactory;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author peter.lawrey
 */
public enum IOTools {
    ;
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static void close(@Nullable Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    public static void close(@NotNull Iterable<? extends Closeable> closeables) {
        for (Closeable closeable : closeables) {
            close(closeable);
        }
    }

    public static int stopBitLength(long l) {
        if (l < 0) return stopBitLength(~l) + 1;
        int count = 1;
        while (l >= 128) {
            l >>>= 7;
            count++;
        }
        return count;
    }

    public static void deleteDir(String dirPath) {
        deleteDir(new File(dirPath));
    }

    private static void deleteDir(File dir) {
        // delete one level.
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null)
                for (File file : files)
                    if (file.isDirectory()) {
                        deleteDir(file);

                    } else if (!file.delete()) {
                        LoggerFactory.getLogger(IOTools.class).info("... unable to delete {}", file);
                    }
        }
        dir.delete();
    }

    public static void clean(ByteBuffer bb) {
        if (bb instanceof DirectBuffer) {
            Cleaner cl = ((DirectBuffer) bb).cleaner();
            if (cl != null)
                cl.clean();
        }
    }
}
