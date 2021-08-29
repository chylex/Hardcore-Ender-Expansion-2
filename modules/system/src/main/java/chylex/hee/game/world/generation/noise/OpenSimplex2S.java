package chylex.hee.game.world.generation.noise;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * OpenSimplex2S (SuperSimplex) with area generators.
 * Taken from https://github.com/KdotJPG/OpenSimplex2 (unlicense).
 */
public final class OpenSimplex2S {
	
	private static final int PSIZE = 2048;
	private static final int PMASK = 2047;
	
	private final short[] perm;
	private final Grad2[] permGrad2;
	private final Grad3[] permGrad3;
	
	public OpenSimplex2S(long seed) {
		perm = new short[PSIZE];
		permGrad2 = new Grad2[PSIZE];
		permGrad3 = new Grad3[PSIZE];
		final short[] source = new short[PSIZE];
		for (short i = 0; i < PSIZE; i++) {
			source[i] = i;
		}
		for (int i = PSIZE - 1; i >= 0; i--) {
			seed = seed * 6364136223846793005L + 1442695040888963407L;
			int r = (int)((seed + 31) % (i + 1));
			if (r < 0) {
				r += (i + 1);
			}
			perm[i] = source[r];
			permGrad2[i] = GRADIENTS_2D[perm[i]];
			permGrad3[i] = GRADIENTS_3D[perm[i]];
			source[r] = source[i];
		}
	}
	
	/*
	 * Traditional evaluators
	 */
	
	/**
	 * 2D SuperSimplex noise, standard lattice orientation.
	 */
	public double noise2(final double x, final double y) {
		
		// Get points for A2* lattice
		final double s = 0.366025403784439 * (x + y);
		final double xs = x + s;
		final double ys = y + s;
		
		return noise2_Base(xs, ys);
	}
	
	/**
	 * 2D SuperSimplex noise, with Y pointing down the main diagonal.
	 * Might be better for a 2D sandbox style game, where Y is vertical.
	 * Probably slightly less optimal for heightmaps or continent maps.
	 */
	public double noise2_XBeforeY(final double x, final double y) {
		
		// Skew transform and rotation baked into one.
		final double xx = x * 0.7071067811865476;
		final double yy = y * 1.224744871380249;
		
		return noise2_Base(yy + xx, yy - xx);
	}
	
	/**
	 * 2D SuperSimplex noise base.
	 * Lookup table implementation inspired by DigitalShadow.
	 */
	private double noise2_Base(final double xs, final double ys) {
		double value = 0;
		
		// Get base points and offsets
		final int xsb = fastFloor(xs);
		final int ysb = fastFloor(ys);
		final double xsi = xs - xsb;
		final double ysi = ys - ysb;
		
		// Index to point list
		final int a = (int)(xsi + ysi);
		final int index =
			(a << 2) |
				(int)(xsi - ysi / 2 + 1 - a / 2.0) << 3 |
				(int)(ysi - xsi / 2 + 1 - a / 2.0) << 4;
		
		final double ssi = (xsi + ysi) * -0.211324865405187;
		final double xi = xsi + ssi;
		final double yi = ysi + ssi;
		
		// Point contributions
		for (int i = 0; i < 4; i++) {
			final LatticePoint2D c = LOOKUP_2D[index + i];
			
			final double dx = xi + c.dx;
			final double dy = yi + c.dy;
			double attn = 2.0 / 3.0 - dx * dx - dy * dy;
			if (attn <= 0) {
				continue;
			}
			
			final int pxm = (xsb + c.xsv) & PMASK;
			final int pym = (ysb + c.ysv) & PMASK;
			final Grad2 grad = permGrad2[perm[pxm] ^ pym];
			final double extrapolation = grad.dx * dx + grad.dy * dy;
			
			attn *= attn;
			value += attn * attn * extrapolation;
		}
		
		return value;
	}
	
	/**
	 * 3D Re-oriented 8-point BCC noise, classic orientation
	 * Proper substitute for what 3D SuperSimplex would be,
	 * in light of Forbidden Formulae.
	 * Use noise3_XYBeforeZ or noise3_XZBeforeY instead, wherever appropriate.
	 */
	public double noise3_Classic(final double x, final double y, final double z) {
		
		// Re-orient the cubic lattices via rotation, to produce the expected look on cardinal planar slices.
		// If texturing objects that don't tend to have cardinal plane faces, you could even remove this.
		// Orthonormal rotation. Not a skew transform.
		final double r = (2.0 / 3.0) * (x + y + z);
		final double xr = r - x;
		final double yr = r - y;
		final double zr = r - z;
		
		// Evaluate both lattices to form a BCC lattice.
		return noise3_BCC(xr, yr, zr);
	}
	
	/**
	 * 3D Re-oriented 8-point BCC noise, with better visual isotropy in (X, Z).
	 * Recommended for 3D terrain and time-varied animations.
	 * The Y coordinate should always be the "different" coordinate in your use case.
	 * If Y is vertical in world coordinates, call noise3_XZBeforeY(x, Y, z).
	 * If Z is vertical in world coordinates, call noise3_XZBeforeY(x, Z, y) or use noise3_XYBeforeZ.
	 * For a time varied animation, call noise3_XZBeforeY(x, T, y) or use noise3_XYBeforeZ.
	 */
	public double noise3_XZBeforeY(final double x, final double y, final double z) {
		
		// Re-orient the cubic lattices without skewing, to make X and Z triangular like 2D.
		// Orthonormal rotation. Not a skew transform.
		final double xz = x + z;
		final double s2 = xz * -0.211324865405187;
		final double yy = y * 0.577350269189626;
		final double xr = x + s2 - yy;
		final double zr = z + s2 - yy;
		final double yr = xz * 0.577350269189626 + yy;
		
		// Evaluate both lattices to form a BCC lattice.
		return noise3_BCC(xr, yr, zr);
	}
	
