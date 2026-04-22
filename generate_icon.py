from PIL import Image, ImageDraw

# Create a 512x512 image with the Forest Green background
size = (512, 512)
forest_green = "#2D6A4F"
mint_green = "#D4E2DD"
white = "#FFFFFF"

img = Image.new("RGB", size, forest_green)
draw = ImageDraw.Draw(img)

# 1. Draw House Outline (White)
# Points for house: Top, Right-Top, Right-Bottom, Left-Bottom, Left-Top
house_points = [
    (256, 120),   # Top Peak
    (380, 220),   # Right roof edge
    (380, 340),   # Right bottom
    (132, 340),   # Left bottom
    (132, 220)    # Left roof edge
]
draw.polygon(house_points, fill=white)

# 2. Draw Keyhole Detail (Mint Green)
# Circle (cx=256, cy=215, r=30)
r = 30
draw.ellipse([256-r, 215-r, 256+r, 215+r], fill=mint_green)
# Rect (x=241, y=235, w=30, h=45) -> [x0, y0, x1, y1]
draw.rectangle([241, 235, 271, 280], fill=mint_green)

# 3. Draw Checkmark (Mint Green)
# Path: M330 250 L285 295 L265 275 L255 285 L285 315 L340 260 L330 250Z
checkmark_points = [
    (330, 250),
    (285, 295),
    (265, 275),
    (255, 285),
    (285, 315),
    (340, 260)
]
draw.polygon(checkmark_points, fill=mint_green)

# Save the icon
img.save("play_store_512.png")
print("Successfully generated play_store_512.png")
