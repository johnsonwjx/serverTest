package com.johnson.utils;


import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Stream;


public class V6ServiceInvoker {


    private static Charset GBK_CHARSET = Charset.forName("GBK");

    public static void loginSystem(String username, String password, String url, String proxy) throws Exception {
        InetAddress localHost = InetAddress.getLocalHost();
        Map<String, String> sendTab = new HashMap<>();
        sendTab.put("service", "useraccess.login");
        sendTab.put("registerName", username);
        sendTab.put("password", password);
        sendTab.put("ip", localHost.getHostAddress());
        sendTab.put("computerName", localHost.getHostName());
        sendTab.put("keyNum", "");
        sendTab.put("ptype", "client");
        //UserAccess服务地址为空的时候报错
        if (Strings.isNullOrEmpty(System.getProperty("useraccess")))
            throw new Exception("验证用户失败！身份认证服务没有启用！");

        Map<String, String> reTab = httpGetMap(sendTab);
        //对结果进行解析
        if (reTab == null) {
            throw new Exception("验证用户失败！服务返回信息为空！");
        }
        String assID = reTab.get("sysAccessID");
        if (Strings.isNullOrEmpty(assID)) {
            throw new Exception("验证用户失败!验证用户权限服务返回消息为空！");
        }
        //将系统访问sysAccessID写入系统变量中
        System.setProperty("sysAccessID", assID);


        sendTab.clear();
        sendTab.put("service", "useraccess.getUserID");
        sendTab.put("sysAccessID", assID);

        reTab = httpGetMap(sendTab);
        String userID = reTab.get("userID");
        if (!Strings.isNullOrEmpty(userID)) {
            //根据系统存储ID取出用户ID,写入系统变量中
            System.setProperty("userID", userID);
        }
        //登录成功，
        //保存服务器地址
        System.setProperty("yf_service_url", url);
        //保存用户名
        System.setProperty("yf_username", username);
        //保存是否使用web代理
        System.setProperty("yf_webproxy", proxy);
        Properties config = ConfigHandler.INSTANCE.getConfig();
        config.setProperty("url", url);
        config.setProperty("username", username);
        config.setProperty("proxy", proxy);
        ConfigHandler.INSTANCE.saveConfig();
    }


    /**
     * 更具地址获取所有服务
     *
     * @param url      地址
     * @param webproxy 启用代理
     * @return 所有服务list对象
     * @throws Exception 调用服务错误
     */
    public static List<MyBean> getServices(String url, boolean webproxy) throws Exception {
        String param = "service:=system.poperties.serviceslocation\nxml:=\n";
        String reMsg = httpGetEncoding(url, param, GBK_CHARSET, webproxy);
        if (Strings.isNullOrEmpty(reMsg)) {
            throw new IllegalAccessException("服务注册失败");
        }
        String xml = v6String2Map(reMsg).get("Xml");
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader xmlEventReader = factory.createXMLEventReader(new StringReader(xml));
        List<MyBean> serviceList = new ArrayList<>(20);
        Stack<MyBean> myBeanStack = new Stack<>();
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            switch (xmlEvent.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    StartElement startElement = xmlEvent.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();
                    if ("service".equals(startElementName)) {
                        MyBean bean = new MyBean(3, "cnname", "name");
                        for (String key : new String[]{"name", "cnname", "addr"}) {
                            String value = startElement.getAttributeByName(new QName(key)).getValue();
                            bean.put(key, value);
                        }
                        bean.updateToString();
                        myBeanStack.push(bean);
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    EndElement endElement = xmlEvent.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();
                    if ("service".equals(endElementName)) {
                        serviceList.add(myBeanStack.pop());
                    }
                    break;
                case XMLEvent.CHARACTERS:
                    break;
            }
        }

        serviceList.sort((o1, o2) -> {
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            String name1 = o1.get("name");
            String name2 = o2.get("name");
            if (Strings.isNullOrEmpty(name1)) {
                return -1;
            }
            if (Strings.isNullOrEmpty(name2)) {
                return 1;
            }
            return name1.compareTo(name2);
        });
        return serviceList;
    }

    public static String httpGetEncoding(String url, String in, Charset encoding, boolean useWebProxy) throws IOException {
        if (!url.toLowerCase().startsWith("http://"))
            url = "http://" + url;

        if (useWebProxy)
            url = url + "/webproxy";
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            URLConnection client = new URL(url).openConnection();
            client.setDoOutput(true);
            outputStream = client.getOutputStream();
            client.getOutputStream().write(in.getBytes());
            client.getOutputStream().flush();
            client.getOutputStream().close();
            int dataLen = client.getContentLength();
            if (dataLen <= 0) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            inputStream = client.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, encoding));
            Stream<String> lines = bufferedReader.lines();
            lines.parallel().forEach(s -> sb.append(s).append("\n"));
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                outputStream.close();
            }
        }
    }


    public static String httpGet(String in) throws IOException {
        String serviceReg = "service:=";
        int serviceIndex = in.indexOf(serviceReg);
        if (serviceIndex == -1) {
            throw new IllegalArgumentException("service参数为空");
        }
        String serviceName = in.substring(serviceIndex + serviceReg.length(), in.indexOf(".", serviceIndex + serviceReg.length()));
        String url = System.getProperty(serviceName);
        if (Strings.isNullOrEmpty(url)) {
            throw new IllegalStateException(serviceName + "服务没安装");
        }
        return httpGetEncoding(url, in, GBK_CHARSET, false);
    }

    public static Map<String, String> httpGetMap(Map<String, String> paramMap) throws IOException {
        String param = Joiner.on("\n").withKeyValueSeparator(":=").join(paramMap);
        return httpGetMap(param);
    }

    public static Map<String, String> httpGetMap(String in) throws IOException {
        return v6String2Map(httpGet(in));
    }

    public static Map<String, String> v6String2Map(String string) {
        if (Strings.isNullOrEmpty(string)) {
            return null;
        }
        return Splitter.on("\n").trimResults().withKeyValueSeparator(Splitter.on(":=").trimResults()).split(string);
    }
}