	/**
	 * Generate overlapping cubic lattices for 3D Re-oriented BCC noise.
	 * Lookup table implementation inspired by DigitalShadow.
	 * It was actually faster to narrow down the points in the loop itself,
	 * than to build up the index with enough info to isolate 8 points.
	 */
	private double noise3_BCC(final double xr, final double yr, final double zr) {
		
		// Get base and offsets inside cube of first lattice.
		final int xrb = fastFloor(xr);
		final int yrb = fastFloor(yr);
		final int zrb = fastFloor(zr);
		final double xri = xr - xrb;
		final double yri = yr - yrb;
		final double zri = zr - zrb;
		
		// Identify which octant of the cube we're in. This determines which cell
		// in the other cubic lattice we're in, and also narrows down one point on each.
		final int xht = (int)(xri + 0.5);
		final int yht = (int)(yri + 0.5);
		final int zht = (int)(zri + 0.5);
		final int index = (xht << 0) | (yht << 1) | (zht << 2);
		
		// Point contributions
		double value = 0;
		LatticePoint3D c = LOOKUP_3D[index];
		while (c != null) {
			final double dxr = xri + c.dxr;
			final double dyr = yri + c.dyr;
			final double dzr = zri + c.dzr;
			double attn = 0.75 - dxr * dxr - dyr * dyr - dzr * dzr;
			if (attn < 0) {
				c = c.nextOnFailure;
			}
			else {
				final int pxm = (xrb + c.xrv) & PMASK;
				final int pym = (yrb + c.yrv) & PMASK;
				final int pzm = (zrb + c.zrv) & PMASK;
				final Grad3 grad = permGrad3[perm[perm[pxm] ^ pym] ^ pzm];
				final double extrapolation = grad.dx * dxr + grad.dy * dyr + grad.dz * dzr;
				
				attn *= attn;
				value += attn * attn * extrapolation;
				c = c.nextOnSuccess;
			}
		}
		return value;
	}
	
	/*
	 * Area Generators
	 */
	
	/**
	 * Generate the 2D noise over a large area.
	 * Propagates by flood-fill instead of iterating over a range.
	 * Results may occasionally slightly exceed [-1, 1] due to the grid-snapped pre-generated kernel.
	 */
	public void generate2(final GenerateContext2D context, final double[][] buffer, final int x0, final int y0) {
		final int height = buffer.length;
		final int width = buffer[0].length;
		generate2(context, buffer, x0, y0, width, height, 0, 0);
	}
	
	/**
	 * Generate the 2D noise over a large area.
	 * Propagates by flood-fill instead of iterating over a range.
	 * Results may occasionally slightly exceed [-1, 1] due to the grid-snapped pre-generated kernel.
	 */
	public void generate2(final GenerateContext2D context, final double[][] buffer, final int x0, final int y0, final int width, final int height, final int skipX, final int skipY) {
		final Queue<AreaGenLatticePoint2D> queue = new LinkedList<>();
		final Set<AreaGenLatticePoint2D> seen = new HashSet<>();
		
		final int scaledRadiusX = context.scaledRadiusX;
		final int scaledRadiusY = context.scaledRadiusY;
		final double[][] kernel;
		final int x0Skipped = x0 + skipX;
		final int y0Skipped = y0 + skipY;
		
		// It seems that it's better for performance, to create a local copy.
		// - Slightly faster than generating the kernel here.
		// - Much faster than referencing it directly from the context object.
		// - Much faster than computing the kernel equation every time.
		// You can remove these lines if you find it's the opposite for you.
		// You'll have to double the bounds again in GenerateContext2D
		kernel = new double[scaledRadiusY * 2][/*scaledRadiusX * 2*/];
		for (int yy = 0; yy < scaledRadiusY; yy++) {
			kernel[yy] = context.kernel[yy].clone();
			kernel[2 * scaledRadiusY - yy - 1] = kernel[yy];
		}
		
		// Get started with one point/vertex.
		// For some lattices, you might need to try a handful of points in the cell,
		// or flip a couple of coordinates, to guarantee it or a neighbor contributes.
		// For An* lattices, the base coordinate seems fine.
		final double x0f = x0Skipped * context.xFrequency;
		final double y0f = y0Skipped * context.yFrequency;
		final double x0s = context.orientation.s00 * x0f + context.orientation.s01 * y0f;
		final double y0s = context.orientation.s10 * x0f + context.orientation.s11 * y0f;
		final int x0sb = fastFloor(x0s);
		final int y0sb = fastFloor(y0s);
		final AreaGenLatticePoint2D firstPoint = new AreaGenLatticePoint2D(context, x0sb, y0sb);
		queue.add(firstPoint);
		seen.add(firstPoint);
		
		while (!queue.isEmpty()) {
			final AreaGenLatticePoint2D point = queue.remove();
			final int destPointX = point.destPointX;
			final int destPointY = point.destPointY;
			
			// Prepare gradient vector
			final int pxm = point.xsv & PMASK;
			final int pym = point.ysv & PMASK;
			final Grad2 grad = context.orientation.gradients[perm[perm[pxm] ^ pym]];
			final double gx = grad.dx * context.xFrequency;
			final double gy = grad.dy * context.yFrequency;
			final double gOff = 0.5 * (gx + gy); // to correct for (0.5, 0.5)-offset kernel
			
			// Contribution kernel bounds
			int yy0 = destPointY - scaledRadiusY;
			if (yy0 < y0Skipped) {
				yy0 = y0Skipped;
			}
			int yy1 = destPointY + scaledRadiusY;
			if (yy1 > y0 + height) {
				yy1 = y0 + height;
			}
			
			// For each row of the contribution circle,
			for (int yy = yy0; yy < yy1; yy++) {
				final int dy = yy - destPointY;
				final int ky = dy + scaledRadiusY;
				
				// Set up bounds so we only loop over what we need to
				final int thisScaledRadiusX = context.kernelBounds[ky];
				int xx0 = destPointX - thisScaledRadiusX;
				if (xx0 < x0Skipped) {
					xx0 = x0Skipped;
				}
				int xx1 = destPointX + thisScaledRadiusX;
				if (xx1 > x0 + width) {
					xx1 = x0 + width;
				}
				
				// For each point on that row
				for (int xx = xx0; xx < xx1; xx++) {
					final int dx = xx - destPointX;
					final int kx = dx + scaledRadiusX;
					
					// gOff accounts for our choice to offset the pre-generated kernel by (0.5, 0.5) to avoid the zero center.
					// I found almost no difference in performance using gOff vs not (under 1ns diff per value on my system)
					final double extrapolation = gx * dx + gy * dy + gOff;
					buffer[yy - y0][xx - x0] += kernel[ky][kx] * extrapolation;
					
				}
			}
			
			// For each neighbor of the point
			for (final int[] ints : NEIGHBOR_MAP_2D) {
				final AreaGenLatticePoint2D neighbor = new AreaGenLatticePoint2D(context,
					point.xsv + ints[0], point.ysv + ints[1]);
				
				// If it's in range of the buffer region and not seen before
				if (neighbor.destPointX + scaledRadiusX >= x0Skipped && neighbor.destPointX - scaledRadiusX <= x0 + width - 1
					&& neighbor.destPointY + scaledRadiusY >= y0Skipped && neighbor.destPointY - scaledRadiusY <= y0 + height - 1
					&& !seen.contains(neighbor)) {
					
					// Add it to the queue so we can process it at some point
					queue.add(neighbor);
					
					// Add it to the set so we don't add it to the queue again
					seen.add(neighbor);
				}
			}
		}
	}
	
