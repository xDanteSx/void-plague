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

# Matriz de paleta fría del Vacío:
# 1. Quitina de soporte y zarcillos:
#    #0B0714 (R:11, G:7, B:20)
#    #130B24 (R:19, G:11, B:36)
# 2. Músculo base del bulbo:
#    #180C30 (R:24, G:12, B:48)
#    #220F45 (R:34, G:15, B:69)
# 3. Membrana translúcida media:
#    #311463 (R:49, G:20, B:99)
#    #3E1A7D (R:62, G:26, B:125)
# 4. Vetas bioluminiscentes frías:
#    #5223A3 (R:82, G:35, B:163)
#    #6A2ECC (R:106, G:46, B:204)
# 5. Reflejos especulares (Lavanda estelar fría):
#    (R:124, G:92, B:230) -> B/R = 1.854 >= 1.8

PALETTE_STOPS = [
    (0.00, (11, 7, 20)),    # #0B0714
    (0.12, (19, 11, 36)),   # #130B24
    (0.28, (24, 12, 48)),   # #180C30
    (0.45, (34, 15, 69)),   # #220F45
    (0.62, (49, 20, 99)),   # #311463
    (0.76, (62, 26, 125)),  # #3E1A7D
    (0.88, (82, 35, 163)),  # #5223A3
    (0.95, (106, 46, 204)), # #6A2ECC
    (1.00, (124, 92, 230))  # Lavanda estelar fría (B >= 1.8 * R)
]

def lerp(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return (
        int(c1[0] + (c2[0] - c1[0]) * t),
        int(c1[1] + (c2[1] - c1[1]) * t),
        int(c1[2] + (c2[2] - c1[2]) * t)
    )

def get_cold_color(val):
    val = max(0.0, min(1.0, val))
    for i in range(len(PALETTE_STOPS) - 1):
        s0, c0 = PALETTE_STOPS[i]
        s1, c1 = PALETTE_STOPS[i + 1]
        if val <= s1:
            t = (val - s0) / (s1 - s0)
            return lerp(c0, c1, t)
    return PALETTE_STOPS[-1][1]

def generate_texture():
    pixels = []
    for y in range(32):
        row = []
        for x in range(32):
            if y < 10:
                # Top section: Pedúnculo corto y raíces
                # Anchoring chitin and fibrous core
                dist = abs((x % 16) - 7.5) / 8.0
                if dist > 0.65:
                    val = 0.05
                elif dist > 0.35:
                    val = 0.15
                else:
                    fiber = math.sin(y * 1.8 + x * 2.2) * 0.5 + 0.5
                    val = 0.20 + fiber * 0.15
            else:
                # Lower section: Bulbo hinchado con músculo índigo y venas frías
                ny = (y - 10) / 22.0
                nx = (x % 16) / 16.0
                
                # Organic cold muscular folds
                w1 = math.sin(nx * 10.0 + ny * 6.0) * 0.5 + 0.5
                w2 = math.cos(nx * 8.0 - ny * 10.0) * 0.5 + 0.5
                
                # Bioluminescent cold nodes
                node1 = math.exp(-((nx - 0.5)**2 + (ny - 0.38)**2) * 35.0)
                node2 = math.exp(-((nx - 0.25)**2 + (ny - 0.70)**2) * 45.0)
                node3 = math.exp(-((nx - 0.75)**2 + (ny - 0.62)**2) * 40.0)
                n_max = max(node1, node2, node3)
                
                base_v = w1 * 0.55 + w2 * 0.45
                
                if n_max > 0.60:
                    # Specular cold lavender / electric cold purple
                    val = 0.88 + ((n_max - 0.60) / 0.40) * 0.12
                elif base_v > 0.70:
                    # Cold bioluminescent veins (#5223A3, #6A2ECC)
                    val = 0.75 + ((base_v - 0.70) / 0.30) * 0.13
                elif base_v > 0.42:
                    # Deep indigo translucent membrane
                    val = 0.45 + ((base_v - 0.42) / 0.28) * 0.25
                elif base_v > 0.20:
                    # Abyssal dark violet muscle
                    val = 0.25 + ((base_v - 0.20) / 0.22) * 0.18
                else:
                    # Cold carbon chitin
                    val = 0.08 + (base_v / 0.20) * 0.14
                    
            r, g, b = get_cold_color(val)
            
            # REGLA CROMÁTICA MATEMÁTICA ESTRICTA:
            # B DEBE ser al menos 1.8 veces mayor que R:
            if b < 1.85 * r:
                r = int(b / 1.85)
                
            row.append((r, g, b, 255))
        pixels.append(row)
        
    # Verify strict math rule across ALL 1024 pixels:
    for y in range(32):
        for x in range(32):
            r, g, b, a = pixels[y][x]
            assert b >= 1.8 * r, f"Violation at ({x}, {y}): R={r}, B={b}"
            
    return pixels

pix = generate_texture()
write_png("src/main/resources/assets/void_plague/textures/entity/incubation_sac.png", 32, 32, pix)
write_png("src/main/resources/assets/void_plague/textures/block/incubation_sac.png", 32, 32, pix)
print("SUCCESS: 32x32 incubation_sac.png generated with 100% verified B >= 1.8 * R mathematical dominance!")
