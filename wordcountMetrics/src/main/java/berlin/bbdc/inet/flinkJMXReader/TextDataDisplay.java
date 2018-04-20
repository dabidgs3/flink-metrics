package berlin.bbdc.inet.flinkJMXReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
/**
 * Created by dgu on 13.01.17.
 */
public class TextDataDisplay {

    //             \* A logger for this class.
    private static final Logger LOG =
            Logger.getLogger(TextDataDisplay.class.getName());

// Creates a new instance of TextDataDisplay \*/
    public TextDataDisplay() {
    }

    public String display(String name, Object data) {
        return display("",name,data);
    }

    public String display(String prefix, String name, Object data) {
        if (name == null)
            throw new IllegalArgumentException("name must not be null");
        final StringBuffer buffer = new StringBuffer();
        return write(buffer,"",name,data).toString();
    }

    public StringBuffer write(StringBuffer buffer, String prefix,
                              String name, Object data) {
        if (data == null) return writeSimple(buffer,prefix,name,null,true);
        if (data.getClass().isArray())
            return writeArray(buffer,prefix,name,data);
        if (data instanceof CompositeData)
            return writeCompositeData(buffer,prefix,name,(CompositeData)data);
        if (data instanceof TabularData)
            return writeTabularData(buffer,prefix,name,(TabularData)data);
        if (data instanceof Map)
            return writeMap(buffer,prefix,name,(Map)data);
        if (data instanceof Collection) {
            return writeArray(buffer,prefix,name,((Collection)data).toArray());
        }
        return writeSimple(buffer,prefix,name,data,true);
    }

    String toString(Object data) {
        if (data==null) return "null";
        else return data.toString();
    }

    StringBuffer writeSimple(StringBuffer buffer, String prefix,
                             String name, Object data, boolean writeline) {
        buffer.append(prefix).append(name).append("=").append(toString(data));
        if (writeline) buffer.append("\\n");
        return buffer;
    }

    StringBuffer writeArray(StringBuffer buffer, String prefix,
                            String name, Object array) {
        if (array == null)
            return writeSimple(buffer,prefix,name,null,true);
        final int length = Array.getLength(array);
        for (int i=0;i<length;i++) {
            final Object data = Array.get(array,i);
            write(buffer,prefix,name+"["+i+"]",data);
        }
        return buffer;
    }

    StringBuffer writeCompositeData(StringBuffer buffer,
                                    String prefix, String name, CompositeData data) {
        if (data == null)
            return writeSimple(buffer,prefix,name,null,true);
        writeSimple(buffer,prefix,name,"CompositeData("+
                data.getCompositeType().getTypeName()+")",true);
        buffer.append(prefix).append("{").append("\\n");
        final String fieldprefix = prefix + " ";
        for (String key : data.getCompositeType().keySet()) {
            write(buffer,fieldprefix,name+"."+key,data.get(key));
        }
        buffer.append(prefix).append("}").append("\\n");
        return buffer;
    }

    StringBuffer writeTabularData(StringBuffer buffer,
                                  String prefix, String name, TabularData data) {
        if (data == null)
            return writeSimple(buffer,prefix,name,null,true);
        writeSimple(buffer,prefix,name,"TabularData("+
                data.getTabularType().getTypeName()+")",true);
        final List<String> keyNames = data.getTabularType().getIndexNames();
        final int indexCount = keyNames.size();
        for (Object keys : data.keySet()) {
            final Object[] keyValues = ((List<?>)keys).toArray();
            final StringBuilder b = new StringBuilder(name);
            b.append("[");
            for (int i=0;i<indexCount;i++) {
                if (i>0) b.append(", ");
                b.append(keyNames.get(i)+"="+keyValues[i]);
            }
            b.append("]");
            writeCompositeData(buffer,prefix,b.toString(),data.get(keyValues));
            b.append("\\n");
        }
        return buffer;
    }

    StringBuffer writeMap(StringBuffer buffer,
                          String prefix, String name, Map<Object,Object> data) {
        if (data == null)
            return writeSimple(buffer,prefix,name,null,true);
        writeSimple(buffer,prefix,name,"java.util.Map",true);
        for (Entry<Object,Object> e : data.entrySet()) {
            write(buffer,prefix,name+"["+e.getKey()+"]",e.getValue());
        }
        return buffer;
    }
}
