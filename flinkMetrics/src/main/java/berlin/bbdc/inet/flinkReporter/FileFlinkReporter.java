package berlin.bbdc.inet.flinkReporter;

import com.codahale.metrics.Reporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.graphite.*;
import org.apache.flink.dropwizard.metrics.*;
import org.apache.flink.metrics.Meter;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.Histogram;
import org.apache.flink.metrics.Gauge;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.metrics.Metric;
import org.apache.flink.metrics.MetricConfig;
import org.apache.flink.metrics.CharacterFilter;
import org.apache.flink.metrics.reporter.MetricReporter;
import org.apache.flink.metrics.reporter.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;

/**
 * Created by dgu on 10.02.17.
 */
public class FileFlinkReporter implements MetricReporter, Scheduled, Reporter, CharacterFilter {

    private static final Logger LOG = LoggerFactory.getLogger(FileFlinkReporter.class);

    protected final MetricRegistry registry;
    protected CsvReporter reporter;
    protected Graphite graphite;
    protected GraphiteReporter gReporter;

    private final Map<Gauge<?>, String> gauges = new HashMap<>();
    private final Map<Counter, String> counters = new HashMap<>();
    private final Map<Histogram, String> histograms = new HashMap<>();
    private final Map<Meter, String> meters = new HashMap<>();

    private enum Protocol {
        TCP,
        UDP
    }

    // used for testing purposes
    Map<Counter, String> getCounters() {
        return counters;
    }

    Map<Meter, String> getMeters() {
        return meters;
    }

    /***
     * Constructor
     */
    public FileFlinkReporter() {
        registry = new MetricRegistry ();
    }

    @Override
    /**
     * Start reporter
     */
    public void open(MetricConfig metricConfig) {
        //Graphite configuration from flink file configuration
        //ARG_HOST ARG_PORT ARG_PREFIX ARG_CONVERSION_RATE ARG_CONVERSION_DURATION
        String host = metricConfig.getString("host", null);
        int port = metricConfig.getInteger("port", -1);
        String prefix = metricConfig.getString("prefix", null);
        String conversionRate = metricConfig.getString("rateConversion", null);
        String conversionDuration = metricConfig.getString("durationConversion", null);
        String protocol = metricConfig.getString("protocol", "TCP");
        //CSV report configuration
        String filePath = metricConfig.getString("path", "/tmp/metrics/");
        //Reporter setup
        synchronized (this) {
            reporter = CsvReporter.forRegistry (registry)
                    .formatFor (Locale.US)
                    .build (new File (filePath));
        }
        LOG.info("INET - Metrics, creating files in: {}", filePath);
        //Host:port argument validation
        if (host == null || host.length() == 0 || port < 1) {
            LOG.info("Invalid host/port configuration. Host: " + host + " Port: " + port);
        }else
        {
            graphite = new Graphite(new InetSocketAddress(host, port));
            gReporter = GraphiteReporter.forRegistry(registry).prefixedWith(prefix).build(graphite);
            LOG.info("INET - Graphite reporting to Host: " + host + " Port: " + port);
        }

    }

    @Override
    public void close() {
        this.reporter.stop();
    }

    @Override
    public void notifyOfAddedMetric(Metric metric, String s, MetricGroup metricGroup) {
        //Metric full name - namespace
        final String fullName = metricGroup.getMetricIdentifier(s, this);
        LOG.debug ("INET - Metrics - added: {}",fullName);
        synchronized (this) {
            if (metric instanceof Counter) {
                counters.put((Counter) metric, fullName);
                registry.register(fullName, new FlinkCounterWrapper ((Counter) metric));
            }else if (metric instanceof Gauge) {
                gauges.put((Gauge<?>) metric, fullName);
                registry.register(fullName, FlinkGaugeWrapper.fromGauge((Gauge<?>) metric));
            } else if (metric instanceof Histogram) {
                Histogram histogram = (Histogram) metric;
                histograms.put(histogram, fullName);

                if (histogram instanceof DropwizardHistogramWrapper) {
                    registry.register(fullName, ((DropwizardHistogramWrapper) histogram).getDropwizardHistogram());
                } else {
                    registry.register(fullName, new FlinkHistogramWrapper (histogram));
                }
            } else if (metric instanceof Meter) {
                Meter meter = (Meter) metric;
                meters.put(meter, fullName);

                if (meter instanceof DropwizardMeterWrapper) {
                    registry.register(fullName, ((DropwizardMeterWrapper) meter).getDropwizardMeter());
                } else {
                    registry.register(fullName, new FlinkMeterWrapper(meter));
                }
            } else {
                LOG.warn("Cannot add metric of type {}. This indicates that the reporter " +
                        "does not support this metric type.", metric.getClass().getName());
            }
        }
    }

