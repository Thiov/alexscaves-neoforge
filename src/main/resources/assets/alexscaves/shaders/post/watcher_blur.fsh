#version 330

#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D BlurSampler;

layout(std140) uniform WatcherConfig {
    float FOV;
};

in vec2 texCoord;

out vec4 fragColor;

// Ported from the 1.21.1 "watcher_blur" program (shared by Watcher possession, Beholder eye vision and the
// Hood/Cloak of Darkness view). Composite the sharp screen with a blurred copy using a radial term so the
// CENTER stays sharp (cd == 1 at the middle) and only the periphery blurs and glows outward — the intended
// "vision tunnel" look. The 26.1 port had replaced this with a uniform full-screen box blur, which smeared
// the ENTIRE view into an unreadable blur (so the player couldn't even tell they were turning — issues #2/#29/#30).
void main() {
    vec4 sharp = texture(InSampler, texCoord);
    vec4 blur = texture(BlurSampler, texCoord);
    float aspect = ScreenSize.x / ScreenSize.y;
    vec2 p = (2.0 * texCoord - 1.0) * vec2(aspect, 1.0) * tan(radians(FOV * 0.5));
    float dist = length(vec3(1.0, p.x, p.y));
    float cd = dist * dist;
    fragColor = vec4((sharp - blur + blur * cd).rgb, 1.0);
}
