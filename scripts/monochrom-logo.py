from PIL import Image


def text_to_monochrome_png(text_data, output_file="output.png", scale_factor=20):
    """
    Converts a string representation of a grid into a monochrome PNG.
    X = Black
    _ = White
    """
    # 1. Parse the string into lines and determine dimensions
    lines = text_data.strip().split('\n')

    # Calculate dimensions
    height = len(lines)
    width = max(len(line) for line in lines) if height > 0 else 0

    if width == 0 or height == 0:
        print("Error: Input string is empty.")
        return

    # 2. Create a new white image (Mode 'RGB')
    # We default to white background, so we only need to draw the black pixels
    img = Image.new('RGB', (width, height), color='white')
    pixels = img.load()

    # 3. Map characters to pixels
    for y, line in enumerate(lines):
        for x, char in enumerate(line):
            # Safety check to ensure we don't go out of bounds if rows are uneven
            if x < width:
                if char == '_':
                    pixels[x, y] = (0, 0, 0)  # Black
                elif char == 'X':
                    pixels[x, y] = (255, 255, 255)  # White

    # 4. Scale up the image so it is visible
    # We use Image.NEAREST to keep the sharp "pixelated" edges
    if scale_factor > 1:
        new_size = (width * scale_factor, height * scale_factor)
        img = img.resize(new_size, resample=Image.Resampling.NEAREST)

    # 5. Save the image
    img.save(output_file)
    print(f"Successfully saved {output_file} ({width}x{height} raw pixels, scaled to {img.width}x{img.height})")


# --- Usage Example ---
input_string = """
_________XX___________________________________
________XX____________________________________
_______XX_____________________________________
______XX______________________________________
_____XX_______________________________________
____XX________________________________________
___XX_________________________________________
__XX__________________________________________
_XX___________________________________________
XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
X____________________________________________X
X____________________________________________X
X__XXXXXXXXXXXXXXX_____XXXXXXXXXXXXXXXXXXX___X
X__X_X_X_X_X_X_X_X_____X_________________X___X
X__XXXXXXXXXXXXXXX_____X__I_I_I_I_I_I_I__X___X
X__X_X_X_X_X_X_X_X_____X_________________X___X
X__XXXXXXXXXXXXXXX_____XXXXXXXXXXXXXXXXXXX___X
X__X_X_X_X_X_X_X_X___________________________X
X__XXXXXXXXXXXXXXX________XXX_______XXX______X
X__X_X_X_X_X_X_X_X_______XXXXX_____XXXXX_____X
X__XXXXXXXXXXXXXXX_______XXXXX_____XXXXX_____X
X__X_X_X_X_X_X_X_X_______XXXXX_____XXXXX_____X
X__XXXXXXXXXXXXXXX________XXX_______XXX______X
X__X_X_X_X_X_X_X_X___________________________X
X__XXXXXXXXXXXXXXX_____XXXXXXXXXXXXXXXXXXX___X
X__X_X_X_X_X_X_X_X_____X_X_X_X_X_X_X_X_X_X___X
X__XXXXXXXXXXXXXXX_____XXXXXXXXXXXXXXXXXXX___X
X____________________________________________X
X____________________________________________X
XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
______XXXX__________________________XXXX______
______XXXX__________________________XXXX______
"""

text_to_monochrome_png(input_string, "src/main/resources/static/icons/radio-logo-monochrome-small.png", scale_factor=1)
