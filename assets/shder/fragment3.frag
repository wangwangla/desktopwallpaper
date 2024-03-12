#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
void main()
{
    float thickness = 1;
    float fade = 0.1;
    float distance = 1.0 - length(v_texCoords);
    vec3 color = vec3(smoothstep(0.0, fade, distance));


    gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
    gl_FragColor.rgb = gl_FragColor.rgb * color;
}
