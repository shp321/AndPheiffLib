#version 300 es
precision highp float;

//Transforms vertices to eye space
uniform mat4 viewModelMatrix;

//Transforms normals to eye space
uniform mat3 normalMatrix;

//Linear depth projection inputs
uniform float projectionScaleX;
uniform float projectionScaleY;
uniform float projectionMaxDepth;

in vec4 vertexPosition4;
in vec3 vertexNormal;
out vec4 positionEyeSpace;
out vec3 normalEyeSpace;

void main()
{
	normalEyeSpace = normalize(normalMatrix * vertexNormal);
	positionEyeSpace = viewModelMatrix * vertexPosition4;

	gl_Position = vec4(
	    positionEyeSpace.x * projectionScaleX,
	    positionEyeSpace.y * projectionScaleY,
	    //Z - After division by w, z will LINEARLY map from [0,projectionMaxDepth] --> [-1,1]
	    positionEyeSpace.z * (positionEyeSpace.z + projectionMaxDepth * 0.5) / (projectionMaxDepth * 0.5),
    	-positionEyeSpace.z
    );
}