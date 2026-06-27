#version 330

#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

layout(std140) uniform BumpyConfig {
    vec2 Frequency;
    vec2 WobbleAmount;
};

in vec2 texCoord;

out vec4 fragColor;

// Ported from the 1.21.1 Citadel "bumpy" post program. The old chain fed an accumulating Time uniform;
// 26.1 post passes get no per-chain time, so drive the wobble from the shared GameTime global instead.
void main() {
    float time = GameTime * 5000.0;
    vec2 offset = vec2(
        sin(texCoord.y * Frequency.x + time) * WobbleAmount.x,
        cos(texCoord.x * Frequency.y + time * 1.37) * WobbleAmount.y
    );
    fragColor = texture(InSampler, texCoord + offset);
}
