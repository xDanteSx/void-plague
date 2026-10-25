import struct
import zlib
import os

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

def generate_atlas_64():
    width, height = 64, 64
    grid = [[(0, 0, 0, 255) for _ in range(width)] for _ in range(height)]

    # Colores con B >= 1.8 * R estrictamente:
    C_TUMOR_0   = (11, 7, 20, 255)       # #0B0714
    C_TUMOR_1   = (19, 11, 36, 255)      # #150B24
    C_TUMOR_VEIN= (32, 15, 62, 255)      # #260F3D

    C_ROOT_0    = (24, 12, 46, 255)      # #180C2E
    C_ROOT_1    = (36, 16, 69, 255)      # #2A1045

    C_LID_BASE  = (23, 11, 43, 255)      # #1E0B2B
    C_LID_RIM   = (48, 21, 90, 255)      # #3D155A

    C_COMMISSURE= (16, 8, 31, 255)       # #13081F

    C_SCLERA_0  = (107, 101, 120, 255)   # #6B6578
    C_SCLERA_1  = (133, 126, 148, 255)   # #857E94
    C_CAPILLARY = (25, 16, 46, 255)      # #20122E

    C_IRIS_DARK = (80, 26, 148, 255)     # #5E1D94
    C_IRIS_LIGHT= (108, 42, 204, 255)    # #7D2EC7
    C_PUPIL     = (0, 0, 0, 255)         # Azabache puro
    C_SPECULAR  = (126, 98, 232, 255)    # #A86CE8

    # =========================================================================
    # 1. (0..24, 0..24): Tumor base de quitina oscura (#0B0714, #150B24) con venas (#260F3D)
    # =========================================================================
    for y in range(0, 24):
        for x in range(0, 24):
            var = ((x * 7 + y * 13) % 5) - 2
            base = C_TUMOR_1 if (x + y) % 3 == 0 else C_TUMOR_0
            r = max(8, min(14, base[0] + var))
            g = max(5, min(10, base[1] + var // 2))
            b = max(18, min(25, base[2] + var))
            grid[y][x] = (r, g, b, 255)

    # Venas abultadas en el tumor
    tumor_veins = [
        (4, 5), (5, 6), (6, 6), (7, 7), (8, 7), (9, 8),
        (14, 4), (15, 5), (16, 5), (17, 6), (18, 7),
        (5, 15), (6, 16), (7, 16), (8, 17), (9, 18),
        (13, 14), (14, 15), (15, 16), (16, 16), (17, 17)
    ]
    for vx, vy in tumor_veins:
        grid[vy][vx] = C_TUMOR_VEIN

    # =========================================================================
    # 2. (24..32, 0..16): Textura de zarcillos fibrosos y raíces (#180C2E, #2A1045)
    # =========================================================================
    for y in range(0, 16):
        for x in range(24, 32):
            col = C_ROOT_1 if (x * 2 + y * 3) % 4 == 0 else C_ROOT_0
            grid[y][x] = col

    # =========================================================================
    # 3. (0..24, 24..35): Párpados carnosos (#1E0B2B) con aristas biseladas (#3D155A)
    # =========================================================================
    for y in range(24, 36):
        for x in range(0, 24):
            is_edge = (y == 24 or y == 35 or x == 0 or x == 23)
            grid[y][x] = C_LID_RIM if is_edge else C_LID_BASE

    # =========================================================================
    # 4. (24..32, 16..29): Comisuras laterales de soporte (#13081F)
    # =========================================================================
    for y in range(16, 30):
        for x in range(24, 32):
            var = ((x * 5 + y * 11) % 5) - 2
            r = max(10, min(18, C_COMMISSURE[0] + var))
            g = max(5, min(10, C_COMMISSURE[1] + var // 2))
            b = max(25, min(35, C_COMMISSURE[2] + var))
            grid[y][x] = (r, g, b, 255)

    # =========================================================================
    # 5. (32..64, 0..32): Cara frontal del globo ocular (+Z)
    # Fondo esclera #6B6578 / #857E94, iris amatista #5E1D94 / #7D2EC7,
    # pupila azabache #000000, especular #A86CE8
    # =========================================================================
    center_x = 47.5
    center_y = 15.5
    for y in range(0, 32):
        for x in range(32, 64):
            dx = x - center_x
            dy = y - center_y
            dist = (dx * dx + dy * dy) ** 0.5

            if dist > 14.5:
                # Esclera de fondo
                grid[y][x] = C_SCLERA_0 if (x + y) % 2 == 0 else C_SCLERA_1
            elif dist > 12.0:
                # Borde corneal / limbo abisal
                grid[y][x] = (40, 14, 80, 255)
            elif dist > 4.5:
                # Anillo de iris
                if dist > 8.5:
                    grid[y][x] = C_IRIS_DARK
                else:
                    grid[y][x] = C_IRIS_LIGHT
            else:
                grid[y][x] = (55, 18, 115, 255)

    # Pupila rasgada vertical azabache puro
    # En x = 47 y 48, desde y = 5 hasta y = 26
    for py in range(5, 27):
        grid[py][47] = C_PUPIL
        grid[py][48] = C_PUPIL
    # Puntas de la hendidura estrechas
    grid[4][47] = C_PUPIL
    grid[4][48] = C_PUPIL
    grid[27][47] = C_PUPIL
    grid[27][48] = C_PUPIL

    # Microreflejo especular estelar frío (#A86CE8)
    grid[9][52] = C_SPECULAR
    grid[10][52] = C_SPECULAR
    grid[9][53] = (95, 45, 180, 255) # Halo especular
    grid[10][53] = (95, 45, 180, 255)

    # =========================================================================
    # 6. (0..64, 32..64): Caras laterales, superior, inferior y trasera del ojo
    # Esclera con microcapilares violeta muy oscuro #20122E, sin rayas diagonales
    # =========================================================================
    for y in range(32, 64):
        for x in range(64):
            var = ((x * 11 + y * 17) % 7) - 3
            base = C_SCLERA_1 if (x + y) % 2 == 0 else C_SCLERA_0
            r = base[0] + var
            g = base[1] + var
            b = base[2] + var + 2
            grid[y][x] = (r, g, b, 255)

    # Microcapilares finos ramificados
    capillaries = [
        (4, 38), (5, 39), (6, 39), (7, 40), (8, 40), (9, 41),
        (20, 36), (21, 37), (22, 37), (23, 38), (24, 38),
        (38, 42), (39, 43), (40, 43), (41, 44), (42, 44),
        (52, 38), (53, 39), (54, 40), (55, 40), (56, 41),
        (8, 52), (9, 53), (10, 53), (11, 54),
        (26, 50), (27, 51), (28, 51), (29, 52),
        (44, 52), (45, 53), (46, 54), (47, 54),
        (56, 51), (57, 52), (58, 52), (59, 53)
    ]
    for cx, cy in capillaries:
        grid[cy][cx] = C_CAPILLARY

    # Verificación estricta B >= 1.8 * R para todos los píxeles
    for y in range(height):
        for x in range(width):
            r, g, b, a = grid[y][x]
            if a > 0:
                is_grey = abs(r - g) <= 12 and abs(r - b) <= 20
                if not is_grey and (r > 0 or g > 0 or b > 0):
                    if b < int(r * 1.8):
                        safe_r = min(r, int(b / 1.85))
                        grid[y][x] = (safe_r, g, b, a)

    out_entity = r"src/main/resources/assets/void_plague/textures/entity/void_watcher_eye.png"
    out_block = r"src/main/resources/assets/void_plague/textures/block/void_watcher_eye.png"

    write_png(out_entity, width, height, grid)
    write_png(out_block, width, height, grid)
    print("Atlas 64x64 de textura generado exitosamente.")

if __name__ == "__main__":
    generate_atlas_64()
