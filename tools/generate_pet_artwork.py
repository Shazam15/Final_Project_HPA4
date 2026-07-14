from pathlib import Path
from xml.sax.saxutils import escape

ROOT = Path(__file__).resolve().parents[1]
SVG_DIR = ROOT / "app/src/main/assets/artwork"
DRAWABLE_DIR = ROOT / "app/src/main/res/drawable"
SVG_DIR.mkdir(parents=True, exist_ok=True)


def path(data, fill="#000000", stroke=None, width=0, alpha=1.0):
    return {"data": data, "fill": fill, "stroke": stroke, "width": width, "alpha": alpha}


def svg(name, paths):
    content = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" role="img">',
        f'  <title>{escape(name.replace("_", " ").title())}</title>',
    ]
    for item in paths:
        attrs = [f'd="{item["data"]}"', f'fill="{item["fill"]}"']
        if item["stroke"]:
            attrs += [
                f'stroke="{item["stroke"]}"',
                f'stroke-width="{item["width"]}"',
                'stroke-linecap="round"',
                'stroke-linejoin="round"',
            ]
        if item["alpha"] != 1.0:
            attrs.append(f'opacity="{item["alpha"]}"')
        content.append(f'  <path {" ".join(attrs)}/>')
    content.append('</svg>')
    (SVG_DIR / f"{name}.svg").write_text("\n".join(content) + "\n", encoding="utf-8")


def vector(name, paths):
    content = [
        '<?xml version="1.0" encoding="utf-8"?>',
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        '    android:width="256dp"',
        '    android:height="256dp"',
        '    android:viewportWidth="256"',
        '    android:viewportHeight="256">',
    ]
    for item in paths:
        content += [
            '    <path',
            f'        android:pathData="{item["data"]}"',
            f'        android:fillColor="{item["fill"]}"',
        ]
        if item["stroke"]:
            content += [
                f'        android:strokeColor="{item["stroke"]}"',
                f'        android:strokeWidth="{item["width"]}"',
                '        android:strokeLineCap="round"',
                '        android:strokeLineJoin="round"',
            ]
        if item["alpha"] != 1.0:
            content.append(f'        android:fillAlpha="{item["alpha"]}"')
        content[-1] += ' />'
    content.append('</vector>')
    (DRAWABLE_DIR / f"{name}.xml").write_text("\n".join(content) + "\n", encoding="utf-8")


def face(state):
    black = "#0A0A0A"
    clear = "#00000000"
    if state == "happy":
        return [
            path("M101,99a7,7 0,1 0,14 0a7,7 0,1 0,-14 0M141,99a7,7 0,1 0,14 0a7,7 0,1 0,-14 0", black),
            path("M122,111l6,5l6,-5z", black),
            path("M109,122c5,18 33,18 38,0", clear, black, 5),
            path("M76,74c-12,-13 -28,3 -12,17c12,10 12,10 12,10s0,0 12,-10c16,-14 0,-30 -12,-17z", "#FF4B67"),
        ]
    if state == "neutral":
        return [
            path("M101,99a7,7 0,1 0,14 0a7,7 0,1 0,-14 0M141,99a7,7 0,1 0,14 0a7,7 0,1 0,-14 0", black),
            path("M122,111l6,5l6,-5z", black),
            path("M113,130h30", clear, black, 5),
        ]
    if state == "sad":
        return [
            path("M99,99c6,-7 13,-7 19,0M138,99c6,-7 13,-7 19,0", clear, black, 5),
            path("M122,111l6,5l6,-5z", black),
            path("M112,137c8,-13 24,-13 32,0", clear, black, 5),
            path("M159,107c-10,15 -10,20 0,23c10,-3 10,-8 0,-23z", "#48B9FF", black, 3),
        ]
    if state == "sick":
        return [
            path("M101,92l14,14M115,92l-14,14M141,92l14,14M155,92l-14,14", clear, black, 5),
            path("M122,111l6,5l6,-5z", black),
            path("M112,137c8,-13 24,-13 32,0", clear, black, 5),
            path("M169,61h24v24h-24z", "#F5F5F5", black, 4),
            path("M181,66v14M174,73h14", clear, "#E53232", 4),
        ]
    return [
        path("M99,94l18,-5M139,89l18,5", clear, black, 5),
        path("M103,102a6,8 0,1 0,12 0a6,8 0,1 0,-12 0M143,102a6,8 0,1 0,12 0a6,8 0,1 0,-12 0", black),
        path("M122,113l6,5l6,-5z", black),
        path("M111,137c9,-16 25,-16 34,0", clear, black, 5),
        path("M185,62v25M185,96v2", clear, "#F6C445", 7),
    ]


