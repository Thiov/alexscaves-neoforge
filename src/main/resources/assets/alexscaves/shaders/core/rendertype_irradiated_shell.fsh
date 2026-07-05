#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// Outer glow "shell": a slightly-scaled-up ADDITIVE emissive duplicate of the model, drawn on top of the
// skin-mix overlay to add a radiating green halo (the port can't do upstream's separate-target screen bloom,
// so this is the same cheap additive-shell trick RaygunRenderHelper uses for its beam glow). Pure pulsing
// light — no skin mix — so it reads as emitted radiation; vertexColor.a carries the fade intensity.
void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    if (tex.a < 0.1) {
        discard;
    }
    float animation1 = sin(GameTime * 2000.0) + 1.0;
    vec3 effectColor = vec3(0.15, animation1 * 0.15 + 0.65, 0.0);
    fragColor = vec4(effectColor, tex.a * vertexColor.a);
}
