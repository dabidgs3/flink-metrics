/**
 * 
 */
package berlin.bbdc.inet.wordcountMetrics;

import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
/**
 * @author dgu
 *
 */
public class WikipediaAnalysis  {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//Get stream execution environment
		StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<WikipediaEditEvent> edits = executionEnvironment.addSource(new WikipediaEditsSource());
		KeyedStream<WikipediaEditEvent,String> keyedEdits = edits.keyBy(new KeySelector<WikipediaEditEvent, String>() {
			@Override
			public String getKey(WikipediaEditEvent value) throws Exception {
				return value.getUser();
			}
		});
		DataStream<Tuple2<String, Long>> result = keyedEdits
				.timeWindow(Time.seconds(5))
				.fold(new Tuple2<>("", 0L), new FoldFunction<WikipediaEditEvent, Tuple2<String,Long>>() {
					@Override
					public Tuple2<String, Long> fold(Tuple2<String, Long> acc, WikipediaEditEvent event) {
						acc.f0 = event.getUser();
						acc.f1 += event.getByteDiff();
						return acc;
					}
				});
		result.print();
		try {
			executionEnvironment.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}
}
