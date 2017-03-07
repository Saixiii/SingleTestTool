package com.truemove.msoc.stt;

/**
 * @Author  : Suphakit Annoppornchai [Saixiii]
 * @Project : stt
 * @Class   : OracleJDBC
 * @Date    : Oct 17, 2015 10:54:46 AM
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


public class OracleJDBC {
    
    private static final String PROPERTIES_FILE_NAME = "oracle.properties";
    
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "oracle.jdbc.OracleDriver";
    static String DB_URL = "jdbc:oracle:thin:@172.16.69.166:1521:VASDB";
    
    //  Database credentials
    static String DB_USER = "stt";
    static String DB_PASS = "stt";
   
   public static void loadProperties () throws ConfigurationException {
       
       Configuration config = new PropertiesConfiguration(PROPERTIES_FILE_NAME);
       DB_URL = config.getString("jdbc_url");
       DB_USER = config.getString("jdbc_user");
       DB_PASS = config.getString("jdbc_pass");
       
   }
   
   private static Connection getDBConnection() {
       
        //try {
        //    loadProperties();
        //} catch (ConfigurationException ex) {
        //    Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
        //}
        
        Connection conn = null;
       
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            return conn;
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       return conn;
       
   }
   
   public static Map<String,String> loadConfig (String group) {
       
       Map<String,String> config = new HashMap<String,String>();
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       
       String querySQL = "SELECT KEY,VALUE FROM CONFIG WHERE CONFIG = ?";
       
        try {
            statement = conn.prepareStatement(querySQL);
            statement.setString(1,group);
            
            // execute select SQL stetement
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                config.put(rs.getString("KEY"),rs.getString("VALUE"));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
           try {
               if(statement != null)
                   statement.close();
               if(conn != null)
                   conn.close();
               
               return config;
           } catch (SQLException ex) {
               Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
           }
        }
        return config;
   }
   
   public List<Map<String,String>> getTV (String des) throws SQLException {
       
       List<Map<String,String>> tv = new ArrayList<>();
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       
       String querySQL = "SELECT DESTINATION.DESTINATION,"
               + "DESTINATION.NE,"
               + "DESTINATION.IP,"
               + "DESTINATION.SSL,"
               + "DESTINATION.KEYSTORE,"
               + "DESTINATION.PASSWORD,"
               + "REQUEST_XML.COMMAND,"
               + "REQUEST_XML.DESCRIPTION,"
               + "REQUEST_XML.CONTEXT_ROOT,"
               + "REQUEST_XML.XML,"
               + "REQUEST_XML.PATH_CODE,"
               + "REQUEST_XML.PATH_DESC "
               + "FROM REQUEST_XML "
               + "JOIN DESTINATION "
               + "ON REQUEST_XML.NE = DESTINATION.NE "
               + "AND UPPER(DESTINATION.DESTINATION) = UPPER(?) "
               + "AND REQUEST_XML.STATUS = 'Y' "
               + "ORDER BY REQUEST_XML.COMMAND";
       
       try {
            statement = conn.prepareStatement(querySQL);
            statement.setString(1,des);
            
            // execute select SQL stetement
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                Map<String,String> row = new HashMap<>();
                
                row.put("DESTINATION",rs.getString("DESTINATION"));
                row.put("NE",rs.getString("NE"));
                row.put("IP",rs.getString("IP"));
                row.put("SSL",rs.getString("SSL"));
                row.put("KEYSTORE",rs.getString("KEYSTORE"));
                row.put("PASSWORD",rs.getString("PASSWORD"));
                row.put("COMMAND",rs.getString("COMMAND"));
                row.put("DESCRIPTION",rs.getString("DESCRIPTION"));
                row.put("CONTEXT_ROOT",rs.getString("CONTEXT_ROOT"));
                row.put("XML",clobToString(rs.getClob("XML")));
                row.put("PATH_CODE",rs.getString("PATH_CODE"));
                row.put("PATH_DESC",rs.getString("PATH_DESC"));
                
                tv.add(row);
            }
            return tv;
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(statement != null)
                statement.close();
            if(conn != null)
                conn.close();
            
            return tv;
        }
   }
   
   public List<Map<String,String>> getTV (String des,String com) throws SQLException {
       
       List<Map<String,String>> tv = new ArrayList<>();
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       
       String querySQL = "SELECT DESTINATION.DESTINATION,"
               + "DESTINATION.NE,"
               + "DESTINATION.IP,"
               + "DESTINATION.SSL,"
               + "DESTINATION.KEYSTORE,"
               + "DESTINATION.PASSWORD,"
               + "REQUEST_XML.COMMAND,"
               + "REQUEST_XML.DESCRIPTION,"
               + "REQUEST_XML.CONTEXT_ROOT,"
               + "REQUEST_XML.XML,"
               + "REQUEST_XML.PATH_CODE,"
               + "REQUEST_XML.PATH_DESC "
               + "FROM REQUEST_XML "
               + "JOIN DESTINATION "
               + "ON REQUEST_XML.NE = DESTINATION.NE "
               + "AND UPPER(DESTINATION.DESTINATION) = UPPER(?) "
               + "AND UPPER(REQUEST_XML.COMMAND) = UPPER(?)";
       
       try {
            statement = conn.prepareStatement(querySQL);
            statement.setString(1,des);
            statement.setString(2,com);
            
            // execute select SQL stetement
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                Map<String,String> row = new HashMap<>();
                
                row.put("DESTINATION",rs.getString("DESTINATION"));
                row.put("NE",rs.getString("NE"));
                row.put("IP",rs.getString("IP"));
                row.put("SSL",rs.getString("SSL"));
                row.put("KEYSTORE",rs.getString("KEYSTORE"));
                row.put("PASSWORD",rs.getString("PASSWORD"));
                row.put("COMMAND",rs.getString("COMMAND"));
                row.put("DESCRIPTION",rs.getString("DESCRIPTION"));
                row.put("CONTEXT_ROOT",rs.getString("CONTEXT_ROOT"));
                row.put("XML",clobToString(rs.getClob("XML")));
                row.put("PATH_CODE",rs.getString("PATH_CODE"));
                row.put("PATH_DESC",rs.getString("PATH_DESC"));
                
                tv.add(row);
            }
            return tv;
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(statement != null)
                statement.close();
            if(conn != null)
                conn.close();
            
            return tv;
        }
   }
   
   public List<Map<String,String>> listTV (String des) throws SQLException {
       
       List<Map<String,String>> tv = new ArrayList<>();
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       
       String querySQL = "SELECT DESTINATION.DESTINATION,"
               + "DESTINATION.NE,"
               + "DESTINATION.IP,"
               + "DESTINATION.SSL,"
               + "DESTINATION.KEYSTORE,"
               + "DESTINATION.PASSWORD,"
               + "REQUEST_XML.COMMAND,"
               + "REQUEST_XML.DESCRIPTION,"
               + "REQUEST_XML.CONTEXT_ROOT,"
               + "REQUEST_XML.XML,"
               + "REQUEST_XML.PATH_CODE,"
               + "REQUEST_XML.PATH_DESC "
               + "FROM FLOW "
               + "JOIN DESTINATION "
               + "ON FLOW.DESTINATION = DESTINATION.DESTINATION "
               + "AND UPPER(FLOW.NAME) = UPPER(?) "
               + "AND FLOW.STATUS = 'Y' "
               + "JOIN REQUEST_XML "
               + "ON REQUEST_XML.NE = DESTINATION.NE "
               + "AND REQUEST_XML.COMMAND = FLOW.COMMAND "
               + "AND ROWNUM < 1000 "
               + "ORDER BY FLOW.SEQUENCE";
       
       try {
            statement = conn.prepareStatement(querySQL);
            statement.setString(1,des);
            
            // execute select SQL stetement
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                Map<String,String> row = new HashMap<>();
                
                row.put("DESTINATION",rs.getString("DESTINATION"));
                row.put("NE",rs.getString("NE"));
                row.put("IP",rs.getString("IP"));
                row.put("SSL",rs.getString("SSL"));
                row.put("KEYSTORE",rs.getString("KEYSTORE"));
                row.put("PASSWORD",rs.getString("PASSWORD"));
                row.put("COMMAND",rs.getString("COMMAND"));
                row.put("DESCRIPTION",rs.getString("DESCRIPTION"));
                row.put("CONTEXT_ROOT",rs.getString("CONTEXT_ROOT"));
                row.put("XML",clobToString(rs.getClob("XML")));
                row.put("PATH_CODE",rs.getString("PATH_CODE"));
                row.put("PATH_DESC",rs.getString("PATH_DESC"));
                
                tv.add(row);
            }
            return tv;
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(statement != null)
                statement.close();
            if(conn != null)
                conn.close();
            
            return tv;
        }
   }
   
   
   public String getXML (String des,String com) throws SQLException {
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       String xml = null;
       
       String querySQL = "SELECT REQUEST_XML.XML "
               + "FROM REQUEST_XML "
               + "JOIN DESTINATION "
               + "ON REQUEST_XML.NE = DESTINATION.NE "
               + "AND UPPER(DESTINATION.DESTINATION) = UPPER(?) "
               + "AND UPPER(REQUEST_XML.COMMAND) = UPPER(?)";
       
        try {
            statement = conn.prepareStatement(querySQL);
            statement.setString(1,des);
            statement.setString(2,com);
            
            // execute select SQL stetement
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                Clob cxml = rs.getClob("XML");
                xml = clobToString(cxml);
            }
            
            return xml;
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(statement != null)
                statement.close();
            if(conn != null)
                conn.close();
            
            return xml;
        }      
   }
   
   public static String getURL (String des,String com) throws SQLException {
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       String url = null;
       
       String querySQL = "SELECT DESTINATION.IP||REQUEST_XML.CONTEXT_ROOT AS URL "
               + "FROM DESTINATION "
               + "JOIN REQUEST_XML "
               + "ON DESTINATION.NE = REQUEST_XML.NE "
               + "AND UPPER(DESTINATION.DESTINATION) = UPPER(?) "
               + "AND UPPER(REQUEST_XML.COMMAND) = UPPER(?)";
       
        try {
            statement = conn.prepareStatement(querySQL);
            statement.setString(1,des);
            statement.setString(2,com);
            
            // execute select SQL stetement
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                url = rs.getString("URL");
            }
            
            return url;
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(statement != null)
                statement.close();
            if(conn != null)
                conn.close();
            
            return url;
        }
   }
   
   public static String getSSL (String des) throws SQLException {
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       String ssl = "N";
       
       String querySQL = "SELECT SSL "
               + "FROM DESTINATION "
               + "WHERE UPPPER(DESTINATION) = UPPER(?)";
               
        try {
            statement = conn.prepareStatement(querySQL);
            statement.setString(1,des);
            
            // execute select SQL stetement
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                ssl = rs.getString("SSL");
            }
            
            return ssl; 
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
            return ssl;
        } finally {
            if(statement != null)
                statement.close();
            if(conn != null)
                conn.close();
            
            return ssl;
        }
   }
   
   public static String getKeystore(String des) throws SQLException {
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       String key = null;
       
       String querySQL = "SELECT KEYSTORE "
               + "FROM DESTINATION "
               + "WHERE UPPER(DESTINATION) = UPPER(?)";
               
        try {
            statement = conn.prepareStatement(querySQL);
            statement.setString(1,des);
            
            // execute select SQL stetement
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                key = rs.getString("KEYSTORE");
            }
            
            return key; 
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
            return key;
        } finally {
            if(statement != null)
                statement.close();
            if(conn != null)
                conn.close();
            
            return key;
        }
   }
   
   public static String getPassword(String des) throws SQLException {
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       String pass = null;
       
       String querySQL = "SELECT PASSWORD "
               + "FROM DESTINATION "
               + "WHERE UPPER(DESTINATION) = UPPER(?)";
               
        try {
            statement = conn.prepareStatement(querySQL);
            statement.setString(1,des);
            
            // execute select SQL stetement
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                pass = rs.getString("PASSWORD");
            }
            
            return pass; 
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
            return pass;
        } finally {
            if(statement != null)
                statement.close();
            if(conn != null)
                conn.close();
            
            return pass;
        }
   }
   
   public String runSQL (String SQL) throws SQLException {
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       String res = null;
       
        try {
            statement = conn.prepareStatement(SQL);
            
            // execute select SQL stetement
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                res = rs.getString(1);
            }
            
            return res;
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(statement != null)
                statement.close();
            if(conn != null)
                conn.close();
            
            return res;
        }      
   }
   
   public static String listFlow() throws SQLException {
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       String ls = "Service Flow List";
       
       String querySQL = "SELECT DISTINCT(NAME) "
               + "FROM FLOW "
               + "WHERE STATUS = 'Y'"
               + "ORDER BY NAME";
               
        try {
            statement = conn.prepareStatement(querySQL);
            
            // execute select SQL stetement
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                ls =  ls + "\n  - " + rs.getString("NAME");
            }
            
            return ls; 
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
            return ls;
        } finally {
            if(statement != null)
                statement.close();
            if(conn != null)
                conn.close();
            
            return ls;
        }
   }
   
   public static String listDes() throws SQLException {
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       String ls = "Service Flow List [-f]";
       
       String querySQL = "SELECT DISTINCT(NAME) "
               + "FROM FLOW "
               + "WHERE STATUS = 'Y'"
               + "ORDER BY NAME";
               
        try {
            statement = conn.prepareStatement(querySQL);
            
            // execute select SQL stetement
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                ls =  ls + "\n  - " + rs.getString("NAME");
            }
            
            ls = ls + "\n\n" + "Destination List [-d]";
            querySQL = "SELECT DISTINCT(DESTINATION) "
               + "FROM DESTINATION "
               + "ORDER BY DESTINATION";
               
            statement = conn.prepareStatement(querySQL);
            
            // execute select SQL stetement
            rs = statement.executeQuery();
            
            while (rs.next()) {
                ls =  ls + "\n  - " + rs.getString("DESTINATION");
            }
            
            return ls; 
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
            return ls;
        } finally {
            if(statement != null)
                statement.close();
            if(conn != null)
                conn.close();
            
            return ls;
        }
   }
   
   public static String listCom(String des) throws SQLException {
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       String ls = "Command List by " + des;
       
       String querySQL = "SELECT DISTINCT REQUEST_XML.COMMAND,REQUEST_XML.DESCRIPTION "
               + "FROM REQUEST_XML "
               + "JOIN DESTINATION "
               + "ON DESTINATION.NE = REQUEST_XML.NE "
               + "AND REQUEST_XML.STATUS = 'Y'"
               + "AND DESTINATION.DESTINATION = ? "
               + "ORDER BY REQUEST_XML.COMMAND";
               
        try {
            statement = conn.prepareStatement(querySQL);
            statement.setString(1,des);
            
            // execute select SQL stetement
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                ls =  ls + "\n  - " + rs.getString("COMMAND") + "  [" + rs.getString("DESCRIPTION") + "]";
            }
            
            return ls; 
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
            return ls;
        } finally {
            if(statement != null)
                statement.close();
            if(conn != null)
                conn.close();
            
            return ls;
        }
   }
   
   public static String avgCom (String ne,String com) throws SQLException {
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       String url = null;
       
       String querySQL = "SELECT NVL(ROUND(AVG(RESP_TIME),3),-1) AS RESP_TIME "
               + "FROM REPORT_DATA "
               + "WHERE REPORT_DATE > SYSDATE - 7 "
               + "AND NE = ? "
               + "AND COMMAND = ? "
               + "AND RESP_CODE = '200'";
       
        try {
            statement = conn.prepareStatement(querySQL);
            statement.setString(1,ne);
            statement.setString(2,com);
            
            // execute select SQL stetement
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                url = Float.toString(rs.getFloat("RESP_TIME"));
            }
            
            return url;
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(statement != null)
                statement.close();
            if(conn != null)
                conn.close();
            
            return url;
        }
   }
   
   
   public int insertReportData (Map<String, String> data) throws SQLException {
       
       Connection conn = getDBConnection();
       PreparedStatement statement = null;
       int res = 0;
       
       String insertSQL = "INSERT INTO REPORT_DATA"
               + "(REPORT_DATE,REQ_CHANNEL,DESTINATION,NE,IP,COMMAND,RESP_CODE,RESP_TIME,REST_CODE,REST_DESC) VALUES"
               + "(?,?,?,?,?,?,?,?,?,?)";
       
        try {
            
            statement = conn.prepareStatement(insertSQL);
            statement.setTimestamp(1,new java.sql.Timestamp(System.currentTimeMillis()));
            statement.setString(2,data.get("Channel"));
            statement.setString(3,data.get("DESTINATION"));
            statement.setString(4,data.get("NE"));
            statement.setString(5,data.get("IP"));
            statement.setString(6,data.get("COMMAND"));
            statement.setString(7,data.get("Response code"));
            statement.setDouble(8,Double.parseDouble(data.get("Response time")));
            statement.setString(9,data.get("Result code"));
            statement.setString(10,data.get("Result desc"));
            
            // execute select SQL stetement
            res = statement.executeUpdate();
            
            return res;
        } catch (SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(statement != null)
                statement.close();
            if(conn != null)
                conn.close();
            
            return res;
        }      
   }
   
   
    @SuppressWarnings("ConvertToTryWithResources")
   public String clobToString(Clob data) {
       
        final StringBuilder sb = new StringBuilder();
        try {    
            final Reader reader = data.getCharacterStream();
            final BufferedReader br = new BufferedReader(reader);
            
            int b;
            while(-1 != (b = br.read()))
                sb.append((char)b);
            
            br.close();
        } catch (IOException | SQLException ex) {
            Logger.getLogger(OracleJDBC.class.getName()).log(Level.SEVERE, null, ex);
            return ex.toString();
        }
        
       return sb.toString();
   }
   
}
