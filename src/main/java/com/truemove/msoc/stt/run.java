package com.truemove.msoc.stt;

import gnu.getopt.Getopt;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 * @Author  : Suphakit Annoppornchai [Saixiii]
 * @Project : stt
 * @Class   : run
 * @Date    : Nov 8, 2015 12:54:46 PM
 */

public class run {
    
    private static final OracleJDBC OracleJDBC = new OracleJDBC();
    private static final CoreEngine CoreEngine = new CoreEngine();
    private static final String channel = "Manual";
    
    public static void usage() {
        System.out.println("Usage: -l [Destination]");
        System.out.println("       -f [Flow]");
        System.out.println("       -d [Destination] -c [Command]");
        System.out.println("");
        System.out.println("Single test tool (API)");
        System.out.println("Mandatory arguments");
        System.out.println("  -f        For test group of service flow");
        System.out.println("  -d        For test all destination");
        System.out.println("");
        System.out.println("Optional arguments");
        System.out.println("  -h        Help");
        System.out.println("  -c        Specific command on destination");
        System.out.println("  -v        Verbose mode");
        System.out.println("  -a        List destination");
        System.out.println("  -l [Des]  List command by destination");
        System.exit(0);
    }
    
    
    public static void main(String argv[]) throws SQLException {
        
        Getopt g = new Getopt("stt", argv, ":f:d:c:l:avh");
        int c;
        boolean verbose = false;
        String flow = null,des = null,com = null;
        while ((c = g.getopt()) != -1) {
            switch(c) {
                case 'f':
                    flow = g.getOptarg().toUpperCase();
                    break;
                case 'd':
                    des = g.getOptarg().toUpperCase();
                    break;
                case 'c':
                    com = g.getOptarg().toUpperCase();
                    break;
                case 'a':
                    System.out.println(OracleJDBC.listDes());
                    System.exit(0);
                    break;
                case 'l':
                    System.out.println(OracleJDBC.listCom(g.getOptarg().toUpperCase()));
                    System.exit(0);
                    break;
                case 'v':
                    verbose = true;
                    break;
                case 'h':
                    usage();
                    break;
                case '?':
                    System.out.println("Try '-h' for more information.");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Try '-h' for more information.");
                    System.exit(0);
            }
        }
        
        List<Map<String,String>> tv  = new ArrayList<Map<String,String>>();
        
        if(StringUtils.isNotBlank(flow) && StringUtils.isBlank(des)) {
            tv  = OracleJDBC.listTV(flow);
        } else if(StringUtils.isBlank(flow) && StringUtils.isNotBlank(des)) {
            if(StringUtils.isBlank(com)) {
                tv  = OracleJDBC.getTV(des);
            } else {
                tv  = OracleJDBC.getTV(des,com);
            }
        } else {
            usage();
        }
        
        if(!tv.isEmpty()) {
            if(verbose)
                CoreEngine.printDetailAPI(CoreEngine.resultAPI(tv,channel));
            else
                CoreEngine.printLineAPI(CoreEngine.resultAPI(tv,channel));
        } else {
            System.out.println("Data did not found on database");
        }
        
    }
    
}
