#version 330

#moj_import <minecraft:globals.glsl>

// Faithful port of the 1.20.1 "submarine_light" post effect (upstream ClientEvents.postRenderStage loads
// shaders/post/submarine_light.json while the driver rides a submarine in first person with lights on).
// It reads the scene depth and, where the depth is FAR (open water), multiplies the scene brightness by a
// soft radial term centred on the screen — this is the forward flood cone lighting the water you look at.
// Near geometry (the sub hull / cockpit interior) is left untouched, so the effect reads as light thrown
// AHEAD rather than a wash over the whole view. The cone quad in SubmarineRenderer is deliberately NOT the
// first-person beam (upstream skips it in first-person floodlight mode); this shader is.

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform SubmarineConfig {
    float FOV;
};

in vec2 texCoord;

out vec4 fragColor;

const float near = 0.1;
const float far = 1000.0;
const float exposure = 100.1;
const float AOE = 15.0;

float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

void main() {
    float depth = LinearizeDepth(texture(InDepthSampler, texCoord).r);
    float aspect = ScreenSize.x / ScreenSize.y;
    float distance = length(vec3(1.0, (2.0 * texCoord - 1.0) * vec2(aspect, 1.0) * tan(radians(FOV / 2.0))) * depth);
    float d = sqrt(pow(texCoord.x - 0.5, 2.0) + pow(texCoord.y - 0.5, 2.0));
    d = exp(-(d * AOE)) * exposure / (distance * 0.01);
    if (depth > 500.0) {
        fragColor = vec4(texture(InSampler, texCoord).rgb * clamp(1.0 + d, 0.0, 10.0), 1.0);
    } else {
        fragColor = texture(InSampler, texCoord);
    }
}