def pet_base(kind):
    black = "#080808"
    color = {"dog": "#F32B32", "cat": "#159FDE", "rabbit": "#49B86E"}[kind]
    if kind == "dog":
        silhouette = "M71,125C56,112 53,86 62,64C71,41 91,34 106,52C119,43 137,43 150,52C166,34 187,41 196,64C205,88 201,111 185,125C201,143 208,167 204,204H174L168,170L162,211H94L88,170L82,204H52C48,166 55,142 71,125Z"
        details = [
            path("M66,66C44,58 39,84 50,105C58,120 72,114 82,101Z", color, black, 7),
            path("M190,66C212,58 217,84 206,105C198,120 184,114 174,101Z", color, black, 7),
            path("M95,205V169M128,210V172M161,205V169", "#00000000", black, 7),
            path("M200,183c22,0 25,18 7,22", "#00000000", black, 7),
        ]
    elif kind == "cat":
        silhouette = "M69,123C57,105 57,78 70,58L78,28L104,48C119,41 137,41 152,48L178,28L186,58C199,79 199,105 187,123C202,143 207,168 202,205H170L165,169L159,211H97L91,169L86,205H54C49,168 54,143 69,123Z"
        details = [
            path("M78,28L104,48L70,58Z", color, black, 7),
            path("M178,28L152,48L186,58Z", color, black, 7),
            path("M95,205V169M128,210V172M161,205V169", "#00000000", black, 7),
            path("M54,190C22,203 20,169 38,161", "#00000000", black, 8),
            path("M92,115H66M92,124H64M164,115H190M164,124H192", "#00000000", black, 4),
        ]
    else:
        silhouette = "M77,122C62,101 66,72 84,57C75,23 89,5 105,14C117,21 119,42 119,48C125,46 131,46 137,48C137,42 139,21 151,14C167,5 181,23 172,57C190,72 194,101 179,122C197,144 204,169 198,205H168L163,170L157,211H99L93,170L88,205H58C52,169 59,144 77,122Z"
        details = [
            path("M91,54C82,26 92,16 101,22C108,28 108,45 107,51Z", "#F6A6B6"),
            path("M165,54C174,26 164,16 155,22C148,28 148,45 149,51Z", "#F6A6B6"),
            path("M100,205V170M128,210V173M156,205V170", "#00000000", black, 7),
            path("M183,157a15,15 0,1 0,30 0a15,15 0,1 0,-30 0", "#F5F5F5", black, 5),
        ]
    return [path(silhouette, color, black, 8)] + details


for pet_kind in ("dog", "cat", "rabbit"):
    for mood in ("happy", "neutral", "sad", "sick", "danger"):
        asset_name = f"pet_{pet_kind}_{mood}"
        artwork = pet_base(pet_kind) + face(mood)
        svg(asset_name, artwork)
        vector(asset_name, artwork)


rewards = {
    "reward_collar_blue": [
        path("M88,139C107,151 149,151 168,139L164,156C143,168 113,168 92,156Z", "#1769E0", "#080808", 6),
        path("M123,157a8,8 0,1 0,16 0a8,8 0,1 0,-16 0", "#F6C445", "#080808", 4),
    ],
    "reward_hat_green": [
        path("M84,63C92,30 111,17 133,22C153,26 166,42 169,65Z", "#35B766", "#080808", 7),
        path("M72,67C95,58 162,58 184,67C170,81 87,81 72,67Z", "#238B4A", "#080808", 7),
    ],
    "reward_hero_cape": [
        path("M72,126C46,142 35,177 40,218L89,204L99,145Z", "#6B43C5", "#080808", 7),
        path("M184,126C210,142 221,177 216,218L167,204L157,145Z", "#6B43C5", "#080808", 7),
        path("M82,132C105,145 151,145 174,132", "#00000000", "#F6C445", 7),
    ],
    "reward_gold_color": [
        path("M128,40a85,85 0,1 0,0 170a85,85 0,1 0,0 -170", "#F6C445", None, 0, 0.20),
        path("M54,62v20M44,72h20M201,105v20M191,115h20M61,177v18M52,186h18", "#00000000", "#F6C445", 6),
    ],
    "reward_garden_bg": [
        path("M24,181C48,146 73,151 96,166C122,137 156,139 178,165C201,150 223,163 235,190V230H21Z", "#67C96F", "#20733C", 6),
        path("M35,198C75,183 173,183 224,202", "#00000000", "#3D9B50", 9),
        path("M54,173v28M45,181l9,8l9,-8M204,170v31M195,178l9,8l9,-8", "#00000000", "#20733C", 5),
        path("M42,169a8,8 0,1 0,16 0a8,8 0,1 0,-16 0M196,166a8,8 0,1 0,16 0a8,8 0,1 0,-16 0", "#F6C445", "#080808", 3),
    ],
}

for reward_name, artwork in rewards.items():
    svg(reward_name, artwork)
    vector(reward_name, artwork)

print(f"Generated 20 SVG files in {SVG_DIR}")
print(f"Generated 20 VectorDrawable files in {DRAWABLE_DIR}")
