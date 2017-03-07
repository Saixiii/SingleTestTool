package com.truemove.msoc.stt;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.text.StrSubstitutor;


/**
 * @Author  : Suphakit Annoppornchai [Saixiii]
 * @Project : stt
 * @Class   : engine
 * @Date    : Oct 19, 2015 12:19:39 PM
 */

public class CoreEngine {
    
    private static final OracleJDBC OracleJDBC = new OracleJDBC();
    private static final httpClient httpClient = new httpClient();
    private static final Map<String,String> configENGINE = OracleJDBC.loadConfig("ENGINE");
    private static final Map<String,String> configXML = OracleJDBC.loadConfig("XML");
    private static final Map<String,String> configDATE = OracleJDBC.loadConfig("DATE");
    private static final Map<String,String> configSQL = OracleJDBC.loadConfig("SQL");
    
    public static String bindXML (String XML) throws SQLException {
        
        Map<String,String> bindvar = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : configDATE.entrySet()) {
            bindvar.put(entry.getKey(),dateformat(entry.getValue()));
        }
        
        for (Map.Entry<String, String> entry : configSQL.entrySet()) {
            bindvar.put(entry.getKey(),OracleJDBC.runSQL(entry.getValue()));
        }
        
        bindvar.putAll(configXML);
        
        StrSubstitutor sub = new StrSubstitutor(bindvar);
        XML = sub.replace(XML);
        
        return XML;
    }
    
    private static String dateformat(String format) {
        
        
        DateFormat dateFormat = new SimpleDateFormat();
        try {
            dateFormat = new SimpleDateFormat(format);
        } catch (IllegalArgumentException ex) {
            dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        }
        
        Date date = new Date();
        
        return dateFormat.format(date);
    }
    
    public static List<Map<String,String>> resultAPI (List<Map<String,String>> tvlist,String channel) throws SQLException {
        
        List<Map<String,String>> reslist = new ArrayList();
        
        Iterator<Map<String,String>> tv = tvlist.iterator();
        while (tv.hasNext()) {
            
            Map<String, String> res = tv.next();
            res.put("Channel",channel);
            
            String SSL = res.get("SSL");
            String URL = res.get("IP") + res.get("CONTEXT_ROOT");
            String XML = bindXML(res.get("XML"));
            
            if(SSL.equals("Y")) {
                String KEY  = res.get("KEYSTORE");
                String PASS = res.get("PASSWORD");
                try {
                    res.putAll(httpClient.httpsreq("https://" + URL,XML,KEY,PASS));
                } catch (Exception ex) {
                    Logger.getLogger(CoreEngine.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    res.putAll(httpClient.httpreq("http://" + URL,XML));
                } catch (Exception ex) {
                    Logger.getLogger(CoreEngine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if("200".equals(res.get("Response code"))) {
                res.put("Result code",httpClient.parserXML(res.get("Response body"),res.get("PATH_CODE")));
                res.put("Result desc",httpClient.parserXML(res.get("Response body"),res.get("PATH_DESC")));
            }
            res.put("Avg time", OracleJDBC.avgCom(res.get("NE"),res.get("COMMAND")));
            
            int row = OracleJDBC.insertReportData(res);
            reslist.add(res);
        }
        
        return reslist;
    }
    
    public static void printLineAPI (List<Map<String,String>> reslist) {
        
        Iterator<Map<String,String>> resit = reslist.iterator();
        String fielddata = configENGINE.get("LinePrint");
        List<String> linedata = Arrays.asList(fielddata.split("\\|"));
        String delimeter = configENGINE.get("LineDelimeter");
        
        System.out.println(fielddata);
        while (resit.hasNext()) {
            Map<String,String> res = resit.next();
            Iterator<String> data = linedata.iterator();
            String prtdata = res.get(data.next());
            while (data.hasNext()) {
                prtdata = prtdata + delimeter + res.get(data.next());
                
            }
            System.out.println(prtdata);
        }
        
    }
    
    public static void printDetailAPI (List<Map<String,String>> reslist) {
        
        Iterator<Map<String,String>> resit = reslist.iterator();
        List<String> detaildata = Arrays.asList(configENGINE.get("DetailPrint").split("\\|"));
        String delimeter = configENGINE.get("DetailDelimeter");
        while (resit.hasNext()) {
            Map<String,String> res = resit.next();
            Iterator<String> data = detaildata.iterator();
            String prtdata = "";
            while (data.hasNext()) {
                String key   = data.next();
                String value = res.get(key);
                prtdata = prtdata + key + " " + delimeter + " " + value + "\n";
            }
            prtdata = prtdata + "========================";
            System.out.println(prtdata);
        }
    }
    
    public static void printBotAPI (List<Map<String,String>> reslist) {
        
        Iterator<Map<String,String>> resit = reslist.iterator();
        List<String> detaildata = Arrays.asList(configENGINE.get("BotPrint").split("\\|"));
        String delimeter = configENGINE.get("BotDelimeter");
        while (resit.hasNext()) {
            Map<String,String> res = resit.next();
            Iterator<String> data = detaildata.iterator();
            String prtdata = "";
            while (data.hasNext()) {
                String key   = data.next();
                String value = res.get(key);
                prtdata = prtdata + key + " " + delimeter + " " + value + "\n";
            }
            prtdata = prtdata + "===============";
            System.out.println(prtdata);
        }
    }
    
    
    public static void main (String args[]) throws SQLException {
        
        
        List<Map<String,String>> tvlist  = OracleJDBC.listTV("SBM3GCCPS2");
        
        //printLineAPI(resultAPI("Manual","CCP8"));
        //printLineAPI(resultAPI("Manual","PCRF-RMV","pcrfList"));
        printLineAPI(resultAPI(tvlist,"test"));
        
        //printDetailAPI(resultAPI("Manual","CCP8"));
        //printDetailAPI(resultAPI("Manual","PCRF-RMV","pcrfList"));
        //printDetailAPI(resultAPI("Manual","SBM_3GCCP-S2"));
    }

}
