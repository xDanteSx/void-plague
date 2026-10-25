import struct
import zlib
import os
import math

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

def matrix_to_grid(matrix_lines, palette, default_alpha=255):
    h = len(matrix_lines)
    w = len(matrix_lines[0])
    grid = []
    for row in matrix_lines:
        r_pixels = []
        for ch in row:
            col_hex = palette[ch]
            rgb = hex_to_rgb(col_hex)
            r_pixels.append((*rgb, default_alpha))
        grid.append(r_pixels)
    return grid

# =========================================================================
# 1. EXACT MATRICES FROM MODULE 3
# =========================================================================

CARAPACE_PALETTE = {
    '.': '#0D0E12',
    '1': '#14161D',
    '2': '#1B1D26',
    '3': '#232631',
    'v': '#2E1542',
    'V': '#461D63'
}

CARAPACE_MATRIX = [
    "11223321.1122211",
    "12233221.1233221",
    "1222211...122221",
    "11111..vVv..1111",
    "..111.vVVVv.1221",
    "11.11.vv1vv.2332",
    "221...v111..2322",
    "3321..11221.1221",
    "22211.123321.111",
    "12111.123221..1.",
    "111..v.12211.111",
    ".11.vVv..11..122",
    "11..vvv.1111.233",
    "221..v..1222.222",
    "2321...123321.11",
    "12211.1122211..1"
]

RIB_SIDE_PALETTE = {
    '.': '#191721',
    '1': '#3A3645',
    '2': '#524D61',
    '3': '#6B657D',
    '4': '#827B96'
}

RIB_SIDE_MATRIX = [
    "123321.123432112",
    "123321.123432112",
    "123221.123332122",
    "122211..12332123",
    "1122111.12322123",
    ".122211.12221123",
    "1123321.11221.12",
    "2234321..111..12",
    "23343211..1..123",
    "23333221.111.123",
    "12332211.1221123",
    "1222211..1232122",
    "112211..12332112",
    ".11211.1234321.1",
    "112221.1234321.1",
    "123321.123332112"
]

RIB_TOP_PALETTE = dict(RIB_SIDE_PALETTE, **{
    'm': '#241330',
    'M': '#381A4A'
})

RIB_TOP_MATRIX = [
    "1122333222111221",
    "1233443332212332",
    "2343322112323322",
    "23321.mm..123221",
    "3321.mMMm..12211",
    "322.mMM.Mm.11221",
    "221.mM..mMm.1232",
    "211.m.mm.Mm.1232",
    "112..mMMm.m.2332",
    "1221.mMMm..12321",
    "23321..m..122211",
    "234321...1233211",
    "1233321112343221",
    "1123332223332211",
    "1112233332221111",
    "1111122221111111"
]

FLESH_PALETTE = {
    '.': '#16081F',
    '1': '#2B0F3B',
    '2': '#421659',
    '3': '#5C1F78',
    'g': '#8A2BE2'
}

FLESH_MATRIX = [
    "122321.11221.122",
    "233321.123321233",
    "23221..233221221",
    "1221..122211111.",
    ".11..123g21.1112",
    "11..123321..1223",
    "221123321..12333",
    "33223321..123322",
    "2333221..123g211",
    "122211..123321..",
    ".111...123321.11",
    "11...1123221.122",
    "221.1233211..233",
    "332123g21...1232",
    "23222221..112221",
    "1221111..1222111"
]

# =========================================================================
# 2. VOID PLASMA ICHOR (16x64 px)
# Palette:
# Base 40%: #230938
# Currents 35%: #4A1478
# Veins 20%: #8E24AA, #BA68C8
# Glow Core 5%: #F3E5F5
# =========================================================================

