package cn.hurrican.common.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 获取Spring上下文
 */
@Component
public class SpringContextManager implements ApplicationContextAware {

    private static ApplicationContext ctx;

    /**
     * 得到当前spring上下文
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return ctx;
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        ctx = applicationContext;
    }

    /**
     * 获得bean
     *
     * @param requiredType 类型
     * @param <T>          类型
     * @return spring 容器中的对象
     */
    public static <T> T getBean(Class<T> requiredType) {
        return getApplicationContext().getBean(requiredType);
    }

    /**
     * 获得SpringBean
     *
     * @param name         Bean 名字
     * @param requiredType 类型
     * @param <T>          强转换类型
     * @return 返回结果
     */
    public static <T> T getBean(String name, Class<T> requiredType) {
        return getApplicationContext().getBean(name, requiredType);
    }


}
