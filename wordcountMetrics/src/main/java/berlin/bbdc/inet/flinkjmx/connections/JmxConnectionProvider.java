
package berlin.bbdc.inet.flinkjmx.connections;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnector;
import java.io.IOException;

/**
 * Created by dgu
 */
public interface JmxConnectionProvider {
	@JsonIgnore
	JMXConnector getServerConnection() throws IOException;

}
