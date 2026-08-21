#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec2 u_resolution;
uniform float time;
uniform vec4 colour_1;
uniform vec4 colour_2;

#define PI 3.14159265359

float line(float distanceToLine, float width) {
    return 1.0 - smoothstep(width, width * 2.5, distanceToLine);
}

float gridLine(float value, float scale) {
    float cell = abs(fract(value * scale + 0.5) - 0.5) / scale;
    return line(cell, 0.0025);
}

void main() {
    vec2 uv = v_texCoords;
    vec2 p = uv * 2.0 - 1.0;
    p.x *= u_resolution.x / max(u_resolution.y, 1.0);

    float t = time * 0.32;

    // A quiet coordinate plane that drifts almost imperceptibly.
    vec2 movingGrid = p + vec2(t * 0.025, -t * 0.015);
    float minorGrid = max(gridLine(movingGrid.x, 8.0), gridLine(movingGrid.y, 8.0));
    float majorGrid = max(gridLine(movingGrid.x, 2.0), gridLine(movingGrid.y, 2.0));
    float axes = max(line(abs(p.x), 0.002), line(abs(p.y), 0.002));

    // Three elementary functions, moving at different frequencies.
    float waveA = 0.27 * sin(p.x * 3.0 + t * 1.7);
    float waveB = 0.18 * sin(p.x * 5.0 - t * 1.15 + sin(t) * 0.5);
    float waveC = 0.10 * cos(p.x * 9.0 + t * 0.65);
    float curveA = line(abs(p.y - waveA), 0.006);
    float curveB = line(abs(p.y - waveB), 0.0045);
    float curveC = line(abs(p.y - waveC), 0.0035);

    // Polar rose: r = a + b*cos(k*theta + time).
    vec2 rosePos = p - vec2(0.48 * sin(t * 0.31), 0.18 * cos(t * 0.27));
    float angle = atan(rosePos.y, rosePos.x);
    float radius = length(rosePos);
    float roseRadius = 0.34 + 0.085 * cos(6.0 * angle + t * 0.9);
    float rose = line(abs(radius - roseRadius), 0.005);
    float halo = exp(-11.0 * abs(radius - roseRadius));

    // Slow interference field: sin(x+t) + sin(y-t) = constant.
    float field = sin(p.x * 4.0 + t) + sin(p.y * 4.0 - t * 0.8);
    float contour = line(abs(fract(field * 0.42 + 0.5) - 0.5), 0.018);

    vec3 navy = vec3(0.018, 0.028, 0.065);
    vec3 blue = colour_1.rgb;
    vec3 violet = colour_2.rgb;
    vec3 color = navy;
    color += vec3(0.018, 0.035, 0.075) * (1.0 - uv.y);
    color += blue * minorGrid * 0.055;
    color += blue * majorGrid * 0.12;
    color += mix(blue, violet, 0.35) * axes * 0.20;
    color += blue * curveA * 0.95;
    color += violet * curveB * 0.82;
    color += mix(blue, violet, 0.5) * curveC * 0.58;
    color += violet * rose * 1.15;
    color += mix(blue, violet, 0.7) * halo * 0.16;
    color += blue * contour * 0.075;

    // Subtle vignette keeps desktop icons readable near the edges.
    float vignette = 1.0 - 0.32 * smoothstep(0.45, 1.45, length(p));
    vec4 tex = texture2D(u_texture, v_texCoords);
    gl_FragColor = vec4(color * vignette, tex.a) * v_color;
}
