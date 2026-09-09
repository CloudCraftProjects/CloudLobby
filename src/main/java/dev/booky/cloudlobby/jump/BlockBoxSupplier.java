package dev.booky.cloudlobby.jump;

import dev.booky.cloudcore.util.BlockBBox;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Random;

@NullMarked
public final class BlockBoxSupplier {

    private final List<BlockBBox> boxes;
    private final long[] prefix;
    private final long totalVolume;

    public BlockBoxSupplier(List<BlockBBox> boxes) {
        this.boxes = List.copyOf(boxes);
        this.prefix = new long[this.boxes.size() + 1];
        long acc = 0;
        for (int i = 0; i < this.boxes.size(); i++) {
            long v = calcVolume(this.boxes.get(i));
            if (v < 0) {
                throw new ArithmeticException("volume overflow");
            }
            prefix[i] = acc;
            acc += v;
            if (acc < prefix[i]) throw new ArithmeticException("volume overflow");
        }
        prefix[this.boxes.size()] = acc;
        this.totalVolume = acc;
    }

    private static long calcVolume(BlockBBox box) {
        return (long) (box.getMaxX() - box.getMinX() + 1)
                * (box.getMaxY() - box.getMinY() + 1)
                * (box.getMaxZ() - box.getMinZ() + 1);
    }

    public BlockPosition getRandom(Random random) {
        if (this.totalVolume == 0) {
            throw new IllegalStateException("No volume available");
        }

        long idx = random.nextLong(this.totalVolume);
        int i = this.findBox(idx);
        long local = idx - this.prefix[i];

        BlockBBox b = this.boxes.get(i);
        int widthX = b.getMaxX() - b.getMinX() + 1;
        int widthY = b.getMaxY() - b.getMinY() + 1;

        int x = (int) (b.getMinX() + local % widthX);
        int y = (int) (b.getMinY() + (local / widthX) % widthY);
        int z = (int) (b.getMinZ() + local / widthX / widthY);
        return Position.block(x, y, z);
    }

    private int findBox(long idx) {
        int lo = 0;
        int hi = this.boxes.size() - 1;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (this.prefix[mid + 1] > idx) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }
}
