/**
 * The MIT License
 * Copyright © 2010 JmxTrans team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package berlin.bbdc.inet.flinkjmx.model.output.support;

import berlin.bbdc.inet.flinkjmx.model.OutputWriter;
import berlin.bbdc.inet.flinkjmx.model.OutputWriterAdapter;
import berlin.bbdc.inet.flinkjmx.model.Query;
import berlin.bbdc.inet.flinkjmx.model.Result;
import berlin.bbdc.inet.flinkjmx.model.Server;
import berlin.bbdc.inet.flinkjmx.model.results.BooleanAsNumberValueTransformer;
import berlin.bbdc.inet.flinkjmx.model.results.IdentityValueTransformer;
import berlin.bbdc.inet.flinkjmx.model.results.ResultValuesTransformer;

import berlin.bbdc.inet.flinkjmx.exceptions.LifecycleException;

import javax.annotation.Nonnull;

import static com.google.common.collect.FluentIterable.from;

public class ResultTransformerOutputWriter<T extends OutputWriter> extends OutputWriterAdapter {

	@Nonnull private final ResultValuesTransformer resultValuesTransformer;
	@Nonnull private final T target;

	public ResultTransformerOutputWriter(@Nonnull ResultValuesTransformer resultValuesTransformer, @Nonnull T target) {
		this.resultValuesTransformer = resultValuesTransformer;
		this.target = target;
	}

	@Override
	public void doWrite(Server server, Query query, Iterable<Result> results) throws Exception {
		target.doWrite(
				server,
				query,
				from(results).transform(resultValuesTransformer).toList());
	}

	public static <T extends OutputWriter> ResultTransformerOutputWriter<T> booleanToNumber(boolean booleanToNumber, T target) {
		if (booleanToNumber) return booleanToNumber(target);
		return identity(target);
	}

	public static <T extends OutputWriter> ResultTransformerOutputWriter<T> booleanToNumber(T target) {
		return new ResultTransformerOutputWriter<>(new ResultValuesTransformer(new BooleanAsNumberValueTransformer(1, 0)), target);
	}

	public static <T extends OutputWriter> ResultTransformerOutputWriter<T> identity(T target) {
		return new ResultTransformerOutputWriter<>(new ResultValuesTransformer(new IdentityValueTransformer()), target);
	}

	public void close() throws Exception {
		target.close();
	}

}
