package com.truemove.msoc.stt;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @Author  : Suphakit Annoppornchai [Saixiii]
 * @Project : stt
 * @Class   : flow
 * @Date    : Nov 9, 2015 3:21:55 PM
 */

public class BotFlow {
    
    private static final OracleJDBC OracleJDBC = new OracleJDBC();
    private static final CoreEngine CoreEngine = new CoreEngine();
    private static final String channel = "Bot";
    
    public static void main(String args[]) throws SQLException {
        
        String flow = null;
        
        if (args.length > 0) {
            flow = args[0];
            List<Map<String,String>> tv  = OracleJDBC.listTV(flow);
            if(!tv.isEmpty()) {
                CoreEngine.printBotAPI(CoreEngine.resultAPI(tv,channel));
            } else {
                System.out.println("Data did not found on database");
            }
        } else {
            System.out.println(OracleJDBC.listFlow());
        }
        
        
        
    }
}
