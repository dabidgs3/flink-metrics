package berlin.bbdc.inet.flinkReporter;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.ScheduledReporter;
import org.apache.flink.api.common.JobID;
import org.apache.flink.configuration.ConfigConstants;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.dropwizard.ScheduledDropwizardReporter;
import org.apache.flink.dropwizard.metrics.DropwizardMeterWrapper;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.Meter;
import org.apache.flink.metrics.MetricConfig;
import org.apache.flink.metrics.SimpleCounter;
import org.apache.flink.metrics.reporter.MetricReporter;
import org.apache.flink.runtime.metrics.MetricRegistry;
import org.apache.flink.runtime.metrics.MetricRegistryConfiguration;
import org.apache.flink.runtime.metrics.groups.TaskManagerJobMetricGroup;
import org.apache.flink.runtime.metrics.groups.TaskManagerMetricGroup;
import org.apache.flink.runtime.metrics.groups.TaskMetricGroup;
import org.apache.flink.util.AbstractID;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by dgu on 12.02.17.
 */
public class FileFlinkReporterTest {
    @Test
    public void notifyOfAddedMetric() throws Exception {
        Configuration configuration = new Configuration();
        String taskName = "test\"Ta\"..sk";
        String jobName = "testJ\"ob:-!ax..?";
        String hostname = "loc<>al\"::host\".:";
        String taskManagerId = "tas:kMana::ger";
        String counterName = "testCounter";

        configuration.setString(ConfigConstants.METRICS_REPORTERS_LIST, "test");
        configuration.setString(
                ConfigConstants.METRICS_REPORTER_PREFIX + "test." + ConfigConstants.METRICS_REPORTER_CLASS_SUFFIX,
                "berlin.bbdc.inet.flinkReporter.FileFlinkReporterTest$FileFlinkReporterTest");

        configuration.setString(ConfigConstants.METRICS_SCOPE_NAMING_TASK, "<host>.<tm_id>.<job_name>");
        configuration.setString(ConfigConstants.METRICS_SCOPE_DELIMITER, "_");

        MetricRegistryConfiguration metricRegistryConfiguration = MetricRegistryConfiguration.fromConfiguration(configuration);

        MetricRegistry metricRegistry = new MetricRegistry(metricRegistryConfiguration);

        char delimiter = metricRegistry.getDelimiter();

        TaskManagerMetricGroup tmMetricGroup = new TaskManagerMetricGroup(metricRegistry, hostname, taskManagerId);
        TaskManagerJobMetricGroup tmJobMetricGroup = new TaskManagerJobMetricGroup(metricRegistry, tmMetricGroup, new JobID (), jobName);
        TaskMetricGroup taskMetricGroup = new TaskMetricGroup(metricRegistry, tmJobMetricGroup, new AbstractID (), new AbstractID(), taskName, 0, 0);

        SimpleCounter myCounter = new SimpleCounter();
        com.codahale.metrics.Meter dropwizardMeter = new com.codahale.metrics.Meter();
        DropwizardMeterWrapper meterWrapper = new DropwizardMeterWrapper(dropwizardMeter);

        taskMetricGroup.counter(counterName, myCounter);
        taskMetricGroup.meter("meter", meterWrapper);

        List<MetricReporter> reporters = metricRegistry.getReporters();

        assertTrue(reporters.size() == 1);
        MetricReporter metricReporter = reporters.get(0);

        assertTrue("Reporter should be of type ScheduledDropwizardReporter", metricReporter instanceof ScheduledDropwizardReporter);

        TestingScheduledFileReporter reporter = (TestingScheduledFileReporter) metricReporter;

        Map<Counter, String> counters = reporter.getCounters();
        assertTrue(counters.containsKey(myCounter));

        Map<Meter, String> meters = reporter.getMeters();
        assertTrue(meters.containsKey(meterWrapper));

        String expectedCounterName = reporter.filterCharacters(hostname)
                + delimiter
                + reporter.filterCharacters(taskManagerId)
                + delimiter
                + reporter.filterCharacters(jobName)
                + delimiter
                + reporter.filterCharacters(counterName);

        assertEquals(expectedCounterName, counters.get(myCounter));

        metricRegistry.shutdown();
    }

    public static class TestingScheduledFileReporter extends FileFlinkReporter {


        public CsvReporter getReporter(MetricConfig config) {
            return null;
        }

        @Override
        public void close() {
            // don't do anything
        }
    }

}