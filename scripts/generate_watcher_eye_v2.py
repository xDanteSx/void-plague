import struct
import zlib
import os

def write_png(filename, width, height, pixels):
    raw = bytearray()
    for row in pixels:
        raw.append(0)  # filter 0 (None)
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

def generate_texture():
    width, height = 32, 32
    # Grid de 32x32 píxeles RGBA
    grid = [[(0, 0, 0, 255) for _ in range(width)] for _ in range(height)]

    # =========================================================================
    # 1. CUENCA DE QUITINA: Top-Left [0:16, 0:16]
    # Base #0B0714 con bordes biselados #261138 y #1B0C29
    # =========================================================================
    for y in range(16):
        for x in range(16):
            dist_edge = min(x, y, 15 - x, 15 - y)
            if dist_edge == 0:
                grid[y][x] = (30, 15, 58, 255) # Borde mineral #261138
            elif dist_edge == 1:
                grid[y][x] = (22, 11, 42, 255) # Bisel intermedio #1B0C29
            else:
                var = ((x * 7 + y * 13) % 5) - 2
                r = max(8, min(13, 11 + var))
                g = max(5, min(9, 7 + var // 2))
                b = max(18, min(24, 20 + var))
                grid[y][x] = (r, g, b, 255)

    # =========================================================================
    # 2. PÁRPADOS DE QUITINA: Top-Right [16:32, 0:16]
    # Quitina necrótica fría #0B0714 y #1E0F2B con bordes #261138
    # =========================================================================
    for y in range(16):
        for x in range(16, 32):
            local_x = x - 16
            local_y = y
            if local_y == 0 or local_y == 15 or local_x == 0 or local_x == 15:
                grid[y][x] = (32, 15, 60, 255) # Borde mineral
            elif local_y in (1, 14):
                grid[y][x] = (23, 13, 44, 255) # Bisel de párpado #1E0F2B
            else:
                ridge = (local_y % 3 == 0)
                if ridge:
                    grid[y][x] = (23, 13, 44, 255) # Placa quitinosa
                else:
                    grid[y][x] = (11, 7, 20, 255)  # Fondo oscuro #0B0714

    # =========================================================================
    # 3. ESCLERÓTICA LATERAL Y TRASERA: Bottom-Left [0:16, 16:32]
    # Base homogénea gris pizarra pálido (#756F80, #8A8296)
    # CERO rayas diagonales, CERO patrones de cebra.
    # Microcapilares quebrados violeta muy oscuro (#241433)
    # =========================================================================
    for y in range(16):
        for x in range(16):
            py = y + 16
            px = x
            # Base gris pizarra pálido
            var = ((x * 11 + y * 17) % 7) - 3
            if (x + y) % 2 == 0:
                base_r, base_g, base_b = 117, 111, 128 # #756F80
            else:
                base_r, base_g, base_b = 126, 120, 138 # tono medio
            r = base_r + var
            g = base_g + var
            b = base_b + var + 2
            grid[py][px] = (r, g, b, 255)

    # Microcapilares quebrados ramificados violeta oscuro (#241433)
    capillaries = [
        (2, 4), (3, 4), (4, 5), (5, 5), (6, 6), (7, 6), (8, 7),
        (5, 5), (6, 4), (7, 4),
        (1, 11), (2, 11), (3, 10), (4, 10), (5, 9), (6, 9), (7, 10), (8, 10),
        (12, 3), (11, 4), (10, 5), (9, 5), (8, 6),
        (13, 12), (12, 11), (11, 11), (10, 10), (9, 10)
    ]
    for lx, ly in capillaries:
        if 0 <= lx < 16 and 0 <= ly < 16:
            grid[ly + 16][lx] = (26, 15, 52, 255) # #241433 violeta oscuro

    # =========================================================================
    # 4. IRIS Y PUPILA FRONTAL: Bottom-Right [16:32, 16:32]
    # Anillo amatista bioluminiscente (#5E1D94, #7D2EC7), pupila hendida (#000000)
    # y especular frío (#A364E8)
    # =========================================================================
    for local_y in range(16):
        for local_x in range(16):
            px = local_x + 16
            py = local_y + 16

            dx = local_x - 7.5
            dy = local_y - 7.5
            dist = (dx * dx + dy * dy) ** 0.5

            if dist > 6.8:
                # Esclerótica circundante
                grid[py][px] = (117, 111, 128, 255)
            elif dist > 5.5:
                # Anillo exterior / limbo abisal oscuro
                grid[py][px] = (40, 14, 80, 255)
            elif dist > 1.8:
                # Anillo de iris amatista
                if dist > 3.8:
                    grid[py][px] = (80, 24, 156, 255) # #5E1D94 amatista profunda
                else:
                    grid[py][px] = (108, 36, 208, 255) # #7D2EC7 amatista luminosa
            else:
                grid[py][px] = (55, 18, 115, 255)

    # Pupila vertical hendida azabache (#000000)
    slit_pupil = [
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
    for lx, ly in slit_pupil:
        grid[ly + 16][lx + 16] = (0, 0, 0, 255)

    # Brillo especular frío (#A364E8) en la esquina superior derecha del iris
    grid[5 + 16][10 + 16] = (125, 88, 238, 255) # #A364E8 especular brillante
    grid[5 + 16][11 + 16] = (90, 42, 175, 255)  # Halo especular
    grid[4 + 16][10 + 16] = (90, 42, 175, 255)  # Halo especular

    # Verificación estricta B >= 1.8 * R en todos los tonos de color
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
    print("Texturas PNG 32x32 generadas exitosamente con paleta abisal estricta.")

if __name__ == "__main__":
    generate_texture()
