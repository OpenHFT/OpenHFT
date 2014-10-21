/*
 * Copyright 2014 Higher Frequency Trading
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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;

import java.nio.CharBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

interface CharBufferReuse {
    static final CharBufferReuse INSTANCE = Inner.getReuse();

    CharBuffer reuse(CharSequence cs, CharBuffer toReuse);

    static class Inner extends Reuses implements Opcodes {
        private static CharBufferReuse getReuse() {
            ClassWriter cw = new ClassWriter(0);
            MethodVisitor mv;

            final String reuseImplClassName = "net/openhft/lang/io/CharBufferReuseImpl";
            cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, reuseImplClassName, null,
                    "sun/reflect/MagicAccessorImpl",
                    new String[] {"net/openhft/lang/io/CharBufferReuse"});

            {
                mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                mv.visitInsn(RETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            {
                mv = cw.visitMethod(ACC_PUBLIC, "reuse",
                        "(Ljava/lang/CharSequence;Ljava/nio/CharBuffer;)Ljava/nio/CharBuffer;",
                        null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 2);
                String stringCharBuffer = "java/nio/StringCharBuffer";
                mv.visitTypeInsn(INSTANCEOF, stringCharBuffer);
                Label l0 = new Label();
                mv.visitJumpInsn(IFEQ, l0);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitTypeInsn(CHECKCAST, stringCharBuffer);
                mv.visitVarInsn(ASTORE, 3);
                mv.visitVarInsn(ALOAD, 3);
                mv.visitInsn(ICONST_M1);
                mv.visitFieldInsn(PUTFIELD, stringCharBuffer, "mark", "I");
                mv.visitVarInsn(ALOAD, 3);
                mv.visitInsn(ICONST_0);
                mv.visitFieldInsn(PUTFIELD, stringCharBuffer, "position", "I");
                mv.visitVarInsn(ALOAD, 1);
                String charSequence = "java/lang/CharSequence";
                mv.visitMethodInsn(INVOKEINTERFACE, charSequence, "length", "()I", true);
                mv.visitVarInsn(ISTORE, 4);
                mv.visitVarInsn(ALOAD, 3);
                mv.visitVarInsn(ILOAD, 4);
                mv.visitFieldInsn(PUTFIELD, stringCharBuffer, "limit", "I");
                mv.visitVarInsn(ALOAD, 3);
                mv.visitVarInsn(ILOAD, 4);
                mv.visitFieldInsn(PUTFIELD, stringCharBuffer, "capacity", "I");
                mv.visitVarInsn(ALOAD, 3);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, stringCharBuffer, "str", "Ljava/lang/CharSequence;");
                mv.visitVarInsn(ALOAD, 3);
                mv.visitInsn(ARETURN);
                mv.visitLabel(l0);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitTypeInsn(NEW, stringCharBuffer);
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitInsn(ICONST_0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitMethodInsn(INVOKEINTERFACE, charSequence, "length", "()I", true);
                mv.visitMethodInsn(INVOKESPECIAL, stringCharBuffer, "<init>",
                        "(Ljava/lang/CharSequence;II)V", false);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(5, 5);
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
                return (CharBufferReuse) clazz.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
