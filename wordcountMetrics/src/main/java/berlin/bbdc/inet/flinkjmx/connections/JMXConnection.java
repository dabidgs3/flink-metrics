/**

 */
package berlin.bbdc.inet.flinkjmx.connections;

import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import java.io.Closeable;
import java.io.IOException;

@ToString
@ThreadSafe
public class JMXConnection implements Closeable {
	@Nullable private final JMXConnector connector;
	@Nonnull @Getter private final MBeanServerConnection mBeanServerConnection;

	public JMXConnection(@Nullable JMXConnector connector, @Nonnull MBeanServerConnection mBeanServerConnection) {
		this.connector = connector;
		this.mBeanServerConnection = mBeanServerConnection;
	}

	@Override
	public void close() throws IOException {
		if (connector != null) connector.close();
	}

	public MBeanServerConnection getMBeanServerConnection() {
		return this.mBeanServerConnection;
	}
}
