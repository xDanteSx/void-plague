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

# -------------------------------------------------------------
# 1. VOID CARAPACE
# 65% basalt/charcoal: #12141A, #181B22, #1E222B
# 25% dark mineral violet: #2D1C3D, #39244D
# 10% ambient occlusion shadows: #0A0B0E
# Angular armor plates, organic bevels, NO checkerboard/grid.
# -------------------------------------------------------------
def gen_void_carapace():
    w, h = 16, 16
    grid = [[(0, 0, 0, 255) for _ in range(w)] for _ in range(h)]

    c_basalt0 = hex_to_rgb("#12141A")
    c_basalt1 = hex_to_rgb("#181B22")
    c_basalt2 = hex_to_rgb("#1E222B")
    c_violet1 = hex_to_rgb("#2D1C3D")
    c_violet2 = hex_to_rgb("#39244D")
    c_shadow  = hex_to_rgb("#0A0B0E")

    # Natural polygonal chitin plate centers (Voronoi-like plates)
    plate_centers = [(3, 4), (12, 3), (7, 10), (14, 13), (2, 13)]

    for y in range(h):
        for x in range(w):
            # Compute distance to closest and second closest plate centers
            dists = []
            for px, py in plate_centers:
                # Wrap-aware distance for seamless tiling
                dx = abs(x - px)
                dy = abs(y - py)
                dx = min(dx, 16 - dx)
                dy = min(dy, 16 - dy)
                # Add organic jitter
                jitter = math.sin(x * 1.3 + y * 0.9) * 0.4
                dists.append(math.hypot(dx, dy) + jitter)
            dists.sort()

            d1, d2 = dists[0], dists[1]
            edge_dist = d2 - d1

            # Micro-texture noise within plates
            noise = math.sin(x * 0.8 + y * 1.5) * 0.5 + math.cos(x * 1.6 - y * 0.7) * 0.5

            if edge_dist < 0.85:
                # Seam / fracture between plates: 25% mineral violet
                if edge_dist < 0.35:
                    col = c_violet2 if (x * 7 + y * 11) % 2 == 0 else c_violet1
                else:
                    # 10% Ambient occlusion shadow along plate boundary
                    col = c_shadow
            else:
                # 65% Basalt / Charcoal plate face
                if noise > 0.35:
                    col = c_basalt2
                elif noise < -0.3:
                    col = c_basalt0
                else:
                    col = c_basalt1

                # Occlusion shadow on bottom/right plate rims
                if edge_dist < 1.4 and (x + y) % 3 == 0:
                    col = c_shadow

            grid[y][x] = (*col, 255)

    return grid

# -------------------------------------------------------------
# 2. CALCIFIED CARAPACE (Quitina Calcificada / Hueso del Vacío)
# Ash gray #42454E, violet bone #585363, dark striations #282730,
# pale bone highlights #6B6578.
# -------------------------------------------------------------
def gen_calcified_carapace():
    w, h = 16, 16
    grid = [[(0, 0, 0, 255) for _ in range(w)] for _ in range(h)]

    c_ash    = hex_to_rgb("#42454E")
    c_bone   = hex_to_rgb("#585363")
    c_dark   = hex_to_rgb("#282730")
    c_high   = hex_to_rgb("#6B6578")
    c_crevice= hex_to_rgb("#1F1E26")

    for y in range(h):
        for x in range(w):
            fx = (x / 16.0) * 2.0 * math.pi
            fy = (y / 16.0) * 2.0 * math.pi

            # Fibrous bone striations oriented diagonally
            diag = (x * 1.2 + y * 0.8)
            striation = math.sin(diag * 1.8 + math.cos(fx - fy)) * 0.5 + 0.5

            # Secondary cross-lamellar osteon rings
            ring = math.sin(fx * 2.0 + math.cos(fy * 2.0)) * 0.3

            val = striation * 0.7 + ring * 0.3
            micro = (((x * 13 + y * 23) % 7) - 3) * 0.02
            val = max(0.0, min(1.0, val + micro))

            if val < 0.20:
                col = c_crevice
            elif val < 0.38:
                col = c_dark
            elif val < 0.68:
                col = c_ash
            elif val < 0.88:
                col = c_bone
            else:
                col = c_high

            grid[y][x] = (*col, 255)

    return grid