def gen_void_plasma_ichor_still():
    w, h = 16, 64
    grid = [[(0, 0, 0, 255) for _ in range(w)] for _ in range(h)]

    c_base  = hex_to_rgb("#230938")
    c_mid   = hex_to_rgb("#4A1478")
    c_vein1 = hex_to_rgb("#8E24AA")
    c_vein2 = hex_to_rgb("#BA68C8")
    c_core  = hex_to_rgb("#F3E5F5")

    for frame in range(4):
        offset_y = frame * 16
        phase = frame * (2.0 * math.pi / 4.0)

        for y in range(16):
            fy = (y / 16.0) * 2.0 * math.pi
            for x in range(16):
                fx = (x / 16.0) * 2.0 * math.pi

                # Continuous swirling harmonics
                v1 = math.sin(fx + math.cos(fy + phase) + phase) * 0.5 + 0.5
                v2 = math.cos(fy - math.sin(fx - phase) + phase * 0.5) * 0.5 + 0.5
                v3 = math.sin(fx * 2.0 + fy * 2.0 - phase) * 0.25 + 0.25
                val = max(0.0, min(1.0, v1 * 0.45 + v2 * 0.35 + v3 * 0.20))

                if val < 0.40:
                    col = lerp_rgb(c_base, c_mid, val / 0.40)
                elif val < 0.75:
                    col = lerp_rgb(c_mid, c_vein1, (val - 0.40) / 0.35)
                elif val < 0.95:
                    col = lerp_rgb(c_vein1, c_vein2, (val - 0.75) / 0.20)
                else:
                    col = lerp_rgb(c_vein2, c_core, (val - 0.95) / 0.05)

                grid[offset_y + y][x] = (*col, 255)

    return grid

def gen_void_plasma_ichor_flow():
    w, h = 16, 64
    grid = [[(0, 0, 0, 255) for _ in range(w)] for _ in range(h)]

    c_base  = hex_to_rgb("#230938")
    c_mid   = hex_to_rgb("#4A1478")
    c_vein1 = hex_to_rgb("#8E24AA")
    c_vein2 = hex_to_rgb("#BA68C8")
    c_core  = hex_to_rgb("#F3E5F5")

    for frame in range(4):
        offset_y = frame * 16
        flow_shift = frame * 4.0
        phase = frame * (2.0 * math.pi / 4.0)

        for y in range(16):
            fy_shifted = ((y - flow_shift) % 16.0) / 16.0 * 2.0 * math.pi
            for x in range(16):
                fx = (x / 16.0) * 2.0 * math.pi

                v_flow = math.sin(fy_shifted + math.sin(fx * 2.0) * 0.4) * 0.5 + 0.5
                v_eddy = math.cos(fx * 2.0 + math.cos(fy_shifted + phase)) * 0.5 + 0.5
                val = max(0.0, min(1.0, v_flow * 0.65 + v_eddy * 0.35))

                if val < 0.40:
                    col = lerp_rgb(c_base, c_mid, val / 0.40)
                elif val < 0.75:
                    col = lerp_rgb(c_mid, c_vein1, (val - 0.40) / 0.35)
                elif val < 0.95:
                    col = lerp_rgb(c_vein1, c_vein2, (val - 0.75) / 0.20)
                else:
                    col = lerp_rgb(c_vein2, c_core, (val - 0.95) / 0.05)

                grid[offset_y + y][x] = (*col, 255)

    return grid

# =========================================================================
# 3. VOID MAW (FAUCES CARNOSAS CON COLMILLOS OSEOS) - 16x16
# =========================================================================
def gen_void_maw():
    w, h = 16, 16
    grid = [[(0, 0, 0, 0) for _ in range(w)] for _ in range(h)]

    c_gum_d   = hex_to_rgb("#1F0A2B")
    c_gum_m   = hex_to_rgb("#3B1252")
    c_gum_l   = hex_to_rgb("#591B7A")
    c_tooth_b = hex_to_rgb("#6B657D")
    c_tooth_w = hex_to_rgb("#E0DCED")
    c_cavity  = hex_to_rgb("#09020D")

    # Dark mouth cavity inside
    for y in range(3, 13):
        for x in range(3, 13):
            grid[y][x] = (*c_cavity, 255)

    # Fleshy gum border
    for y in range(1, 15):
        for x in range(1, 15):
            if x in (1, 2, 13, 14) or y in (1, 2, 13, 14):
                col = c_gum_l if (x + y) % 2 == 0 else c_gum_m
                if x in (1, 14) or y in (1, 14):
                    col = c_gum_d
                grid[y][x] = (*col, 255)

    # Sharp calcified bone teeth pointing inward
    teeth_pts = [
        # Upper teeth
        (3, 3), (5, 3), (7, 4), (8, 4), (10, 3), (12, 3),
        # Lower teeth
        (3, 12), (5, 12), (7, 11), (8, 11), (10, 12), (12, 12),
        # Left/Right fangs
        (3, 6), (3, 9), (12, 6), (12, 9)
    ]
    for tx, ty in teeth_pts:
        grid[ty][tx] = (*c_tooth_w, 255)
        # Tooth base shading
        if ty <= 3:
            grid[ty - 1][tx] = (*c_tooth_b, 255)
        elif ty >= 12:
            grid[ty + 1][tx] = (*c_tooth_b, 255)

    return grid

