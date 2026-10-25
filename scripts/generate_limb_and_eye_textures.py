import struct
import zlib
import os

def write_png(filename, width, height, pixels):
    raw = bytearray()
    for row in pixels:
        raw.append(0)
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

# 1. GRASPING LIMB TEXTURE (16x16)
# Completely organic: calcified bone (#5A5463), deep purple tendons (#3B144A), torn flesh (#2E143D)
# Zero stripes.
def make_grasping_limb():
    palette = {
        'B': hex_to_rgb('#5A5463'), # Calcified bone
        'b': hex_to_rgb('#47414F'), # Dark bone edge
        'L': hex_to_rgb('#736B7D'), # Light bone highlight
        'T': hex_to_rgb('#3B144A'), # Dark purple tendon
        't': hex_to_rgb('#260C3B'), # Deep tendon shadow
        'F': hex_to_rgb('#2E143D'), # Torn flesh
        'f': hex_to_rgb('#160824'), # Abyssal flesh
        'C': hex_to_rgb('#12131A'), # Sharp chitin claw
        '.': (0, 0, 0, 0)
    }
    
    # 16x16 icon/texture
    art = [
        "....CCC..CCC....",
        "...CLLBC.CBLC...",
        "...CBBTC.CTBBC..",
        "....CBTC.CTBC...",
        "....CBTC.CTBC...",
        ".....CBT.TBC....",
        ".....CBBTBB.....",
        ".....LBBTBBL....",
        ".....BBTTTBB....",
        "....LBBTTTBBL...",
        "....BBTTTTTBB...",
        "....BBTTTTTBB...",
        "...LBBTTTTTBBL..",
        "...FbbTTTTTbbF..",
        "..FFFFfFFFfFFFF.",
        ".ffffffffffffff."
    ]
    
    pixels = []
    for row in art:
        r_pix = []
        for ch in row:
            col = palette[ch]
            if len(col) == 4:
                r_pix.append(col)
            else:
                r_pix.append((*col, 255))
        pixels.append(r_pix)
    return pixels

# 2. VOID WATCHER EYE TEXTURE (16x16)
# Chitin socket (#12131A), muscle folds (#2E143D), sclera (#857B8A), iris (#6A1B9A), pupil (#000000)
def make_watcher_eye():
    palette = {
        'C': hex_to_rgb('#12131A'), # Chitin socket
        'c': hex_to_rgb('#1E1F2A'), # Chitin highlight
        'M': hex_to_rgb('#2E143D'), # Muscle folds
        'm': hex_to_rgb('#421857'), # Muscle highlight
        'S': hex_to_rgb('#857B8A'), # Sclera
        's': hex_to_rgb('#A197A6'), # Sclera highlight
        'v': hex_to_rgb('#695C6E'), # Sclera vein
        'I': hex_to_rgb('#6A1B9A'), # Amethyst iris
        'i': hex_to_rgb('#8E24AA'), # Bright iris
        'P': hex_to_rgb('#000000'), # Jet black pupil
        '.': (0, 0, 0, 0)
    }
    
    art = [
        "....CCCCCC......",
        "..CCcmMMCmmCC...",
        ".CcMMMMMMMMmcC..",
        ".CMMSSSSSSSSMMC.",
        "CcmSSvSSvSSSSmcC",
        "CMMSSIIiiIISSMMC",
        "CMMSSIIPPIISSMMC",
        "CMMSSIIPPIISSMMC",
        "CMMSSIIPPIISSMMC",
        "CMMSSIIPPIISSMMC",
        "CMMSSIIiiIISSMMC",
        "CcmSSvSSSSvSSmcC",
        ".CMMSSSSSSSSMMC.",
        ".CcMMMMMMMMmcC..",
        "..CCcmMMCmmCC...",
        "....CCCCCC......"
    ]
    
    pixels = []
    for row in art:
        r_pix = []
        for ch in row:
            col = palette[ch]
            if len(col) == 4:
                r_pix.append(col)
            else:
                r_pix.append((*col, 255))
        pixels.append(r_pix)
    return pixels

write_png("src/main/resources/assets/void_plague/textures/block/grasping_limb.png", 16, 16, make_grasping_limb())
write_png("src/main/resources/assets/void_plague/textures/block/void_watcher_eye.png", 16, 16, make_watcher_eye())
print("Generated grasping_limb.png and void_watcher_eye.png")
