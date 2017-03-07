package com.truemove.msoc.stt;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map; 
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @Author  : Suphakit Annoppornchai [Saixiii]
 * @Project : stt
 * @Class   : httpClient
 * @Date    : Oct 18, 2015 2:41:01 PM
 */

public class httpClient {

    private final int timeout = 180 * 1000;
    
    
    public Map<String, String> httpreq (String url,String reqxml) throws Exception {
        
        Map<String, String> res = new HashMap<String, String>();
        
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout).build();
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        CloseableHttpResponse httpResponse = null;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", "Jakarta Commons-HttpClient/4.5.1");
        httpPost.setHeader("Content-Type", "application/xml");
        httpPost.setHeader("SOAPAction", "");
        httpPost.setHeader("Connection", "close");
        
        try {
            StringEntity xmlEntity = new StringEntity(reqxml);
            httpPost.setEntity(xmlEntity);
            long start = System.currentTimeMillis();
            httpResponse = httpClient.execute(httpPost);
            long end = System.currentTimeMillis();
            
            res.put("Request body",reqxml);
            res.put("Response code",Integer.toString(httpResponse.getStatusLine().getStatusCode()));
            res.put("Response time",Float.toString((float) (end-start)/1000));
            res.putAll(getResponseHeader(httpResponse));
            res.put("Response body",EntityUtils.toString(httpResponse.getEntity()));
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(httpClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            //Logger.getLogger(httpClient.class.getName()).log(Level.SEVERE, null, ex);
            res.put("Response code","Timeout");
            res.put("Response time","0");
        } finally {
            httpClient.close();
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
        return res;
    }
    
    public Map<String, String> httpsreq (String url,String reqxml,String trustkey,String password) throws Exception {
        
        Map<String, String> res = new HashMap<String, String>();
        
        KeyStore keyStore  = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream instream = new FileInputStream(new File(trustkey));
        try {
            keyStore.load(instream, password.toCharArray());
        } finally {
            instream.close();
        }
        
        
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, password.toCharArray())
                .loadTrustMaterial(keyStore)
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1" },
                null,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout).build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setSSLSocketFactory(sslsf)
                .build();
        CloseableHttpResponse httpResponse = null;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", "Jakarta Commons-HttpClient/4.5.1");
        httpPost.setHeader("Content-Type", "text/xml");
        httpPost.setHeader("SOAPAction", "");
        httpPost.setHeader("Connection", "close");
        
        try {
            StringEntity xmlEntity = new StringEntity(reqxml);
            httpPost.setEntity(xmlEntity);
            long start = System.currentTimeMillis();
            httpResponse = httpClient.execute(httpPost);
            long end = System.currentTimeMillis();
            
            res.put("Request body",reqxml);
            res.put("Response code",Integer.toString(httpResponse.getStatusLine().getStatusCode()));
            res.put("Response time",Float.toString((float) (end-start)/1000));
            res.putAll(getResponseHeader(httpResponse));
            res.put("Response body",EntityUtils.toString(httpResponse.getEntity()));
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(httpClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            //Logger.getLogger(httpClient.class.getName()).log(Level.SEVERE, null, ex);
            res.put("Response code","Timeout");
            res.put("Response time","0");
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
            httpClient.close();
        }
        return res;
    }
    
    private static Map<String, String> getResponseHeader (CloseableHttpResponse httpResponse) {
        
        Map<String, String> httpheader = new HashMap<String, String>();
        Header[] headers = httpResponse.getAllHeaders();
        for (Header header : headers) {
            httpheader.put(header.getName(),header.getValue()); 
        }
        return httpheader;
    }
    
    public static Object convertNodesFromXml(String xml) throws Exception {
        
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(is);
        return createMap(document.getDocumentElement());
        
    }
    
    public static Object createMap(Node node) {
        
        Map<String, Object> map = new HashMap<String, Object>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            String name = currentNode.getNodeName();
            Object value = null;
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                value = createMap(currentNode);
            } else if (currentNode.getNodeType() == Node.TEXT_NODE) {
                return currentNode.getTextContent();
            }
            if (map.containsKey(name)) {
                Object os = map.get(name);
                if (os instanceof List) {
                    ((List<Object>)os).add(value);
                } else {
                    List<Object> objs = new LinkedList<Object>();
                    objs.add(os);
                    objs.add(value);
                    map.put(name, objs);
                }
            } else {
                map.put(name, value);
            }
        }
        return map;
    }
    
    public static String parserXML(String xml,String path) {
        
        String val = null;
        
        try {
            SAXReader reader = new SAXReader();
            org.dom4j.Document doc = reader.read(new StringReader(xml));
            
            XPath xpath = new Dom4jXPath(path);
            val = String.valueOf(((Element) xpath.selectSingleNode(doc)).getData());
        } catch (DocumentException | JaxenException ex) {
            Logger.getLogger(httpClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return val;
        }
        
    }
    
}