# -------------------------------------------------------------
# 3. VOID MEMBRANE (Lecho del Abismo / Vórtice Sedoso)
# Deep void #08070D, fluid undulating space violet #1C0F2B and #2A143F.
# Silky, abyssal, smooth.
# -------------------------------------------------------------
def gen_void_membrane():
    w, h = 16, 16
    grid = [[(0, 0, 0, 255) for _ in range(w)] for _ in range(h)]

    c_abyss  = hex_to_rgb("#08070D")
    c_mid1   = hex_to_rgb("#130B1E")
    c_wisp1  = hex_to_rgb("#1C0F2B")
    c_wisp2  = hex_to_rgb("#2A143F")
    c_crest  = hex_to_rgb("#381954")

    for y in range(h):
        for x in range(w):
            fx = (x / 16.0) * 2.0 * math.pi
            fy = (y / 16.0) * 2.0 * math.pi

            # Sinuous swirling cosmic currents (seamless toroidal)
            v1 = math.sin(fx + math.cos(fy * 1.5)) * 0.5 + 0.5
            v2 = math.cos(fy + math.sin(fx * 1.5)) * 0.5 + 0.5
            val = v1 * 0.6 + v2 * 0.4

            # Subtle fluid turbulence
            turb = math.sin(fx * 2.0 + fy * 2.0) * 0.08
            val = max(0.0, min(1.0, val + turb))

            if val < 0.40:
                col = lerp_rgb(c_abyss, c_mid1, val / 0.40)
            elif val < 0.70:
                col = lerp_rgb(c_mid1, c_wisp1, (val - 0.40) / 0.30)
            elif val < 0.90:
                col = lerp_rgb(c_wisp1, c_wisp2, (val - 0.70) / 0.20)
            else:
                col = lerp_rgb(c_wisp2, c_crest, (val - 0.90) / 0.10)

            grid[y][x] = (*col, 255)

    return grid

# -------------------------------------------------------------
# 4. VOID TENDRIL CARPET (Manto Multifacial con Transparencia)
# Branching chitin tendrils (#3E1C59, #612B8C, accents #7F38B5)
# with transparent gaps allowing ground to show through.
# -------------------------------------------------------------
def gen_void_tendril_carpet():
    w, h = 16, 16
    grid = [[(0, 0, 0, 0) for _ in range(w)] for _ in range(h)]

    c_dark   = hex_to_rgb("#2A123D")
    c_mid    = hex_to_rgb("#3E1C59")
    c_bright = hex_to_rgb("#612B8C")
    c_accent = hex_to_rgb("#7F38B5")

    # Organic branching vine network
    vine_pixels = set()
    # Main trunk lines
    trunk1 = [(0, 7), (1, 7), (2, 6), (3, 6), (4, 5), (5, 5), (6, 6), (7, 7),
              (8, 7), (9, 8), (10, 8), (11, 9), (12, 10), (13, 11), (14, 11), (15, 12)]
    trunk2 = [(7, 0), (7, 1), (6, 2), (6, 3), (6, 4), (6, 5), (7, 6), (7, 7),
              (8, 8), (8, 9), (9, 10), (9, 11), (10, 12), (10, 13), (11, 14), (11, 15)]
    branches = [
        (4, 5), (4, 4), (3, 3), (2, 3), (1, 2), (0, 2),
        (9, 8), (10, 7), (11, 6), (12, 6), (13, 5), (14, 5),
        (6, 6), (5, 7), (4, 8), (4, 9), (3, 10), (2, 11),
        (9, 10), (10, 10), (11, 11), (12, 11), (13, 12),
        (6, 3), (5, 2), (4, 1), (3, 1), (2, 0),
        (10, 13), (10, 14), (9, 15)
    ]

    for pt in trunk1 + trunk2 + branches:
        vine_pixels.add(pt)

    for x, y in list(vine_pixels):
        if 0 <= x < w and 0 <= y < h:
            # Core vine pixel
            col = c_mid if (x + y) % 2 == 0 else c_bright
            if (x * 3 + y * 7) % 5 == 0:
                col = c_accent
            grid[y][x] = (*col, 255)

            # Thicken slightly at intersections
            for dx, dy in [(1, 0), (0, 1)]:
                nx, ny = x + dx, y + dy
                if 0 <= nx < w and 0 <= ny < h and (nx, ny) not in vine_pixels:
                    if (x * 11 + y * 13) % 4 == 0:
                        grid[ny][nx] = (*c_dark, 220)

    return grid

# -------------------------------------------------------------
# 5. OOZING FLESH (Carne Supurante con Micro-Sombras)
# Fleshy folds in deep purples: #36123D, #4A1B54, #5C2468,
# dripping micro-nodules #742C82, crevices #220B27.
# -------------------------------------------------------------
def gen_oozing_flesh():
    w, h = 16, 16
    grid = [[(0, 0, 0, 255) for _ in range(w)] for _ in range(h)]

    c_crevice = hex_to_rgb("#220B27")
    c_deep    = hex_to_rgb("#36123D")
    c_mid     = hex_to_rgb("#4A1B54")
    c_bright  = hex_to_rgb("#5C2468")
    c_nodule  = hex_to_rgb("#742C82")

    for y in range(h):
        for x in range(w):
            fx = (x / 16.0) * 2.0 * math.pi
            fy = (y / 16.0) * 2.0 * math.pi

            # Undulating fleshy folds drooping downwards (higher vertical frequency)
            fold1 = math.sin(fx * 2.0 + math.cos(fy * 3.0))
            droop = math.sin(fy * 2.5 + fold1 * 0.5) * 0.5 + 0.5

            # Fleshy pore / bubble clusters
            bubble = math.sin(fx * 4.0) * math.cos(fy * 4.0)

            val = droop * 0.7 + bubble * 0.3
            micro = (((x * 17 + y * 29) % 7) - 3) * 0.02
            val = max(0.0, min(1.0, val + micro))

            if val < 0.22:
                col = c_crevice
            elif val < 0.50:
                col = c_deep
            elif val < 0.78:
                col = c_mid
            elif val < 0.90:
                col = c_bright
            else:
                col = c_nodule

            grid[y][x] = (*col, 255)

    return grid

