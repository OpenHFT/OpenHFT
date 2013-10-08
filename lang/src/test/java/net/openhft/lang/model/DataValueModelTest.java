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

package net.openhft.lang.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: plawrey
 * Date: 06/10/13
 * Time: 18:12
 */
public class DataValueModelTest {
    @Test
    public void testAcquire() {
        DataValueModel<MinimalInterface> midvm = DataValueModels.acquireModel(MinimalInterface.class);
        assertEquals("{byte$=FieldModel{name='byte$', getter=public abstract byte net.openhft.lang.model.MinimalInterface.byte$(), setter=public abstract void net.openhft.lang.model.MinimalInterface.byte$(byte)}\n" +
                " char$=FieldModel{name='char$', getter=public abstract char net.openhft.lang.model.MinimalInterface.char$(), setter=public abstract void net.openhft.lang.model.MinimalInterface.char$(char)}\n" +
                " double$=FieldModel{name='double$', getter=public abstract double net.openhft.lang.model.MinimalInterface.double$(), setter=public abstract void net.openhft.lang.model.MinimalInterface.double$(double)}\n" +
                " flag=FieldModel{name='flag', getter=public abstract boolean net.openhft.lang.model.MinimalInterface.flag(), setter=public abstract void net.openhft.lang.model.MinimalInterface.flag(boolean)}\n" +
                " float$=FieldModel{name='float$', getter=public abstract float net.openhft.lang.model.MinimalInterface.float$(), setter=public abstract void net.openhft.lang.model.MinimalInterface.float$(float)}\n" +
                " int$=FieldModel{name='int$', getter=public abstract int net.openhft.lang.model.MinimalInterface.int$(), setter=public abstract void net.openhft.lang.model.MinimalInterface.int$(int)}\n" +
                " long$=FieldModel{name='long$', getter=public abstract long net.openhft.lang.model.MinimalInterface.long$(), setter=public abstract void net.openhft.lang.model.MinimalInterface.long$(long)}\n" +
                " short$=FieldModel{name='short$', getter=public abstract short net.openhft.lang.model.MinimalInterface.short$(), setter=public abstract void net.openhft.lang.model.MinimalInterface.short$(short)}}"
                , midvm.fieldMap().toString().replaceAll("},", "}\n"));
        DataValueModel<JavaBeanInterface> jbdvm = DataValueModels.acquireModel(JavaBeanInterface.class);
        assertEquals("{byte=FieldModel{name='byte', getter=public abstract byte net.openhft.lang.model.JavaBeanInterface.getByte(), setter=public abstract void net.openhft.lang.model.JavaBeanInterface.setByte(byte)}\n" +
                " char=FieldModel{name='char', getter=public abstract char net.openhft.lang.model.JavaBeanInterface.getChar(), setter=public abstract void net.openhft.lang.model.JavaBeanInterface.setChar(char)}\n" +
                " double=FieldModel{name='double', getter=public abstract double net.openhft.lang.model.JavaBeanInterface.getDouble(), setter=public abstract void net.openhft.lang.model.JavaBeanInterface.setDouble(double)}\n" +
                " flag=FieldModel{name='flag', getter=public abstract boolean net.openhft.lang.model.JavaBeanInterface.getFlag(), setter=public abstract void net.openhft.lang.model.JavaBeanInterface.setFlag(boolean)}\n" +
                " float=FieldModel{name='float', getter=public abstract float net.openhft.lang.model.JavaBeanInterface.getFloat(), setter=public abstract void net.openhft.lang.model.JavaBeanInterface.setFloat(float)}\n" +
                " int=FieldModel{name='int', getter=public abstract int net.openhft.lang.model.JavaBeanInterface.getInt(), setter=public abstract void net.openhft.lang.model.JavaBeanInterface.setInt(int)}\n" +
                " long=FieldModel{name='long', getter=public abstract long net.openhft.lang.model.JavaBeanInterface.getLong(), setter=public abstract void net.openhft.lang.model.JavaBeanInterface.setLong(long)}\n" +
                " short=FieldModel{name='short', getter=public abstract short net.openhft.lang.model.JavaBeanInterface.getShort(), setter=public abstract void net.openhft.lang.model.JavaBeanInterface.setShort(short)}\n" +
                " string=FieldModel{name='string', getter=public abstract java.lang.String net.openhft.lang.model.JavaBeanInterface.getString(), setter=public abstract void net.openhft.lang.model.JavaBeanInterface.setString(java.lang.String), size= @net.openhft.lang.constraints.Size(value=8)}}"
                , jbdvm.fieldMap().toString().replaceAll("},", "}\n"));
    }
}
