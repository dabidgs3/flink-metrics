package berlin.bbdc.inet.flinkJMXReader;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.logging.Logger;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;


/**
 * Created by dgu on 14.01.17.
 */
public class JVMMBeanDataDisplay extends MBeanDataDisplay {


 //           \* A logger for this class.

    private static final Logger LOG =
            Logger.getLogger(JVMMBeanDataDisplay.class.getName());

    private final static ObjectName THREAD_MXBEAN_NAME;
    static {
        try {
            THREAD_MXBEAN_NAME =
                    new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
        } catch (Exception x) {
            throw new ExceptionInInitializerError(x);
        }
    }

     //  Creates a new instance of JVMMBeanDataDisplay

    public JVMMBeanDataDisplay(MBeanServerConnection conn) {
        super(conn);
    }


    @Override
    StringBuffer writeAttribute(StringBuffer buffer,
                                String prefix, ObjectName mbean,
                                MBeanAttributeInfo info, Object value) {
        if (THREAD_MXBEAN_NAME.equals(mbean) &&
                info.getName().equals("AllThreadIds")) {

            // Instead of displaying only the thread ids, we will
            // display the thread infos.
            final Object threadInfos;

            try {
                threadInfos = server.invoke(mbean,"getThreadInfo",
                        new Object[] { value, 1 },
                        new String[] { long[].class.getName(),
                                int.class.getName() });
            } catch (Exception ex) {
                throw new IllegalArgumentException(mbean.toString(),ex);
            }
            buffer.append(prefix).append("# ").append("AllThreadInfo")
                    .append("\\n");
            return dataDisplay.write(buffer,prefix,"AllThreadInfo",
                    threadInfos);
        } else {
            // default display.
            return super.writeAttribute(buffer,prefix,mbean,info,value);
        }
    }
}
