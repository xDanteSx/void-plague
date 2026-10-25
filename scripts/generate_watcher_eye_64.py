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

def generate_64x64_texture():
    width, height = 64, 64
    grid = [[(0, 0, 0, 0) for _ in range(width)] for _ in range(height)]

    # Colores con B >= 1.8 * R estrictamente:
    C_CHITIN_BASE = (11, 7, 20, 255)       # #0B0714
    C_CHITIN_EDGE = (28, 14, 56, 255)      # Borde mineral

    C_MUSCLE_0    = (23, 10, 44, 255)      # #1E0B2B
    C_MUSCLE_1    = (40, 18, 78, 255)      # #31144D

    C_EYELID      = (28, 12, 55, 255)      # #240E36
    C_EYELID_EDGE = (36, 15, 68, 255)

    C_SCLERA      = (117, 111, 128, 255)   # #756F80
    C_SCLERA_PALE = (128, 122, 140, 255)   # #807A8C
    C_VEIN        = (26, 15, 52, 255)      # Venas oscuras frías

    C_IRIS_DARK   = (84, 24, 156, 255)     # #6A1B9A
    C_IRIS_LIGHT  = (98, 30, 185, 255)     # #8E24AA
    C_PUPIL       = (0, 0, 0, 255)         # Azabache puro
    C_SPECULAR    = (132, 110, 245, 255)   # #C88DF5

    def fill_rect(x1, y1, w, h, col):
        for y in range(y1, y1 + h):
            for x in range(x1, x1 + w):
                grid[y][x] = col

    # 1. Base (0, 0): width 30, height 15
    for y in range(0, 15):
        for x in range(0, 30):
            var = ((x * 7 + y * 13) % 5) - 2
            r = max(8, min(14, C_CHITIN_BASE[0] + var))
            g = max(5, min(10, C_CHITIN_BASE[1] + var // 2))
            b = max(18, min(24, C_CHITIN_BASE[2] + var))
            grid[y][x] = (r, g, b, 255)

    # 2. Rim Top (0, 15): width 30, height 5
    for y in range(15, 20):
        for x in range(0, 30):
            col = C_MUSCLE_1 if (x + y) % 2 == 0 else C_MUSCLE_0
            grid[y][x] = col

    # 3. Rim Bottom (0, 20): width 30, height 5
    for y in range(20, 25):
        for x in range(0, 30):
            col = C_MUSCLE_1 if (x + y) % 2 == 0 else C_MUSCLE_0
            grid[y][x] = col

    # 4. Rim Left (30, 0): width 10, height 11
    for y in range(0, 11):
        for x in range(30, 40):
            col = C_MUSCLE_1 if (x + y) % 2 == 0 else C_MUSCLE_0
            grid[y][x] = col

    # 5. Rim Right (40, 0): width 10, height 11
    for y in range(0, 11):
        for x in range(40, 50):
            col = C_MUSCLE_1 if (x + y) % 2 == 0 else C_MUSCLE_0
            grid[y][x] = col

    # 6. Eyelid Upper (0, 25): width 20, height 4
    for y in range(25, 29):
        for x in range(0, 20):
            grid[y][x] = C_EYELID_EDGE if (y == 27 or y == 28) else C_EYELID

    # 7. Eyelid Lower (0, 29): width 20, height 4
    for y in range(29, 33):
        for x in range(0, 20):
            grid[y][x] = C_EYELID_EDGE if (y == 29 or y == 30) else C_EYELID

    # 8. Globo Ocular 6x6x6 en (0, 33): width 24, height 12
    # Rellenar todas las caras con esclerótica base
    for y in range(33, 45):
        for x in range(0, 24):
            # Textura esclerótica con suaves variaciones orgánicas
            var = ((x * 11 + y * 17) % 7) - 3
            r = C_SCLERA[0] + var
            g = C_SCLERA[1] + var
            b = C_SCLERA[2] + var + 2
            grid[y][x] = (r, g, b, 255)

    # Microvenas en caras laterales/traseras
    veins = [
        (2, 35), (3, 36), (4, 36),
        (8, 35), (9, 36), (10, 35),
        (14, 35), (15, 36), (16, 36),
        (2, 41), (3, 42), (4, 42),
        (8, 41), (9, 42), (10, 41),
        (13, 41), (14, 42), (15, 41)
    ]
    for vx, vy in veins:
        grid[vy][vx] = C_VEIN

    # CARA FRONTAL (South Face) en [18:24, 39:45]:
    # 6x6 píxeles:
    # lx=0..5, ly=0..5 -> x = 18 + lx, y = 39 + ly
    iris_map = [
        [C_SCLERA,     C_IRIS_DARK,  C_IRIS_LIGHT, C_IRIS_LIGHT, C_SPECULAR,   C_SCLERA],
        [C_IRIS_DARK,  C_IRIS_LIGHT, C_PUPIL,      C_PUPIL,      C_SPECULAR,   C_IRIS_DARK],
        [C_IRIS_LIGHT, C_IRIS_LIGHT, C_PUPIL,      C_PUPIL,      C_IRIS_LIGHT, C_IRIS_LIGHT],
        [C_IRIS_LIGHT, C_IRIS_LIGHT, C_PUPIL,      C_PUPIL,      C_IRIS_LIGHT, C_IRIS_LIGHT],
        [C_IRIS_DARK,  C_IRIS_LIGHT, C_PUPIL,      C_PUPIL,      C_IRIS_LIGHT, C_IRIS_DARK],
        [C_SCLERA,     C_IRIS_DARK,  C_IRIS_LIGHT, C_IRIS_LIGHT, C_IRIS_DARK,  C_SCLERA]
    ]
    for ly in range(6):
        for lx in range(6):
            grid[39 + ly][18 + lx] = iris_map[ly][lx]

    # También pintamos North face [6:12, 39:45] por si el ojo mira en dirección opuesta
    for ly in range(6):
        for lx in range(6):
            grid[39 + ly][6 + lx] = iris_map[ly][lx]

    # Verificación estricta B >= 1.8 * R
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
    print("Textura 64x64 generada con éxito.")

if __name__ == "__main__":
    generate_64x64_texture()
