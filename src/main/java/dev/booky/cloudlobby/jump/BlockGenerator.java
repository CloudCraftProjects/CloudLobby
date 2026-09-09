package dev.booky.cloudlobby.jump;
// Created by booky10 in CloudLobby (9:15 PM 08.09.2026)

import dev.booky.cloudlobby.CloudLobbyConfig;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

@NullMarked
public final class BlockGenerator {

    public static final int MAX_TRIES = 512;
    public static final double GRAVITY_FACTOR = 0.8d;

    private Layer[] layers = new Layer[0];

    public void parse(List<CloudLobbyConfig.JumpConfig.BlockPattern> patternLayers) {
        this.layers = patternLayers.stream()
                .map(pattern -> Layer.parse(pattern.offset(), pattern.pattern()))
                .toArray(Layer[]::new);
    }

    public BlockPosition getRandomBlock(Position center, Random random, double previousDistance) {
        return this.getRandomBlock(center, random, previousDistance, -Layer.TAU, Layer.TAU);
    }

    public BlockPosition getRandomBlock(Position center, Random random, double previousDistance, float angleMin, float angleMax) {
        Layer layer = this.layers[random.nextInt(this.layers.length)];
        byte offset = layer.getRandom(random, previousDistance, angleMin, angleMax);
        return Position.block(
                center.blockX() + Layer.relX(offset),
                center.blockY() + layer.getOffset(),
                center.blockZ() + Layer.relZ(offset)
        );
    }

    public BlockPosition getRandomBlock(Position center, Random random, double previousDistance, Predicate<BlockPosition> predicate) {
        return this.getRandomBlock(center, random, previousDistance, -Layer.TAU, Layer.TAU, predicate, predicate);
    }

    public BlockPosition getRandomBlock(
            Position center, Random random, double previousDistance,
            float angleMin, float angleMax,
            Predicate<BlockPosition> predicate
    ) {
        return this.getRandomBlock(center, random, previousDistance, angleMin, angleMax, predicate, predicate);
    }

    public BlockPosition getRandomBlock(
            Position center, Random random, double previousDistance,
            float angleMin, float angleMax,
            Predicate<BlockPosition> primaryPredicate,
            Predicate<BlockPosition> fallbackPredicate
    ) {
        for (int i = 0; i < MAX_TRIES; i++) {
            BlockPosition pos = this.getRandomBlock(center, random, previousDistance, angleMin, angleMax);
            if (primaryPredicate.test(pos)) {
                return pos;
            }
        }
        // check again without angle restriction, using relaxed predicate
        int i = 0;
        while (true) {
            BlockPosition pos = this.getRandomBlock(center, random, previousDistance);
            if (i++ == MAX_TRIES || fallbackPredicate.test(pos)) {
                return pos;
            }
        }
    }

    public Layer getRandomLayer(Random random) {
        return this.layers[random.nextInt(this.layers.length)];
    }

    public static class Layer {

        private static final float TAU = (float) (Math.PI * 2d);

        private static final char AIR_CHAR = '-';
        private static final char BLOCK_CHAR = 'X';

        private final int offset;
        private final byte[] blocks;
        private final float[] angles;
        private final double[] distances;

        private Layer(int offset, byte[] blocks) {
            this.offset = offset;

            // build angles array to efficiently select random blocks in specific angle range
            float[] angles = new float[blocks.length];
            for (int i = 0; i < blocks.length; i++) {
                angles[i] = normalize((float) Math.atan2(relZ(blocks[i]), relX(blocks[i])));
            }
            Integer[] idx = new Integer[blocks.length];
            for (int i = 0; i < idx.length; i++) {
                idx[i] = i;
            }
            Arrays.sort(idx, Comparator.comparingDouble(i -> angles[i]));
            byte[] sorted = new byte[blocks.length];
            float[] sortedAngles = new float[blocks.length];
            double[] sortedDistances = new double[blocks.length];
            for (int i = 0; i < idx.length; i++) {
                byte block = blocks[idx[i]];
                sorted[i] = block;
                sortedAngles[i] = angles[idx[i]];
                int rx = relX(block);
                int rz = relZ(block);
                double horizontal = Math.sqrt((double) rx * rx + (double) rz * rz);
                sortedDistances[i] = Math.max(horizontal + offset * BlockGenerator.GRAVITY_FACTOR, 0.1);
            }
            this.blocks = sorted;
            this.angles = sortedAngles;
            this.distances = sortedDistances;
        }

