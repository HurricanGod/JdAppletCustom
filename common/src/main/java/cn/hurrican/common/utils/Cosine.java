package cn.hurrican.common.utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cosine {

    private static final Pattern p = Pattern.compile("[\u4e00-\u9fa5]");

    public static boolean isContainChineseWord(String str) {
        Matcher m = p.matcher(str);
        return m.find();
    }

    public static double getSimilarity(String doc1, String doc2) {
        if (doc1 != null && doc1.trim().length() > 0 && doc2 != null && doc2.trim().length() > 0) {

            Map<Integer, int[]> AlgorithmMap = new HashMap<>(32);

            //将两个字符串中的中文字符以及出现的总数封装到，AlgorithmMap中
            for (int i = 0; i < doc1.length(); i++) {
                char d1 = doc1.charAt(i);
                if (isHanZi(d1)) {//标点和数字不处理
                    int charIndex = getGB2312Id(d1);//保存字符对应的GB2312编码
                    if (charIndex != -1) {
                        int[] fq = AlgorithmMap.get(charIndex);
                        if (fq != null && fq.length == 2) {
                            fq[0]++;//已有该字符，加1
                        } else {
                            fq = new int[2];
                            fq[0] = 1;
                            fq[1] = 0;
                            AlgorithmMap.put(charIndex, fq);//新增字符入map
                        }
                    }
                }
            }

            for (int i = 0; i < doc2.length(); i++) {
                char d2 = doc2.charAt(i);
                if (isHanZi(d2)) {
                    int charIndex = getGB2312Id(d2);
                    if (charIndex != -1) {
                        int[] fq = AlgorithmMap.get(charIndex);
                        if (fq != null && fq.length == 2) {
                            fq[1]++;
                        } else {
                            fq = new int[2];
                            fq[0] = 0;
                            fq[1] = 1;
                            AlgorithmMap.put(charIndex, fq);
                        }
                    }
                }
            }

            Iterator<Integer> iterator = AlgorithmMap.keySet().iterator();
            double sqdoc1 = 0;
            double sqdoc2 = 0;
            double denominator = 0;
            while (iterator.hasNext()) {
                int[] c = AlgorithmMap.get(iterator.next());
                denominator += c[0] * c[1];
                sqdoc1 += c[0] * c[0];
                sqdoc2 += c[1] * c[1];
            }

            return denominator / Math.sqrt(sqdoc1 * sqdoc2);//余弦计算
        } else {
            throw new NullPointerException(" the Document is null or have not chars!");
        }
    }

    public static boolean isHanZi(char ch) {
        // 判断是否汉字
        return (ch >= 0x4E00 && ch <= 0x9FA5);
    }

    /**
     * 根据输入的Unicode字符，获取它的GB2312编码或者ascii编码，
     *
     * @param ch 输入的GB2312中文字符或者ASCII字符(128个)
     * @return ch在GB2312中的位置，-1表示该字符不认识
     */
    public static short getGB2312Id(char ch) {
        try {
            byte[] buffer = Character.toString(ch).getBytes("GB2312");
            if (buffer.length != 2) {
                // 正常情况下buffer应该是两个字节，否则说明ch不属于GB2312编码，故返回'?'，此时说明不认识该字符
                return -1;
            }
            int b0 = (buffer[0] & 0x0FF) - 161; // 编码从A1开始，因此减去0xA1=161
            int b1 = (buffer[1] & 0x0FF) - 161;
            return (short) (b0 * 94 + b1);// 第一个字符和最后一个字符没有汉字，因此每个区只收16*6-2=94个汉字
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void main(String[] args) {

        String str1 = "中国计算机辅助设计与图形学大会";
        String str2 = "第十二届中国计算机图形学大";

        double similarity = getSimilarity(str1, str2);
        System.out.println("similarity = " + similarity);
    }
}