	/**
	 * Generate the 3D noise over a large area/volume.
	 * Propagates by flood-fill instead of iterating over a range.
	 * Results may occasionally slightly exceed [-1, 1] due to the grid-snapped pre-generated kernel.
	 */
	public void generate3(final GenerateContext3D context, final double[][][] buffer, final int x0, final int y0, final int z0) {
		final int depth = buffer.length;
		final int height = buffer[0].length;
		final int width = buffer[0][0].length;
		generate3(context, buffer, x0, y0, z0, width, height, depth, 0, 0, 0);
	}
	
	/**
	 * Generate the 3D noise over a large area/volume.
	 * Propagates by flood-fill instead of iterating over a range.
	 * Results may occasionally slightly exceed [-1, 1] due to the grid-snapped pre-generated kernel.
	 */
	public void generate3(final GenerateContext3D context, final double[][][] buffer, final int x0, final int y0, final int z0, final int width, final int height, final int depth, final int skipX, final int skipY, final int skipZ) {
		final Queue<AreaGenLatticePoint3D> queue = new LinkedList<>();
		final Set<AreaGenLatticePoint3D> seen = new HashSet<>();
		
		final int scaledRadiusX = context.scaledRadiusX;
		final int scaledRadiusY = context.scaledRadiusY;
		final int scaledRadiusZ = context.scaledRadiusZ;
		final double[][][] kernel = context.kernel;
		final int x0Skipped = x0 + skipX;
		final int y0Skipped = y0 + skipY;
		final int z0Skipped = z0 + skipZ;
		
		// Quaternion multiplication for rotation.
		// https://blog.molecular-matters.com/2013/05/24/a-faster-quaternion-vector-multiplication/
		final double qx = context.orientation.qx;
		final double qy = context.orientation.qy;
		final double qz = context.orientation.qz;
		final double qw = context.orientation.qw;
		final double x0f = x0Skipped * context.xFrequency;
		final double y0f = y0Skipped * context.yFrequency;
		final double z0f = z0Skipped * context.zFrequency;
		final double tx = 2 * (qy * z0f - qz * y0f);
		final double ty = 2 * (qz * x0f - qx * z0f);
		final double tz = 2 * (qx * y0f - qy * x0f);
		final double x0r = x0f + qw * tx + (qy * tz - qz * ty);
		final double y0r = y0f + qw * ty + (qz * tx - qx * tz);
		final double z0r = z0f + qw * tz + (qx * ty - qy * tx);
		
		final int x0rb = fastFloor(x0r);
		final int y0rb = fastFloor(y0r);
		final int z0rb = fastFloor(z0r);
		
		final AreaGenLatticePoint3D firstPoint = new AreaGenLatticePoint3D(context, x0rb, y0rb, z0rb, 0);
		queue.add(firstPoint);
		seen.add(firstPoint);
		
		while (!queue.isEmpty()) {
			final AreaGenLatticePoint3D point = queue.remove();
			final int destPointX = point.destPointX;
			final int destPointY = point.destPointY;
			final int destPointZ = point.destPointZ;
			
			// Prepare gradient vector
			final int pxm = point.xsv & PMASK;
			final int pym = point.ysv & PMASK;
			final int pzm = point.zsv & PMASK;
			final Grad3 grad = context.orientation.gradients[perm[perm[perm[pxm] ^ pym] ^ pzm]];
			final double gx = grad.dx * context.xFrequency;
			final double gy = grad.dy * context.yFrequency;
			final double gz = grad.dz * context.zFrequency;
			final double gOff = 0.5 * (gx + gy + gz); // to correct for (0.5, 0.5, 0.5)-offset kernel
			
			// Contribution kernel bounds.
			int zz0 = destPointZ - scaledRadiusZ;
			if (zz0 < z0Skipped) {
				zz0 = z0Skipped;
			}
			int zz1 = destPointZ + scaledRadiusZ;
			if (zz1 > z0 + depth) {
				zz1 = z0 + depth;
			}
			
			// For each x/y slice of the contribution sphere,
			for (int zz = zz0; zz < zz1; zz++) {
				final int dz = zz - destPointZ;
				final int kz = dz + scaledRadiusZ;
				
				// Set up bounds so we only loop over what we need to
				final int thisScaledRadiusY = context.kernelBoundsY[kz];
				int yy0 = destPointY - thisScaledRadiusY;
				if (yy0 < y0Skipped) {
					yy0 = y0Skipped;
				}
				int yy1 = destPointY + thisScaledRadiusY;
				if (yy1 > y0 + height) {
					yy1 = y0 + height;
				}
				
				// For each row of the contribution circle,
				for (int yy = yy0; yy < yy1; yy++) {
					final int dy = yy - destPointY;
					final int ky = dy + scaledRadiusY;
					
					// Set up bounds so we only loop over what we need to
					final int thisScaledRadiusX = context.kernelBoundsX[kz][ky];
					int xx0 = destPointX - thisScaledRadiusX;
					if (xx0 < x0Skipped) {
						xx0 = x0Skipped;
					}
					int xx1 = destPointX + thisScaledRadiusX;
					if (xx1 > x0 + width) {
						xx1 = x0 + width;
					}
					
					// For each point on that row
					for (int xx = xx0; xx < xx1; xx++) {
						final int dx = xx - destPointX;
						final int kx = dx + scaledRadiusX;
						
						// gOff accounts for our choice to offset the pre-generated kernel by (0.5, 0.5, 0.5) to avoid the zero center.
						final double extrapolation = gx * dx + gy * dy + gz * dz + gOff;
						buffer[zz - z0][yy - y0][xx - x0] += kernel[kz][ky][kx] * extrapolation;
						
					}
				}
			}
			
			// For each neighbor of the point
			for (int i = 0; i < NEIGHBOR_MAP_3D[0].length; i++) {
				final int l = point.lattice;
				final AreaGenLatticePoint3D neighbor = new AreaGenLatticePoint3D(context,
					point.xsv + NEIGHBOR_MAP_3D[l][i][0], point.ysv + NEIGHBOR_MAP_3D[l][i][1], point.zsv + NEIGHBOR_MAP_3D[l][i][2], 1 ^ l);
				
				// If it's in range of the buffer region and not seen before
				if (neighbor.destPointX + scaledRadiusX >= x0Skipped && neighbor.destPointX - scaledRadiusX <= x0 + width - 1
					&& neighbor.destPointY + scaledRadiusY >= y0Skipped && neighbor.destPointY - scaledRadiusY <= y0 + height - 1
					&& neighbor.destPointZ + scaledRadiusZ >= z0Skipped && neighbor.destPointZ - scaledRadiusZ <= z0 + depth - 1
					&& !seen.contains(neighbor)) {
					
					// Add it to the queue so we can process it at some point
					queue.add(neighbor);
					
					// Add it to the set so we don't add it to the queue again
					seen.add(neighbor);
				}
			}
		}
	}
	
