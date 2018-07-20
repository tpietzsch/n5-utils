/**
 *                         THE CRAPL v0 BETA 1
 *
 *
 * 0. Information about the CRAPL
 *
 * If you have questions or concerns about the CRAPL, or you need more
 * information about this license, please contact:
 *
 *    Matthew Might
 *    http://matt.might.net/
 *
 *
 * I. Preamble
 *
 * Science thrives on openness.
 *
 * In modern science, it is often infeasible to replicate claims without
 * access to the software underlying those claims.
 *
 * Let's all be honest: when scientists write code, aesthetics and
 * software engineering principles take a back seat to having running,
 * working code before a deadline.
 *
 * So, let's release the ugly.  And, let's be proud of that.
 *
 *
 * II. Definitions
 *
 * 1. "This License" refers to version 0 beta 1 of the Community
 *     Research and Academic Programming License (the CRAPL).
 *
 * 2. "The Program" refers to the medley of source code, shell scripts,
 *     executables, objects, libraries and build files supplied to You,
 *     or these files as modified by You.
 *
 *    [Any appearance of design in the Program is purely coincidental and
 *     should not in any way be mistaken for evidence of thoughtful
 *     software construction.]
 *
 * 3. "You" refers to the person or persons brave and daft enough to use
 *     the Program.
 *
 * 4. "The Documentation" refers to the Program.
 *
 * 5. "The Author" probably refers to the caffeine-addled graduate
 *     student that got the Program to work moments before a submission
 *     deadline.
 *
 *
 * III. Terms
 *
 * 1. By reading this sentence, You have agreed to the terms and
 *    conditions of this License.
 *
 * 2. If the Program shows any evidence of having been properly tested
 *    or verified, You will disregard this evidence.
 *
 * 3. You agree to hold the Author free from shame, embarrassment or
 *    ridicule for any hacks, kludges or leaps of faith found within the
 *    Program.
 *
 * 4. You recognize that any request for support for the Program will be
 *    discarded with extreme prejudice.
 *
 * 5. The Author reserves all rights to the Program, except for any
 *    rights granted under any additional licenses attached to the
 *    Program.
 *
 *
 * IV. Permissions
 *
 * 1. You are permitted to use the Program to validate published
 *    scientific claims.
 *
 * 2. You are permitted to use the Program to validate scientific claims
 *    submitted for peer review, under the condition that You keep
 *    modifications to the Program confidential until those claims have
 *    been published.
 *
 * 3. You are permitted to use and/or modify the Program for the
 *    validation of novel scientific claims if You make a good-faith
 *    attempt to notify the Author of Your work and Your claims prior to
 *    submission for publication.
 *
 * 4. If You publicly release any claims or data that were supported or
 *    generated by the Program or a modification thereof, in whole or in
 *    part, You will release any inputs supplied to the Program and any
 *    modifications You made to the Progam.  This License will be in
 *    effect for the modified program.
 *
 *
 * V. Disclaimer of Warranty
 *
 * THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY
 * APPLICABLE LAW. EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT
 * HOLDERS AND/OR OTHER PARTIES PROVIDE THE PROGRAM "AS IS" WITHOUT
 * WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND
 * PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE
 * DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR
 * CORRECTION.
 *
 *
 * VI. Limitation of Liability
 *
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
 * WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MODIFIES AND/OR
 * CONVEYS THE PROGRAM AS PERMITTED ABOVE, BE LIABLE TO YOU FOR DAMAGES,
 * INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OR INABILITY TO USE THE PROGRAM (INCLUDING BUT
 * NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED INACCURATE OR
 * LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE OF THE PROGRAM
 * TO OPERATE WITH ANY OTHER PROGRAMS), EVEN IF SUCH HOLDER OR OTHER
 * PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *
 */
package org.saalfeldlab;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.saalfeldlab.N5Factory.N5Options;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.RandomAccessibleIntervalMipmapSource;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.volatiles.AbstractVolatileNativeRealType;
import net.imglib2.type.volatiles.VolatileDoubleType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