# -------------------------------------------------------------
# 6. VOID STALK (Tallo Biomecánico - Cutout 16x16)
# Central spine column with segmented ribs in #2A1838, #422057, #585363.
# -------------------------------------------------------------
def gen_void_stalk():
    w, h = 16, 16
    grid = [[(0, 0, 0, 0) for _ in range(w)] for _ in range(h)]

    c_dark = hex_to_rgb("#1F102B")
    c_stem = hex_to_rgb("#351949")
    c_bone = hex_to_rgb("#585363")
    c_glow = hex_to_rgb("#7A2E9E")

    for y in range(h):
        # Stalk width varies slightly per segment (every 4 pixels a vertebra)
        seg = y % 4
        stalk_w = 2 if seg == 0 else 1

        for x in range(w):
            dx = abs(x - 7.5)
            if dx <= stalk_w:
                if seg == 0:
                    col = c_bone  # segmented bone ring
                elif seg == 2 and dx < 0.8:
                    col = c_glow  # glowing central vein
                else:
                    col = c_stem if dx < 0.8 else c_dark
                grid[y][x] = (*col, 255)
            elif dx <= stalk_w + 1.2 and seg == 1:
                # Small side thorns/spurs
                if (x + y) % 2 == 0:
                    grid[y][x] = (*c_bone, 255)

    return grid

# -------------------------------------------------------------
# 7. VOID BLOSSOM (Flor Bioluminiscente - Cutout 16x16)
# Chitin sepals #3E1C59, bioluminescent bulb core #E024C3, #FF4DE0.
# -------------------------------------------------------------
def gen_void_blossom():
    w, h = 16, 16
    grid = [[(0, 0, 0, 0) for _ in range(w)] for _ in range(h)]

    c_sepal  = hex_to_rgb("#2E1442")
    c_petal  = hex_to_rgb("#4D1A69")
    c_core   = hex_to_rgb("#A81F93")
    c_glow   = hex_to_rgb("#E024C3")
    c_bright = hex_to_rgb("#FF5AE3")
    c_spec   = hex_to_rgb("#FFD4FA")

    # Flower is centered around (7.5, 7.5)
    cx, cy = 7.5, 7.5

    for y in range(h):
        for x in range(w):
            dx = x - cx
            dy = y - cy
            dist = math.hypot(dx, dy)
            angle = math.atan2(dy, dx)

            # 4-lobed petal shape
            petal_rad = 5.0 + 1.8 * math.cos(angle * 4.0)

            if dist <= petal_rad:
                if dist > 4.2:
                    col = c_sepal
                elif dist > 2.8:
                    col = c_petal
                elif dist > 1.6:
                    col = c_core
                elif dist > 0.8:
                    col = c_glow
                else:
                    col = c_bright

                # Center highlight glint
                if dist < 0.6:
                    col = c_spec

                grid[y][x] = (*col, 255)
            elif dy > 4.5 and abs(dx) < 1.2:
                # Base stem connector
                grid[y][x] = (*c_sepal, 255)

    return grid

if __name__ == '__main__':
    base_dir = r"src/main/resources/assets/void_plague/textures/block"
    write_png(os.path.join(base_dir, "void_carapace.png"), 16, 16, gen_void_carapace())
    print("Generated void_carapace.png")
    write_png(os.path.join(base_dir, "calcified_carapace.png"), 16, 16, gen_calcified_carapace())
    print("Generated calcified_carapace.png")
    write_png(os.path.join(base_dir, "void_membrane.png"), 16, 16, gen_void_membrane())
    print("Generated void_membrane.png")
    write_png(os.path.join(base_dir, "void_tendril_carpet.png"), 16, 16, gen_void_tendril_carpet())
    print("Generated void_tendril_carpet.png")
    write_png(os.path.join(base_dir, "oozing_flesh.png"), 16, 16, gen_oozing_flesh())
    print("Generated oozing_flesh.png")
    write_png(os.path.join(base_dir, "void_stalk.png"), 16, 16, gen_void_stalk())
    print("Generated void_stalk.png")
    write_png(os.path.join(base_dir, "void_blossom.png"), 16, 16, gen_void_blossom())
    print("Generated void_blossom.png")
    print("All textures generated successfully!")
