from PIL import Image, ImageDraw, ImageFont
import os

# Dimensions: 1024 x 500
size = (1024, 500)
forest_green = "#2D6A4F"
mint_green = "#D4E2DD"
white = "#FFFFFF"

# Create image with Forest Green background
img = Image.new("RGB", size, forest_green)
draw = ImageDraw.Draw(img)

# 1. Add a subtle gradient or pattern (Optional, keeping it clean as per "Calm and Clean")
# Let's add a soft mint green circle in the corner for depth
draw.ellipse([700, -100, 1100, 300], fill="#347A5A") # Slightly lighter forest

# 2. Draw the House Icon (Enlarged) on the right side
# Scale up the previous coordinates
# Peak: (850, 150)
house_points = [
    (850, 150),   # Top Peak
    (940, 220),   # Right roof edge
    (940, 350),   # Right bottom
    (760, 350),   # Left bottom
    (760, 220)    # Left roof edge
]
draw.polygon(house_points, fill=white)

# Keyhole/Window
r = 20
draw.ellipse([850-r, 220-r, 850+r, 220+r], fill=mint_green)
draw.rectangle([840, 235, 860, 275], fill=mint_green)

# Checkmark
checkmark_points = [
    (910, 240),
    (875, 275),
    (860, 260),
    (852, 268),
    (875, 291),
    (918, 248)
]
draw.polygon(checkmark_points, fill=mint_green)

# 3. Add Text "RentLog"
# Try to find a system font
font_path = "C:/Windows/Fonts/segoeuib.ttf" # Segoe UI Bold
if not os.path.exists(font_path):
    font_path = "C:/Windows/Fonts/arialbd.ttf" # Arial Bold

try:
    font_title = ImageFont.truetype(font_path, 80)
    font_subtitle = ImageFont.truetype(font_path, 32)
except:
    font_title = ImageFont.load_default()
    font_subtitle = ImageFont.load_default()

draw.text((80, 180), "RentLog", fill=white, font=font_title)
draw.text((80, 280), "Professional. Calm. Private.", fill=mint_green, font=font_subtitle)
draw.text((80, 330), "Generate HRA receipts in seconds.", fill=mint_green.replace('D','C'), font=font_subtitle)

# Save the graphic
img.save("feature_graphic_1024_500.png")
print("Successfully generated feature_graphic_1024_500.png")