# =========================================================================
# 4. VOID SPINES & NERVES (AGUJAS OSEAS Y NERVIOS) - 16x16
# =========================================================================
def gen_void_spines_nerves():
    w, h = 16, 16
    grid = [[(0, 0, 0, 0) for _ in range(w)] for _ in range(h)]

    c_chitin_d = hex_to_rgb("#101117")
    c_chitin_m = hex_to_rgb("#1B1D26")
    c_bone_m   = hex_to_rgb("#524D61")
    c_bone_l   = hex_to_rgb("#7D7591")
    c_nerve    = hex_to_rgb("#BA68C8")
    c_nerve_g  = hex_to_rgb("#F3E5F5")

    # Central vertebrae spine rising from (8, 15) to (7, 1)
    spine = [
        (8, 15), (7, 15), (8, 14), (7, 14),
        (8, 13), (8, 12), (7, 12), (7, 11),
        (7, 10), (8, 10), (8, 9), (7, 8),
        (7, 7), (8, 6), (7, 5), (7, 4),
        (6, 3), (6, 2), (7, 1)
    ]
    for sx, sy in spine:
        grid[sy][sx] = (*c_bone_m, 255)
        if (sx + sy) % 3 == 0:
            grid[sy][sx] = (*c_bone_l, 255)

    # Glowing purple nerve filament in the spine core
    nerve = [(8, 13), (7, 11), (8, 9), (7, 7), (7, 5), (6, 3)]
    for nx, ny in nerve:
        grid[ny][nx] = (*c_nerve, 255)
    grid[3][6] = (*c_nerve_g, 255)

    # Black chitin needle thorns branching off
    thorns = [
        (9, 13), (10, 12), (11, 11), (12, 10),
        (6, 12), (5, 11), (4, 10), (3, 9),
        (9, 8), (10, 7), (11, 6),
        (6, 7), (5, 6), (4, 5),
        (8, 4), (9, 3)
    ]
    for tx, ty in thorns:
        col = c_chitin_m if tx in (9, 6) else c_chitin_d
        grid[ty][tx] = (*col, 255)

    return grid

# =========================================================================
# 5. VOID WATCHER EYE (TEXTURA DE BLOQUE / RENDER) - 16x16
# =========================================================================
def gen_void_watcher_eye():
    w, h = 16, 16
    grid = [[(0, 0, 0, 255) for _ in range(w)] for _ in range(h)]

    c_chitin_rim = hex_to_rgb("#12141C")
    c_flesh_d    = hex_to_rgb("#240B33")
    c_flesh_m    = hex_to_rgb("#401459")
    c_sclera_d   = hex_to_rgb("#7D7591")
    c_sclera_l   = hex_to_rgb("#D6CFE3")
    c_iris_d     = hex_to_rgb("#7B1FA2")
    c_iris_b     = hex_to_rgb("#BA68C8")
    c_pupil      = hex_to_rgb("#05010A")
    c_glint      = hex_to_rgb("#FFFFFF")

    cx, cy = 7.5, 7.5
    for y in range(h):
        for x in range(w):
            dx = x - cx
            dy = y - cy
            d = math.hypot(dx, dy)

            if d > 7.0:
                grid[y][x] = (*c_chitin_rim, 255)
            elif d > 5.5:
                col = c_flesh_m if (x + y) % 2 == 0 else c_flesh_d
                grid[y][x] = (*col, 255)
            elif d > 3.5:
                col = c_sclera_l if dy < 0 else c_sclera_d
                grid[y][x] = (*col, 255)
            elif d > 1.8:
                col = c_iris_b if dx < 0 else c_iris_d
                grid[y][x] = (*col, 255)
            else:
                # Vertical slit pupil
                if abs(dx) <= 0.7:
                    grid[y][x] = (*c_pupil, 255)
                else:
                    grid[y][x] = (*c_iris_d, 255)

    # Specular glint
    grid[6][6] = (*c_glint, 255)
    return grid

