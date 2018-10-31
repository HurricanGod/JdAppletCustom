package cn.hurrican.common.utils;

import java.util.UUID;

/**
 * 生成加密后的32位UUID
 */
public class UUIDUtils {

    /**
     * 生成32位UUID字符串
     *
     * @return 发生异常返回 null
     */
    public static String generateUUID() {
        try {
            UUID uuidInstance = UUID.randomUUID();
            String uuidStr = uuidInstance.toString();
            return uuidStr.replaceAll("-", "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
