package sg.ncs.kp.admin.config;

import com.mysql.cj.util.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * @className LdapSocketFactory
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-19
 */
@Component
@Slf4j
public class LdapSocketFactory extends SocketFactory {

    private static LdapSocketFactory instance = null;
    private SSLContext sslContext = null;
    private static ApplicationContext applicationContext;

    public static SocketFactory getDefault() {

        try {
            instance = new LdapSocketFactory();
            instance.initFactory();
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("Returning null socket factory");
        }

        return instance;
    }

    private void initFactory() throws Exception {
        log.debug("Initializing socket factory...");
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Environment env = applicationContext.getEnvironment();
        String jks = env.getProperty("ad.jks");
        String jksPassword = env.getProperty("ad.jksPassword");
        KeyStore trustStore = KeyStore.getInstance("pkcs12");
        try (InputStream is = ResourceUtils.getURL(jks).openStream()) {
            trustStore.load(is, jksPassword.toCharArray());
            // 创建SSL上下文
            sslContext = SSLContext.getInstance("TLS");

            // 设置信任的证书
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            X509TrustManager trustManager = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];

            // 设置密码
            //KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            //keyManagerFactory.init(trustStore, jksPassword.toCharArray());

            // 初始化SSL上下文
            sslContext.init(null, new TrustManager[] { trustManager }, new SecureRandom());
        } catch (Exception e) {
            log.error("truststore password was incorrect! this will cause \"keystore password was incrrect\" please check your truststore password!");
        }
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException,
            UnknownHostException {
        // TODO Auto-generated method stub
        return sslContext.getSocketFactory().createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        // TODO Auto-generated method stub
        return sslContext.getSocketFactory().createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        // TODO Auto-generated method stub
        return sslContext.getSocketFactory().createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
                               int localPort) throws IOException {
        // TODO Auto-generated method stub
        return sslContext.getSocketFactory().createSocket(address, port, localAddress, localPort);
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        LdapSocketFactory.applicationContext = applicationContext;
    }
}
