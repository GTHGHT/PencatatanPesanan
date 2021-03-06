package util;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MysqlDataSource {
    public static DataSource getDataSource(){
        Properties prop = new Properties();
        try(
                InputStream is = MysqlDataSource.class.getResourceAsStream("/util/db.properties")
        ){
            prop.load(is);
            com.mysql.cj.jdbc.MysqlDataSource mysqlDS = new com.mysql.cj.jdbc.MysqlDataSource();
            mysqlDS.setURL(prop.getProperty("DB_URL"));
            mysqlDS.setUser(prop.getProperty("DB_USERNAME"));
            mysqlDS.setPassword(prop.getProperty("DB_PASSWORD"));
            return mysqlDS;
        }catch (IOException|NullPointerException e){
            e.printStackTrace();
            return null;
        }
    }
}
