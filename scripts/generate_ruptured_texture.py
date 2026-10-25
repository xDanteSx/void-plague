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

# Paleta exacta especificada por el usuario:
# CARA EXTERIOR (Quitina fría y sana):
C_BASE_0    = (11, 7, 20)    # #0B0714
C_BASE_1    = (24, 12, 48)   # #180C30
C_BEVEL     = (49, 20, 99)   # #311463
C_VEIN      = (82, 35, 163)  # #5223A3

# BORDES DE LA ROTURA (Marfil necrótico):
C_TOOTH_0   = (69, 58, 82)   # #453A52
C_TOOTH_1   = (97, 83, 115)  # #615373

# CAVIDAD INTERIOR HUECA (Interior expuesto y profundo):
C_CAV_DEEP  = (5, 2, 10)     # #05020A
C_CAV_WALL  = (36, 14, 51)   # #240E33
C_CAV_CLOT  = (56, 18, 79)   # #38124F

def lerp(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return (
        int(c1[0] + (c2[0] - c1[0]) * t),
        int(c1[1] + (c2[1] - c1[1]) * t),
        int(c1[2] + (c2[2] - c1[2]) * t)
    )

def make_ruptured_texture():
    pixels = []
    for y in range(32):
        row = []
        for x in range(32):
            if y < 10:
                # 1. ANCLAJE Y PEDÚNCULO (Quitina oscura)
                dist = abs((x % 16) - 7.5) / 8.0
                if dist > 0.65:
                    col = C_BASE_0
                elif dist > 0.35:
                    col = lerp(C_BASE_0, C_BASE_1, (dist - 0.35) / 0.30)
                else:
                    fiber = math.sin(y * 2.0 + x * 2.5) * 0.5 + 0.5
                    col = lerp(C_BASE_1, C_BEVEL, fiber * 0.6)
                alpha = 255

            elif y < 17:
                # 2. CARA EXTERIOR DE LA COPA (Quitina sana con estrías y venas sutiles)
                ny = (y - 10) / 7.0
                nx = (x % 16) / 16.0
                stria = math.sin(nx * 24.0 + ny * 3.0) * 0.5 + 0.5
                vein_node = math.sin(nx * 12.0 - ny * 8.0) * 0.5 + 0.5

                if vein_node > 0.78:
                    col = lerp(C_BEVEL, C_VEIN, (vein_node - 0.78) / 0.22)
                elif stria > 0.55:
                    col = lerp(C_BASE_1, C_BEVEL, (stria - 0.55) / 0.45)
                else:
                    col = lerp(C_BASE_0, C_BASE_1, stria / 0.55)
                alpha = 255

            elif y < 24:
                # 3. BORDES DE LA ROTURA (Dientes dentados de marfil necrótico #453A52 y #615373)
                ny = (y - 17) / 7.0
                nx = (x % 8) / 8.0  # Dientes de 4 a 8 píxeles de repetición
                sawtooth = abs(nx - 0.5) * 2.0  # 1 en bordes, 0 en pico
                lip_grad = ny * 0.6 + sawtooth * 0.4

                if lip_grad > 0.65:
                    # Pico más afilado y descolorido de la fractura ósea
                    col = lerp(C_TOOTH_0, C_TOOTH_1, (lip_grad - 0.65) / 0.35)
                elif lip_grad > 0.30:
                    col = lerp(C_BEVEL, C_TOOTH_0, (lip_grad - 0.30) / 0.35)
                else:
                    col = lerp(C_BASE_1, C_BEVEL, lip_grad / 0.30)
                alpha = 255

            else:
                # 4. CAVIDAD INTERIOR HUECA (Sombra cavernosa #05020A, desecado #240E33 y coágulos #38124F)
                ny = (y - 24) / 8.0  # 0 en boca interior, 1 en fondo abisal
                nx = (x % 16) / 16.0
                clot_noise = math.sin(nx * 14.0 + ny * 9.0) * math.cos(nx * 7.0 - ny * 11.0)

                if ny > 0.65:
                    # Fondo cavernoso profundo: Sombra negra casi total #05020A
                    col = lerp(C_CAV_WALL, C_CAV_DEEP, (ny - 0.65) / 0.35)
                elif clot_noise > 0.25:
                    # Grumos de médula coagulada #38124F
                    col = lerp(C_CAV_WALL, C_CAV_CLOT, (clot_noise - 0.25) / 0.75)
                else:
                    # Paredes de tejido muscular desecado en amatista apagada #240E33
                    col = lerp(C_CAV_DEEP, C_CAV_WALL, 0.4 + ny * 0.4)
                alpha = 255

            row.append((col[0], col[1], col[2], alpha))
        pixels.append(row)
    return pixels

pix = make_ruptured_texture()
write_png("src/main/resources/assets/void_plague/textures/entity/ruptured_incubation_sac.png", 32, 32, pix)
write_png("src/main/resources/assets/void_plague/textures/block/ruptured_incubation_sac.png", 32, 32, pix)
print("Updated ruptured_incubation_sac.png with sharp chromatic contrast and volumetric depth")
