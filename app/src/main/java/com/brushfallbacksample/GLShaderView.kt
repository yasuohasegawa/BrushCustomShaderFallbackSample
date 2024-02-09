package com.brushfallbacksample

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@Composable
fun GLShaderCompose() {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth(),
        factory = { context ->
            GLSurfaceView(context).apply {
                // Create and set renderer
                setEGLContextClientVersion(2) // Required for GLES 2.0
                setRenderer(GLShaderRenderer(context))
            }
        })
}

class GLShaderRenderer(context: Context) : GLSurfaceView.Renderer {
    private lateinit var mQuad: Quad

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(1f, 1f, 1f, 1f)
        mQuad = Quad()
    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        mQuad.draw()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        mQuad.resolution[0] = width.toFloat()
        mQuad.resolution[1] = height.toFloat()
    }
}

const val COORDS_PER_VERTEX = 3
var quadCoords = floatArrayOf(     // in counterclockwise order:
    -1.0f, -1.0f, 0.0f,      // top left
    1.0f, -1.0f, 0.0f,    // bottom left
    -1.0f,  1.0f, 0.0f,      // bottom right
    1.0f,  1.0f, 0.0f,      // top right
)

class Quad {
    private val vertexShaderCode = ShaderUtils.VERTEX_SHADER_CODE
    private val fragmentShaderCode = ShaderUtils.FRAGMENT_SHADER_CODE

    private val color = ShaderUtils.color
    private val color2 = ShaderUtils.color2

    private val vertexCount: Int = quadCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(quadCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(quadCoords)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    private var mProgram: Int
    private var timeLocation: Int = 0
    private var resolutionLocation: Int = 0
    private var colorLocation: Int = 0
    private var color2Location: Int = 0
    private var startTime: Long = 0

    private var positionHandle: Int = 0

    public var resolution = floatArrayOf(0f,0f)

    init {

        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram().also {

            // add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)
        }

        // Get uniform location for time variable in the shader
        timeLocation = GLES20.glGetUniformLocation(mProgram, "iTime")
        colorLocation = GLES20.glGetUniformLocation(mProgram, "color")
        color2Location = GLES20.glGetUniformLocation(mProgram, "color2")
        resolutionLocation = GLES20.glGetUniformLocation(mProgram, "resolution")

        // Start time for animation
        startTime = System.currentTimeMillis()
    }

    fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    fun draw() {
        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000f

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(it)

            // Prepare the quad coordinate data
            GLES20.glVertexAttribPointer(
                it,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )

            // Set up uniform values
            GLES20.glUniform4fv(colorLocation, 1, color, 0)
            GLES20.glUniform4fv(color2Location, 1, color2, 0)
            GLES20.glUniform1f(timeLocation, elapsedTime)
            GLES20.glUniform2fv(resolutionLocation,1,resolution,0)

            // Draw the triangle
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount)

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(it)
        }
    }
}