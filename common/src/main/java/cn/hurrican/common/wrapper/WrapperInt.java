package cn.hurrican.common.wrapper;


/**
 * @Author: Hurrican
 * @Description:
 * @Date 2018/9/20
 * @Modified 15:57
 */

public class WrapperInt {

    public static Integer toInt(String value) {
        return value != null ? Integer.valueOf(value) : 0;
    }

    public static Integer toInt(Object value) {
        return value != null ? value instanceof Integer
                ? (Integer) (value) : value instanceof Long
                ? ((Long) value).intValue() : 0 : 0;
    }
}
