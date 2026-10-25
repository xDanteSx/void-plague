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

# Paleta cromática anatómica del Vacío:
# Base conectiva / quitina de anclaje: #170924 (23, 9, 36)
# Tejido muscular del saco: #2D0F3D (45, 15, 61)
# Membrana externa translúcida: #48175E (72, 23, 94)
# Venas y pústulas bioluminiscentes: #70228F (112, 34, 143) y #9C33C4 (156, 51, 196)
# Núcleos tenues: #D18CE0 (209, 140, 224)

C_CHITIN = (23, 9, 36)
C_MUSCLE = (45, 15, 61)
C_MEMBRANE = (72, 23, 94)
C_VEIN_DARK = (112, 34, 143)
C_VEIN_BRIGHT = (156, 51, 196)
C_NUCLEUS = (209, 140, 224)

def lerp(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return (
        int(c1[0] + (c2[0] - c1[0]) * t),
        int(c1[1] + (c2[1] - c1[1]) * t),
        int(c1[2] + (c2[2] - c1[2]) * t)
    )

def generate_texture():
    # 32x32 texture
    pixels = []
    for y in range(32):
        row = []
        for x in range(32):
            if y < 12:
                # TOP SECTION: Peduncle, fibrous cord, roots
                # Fibrous vertical cord with dark chitin edges
                dist_center = abs(x % 16 - 7.5) / 8.0
                if dist_center > 0.6:
                    col = C_CHITIN
                elif dist_center > 0.3:
                    col = lerp(C_CHITIN, C_MUSCLE, (dist_center - 0.3) / 0.3)
                else:
                    # Inner tendon fiber
                    fiber = math.sin(y * 1.5 + x * 2.0) * 0.5 + 0.5
                    col = lerp(C_MUSCLE, C_MEMBRANE, fiber)
            else:
                # LOWER SECTION (y >= 12): Bulbous sac body with muscle, veins, glowing pustules
                ny = (y - 12) / 20.0
                nx = (x % 16) / 16.0
                
                # Organic biological noise
                w1 = math.sin(nx * 12.0 + ny * 6.0) * 0.5 + 0.5
                w2 = math.cos(nx * 8.0 - ny * 10.0) * 0.5 + 0.5
                pustule = math.exp(-((nx - 0.5)**2 + (ny - 0.4)**2) * 25.0)
                pustule2 = math.exp(-((nx - 0.25)**2 + (ny - 0.75)**2) * 35.0)
                
                val = w1 * 0.55 + w2 * 0.45
                
                if pustule > 0.6 or pustule2 > 0.7:
                    # Glowing pustule core
                    p_val = max(pustule, pustule2)
                    col = lerp(C_VEIN_BRIGHT, C_NUCLEUS, (p_val - 0.6) / 0.4)
                elif val > 0.72:
                    # Bioluminescent vein
                    col = lerp(C_VEIN_DARK, C_VEIN_BRIGHT, (val - 0.72) / 0.28)
                elif val > 0.40:
                    # Outer translucent membrane
                    col = lerp(C_MUSCLE, C_MEMBRANE, (val - 0.40) / 0.32)
                else:
                    # Deep muscular wall
                    col = lerp(C_CHITIN, C_MUSCLE, val / 0.40)
                    
            row.append((*col, 255))
        pixels.append(row)
    return pixels

pix = generate_texture()
write_png("src/main/resources/assets/void_plague/textures/entity/incubation_sac.png", 32, 32, pix)
write_png("src/main/resources/assets/void_plague/textures/block/incubation_sac.png", 32, 32, pix)

print("Generated 32x32 incubation_sac.png in both entity and block textures")
