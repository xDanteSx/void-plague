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

def generate_clean_32x32():
    width, height = 32, 32
    grid = [[(0, 0, 0, 255) for _ in range(width)] for _ in range(height)]

    # Colores con B >= 1.8 * R estrictamente:
    C_CHITIN_BASE = (11, 7, 20, 255)       # #0B0714
    C_CHITIN_EDGE = (30, 15, 58, 255)      # #261138 (borde biselado)
    C_CHITIN_MID  = (22, 11, 42, 255)      # Bisel intermedio

    C_SCLERA      = (117, 111, 128, 255)   # #756F80 (gris necrótico neutro)
    C_SCLERA_PALE = (128, 122, 140, 255)   # #807A8C
    C_VEIN        = (26, 14, 50, 255)      # Microvena violeta oscura

    C_IRIS_DARK   = (80, 24, 156, 255)     # #5E1D94
    C_IRIS_LIGHT  = (108, 36, 208, 255)    # #7D2EC7
    C_PUPIL       = (0, 0, 0, 255)         # Azabache puro
    C_SPECULAR    = (125, 88, 238, 255)    # #A364E8 especular frío

    # =========================================================================
    # 1. Cuadrante Superior Izquierdo (0..16, 0..16): Zócalo
    # Cuenca de quitina fría #0B0714 con borde biselado #261138
    # =========================================================================
    for y in range(16):
        for x in range(16):
            dist_edge = min(x, y, 15 - x, 15 - y)
            if dist_edge == 0:
                grid[y][x] = C_CHITIN_EDGE
            elif dist_edge == 1:
                grid[y][x] = C_CHITIN_MID
            else:
                var = ((x * 7 + y * 13) % 5) - 2
                r = max(8, min(14, C_CHITIN_BASE[0] + var))
                g = max(5, min(10, C_CHITIN_BASE[1] + var // 2))
                b = max(18, min(24, C_CHITIN_BASE[2] + var))
                grid[y][x] = (r, g, b, 255)

    # =========================================================================
    # 2. Cuadrante Superior Derecho (16..32, 0..16): Cara Frontal del Ojo
    # Fondo esclera #756F80, iris amatista #5E1D94 / #7D2EC7, pupila vertical #000000, especular #A364E8
    # =========================================================================
    for ly in range(16):
        for lx in range(16):
            gx = 16 + lx
            gy = ly

            dx = lx - 7.5
            dy = ly - 7.5
            dist = (dx * dx + dy * dy) ** 0.5

            if dist > 6.8:
                grid[gy][gx] = C_SCLERA
            elif dist > 5.5:
                grid[gy][gx] = (40, 14, 80, 255) # Anillo perimetral oscuro
            elif dist > 2.0:
                if dist > 4.0:
                    grid[gy][gx] = C_IRIS_DARK
                else:
                    grid[gy][gx] = C_IRIS_LIGHT
            else:
                grid[gy][gx] = (55, 18, 115, 255)

    # Pupila hendidura vertical azabache puro
    pupil_pixels = [
        (7, 2), (8, 2),
        (7, 3), (8, 3),
        (7, 4), (8, 4),
        (7, 5), (8, 5),
        (7, 6), (8, 6),
        (7, 7), (8, 7),
        (7, 8), (8, 8),
        (7, 9), (8, 9),
        (7, 10), (8, 10),
        (7, 11), (8, 11),
        (7, 12), (8, 12),
        (7, 13), (8, 13)
    ]
    for px, py in pupil_pixels:
        grid[py][16 + px] = C_PUPIL

    # Brillo especular frío en esquina superior derecha del iris
    grid[5][16 + 10] = C_SPECULAR
    grid[5][16 + 11] = (90, 42, 175, 255)
    grid[4][16 + 10] = (90, 42, 175, 255)

    # =========================================================================
    # 3. Mitad Inferior (0..32, 16..32): Caras Laterales del Ojo
    # Esclera pálida limpia sin rayas diagonales
    # =========================================================================
    for y in range(16, 32):
        for x in range(32):
            var = ((x * 11 + y * 17) % 7) - 3
            if (x + y) % 2 == 0:
                base_r, base_g, base_b = C_SCLERA[0], C_SCLERA[1], C_SCLERA[2]
            else:
                base_r, base_g, base_b = C_SCLERA_PALE[0], C_SCLERA_PALE[1], C_SCLERA_PALE[2]
            r = base_r + var
            g = base_g + var
            b = base_b + var + 2
            grid[y][x] = (r, g, b, 255)

    # Sutiles microvenas que nacen desde atrás
    side_veins = [
        (4, 20), (5, 21), (6, 21),
        (12, 19), (13, 20), (14, 20),
        (22, 22), (23, 23), (24, 23),
        (8, 27), (9, 28), (10, 27),
        (18, 26), (19, 27), (20, 27)
    ]
    for vx, vy in side_veins:
        grid[vy][vx] = C_VEIN

    # Verificación estricta B >= 1.8 * R
    for y in range(height):
        for x in range(width):
            r, g, b, a = grid[y][x]
            is_grey = abs(r - g) <= 12 and abs(r - b) <= 20
            if not is_grey and (r > 0 or g > 0 or b > 0):
                if b < int(r * 1.8):
                    safe_r = min(r, int(b / 1.85))
                    grid[y][x] = (safe_r, g, b, a)

    out_entity = r"src/main/resources/assets/void_plague/textures/entity/void_watcher_eye.png"
    out_block = r"src/main/resources/assets/void_plague/textures/block/void_watcher_eye.png"

    write_png(out_entity, width, height, grid)
    write_png(out_block, width, height, grid)
    print("Textura 32x32 limpia generada con éxito.")

if __name__ == "__main__":
    generate_clean_32x32()
