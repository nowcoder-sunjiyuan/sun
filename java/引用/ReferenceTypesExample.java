
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ReferenceTypesExample {

    public static void main(String[] args) throws InterruptedException {
        strongReferenceExample();
        softReferenceExample();
        weakReferenceExample();
        phantomReferenceExample();
    }

    /**
     * 1. 强引用 (Strong Reference)
     * 这是 Java 中最常见的引用类型。如果一个对象具有强引用，垃圾回收器绝不会回收它，即使系统内存不足，也会抛出 OutOfMemoryError。
     * 只有当该对象的所有强引用都断开时，它才可能被回收。
     */
    public static void strongReferenceExample() {
        System.out.println("--- 强引用示例 ---");
        // new Object() 创建了一个 Object 实例，myObject 是对这个实例的强引用。
        Object myObject = new Object();
        
        System.out.println("强引用对象: " + myObject);

        // 手动断开强引用
        myObject = null;
        
        // 当 myObject 设置为 null 后，就没有强引用指向最初创建的 Object 实例了。
        // 在下一次垃圾回收发生时，这个实例就有资格被回收了。
        System.gc(); // 建议 JVM 进行垃圾回收 (不保证立即执行)
        System.out.println("强引用已断开，对象等待被回收。\n");
    }

    /**
     * 2. 软引用 (Soft Reference)
     * 软引用用来描述一些还有用但并非必需的对象。
     * 只有在内存不足（即将发生 OutOfMemoryError）时，垃圾回收器才会回收只被软引用的对象。
     * 非常适合用来实现内存敏感的高速缓存。
     */
    public static void softReferenceExample() {
        System.out.println("--- 软引用示例 ---");
        // 创建一个对象，并用强引用指向它
        Object strongObj = new Object();
        
        // 创建一个软引用，指向这个对象
        SoftReference<Object> softRef = new SoftReference<>(strongObj);

        System.out.println("软引用 get() (强引用存在时): " + softRef.get());

        // 断开强引用，现在只有软引用指向这个对象
        strongObj = null; 
        System.out.println("强引用已断开...");

        // 在内存充足的情况下，垃圾回收不一定会回收软引用的对象
        System.gc();
        System.out.println("内存充足时 GC 后，软引用 get(): " + softRef.get());

        // 模拟内存不足的情况
        try {
            // 请求大量内存，来迫使 JVM 回收软引用对象
            List<byte[]> memoryHog = new ArrayList<>();
            // 这个大小取决于你的堆内存设置，可能需要调整
            while (softRef.get() != null) {
                memoryHog.add(new byte[1024 * 1024]); // 每次分配 1MB
            }
        } catch (OutOfMemoryError e) {
            System.out.println("内存溢出，软引用对象应该已经被回收了。");
        }

        System.out.println("内存不足时 GC 后，软引用 get(): " + softRef.get());
        System.out.println("软引用主要用于实现缓存。\n");
    }

    /**
     * 3. 弱引用 (Weak Reference)
     * 弱引用的强度比软引用更弱。
     * 当垃圾回收器扫描时，一旦发现了只具有弱引用的对象，不管当前内存空间是否足够，都会回收它的内存。
     * 弱引用通常用于监控对象是否被回收，或者在像 WeakHashMap 这样的数据结构中使用。
     */
    public static void weakReferenceExample() throws InterruptedException {
        System.out.println("--- 弱引用示例 ---");
        
        // 创建一个对象，并用强引用指向它
        Object strongObj = new Object();
        
        // 创建一个引用队列，当弱引用指向的对象被回收时，弱引用自身会被放入这个队列
        ReferenceQueue<Object> refQueue = new ReferenceQueue<>();
        
        // 创建一个弱引用，并关联引用队列
        WeakReference<Object> weakRef = new WeakReference<>(strongObj, refQueue);

        System.out.println("弱引用 get() (强引用存在时): " + weakRef.get());
        System.out.println("引用队列中是否有对象: " + (refQueue.poll() != null));

        // 断开强引用，现在只有弱引用指向这个对象
        strongObj = null;
        System.out.println("强引用已断开...");

        // 执行垃圾回收
        System.gc();
        
        // 等待一段时间，让GC完成
        Thread.sleep(100); 

        System.out.println("GC 后，弱引用 get(): " + weakRef.get());
        
        Reference<?> refFromQueue = refQueue.poll();
        System.out.println("引用队列中是否有对象: " + (refFromQueue != null));
        if (refFromQueue != null) {
            System.out.println("从队列中取出的引用与 weakRef 是否相同: " + (refFromQueue == weakRef));
        }
        System.out.println("弱引用常用于防止内存泄漏，例如在 WeakHashMap 中。\n");
    }

    /**
     * 4. 虚引用/幻象引用 (Phantom Reference)
     * 虚引用是所有引用类型中最弱的一个。它不会影响对象的生命周期。
     * 如果一个对象仅持有虚引用，那么它就和没有任何引用一样，在任何时候都可能被垃圾回收器回收。
     * 它的 get() 方法永远返回 null。
     * 虚引用必须和引用队列（ReferenceQueue）联合使用。主要作用是跟踪对象被垃圾回收的状态，
     * 可以在对象被回收后进行一些清理工作，比如释放直接内存（Direct Memory）。
     */
    public static void phantomReferenceExample() throws InterruptedException {
        System.out.println("--- 虚引用/幻象引用示例 ---");

        Object strongObj = new Object();
        ReferenceQueue<Object> refQueue = new ReferenceQueue<>();
        
        // 创建虚引用，必须关联引用队列
        PhantomReference<Object> phantomRef = new PhantomReference<>(strongObj, refQueue);

        System.out.println("虚引用 get(): " + phantomRef.get()); // 永远是 null

        // 断开强引用
        strongObj = null;
        System.out.println("强引用已断开...");

        // 执行垃圾回收
        System.gc();

        // 等待一段时间
        Thread.sleep(100);

        // 对象被回收后，虚引用本身会被加入到引用队列中
        Reference<?> refFromQueue = refQueue.poll();
        System.out.println("引用队列中是否有对象: " + (refFromQueue != null));
        if (refFromQueue != null) {
            System.out.println("这表明对象已被回收，我们可以执行后续的清理操作了。");
        }
        System.out.println("虚引用主要用于管理堆外内存的释放。\n");
    }
}
