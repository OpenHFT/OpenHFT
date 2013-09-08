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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter.lawrey
 */
public interface ByteStringParser extends BytesCommon {
    void parseUTF(@NotNull Appendable builder, @NotNull StopCharTester tester);

    @NotNull
    String parseUTF(@NotNull StopCharTester tester);

    @Nullable
    <E> E parseEnum(@NotNull Class<E> eClass, @NotNull StopCharTester tester);

    @NotNull
    MutableDecimal parseDecimal(@NotNull MutableDecimal decimal);

    long parseLong();

    double parseDouble();

    /**
     * Make sure we just read a stop character
     *
     * @param tester to stop at
     * @return true if we stopped at a stop character, false if we ran out of data.
     */
    boolean stepBackAndSkipTo(@NotNull StopCharTester tester);

    /**
     * Wind from this positionAddr to the end of the field
     *
     * @param tester to stop at
     * @return true if we stopped at a stop character, false if we ran out of data.
     */
    boolean skipTo(@NotNull StopCharTester tester);
}
