package com.github.jackieonway.copier.example.container;

import com.github.jackieonway.copier.example.User;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

/**
 * 容器集成测试，验证 Spring、CDI、JSR330 模式生成的代码。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class ContainerIntegrationTest {

    private User user;

    @Before
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);
    }

    // ========== Spring 容器模式测试 ==========

    @Test
    public void testSpringCopierClassAnnotations() throws Exception {
        Class<?> copierClass = Class.forName("com.github.jackieonway.copier.example.container.SpringUserDtoCopier");

        // 验证类上有 @Component 注解
        boolean hasComponent = false;
        for (Annotation annotation : copierClass.getAnnotations()) {
            if (annotation.annotationType().getSimpleName().equals("Component")) {
                hasComponent = true;
                break;
            }
        }
        assertTrue("Spring Copier 应该有 @Component 注解", hasComponent);
    }

    @Test
    public void testSpringCopierIsNotStatic() throws Exception {
        Class<?> copierClass = Class.forName("com.github.jackieonway.copier.example.container.SpringUserDtoCopier");

        // 验证 toDto 方法不是静态的
        Method toDtoMethod = copierClass.getMethod("toDto", User.class);
        assertFalse("Spring 模式的 toDto 方法不应该是静态的", Modifier.isStatic(toDtoMethod.getModifiers()));
        assertTrue("Spring 模式的 toDto 方法应该是 public 的", Modifier.isPublic(toDtoMethod.getModifiers()));
    }

    @Test
    public void testSpringCopierHasNoArgConstructor() throws Exception {
        Class<?> copierClass = Class.forName("com.github.jackieonway.copier.example.container.SpringUserDtoCopier");

        // 验证有无参构造方法
        Constructor<?> noArgConstructor = copierClass.getDeclaredConstructor();
        assertNotNull("Spring Copier 应该有无参构造方法", noArgConstructor);
        assertTrue("无参构造方法应该是 public 的", Modifier.isPublic(noArgConstructor.getModifiers()));
    }

    @Test
    public void testSpringCopierFunctionalityWithInstance() throws Exception {
        Class<?> copierClass = Class.forName("com.github.jackieonway.copier.example.container.SpringUserDtoCopier");

        // 创建实例
        Object copierInstance = copierClass.getDeclaredConstructor().newInstance();

        // 调用 toDto 方法
        Method toDtoMethod = copierClass.getMethod("toDto", User.class);
        SpringUserDto dto = (SpringUserDto) toDtoMethod.invoke(copierInstance, user);

        // 验证转换结果
        assertNotNull("转换结果不应该为 null", dto);
        assertEquals("ID 应该相等", user.getId(), dto.getId());
        assertEquals("Name 应该相等", user.getName(), dto.getName());
        assertEquals("Email 应该相等", user.getEmail(), dto.getEmail());
        assertEquals("Age 应该相等", user.getAge(), dto.getAge());
    }

    // ========== CDI 容器模式测试 ==========

    @Test
    public void testCdiCopierClassAnnotations() throws Exception {
        Class<?> copierClass = Class.forName("com.github.jackieonway.copier.example.container.CdiUserDtoCopier");

        // 验证类上有 @ApplicationScoped 注解
        boolean hasApplicationScoped = false;
        for (Annotation annotation : copierClass.getAnnotations()) {
            if (annotation.annotationType().getSimpleName().equals("ApplicationScoped")) {
                hasApplicationScoped = true;
                break;
            }
        }
        assertTrue("CDI Copier 应该有 @ApplicationScoped 注解", hasApplicationScoped);
    }

    @Test
    public void testCdiCopierIsNotStatic() throws Exception {
        Class<?> copierClass = Class.forName("com.github.jackieonway.copier.example.container.CdiUserDtoCopier");

        // 验证 toDto 方法不是静态的
        Method toDtoMethod = copierClass.getMethod("toDto", User.class);
        assertFalse("CDI 模式的 toDto 方法不应该是静态的", Modifier.isStatic(toDtoMethod.getModifiers()));
        assertTrue("CDI 模式的 toDto 方法应该是 public 的", Modifier.isPublic(toDtoMethod.getModifiers()));
    }

    @Test
    public void testCdiCopierHasNoArgConstructor() throws Exception {
        Class<?> copierClass = Class.forName("com.github.jackieonway.copier.example.container.CdiUserDtoCopier");

        // 验证有无参构造方法
        Constructor<?> noArgConstructor = copierClass.getDeclaredConstructor();
        assertNotNull("CDI Copier 应该有无参构造方法", noArgConstructor);
        assertTrue("无参构造方法应该是 public 的", Modifier.isPublic(noArgConstructor.getModifiers()));
    }

    @Test
    public void testCdiCopierFunctionalityWithInstance() throws Exception {
        Class<?> copierClass = Class.forName("com.github.jackieonway.copier.example.container.CdiUserDtoCopier");

        // 创建实例
        Object copierInstance = copierClass.getDeclaredConstructor().newInstance();

        // 调用 toDto 方法
        Method toDtoMethod = copierClass.getMethod("toDto", User.class);
        CdiUserDto dto = (CdiUserDto) toDtoMethod.invoke(copierInstance, user);

        // 验证转换结果
        assertNotNull("转换结果不应该为 null", dto);
        assertEquals("ID 应该相等", user.getId(), dto.getId());
        assertEquals("Name 应该相等", user.getName(), dto.getName());
        assertEquals("Email 应该相等", user.getEmail(), dto.getEmail());
        assertEquals("Age 应该相等", user.getAge(), dto.getAge());
    }

    // ========== JSR-330 容器模式测试 ==========

    @Test
    public void testJsr330CopierClassAnnotations() throws Exception {
        Class<?> copierClass = Class.forName("com.github.jackieonway.copier.example.container.Jsr330UserDtoCopier");

        // 验证类上有 @Named 和 @Singleton 注解
        boolean hasNamed = false;
        boolean hasSingleton = false;
        for (Annotation annotation : copierClass.getAnnotations()) {
            String simpleName = annotation.annotationType().getSimpleName();
            if (simpleName.equals("Named")) {
                hasNamed = true;
            }
            if (simpleName.equals("Singleton")) {
                hasSingleton = true;
            }
        }
        assertTrue("JSR-330 Copier 应该有 @Named 注解", hasNamed);
        assertTrue("JSR-330 Copier 应该有 @Singleton 注解", hasSingleton);
    }

    @Test
    public void testJsr330CopierIsNotStatic() throws Exception {
        Class<?> copierClass = Class.forName("com.github.jackieonway.copier.example.container.Jsr330UserDtoCopier");

        // 验证 toDto 方法不是静态的
        Method toDtoMethod = copierClass.getMethod("toDto", User.class);
        assertFalse("JSR-330 模式的 toDto 方法不应该是静态的", Modifier.isStatic(toDtoMethod.getModifiers()));
        assertTrue("JSR-330 模式的 toDto 方法应该是 public 的", Modifier.isPublic(toDtoMethod.getModifiers()));
    }

    @Test
    public void testJsr330CopierHasNoArgConstructor() throws Exception {
        Class<?> copierClass = Class.forName("com.github.jackieonway.copier.example.container.Jsr330UserDtoCopier");

        // 验证有无参构造方法
        Constructor<?> noArgConstructor = copierClass.getDeclaredConstructor();
        assertNotNull("JSR-330 Copier 应该有无参构造方法", noArgConstructor);
        assertTrue("无参构造方法应该是 public 的", Modifier.isPublic(noArgConstructor.getModifiers()));
    }

    @Test
    public void testJsr330CopierFunctionalityWithInstance() throws Exception {
        Class<?> copierClass = Class.forName("com.github.jackieonway.copier.example.container.Jsr330UserDtoCopier");

        // 创建实例
        Object copierInstance = copierClass.getDeclaredConstructor().newInstance();

        // 调用 toDto 方法
        Method toDtoMethod = copierClass.getMethod("toDto", User.class);
        Jsr330UserDto dto = (Jsr330UserDto) toDtoMethod.invoke(copierInstance, user);

        // 验证转换结果
        assertNotNull("转换结果不应该为 null", dto);
        assertEquals("ID 应该相等", user.getId(), dto.getId());
        assertEquals("Name 应该相等", user.getName(), dto.getName());
        assertEquals("Email 应该相等", user.getEmail(), dto.getEmail());
        assertEquals("Age 应该相等", user.getAge(), dto.getAge());
    }

    // ========== 对比测试：验证不同模式的差异 ==========

    @Test
    public void testDefaultModeIsStatic() throws Exception {
        // UserDto 使用默认模式（DEFAULT），应该生成静态方法
        Class<?> copierClass = Class.forName("com.github.jackieonway.copier.example.UserDtoCopier");

        Method toDtoMethod = copierClass.getMethod("toDto", User.class);
        assertTrue("DEFAULT 模式的 toDto 方法应该是静态的", Modifier.isStatic(toDtoMethod.getModifiers()));
    }

    @Test
    public void testContainerModesAreNotStatic() throws Exception {
        // Spring、CDI、JSR330 模式都应该生成实例方法
        Class<?>[] copierClasses = {
            Class.forName("com.github.jackieonway.copier.example.container.SpringUserDtoCopier"),
            Class.forName("com.github.jackieonway.copier.example.container.CdiUserDtoCopier"),
            Class.forName("com.github.jackieonway.copier.example.container.Jsr330UserDtoCopier")
        };

        for (Class<?> copierClass : copierClasses) {
            Method toDtoMethod = copierClass.getMethod("toDto", User.class);
            assertFalse(
                copierClass.getSimpleName() + " 的 toDto 方法不应该是静态的",
                Modifier.isStatic(toDtoMethod.getModifiers())
            );
        }
    }
}