	/*
	 * Utility
	 */
	
	private static int fastFloor(final double value) {
		final int i = (int)value;
		return value < i ? i - 1 : i;
	}
	
	/*
	 * Definitions
	 */
	
	private static final LatticePoint2D[] LOOKUP_2D;
	private static final LatticePoint3D[] LOOKUP_3D;
	
	static {
		LOOKUP_2D = new LatticePoint2D[8 * 4];
		LOOKUP_3D = new LatticePoint3D[8];
		
		for (int i = 0; i < 8; i++) {
			final int i1;
			final int j1;
			final int i2;
			final int j2;
			if ((i & 1) == 0) {
				if ((i & 2) == 0) {
					i1 = -1;
					j1 = 0;
				}
				else {
					i1 = 1;
					j1 = 0;
				}
				if ((i & 4) == 0) {
					i2 = 0;
					j2 = -1;
				}
				else {
					i2 = 0;
					j2 = 1;
				}
			}
			else {
				if ((i & 2) != 0) {
					i1 = 2;
					j1 = 1;
				}
				else {
					i1 = 0;
					j1 = 1;
				}
				if ((i & 4) != 0) {
					i2 = 1;
					j2 = 2;
				}
				else {
					i2 = 1;
					j2 = 0;
				}
			}
			LOOKUP_2D[i * 4 + 0] = new LatticePoint2D(0, 0);
			LOOKUP_2D[i * 4 + 1] = new LatticePoint2D(1, 1);
			LOOKUP_2D[i * 4 + 2] = new LatticePoint2D(i1, j1);
			LOOKUP_2D[i * 4 + 3] = new LatticePoint2D(i2, j2);
		}
		
		for (int i = 0; i < 8; i++) {
			final int i1;
			final int j1;
			final int k1;
			final int i2;
			final int j2;
			final int k2;
			i1 = (i >> 0) & 1;
			j1 = (i >> 1) & 1;
			k1 = (i >> 2) & 1;
			i2 = i1 ^ 1;
			j2 = j1 ^ 1;
			k2 = k1 ^ 1;
			
			// The two points within this octant, one from each of the two cubic half-lattices.
			final LatticePoint3D c0 = new LatticePoint3D(i1, j1, k1, 0);
			final LatticePoint3D c1 = new LatticePoint3D(i1 + i2, j1 + j2, k1 + k2, 1);
			
			// (1, 0, 0) vs (0, 1, 1) away from octant.
			final LatticePoint3D c2 = new LatticePoint3D(i1 ^ 1, j1, k1, 0);
			final LatticePoint3D c3 = new LatticePoint3D(i1, j1 ^ 1, k1 ^ 1, 0);
			
			// (1, 0, 0) vs (0, 1, 1) away from octant, on second half-lattice.
			final LatticePoint3D c4 = new LatticePoint3D(i1 + (i2 ^ 1), j1 + j2, k1 + k2, 1);
			final LatticePoint3D c5 = new LatticePoint3D(i1 + i2, j1 + (j2 ^ 1), k1 + (k2 ^ 1), 1);
			
			// (0, 1, 0) vs (1, 0, 1) away from octant.
			final LatticePoint3D c6 = new LatticePoint3D(i1, j1 ^ 1, k1, 0);
			final LatticePoint3D c7 = new LatticePoint3D(i1 ^ 1, j1, k1 ^ 1, 0);
			
			// (0, 1, 0) vs (1, 0, 1) away from octant, on second half-lattice.
			final LatticePoint3D c8 = new LatticePoint3D(i1 + i2, j1 + (j2 ^ 1), k1 + k2, 1);
			final LatticePoint3D c9 = new LatticePoint3D(i1 + (i2 ^ 1), j1 + j2, k1 + (k2 ^ 1), 1);
			
			// (0, 0, 1) vs (1, 1, 0) away from octant.
			final LatticePoint3D cA = new LatticePoint3D(i1, j1, k1 ^ 1, 0);
			final LatticePoint3D cB = new LatticePoint3D(i1 ^ 1, j1 ^ 1, k1, 0);
			
			// (0, 0, 1) vs (1, 1, 0) away from octant, on second half-lattice.
			final LatticePoint3D cC = new LatticePoint3D(i1 + i2, j1 + j2, k1 + (k2 ^ 1), 1);
			final LatticePoint3D cD = new LatticePoint3D(i1 + (i2 ^ 1), j1 + (j2 ^ 1), k1 + k2, 1);
			
			// First two points are guaranteed.
			c0.nextOnFailure = c1;
			c0.nextOnSuccess = c1;
			c1.nextOnFailure = c2;
			c1.nextOnSuccess = c2;
			
			// If c2 is in range, then we know c3 and c4 are not.
			c2.nextOnFailure = c3;
			c2.nextOnSuccess = c5;
			c3.nextOnFailure = c4;
			c3.nextOnSuccess = c4;
			
			// If c4 is in range, then we know c5 is not.
			c4.nextOnFailure = c5;
			c4.nextOnSuccess = c6;
			c5.nextOnFailure = c6;
			c5.nextOnSuccess = c6;
			
			// If c6 is in range, then we know c7 and c8 are not.
			c6.nextOnFailure = c7;
			c6.nextOnSuccess = c9;
			c7.nextOnFailure = c8;
			c7.nextOnSuccess = c8;
			
			// If c8 is in range, then we know c9 is not.
			c8.nextOnFailure = c9;
			c8.nextOnSuccess = cA;
			c9.nextOnFailure = cA;
			c9.nextOnSuccess = cA;
			
			// If cA is in range, then we know cB and cC are not.
			cA.nextOnFailure = cB;
			cA.nextOnSuccess = cD;
			cB.nextOnFailure = cC;
			cB.nextOnSuccess = cC;
			
			// If cC is in range, then we know cD is not.
			cC.nextOnFailure = cD;
			cC.nextOnSuccess = null;
			cD.nextOnFailure = null;
			cD.nextOnSuccess = null;
			
			LOOKUP_3D[i] = c0;
			
		}
	}
	
