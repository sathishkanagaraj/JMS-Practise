import javax.jms.ConnectionMetaData;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: sathih
 * Date: 18/9/14
 * Time: 5:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetaData {

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY,"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        properties.put(Context.PROVIDER_URL,"tcp://localhost:61616");
        properties.put(Context.SECURITY_PRINCIPAL,"system");
        properties.put(Context.SECURITY_CREDENTIALS,"manager");
        properties.put("connectionFactoryNames","QueueCF");
        try {
            Context context = new InitialContext(properties);
            QueueConnectionFactory queueCF = (QueueConnectionFactory) context.lookup("QueueCF");
            QueueConnection queueConnection = queueCF.createQueueConnection();
            ConnectionMetaData metaData = queueConnection.getMetaData();
            System.out.println("JMSVersion = " + metaData.getJMSMajorVersion()+"."+metaData.getJMSMinorVersion());
            System.out.println("JMSProviderName = " + metaData.getJMSProviderName());
            System.out.println("JMSProviderVersion = " + metaData.getProviderVersion());
            Enumeration jmsxPropertyNames = metaData.getJMSXPropertyNames();
            System.out.println("JMSProviderProperties : ");
            while (jmsxPropertyNames.hasMoreElements()) {
            System.out.println(jmsxPropertyNames.nextElement());
            }

        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
