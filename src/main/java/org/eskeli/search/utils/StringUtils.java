package org.eskeli.search.utils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class Desc : 字符串工具类
 *
 * @author elisontl
 */
public class StringUtils {

    /**
     * 判断字符串不为空
     *
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        if (null != str && str.trim().length() > 0) return true;
        else return false;
    }

    /**
     * 判断字符串为空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.trim().length() < 1) return true;
        else return false;
    }

    /**
     * 字符串转UTF-8编码
     *
     * @param srcStr
     * @return
     */
    public static String encode2Utf8(String srcStr) {
        if (isNotEmpty(srcStr)) {
            StringBuffer output = new StringBuffer();
            for (int i = 0; i < srcStr.length(); i++) {
                output.append("\\u" + Integer.toString(srcStr.charAt(i), 16));
            }
            return output.toString();
        }
        return "";
    }

    /**
     * 解码：对于前台encodeURIComponent后的数据
     *
     * @param str
     * @return
     */
    public static String decode(String str) {
        try {
            return java.net.URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 判断一个字符串中是否含有字母(a-z A-Z)
     *
     * @param str
     * @return 包含字母返回true，不包含字母返回false
     */
    public static boolean containLetter(String str) {
        return Pattern.compile(".*[a-zA-Z]+.*").matcher(str).matches();
    }

    /**
     * 判断一个字符串中是否含有中文
     *
     * @param str
     * @return 包含中文返回true，不包含中文返回false
     */
    public static boolean containChinese(String str) {
        return Pattern.compile("[\u4e00-\u9fa5]").matcher(str).find();
    }

    /**
     * 判断一个文件中，是否含有指定字符，含有则返回true，否则返回false
     *
     * @param filePath: String
     * @param str:      String
     */
    @SuppressWarnings("resource")
    public static boolean containSpecifiedStr(String filePath, String str) {
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8");
            BufferedReader br = new BufferedReader(is);
            String tempStr = null;
            while (isNotEmpty(tempStr = br.readLine())) {
                tempStr = tempStr.trim();
                if (tempStr.equals(str) && tempStr.length() == str.length()) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 去除字符串中的空格
     *
     * @param str
     * @return
     */
    public static String subWhitespace(String str) {
        if (isNotEmpty(str)) {
            str = str.replace(" ", "");
        }
        return str;
    }

    /**
     * 获取项目根路径
     *
     * @return
     */
    public static String getRootPath() {
        String rootPath = StringUtils.class.getResource("/").getPath();
        rootPath = rootPath.substring(1)
                .replace("\\/", "\\")
                .replace("%20", " ");
        return rootPath;
    }

    /**
     * 将字符串中小写字母转换为大写字母
     *
     * @param str
     * @return
     */
    public static String convertStrUpperCase(String str) {
        StringBuffer sb_Str = null;
        if (isNotEmpty(str)) {
            sb_Str = new StringBuffer();
            char c[] = str.toCharArray();
            for (int i = 0; i < str.length(); i++) {
                if (c[i] >= 97) {
                    sb_Str.append((c[i] + "").toUpperCase());
                }
            }
            return sb_Str.toString();
        }
        return null;
    }

    public static String disposeErrerStr(String str) {
        if (isNotEmpty(str)) {
            if (str.indexOf("&ldquo") != -1) {
                str = str.replace("&ldquo", "&ldquo;");
            }
        }
        return str;
    }

    // 首字母大写
    public static String initialUpcase(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // 文件拓展名替换
    public static String replaceSuffix(String fileName, String sourceSuffix, String targetSuffix) {
        if (fileName.endsWith(sourceSuffix)) {
            int index = fileName.lastIndexOf(sourceSuffix);
            fileName = fileName.substring(0, index) + targetSuffix;
        }
        return fileName;
    }

    /**
     * 判断传入的字符串为数字或小数
     *
     * @param str : String
     * @return
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        if (str.indexOf(".") > 0) {
            if (str.indexOf(".") == str.lastIndexOf(".") && str.split("\\.").length == 2) {
                return pattern.matcher(str.replace(".", "")).matches();
            } else {
                return false;
            }
        } else {
            return pattern.matcher(str).matches();
        }
    }

    /**
     * 判断传入字符串是否为数字开头
     *
     * @return
     * @parma str : String
     */
    public static boolean isStartWithNumeric(String str) {
        if (str.length() <= 0) return false;
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str.charAt(0) + "");
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * Unicode 转 UTF-8 编码
     *
     * @param theString
     * @return
     */
    public static String unicodeToUtf8(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }

    /**
     * 识别字符串首字母类型
     *
     * @param str : String : 参数
     * @return
     */
    public static String judgeFirstChar(String str) {
        char c = str.charAt(0);
        // 数字
        if (c >= '0' && c <= '9') {
            return "num";
        }
        // 字母
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
            return "eng";
        }
        // 汉字
        String regx = "([\u4E00-\u9FA5]{1,})";
        if (Pattern.matches(regx, c + "")) {
            return "chi";
        }
        // 其他
        return "oth";
    }

}
