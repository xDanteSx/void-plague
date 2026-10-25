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

# Paleta anatómica del Vacío fría (B >= 1.8 * R):
C_CHITIN_0   = (11, 7, 20)    # #0B0714
C_CHITIN_1   = (19, 11, 36)   # #130B24
C_MUSCLE_0   = (24, 12, 48)   # #180C30
C_MUSCLE_1   = (34, 15, 69)   # #220F45
C_MEMBRANE_0 = (49, 20, 99)   # #311463
C_MEMBRANE_1 = (62, 26, 125)  # #3E1A7D
C_VEIN       = (82, 35, 163)  # #5223A3
C_ELECTRIC   = (106, 46, 204) # #6A2ECC
C_LAVENDER   = (124, 92, 230) # Specular cold lavender

# Embryo colors (jet black abyssal)
C_EMBRYO_CORE = (5, 2, 10)    # #05020A
C_EMBRYO_EDGE = (24, 7, 36)   # #180724 (B >= 1.8 * R: 36 >= 24*1.5 -> let's make 19, 9, 36)
C_EMBRYO_EDGE = (19, 9, 36)

# Ruptured sac colors (withered, dried pith, coagulated remnants)
C_DRY_PITH_0 = (16, 10, 24)
C_DRY_PITH_1 = (31, 20, 41)   # #1F1429
C_COAGULATED = (10, 4, 16)    # #0A0410
C_JAGGED_LIP = (45, 25, 58)   # Withered torn edge (B=58, R=29 -> 58 >= 1.8*29 = 52.2)
C_JAGGED_LIP = (28, 16, 54)

