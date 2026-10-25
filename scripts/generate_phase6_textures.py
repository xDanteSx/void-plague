import struct
import zlib
import os
import math
import random

def write_png(filename, width, height, pixels):
    raw = bytearray()
    for row in pixels:
        raw.append(0)  # filter 0
        for r, g, b, a in row:
            raw.extend((r, g, b, a))

    def chunk(tag, data):
        return struct.pack('>I', len(data)) + tag + data + struct.pack('>I', zlib.crc32(tag + data) & 0xffffffff)

    ihdr = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    idat = zlib.compress(bytes(raw), 9)

    os.makedirs(os.path.dirname(filename), exist_ok=True)
    with open(filename, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n')
        f.write(chunk(b'IHDR', ihdr))
        f.write(chunk(b'IDAT', idat))
        f.write(chunk(b'IEND', b''))

def hex_to_rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))

def lerp_rgb(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return (
        int(c1[0] + (c2[0] - c1[0]) * t),
        int(c1[1] + (c2[1] - c1[1]) * t),
        int(c1[2] + (c2[2] - c1[2]) * t)
    )

# =========================================================================
# 1. VOID ICHOR STILL & FLOW (16x64: 4 frames of 16x16)
# Deep abyssal liquid petroleum:
# Base petrol: #09040E
# Thick oil swirls: #160824
# Subtle fluid veins: #280D3D
# ZERO isolated pixels, ZERO bright magenta confetti / specks.
# Smooth wave gradients.
# =========================================================================
def gen_void_ichor_still():
    w, h = 16, 64
    grid = [[(0, 0, 0, 255) for _ in range(w)] for _ in range(h)]

    c_petrol = hex_to_rgb("#09040E")
    c_oil    = hex_to_rgb("#160824")
    c_vein   = hex_to_rgb("#280D3D")

    for frame in range(4):
        frame_offset_y = frame * 16
        # Continuous cyclic phase for seamless 4-frame animation loop
        phase = frame * (2.0 * math.pi / 4.0)

        for y in range(16):
            for x in range(16):
                fx = (x / 16.0) * 2.0 * math.pi
                fy = (y / 16.0) * 2.0 * math.pi

                # Large, smooth continuous harmonic field (no isolated speckles)
                v1 = math.sin(fx + math.cos(fy + phase) + phase) * 0.5 + 0.5
                v2 = math.cos(fy - math.sin(fx - phase) + phase * 0.5) * 0.5 + 0.5
                val = max(0.0, min(1.0, v1 * 0.6 + v2 * 0.4))

                # Smooth gradient color ramping
                if val < 0.5:
                    col = lerp_rgb(c_petrol, c_oil, val / 0.5)
                else:
                    col = lerp_rgb(c_oil, c_vein, (val - 0.5) / 0.5)

                grid[frame_offset_y + y][x] = (*col, 255)

    return grid

def gen_void_ichor_flow():
    w, h = 16, 64
    grid = [[(0, 0, 0, 255) for _ in range(w)] for _ in range(h)]

    c_petrol = hex_to_rgb("#09040E")
    c_oil    = hex_to_rgb("#160824")
    c_vein   = hex_to_rgb("#280D3D")

    for frame in range(4):
        frame_offset_y = frame * 16
        flow_shift = frame * 4.0
        phase = frame * (2.0 * math.pi / 4.0)

        for y in range(16):
            fy_shifted = ((y - flow_shift) % 16.0) / 16.0 * 2.0 * math.pi

            for x in range(16):
                fx = (x / 16.0) * 2.0 * math.pi

                # Smooth laminar flow lines with viscous shearing
                v_flow = math.sin(fy_shifted + math.sin(fx) * 0.4) * 0.5 + 0.5
                v_eddy = math.cos(fx + math.cos(fy_shifted + phase)) * 0.5 + 0.5
                val = max(0.0, min(1.0, v_flow * 0.65 + v_eddy * 0.35))

                if val < 0.5:
                    col = lerp_rgb(c_petrol, c_oil, val / 0.5)
                else:
                    col = lerp_rgb(c_oil, c_vein, (val - 0.5) / 0.5)

                grid[frame_offset_y + y][x] = (*col, 255)

    return grid

# =========================================================================
# 2. VOID CARAPACE (Roca de Quitina Abisal - 16x16)
# STRICTLY NO DIAGONAL PATTERNS / CARBON FIBER.
# Basalt (#111217), Charcoal Slate (#171920), Obsidian (#1E212A).
# Fractures in Mineral Violet (#2A163B).
# Occlusion shadows in #0A0B0E.
# =========================================================================
def gen_void_carapace():
    w, h = 16, 16
    grid = [[(0, 0, 0, 255) for _ in range(w)] for _ in range(h)]

    c_occl   = hex_to_rgb("#0A0B0E")
    c_basalt = hex_to_rgb("#111217")
    c_slate  = hex_to_rgb("#171920")
    c_obsi   = hex_to_rgb("#1E212A")
    c_obsi_h = hex_to_rgb("#242833")
    c_frac1  = hex_to_rgb("#2A163B")
    c_frac2  = hex_to_rgb("#381E4F")

    # Organic non-diagonal, hand-placed polygonal plate centers for seamless 16x16 tiling
    plate_seeds = [
        (3.0, 3.5),
        (12.5, 4.0),
        (4.0, 12.0),
        (11.5, 11.5),
        (8.0, 7.5)
    ]

    # Pre-calculated seamless distance field with organic non-diagonal noise
    for y in range(h):
        for x in range(w):
            dists = []
            for idx, (px, py) in enumerate(plate_seeds):
                # Toroidal distance for seamless tiling
                dx = abs(x - px)
                dy = abs(y - py)
                dx = min(dx, 16.0 - dx)
                dy = min(dy, 16.0 - dy)

                # Irregular non-diagonal organic deformation
                warp = math.sin(x * 1.7) * 0.5 + math.cos(y * 1.9) * 0.5 + math.sin((x * 2.3 + idx) * 0.7) * 0.3
                d = math.hypot(dx, dy) + warp
                dists.append((d, idx))

            dists.sort(key=lambda t: t[0])
            d1, idx1 = dists[0]
            d2, idx2 = dists[1]
            diff = d2 - d1

            # Micro-roughness
            rand_val = math.sin(x * 13.7 + y * 29.3 + idx1 * 5.1) * 0.5 + 0.5

            if diff < 0.45:
                # Mineral violet jagged fracture line
                col = c_frac2 if rand_val > 0.65 else c_frac1
            elif diff < 0.95:
                # Ambient occlusion shadow on fracture edges
                col = c_occl
            else:
                # Solid stone plate body
                if diff > 2.8 and rand_val > 0.60:
                    # Obsidian high sheen facet
                    col = c_obsi_h if rand_val > 0.85 else c_obsi
                elif rand_val > 0.40:
                    col = c_slate
                elif rand_val > 0.15:
                    col = c_basalt
                else:
                    col = c_occl

            grid[y][x] = (*col, 255)

    return grid

# =========================================================================
# 3. VOID TENDRIL CARPET (Manto de Zarcillos / Micelio - 16x16)
# STRICT TRANSPARENCY: >= 60% Alpha = 0 (only ~70-95 opaque pixels).
# Root-like branching chitin tendrils from borders inward in:
# Dark Violet (#33134A) and Muted Fuchsia (#5E1E7D), nodes (#7A289E).
# NO "+" or "X" crosses, NO checkerboard dance floor!
# =========================================================================
def gen_void_tendril_carpet():
    w, h = 16, 16
    grid = [[(0, 0, 0, 0) for _ in range(w)] for _ in range(h)]

    c_root_d = hex_to_rgb("#250D36")
    c_root_m = hex_to_rgb("#33134A")
    c_root_f = hex_to_rgb("#5E1E7D")
    c_node   = hex_to_rgb("#7A289E")

    # Artisanal creeping mycelium tendrils connecting seamlessly across boundaries
    # Path 1: crawling from left border (0, 4) winding to (7, 6) then splitting to (15, 8) and (8, 0)
    tendril_pts = [
        # Main root A: (0, 4) -> (7, 6) -> (15, 8)
        (0, 4), (1, 4), (2, 5), (3, 5), (4, 6), (5, 6), (6, 6), (7, 6),
        (8, 7), (9, 7), (10, 7), (11, 8), (12, 8), (13, 8), (14, 8), (15, 8),
        # Branch A1 curving up to top edge:
        (7, 5), (7, 4), (8, 3), (8, 2), (8, 1), (8, 0),
        # Sub-branch A2 reaching to (2, 1):
        (3, 4), (2, 3), (2, 2), (1, 1),
        # Main root B from bottom edge (11, 15) climbing and winding to (9, 10):
        (11, 15), (11, 14), (10, 13), (10, 12), (9, 11), (9, 10), (8, 9), (8, 8),
        # Branch B1 reaching toward bottom-left:
        (9, 12), (8, 13), (7, 13), (6, 14), (5, 14), (4, 15),
        # Branch B2 toward right:
        (10, 11), (11, 11), (12, 12), (13, 12), (14, 13), (15, 13),
        # Delicate tendril twig near center-top:
        (9, 4), (10, 3), (11, 3), (12, 2),
        # Minor sprout on left bottom:
        (1, 8), (1, 9), (2, 10), (3, 10)
    ]

    tendril_set = set(tendril_pts)

    # Thicken slightly at natural organic nodes
    node_pts = [(7, 6), (8, 7), (9, 10), (10, 12), (2, 5), (11, 8)]
    for nx, ny in node_pts:
        for ox, oy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
            if (nx + ox, ny + oy) not in tendril_set and random.random() < 0.4:
                tendril_set.add((nx + ox, ny + oy))

    opaque_count = 0
    for x, y in tendril_set:
        if 0 <= x < 16 and 0 <= y < 16:
            # Color based on hierarchy
            if (x, y) in node_pts:
                col = c_node
                alpha = 255
            else:
                dist_to_node = min(math.hypot(x - nx, y - ny) for nx, ny in node_pts)
                if dist_to_node < 1.8:
                    col = c_root_f
                    alpha = 250
                elif dist_to_node < 3.5:
                    col = c_root_m
                    alpha = 240
                else:
                    col = c_root_d
                    alpha = 230

            grid[y][x] = (*col, alpha)
            opaque_count += 1

    print(f"Void Tendril Carpet: {opaque_count} opaque pixels out of 256 ({100.0 * (256 - opaque_count) / 256.0:.1f}% transparent)")
    return grid

# =========================================================================
# 4. CALCIFIED RIB (SIDE & TOP) (Hueso Fosilizado - 16x16)
# NO concentric speaker circles!
# SIDE: Longitudinal mineralized ivory striations (#5A5663, #6D6878),
#       dark cracks/pores (#1E1B26, #3A3644).
# TOP: Irregular titan cross-section, cortical bone edge, spongy dark
#      marrow (#2D2538), porous pits.
# =========================================================================
def gen_calcified_rib_side():
    w, h = 16, 16
    grid = [[(0, 0, 0, 255) for _ in range(w)] for _ in range(h)]

    c_fissure = hex_to_rgb("#1E1B26")
    c_shadow  = hex_to_rgb("#3A3644")
    c_ivory   = hex_to_rgb("#5A5663")
    c_pale    = hex_to_rgb("#6D6878")
    c_ridge   = hex_to_rgb("#827C8E")

    # Longitudinal striations parallel to the bone axis (running vertically)
    for y in range(h):
        for x in range(w):
            fx = (x / 16.0) * 2.0 * math.pi
            fy = (y / 16.0) * 2.0 * math.pi

            # Vertical longitudinal grain lines
            grain1 = math.sin(fx * 3.0 + math.sin(fy * 1.0) * 0.4) * 0.5 + 0.5
            grain2 = math.sin(fx * 7.0 + math.cos(fy * 2.0) * 0.3) * 0.25 + 0.25

            # Longitudinal micro-fibers
            fiber = math.sin(fx * 11.0) * 0.15

            val = max(0.0, min(1.0, grain1 * 0.6 + grain2 * 0.3 + fiber))

            # Vertical fossil cracks (irregular weathered grooves)
            crack_pos = (x == 2 and (y < 6 or y > 10)) or (x == 9 and (3 <= y <= 13)) or (x == 14 and (y < 4 or y > 11))

            if crack_pos:
                col = c_fissure
            elif val < 0.22:
                col = c_shadow
            elif val < 0.55:
                col = c_ivory
            elif val < 0.82:
                col = c_pale
            else:
                col = c_ridge

            grid[y][x] = (*col, 255)

    return grid

def gen_calcified_rib_top():
    w, h = 16, 16
    grid = [[(0, 0, 0, 255) for _ in range(w)] for _ in range(h)]

    c_rim_dark = hex_to_rgb("#1E1B26")
    c_cortex_d = hex_to_rgb("#45404E")
    c_cortex_m = hex_to_rgb("#5A5663")
    c_cortex_l = hex_to_rgb("#6D6878")
    c_marrow_d = hex_to_rgb("#1C1724")
    c_marrow_m = hex_to_rgb("#2D2538")
    c_marrow_b = hex_to_rgb("#3D334A")

    # Asymmetrical oval titan rib cross-section (not circular!)
    cx, cy = 7.5, 7.5

    for y in range(h):
        for x in range(w):
            dx = x - cx
            dy = y - cy
            # Elongated oval cross-section rotated slightly
            dist_sq = (dx * 0.95)**2 + (dy * 1.15)**2
            dist = math.sqrt(dist_sq)

            # Irregular boundary modulation
            angle = math.atan2(dy, dx)
            contour = 6.2 + math.cos(angle * 3.0) * 0.6 + math.sin(angle * 5.0) * 0.3

            # Organic sponge noise in the marrow
            sponge_noise = math.sin(x * 4.3 + y * 7.1) * 0.5 + math.cos(x * 6.7 - y * 3.9) * 0.5

            if dist > contour + 0.8:
                # Outer perimeter corner bevel / fissure
                col = c_rim_dark
            elif dist > contour - 1.6:
                # Dense cortical bone outer ring
                rel_cort = (dist - (contour - 1.6)) / 2.4
                if rel_cort > 0.7:
                    col = c_cortex_d
                elif rel_cort > 0.3:
                    col = c_cortex_m
                else:
                    col = c_cortex_l
            else:
                # Spongy cancellous marrow (trabecular network)
                if sponge_noise < -0.35:
                    col = c_marrow_d  # Marrow pore cavity
                elif sponge_noise < 0.25:
                    col = c_marrow_m  # Spongy marrow tissue
                else:
                    col = c_marrow_b  # Bone trabecula bridge

            grid[y][x] = (*col, 255)

    return grid

# =========================================================================
# 5. VOID POLYPS (Pólipos Abisales - 16x16)
# Fondo 100% transparente.
# Stems and tentacles in Electric Orchid (#9C27B0, #BA68C8).
# Incandescent white bulb tips (#F3E5F5, #FFFFFF).
# Deep-sea hydrothermal biomechanic anemone.
# =========================================================================
def gen_void_polyps():
    w, h = 16, 16
    grid = [[(0, 0, 0, 0) for _ in range(w)] for _ in range(h)]

    c_base   = hex_to_rgb("#4A148C")
    c_stem_d = hex_to_rgb("#7B1FA2")
    c_stem_m = hex_to_rgb("#9C27B0")
    c_stem_l = hex_to_rgb("#BA68C8")
    c_glow   = hex_to_rgb("#E1BEE7")
    c_bulb   = hex_to_rgb("#F3E5F5")
    c_white  = hex_to_rgb("#FFFFFF")

    # Artisanal abyssal polyp cluster: 3 main anemone stalks rising and blooming
    # Stalk 1 (Left): base at (4, 15) -> crown at (3, 6)
    stalk1 = [
        (4, 15, c_base), (4, 14, c_base), (4, 13, c_stem_d), (3, 12, c_stem_d),
        (3, 11, c_stem_m), (3, 10, c_stem_m), (2, 9, c_stem_l), (2, 8, c_stem_l),
        (3, 7, c_glow), (3, 6, c_bulb), (2, 5, c_white), (4, 5, c_white),
        (1, 6, c_glow), (4, 6, c_glow), (3, 5, c_white)
    ]

    # Stalk 2 (Center-Tall): base at (8, 15) -> crown at (8, 2)
    stalk2 = [
        (8, 15, c_base), (7, 15, c_base), (8, 14, c_base), (8, 13, c_stem_d),
        (7, 12, c_stem_d), (8, 11, c_stem_m), (8, 10, c_stem_m), (8, 9, c_stem_m),
        (9, 8, c_stem_l), (8, 7, c_stem_l), (8, 6, c_stem_l), (7, 5, c_glow),
        (8, 4, c_glow), (8, 3, c_bulb), (7, 2, c_white), (8, 2, c_white), (9, 2, c_white),
        (6, 3, c_bulb), (10, 3, c_bulb), (8, 1, c_white)
    ]

    # Stalk 3 (Right): base at (11, 15) -> crown at (12, 7)
    stalk3 = [
        (11, 15, c_base), (11, 14, c_base), (12, 13, c_stem_d), (12, 12, c_stem_d),
        (12, 11, c_stem_m), (13, 10, c_stem_m), (13, 9, c_stem_l), (12, 8, c_glow),
        (12, 7, c_bulb), (11, 6, c_white), (13, 6, c_white), (14, 7, c_glow),
        (12, 6, c_white)
    ]

    # Minor bioluminescent tendril sprouts
    sprouts = [
        (5, 13, c_stem_d), (6, 12, c_stem_m), (6, 11, c_bulb),
        (10, 13, c_stem_d), (10, 11, c_stem_m), (11, 10, c_bulb)
    ]

    for pt in stalk1 + stalk2 + stalk3 + sprouts:
        x, y, col = pt
        if 0 <= x < 16 and 0 <= y < 16:
            alpha = 255 if col in (c_white, c_bulb, c_glow) else 245
            grid[y][x] = (*col, alpha)

    return grid

if __name__ == '__main__':
    base_dir = r"src/main/resources/assets/void_plague/textures/block"
    os.makedirs(base_dir, exist_ok=True)

    # 1. Void Ichor Still & Flow
    write_png(os.path.join(base_dir, "void_ichor_still.png"), 16, 64, gen_void_ichor_still())
    print("Generated animated void_ichor_still.png (16x64)")

    write_png(os.path.join(base_dir, "void_ichor_flow.png"), 16, 64, gen_void_ichor_flow())
    print("Generated animated void_ichor_flow.png (16x64)")

    # 2. Void Carapace
    write_png(os.path.join(base_dir, "void_carapace.png"), 16, 16, gen_void_carapace())
    print("Generated void_carapace.png")

    # 3. Void Tendril Carpet
    write_png(os.path.join(base_dir, "void_tendril_carpet.png"), 16, 16, gen_void_tendril_carpet())
    print("Generated void_tendril_carpet.png")

    # 4. Calcified Rib Side & Top
    write_png(os.path.join(base_dir, "calcified_rib_side.png"), 16, 16, gen_calcified_rib_side())
    print("Generated calcified_rib_side.png")

    write_png(os.path.join(base_dir, "calcified_rib_top.png"), 16, 16, gen_calcified_rib_top())
    print("Generated calcified_rib_top.png")

    # 5. Void Polyps
    write_png(os.path.join(base_dir, "void_polyps.png"), 16, 16, gen_void_polyps())
    print("Generated void_polyps.png")

    print("Phase 6 textures generated successfully!")