	// Hexagon surrounding each vertex.
	private static final int[][] NEIGHBOR_MAP_2D = {
		{ 1, 0 }, { 1, 1 }, { 0, 1 }, { 0, -1 }, { -1, -1 }, { -1, 0 }
	};
	
	// Cube surrounding each vertex.
	// Alternates between half-lattices.
	private static final int[][][] NEIGHBOR_MAP_3D = {
		{
			{ 1024, 1024, 1024 }, { 1025, 1024, 1024 }, { 1024, 1025, 1024 }, { 1025, 1025, 1024 },
			{ 1024, 1024, 1025 }, { 1025, 1024, 1025 }, { 1024, 1025, 1025 }, { 1025, 1025, 1025 }
		},
		{
			{ -1024, -1024, -1024 }, { -1025, -1024, 1024 }, { -1024, -1025, -1024 }, { -1025, -1025, -1024 },
			{ -1024, -1024, -1025 }, { -1025, -1024, -1025 }, { -1024, -1025, -1025 }, { -1025, -1025, 1025 }
		},
	};
	
	private static class LatticePoint2D {
		final int xsv, ysv;
		final double dx, dy;
		
		public LatticePoint2D(final int xsv, final int ysv) {
			this.xsv = xsv;
			this.ysv = ysv;
			final double ssv = (xsv + ysv) * -0.211324865405187;
			this.dx = -xsv - ssv;
			this.dy = -ysv - ssv;
		}
	}
	
	private static class LatticePoint3D {
		public final double dxr, dyr, dzr;
		public final int xrv, yrv, zrv;
		LatticePoint3D nextOnFailure, nextOnSuccess;
		
		public LatticePoint3D(final int xrv, final int yrv, final int zrv, final int lattice) {
			this.dxr = -xrv + lattice * 0.5;
			this.dyr = -yrv + lattice * 0.5;
			this.dzr = -zrv + lattice * 0.5;
			this.xrv = xrv + lattice * 1024;
			this.yrv = yrv + lattice * 1024;
			this.zrv = zrv + lattice * 1024;
		}
	}
	
	private static class AreaGenLatticePoint2D {
		final int xsv, ysv;
		final int destPointX, destPointY;
		
		public AreaGenLatticePoint2D(final GenerateContext2D context, final int xsv, final int ysv) {
			this.xsv = xsv;
			this.ysv = ysv;
			
			//Matrix multiplication for inverse rotation. Simplex skew transforms have always been shorthand for matrices.
			this.destPointX = (int)Math.ceil((context.orientation.t00 * xsv + context.orientation.t01 * ysv) * context.xFrequencyInverse);
			this.destPointY = (int)Math.ceil((context.orientation.t10 * xsv + context.orientation.t11 * ysv) * context.yFrequencyInverse);
		}
		
