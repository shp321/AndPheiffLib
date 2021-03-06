package com.pheiffware.lib.graphics.managed.program.shader;

import com.pheiffware.lib.ParseException;
import com.pheiffware.lib.graphics.GraphicsException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Removes all comments from given line-fragments of a program.
 * Handle basic preprocessor:
 * #define name
 * #if (!)name
 * #endif
 * (nesting allowed)
 * Created by Steve on 8/4/2017.
 */

class Preprocessor
{
    //Finds any comment related pattern
    private static final Pattern commentPattern = Pattern.compile("(//)|(/\\*)|(\\*/)");

    //Pattern for a #define statement
    private static final Pattern definePattern = Pattern.compile("\\s*#const\\s+(\\S+)\\s+((true)|(false))\\s*");

    //Pattern for an #if statement
    private static final Pattern ifPattern = Pattern.compile("\\s*#if\\s+(!?)(\\S+)\\s*");

    //Pattern for an #endif statement
    private static final Pattern elsePattern = Pattern.compile("\\s*#else\\s*");

    //Pattern for an #endif statement
    private static final Pattern endifPattern = Pattern.compile("\\s*#endif\\s*");

    //Lines of the shader to pre-process
    private final List<ShaderFragment> shaderLines;

    //Is the parser in a block comment or not?
    private boolean inBlockComment = false;

    //A stack of all surrounding #if blocks
    private final LinkedList<Boolean> nestIfState = new LinkedList<>();

    //Whenever a #if block is encountered a new state is added to this stack representing the AND-ed state of it and all surrounding blocks.
    private final LinkedList<Boolean> nestAndIfState = new LinkedList<>();

    //Stores all boolean constants (defines) either preset or found during parsing
    private final Map<String, Boolean> boolConstants = new HashMap<>();

    public Preprocessor(List<ShaderFragment> shaderLines, Map<String, Object> constantSettings)
    {
        this.shaderLines = shaderLines;

        //Pre-load all boolean constants
        for (Map.Entry<String, Object> entry : constantSettings.entrySet())
        {
            if (entry.getValue() instanceof Boolean)
            {
                boolConstants.put(entry.getKey(), (Boolean) entry.getValue());
            }
        }
    }

    /**
     * Removes all comments from the given lines code.
     *
     * @throws GraphicsException
     */
    void preProcess() throws GraphicsException, ParseException
    {
        removeComments();
        processIf();
    }

    private void removeComments() throws GraphicsException
    {
        for (ShaderFragment shaderLine : shaderLines)
        {
            removeCommentsFromLine(shaderLine);
        }
    }

    /**
     * Remove comments from a given line.  Updates the state of inBlockComment as well.
     *
     * @param shaderLine
     * @throws GraphicsException
     */
    private void removeCommentsFromLine(ShaderFragment shaderLine) throws GraphicsException
    {
        StringBuilder builder = new StringBuilder();
        int start = 0;

        Matcher matcher = commentPattern.matcher(shaderLine.code);
        while (matcher.find())
        {
            if (inBlockComment)
            {
                if (matcher.group().equals("*/"))
                {
                    start = matcher.end();
                    inBlockComment = false;
                }
            }
            else
            {
                //Starting line comment
                if (matcher.group().equals("//"))
                {
                    builder.append(shaderLine.code.substring(start, matcher.start()));
                    //Throw away the rest of the line
                    shaderLine.code = builder.toString();
                    return;
                }
                //Starting block comment
                else if (matcher.group().equals("/*"))
                {
                    builder.append(shaderLine.code.substring(start, matcher.start()));
                    inBlockComment = true;
                }
                //Ending block comment
                else
                {
                    throw new GraphicsException("Shader contains block comment end with matching beginning");
                }
            }
        }
        if (!inBlockComment)
        {
            //If in block comment at end of line, then clip until end of line
            builder.append(shaderLine.code.substring(start, shaderLine.code.length()));
        }
        shaderLine.code = builder.toString();
    }

    /**
     * Goes through code processing and removing all #preprocessor statements along with code inside false #if blocks.
     *
     * @throws ParseException
     */
    void processIf() throws ParseException
    {
        for (ShaderFragment shaderLine : shaderLines)
        {
            if (getAndedIfState())
            {
                if (!processDefine(shaderLine))
                {
                    if (!processIf(shaderLine))
                    {
                        if (!processElse(shaderLine))
                        {
                            processEndif(shaderLine);
                        }
                    }
                }
            }
            else
            {
                if (!processIf(shaderLine))
                {
                    if (!processElse(shaderLine))
                    {
                        processEndif(shaderLine);
                    }
                }
                shaderLine.code = "";
            }
        }
        if (nestIfState.size() != 0)
        {
            throw new ParseException("Open #if block");
        }
    }

    private boolean processDefine(ShaderFragment shaderLine)
    {
        Matcher matcher;
        matcher = definePattern.matcher(shaderLine.code);
        if (matcher.matches())
        {
            String defName = matcher.group(1);
            boolean defValue = Boolean.valueOf(matcher.group(2));
            if (!boolConstants.containsKey(defName))
            {
                boolConstants.put(defName, defValue);
            }
            //Throw away all preprocessor lines
            shaderLine.code = "";
            return true;
        }
        return false;
    }

    private boolean processIf(ShaderFragment shaderLine) throws ParseException
    {
        Matcher matcher;
        matcher = ifPattern.matcher(shaderLine.code);
        if (matcher.matches())
        {
            String negate = matcher.group(1);
            String boolConstantName = matcher.group(2);
            Boolean expressionValue = boolConstants.get(boolConstantName);
            if (expressionValue == null)
            {
                throw new ParseException("Undefined constant used in #if: " + boolConstantName);
            }
            if (negate.equals("!"))
            {
                expressionValue = !expressionValue;
            }


            nestAndIfState.add(expressionValue & getAndedIfState());
            nestIfState.add(expressionValue);

            //Throw away all preprocessor lines
            shaderLine.code = "";
            return true;
        }
        return false;
    }

    private boolean processElse(ShaderFragment shaderLine) throws ParseException
    {
        Matcher matcher;
        matcher = elsePattern.matcher(shaderLine.code);
        if (matcher.matches())
        {
            if (nestIfState.size() == 0)
            {
                throw new ParseException("Encountered #else without matching #if");
            }

            boolean invertedIfState = !nestIfState.removeLast();

            //Replace last and-ed state with new and-ed state
            nestAndIfState.removeLast();
            nestAndIfState.add(invertedIfState & getAndedIfState());

            //Add reverse state of the last nested if state
            nestIfState.add(invertedIfState);

            //Throw away all preprocessor lines
            shaderLine.code = "";
            return true;
        }
        return false;
    }

    private void processEndif(ShaderFragment shaderLine) throws ParseException
    {
        Matcher matcher;
        matcher = endifPattern.matcher(shaderLine.code);
        if (matcher.matches())
        {
            if (nestIfState.size() == 0)
            {
                throw new ParseException("Encountered #endif without matching #if");
            }

            nestIfState.removeLast();
            nestAndIfState.removeLast();

            //Throw away all preprocessor lines
            shaderLine.code = "";
        }
    }

    /**
     * Get the combined (AND-ed) true state of the #if blocks surrounding the current #if block.
     * This will be false if 1 or more surrounding blocks is false and true otherwise.
     * <p>
     * If false this code should be removed from the shader.
     *
     * @return
     */
    private boolean getAndedIfState()
    {
        return nestAndIfState.size() == 0 || nestAndIfState.getLast();
    }
}