        private static float normalize(float angle) {
            // we need to normalize angles because we order by angle
            angle %= TAU;
            return angle < 0 ? angle + TAU : angle;
        }

        public static int relX(byte index) {
            return ((index >> 4) << 28) >> 28; // shift cause of sign
        }

        public static int relZ(byte index) {
            return ((index & 0xF) << 28) >> 28; // shift cause of sign
        }

        private static byte index(int relX, int relZ) {
            return (byte) ((relX & 0xF) << 4 | (relZ & 0xF));
        }

        public static Layer parse(int offset, String pattern) {
            String[] lanes = StringUtils.split(pattern, '\n');
            if (Arrays.stream(lanes).anyMatch(lane -> lane.length() != lanes.length)) {
                throw new IllegalStateException("Illegal pattern, must be square " + lanes.length + "x" + lanes.length);
            }
            int length = lanes.length - 1 + lanes.length;
            if (length * length > 255) { // check for storage cap (we only support coords from -7 to 8 cause of how we store it)
                throw new IllegalStateException("Max layer size is 15x15, got " + length + "x" + length);
            }
            byte[] blocks = new byte[length * length];
            int blockCount = 0;
            for (int x = 0; x < lanes.length; x++) {
                String lane = lanes[x];
                for (int z = 0; z < lane.length(); z++) {
                    char c = lane.charAt(z);
                    if (c != BLOCK_CHAR) {
                        if (c == AIR_CHAR) {
                            continue;
                        }
                        throw new IllegalStateException("Illegal character in pattern: '" + c + "'"
                                + " (only '" + BLOCK_CHAR + "' and '" + AIR_CHAR + "' allowed)");
                    }

                    // transform to make center the origin
                    int relX = lanes.length - 1 - x;
                    // save block pattern in array (+mirror)
                    blocks[blockCount++] = index(relX, z);
                    if (relX != 0) {
                        blocks[blockCount++] = index(-relX, z);
                        if (z != 0) {
                            blocks[blockCount++] = index(-relX, -z);
                        }
                    }
                    if (z != 0) {
                        blocks[blockCount++] = index(relX, -z);
                    }
                }
            }
            return new Layer(offset, Arrays.copyOf(blocks, blockCount));
        }

        public int getOffset() {
            return this.offset;
        }

        public byte getRandom(Random random) {
            return this.blocks[random.nextInt(this.blocks.length)];
        }

        private int lowerBound(float angle) {
            // searches first index with angles[idx] >= angle
            int lo = 0;
            int hi = this.blocks.length;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (this.angles[mid] < angle) {
                    lo = mid + 1;
                } else {
                    hi = mid;
                }
            }
            return lo;
        }

        public byte getRandom(Random random, double previousDistance, float angleMin, float angleMax) {
            float angleRange = angleMax - angleMin;
            if (angleRange >= TAU) { // full circle requested, fast path: all blocks
                return this.getWeightedRandom(random, previousDistance, 0, this.blocks.length);
            }
            angleMin = normalize(angleMin);
            angleMax = normalize(angleMax);
            int low = this.lowerBound(angleMin);
            int high = angleMin < angleMax
                    ? this.lowerBound(angleMax)
                    : this.lowerBound(angleMax) + this.blocks.length;
            if (low >= high) { // at least one
                high = low + 1;
            }
            return this.getWeightedRandom(random, previousDistance, low, high);
        }

        private byte getWeightedRandom(Random random, double previousDistance, int low, int high) {
            int len = high - low;
            if (len <= 1) {
                return this.blocks[low % this.blocks.length];
            }
            if (previousDistance < 0d) {
                // uniform random
                int i = random.nextInt(len);
                return this.blocks[(low + i) % this.blocks.length];
            }

            // weight each candidate by how different its distance is from the
            // previous jump to make short jumps not repeat
            double total = 0d;
            double[] weights = new double[len];
            for (int i = 0; i < len; i++) {
                double dist = this.distances[(low + i) % this.blocks.length];
                weights[i] = 1d + Math.abs(dist - previousDistance);
                total += weights[i];
            }

            // weighted random selection
            double r = random.nextDouble() * total;
            double sum = 0d;
            for (int i = 0; i < len; i++) {
                sum += weights[i];
                if (r < sum) {
                    return this.blocks[(low + i) % this.blocks.length];
                }
            }
            return this.blocks[(low + len - 1) % this.blocks.length]; // fallback
        }
    }
}
