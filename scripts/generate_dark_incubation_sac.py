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

# Paleta anatómica del Vacío:
# Quitina de anclaje y raíces: Negro carbón violáceo (#0D0714, #140B1F)
# Capa muscular base del saco: Violeta abisal oscuro (#1D0B2B, #260E38)
# Membrana translúcida externa: Púrpura mineral profundo (#35144D, #461B66)
# Venas orgánicas tenues: Amatista oscura (#5E2287)
# Puntos de bioluminiscencia tenue: Lavanda fría (#7E34B3, #9949D6)

C_CHITIN_0 = (13, 7, 20)    # #0D0714
C_CHITIN_1 = (20, 11, 31)   # #140B1F
C_MUSCLE_0 = (29, 11, 43)   # #1D0B2B
C_MUSCLE_1 = (38, 14, 56)   # #260E38
C_MEMBRANE_0 = (53, 20, 77) # #35144D
C_MEMBRANE_1 = (70, 27, 102)# #461B66
C_VEIN = (94, 34, 135)      # #5E2287
C_GLOW_0 = (126, 52, 179)   # #7E34B3
C_GLOW_1 = (153, 73, 214)   # #9949D6

def lerp(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return (
        int(c1[0] + (c2[0] - c1[0]) * t),
        int(c1[1] + (c2[1] - c1[1]) * t),
        int(c1[2] + (c2[2] - c1[2]) * t)
    )

def generate_texture():
    pixels = []
    for y in range(32):
        row = []
        for x in range(32):
            if y < 10:
                # TOP SECTION: Pedúnculo corto y raíces de fijación
                dist_center = abs((x % 16) - 7.5) / 8.0
                if dist_center > 0.65:
                    col = C_CHITIN_0
                elif dist_center > 0.35:
                    col = lerp(C_CHITIN_0, C_CHITIN_1, (dist_center - 0.35) / 0.30)
                else:
                    # Fibra conectiva interna
                    fiber = math.sin(y * 2.0 + x * 1.5) * 0.5 + 0.5
                    col = lerp(C_CHITIN_1, C_MUSCLE_0, fiber)
            else:
                # LOWER SECTION (y >= 10): Bulbo ovoide hinchado y tenso
                ny = (y - 10) / 22.0
                nx = (x % 16) / 16.0
                
                # Biomechanical muscular striations and tense stretched membrane
                w1 = math.sin(nx * 10.0 + ny * 5.0) * 0.5 + 0.5
                w2 = math.cos(nx * 6.0 - ny * 8.0) * 0.5 + 0.5
                
                # Small, deep bioluminescent nodes
                pustule1 = math.exp(-((nx - 0.5)**2 + (ny - 0.4)**2) * 32.0)
                pustule2 = math.exp(-((nx - 0.25)**2 + (ny - 0.72)**2) * 45.0)
                pustule3 = math.exp(-((nx - 0.78)**2 + (ny - 0.6)**2) * 40.0)
                
                val = w1 * 0.60 + w2 * 0.40
                p_max = max(pustule1, pustule2, pustule3)
                
                if p_max > 0.65:
                    # Lavanda fría bioluminiscente tenue
                    col = lerp(C_GLOW_0, C_GLOW_1, (p_max - 0.65) / 0.35)
                elif val > 0.75:
                    # Amatista oscura (venas orgánicas tenues)
                    col = lerp(C_MEMBRANE_1, C_VEIN, (val - 0.75) / 0.25)
                elif val > 0.45:
                    # Púrpura mineral profundo (membrana translúcida externa)
                    col = lerp(C_MEMBRANE_0, C_MEMBRANE_1, (val - 0.45) / 0.30)
                elif val > 0.22:
                    # Violeta abisal oscuro (capa muscular base)
                    col = lerp(C_MUSCLE_0, C_MUSCLE_1, (val - 0.22) / 0.23)
                else:
                    # Negro carbón violáceo en hendiduras
                    col = lerp(C_CHITIN_1, C_MUSCLE_0, val / 0.22)
                    
            row.append((*col, 255))
        pixels.append(row)
    return pixels

pix = generate_texture()
write_png("src/main/resources/assets/void_plague/textures/entity/incubation_sac.png", 32, 32, pix)
write_png("src/main/resources/assets/void_plague/textures/block/incubation_sac.png", 32, 32, pix)

print("Generated refined dark abyssal violet incubation_sac.png")
