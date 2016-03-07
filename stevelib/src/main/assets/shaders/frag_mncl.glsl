precision mediump float;

//Specular color of material - Assume specular always reflects all light
const vec4 specMaterialColor = vec4(1.0,1.0,1.0,1.0);

//Position of light
uniform vec4 lightPosition;

//Light color and intensity
uniform vec4 lightColorIntensity;

//Ambient light color and intensity
uniform vec4 ambientColorIntensity;

// How shiny the material is.  This determines the exponent used in rendering.
uniform float shininess;

//From vertex shader
varying vec4 varyingPosition;
varying vec4 varyingColor;
varying vec4 varyingNormal;


void main()
{
    //Base color of material
    vec4 baseMaterialColor = varyingColor;

    //Normalize the surface's normal
    vec4 surfaceNormal = normalize(varyingNormal);

    //Calc ambient color
    vec4 ambientColor = baseMaterialColor * ambientColorIntensity;
    //Calc diffuse color
    vec4 diffuseColor = baseMaterialColor * lightColorIntensity;
    //Calc specular color
    vec4 specColor = specMaterialColor * lightColorIntensity;

    //Incoming light vector to current position
    vec4 incomingLightDirection = normalize(varyingPosition-lightPosition);

    //Reflected light vector from current position
    vec4 outgoingLightDirection = reflect(incomingLightDirection,surfaceNormal);

    //Vector from position to eye.  Since all geometry is assumed to be in eye space, the eye is always at the origin.
    vec4 positionToEyeDirection = normalize(-varyingPosition);

    //Calculate how bright various types of light are
	float diffuseBrightness = max(dot(incomingLightDirection,-surfaceNormal),0.0);
	float specBrightness = max(dot(outgoingLightDirection, positionToEyeDirection),0.0);
    specBrightness = pow(specBrightness,shininess);

    //Color of fragment is the combination of all colors
	gl_FragColor = ambientColor + diffuseBrightness * diffuseColor + specBrightness * specColor;
}