/**
 *
 *
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 */
public class View {

	protected static class ReaderInfo {

		public final N5Reader n5;
		public final String[] groupNames;
		public final double[][] resolutions;
		public final double[][] contrastRanges;
		public final double[][] offsets;

		public ReaderInfo(
				final N5Reader n5,
				final String[] groupNames,
				final double[][] resolutions,
				final double[][] contrastRanges,
				final double[][] offsets) {

			this.n5 = n5;
			this.groupNames = groupNames;
			this.resolutions = resolutions;
			this.contrastRanges = contrastRanges;
			this.offsets = offsets;
		}
	}

	public static class Options implements Serializable {

		@Option(name = "-i", aliases = {"--container"}, required = true, usage = "container paths, e.g. -i $HOME/fib19.n5 -i /nrs/flyem ...")
		private final List<String> containerPaths = null;

		@Option(name = "-d", aliases = {"--datasets"}, required = true, usage = "comma separated list of datasets, one list per container, e.g. -d '/slab-26,slab-27' -d '/volumes/raw' ...")
		final List<String> groupLists = null;

		@Option(name = "-r", aliases = {"--resolution"}, usage = "comma separated list of scale factors, one per dataset or all following the last, e.g. -r '4,4,40'")
		final List<String> resolutionStrings = null;

		@Option(name = "-c", aliases = {"--contrast"}, usage = "comma separated contrast range, one per dataset or all following the last, e.g. -c '0,255'")
		final List<String> contrastStrings = null;

		@Option(name = "-o", aliases = {"--offset"}, usage = "comma separated list of offsets (in scaled world coordinates), one per dataset or all following the last, e.g. -o '100.0,200.0,10.0'")
		final List<String> offsetStrings = null;

		private boolean parsedSuccessfully = false;

		private final ArrayList<ReaderInfo> readerInfos = new ArrayList<>();

		protected static final boolean parseCSDoubleArray(final String csv, final double[] array) {

			final String[] stringValues = csv.split(",\\s*");
			if (stringValues.length != array.length)
				return false;
			try {
				for (int i = 0; i < array.length; ++i)
					array[i] = Double.parseDouble(stringValues[i]);
			} catch (final NumberFormatException e) {
				e.printStackTrace(System.err);
				return false;
			}
			return true;
		}

		protected static final double[] parseCSDoubleArray(final String csv) {

			final String[] stringValues = csv.split(",\\s*");
			final double[] array = new double[stringValues.length];
			try {
				for (int i = 0; i < array.length; ++i)
					array[i] = Double.parseDouble(stringValues[i]);
			} catch (final NumberFormatException e) {
				e.printStackTrace(System.err);
				return null;
			}
			return array;
		}

		protected static final double[] parseContrastRange(final String csv) {

			if (csv.equalsIgnoreCase("labels"))
				return null;
			else {
				final String[] stringValues = csv.split(",\\s*");
				final double[] array = new double[stringValues.length];
				try {
					for (int i = 0; i < array.length; ++i)
						array[i] = Double.parseDouble(stringValues[i]);
				} catch (final NumberFormatException e) {
					e.printStackTrace(System.err);
					return null;
				}
				return array;
			}
		}

