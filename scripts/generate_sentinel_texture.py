import struct
import zlib
import os
import math

def write_png(filename, width, height, pixels):
    raw = bytearray()
    for row in pixels:
        raw.append(0)  # filter type 0 (None)
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
    print(f"Written: {filename}")

def validate_pixel(r, g, b, a, context=""):
    if a > 0 and (r > 0 or g > 0 or b > 0):
        assert b >= 1.8 * r - 0.001, f"Validation failed at {context}: ({r},{g},{b}) has B/R={b/r:.2f} < 1.8"

def generate_textures():
    W, H = 128, 128
    base_tex = [[(0, 0, 0, 0) for _ in range(W)] for _ in range(H)]
    eyes_tex = [[(0, 0, 0, 0) for _ in range(W)] for _ in range(H)]

    # =========================================================================
    # PALETA FRÍA ABISAL (B >= 1.8 * R estrictamente)
    # =========================================================================
    # Casco blindado de quitina
    C_CARAPACE_DEEP   = (10, 5, 18, 255)    # #0A0512 - carbón abisal
    C_CARAPACE_MID    = (18, 9, 36, 255)    # #120924 - textura quitina
    C_CARAPACE_BEVEL  = (28, 14, 54, 255)   # #1C0E36 - biseles
    C_CARAPACE_HIGH   = (38, 16, 76, 255)   # #26104C - aristas y crestas
    C_CARAPACE_SEAM   = (5, 3, 10, 255)     # #05030A - juntas oscuras

    # Cuernos superiores
    C_HORN_BASE       = (14, 7, 28, 255)
    C_HORN_EDGE       = (32, 14, 65, 255)

    # Esclera pizarra necrótica con microvenas
    C_SCLERA_DARK     = (38, 32, 70, 255)
    C_SCLERA_MID      = (65, 58, 120, 255)  # #413A78 - pizarra necrótica

    # Anillo de colmillos / garras en marfil frío dibujados en la textura del ojo
    C_TOOTH_ROOT      = (45, 40, 85, 255)   # base sombreada del diente
    C_TOOTH_BODY      = (75, 70, 140, 255)  # cuerpo marfil
    C_TOOTH_TIP       = (100, 95, 185, 255) # #645FB9 - punta marfil brillante

    # Iris amatista brillante concéntrico
    C_IRIS_OUTER      = (65, 18, 130, 255)  # #411282
    C_IRIS_MID        = (95, 26, 185, 255)  # #5F1AB9
    C_IRIS_BRIGHT     = (125, 34, 235, 255) # #7D22EB
    C_SPECULAR        = (138, 110, 252, 255)# reflejo especular
    C_PUPIL           = (0, 0, 0, 255)      # hendidura vertical azabache pura

    # Tejido lateral y posterior del globo ocular
    C_EYE_TISSUE_BASE = (15, 8, 32, 255)
    C_EYE_TISSUE_VEIN = (25, 12, 54, 255)

    # 8 Tentáculos colosales articulados
    C_TENT_BASE_0     = (18, 8, 38, 255)
    C_TENT_BASE_1     = (25, 11, 52, 255)
    C_TENT_MID_0      = (34, 14, 72, 255)
    C_TENT_MID_1      = (44, 17, 92, 255)
    C_TENT_TIP_0      = (55, 20, 120, 255)
    C_TENT_TIP_1      = (80, 28, 175, 255)  # ápice bioluminiscente

    # =========================================================================
    # 1. CASCO / CORAZA PRINCIPAL (0..64, 0..58)
    # =========================================================================
    # A. Bloque central del cráneo (0..64, 0..26) -> texOffs(0, 0)
    for y in range(0, 26):
        for x in range(0, 64):
            base = C_CARAPACE_MID if (x + y * 2) % 4 == 0 else C_CARAPACE_DEEP
            if (x % 6 == 0) or (y % 5 == 0):
                base = C_CARAPACE_SEAM
            elif (x + y) % 7 == 2:
                base = C_CARAPACE_BEVEL
            base_tex[y][x] = base

    # B. Coronilla biselada superior (0..52, 27..43) -> texOffs(0, 27)
    for y in range(27, 43):
        for x in range(0, 52):
            base = C_CARAPACE_BEVEL if (x + y) % 3 == 0 else C_CARAPACE_MID
            if x in (0, 12, 13, 25, 26, 38, 39, 51) or y in (27, 39, 40, 42):
                base = C_CARAPACE_HIGH
            base_tex[y][x] = base

    # C. Faldón posterior alargado (0..42, 44..58) -> texOffs(0, 44)
    for y in range(44, 58):
        for x in range(0, 42):
            base = C_CARAPACE_MID if (x + y) % 3 == 0 else C_CARAPACE_DEEP
            if y == 57 or x in (0, 5, 6, 20, 21, 26, 27, 41):
                base = C_CARAPACE_SEAM
            elif (x + y) % 5 == 2:
                base = C_CARAPACE_BEVEL
            base_tex[y][x] = base

    # D. Cuernos de quitina superiores traseros (0..10, 59..67) -> texOffs(0, 59)
    for y in range(59, 67):
        for x in range(0, 10):
            base_tex[y][x] = C_HORN_EDGE if (x + y) % 2 == 0 else C_HORN_BASE

    # =========================================================================
    # 2. VISERA, QUIJADA Y MEJILLAS (43..106, 0..51)
    # =========================================================================
    # A. Visera frontal / Brow plate (53..91, 27..34) -> texOffs(53, 27)
    for y in range(27, 34):
        for x in range(53, 91):
            base = C_CARAPACE_BEVEL if y == 30 else C_CARAPACE_DEEP
            if y == 27 or y == 33 or x in (53, 56, 71, 72, 87, 90):
                base = C_CARAPACE_SEAM
            elif (x + y) % 4 == 0:
                base = C_CARAPACE_HIGH
            base_tex[y][x] = base

    # B. Mejilla izquierda / Cheek left (65..85, 0..16) -> texOffs(65, 0)
    for y in range(0, 16):
        for x in range(65, 85):
            base = C_CARAPACE_MID if (x + y) % 3 == 0 else C_CARAPACE_DEEP
            if x in (65, 72, 73, 84) or y in (0, 7, 8, 15):
                base = C_CARAPACE_BEVEL
            base_tex[y][x] = base

    # C. Mejilla derecha / Cheek right (86..106, 0..16) -> texOffs(86, 0)
    for y in range(0, 16):
        for x in range(86, 106):
            base = C_CARAPACE_MID if (x + y) % 3 == 0 else C_CARAPACE_DEEP
            if x in (86, 93, 94, 105) or y in (0, 7, 8, 15):
                base = C_CARAPACE_BEVEL
            base_tex[y][x] = base

    # D. Quijada / Jaw plate (43..79, 44..51) -> texOffs(43, 44)
    for y in range(44, 51):
        for x in range(43, 79):
            base = C_CARAPACE_BEVEL if y == 47 else C_CARAPACE_DEEP
            if y == 44 or y == 50 or x in (43, 46, 60, 61, 75, 78):
                base = C_CARAPACE_SEAM
            elif (x + y) % 3 == 0:
                base = C_CARAPACE_HIGH
            base_tex[y][x] = base

    # =========================================================================
    # 3. GLOBO OCULAR EXPUESTO Y PROMINENTE (72..108, 17..35) -> texOffs(72, 17)
    # =========================================================================
    # Tejido general esclerótico del cubo (caras superior, inferior, laterales y trasera)
    for y in range(17, 35):
        for x in range(72, 108):
            var = (x * 3 + y * 7) % 5
            color = C_EYE_TISSUE_VEIN if var == 1 else C_EYE_TISSUE_BASE
            base_tex[y][x] = color

    # Cara Frontal del ojo (-Z, mirando directamente al frente):
    # Front face at texOffs(72, 17) -> U: [81..89], V: [26..34] (9x9 px)
    cx, cy = 4.0, 4.0
    for ey in range(9):
        for ex in range(9):
            gx = 81 + ex
            gy = 26 + ey
            dist = math.sqrt((ex - cx) ** 2 + (ey - cy) ** 2)

            # 1. Pupila vertical nítida de 2x7 px en negro puro (#000000)
            if (ex in (3, 4)) and (1 <= ey <= 7):
                color = C_PUPIL

            # 2. Reflejo especular blanco/amatista en el cuadrante superior derecho
            elif (ex == 5 and ey == 2) or (ex == 6 and ey == 2):
                color = C_SPECULAR

            # 3. Anillo de colmillos/garras afiladas en marfil frío dibujados en el borde del ojo
            elif (ey == 1 and ex in (2, 5, 6)) or (ey == 7 and ex in (2, 3, 5, 6)):
                color = C_TOOTH_TIP
            elif (ex == 1 and ey in (3, 5)) or (ex == 7 and ey in (3, 5)):
                color = C_TOOTH_TIP
            elif (ey == 0 and ex in (2, 3, 5, 6)) or (ey == 8 and ex in (2, 3, 5, 6)):
                color = C_TOOTH_ROOT
            elif (ex == 0 and ey in (3, 5)) or (ex == 8 and ey in (3, 5)):
                color = C_TOOTH_ROOT

            # 4. Anillos concéntricos del iris amatista brillante
            elif dist <= 2.2:
                color = C_IRIS_BRIGHT
            elif dist <= 3.2:
                color = C_IRIS_MID
            elif dist <= 4.1:
                color = C_IRIS_OUTER

            # 5. Esclera pizarra necrótica en las comisuras y fondo
            else:
                color = C_SCLERA_MID if (ex + ey) % 2 == 0 else C_SCLERA_DARK

            base_tex[gy][gx] = color

            # Capa emisiva (void_watcher_sentinel_eyes.png)
            # Solo el iris y el brillo resplandecen en la oscuridad; pupila, dientes y esclera son transparentes
            if color in (C_IRIS_BRIGHT, C_IRIS_MID, C_IRIS_OUTER, C_SPECULAR):
                eyes_tex[gy][gx] = color
            else:
                eyes_tex[gy][gx] = (0, 0, 0, 0)

    # =========================================================================
    # 4. 8 TENTÁCULOS COLOSALES EN CASCADA
    # =========================================================================
    # Segmento 1 (Base: U: 0..10, V: 94..102) -> texOffs(0, 94)
    for y in range(94, 102):
        for x in range(0, 10):
            color = C_TENT_BASE_1 if (x + y) % 2 == 0 else C_TENT_BASE_0
            if y == 94 or y == 101:
                color = C_CARAPACE_SEAM
            elif (x + y) % 4 == 1:
                color = C_IRIS_OUTER
            base_tex[y][x] = color

    # Segmento 2 (Medio 1: U: 12..20, V: 94..101) -> texOffs(12, 94)
    for y in range(94, 101):
        for x in range(12, 20):
            color = C_TENT_MID_1 if (x + y) % 2 == 0 else C_TENT_MID_0
            if y == 94 or y == 100:
                color = C_TENT_BASE_0
            elif (x + y) % 4 == 0:
                color = C_IRIS_MID
            base_tex[y][x] = color

    # Segmento 3 (Medio 2: U: 20..26, V: 94..101) -> texOffs(20, 94)
    for y in range(94, 101):
        for x in range(20, 26):
            color = C_TENT_MID_1 if (x + y) % 2 == 0 else C_TENT_MID_0
            base_tex[y][x] = color

    # Segmento 4 (Punta: U: 26..30, V: 94..101) -> texOffs(26, 94)
    for y in range(94, 101):
        for x in range(26, 30):
            color = C_TENT_TIP_1 if y >= 98 else C_TENT_TIP_0
            base_tex[y][x] = color
            # Ápices bioluminiscentes en la oscuridad
            if y >= 98:
                eyes_tex[y][x] = C_TENT_TIP_1

    # Validación exhaustiva de paleta fría abisal (B >= 1.8 * R, cero rojo, cero fucsia)
    for y in range(H):
        for x in range(W):
            r, g, b, a = base_tex[y][x]
            validate_pixel(r, g, b, a, f"base_tex[{y}][{x}]")
            er, eg, eb, ea = eyes_tex[y][x]
            validate_pixel(er, eg, eb, ea, f"eyes_tex[{y}][{x}]")

    print("All 128x128 pixels successfully validated: B >= 1.8 * R strictly held on every non-black pixel!")

    target_dir = "src/main/resources/assets/void_plague/textures/entity"
    write_png(f"{target_dir}/void_watcher_sentinel.png", W, H, base_tex)
    write_png(f"{target_dir}/void_watcher_sentinel_eyes.png", W, H, eyes_tex)

if __name__ == "__main__":
    generate_textures()
