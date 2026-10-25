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

# Palette:
# Base asfáltica: #0A0410 (10, 4, 16)
# Bilis densa: #160824 (22, 8, 36)
# Vetas viscosas: #260C3B (38, 12, 59)
# Brillo sutil bioluminiscente: #421261 (66, 18, 97)

C_BASE = (10, 4, 16)
C_BILE = (22, 8, 36)
C_VEIN = (38, 12, 59)
C_GLOW = (66, 18, 97)

def lerp_color(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return (
        int(c1[0] + (c2[0] - c1[0]) * t),
        int(c1[1] + (c2[1] - c1[1]) * t),
        int(c1[2] + (c2[2] - c1[2]) * t)
    )

def get_viscous_color(val):
    if val < 0.45:
        return lerp_color(C_BASE, C_BILE, val / 0.45)
    elif val < 0.80:
        return lerp_color(C_BILE, C_VEIN, (val - 0.45) / 0.35)
    else:
        return lerp_color(C_VEIN, C_GLOW, (val - 0.80) / 0.20)

def generate_ichor(flow=False):
    pixels = []
    for frame in range(4):
        # 1 pixel shift per frame: continuous microscopic wave
        flow_y = frame * 1.0 if flow else 0.0
        wave_phase = frame * 0.25 # very gentle smooth phase advance
        
        for y in range(16):
            row = []
            fy = (y + flow_y) % 16
            for x in range(16):
                w1 = math.sin((x * 0.35) + (fy * 0.3) + wave_phase) * 0.5 + 0.5
                w2 = math.cos((x * 0.25) - (fy * 0.4) + wave_phase) * 0.5 + 0.5
                w3 = math.sin(((x + fy) * 0.25) + wave_phase * 0.8) * 0.5 + 0.5
                
                val = w1 * 0.50 + w2 * 0.35 + w3 * 0.15
                # Damped subtle dynamic range (no harsh flashes)
                val = 0.15 + val * 0.75
                
                rgb = get_viscous_color(val)
                row.append((*rgb, 255))
            pixels.append(row)
    return pixels

still_pixels = generate_ichor(flow=False)
write_png("src/main/resources/assets/void_plague/textures/block/void_ichor_still.png", 16, 64, still_pixels)

flow_pixels = generate_ichor(flow=True)
write_png("src/main/resources/assets/void_plague/textures/block/void_ichor_flow.png", 16, 64, flow_pixels)

print("Successfully generated void_ichor_still.png and void_ichor_flow.png")