		public Options(final String[] args) throws NumberFormatException, IOException {

			final CmdLineParser parser = new CmdLineParser(this);
			try {
				parser.parseArgument(args);
				double[] resolution = new double[]{1, 1, 1};
				double[] contrast = new double[]{0, 255};
				double[] offset = new double[]{0, 0, 0};
				for (int i = 0, j = 0; i < containerPaths.size(); ++i) {
					final String containerPath = containerPaths.get(i);
					final N5Reader n5 = N5Factory.createN5Reader(new N5Options(containerPath, new int[] {64}, null));
					final String[] groups = groupLists.get(i).split(",\\s*");
					final double[][] resolutions = new double[groups.length][];
					final double[][] contrastRanges = new double[groups.length][];
					final double[][] offsets = new double[groups.length][];
					for (int k = 0; k < groups.length; ++k, ++j) {
						if (resolutionStrings != null && j < resolutionStrings.size())
							resolution = parseCSDoubleArray(resolutionStrings.get(j));
						if (contrastStrings != null && j < contrastStrings.size())
							contrast = parseContrastRange(contrastStrings.get(j));
						if (offsetStrings != null && j < offsetStrings.size())
							offset = parseCSDoubleArray(offsetStrings.get(j));

						resolutions[k] = resolution.clone();
						contrastRanges[k] = contrast == null ? null : contrast.clone();
						offsets[k] = offset.clone();
					}

					readerInfos.add(new ReaderInfo(n5, groups, resolutions, contrastRanges, offsets));
				}
				parsedSuccessfully = true;
			} catch (final CmdLineException e) {
				System.err.println(e.getMessage());
				parser.printUsage(System.err);
			}
		}

		public boolean isParsedSuccessfully() {

			return parsedSuccessfully;
		}

		/**
		 * @return the readers
		 */
		public List<ReaderInfo> getReaderInfos() {

			return readerInfos;
		}

		/**
		 * @param parsedSuccessfully the parsedSuccessfully to set
		 */
		public void setParsedSuccessfully(final boolean parsedSuccessfully) {
			this.parsedSuccessfully = parsedSuccessfully;
		}
	}

	private static final double[] rs = new double[]{1, 1, 0, 0, 0, 1, 1};
	private static final double[] gs = new double[]{0, 1, 1, 1, 0, 0, 0};
	private static final double[] bs = new double[]{0, 0, 0, 1, 1, 1, 0};

	final static private double goldenRatio = 1.0 / (0.5 * Math.sqrt(5) + 0.5);

	private static final double getDouble(final long id) {

		final double x = id * goldenRatio;
		return x - (long)Math.floor(x);
	}

	private static final int interpolate(final double[] xs, final int k, final int l, final double u, final double v) {

		return (int)((v * xs[k] + u * xs[l]) * 255.0 + 0.5);
	}

	private static final int argb(final int r, final int g, final int b, final int alpha) {

		return (((r << 8) | g) << 8) | b | alpha;
	}

	private static final int argb(final long id) {

		double x = getDouble(id);
		x *= 6.0;
		final int k = (int)x;
		final int l = k + 1;
		final double u = x - k;
		final double v = 1.0 - u;

		final int r = interpolate( rs, k, l, u, v );
		final int g = interpolate( gs, k, l, u, v );
		final int b = interpolate( bs, k, l, u, v );

		return argb( r, g, b, 0xff );
	}



