package dev.booky.cloudlobby.jump;
// Created by booky10 in CloudLobby (2:38 AM 09.09.2026)

import org.bukkit.Material;
import org.bukkit.block.BlockType;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Random;

@NullMarked
public record JumpMaterial(Material glass, Material concrete) {

    public static final JumpMaterial WHITE = new JumpMaterial(Material.WHITE_STAINED_GLASS, Material.WHITE_CONCRETE);
    public static final JumpMaterial GRAY = new JumpMaterial(Material.GRAY_STAINED_GLASS, Material.GRAY_CONCRETE);
    public static final JumpMaterial BLACK = new JumpMaterial(Material.BLACK_STAINED_GLASS, Material.BLACK_CONCRETE);
    public static final JumpMaterial RED = new JumpMaterial(Material.RED_STAINED_GLASS, Material.RED_CONCRETE);
    public static final JumpMaterial ORANGE = new JumpMaterial(Material.ORANGE_STAINED_GLASS, Material.ORANGE_CONCRETE);
    public static final JumpMaterial YELLOW = new JumpMaterial(Material.YELLOW_STAINED_GLASS, Material.YELLOW_CONCRETE);
    public static final JumpMaterial LIME = new JumpMaterial(Material.LIME_STAINED_GLASS, Material.LIME_CONCRETE);
    public static final JumpMaterial CYAN = new JumpMaterial(Material.CYAN_STAINED_GLASS, Material.CYAN_CONCRETE);
    public static final JumpMaterial PURPLE = new JumpMaterial(Material.PURPLE_STAINED_GLASS, Material.PURPLE_CONCRETE);
    public static final JumpMaterial MAGENTA = new JumpMaterial(Material.MAGENTA_STAINED_GLASS, Material.MAGENTA_CONCRETE);
    public static final JumpMaterial PINK = new JumpMaterial(Material.PINK_STAINED_GLASS, Material.PINK_CONCRETE);

    public static final List<JumpMaterial> MATERIALS = List.of(WHITE, GRAY, BLACK, RED, ORANGE, YELLOW, LIME, CYAN, PURPLE, MAGENTA, PINK);

    public static JumpMaterial provideRandom(Random random) {
        return MATERIALS.get(random.nextInt(MATERIALS.size()));
    }
}
