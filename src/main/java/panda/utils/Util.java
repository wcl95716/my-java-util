package panda.utils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import panda.utils.KDTree.Point;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Util {

    public static final double PI    = 3.1415926;
    public static final double r     = 6378137;   // 地球的赤道半径
    public static final double delta = 1e-7;      // GPS点的精度
    static double degToRad = PI / 180.0;
    static double radToDeg = 180.0 / PI;

    /**
     * 常用工具
     */


    public static  double distance(Point start, Point end) {

        double lat1 = start.getLat() / 180.0 * PI;
        double lon1 = start.getLng() / 180.0 * PI;
        double lat2 = end.getLat() / 180.0 * PI;
        double lon2 = end.getLng() / 180.0 * PI;
        double d1 = Math.abs(lat1 - lat2);
        double d2 = Math.abs(lon1 - lon2);
        double t = Math.pow(Math.sin(d1 / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(d2 / 2), 2);
        double dis = r * 2 * Math.asin(Math.sqrt(t));
        return dis;
    }

    /**
     * 导出生成csv格式的文件
     *
     * @param titles    csv格式头文
     * @param propertys 需要导出的数据实体的属性，注意与title一一对应
     * @param list      需要导出的对象集合
     * @return
     * @throws IOException              Created         2017年1月5日 上午10:51:44
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @author ccg
     */
    public static <T> String exportCsv(String[] titles, String[] propertys, List<T> list) throws IOException, IllegalArgumentException, IllegalAccessException {
        File file = new File("./test.csv");
        //构建输出流，同时指定编码
        OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(file), "gbk");

        //csv文件是逗号分隔，除第一个外，每次写入一个单元格数据后需要输入逗号
        for (String title : titles) {
            ow.write(title);
            ow.write(",");
        }
        //写完文件头后换行
        ow.write("\r\n");
        //写内容
        for (Object obj : list) {
            //利用反射获取所有字段
            Field[] fields = obj.getClass().getDeclaredFields();
            for (String property : propertys) {
                for (Field field : fields) {
                    //设置字段可见性
                    field.setAccessible(true);
                    if (property.equals(field.getName())) {
                        ow.write(field.get(obj).toString());
                        ow.write(",");
                        continue;
                    }
                }
            }
            //写完一行换行
            ow.write("\r\n");
        }
        ow.flush();
        ow.close();
        return "0";
    }

    //读取json文件
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<JSONObject> readJsonsFile(String jsonFilePath) {
        List<JSONObject> jsonObjectList = new ArrayList<>();

        try {
            File jsonFile = new File(jsonFilePath);
            BufferedReader br = new BufferedReader(new FileReader(jsonFile));
            String str;
            int jsonFailCount = 0 ;
            do {
                str = br.readLine();
                //System.out.println("asdasd :" + str);
                JSONObject jsonObject = null;

                try {

                    System.out.println(str);
                    jsonObject = JSONObject.parseObject(str);
                }
                catch ( JSONException e ) {
                    jsonFailCount += 1;
                    // System.out.println(" 解析失败" + jsonFailCount);
                    //exit(1);
                }
                //jsonObject = JSONObject.parseObject(str);
                if(jsonObject != null){
                    jsonObjectList.add(jsonObject);
                }

            } while (str != null );

            return jsonObjectList;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static JSONObject readJsonObj(String fileName){
        try {
            String jsonStr =  readJsonFile(fileName);
            //System.out.println(jsonStr);
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);

            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    public static void writeFile(List<List<String>> message, String path) throws IOException {
        createFile(path);
        OutputStream output = new FileOutputStream(path);
        for (List<String> line : message) {
            boolean tag = false;
            for (String data : line) {
                if (tag) {
                    output.write(",".getBytes("UTF-8"));
                }
                output.write(data.getBytes("UTF-8"));
                tag = true;

            }
            output.write("\n".getBytes("UTF-8"));
        }
        output.close();
    }

    public static void writeString(String data, String path)   {
        try {
            createFile(path);
            OutputStream output = new FileOutputStream(path);
            output.write(data.getBytes("UTF-8"));
            output.write("\n".getBytes("UTF-8"));

            output.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static  void writeList(List<String> message, String path) {

        try {
            createFile(path);
            OutputStream output = new FileOutputStream(path);
            for (String data : message) {
                output.write(data.getBytes("UTF-8"));
                output.write("\n".getBytes("UTF-8"));
                output.flush();
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createFile(String src) {

        // path表示你所创建文件的路径
        String path = src.substring(0, src.lastIndexOf("/"));
        // fileName表示你创建的文件名
        String fileName = src.substring(src.lastIndexOf("/") + 1, src.length());
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        File file = new File(f, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 一行一行读取文件，解决读取中文字符时出现乱码
     * <p>
     * 流的关闭顺序：先打开的后关，后打开的先关，
     * 否则有可能出现java.io.IOException: Stream closed异常
     *
     * @throws IOException
     */
    public List<Point> readPoints(String path)   {

        List<Point> pointList = new ArrayList<>();
        File file = new File(path);
        //如果文件夹不存在则创建
        if (!file.exists()) {
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(path);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            //简写如下
            //BufferedReader br = new BufferedReader(new InputStreamReader(
            String line = "";
            String[] arrs = null;

            while ((line = br.readLine()) != null) {
                arrs = line.split(",");
                //System.out.println(arrs[0] + " : " + arrs[1] + " : ");
                Point point = new Point(Double.valueOf(arrs[0]), Double.valueOf(arrs[1]));
                pointList.add(point);
            }
            br.close();
            isr.close();
            fis.close();
            return pointList;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 一行一行读取文件，解决读取中文字符时出现乱码
     * <p>
     * 流的关闭顺序：先打开的后关，后打开的先关，
     * 否则有可能出现java.io.IOException: Stream closed异常
     *
     * @throws IOException
     */
    public List<Point> readPoints2(String path)   {

        List<Point> pointList = new ArrayList<>();
        File file = new File(path);
        //如果文件夹不存在则创建
        if (!file.exists()) {
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(path);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            //简写如下
            //BufferedReader br = new BufferedReader(new InputStreamReader(
            String line = "";
            String[] arrs = null;

            while ((line = br.readLine()) != null) {
                arrs = line.split(",");
                //System.out.println(arrs[0] + " : " + arrs[1] + " : ");
                Point point = new Point(Double.valueOf(arrs[0]), Double.valueOf(arrs[1]));
                pointList.add(point);
                Point point2 = new Point(Double.valueOf(arrs[2]), Double.valueOf(arrs[3]));
                pointList.add(point2);
            }
            br.close();
            isr.close();
            fis.close();
            return pointList;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static  List<String[]> readFileLines(String path )   {
        return readFileLines(path , 100);
    }
    public static List<String[]> readFileLines(String path , int splitLimit)   {

        List<String[] > fileLines = new ArrayList<>();
        File file = new File(path);
        //如果文件夹不存在则创建
        if (!file.exists()) {
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(path);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            //简写如下
            //BufferedReader br = new BufferedReader(new InputStreamReader(
            String line = "";
            String[] arrs = null;

            while ((line = br.readLine()) != null) {
                arrs = line.split("," , splitLimit);
                fileLines.add(arrs);
            }
            br.close();
            isr.close();
            fis.close();
            return fileLines;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> readFileList(String path )   {

        List<String > fileLines = new ArrayList<>();
        File file = new File(path);
        //如果文件夹不存在则创建
        if (!file.exists()) {
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(path);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            //简写如下
            //BufferedReader br = new BufferedReader(new InputStreamReader(
            String line = "";
            String[] arrs = null;

            while ((line = br.readLine()) != null) {

                fileLines.add(line);
            }
            br.close();
            isr.close();
            fis.close();
            return fileLines;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 第二种方式：通过UUID类（表示通用唯一标识符的类）获得唯一值，UUID表示一个128位的值
     *
     */
    public static String getUUID()
    {
        String s= UUID.randomUUID().toString().replace("-","");
        return s;
    }

}
