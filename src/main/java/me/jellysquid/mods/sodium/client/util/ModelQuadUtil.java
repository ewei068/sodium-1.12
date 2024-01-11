package me.jellysquid.mods.sodium.client.util;

import me.jellysquid.mods.sodium.common.util.DirectionUtil;
import net.minecraft.util.EnumFacing;

/**
 * Provides some utilities and constants for interacting with vanilla's model quad vertex format.
 *
 * This is the current vertex format used by Minecraft for chunk meshes and model quads. Internally, it uses integer
 * arrays for store baked quad data, and as such the following table provides both the byte and int indices.
 *
 * Byte Index    Integer Index             Name                 Format                 Fields
 * 0 ..11        0..2                      Position             3 floats               x, y, z
 * 12..15        3                         Color                4 unsigned bytes       a, r, g, b
 * 16..23        4..5                      Block Texture        2 floats               u, v
 * 24..27        6                         Light Texture        2 shorts               u, v
 * 28..30        7                         Normal               3 unsigned bytes       x, y, z
 * 31                                      Padding              1 byte
 */
public class ModelQuadUtil {
    // Integer indices for vertex attributes, useful for accessing baked quad data
    public static final int POSITION_INDEX = 0,
            COLOR_INDEX = 3,
            TEXTURE_INDEX = 4,
            NORMAL_INDEX = 6;

    // Size of vertex format in 4-byte integers
    public static final int VERTEX_SIZE = 7;
    public static final int VERTEX_SIZE_BYTES = VERTEX_SIZE * 4;

    // Cached array of normals for every facing to avoid expensive computation
    static final int[] NORMALS = new int[DirectionUtil.ALL_DIRECTIONS.length];

    static {
        for (int i = 0; i < NORMALS.length; i++) {
            NORMALS[i] = Norm3b.pack(DirectionUtil.ALL_DIRECTIONS[i].getDirectionVec());
        }
    }

    /**
     * Returns the normal vector for a model quad with the given {@param facing}.
     */
    public static int getFacingNormal(EnumFacing facing) {
        return NORMALS[facing.ordinal()];
    }

    public static int getFacingNormal(EnumFacing facing, int bakedNormal) {
        if(!hasNormal(bakedNormal))
            return NORMALS[facing.ordinal()];
        return bakedNormal;
    }

    public static boolean hasNormal(int n) {
        return (n & 0xFFFFFF) != 0;
    }

    /**
     * @param vertexIndex The index of the vertex to access
     * @return The starting offset of the vertex's attributes
     */
    public static int vertexOffset(int vertexIndex) {
        return vertexIndex * VERTEX_SIZE;
    }

    public static int mergeBakedLight(int packedLight, int calcLight) {
        // bail early in most cases
        if (packedLight == 0)
            return calcLight;

        int psl = (packedLight >> 16) & 0xFF;
        int csl = (calcLight >> 16) & 0xFF;
        int pbl = (packedLight) & 0xFF;
        int cbl = (calcLight) & 0xFF;
        int bl = Math.max(pbl, cbl);
        int sl = Math.max(psl, csl);
        return (sl << 16) | bl;
    }
}
