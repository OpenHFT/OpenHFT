#JavaLang
This module provides marshalling, de-marshalling and handling of thread safe off heap memory through ByteBuffers.

This module is available on maven central as

    <dependency>
        <groupId>net.openhft</groupId>
        <artifactId>lang</artifactId>
        <version>6.3.13</version>
    </dependency>

The version 6.x signifies that it is build for Java 6+. (It requires Java 6 update 18 or later to build)

##  JavaDoc
Check out our documentation at [JavaDoc] (http://openhft.github.io/Java-Lang/apidocs/)

## Working with off heap objects.

Java-Lang 6.1 adds support for basic off heap data structures.  More collections types and more complex data types will be added in future versions.

    public interface DataType {
         // add getters and setters here
    }
    
    // can create an array of any size (provided you have the memory) off heap.
    HugeArray<DataType> array = HugeCollections.newArray(DataType.class, 10*1000*1000*1000L);
    DataType dt = array.get(1111111111);
    
    // set data on dt
    array.recycle(dt); // recycle the reference (or discard it)
    
    // create a ring writeBuffer
    HugeQueue<DataType> queue = HugeCollections.newQueue(DataType.class, 10*1000*1000L);
    // give me a reference to an object to populate
    DataType dt2 = queue.offer();
    // set the values od dt2
    queue.recycle(dt2);
    
    DataType dt3 = queue.take();
    // get values
    queue.recycle(dt3);
    
This is designed to be largely GC-less and you can queue millions of entries with 32 MB heap and not trigger GCs.
    
## Working with buffers
To work with buffers there is a several options:
* _ByteBufferBytes_ which wraps [java.nio.ByteBuffer](http://docs.oracle.com/javase/7/docs/api/java/nio/ByteBuffer.html)
* _DirectBytes_ which is slices/records of [DirectStore](https://github.com/OpenHFT/Java-Lang/blob/master/lang/src/main/java/net/openhft/lang/io/DirectStore.java) - own implementation for offheap storage

Both classes provide functionality:
* write\read operations for primitives (writeLong(long n), readLong() etc.)
* locking in native memory, so you can add thread safe constructs to your native record.
* CAS operations for int and long _boolean compareAndSwapInt(long offset, int expected, int x)_, _boolean compareAndSwapLong(long offset, long expected, long x)_
* addAndGetInt and getAndAddInt operations

####Example
    ByteBuffer byteBuffer = ByteBuffer.allocate(SIZE);
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

