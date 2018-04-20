package berlin.bbdc.inet.flinkJMXReader;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;

/**
 * Created by dgu on 28.11.16.
 */
public class FlinkJMXClient {
    private static String port = "9012";

    public static void main(String[] args) {
        // Create an RMI connector client and
        // connect it to the RMI connector server
        //
        echo("\nCreate an RMI connector client and " + "connect it to the RMI connector server");
        //here: /home/dgu/developer/flink/flink-metrics/flink-metrics-jmx/src/main/java/org/apache/flink/metrics/jmx/JMXReporter.java
        //Important - JVM need arguments for JMX login client
        //ToDO: It shouldn't be burned code
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

        //ToDo: Implement this to update values
        //ClientListener listener = new ClientListener();
        MBeanServerConnection mbsc = null;
        try {
            mbsc = jmxc.getMBeanServerConnection();
        } catch (IOException e) {
            e.printStackTrace ( );
        }
        ObjectName loadFlink = null;
        ObjectName jobsInFlink = null;
        try {
            loadFlink = new ObjectName("org.apache.flink.jobmanager.Status.JVM.CPU.Load:host=127.0.0.1");
            jobsInFlink = new ObjectName ("org.apache.flink.taskmanager.Status.JVM.CPU.Time");
//
//            jobsInFlink = new ObjectName ("org.apache.flink.jobmanager.numRunningJobs:host=127.0.0.1");
//            org.apache.flink.taskmanager.Status.JVM.CPU.Load:host=localhost,tm_id=076b9b0d66575e82106b9e59aca3c342
        } catch (MalformedObjectNameException e) {
            e.printStackTrace ( );
        }
        String loadFlinkJVMValue = null;
        String jobsFlinkJVMValue = null;
        try {
//            MBeanAttributeInfo[] attrs = mbsc.getAttributes (loadFlink,"MBeans");
            loadFlinkJVMValue = mbsc.getAttribute(loadFlink, "Value").toString();
            jobsFlinkJVMValue = mbsc.getAttribute(jobsInFlink, "Value").toString();
            mbsc.queryNames (null,null);
        } catch (MBeanException e) {
            e.printStackTrace ( );
        } catch (AttributeNotFoundException e) {
            e.printStackTrace ( );
        } catch (InstanceNotFoundException e) {
            e.printStackTrace ( );
        } catch (ReflectionException e) {
            e.printStackTrace ( );
        } catch (IOException e) {
            e.printStackTrace ( );
        }
        echo("CPU  Jobs");
        echo(loadFlinkJVMValue+","+jobsFlinkJVMValue);
    }

    /**
     * Inner class that will handle the notifications.
     */
    public static class ClientListener implements NotificationListener {
        public void handleNotification(Notification notification,
                                       Object handback) {
            echo("\nReceived notification:");
            echo("\tClassName: " + notification.getClass().getName());
            echo("\tSource: " + notification.getSource());
            echo("\tType: " + notification.getType());
            echo("\tMessage: " + notification.getMessage());
            if (notification instanceof AttributeChangeNotification) {
                AttributeChangeNotification acn =
                        (AttributeChangeNotification) notification;
                echo("\tAttributeName: " + acn.getAttributeName());
                echo("\tAttributeType: " + acn.getAttributeType());
                echo("\tNewValue: " + acn.getNewValue());
                echo("\tOldValue: " + acn.getOldValue());
            }
        }
    }

    private static void echo(String msg) {
        System.out.println(msg);
    }
}
