import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 一个完整的、可运行的 JDK 动态代理示例。
 * 你可以直接运行 main 方法查看效果。
 */
public class JdkProxyExample {

    // 1. 定义一个接口
    interface Hello {
        void sayHello();
    }

    // 2. 定义接口的实现类（目标对象）
    static class HelloImpl implements Hello {
        @Override
        public void sayHello() {
            System.out.println("Hello World from HelloImpl!");
        }
    }

    // 3. 实现 InvocationHandler 接口，这是代理的核心逻辑处理器
    static class MyInvocationHandler implements InvocationHandler {
        // 持有目标对象的引用
        private final Object target;

        public MyInvocationHandler(Object target) {
            this.target = target;
        }

        /**
         * 所有对代理对象的方法调用，都会被转发到这个 invoke 方法中
         * @param proxy 代理对象本身（一般很少使用）
         * @param method 被调用的方法对象
         * @param args 方法的参数
         * @return 方法的返回值
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 在调用目标方法【之前】，我们可以添加自定义逻辑
            System.out.println("[Proxy] Invoking method: " + method.getName());

            // 这个 invoke 方法是所有代理逻辑的中心。
            // 在一个更复杂的应用中（比如 Spring AOP），这个方法会变得更“智能”。
            // 它可以根据 method 对象（比如检查方法上的注解 @Transactional），
            // 来决定是执行事务逻辑、日志逻辑，还是其他增强逻辑。
            // 这样，一个 InvocationHandler 就可以处理多种不同的切面，实现真正的AOP。

            // 通过反射调用目标对象的原始方法
            Object result = method.invoke(target, args);

            // 在调用目标方法【之后】，我们也可以添加自定义逻辑
            System.out.println("[Proxy] Method invocation finished.");

            return result;
        }
    }


    public static void main(String[] args) {
        // 4. 创建目标对象实例
        HelloImpl target = new HelloImpl();

        // 5. 创建一个 InvocationHandler 实例，并传入目标对象
        MyInvocationHandler handler = new MyInvocationHandler(target);

        // 6. 使用 Proxy.newProxyInstance() 动态创建代理对象
        //    这行代码是 JDK 动态代理的核心，它在【运行时】做了三件大事：
        //    a. 在内存中动态生成一个新类的字节码，这个类我们称之为代理类（例如 com.sun.proxy.$Proxy0）。
        //    b. 这个代理类会实现我们传入的接口（这里是 Hello.class），并把所有方法的调用都转发给 handler 的 invoke 方法。
        //    c. 使用类加载器将这个新生成的类加载到 JVM 中，并创建它的实例。
        Hello proxyHello = (Hello) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                handler
        );

        // 7. 通过代理对象调用方法
        //    你会看到，调用 sayHello() 方法时，MyInvocationHandler 中的 invoke 方法被触发了。
        System.out.println("--- Calling method via proxy ---");
        proxyHello.sayHello();
        System.out.println("--------------------------------");

        // 我们可以验证一下代理对象的类型
        System.out.println("\nProxy object class: " + proxyHello.getClass().getName());
        System.out.println("Is proxy a Proxy? " + Proxy.isProxyClass(proxyHello.getClass()));
        System.out.println("Is proxy a Hello? " + (proxyHello instanceof Hello));
        System.out.println("Is proxy a HelloImpl? " + (proxyHello instanceof HelloImpl));
    }
}
