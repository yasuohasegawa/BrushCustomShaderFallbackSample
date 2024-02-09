package com.brushfallbacksample

class ShaderUtils {
    companion object {
        val VERTEX_SHADER_CODE = """
            attribute vec4 vPosition;
            void main() {
              gl_Position = vPosition;
            }
        """.trimIndent()

        // AGSL
        val FRAGMENT_SHADER_CODE_BRUSH = """
            uniform float2 resolution;
            uniform float iTime;
            layout(color) uniform half4 color;
            layout(color) uniform half4 color2;
            
            half4 main(in float2 fragCoord) {
                float2 uv = (fragCoord-0.5*resolution.xy)/resolution.y;
                float d = length(uv)-0.1;
                float mixValue = distance(uv, vec2(0, 1));
                vec4 col = mix(color, color2, mixValue);
                col.xyz += 0.5 + 0.5*cos(iTime+uv.xyx+vec3(0,2,4));
                col = mix(col,vec4(1.0),1.0-smoothstep(0.,0.001,d));
                return col;
            }
        """.trimIndent()

        // GLSL
        val FRAGMENT_SHADER_CODE = """
            precision mediump float;
            uniform vec2 resolution;
            uniform vec4 color;
            uniform vec4 color2;
            uniform float iTime;
            void main() {
                vec2 uv = (gl_FragCoord.xy - 0.5 * resolution.xy)/resolution.y;
                float d = length(uv)-0.1;
                float mixValue = distance(uv, vec2(0, 1));
                vec4 col = mix(color, color2, mixValue);
                col.xyz += 0.5 + 0.5*cos(iTime+uv.xyx+vec3(0,2,4));
                col = mix(col,vec4(1.0),1.0-smoothstep(0.,0.001,d));
                gl_FragColor = col;
            }
        """.trimIndent()

        val color = floatArrayOf(1f, 1f, 0f, 1f)
        val color2 = floatArrayOf(0f, 1f, 1f, 1f)
    }
}