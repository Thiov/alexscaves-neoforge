#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// Holographic projection. Upstream drew a translucent duplicate of the scanned entity/player into a Citadel
// post target that a shader then washed cyan-blue with scanlines and a flicker. This port has no post target,
// so — exactly like rendertype_irradiated.fsh bakes the radioactive green into the overlay — we bake the
// hologram look straight into the sampled model: keep the skin's shape/detail as luminance, but recolor it to
// the cool blue projector palette and add moving horizontal scanlines + a faint flicker so it reads as a
// projected light-field rather than a solid recolor. vertexColor.a carries the projector fade (spin-up /
// switch amount), and the .rgb carries the caller's per-vertex tint.
void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    if (tex.a < 0.1) {
        discard;
    }
    // Skin detail as a single luminance value drives the hologram brightness.
    float luma = dot(tex.rgb, vec3(0.299, 0.587, 0.114));
    // Cool holographic blue palette: dark navy in the shadows up to bright cyan in the highlights.
    vec3 holoColor = mix(vec3(0.05, 0.30, 0.70), vec3(0.55, 0.85, 1.0), luma);
    // Moving horizontal scanlines over the model, panned by GameTime.
    float scanline = 0.85 + 0.15 * sin((texCoord0.y * 90.0) - GameTime * 1200.0);
    // Slow whole-hologram flicker so the projection reads as unstable light.
    float flicker = 0.9 + 0.1 * sin(GameTime * 4000.0);
    vec3 outRgb = holoColor * scanline * flicker * vertexColor.rgb;
    // Semi-transparent projection: the skin luminance and the projector fade both drive the alpha so brighter
    // parts of the model are more visible, dimmer parts more see-through.
    float alpha = clamp((0.35 + luma * 0.5) * vertexColor.a, 0.0, 1.0);
    fragColor = vec4(outRgb, alpha);
}
