#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// Outer pink/purple glow "shell" for the licowitch teleport double — mirrors rendertype_irradiated_shell.fsh.
// A pure pulsing-magenta emissive duplicate of the model (no skin mix) that adds a radiating purple halo, the
// same cheap additive-shell trick the radiation glow uses in place of upstream's separate-target screen bloom.
// vertexColor.a carries the teleport progress fade; the caller tints .rgb magenta.
void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    if (tex.a < 0.1) {
        discard;
    }
    float animation1 = (sin(GameTime * 5000.0) + 1.0) * 0.5;
    vec3 effectColor = vec3(animation1 * 0.15 + 0.85, 0.0, animation1 * 0.15 + 0.85);
    fragColor = vec4(effectColor * vertexColor.rgb, tex.a * vertexColor.a);
}
