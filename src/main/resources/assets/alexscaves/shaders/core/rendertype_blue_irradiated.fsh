#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// Gamma (blue) irradiated glow — same skin-baking approach as rendertype_irradiated.fsh, pulsing the blue
// channel so the skin shows through a radioactive cyan-blue glow instead of a solid coat.
void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    if (tex.a < 0.1) {
        discard;
    }
    float animation1 = sin(GameTime * 2000.0) + 1.0;
    vec3 effectColor = vec3(0.15, 0.6, animation1 * 0.1 + 0.75);
    float mixAmount = clamp(0.4 + vertexColor.a * 0.5, 0.0, 0.85);
    vec3 outRgb = mix(tex.rgb, effectColor, mixAmount);
    fragColor = vec4(outRgb, tex.a);
}
