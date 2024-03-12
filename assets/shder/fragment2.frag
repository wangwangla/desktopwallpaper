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
    vec2 ar = vec2(1.0,1.0);
    vec2 p = (uv-.5)*ar;
    p *= 1.0-smoothstep(35., 37.5, 1.0);
    vec3 c = vec3(.8);
    float rnd = fract(dot(4432.1123 ,cos((fract(v_texCoords.y*975.321+uv_texCoordsv.x*123.345)))));
    float time = 1.0 + rnd*.3;
    c = mix(c,vec3(1.0,1.0,1.0),.2+cos(time+tan(log(length(p)))));

    gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
    gl_FragColor.rgb = gl_FragColor.rgb * c;
}


