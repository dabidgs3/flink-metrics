package berlin.bbdc.inet.flinkJMXReader;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.logging.Logger;
import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Created by dgu on 13.01.17.
 */
public class JMXFlinkQuery {
    private static String port = "9012";
    private static final Logger LOG =
            Logger.getLogger(JMXFlinkQuery.class.getName());
    public final static String separator = "*******";

    public static void displayAll(MBeanServerConnection conn,
                                  ObjectName pattern) throws IOException, JMException {
        final JVMMBeanDataDisplay display = new JVMMBeanDataDisplay(conn);
        System.out.println(separator);
        for (ObjectName mbean : conn.queryNames(pattern,null)) {
            System.out.println(display.toString(mbean));
            System.out.println(separator);
        }
    }

    public static void main(String[] args) throws Exception {

        String serviceUrl = "service:jmx:rmi://localhost:" + port + "/jndi/rmi://localhost:" + port + "/jmxrmi";
        JMXServiceURL url;
        JMXConnector jmxc = null;
        try {
            url = new JMXServiceURL(serviceUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed service url created " + serviceUrl, e);
        }
        //ToDo not single catch
        try {
            jmxc = JMXConnectorFactory.connect(url, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
//        Set<ObjectInstance> instances = server.queryMBeans(null, null);
//
//        Iterator<ObjectInstance> iterator = instances.iterator();

//        final MBeanServerConnection conn = ManagementFactory.getPlatformMBeanServer();
//        while (iterator.hasNext()) {
//            ObjectInstance instance = iterator.next();
//            System.out.println("MBean Found:");
//            System.out.println("Class Name:" + instance.getClassName());
//            System.out.println("Object Name:" + instance.getObjectName());
//            System.out.println("****************************************");
//            displayAll(conn,instance.getObjectName ());
//        }
        final MBeanServerConnection conn = jmxc.getMBeanServerConnection();
//        displayAll(conn,new ObjectName("java.nio:*"));
//        displayAll(conn,new ObjectName("org.apache.flink.jobmanager.Status.JVM.CPU.Load:host=127.0.0.1"));
//        displayAll(conn,new ObjectName("org.apache.flink.taskmanager.Status.JVM.CPU.Load:host=localhost,tm_id=076b9b0d66575e82106b9e59aca3c342"));
        //displayAll(conn,null);
//        displayAll(conn,new ObjectName("org.apache.flink.*:*"));
        displayAll(conn,new ObjectName("*.*:*"));
//        displayAll(conn,new ObjectName("*.*:*"));
    }
}
