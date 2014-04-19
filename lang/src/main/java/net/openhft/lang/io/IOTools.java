/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.io;

import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.model.constraints.Nullable;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.logging.Logger;

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
                        Logger.getLogger(IOTools.class.getName()).info("... unable to delete " + file);
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
