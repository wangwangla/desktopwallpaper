#ifdef GL_ES

#endif

varying vec4 v_color;
varying vec2 v_textCoords;
uniform sampler2D u_texture;
uniform float time;
uniform float wave_offset;
uniform vec2 center;
uniform float wave_strength;
uniform float wave_radius;

//void main() {
//    float wave_radius = 0.9 * wave_strength;
//    vec2 distance_vec = center - v_textCoords;
//    distance_vec = distance_vec * vec2(1.0, 1.0);
//    float distance = sqrt(distance_vec.x * distance_vec.x + distance_vec.y * distance_vec.y);
//    float sin_factor = sin(distance * 100.0 - time) * 0.7 * wave_strength;
//    float discard_factor = clamp(wave_radius - abs(wave_offset - distance), 0.0, 1.0);
//    vec2 offset = normalize(distance_vec) * sin_factor * discard_factor;
//    vec2 uv = -offset + v_textCoords;
//    gl_FragColor = texture2D(u_texture, uv);
//
//}

void main() {
    vec2 distance_vec = center - v_textCoords;
    distance_vec = distance_vec * vec2(1.0, 1.0);
    float distance = sqrt(distance_vec.x * distance_vec.x + distance_vec.y * distance_vec.y);

    // distance小于1，但是我们希望能有多个波峰波谷，所以在sin的内部乘上一个比较大的倍数
    // sin函数的值在-1到1之间，我们希望偏移值很小，所以输出的时候需要缩小一定的倍数倍
    float sin_factor = sin(distance * 100.0 + time) * 0.1;
    float discard_factor = clamp(wave_radius - abs(wave_offset - distance), 0.0, 1.0);

    // 计算总的uv的偏移值
    vec2 offset = normalize(distance_vec) * sin_factor * discard_factor;
    vec2 uv = offset + v_textCoords;

    gl_FragColor = texture2D(u_texture, uv);
}