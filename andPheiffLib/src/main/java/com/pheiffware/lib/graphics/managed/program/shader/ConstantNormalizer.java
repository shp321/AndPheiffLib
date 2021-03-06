package com.pheiffware.lib.graphics.managed.program.shader;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces inline int/float values with existing constants or newly created anonymous constants.
 * This is necessary as constant memory can be quite limited and GLSL makes no requirements about
 * intelligent use of constant memory.  In other words, if "1.0" appears several times in a shader
 * each instance may use up precious constant memory.
 * Example:
 * <p>
 * Constants: int x=5; float y=6.0
 * <p>
 * Code:
 * int blah = 5 + 6;
 * float var = 6.0;
 * <p>
 * New Constants: int I_6=6; float F_6_0=6.0;
 * <p>
 * New Code:
 * int blah = x + I_6;
 * float var = F_6_0;
 * <p>
 * <p>
 * Created by Steve on 8/6/2017.
 */

class ConstantNormalizer
{
    //TODO 0.5 = 1/2: Replace {1,100} with +
    //Regex for an isolated integer constant in code.  Note: {1,100} is a "fancy" version of "+".  For some reason Android device requires bounded maximum length in look behind.
    private static final Pattern intPattern = Pattern.compile("(?<!([a-zA-Z0-9_.]{1,100}))(?<!layout \\( location = )\\d+(?![a-zA-Z0-9_.]+)");

    //Regex for an isolated float constant in code
    private static final Pattern floatPattern = Pattern.compile("[0-9]*\\.[0-9]+");

    private final LinkedHashMap<Object, ShaderConstant> valueToConstantMap = new LinkedHashMap<>();
    private LinkedList<ShaderConstant> normalizedConstants = new LinkedList<>();
    private String code;

    /**
     * Performs normalization.  Results obtained via, getCode() and getNormalizedConstants().
     */
    void normalize(List<ShaderConstant> constants, String code)
    {
        clear();
        this.code = code;
        for (ShaderConstant constant : constants)
        {
            valueToConstantMap.put(constant.getValue(), constant);
            normalizedConstants.add(constant);
        }

        normalizeIntConstants();
        normalizeFloatConstants();
    }

    /**
     * Get normalized code with comments replace.
     *
     * @return
     */
    String getCode()
    {
        return code;
    }

    /**
     * Get normalized constants.  This will be the original list of constants, in order, with new anonymous constants added to the end.
     *
     * @return
     */
    LinkedList<ShaderConstant> getNormalizedConstants()
    {
        return normalizedConstants;
    }

    private void clear()
    {
        valueToConstantMap.clear();
        normalizedConstants.clear();
    }

    /**
     * Finds and replaces all inline integer values with references to constants.
     */
    private void normalizeIntConstants()
    {
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = intPattern.matcher(code);
        while (matcher.find())
        {
            int value = Integer.parseInt(matcher.group());
            ShaderConstant shaderConstant = valueToConstantMap.get(value);
            if (shaderConstant == null)
            {
                shaderConstant = addAnonymousIntConstant(value);
            }
            matcher.appendReplacement(buffer, shaderConstant.getName());
        }
        matcher.appendTail(buffer);
        code = buffer.toString();
    }

    /**
     * Finds and replaces all inline float values with references to constants.
     */
    private void normalizeFloatConstants()
    {
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = floatPattern.matcher(code);
        while (matcher.find())
        {
            Float value = Float.parseFloat(matcher.group());
            ShaderConstant shaderConstant = valueToConstantMap.get(value);
            if (shaderConstant == null)
            {
                shaderConstant = addAnonymousFloatConstant(value);
            }
            matcher.appendReplacement(buffer, shaderConstant.getName());
        }
        matcher.appendTail(buffer);
        code = buffer.toString();
    }

    /**
     * Creates an anonymous integer constant to replace the given inline value.
     * name = "I_value"
     *
     * @param value
     * @return
     */
    private ShaderConstant addAnonymousIntConstant(int value)
    {
        ShaderConstant shaderConstant = new ShaderConstant("I_" + value, "int", value, "<Generated>");
        valueToConstantMap.put(shaderConstant.getValue(), shaderConstant);
        normalizedConstants.add(shaderConstant);
        return shaderConstant;
    }

    /**
     * Creates an anonymous integer constant to replace the given inline value.
     * name = "F_whole_decimal".  A negative sign is replaced with "neg".
     *
     * @param value
     * @return
     */
    private ShaderConstant addAnonymousFloatConstant(float value)
    {
        String name = String.valueOf(value).replace(".", "_");
        name = name.replace("-", "neg");
        ShaderConstant shaderConstant = new ShaderConstant("F_" + name, "float", value, "<Generated>");
        valueToConstantMap.put(shaderConstant.getValue(), shaderConstant);
        normalizedConstants.add(shaderConstant);
        return shaderConstant;
    }
}