    @Override
    public void notifyOfRemovedMetric(Metric metric, String s, MetricGroup metricGroup) {

        LOG.debug ("INET - Metrics - removed: {}",s);
        synchronized (this) {
            String fullName;

            if (metric instanceof Counter) {
                fullName = counters.remove(metric);
            } else if (metric instanceof Gauge) {
                fullName = gauges.remove(metric);
            } else if (metric instanceof Histogram) {
                fullName = histograms.remove(metric);
            } else {
                fullName = null;
            }

            if (fullName != null) {
                registry.remove(fullName);
            }
        }
    }

    @Override
    /***
     * replace null string in reporting
     * @s metric name
     */
    public String filterCharacters(String s) {
        char[] chars = null;
        final int strLen = s.length();
        int pos = 0;

        for (int i = 0; i < strLen; i++) {
            final char c = s.charAt(i);
            switch (c) {
                case '.':
                    if (chars == null) {
                        chars = s.toCharArray();
                    }
                    chars[pos++] = '-';
                    break;
                case '"':
                    if (chars == null) {
                        chars = s.toCharArray();
                    }
                    break;

                default:
                    if (chars != null) {
                        chars[pos] = c;
                    }
                    pos++;
            }
        }

        return chars == null ? s : new String(chars, 0, pos);
    }

    @Override
    /**
     * Method from ScheduledReporter. The abstract base class for all scheduled reporters (i.e., reporters which process a registry's
     * com.codahale.metrics
     */
    public void report() {
//        final SortedMap<String, com.codahale.metrics.Counter> countersF = registry.getCounters();

        final SortedMap<String, com.codahale.metrics.Gauge> gauges = registry.getGauges();
        final SortedMap<String, com.codahale.metrics.Counter> counters = registry.getCounters();
        final SortedMap<String, com.codahale.metrics.Histogram> histograms = registry.getHistograms();
        final SortedMap<String, com.codahale.metrics.Meter> meters = registry.getMeters();
        final SortedMap<String, com.codahale.metrics.Timer> timers = registry.getTimers();

        for (Map.Entry<String, com.codahale.metrics.Gauge> entry : gauges.entrySet()) {
            LOG.debug ("INET - Metrics - gauge: {} | {}",entry.getKey(), entry.getValue().getValue ());
        }
        for (Map.Entry<String, com.codahale.metrics.Counter> entry : counters.entrySet()) {
            LOG.debug ("INET - Metrics - counters: {} | {}",entry.getKey(), entry.getValue().getCount ());
        }
        for (Map.Entry<String, com.codahale.metrics.Histogram> entry : histograms.entrySet()) {
            LOG.debug ("INET - Metrics - Histogram [mean]: {} | {}",entry.getKey(), entry.getValue().getSnapshot ().getMean ());
        }
        for (Map.Entry<String, com.codahale.metrics.Meter> entry : meters.entrySet()) {
            LOG.debug ("INET - Metrics - Meter [Mean rate]: {} | {}",entry.getKey(), entry.getValue().getMeanRate ());
        }
        for (Map.Entry<String, com.codahale.metrics.Timer> entry : timers.entrySet()) {
            LOG.debug ("INET - Metrics - Timer [Mean rate]: {} | {}",entry.getKey(), entry.getValue().getMeanRate ());
        }
        synchronized (this) {
            try {
                this.reporter.report(gauges, counters, histograms, meters, timers);
            }catch (Exception e)
            {//To do DEBUG
                LOG.debug (e.getMessage());
                 }
            if(gReporter !=null)
                this.gReporter.report(gauges, counters, histograms, meters, timers);
        }

    }
}