	@SuppressWarnings( "unchecked" )
	public static final void main(final String... args) throws IOException, InterruptedException, ExecutionException {

		final Options options = new Options(args);

		if (!options.parsedSuccessfully)
			return;

		final int numProc = Runtime.getRuntime().availableProcessors();
		final SharedQueue queue = new SharedQueue(Math.min(8, Math.max(1, numProc / 2)));
		BdvStackSource<?> bdv = null;

		int id = 0;
		for (final ReaderInfo entry : options.getReaderInfos()) {

			final N5Reader n5 = entry.n5;
			for (int i = 0; i < entry.groupNames.length; ++i) {

				final String groupName = entry.groupNames[i];
				final double[] resolution = entry.resolutions[i];
				final double[] contrast = entry.contrastRanges[i];
				final boolean isLabel = contrast == null;
				final double[] offset = entry.offsets[i];

				System.out.println(n5 + " : " + groupName + ", " + Arrays.toString(resolution) + ", " + (isLabel ? " labels " : Arrays.toString(contrast)) + ", " + (offset == null ? "dataset offset" : Arrays.toString(offset)));

				final Pair<RandomAccessibleInterval<NativeType>[], double[][]> n5Sources;
				int n;
				if (n5.datasetExists(groupName)) {
					// this works for javac openjdk 8
					final RandomAccessibleInterval<NativeType> source = (RandomAccessibleInterval)N5Utils.openVolatile(n5, groupName);
					n = source.numDimensions();
					final double[] scale = new double[n];
					Arrays.fill(scale, 1);
					n5Sources = new ValuePair<>(new RandomAccessibleInterval[] {source}, new double[][]{scale});
				}
				else {
					n5Sources = N5Utils.openMipmaps(n5, groupName, true);
					n = n5Sources.getA()[0].numDimensions();
				}

				/* make volatile */
				final RandomAccessibleInterval<NativeType>[] ras = n5Sources.getA();
				final RandomAccessibleInterval[] vras = new RandomAccessibleInterval[ras.length];
				Arrays.setAll(vras, k ->
					VolatileViews.wrapAsVolatile(
							n5Sources.getA()[k],
							queue,
							new CacheHints(LoadingStrategy.VOLATILE, 0, true)));

				/* remove 1-size dimensions */
				final double[][] scales = n5Sources.getB();
				for (int d = 0; d < n;) {
					if (ras[0].dimension(d) == 1) {
						--n;
						for (int k = 0; k < vras.length; ++k) {
							vras[k] = Views.hyperSlice(vras[k], d, 0);
							final double[] oldScale = scales[k];
							scales[k] = Arrays.copyOf(oldScale, n);
							System.arraycopy(oldScale, d + 1, scales[k], d, scales[k].length - d);
						}
					} else ++d;
				}

				final BdvOptions bdvOptions = n == 2 ? Bdv.options().is2D() : Bdv.options();

				final RandomAccessibleInterval<VolatileDoubleType>[] convertedSources = new RandomAccessibleInterval[n5Sources.getA().length];
				for (int k = 0; k < vras.length; ++k) {
					final Converter<AbstractVolatileNativeRealType<?, ?>, VolatileDoubleType> converter;
					if (isLabel)
						converter = (a, b) -> {
							b.setValid(a.isValid());
							if (b.isValid()) {
								Integer.hashCode(1);
								int x = Double.hashCode(a.get().getRealDouble());
								// hash code from https://stackoverflow.com/questions/664014/what-integer-hash-function-are-good-that-accepts-an-integer-hash-key
								x = ((x >>> 16) ^ x) * 0x45d9f3b;
								x = ((x >>> 16) ^ x) * 0x45d9f3b;
								x = (x >>> 16) ^ x;
								final double v = ((double)x / Integer.MAX_VALUE + 1) * 500.0;
								b.setReal(v);
							}
						};
					else
						converter = (a, b) -> {
							b.setValid(a.isValid());
							if (b.isValid()) {
								double v = a.get().getRealDouble();
								v -= contrast[0];
								v /= contrast[1] - contrast[0];
								v *= 1000;
								b.setReal(v);
							}
						};
					convertedSources[k] = Converters.convert(
							(RandomAccessibleInterval<AbstractVolatileNativeRealType<?, ?>>)vras[k],
							converter,
							new VolatileDoubleType());
					final double[] scale = n5Sources.getB()[k];
					Arrays.setAll(scale, j -> scale[j] * resolution[j]);
				}

				/* offset transform */
				final AffineTransform3D sourceTransform = new AffineTransform3D();
				sourceTransform.setTranslation(offset);
				System.out.println(groupName + " " + sourceTransform.toString());

				final RandomAccessibleIntervalMipmapSource<VolatileDoubleType> mipmapSource =
						new RandomAccessibleIntervalMipmapSource<>(
								convertedSources,
								new VolatileDoubleType(),
								n5Sources.getB(),
								new FinalVoxelDimensions("px", resolution),
								sourceTransform,
								groupName);

				bdv = BdvFunctions.show(
						mipmapSource,
						bdv == null ? bdvOptions : bdvOptions.addTo(bdv));
				bdv.setDisplayRange(0, 1000);
				bdv.setColor(new ARGBType(argb(id++)));
			}

			if (id == 1)
				bdv.setColor(new ARGBType(0xffffffff));
		}
	}
}
