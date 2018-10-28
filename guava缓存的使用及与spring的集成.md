# guava缓存的使用及与spring的集成

个人分类： [spring](https://blog.csdn.net/chinabestchina/article/category/6934447)[缓存](https://blog.csdn.net/chinabestchina/article/category/7244551)

版权声明：本文为博主原创文章，未经博主允许不得转载。	https://blog.csdn.net/chinabestchina/article/details/78009220

一、guava缓存简介

guava是google为java开发的库，对jdk进行了扩展。在此我们只介绍guava的缓存。

guava缓存是内存缓存，也就是数据存在内存中的。

二、guava基本使用

1、添加maven依赖



```xml
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>23.0</version>
</dependency>
```



2、自定义guava工具类



```java
public class GuavaCacheUtil {
    public static LoadingCache<String, String> strCache = CacheBuilder
            .newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String, String>() {
                        @Override
                        public String load(String s) throws Exception { //当缓存中不存在时，自动加载新数据到缓存
                            return null;
                        }
                    }
            );

    /**
     * 将值存入缓存
     *
     * @param key
     * @param val
     */
    public static void setStr(String key, String val) {
        strCache.put(key, val);
    }

    /**
     * 从缓存中取值
     *
     * @param key
     * @return
     */
    public static String getStr(String key) {
        String val = "";
        try {
            val = strCache.get(key);
        } catch (ExecutionException e) {
        }
        return val;
    }
}
```



3、使用



```java
public class GuavaCacheMain {
    public static void main(String[] args) {
        String key = "k1";
        String val = "v1";
        GuavaCacheUtil.setStr(key, val);

        String v1 = GuavaCacheUtil.getStr(key);
        System.out.println("k1 : " + v1);
        String v2 = GuavaCacheUtil.getStr(key);
        System.out.println("k2 : " + v2);
    }
}
```

输出：



k1 : v1
k1 : v1

三、guava缓存与spring的集成

1、添加maven依赖



```xml
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>23.0</version>
</dependency>
```

2、在spring中配置guava缓存





```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:cache="http://www.springframework.org/schema/cache"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd
       http://www.springframework.org/schema/cache
       http://www.springframework.org/schema/cache/spring-cache.xsd
        ">
    <context:component-scan base-package="com.dragon.study" />
    <cache:annotation-driven />

    <bean id="cacheManager" class="org.springframework.cache.guava.GuavaCacheManager">
        <property name="cacheSpecification" value="concurrencyLevel=4,expireAfterAccess=100s,expireAfterWrite=100s" />
        <property name="cacheNames">
            <list>
                <value>guavaCache</value>
            </list>
        </property>
    </bean>
</beans>
```

3、在服务中使用guava缓存





```java
public interface StudentService {
    public Student getStudent(Integer id);

    public Student updateStudent(Student stu);

    public void deleteStudent(Integer id);

    public void deleteAllStudent();

    public void myDelete(Integer id);
}
@Service("studentGuavaCache")
public class StudentGuavaCacheImpl implements StudentService {

    @Cacheable(value = "guavaCache",key="'id_'+#id",condition = "#id<3")
    public Student getStudent(Integer id) {
        Student stu = new Student();
        stu.setId(id);
        stu.setName("apple");
        return stu;
    }

    @CachePut(value = "guavaCache",key="'id_'+#stu.getId()")
    public Student updateStudent(Student stu){
        System.out.println("update stu");
        return stu;
    }


    @CacheEvict(value = "guavaCache",key="'id_'+#id")
    public void deleteStudent(Integer id){
        System.out.println("delete student "+id);
    }

    public void myDelete(Integer id){
        try {
            StudentService ss = (StudentService) AopContext.currentProxy();
            ss.deleteStudent(id);
            return ;
        }catch (Exception e){
            e.printStackTrace();

        }
        this.deleteStudent(id);
    }

    @CacheEvict(value = "guavaCache",allEntries = true)
    public void deleteAllStudent(){
        System.out.println("delete all student ");
    }
}
```

4、测试





```java
public class SpringGuavaCacheMain {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("classpath:spring-guava-cache.xml");
        StudentService studentService = (StudentService) ac.getBean("studentGuavaCache");

        Integer id =1;
        Student stu = studentService.getStudent(id);  //新建缓存
        stu = studentService.getStudent(id);   //从缓存中取

        studentService.myDelete(id);
        stu = studentService.getStudent(id);   //从缓存中取

        stu.setName("banana");  //重新设置值
        studentService.updateStudent(stu); //更新缓存
        stu = studentService.getStudent(id); //从缓存中取出新值

        stu = new Student();  //新实例
        stu.setId(0);
        studentService.updateStudent(stu);  //用新建的实例进行更新，会新建缓存
        stu = studentService.getStudent(0);  //从缓存中取

        studentService.deleteStudent(id);  // 删除缓存
        stu = studentService.getStudent(id);  //再次新建缓存

        id=2;
        stu = studentService.getStudent(id); //新建缓存
        studentService.deleteAllStudent(); //删除所有缓存
        id=1;
        stu = studentService.getStudent(id); //因所有缓存被前一步清除，会新建缓存

        id=5;
        stu = studentService.getStudent(id); //不会新建缓存 因为设置了缓存条件必须小于3
        stu = studentService.getStudent(id); //因没有缓存，不会从缓存中取

        Assert.notNull(stu);
    }
}
```

输出：



delete student 1
update stu
update stu
delete student 1
delete all student

四、guava缓存与其它形式缓存共存

除了guava缓存，在还有其它如redis等缓存，使用方法类似，在此仅列出其spring的配置文件



```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:cache="http://www.springframework.org/schema/cache"
       xmlns:p="http://www.springframework.org/schema/p"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd
       http://www.springframework.org/schema/cache
       http://www.springframework.org/schema/cache/spring-cache.xsd
        ">
    <context:component-scan base-package="com.dragon.study" />
    <cache:annotation-driven />

    <bean id="cacheManager" class="org.springframework.cache.support.CompositeCacheManager">
        <property name="cacheManagers">
            <list>
                <ref bean="simpleCacheManager" />
                <ref bean="guavaCacheManager" />
            </list>
        </property>
        <property name="fallbackToNoOpCache" value="true" />
    </bean>

    <bean id="simpleCacheManager" class="org.springframework.cache.support.SimpleCacheManager">
        <property name="caches">
            <set>
                <bean class="org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean" p:name="default" />
                <bean class="org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean" p:name="mycache" />
                <bean class="com.dragon.tbscheduleStudy.cache.RedisCache" p:name="redisCache" p:timeout="60000" />
            </set>
        </property>
    </bean>

    <bean id="guavaCacheManager" class="org.springframework.cache.guava.GuavaCacheManager">
        <property name="cacheSpecification" value="concurrencyLevel=4,expireAfterAccess=100s,expireAfterWrite=100s" />
        <property name="cacheNames">
            <list>
                <value>guavaCache</value>
            </list>
        </property>
    </bean>
</beans>
```