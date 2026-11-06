#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

// 辐射雨的颜色
const vec3 RADIATION_RAIN_COLOR = vec3(0.58, 1.0, 0.45);

void main() {
    vec4 texel = texture(Sampler0, texCoord0);
    vec4 color = vec4(RADIATION_RAIN_COLOR, texel.a * vertexColor.a * ColorModulator.a);

    if (color.a < 0.1) {
        discard;
    }

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
