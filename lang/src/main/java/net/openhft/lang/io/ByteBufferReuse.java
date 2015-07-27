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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;

interface ByteBufferReuse {
    ByteBufferReuse INSTANCE = Inner.getReuse();

    ByteBuffer reuse(long addr, int cap, Object att, ByteBuffer toReuse);

    class Inner extends Reuses implements Opcodes {
        private static ByteBufferReuse getReuse() {
            ClassWriter cw = new ClassWriter(0);
            MethodVisitor mv;

            final String reuseImplClassName = "net/openhft/lang/io/ByteBufferReuseImpl";
            cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, reuseImplClassName, null,
                    "sun/reflect/MagicAccessorImpl",
                    new String[]{"net/openhft/lang/io/ByteBufferReuse"});

            {
                mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                mv.visitInsn(RETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }

            String attachedBufferFieldName = getAttachedBufferFieldName();
            {
                mv = cw.visitMethod(ACC_PUBLIC, "reuse",
                        "(JILjava/lang/Object;Ljava/nio/ByteBuffer;)Ljava/nio/ByteBuffer;",
                        null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 5);
                String directByteBuffer = "java/nio/DirectByteBuffer";
                mv.visitTypeInsn(INSTANCEOF, directByteBuffer);
                Label l0 = new Label();
                mv.visitJumpInsn(IFEQ, l0);
                mv.visitVarInsn(ALOAD, 5);
                mv.visitTypeInsn(CHECKCAST, directByteBuffer);
                mv.visitVarInsn(ASTORE, 6);
                mv.visitVarInsn(ALOAD, 6);
                mv.visitFieldInsn(GETFIELD, directByteBuffer, attachedBufferFieldName, "Ljava/lang/Object;");
                String settableAtt = "net/openhft/lang/io/SettableAtt";
                mv.visitTypeInsn(INSTANCEOF, settableAtt);
                mv.visitJumpInsn(IFEQ, l0);
                mv.visitVarInsn(ALOAD, 6);
                mv.visitVarInsn(LLOAD, 1);
                mv.visitFieldInsn(PUTFIELD, directByteBuffer, "address", "J");
                mv.visitVarInsn(ALOAD, 6);
                mv.visitInsn(ICONST_M1);
                mv.visitFieldInsn(PUTFIELD, directByteBuffer, "mark", "I");
                mv.visitVarInsn(ALOAD, 6);
                mv.visitInsn(ICONST_0);
                mv.visitFieldInsn(PUTFIELD, directByteBuffer, "position", "I");
                mv.visitVarInsn(ALOAD, 6);
                mv.visitVarInsn(ILOAD, 3);
                mv.visitFieldInsn(PUTFIELD, directByteBuffer, "limit", "I");
                mv.visitVarInsn(ALOAD, 6);
                mv.visitVarInsn(ILOAD, 3);
                mv.visitFieldInsn(PUTFIELD, directByteBuffer, "capacity", "I");
                mv.visitVarInsn(ALOAD, 6);
                mv.visitFieldInsn(GETFIELD, directByteBuffer, attachedBufferFieldName, "Ljava/lang/Object;");
                mv.visitTypeInsn(CHECKCAST, settableAtt);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitFieldInsn(PUTFIELD, settableAtt, "att", "Ljava/lang/Object;");
                mv.visitVarInsn(ALOAD, 6);
                mv.visitInsn(ARETURN);
                mv.visitLabel(l0);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitTypeInsn(NEW, settableAtt);
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, settableAtt, "<init>", "()V", false);
                mv.visitVarInsn(ASTORE, 6);
                mv.visitVarInsn(ALOAD, 6);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitFieldInsn(PUTFIELD, settableAtt, "att", "Ljava/lang/Object;");
                mv.visitTypeInsn(NEW, directByteBuffer);
                mv.visitInsn(DUP);
                mv.visitVarInsn(LLOAD, 1);
                mv.visitVarInsn(ILOAD, 3);
                mv.visitVarInsn(ALOAD, 6);
                mv.visitMethodInsn(INVOKESPECIAL, directByteBuffer, "<init>",
                        "(JILjava/lang/Object;)V", false);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(6, 7);
                mv.visitEnd();
            }
            cw.visitEnd();

            final byte[] impl = cw.toByteArray();

            final Unsafe unsafe = NativeBytes.UNSAFE;
            Class clazz = AccessController.doPrivileged(new PrivilegedAction<Class>() {
                @Override
                public Class run() {
                    ClassLoader cl = MAGIC_CLASS_LOADER;
                    return unsafe.defineClass(reuseImplClassName, impl, 0, impl.length, cl, null);
                }
            });
            try {
                return (ByteBufferReuse) clazz.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        private static String getAttachedBufferFieldName() {
            try {
                Class<?> clazz = Class.forName("java.nio.DirectByteBuffer");
                String[] possibleFieldNames = new String[] { "att",
                        "viewedBuffer" };
                for (String possibleFieldName : possibleFieldNames) {
                    try {
                        clazz.getDeclaredField(possibleFieldName);
                        return possibleFieldName;
                    } catch (Exception e) {
                        continue;
                    }
                }

                throw new RuntimeException(
                        "Failed to find any of the possible field names on DirectByteBuffer: "
                                + Arrays.toString(possibleFieldNames));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
