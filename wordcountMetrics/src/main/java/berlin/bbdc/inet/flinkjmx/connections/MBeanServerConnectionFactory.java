
package berlin.bbdc.inet.flinkjmx.connections;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;

import javax.annotation.Nonnull;
import javax.management.remote.JMXConnector;
import java.io.IOException;

public class MBeanServerConnectionFactory extends BaseKeyedPoolableObjectFactory<JmxConnectionProvider, JMXConnection> {
	@Override
	@Nonnull
	public JMXConnection makeObject(@Nonnull JmxConnectionProvider server) throws IOException {

			JMXConnector connection = server.getServerConnection();
			return new JMXConnection(connection, connection.getMBeanServerConnection());

	}

	@Override
	public void destroyObject(@Nonnull JmxConnectionProvider key, @Nonnull JMXConnection obj) throws IOException {
		obj.close();
	}
}
