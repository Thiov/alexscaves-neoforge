#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// Licowitch teleport "double" glow. Upstream drew a duplicate of the witch model with a custom program that
// forced a pulsing pink/purple driven by GameTime; the coplanar overlay let the skin show through underneath.
// Same approach as rendertype_irradiated.fsh: bake the skin INTO the overlay (sample the entity texture and
// mix it toward the pulsing magenta) so it survives 26.1's deferred compositing where a translucent duplicate
// would be covered. vertexColor.a carries the teleport progress fade; the caller tints .rgb magenta so the
// mix stays on-palette.
void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    if (tex.a < 0.1) {
        discard;
    }
    float animation1 = (sin(GameTime * 5000.0) + 1.0) * 0.5;
    vec3 effectColor = vec3(animation1 * 0.15 + 0.85, 0.0, animation1 * 0.15 + 0.85);
    float mixAmount = clamp(0.4 + vertexColor.a * 0.5, 0.0, 0.85);
    vec3 outRgb = mix(tex.rgb, effectColor * vertexColor.rgb, mixAmount);
    fragColor = vec4(outRgb, tex.a * vertexColor.a);
}
