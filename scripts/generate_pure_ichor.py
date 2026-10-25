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

# Paleta cromática cerrada:
# Fondo abisal: #12071E (18, 7, 30)
# Bilis densa: #1E0B30 (30, 11, 48)
# Corrientes violetas: #2E1048 (46, 16, 72)
# Vetas de plasma luminiscente: #4A1B6D (74, 27, 109)
# Puntos de brillo bioluminiscente tenue: #6A2B96 (106, 43, 150)

C0 = (18, 7, 30)
C1 = (30, 11, 48)
C2 = (46, 16, 72)
C3 = (74, 27, 109)
C4 = (106, 43, 150)

def lerp_rgb(ca, cb, t):
    t = max(0.0, min(1.0, t))
    return (
        int(ca[0] + (cb[0] - ca[0]) * t),
        int(ca[1] + (cb[1] - ca[1]) * t),
        int(ca[2] + (cb[2] - ca[2]) * t)
    )

def evaluate_palette(v):
    v = max(0.0, min(1.0, v))
    if v < 0.35:
        return lerp_rgb(C0, C1, v / 0.35)
    elif v < 0.65:
        return lerp_rgb(C1, C2, (v - 0.35) / 0.30)
    elif v < 0.88:
        return lerp_rgb(C2, C3, (v - 0.65) / 0.23)
    else:
        return lerp_rgb(C3, C4, (v - 0.88) / 0.12)

def generate_ichor(is_flow=False):
    target_mean = 0.50
    all_frame_vals = []
    
    kx = 2.0 * math.pi / 16.0
    ky = 2.0 * math.pi / 16.0

    for frame in range(4):
        theta = frame * (2.0 * math.pi / 4.0)
        flow_y_offset = (frame * 1.5) if is_flow else 0.0
        
        frame_grid = []
        frame_sum = 0.0
        
        for y in range(16):
            row = []
            wy = (y + flow_y_offset) % 16.0
            for x in range(16):
                # Traveling waves with cyclic spatial & temporal boundary conditions
                # Pure traveling wave in 2D maintains exact spatial integral over periodic torus
                w1 = math.sin(kx * x + ky * wy - theta) * 0.5 + 0.5
                w2 = math.cos(2.0 * kx * x - ky * wy + theta) * 0.5 + 0.5
                w3 = math.sin(kx * x - 2.0 * ky * wy + 2.0 * theta) * 0.5 + 0.5
                
                v = w1 * 0.50 + w2 * 0.30 + w3 * 0.20
                frame_sum += v
                row.append(v)
            frame_grid.append(row)
            
        cur_mean = frame_sum / (16 * 16)
        all_frame_vals.append((frame_grid, cur_mean))

    pixels = []
    frame_luminances = []
    
    for frame, (grid, mean_v) in enumerate(all_frame_vals):
        frame_rows = []
        lum_sum = 0.0
        
        for y in range(16):
            row = []
            for x in range(16):
                # Normalize so mean is identical across all frames
                norm_v = grid[y][x] + (target_mean - mean_v)
                val = 0.08 + norm_v * 0.84
                
                rgb = evaluate_palette(val)
                lum = 0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]
                lum_sum += lum
                row.append((*rgb, 255))
            frame_rows.append(row)
            
        frame_luminances.append(round(lum_sum / (16 * 16), 3))
        pixels.extend(frame_rows)
        
    print(f"{'Flow' if is_flow else 'Still'} frame average luminances: {frame_luminances}")
    return pixels

still_data = generate_ichor(is_flow=False)
write_png("src/main/resources/assets/void_plague/textures/block/void_ichor_still.png", 16, 64, still_data)

flow_data = generate_ichor(is_flow=True)
write_png("src/main/resources/assets/void_plague/textures/block/void_ichor_flow.png", 16, 64, flow_data)

print("Generated perfectly normalized void_ichor_still.png and void_ichor_flow.png")
