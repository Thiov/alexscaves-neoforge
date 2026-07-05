#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// Irradiated glow. Upstream drew a semi-transparent pure-green duplicate over the base model so the skin
// showed through the green. In this deferred pipeline a coplanar emissive overlay covers the model opaquely
// (the fragment alpha only dims it, it never lets the base skin blend through), so instead we bake the skin
// INTO the overlay: sample the entity's own texture and mix it toward the pulsing radioactive green. The
// result is the same net look — a translucent-looking green glow with the skin visible underneath — but
// robust to the compositing. vertexColor.a carries the amplifier fade (higher level = greener, skin never
// fully hidden).
void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    if (tex.a < 0.1) {
        discard;
    }
    float animation1 = sin(GameTime * 2000.0) + 1.0;
    vec3 effectColor = vec3(0.15, animation1 * 0.15 + 0.65, 0.0);
    float mixAmount = clamp(0.35 + vertexColor.a * 0.5, 0.0, 0.8);
    vec3 outRgb = mix(tex.rgb, effectColor, mixAmount);
    fragColor = vec4(outRgb, tex.a);
}
