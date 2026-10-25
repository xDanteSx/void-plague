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

def hex_to_rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))

def gen_void_rift_tear():
    w, h = 64, 64
    grid = [[(0, 0, 0, 0) for _ in range(w)] for _ in range(h)]

    c_white   = hex_to_rgb("#FFFFFF")
    c_bright  = hex_to_rgb("#FF80FF")
    c_magenta = hex_to_rgb("#FF33EE")
    c_fuchsia = hex_to_rgb("#D134EB")
    c_violet  = hex_to_rgb("#8A1CBA")
    c_deep    = hex_to_rgb("#45086B")

    # Generate a vertical jagged tear from y=3 to y=60
    for y in range(h):
        if y < 3 or y > 60:
            continue

        # Normalized vertical position
        ny = (y - 3) / 57.0  # 0.0 to 1.0
        # Elliptical / sinuous envelope: needle tips at top & bottom, widest in center
        envelope = math.sin(ny * math.pi) ** 0.65
        max_half_width = envelope * 12.0  # up to 24px wide in center

        # Jagged centerline wandering
        jag1 = math.sin(y * 0.45) * 1.8
        jag2 = math.cos(y * 0.90) * 1.2
        jag3 = math.sin(y * 1.7) * 0.6
        cx = 31.5 + jag1 + jag2 + jag3

        for x in range(w):
            dist = abs(x - cx)

            # High-frequency lightning noise along the edges
            micro_edge = (math.sin(x * 1.5 + y * 2.3) * 0.5 + 0.5) * 1.5
            dist_eff = dist + micro_edge

            if dist_eff <= max_half_width:
                rel = dist_eff / max_half_width

                if rel < 0.16:
                    # Blinding white core
                    col = c_white
                    alpha = 255
                elif rel < 0.35:
                    # Bright electric magenta transition
                    t = (rel - 0.16) / 0.19
                    col = (
                        int(c_white[0] + (c_bright[0] - c_white[0]) * t),
                        int(c_white[1] + (c_bright[1] - c_white[1]) * t),
                        int(c_white[2] + (c_bright[2] - c_white[2]) * t)
                    )
                    alpha = 255
                elif rel < 0.60:
                    # Electric fuchsia
                    t = (rel - 0.35) / 0.25
                    col = (
                        int(c_bright[0] + (c_fuchsia[0] - c_bright[0]) * t),
                        int(c_bright[1] + (c_fuchsia[1] - c_bright[1]) * t),
                        int(c_bright[2] + (c_fuchsia[2] - c_bright[2]) * t)
                    )
                    alpha = 245
                elif rel < 0.82:
                    # Deep violet corona
                    t = (rel - 0.60) / 0.22
                    col = (
                        int(c_fuchsia[0] + (c_violet[0] - c_fuchsia[0]) * t),
                        int(c_fuchsia[1] + (c_violet[1] - c_fuchsia[1]) * t),
                        int(c_fuchsia[2] + (c_violet[2] - c_fuchsia[2]) * t)
                    )
                    alpha = int(240 - t * 80)
                else:
                    # Fading outer spatial distortion halo
                    t = (rel - 0.82) / 0.18
                    col = (
                        int(c_violet[0] + (c_deep[0] - c_violet[0]) * t),
                        int(c_violet[1] + (c_deep[1] - c_violet[1]) * t),
                        int(c_violet[2] + (c_deep[2] - c_violet[2]) * t)
                    )
                    alpha = int(160 * (1.0 - t))

                # Occasional horizontal energy spark discharge extending outward
                if dist_eff > max_half_width * 0.7:
                    if (y % 7 == 0 or y % 11 == 0) and abs(x - cx) < max_half_width + 3.0:
                        col = c_fuchsia
                        alpha = max(alpha, 180)

                grid[y][x] = (*col, max(0, min(255, alpha)))

    return grid

if __name__ == '__main__':
    out_path = r"src/main/resources/assets/void_plague/textures/entity/void_rift_tear.png"
    write_png(out_path, 64, 64, gen_void_rift_tear())
    print("Generated 64x64 void_rift_tear.png successfully at", out_path)
