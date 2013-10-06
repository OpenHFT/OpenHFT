#JavaLang
This module provides marshalling, de-marshalling and handling of thread safe off heap memory through ByteBuffers.
## Working with buffers
To work with buffers there is a several options:
* _ByteBufferBytes_ wich wraps [java.nio.ByteBuffer](http://docs.oracle.com/javase/7/docs/api/java/nio/ByteBuffer.html)
* _DirectBytes_ that uses [DirectStore](https://github.com/OpenHFT/Java-Lang/blob/master/lang/src/main/java/net/openhft/lang/io/DirectStore.java) - own implementation for offheap storage

Both classes provide functionality:
* write\read operations for primitives (writeLong(long n), readLong() etc.)
* CAS operations for int and long _boolean compareAndSetInt(long offset, int expected, int x)_, _boolean compareAndSetLong(long offset, long expected, long x)_

####Example
    ByteBuffer byteBuffer = ByteBuffer.allocate(SIZE).order(ByteOrder.nativeOrder());
    ByteBufferBytes bytes = new ByteBufferBytes(byteBuffer);
    for (long i = 0; i < bytes.capacity(); i++)
        bytes.writeLong(i);
    for (long i = bytes.capacity()-8; i >= 0; i -= 8) {
        int j = bytes.readLong(i);
        assert i ==  j;
    }

#Building for eclipse

Download Java-Lang zip from git https://github.com/OpenHFT/Java-Lang/archive/master.zip

Unzip master.zip, Java-Lang-master folder will be extracted from zip.

    cd Java-Lang-master
    mvn eclipse:eclipse

Now you have an eclipse project, import project into Eclipse

If your Eclipse configuration is not UTF-8, after importing the project you may see some errors and strange characters in some .java files. To get rid of this problem change character enconding to UTF-8: project->properties->resource->text file encoding->utf8