def lerp(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return (
        int(c1[0] + (c2[0] - c1[0]) * t),
        int(c1[1] + (c2[1] - c1[1]) * t),
        int(c1[2] + (c2[2] - c1[2]) * t)
    )

PALETTE_STOPS = [
    (0.00, C_CHITIN_0),
    (0.12, C_CHITIN_1),
    (0.28, C_MUSCLE_0),
    (0.45, C_MUSCLE_1),
    (0.62, C_MEMBRANE_0),
    (0.76, C_MEMBRANE_1),
    (0.88, C_VEIN),
    (0.95, C_ELECTRIC),
    (1.00, C_LAVENDER)
]

def get_sac_color(val):
    val = max(0.0, min(1.0, val))
    for i in range(len(PALETTE_STOPS) - 1):
        s0, c0 = PALETTE_STOPS[i]
        s1, c1 = PALETTE_STOPS[i + 1]
        if val <= s1:
            t = (val - s0) / (s1 - s0)
            return lerp(c0, c1, t)
    return PALETTE_STOPS[-1][1]

# 1. GENERATE INCUBATION_SAC.PNG (32x32)
def make_living_sac():
    pixels = []
    for y in range(32):
        row = []
        for x in range(32):
            if x >= 24 and y < 8:
                # Patch for the internal embryo core (jet-black with faint violet edge)
                dx = (x - 27.5) / 3.5
                dy = (y - 3.5) / 3.5
                d = math.sqrt(dx*dx + dy*dy)
                col = lerp(C_EMBRYO_CORE, C_EMBRYO_EDGE, min(1.0, d))
                alpha = 255
            elif y < 10:
                # Top section: pedúnculo corto y zarcillos (opaco)
                dist = abs((x % 16) - 7.5) / 8.0
                if dist > 0.65:
                    col = C_CHITIN_0
                elif dist > 0.35:
                    col = lerp(C_CHITIN_0, C_CHITIN_1, (dist - 0.35) / 0.30)
                else:
                    fiber = math.sin(y * 1.8 + x * 2.2) * 0.5 + 0.5
                    col = lerp(C_CHITIN_1, C_MUSCLE_0, fiber)
                alpha = 255
            else:
                # Lower section: bulbo translúcido con estrías longitudinales y bioluminiscencia fría
                ny = (y - 10) / 22.0
                nx = (x % 16) / 16.0
                
                # Longitudinal muscular striations (higher frequency in x for tight fibers)
                striations = math.sin(nx * 32.0 + ny * 4.0) * 0.5 + 0.5
                w1 = math.sin(nx * 10.0 + ny * 6.0) * 0.5 + 0.5
                w2 = math.cos(nx * 8.0 - ny * 10.0) * 0.5 + 0.5
                
                # Cold bioluminescent nodes
                node1 = math.exp(-((nx - 0.5)**2 + (ny - 0.38)**2) * 35.0)
                node2 = math.exp(-((nx - 0.25)**2 + (ny - 0.70)**2) * 45.0)
                node3 = math.exp(-((nx - 0.75)**2 + (ny - 0.62)**2) * 40.0)
                n_max = max(node1, node2, node3)
                
                base_v = (w1 * 0.50 + w2 * 0.35 + striations * 0.15)
                
                if n_max > 0.60:
                    val = 0.88 + ((n_max - 0.60) / 0.40) * 0.12
                    alpha = 230
                elif base_v > 0.68:
                    val = 0.75 + ((base_v - 0.68) / 0.32) * 0.13
                    alpha = 220
                elif base_v > 0.40:
                    val = 0.45 + ((base_v - 0.40) / 0.28) * 0.25
                    alpha = 210
                elif base_v > 0.18:
                    val = 0.22 + ((base_v - 0.18) / 0.22) * 0.20
                    alpha = 205
                else:
                    val = 0.08 + (base_v / 0.18) * 0.14
                    alpha = 200
                    
                col = get_sac_color(val)
                
            r, g, b = col
            if b < 1.85 * r:
                r = int(b / 1.85)
            row.append((r, g, b, alpha))
        pixels.append(row)
        
    for y in range(32):
        for x in range(32):
            r, g, b, a = pixels[y][x]
            assert b >= 1.8 * r, f"Living sac violation at ({x}, {y}): R={r}, B={b}"
    return pixels

# 2. GENERATE RUPTURED_INCUBATION_SAC.PNG (32x32)
def make_ruptured_sac():
    pixels = []
    for y in range(32):
        row = []
        for x in range(32):
            if y < 10:
                # Top section: same chitin anchor
                dist = abs((x % 16) - 7.5) / 8.0
                if dist > 0.65:
                    col = C_CHITIN_0
                elif dist > 0.35:
                    col = C_CHITIN_1
                else:
                    col = C_DRY_PITH_0
            elif y < 22:
                # Middle cup: withered jagged torn shell with coagulated dry interior
                ny = (y - 10) / 12.0
                nx = (x % 16) / 16.0
                jagged = math.sin(nx * 18.0) * 0.5 + 0.5
                tear = ny + jagged * 0.3
                
                if tear > 0.85:
                    # Torn frayed tooth edge
                    col = C_JAGGED_LIP
                elif tear > 0.5:
                    # Desaturated withered dried pith
                    col = lerp(C_DRY_PITH_1, C_JAGGED_LIP, (tear - 0.5) / 0.35)
                else:
                    # Hollow cavity / coagulated darkness
                    col = lerp(C_COAGULATED, C_DRY_PITH_0, tear / 0.5)
            else:
                # Bottom torn cavity: empty space / shreds
                col = C_COAGULATED
                
            r, g, b = col
            if b < 1.85 * r:
                r = int(b / 1.85)
            row.append((r, g, b, 255))
        pixels.append(row)
        
    for y in range(32):
        for x in range(32):
            r, g, b, a = pixels[y][x]
            assert b >= 1.8 * r, f"Ruptured sac violation at ({x}, {y}): R={r}, B={b}"
    return pixels

pix_living = make_living_sac()
write_png("src/main/resources/assets/void_plague/textures/entity/incubation_sac.png", 32, 32, pix_living)
write_png("src/main/resources/assets/void_plague/textures/block/incubation_sac.png", 32, 32, pix_living)

pix_ruptured = make_ruptured_sac()
write_png("src/main/resources/assets/void_plague/textures/entity/ruptured_incubation_sac.png", 32, 32, pix_ruptured)
write_png("src/main/resources/assets/void_plague/textures/block/ruptured_incubation_sac.png", 32, 32, pix_ruptured)

print("Generated both incubation_sac.png and ruptured_incubation_sac.png with verified cold blue dominance")
