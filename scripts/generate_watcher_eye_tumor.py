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

def generate_tumor_texture():
    width, height = 64, 64
    grid = [[(0, 0, 0, 0) for _ in range(width)] for _ in range(height)]

    # Colores con B >= 1.8 * R estrictamente:
    # Base y Tumor
    C_CHITIN_0   = (11, 7, 20, 255)       # #0B0714
    C_CHITIN_1   = (16, 7, 30, 255)       # #12071E
    C_CHITIN_2   = (26, 12, 48, 255)      # #1F0D30
    C_VEIN_BASE  = (38, 14, 70, 255)      # Vena abultada en el tumor

    # Zarcillos
    C_ROOT_0     = (20, 10, 36, 255)      # #160B24
    C_ROOT_1     = (34, 16, 62, 255)      # #2A113E

    # Cuenca y Órbita
    C_ORBIT_LIP  = (23, 10, 44, 255)      # #1D0B2B
    C_PUSTULE    = (58, 26, 108, 255)     # #4E1F6B bioluminiscente

    # Párpados
    C_LID_BASE   = (11, 7, 20, 255)       # #0D0714
    C_LID_BORDER = (78, 34, 145, 255)     # #5A2491 lavanda fría

    # Esclera
    C_SCLERA_0   = (107, 101, 120, 255)   # #6B6578 (gris neutro)
    C_SCLERA_1   = (136, 128, 150, 255)   # #888096 (gris neutro pálido)
    C_SCLERA_VEIN= (26, 14, 50, 255)      # #241433

    # Iris & Pupila
    C_IRIS_0     = (75, 26, 138, 255)     # #5A1E8A
    C_IRIS_1     = (112, 42, 212, 255)    # #7D2ED4
    C_PUPIL      = (0, 0, 0, 255)         # Azabache puro
    C_SPECULAR   = (126, 96, 232, 255)    # #A86CE8 estelar

    # 1. Base y Tumor (0, 0): [0..30] x [0..15]
    for y in range(0, 15):
        for x in range(0, 30):
            var = ((x * 7 + y * 13) % 5) - 2
            if (x + y) % 3 == 0:
                col = C_CHITIN_2
            elif (x + y) % 2 == 0:
                col = C_CHITIN_1
            else:
                col = C_CHITIN_0
            r = max(8, min(14, col[0] + var))
            g = max(5, min(10, col[1] + var // 2))
            b = max(18, min(26, col[2] + var))
            grid[y][x] = (r, g, b, 255)

    # Venas abultadas en el tumor
    tumor_veins = [
        (4, 3), (5, 4), (6, 4), (7, 5), (8, 5), (9, 6),
        (18, 3), (19, 4), (20, 5), (21, 5), (22, 6),
        (10, 10), (11, 11), (12, 11), (13, 12), (14, 12)
    ]
    for vx, vy in tumor_veins:
        grid[vy][vx] = C_VEIN_BASE

    # 2. Zarcillos:
    # root_ul: (0, 15), [0..12] x [15..21]
    for y in range(15, 21):
        for x in range(0, 12):
            grid[y][x] = C_ROOT_1 if (x + y) % 2 == 0 else C_ROOT_0
    # root_dl: (12, 15), [12..24] x [15..22]
    for y in range(15, 22):
        for x in range(12, 24):
            grid[y][x] = C_ROOT_1 if (x + y) % 2 == 0 else C_ROOT_0
    # root_dr: (24, 15), [24..36] x [15..20]
    for y in range(15, 20):
        for x in range(24, 36):
            grid[y][x] = C_ROOT_1 if (x + y) % 2 == 0 else C_ROOT_0
    # root_ur: (36, 15), [36..48] x [15..21]
    for y in range(15, 21):
        for x in range(36, 48):
            grid[y][x] = C_ROOT_1 if (x + y) % 2 == 0 else C_ROOT_0

    # 3. Cuenca y Órbita:
    # orbit_top: (0, 22), [0..26] x [22..27]
    for y in range(22, 27):
        for x in range(0, 26):
            grid[y][x] = C_ORBIT_LIP
    # orbit_bottom: (0, 27), [0..26] x [27..32]
    for y in range(27, 32):
        for x in range(0, 26):
            grid[y][x] = C_ORBIT_LIP
    # orbit_left: (0, 32), [0..10] x [32..43]
    for y in range(32, 43):
        for x in range(0, 10):
            grid[y][x] = C_ORBIT_LIP
    # orbit_right: (10, 32), [10..20] x [32..43]
    for y in range(32, 43):
        for x in range(10, 20):
            grid[y][x] = C_ORBIT_LIP

    # Pústulas bioluminiscentes en la órbita
    pustules = [
        (4, 24), (5, 24), (18, 24), (19, 24),
        (8, 29), (9, 29), (22, 29), (23, 29),
        (4, 36), (5, 36), (14, 37), (15, 37)
    ]
    for px, py in pustules:
        grid[py][px] = C_PUSTULE

    # 4. Párpados:
    # eyelid_upper: (26, 22), [26..46] x [22..26]
    for y in range(22, 26):
        for x in range(26, 46):
            grid[y][x] = C_LID_BORDER if (y == 24 or y == 25) else C_LID_BASE
    # eyelid_lower: (26, 26), [26..46] x [26..30]
    for y in range(26, 30):
        for x in range(26, 46):
            grid[y][x] = C_LID_BORDER if (y == 26 or y == 27) else C_LID_BASE

    # 5. Globo Ocular 5x5x5 en (0, 43): [0..20] x [43..53]
    for y in range(43, 53):
        for x in range(0, 20):
            var = ((x * 11 + y * 17) % 7) - 3
            base = C_SCLERA_1 if (x + y) % 2 == 0 else C_SCLERA_0
            r = base[0] + var
            g = base[1] + var
            b = base[2] + var + 2
            grid[y][x] = (r, g, b, 255)

    # Microvenas escleróticas en laterales
    sclera_veins = [
        (1, 49), (2, 50), (3, 50),
        (11, 49), (12, 50), (13, 50),
        (7, 45), (8, 46), (12, 45), (13, 46)
    ]
    for vx, vy in sclera_veins:
        grid[vy][vx] = C_SCLERA_VEIN

    # Cara Frontal (South Face): [15..20] x [48..53] (5x5 px)
    # y North Face: [5..10] x [48..53]
    front_faces = [15, 5]
    for start_x in front_faces:
        iris_5x5 = [
            [C_SCLERA_0,  C_IRIS_0,    C_SPECULAR, C_IRIS_0,    C_SCLERA_0],
            [C_IRIS_0,    C_IRIS_1,    C_PUPIL,    C_IRIS_1,    C_IRIS_0],
            [C_IRIS_1,    C_IRIS_1,    C_PUPIL,    C_IRIS_1,    C_IRIS_1],
            [C_IRIS_0,    C_IRIS_1,    C_PUPIL,    C_IRIS_1,    C_IRIS_0],
            [C_SCLERA_0,  C_IRIS_0,    C_IRIS_1,   C_IRIS_0,    C_SCLERA_0]
        ]
        for ly in range(5):
            for lx in range(5):
                grid[48 + ly][start_x + lx] = iris_5x5[ly][lx]

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
    print("Textura 64x64 Tumor Invasor generada con éxito.")

if __name__ == "__main__":
    generate_tumor_texture()
