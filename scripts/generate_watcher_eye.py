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

# Paleta exacta con B >= 1.8 * R:
# Cuenca y quitina perimetral:
C_CHITIN_0   = (11, 7, 20)     # #0B0714
C_CHITIN_1   = (20, 11, 36)    # #160B24 (20*1.8 = 36 <= 36)

# Músculos y pliegues internos:
C_MUSCLE_0   = (32, 15, 59)    # #260F3B (32*1.8 = 57.6 <= 59)
C_MUSCLE_1   = (46, 21, 84)    # #381554 (46*1.8 = 82.8 <= 84)

# Esclera necrótica (gris azulado frío pálido):
C_SCLERA_0   = (65, 85, 120)   # #6B6578 (65*1.8 = 117 <= 120)
C_SCLERA_1   = (80, 105, 150)  # #888096 (80*1.8 = 144 <= 150)
C_SCLERA_VEIN= (60, 45, 115)   # Fine cold violet veins

# Iris del Vacío:
C_IRIS_0     = (76, 30, 138)   # #5A1E8A (76*1.8 = 136.8 <= 138)
C_IRIS_1     = (117, 46, 212)  # #7B2ED4 (117*1.8 = 210.6 <= 212)

# Pupila en hendidura vertical:
C_PUPIL      = (2, 1, 5)       # #020105

# Reflejo especular:
C_SPECULAR   = (126, 108, 232) # #A86CE8 (126*1.8 = 226.8 <= 232)

def lerp(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return (
        int(c1[0] + (c2[0] - c1[0]) * t),
        int(c1[1] + (c2[1] - c1[1]) * t),
        int(c1[2] + (c2[2] - c1[2]) * t)
    )

def make_texture():
    pixels = []
    for y in range(32):
        row = []
        for x in range(32):
            if x < 16 and y < 16:
                # 1. CUENCA Y QUITINA PERIMETRAL (x: 0..15, y: 0..15)
                nx = (x - 7.5) / 7.5
                ny = (y - 7.5) / 7.5
                dist = math.sqrt(nx*nx + ny*ny)
                stria = math.sin(x * 1.5 + y * 2.0) * 0.5 + 0.5
                col = lerp(C_CHITIN_0, C_CHITIN_1, min(1.0, dist * 0.7 + stria * 0.3))

            elif x >= 16 and y < 16:
                # 2. PLIEGUES MUSCULARES CONCÉNTRICOS (x: 16..31, y: 0..15)
                nx = (x - 23.5) / 7.5
                ny = (y - 7.5) / 7.5
                dist = math.sqrt(nx*nx + ny*ny)
                ring = math.sin(dist * 12.0) * 0.5 + 0.5
                col = lerp(C_MUSCLE_0, C_MUSCLE_1, ring)

            elif x < 16 and y >= 16:
                # 3. ESCLERA NECRÓTICA LATERAL Y TRASERA (x: 0..15, y: 16..31)
                nx = x / 15.0
                ny = (y - 16) / 15.0
                base_noise = math.sin(nx * 8.0 + ny * 6.0) * 0.5 + 0.5
                vein = math.sin(nx * 18.0 - ny * 12.0) * 0.5 + 0.5
                if vein > 0.85:
                    col = C_SCLERA_VEIN
                else:
                    col = lerp(C_SCLERA_0, C_SCLERA_1, base_noise)

            else:
                # 4. CARA FRONTAL DEL OJO (Iris + Hendidura + Pupila) (x: 16..31, y: 16..31)
                cx = x - 23.5
                cy = y - 23.5
                r = math.sqrt(cx*cx + cy*cy)
                angle = math.atan2(cy, cx)

                # Pupila vertical en hendidura
                is_pupil = abs(cx) <= (0.85 + 0.35 * (1.0 - abs(cy) / 5.5)) and abs(cy) <= 5.2
                is_specular = (x == 25 and y == 21) or (x == 25 and y == 22)

                if is_specular:
                    col = C_SPECULAR
                elif is_pupil:
                    col = C_PUPIL
                elif r <= 7.0:
                    # Iris con fibras radiales
                    fiber = math.sin(angle * 12.0 + r * 2.0) * 0.5 + 0.5
                    t_r = (r - 1.0) / 6.0
                    col = lerp(C_IRIS_1, C_IRIS_0, t_r * 0.6 + fiber * 0.4)
                elif r <= 8.2:
                    # Borde límbico del iris a la esclera
                    col = lerp(C_IRIS_0, C_SCLERA_0, (r - 7.0) / 1.2)
                else:
                    col = C_SCLERA_0

            # Verificación de color frío (B >= 1.8 * R)
            cr, cg, cb = col
            if cb < int(1.8 * cr):
                cr = int(cb / 1.8)
            row.append((cr, cg, cb, 255))
        pixels.append(row)

    for y in range(32):
        for x in range(32):
            r, g, b, a = pixels[y][x]
            assert b >= int(1.8 * r), f"Violation at ({x}, {y}): R={r}, B={b}"

    return pixels

pix = make_texture()
write_png("src/main/resources/assets/void_plague/textures/entity/void_watcher_eye.png", 32, 32, pix)
write_png("src/main/resources/assets/void_plague/textures/block/void_watcher_eye.png", 32, 32, pix)
print("Generated void_watcher_eye.png (both entity & block) with verified cold blue dominance")