# =========================================================================
# 6. GRASPING LIMB (PALE CALCIFIED CHITIN & EXPOSED SINEW) - 16x16
# =========================================================================
def gen_grasping_limb():
    w, h = 16, 16
    grid = [[(0, 0, 0, 255) for _ in range(w)] for _ in range(h)]

    c_bone_shadow = hex_to_rgb("#3A3645")
    c_bone_mid    = hex_to_rgb("#5A5469")
    c_bone_light  = hex_to_rgb("#878099")
    c_tendon_d    = hex_to_rgb("#300D42")
    c_tendon_m    = hex_to_rgb("#571775")
    c_claw_black  = hex_to_rgb("#0F1017")

    for y in range(h):
        for x in range(w):
            if y < 4:
                # Claw fingers tips
                if x in (2, 6, 9, 13):
                    grid[y][x] = (*c_claw_black, 255)
                elif x in (3, 7, 10, 12):
                    grid[y][x] = (*c_bone_light, 255)
                else:
                    grid[y][x] = (*c_bone_shadow, 255)
            elif y < 10:
                # Hand and wrist tendons
                if (x + y) % 3 == 0:
                    grid[y][x] = (*c_tendon_m, 255)
                elif (x + y) % 2 == 0:
                    grid[y][x] = (*c_tendon_d, 255)
                else:
                    grid[y][x] = (*c_bone_mid, 255)
            else:
                # Pale calcified forearm
                if x < 4 or x > 11:
                    grid[y][x] = (*c_bone_shadow, 255)
                elif x in (4, 11):
                    grid[y][x] = (*c_bone_mid, 255)
                else:
                    grid[y][x] = (*c_bone_light, 255)

    return grid

if __name__ == '__main__':
    base_dir = r"src/main/resources/assets/void_plague/textures/block"
    os.makedirs(base_dir, exist_ok=True)

    # 1. Exact pixel matrices
    write_png(os.path.join(base_dir, "void_carapace.png"), 16, 16, matrix_to_grid(CARAPACE_MATRIX, CARAPACE_PALETTE))
    print("Generated exact void_carapace.png")

    write_png(os.path.join(base_dir, "calcified_rib_side.png"), 16, 16, matrix_to_grid(RIB_SIDE_MATRIX, RIB_SIDE_PALETTE))
    print("Generated exact calcified_rib_side.png")

    write_png(os.path.join(base_dir, "calcified_rib_top.png"), 16, 16, matrix_to_grid(RIB_TOP_MATRIX, RIB_TOP_PALETTE))
    print("Generated exact calcified_rib_top.png")

    write_png(os.path.join(base_dir, "oozing_flesh.png"), 16, 16, matrix_to_grid(FLESH_MATRIX, FLESH_PALETTE))
    print("Generated exact oozing_flesh.png")

    # 2. Void plasma ichor (still and flow)
    write_png(os.path.join(base_dir, "void_ichor_still.png"), 16, 64, gen_void_plasma_ichor_still())
    print("Generated void_ichor_still.png (plasma)")

    write_png(os.path.join(base_dir, "void_ichor_flow.png"), 16, 64, gen_void_plasma_ichor_flow())
    print("Generated void_ichor_flow.png (plasma)")

    # 3. Void Maw (mimic_orchid)
    write_png(os.path.join(base_dir, "mimic_orchid.png"), 16, 16, gen_void_maw())
    print("Generated mimic_orchid.png (void_maw)")

    # 4. Void Spines & Nerves (void_polyps)
    write_png(os.path.join(base_dir, "void_polyps.png"), 16, 16, gen_void_spines_nerves())
    print("Generated void_polyps.png (spines & nerves)")

    # 5. Void Watcher Eye
    write_png(os.path.join(base_dir, "void_watcher_eye.png"), 16, 16, gen_void_watcher_eye())
    print("Generated void_watcher_eye.png")

    # 6. Grasping Limb
    write_png(os.path.join(base_dir, "grasping_limb.png"), 16, 16, gen_grasping_limb())
    print("Generated grasping_limb.png")

    print("All textures generated successfully!")
