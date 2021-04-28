package panda.utils.Database;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * Druid 连接池的工具类
 *         <dependency>
 *             <groupId>mysql</groupId>
 *             <artifactId>mysql-connector-java</artifactId>
 *             <version>8.0.21</version>
 *             <scope>runtime</scope>
 *         </dependency>
 *      必须导入mysql包
 */
public class MyDuridUtil {

    // 1.定义成员变量
    private static DataSource dataSource;

    static {
        //加载配置文件
        Properties pro = new Properties();
        try {
            pro.load(MyDuridUtil.class.getClassLoader().getResourceAsStream("druid.properties"));
            // 2 获取datasource
            dataSource = DruidDataSourceFactory.createDataSource(pro);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取连接
     */
    public static Connection getConnection()   {
        try {
            return dataSource.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     * 释放资源
     */
    public static void close(Statement stmt, Connection conn) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取连接池方法
     */
    public static DataSource getDataSource() {
        return dataSource;
    }

    public static boolean Insert(String sql) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // 获得连接:
            conn =  MyDuridUtil.getConnection();

            pstmt = conn.prepareStatement(sql);

            int rs = pstmt.executeUpdate(sql);
            if (rs != 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            MyDuridUtil.close(null, pstmt, conn);
        }
        return false;
    }


    public static void main(String[] args) {
        System.out.println("asd2");
        for(int i = 0 ; i < 20 ; ++i) {
            System.out.println(getConnection());
        }

    }
}