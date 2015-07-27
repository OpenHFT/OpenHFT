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

/**
 * @author peter.lawrey
 */
public enum StopCharTesters implements StopCharTester {
    COMMA_STOP {
        @Override
        public boolean isStopChar(int ch) {
            return ch < ' ' || ch == ',';
        }
    }, CONTROL_STOP {
        @Override
        public boolean isStopChar(int ch) {
            return ch < ' ';
        }
    },
    SPACE_STOP {
        @Override
        public boolean isStopChar(int ch) {
            return Character.isWhitespace(ch) || ch == 0;
        }
    },
    XML_TEXT {
        @Override
        public boolean isStopChar(int ch) {
            return ch == '"' || ch == '<' || ch == '>' || ch == 0;
        }
    },
    FIX_TEXT {
        @Override
        public boolean isStopChar(int ch) {
            return ch <= 1;
        }
    },
    ALL {
        @Override
        public boolean isStopChar(int ch) {
            return ch < 0;
        }
    };

    @NotNull
    public static StopCharTester forChars(@NotNull CharSequence sequence) {
        if (sequence.length() == 1)
            return forChar(sequence.charAt(0));
        return new CSCSTester(sequence);
    }

    @NotNull
    private static StopCharTester forChar(char ch) {
        return new CharCSTester(ch);
    }

    static class CSCSTester implements StopCharTester {
        @NotNull
        private final String seperators;

        public CSCSTester(@NotNull CharSequence cs) {
            seperators = cs.toString();
        }

        @Override
        public boolean isStopChar(int ch) {
            return seperators.indexOf(ch) >= 0;
        }
    }

    static class CharCSTester implements StopCharTester {
        private final char ch;

        public CharCSTester(char ch) {
            this.ch = ch;
        }

        @Override
        public boolean isStopChar(int ch) {
            return this.ch == ch;
        }
    }
}