		@Override
		public int hashCode() {
			return xsv * 7841 + ysv;
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof AreaGenLatticePoint2D)) {
				return false;
			}
			final AreaGenLatticePoint2D other = (AreaGenLatticePoint2D)obj;
			return (other.xsv == this.xsv && other.ysv == this.ysv);
		}
	}
	
	private static class AreaGenLatticePoint3D {
		final int xsv, ysv, zsv, lattice;
		final int destPointX, destPointY, destPointZ;
		
		public AreaGenLatticePoint3D(final GenerateContext3D context, final int xsv, final int ysv, final int zsv, final int lattice) {
			this.xsv = xsv;
			this.ysv = ysv;
			this.zsv = zsv;
			this.lattice = lattice;
			final double xr = (xsv - lattice * 1024.5);
			final double yr = (ysv - lattice * 1024.5);
			final double zr = (zsv - lattice * 1024.5);
			
			// Quaternion multiplication for inverse rotation.
			// https://blog.molecular-matters.com/2013/05/24/a-faster-quaternion-vector-multiplication/
			final double qx = -context.orientation.qx;
			final double qy = -context.orientation.qy;
			final double qz = -context.orientation.qz;
			final double qw = context.orientation.qw;
			final double tx = 2 * (qy * zr - qz * yr);
			final double ty = 2 * (qz * xr - qx * zr);
			final double tz = 2 * (qx * yr - qy * xr);
			final double xrr = xr + qw * tx + (qy * tz - qz * ty);
			final double yrr = yr + qw * ty + (qz * tx - qx * tz);
			final double zrr = zr + qw * tz + (qx * ty - qy * tx);
			
			this.destPointX = (int)Math.ceil(xrr * context.xFrequencyInverse);
			this.destPointY = (int)Math.ceil(yrr * context.yFrequencyInverse);
			this.destPointZ = (int)Math.ceil(zrr * context.zFrequencyInverse);
		}
		
		@Override
		public int hashCode() {
			return xsv * 2122193 + ysv * 2053 + zsv * 2 + lattice;
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof AreaGenLatticePoint3D)) {
				return false;
			}
			final AreaGenLatticePoint3D other = (AreaGenLatticePoint3D)obj;
			return (other.xsv == this.xsv && other.ysv == this.ysv && other.zsv == this.zsv && other.lattice == this.lattice);
		}
	}
	
	public static class GenerateContext2D {
		
		final double xFrequency;
		final double yFrequency;
		final double xFrequencyInverse;
		final double yFrequencyInverse;
		final int scaledRadiusX;
		final int scaledRadiusY;
		final double[][] kernel;
		final int[] kernelBounds;
		final LatticeOrientation2D orientation;
		
		public GenerateContext2D(final LatticeOrientation2D orientation, final double xFrequency, final double yFrequency, final double amplitude) {
			
			// These will be used by every call to generate
			this.orientation = orientation;
			this.xFrequency = xFrequency;
			this.yFrequency = yFrequency;
			this.xFrequencyInverse = 1.0 / xFrequency;
			this.yFrequencyInverse = 1.0 / yFrequency;
			
			final double preciseScaledRadiusX = Math.sqrt(2.0 / 3.0) * xFrequencyInverse;
			final double preciseScaledRadiusY = Math.sqrt(2.0 / 3.0) * yFrequencyInverse;
			
			// 0.25 because we offset center by 0.5
			this.scaledRadiusX = (int)Math.ceil(preciseScaledRadiusX + 0.25);
			this.scaledRadiusY = (int)Math.ceil(preciseScaledRadiusY + 0.25);
			
			// So will these
			kernel = new double[scaledRadiusY/* * 2*/][];
			kernelBounds = new int[scaledRadiusY * 2];
			for (int yy = 0; yy < scaledRadiusY * 2; yy++) {
				
				// Pre-generate boundary of circle
				kernelBounds[yy] = (int)Math.ceil(
					Math.sqrt(1.0
						- (yy + 0.5 - scaledRadiusY) * (yy + 0.5 - scaledRadiusY) / (scaledRadiusY * scaledRadiusY)
					) * scaledRadiusX);
				
				if (yy < scaledRadiusY) {
					kernel[yy] = new double[scaledRadiusX * 2];
					
					// Pre-generate kernel
					for (int xx = 0; xx < scaledRadiusX * 2; xx++) {
						final double dx = (xx + 0.5 - scaledRadiusX) * xFrequency;
						final double dy = (yy + 0.5 - scaledRadiusY) * yFrequency;
						double attn = (2.0 / 3.0) - dx * dx - dy * dy;
						if (attn > 0) {
							attn *= attn;
							kernel[yy][xx] = attn * attn * amplitude;
						}
						else {
							kernel[yy][xx] = 0.0;
						}
					}
				} /* else kernel[yy] = kernel[2 * scaledRadiusY - yy - 1];*/
			}
		}
	}
	
	public static class GenerateContext3D {
		
		final double xFrequency;
		final double yFrequency;
		final double zFrequency;
		final double xFrequencyInverse;
		final double yFrequencyInverse;
		final double zFrequencyInverse;
		final int scaledRadiusX;
		final int scaledRadiusY;
		final int scaledRadiusZ;
		final double[][][] kernel;
		final int[] kernelBoundsY;
		final int[][] kernelBoundsX;
		final LatticeOrientation3D orientation;
		
		public GenerateContext3D(final LatticeOrientation3D orientation, final double xFrequency, final double yFrequency, final double zFrequency, final double amplitude) {
			
			// These will be used by every call to generate
			this.orientation = orientation;
			this.xFrequency = xFrequency;
			this.yFrequency = yFrequency;
			this.zFrequency = zFrequency;
			this.xFrequencyInverse = 1.0 / xFrequency;
			this.yFrequencyInverse = 1.0 / yFrequency;
			this.zFrequencyInverse = 1.0 / zFrequency;
			
			final double preciseScaledRadiusX = Math.sqrt(0.75) * xFrequencyInverse;
			final double preciseScaledRadiusY = Math.sqrt(0.75) * yFrequencyInverse;
			final double preciseScaledRadiusZ = Math.sqrt(0.75) * zFrequencyInverse;
			
			// 0.25 because we offset center by 0.5
			this.scaledRadiusX = (int)Math.ceil(preciseScaledRadiusX + 0.25);
			this.scaledRadiusY = (int)Math.ceil(preciseScaledRadiusY + 0.25);
			this.scaledRadiusZ = (int)Math.ceil(preciseScaledRadiusZ + 0.25);
			
			// So will these
			kernel = new double[scaledRadiusZ * 2][][];
			kernelBoundsY = new int[scaledRadiusZ * 2];
			kernelBoundsX = new int[scaledRadiusZ * 2][];
			for (int zz = 0; zz < scaledRadiusZ * 2; zz++) {
				
				// Pre-generate boundary of sphere
				kernelBoundsY[zz] = (int)Math.ceil(
					Math.sqrt(1.0 - (zz + 0.5 - scaledRadiusZ) * (zz + 0.5 - scaledRadiusZ)
						/ (scaledRadiusZ * scaledRadiusZ)) * scaledRadiusY);
				
				if (zz < scaledRadiusZ) {
					kernel[zz] = new double[scaledRadiusY * 2][];
					kernelBoundsX[zz] = new int[scaledRadiusY * 2];
				}
				else {
					kernel[zz] = kernel[2 * scaledRadiusZ - zz - 1];
					kernelBoundsX[zz] = kernelBoundsX[2 * scaledRadiusZ - zz - 1];
				}
				
				if (zz < scaledRadiusZ) {
					for (int yy = 0; yy < scaledRadiusY * 2; yy++) {
						
						// Pre-generate boundary of sphere
						kernelBoundsX[zz][yy] = (int)Math.ceil(
							Math.sqrt(1.0
								- (yy + 0.5 - scaledRadiusY) * (yy + 0.5 - scaledRadiusY) / (scaledRadiusY * scaledRadiusY)
								- (zz + 0.5 - scaledRadiusZ) * (zz + 0.5 - scaledRadiusZ) / (scaledRadiusZ * scaledRadiusZ)
							) * scaledRadiusX);
						
						if (yy < scaledRadiusY) {
							kernel[zz][yy] = new double[scaledRadiusX * 2];
							
							// Pre-generate kernel
							for (int xx = 0; xx < scaledRadiusX * 2; xx++) {
								final double dx = (xx + 0.5 - scaledRadiusX) * xFrequency;
								final double dy = (yy + 0.5 - scaledRadiusY) * yFrequency;
								final double dz = (zz + 0.5 - scaledRadiusZ) * zFrequency;
								double attn = 0.75 - dx * dx - dy * dy - dz * dz;
								if (attn > 0) {
									attn *= attn;
									kernel[zz][yy][xx] = attn * attn * amplitude;
								}
								else {
									kernel[zz][yy][xx] = 0.0;
								}
							}
							
						}
						else {
							kernel[zz][yy] = kernel[zz][2 * scaledRadiusY - yy - 1];
						}
					}
				}
			}
		}
	}
	
	public enum LatticeOrientation2D {
		// Simplex skew transforms have always been shorthand for the matrices they represent.
		// But when we bake the rotation into the skew transform, we need to use the general form.
		Standard(GRADIENTS_2D,
			1.366025403784439, 0.366025403784439, 0.366025403784439, 1.366025403784439,
			0.788675134594813, -0.211324865405187, -0.211324865405187, 0.788675134594813),
		XBeforeY(GRADIENTS_2D_X_BEFORE_Y,
			0.7071067811865476, 1.224744871380249, -0.7071067811865476, 1.224744871380249,
			0.7071067811865476, -0.7071067811865476, 0.40824829046764305, 0.40824829046764305);
		
		final Grad2[] gradients;
		final double s00;
		final double s01;
		final double s10;
		final double s11;
		final double t00;
		final double t01;
		final double t10;
		final double t11;
		
		LatticeOrientation2D(final Grad2[] gradients,
		                     final double s00, final double s01, final double s10, final double s11,
		                     final double t00, final double t01, final double t10, final double t11) {
			this.gradients = gradients;
			this.s00 = s00;
			this.s01 = s01;
			this.s10 = s10;
			this.s11 = s11;
			this.t00 = t00;
			this.t01 = t01;
			this.t10 = t10;
			this.t11 = t11;
		}
	}
	
	public enum LatticeOrientation3D {
		// Quaternions for 3D. Could use matrices, but I already wrote this code before I moved them into here.
		Classic(GRADIENTS_3D_CLASSIC, 0.577350269189626, 0.577350269189626, 0.577350269189626, 0),
		XZBeforeY(GRADIENTS_3D_XZ_BEFORE_Y, -0.3250575836718682, 0, 0.3250575836718682, 0.8880738339771154);
		
		final Grad3[] gradients;
		final double qx;
		final double qy;
		final double qz;
		final double qw;
		
		LatticeOrientation3D(final Grad3[] gradients, final double qx, final double qy, final double qz, final double qw) {
			this.gradients = gradients;
			this.qx = qx;
			this.qy = qy;
			this.qz = qz;
			this.qw = qw;
		}
	}
	
	/*
	 * Gradients
	 */
	
	public static final double N2 = 0.05481866495625118;
	public static final double N3 = 0.2781926117527186;
	
	private static class Grad2 {
		final double dx, dy;
		
		public Grad2(final double dx, final double dy) {
			this.dx = dx / N2;
			this.dy = dy / N2;
		}
	}
	
	private static class Grad3 {
		final double dx, dy, dz;
		
		public Grad3(final double dx, final double dy, final double dz) {
			this.dx = dx / N3;
			this.dy = dy / N3;
			this.dz = dz / N3;
		}
	}
	
	private static final Grad2[] GRADIENTS_2D, GRADIENTS_2D_X_BEFORE_Y;
	private static final Grad3[] GRADIENTS_3D, GRADIENTS_3D_CLASSIC, GRADIENTS_3D_XZ_BEFORE_Y;
	
	static {
		
		GRADIENTS_2D = new Grad2[PSIZE];
		GRADIENTS_2D_X_BEFORE_Y = new Grad2[PSIZE];
		final Grad2[] grad2 = {
			new Grad2(0.130526192220052, 0.99144486137381),
			new Grad2(0.38268343236509, 0.923879532511287),
			new Grad2(0.608761429008721, 0.793353340291235),
			new Grad2(0.793353340291235, 0.608761429008721),
			new Grad2(0.923879532511287, 0.38268343236509),
			new Grad2(0.99144486137381, 0.130526192220051),
			new Grad2(0.99144486137381, -0.130526192220051),
			new Grad2(0.923879532511287, -0.38268343236509),
			new Grad2(0.793353340291235, -0.60876142900872),
			new Grad2(0.608761429008721, -0.793353340291235),
			new Grad2(0.38268343236509, -0.923879532511287),
			new Grad2(0.130526192220052, -0.99144486137381),
			new Grad2(-0.130526192220052, -0.99144486137381),
			new Grad2(-0.38268343236509, -0.923879532511287),
			new Grad2(-0.608761429008721, -0.793353340291235),
			new Grad2(-0.793353340291235, -0.608761429008721),
			new Grad2(-0.923879532511287, -0.38268343236509),
			new Grad2(-0.99144486137381, -0.130526192220052),
			new Grad2(-0.99144486137381, 0.130526192220051),
			new Grad2(-0.923879532511287, 0.38268343236509),
			new Grad2(-0.793353340291235, 0.608761429008721),
			new Grad2(-0.608761429008721, 0.793353340291235),
			new Grad2(-0.38268343236509, 0.923879532511287),
			new Grad2(-0.130526192220052, 0.99144486137381)
		};
		final Grad2[] grad2XBeforeY = new Grad2[grad2.length];
		for (int i = 0; i < grad2.length; i++) {
			// Unrotated gradients for XBeforeY 2D
			final double xx = grad2[i].dx * 0.7071067811865476;
			final double yy = grad2[i].dy * 0.7071067811865476;
			grad2XBeforeY[i] = new Grad2(xx - yy, xx + yy);
		}
		for (int i = 0; i < PSIZE; i++) {
			GRADIENTS_2D[i] = grad2[i % grad2.length];
			GRADIENTS_2D_X_BEFORE_Y[i] = grad2XBeforeY[i % grad2XBeforeY.length];
		}
		
		GRADIENTS_3D = new Grad3[PSIZE];
		GRADIENTS_3D_CLASSIC = new Grad3[PSIZE];
		GRADIENTS_3D_XZ_BEFORE_Y = new Grad3[PSIZE];
		final Grad3[] grad3 = {
			new Grad3(-2.22474487139, -2.22474487139, -1.0),
			new Grad3(-2.22474487139, -2.22474487139, 1.0),
			new Grad3(-3.0862664687972017, -1.1721513422464978, 0.0),
			new Grad3(-1.1721513422464978, -3.0862664687972017, 0.0),
			new Grad3(-2.22474487139, -1.0, -2.22474487139),
			new Grad3(-2.22474487139, 1.0, -2.22474487139),
			new Grad3(-1.1721513422464978, 0.0, -3.0862664687972017),
			new Grad3(-3.0862664687972017, 0.0, -1.1721513422464978),
			new Grad3(-2.22474487139, -1.0, 2.22474487139),
			new Grad3(-2.22474487139, 1.0, 2.22474487139),
			new Grad3(-3.0862664687972017, 0.0, 1.1721513422464978),
			new Grad3(-1.1721513422464978, 0.0, 3.0862664687972017),
			new Grad3(-2.22474487139, 2.22474487139, -1.0),
			new Grad3(-2.22474487139, 2.22474487139, 1.0),
			new Grad3(-1.1721513422464978, 3.0862664687972017, 0.0),
			new Grad3(-3.0862664687972017, 1.1721513422464978, 0.0),
			new Grad3(-1.0, -2.22474487139, -2.22474487139),
			new Grad3(1.0, -2.22474487139, -2.22474487139),
			new Grad3(0.0, -3.0862664687972017, -1.1721513422464978),
			new Grad3(0.0, -1.1721513422464978, -3.0862664687972017),
			new Grad3(-1.0, -2.22474487139, 2.22474487139),
			new Grad3(1.0, -2.22474487139, 2.22474487139),
			new Grad3(0.0, -1.1721513422464978, 3.0862664687972017),
			new Grad3(0.0, -3.0862664687972017, 1.1721513422464978),
			new Grad3(-1.0, 2.22474487139, -2.22474487139),
			new Grad3(1.0, 2.22474487139, -2.22474487139),
			new Grad3(0.0, 1.1721513422464978, -3.0862664687972017),
			new Grad3(0.0, 3.0862664687972017, -1.1721513422464978),
			new Grad3(-1.0, 2.22474487139, 2.22474487139),
			new Grad3(1.0, 2.22474487139, 2.22474487139),
			new Grad3(0.0, 3.0862664687972017, 1.1721513422464978),
			new Grad3(0.0, 1.1721513422464978, 3.0862664687972017),
			new Grad3(2.22474487139, -2.22474487139, -1.0),
			new Grad3(2.22474487139, -2.22474487139, 1.0),
			new Grad3(1.1721513422464978, -3.0862664687972017, 0.0),
			new Grad3(3.0862664687972017, -1.1721513422464978, 0.0),
			new Grad3(2.22474487139, -1.0, -2.22474487139),
			new Grad3(2.22474487139, 1.0, -2.22474487139),
			new Grad3(3.0862664687972017, 0.0, -1.1721513422464978),
			new Grad3(1.1721513422464978, 0.0, -3.0862664687972017),
			new Grad3(2.22474487139, -1.0, 2.22474487139),
			new Grad3(2.22474487139, 1.0, 2.22474487139),
			new Grad3(1.1721513422464978, 0.0, 3.0862664687972017),
			new Grad3(3.0862664687972017, 0.0, 1.1721513422464978),
			new Grad3(2.22474487139, 2.22474487139, -1.0),
			new Grad3(2.22474487139, 2.22474487139, 1.0),
			new Grad3(3.0862664687972017, 1.1721513422464978, 0.0),
			new Grad3(1.1721513422464978, 3.0862664687972017, 0.0)
		};
		final Grad3[] grad3Classic = new Grad3[grad3.length];
		final Grad3[] grad3XZBeforeY = new Grad3[grad3.length];
		for (int i = 0; i < grad3.length; i++) {
			final double gxr = grad3[i].dx;
			final double gyr = grad3[i].dy;
			final double gzr = grad3[i].dz;
			
			// Unrotated gradients for classic 3D
			final double grr = (2.0 / 3.0) * (gxr + gyr + gzr);
			final double dx = grr - gxr;
			final double dy = grr - gyr;
			final double dz = grr - gzr;
			grad3Classic[i] = new Grad3(dx, dy, dz);
			
			// Unrotated gradients for plane-first 3D
			final double s2 = (gxr + gzr) * -0.211324865405187;
			final double yy = gyr * 0.577350269189626;
			grad3XZBeforeY[i] = new Grad3(gxr + s2 + yy, (gyr - gxr - gzr) * 0.577350269189626, gzr + s2 + yy);
		}
		for (int i = 0; i < PSIZE; i++) {
			GRADIENTS_3D[i] = grad3[i % grad3.length];
			GRADIENTS_3D_CLASSIC[i] = grad3Classic[i % grad3Classic.length];
			GRADIENTS_3D_XZ_BEFORE_Y[i] = grad3XZBeforeY[i % grad3XZBeforeY.length];
		}
	}
}
