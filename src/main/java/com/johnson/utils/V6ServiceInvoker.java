package com.johnson.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import youngfriend.common.util.net.ServiceInvokerUtil;
import youngfriend.common.util.net.exception.ServiceInvokerException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by johnson on 15/06/2017.
 */
public class V6ServiceInvoker {


    private static Charset GBK_CHARSET = Charset.forName("GBK");

    public static void loginSystem(String... params) throws Exception {
        String username = params[0];
        String password = params[1];
        String url = params[2];
        String proxy = params[3];
        InetAddress localHost = InetAddress.getLocalHost();
        Hashtable<String, String> sendTab = new Hashtable<String, String>();
        sendTab.put("service", "useraccess.login");
        sendTab.put("registerName", username);
        sendTab.put("password", password);
        sendTab.put("ip", localHost.getHostAddress());
        sendTab.put("computerName", localHost.getHostName());
        sendTab.put("keyNum", "");
        sendTab.put("ptype", "client");
        //UserAccess服务地址为空的时候报错
        if (Strings.isNullOrEmpty(System.getProperty("useraccess")))
            throw new ServiceInvokerException(V6ServiceInvoker.class, "验证用户失败！", "身份认证服务没有启用！");

        Hashtable<String, String> reTab = ServiceInvokerUtil.invoker(sendTab);
        //对结果进行解析
        if (reTab.isEmpty()) {
            throw new ServiceInvokerException(V6ServiceInvoker.class, "验证用户失败！", "服务返回信息为空！");
        }
        String assID = reTab.get("sysAccessID");
        if (Strings.isNullOrEmpty(assID)) {
            throw new ServiceInvokerException(V6ServiceInvoker.class, "验证用户失败!", "验证用户权限服务返回消息为空！");
        }
        //将系统访问sysAccessID写入系统变量中
        System.setProperty("sysAccessID", assID);

        Hashtable<String, String> sendTab2 = new Hashtable<String, String>();
        sendTab2.put("service", "useraccess.getUserID");
        sendTab2.put("sysAccessID", assID);

        Hashtable<String, String> reTab2 = ServiceInvokerUtil.invoker(sendTab2);
        String userID = reTab2.get("userID");
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
    }

    public static String serviceInvoke(Hashtable<String, String> inparam, String url, Boolean webproxy) throws Exception {
        String param = Joiner.on("\n").withKeyValueSeparator(":=").join(inparam);
        String reMsg = sendData(param, url, webproxy);//调用服务并返回值
        if (Strings.isNullOrEmpty(reMsg)) {
            throw new Exception("返回数据为空");
        } else {
            if (reMsg.startsWith("errorMessage")) {
                throw new Exception(reMsg);
            }
            reMsg = reMsg.substring(reMsg.indexOf(":=") + 2).trim();
            if (Strings.isNullOrEmpty(reMsg)) {
                throw new Exception("返回数据异常");
            }
        }
        if (reMsg.indexOf("未找到") != -1 || reMsg.indexOf("服务已停止") != -1) {
            throw new RuntimeException(reMsg);
        }
        return reMsg;
    }

    /**
     * 发送信息到服务器
     *
     * @param msgVar     消息字符串
     * @param serviceUrl 服务地址
     * @return 返回消息结果
     */
    public static String sendData(String msgVar, String serviceUrl, boolean useWebProxy) throws Exception {
        //检验地址的合法性
        if (!serviceUrl.toLowerCase().startsWith("http://"))
            serviceUrl = "http://" + serviceUrl;

        if (useWebProxy)
            serviceUrl = serviceUrl + "/webproxy";
        String output = null;
        URL url = new URL(serviceUrl);
        URLConnection client = url.openConnection();
        client.setDoOutput(true);
        client.getOutputStream().write(msgVar.getBytes());
        client.getOutputStream().flush();
        client.getOutputStream().close();
        //client.connect();
        int dataLen = client.getContentLength();
        if (dataLen > 0) {
            byte[] data = new byte[dataLen];
            int p = 0;
            while (p < dataLen) {
                int r = client.getInputStream().read(data, p, dataLen);
                if (r < 0)
                    break;
                p = p + r;
            }
            output = new String(data, GBK_CHARSET);
        }
        return output;
    }


    public static List<MyBean> getServices(String url, boolean webproxy) throws Exception {
        Hashtable<String, String> inparam = new Hashtable<String, String>();
        inparam.put("service", "system.poperties.serviceslocation");
        inparam.put("xml", "");
        String reMsg = serviceInvoke(inparam, url, webproxy);
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader xmlEventReader = factory.createXMLEventReader(new StringReader(reMsg));
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

        Collections.sort(serviceList, (o1, o2) -> {
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
}
