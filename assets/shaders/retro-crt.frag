#ifdef GL_ES
precision mediump float;
#endif
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec2 u_resolution;
uniform float u_time;
uniform float u_strength;
void main() {
    vec2 uv = v_texCoords;
    // Zero means an exact bypass, including brightness and edge correction.
    if (u_strength < 0.001) {
        gl_FragColor = texture2D(u_texture, uv) * v_color;
        return;
    }
    vec2 p = uv * 2.0 - 1.0;
    p *= 1.0 - 0.014 * u_strength;
    p += p * p.yx * p.yx * vec2(0.055, 0.075) * u_strength;
    vec2 sampleUV = p * 0.5 + 0.5;
    vec2 edge = 1.0 - smoothstep(vec2(0.975), vec2(1.0), abs(p));
    float mask = edge.x * edge.y;
    vec2 shift = vec2(1.15 * u_strength / u_resolution.x, 0.0);
    vec3 source = texture2D(u_texture, sampleUV).rgb;
    vec3 rgb = vec3(texture2D(u_texture, sampleUV + shift).r,
                    source.g, texture2D(u_texture, sampleUV - shift).b);
    float scan = 0.5 + 0.5 * cos(sampleUV.y * u_resolution.y * 3.14159265);
    rgb *= 1.0 - 0.20 * u_strength * scan;
    vec3 phosphor = 0.5 + 0.5 * cos(sampleUV.x * u_resolution.x * 2.094395
                                   + vec3(0.0, 2.094395, 4.18879));
    rgb *= vec3(1.0) - 0.065 * u_strength * phosphor;
    // A small local glow keeps thin amber text luminous without a blur pass.
    vec2 pixel = 1.0 / u_resolution;
    vec3 glow = texture2D(u_texture, sampleUV + vec2(pixel.x * 2.0, 0.0)).rgb
              + texture2D(u_texture, sampleUV - vec2(pixel.x * 2.0, 0.0)).rgb
              + texture2D(u_texture, sampleUV + vec2(0.0, pixel.y * 2.0)).rgb
              + texture2D(u_texture, sampleUV - vec2(0.0, pixel.y * 2.0)).rgb;
    rgb += glow * 0.045 * u_strength;
    float vignette = 1.0 - 0.22 * u_strength * dot(p, p);
    float flicker = 1.0 + 0.003 * u_strength * sin(u_time * 43.0);
    gl_FragColor = vec4(rgb * vignette * flicker * mask, 1.0) * v_color;
}
