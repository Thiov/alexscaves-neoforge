#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// Gamma (blue) outer glow shell — see rendertype_irradiated_shell.fsh.
void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    if (tex.a < 0.1) {
        discard;
    }
    float animation1 = sin(GameTime * 2000.0) + 1.0;
    vec3 effectColor = vec3(0.15, 0.6, animation1 * 0.1 + 0.75);
    fragColor = vec4(effectColor, tex.a * vertexColor.a);
}
