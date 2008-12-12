package limelight.ui.model;

import junit.framework.TestCase;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dennyabraham
 * Date: Dec 10, 2008
 * Time: 3:44:56 PM
 * To change this template use File | Settings | File Templates.
 */

public class StyledTextParserTest extends TestCase
{

    private StyledTextParser parser;
    private List<StyledText> parsedTextList;
    private StyledText parsedText;
    private StyledText parsedText1;
    private StyledText parsedText2;
    private StyledText parsedText3;
    private StyledText parsedText4;
    private StyledText parsedText5;
    private StyledText parsedText6;
    private StyledText parsedText7;
    private StyledText parsedText8;
    private StyledText parsedText9;


    public void setUp()
    {
        parser = new StyledTextParser();
    }

    public void testUnstyledText() throws Exception
    {
        parsedTextList = parser.parse("Hello There");
        parsedText = parsedTextList.get(0);

        //parsedText.instanceOf?(StyledText);
        assertTrue(parsedText.equals(new StyledText("Hello There")));
    }

    public void testOtherUnstyledText() throws Exception
    {
        parsedTextList = parser.parse("Howdy");
        parsedText = parsedTextList.get(0);

        assertTrue(parsedText.equals(new StyledText("Howdy")));
    }

    public void testStyledText() throws Exception
    {
        parsedTextList = parser.parse("<b>I am serious</b>");
        parsedText = parsedTextList.get(0);

        assertTrue(parsedText.equals(new StyledText("I am serious", "b")));
    }

    public void testMultipleStyledText() throws Exception
    {
        parsedTextList = parser.parse("<b>I am</b><c> serious</c>");
        parsedText1 = parsedTextList.get(0);
        parsedText2 = parsedTextList.get(1);

        assertTrue(parsedText1.equals(new StyledText("I am", "b")));
        assertTrue(parsedText2.equals(new StyledText(" serious", "c")));
    }

    public void testUnstyledTextBetweenStyledText() throws Exception
    {
        parsedTextList = parser.parse("<b>I am</b> not really<c> serious</c>");

        parsedText1 = parsedTextList.get(0);
        parsedText2 = parsedTextList.get(1);
        parsedText3 = parsedTextList.get(2);

        assertTrue(parsedText1.equals(new StyledText("I am", "b")));
        assertTrue(parsedText2.equals(new StyledText(" not really", "default")));
        assertTrue(parsedText3.equals(new StyledText(" serious", "c")));
    }

    public void testTrailingUnstyledText() throws Exception
    {

        parsedTextList = parser.parse("<b>styled</b> unstyled");

        parsedText1 = parsedTextList.get(0);
        parsedText2 = parsedTextList.get(1);

        assertTrue(parsedText1.equals(new StyledText("styled", "b")));
        assertTrue(parsedText2.equals(new StyledText(" unstyled", "default")));
    }


    public void testAlternatingStylingAndUnstyling() throws Exception
    {
        parsedTextList = parser.parse("unstyled1<b>styled1</b>unstyled2<c>styled2</c>unstyled3");

        parsedText1 = parsedTextList.get(0);
        parsedText2 = parsedTextList.get(1);
        parsedText3 = parsedTextList.get(2);
        parsedText4 = parsedTextList.get(3);
        parsedText5 = parsedTextList.get(4);

        assertTrue(parsedText1.equals(new StyledText("unstyled1", "default")));
        assertTrue(parsedText2.equals(new StyledText("styled1", "b")));
        assertTrue(parsedText3.equals(new StyledText("unstyled2", "default")));
        assertTrue(parsedText4.equals(new StyledText("styled2", "c")));
        assertTrue(parsedText5.equals(new StyledText("unstyled3", "default")));
    }

    public void testNestedTags() throws Exception
    {
        parsedTextList = parser.parse("default<b>b<c>c<d>d<e>e</e>d</d>c</c>b</b>default");

        parsedText1 = parsedTextList.get(0);
        parsedText2 = parsedTextList.get(1);
        parsedText3 = parsedTextList.get(2);
        parsedText4 = parsedTextList.get(3);
        parsedText5 = parsedTextList.get(4);
        parsedText6 = parsedTextList.get(5);
        parsedText7 = parsedTextList.get(6);
        parsedText8 = parsedTextList.get(7);
        parsedText9 = parsedTextList.get(8);

        assertTrue(parsedText1.equals(new StyledText("default", "default")));
        assertTrue(parsedText2.equals(new StyledText("b", "b")));
        assertTrue(parsedText3.equals(new StyledText("c", "c")));
        assertTrue(parsedText4.equals(new StyledText("d", "d")));
        assertTrue(parsedText5.equals(new StyledText("e", "e")));
        assertTrue(parsedText6.equals(new StyledText("d", "d")));
        assertTrue(parsedText7.equals(new StyledText("c", "c")));
        assertTrue(parsedText8.equals(new StyledText("b", "b")));
        assertTrue(parsedText9.equals(new StyledText("default", "default")));
    }

    public void display(StyledText text)
    {
        System.out.println("text: "+text.getText()+"; style: "+text.getStyleName());

    }

    public void testMultipleNestedAndAlternatingTags() throws Exception
    {
        parsedTextList = parser.parse("default<a>a<b>b</b>a</a>default<c>c<d>d</d>c</c>default");

        parsedText1 = parsedTextList.get(0);
        display(parsedText1);
        parsedText2 = parsedTextList.get(1);
        parsedText3 = parsedTextList.get(2);
        parsedText4 = parsedTextList.get(3);
        parsedText5 = parsedTextList.get(4);
        parsedText6 = parsedTextList.get(5);
        parsedText7 = parsedTextList.get(6);
        parsedText8 = parsedTextList.get(7);
        parsedText9 = parsedTextList.get(8);

        assertTrue(parsedText1.equals(new StyledText("default", "default")));
        assertTrue(parsedText2.equals(new StyledText("a", "a")));
        assertTrue(parsedText3.equals(new StyledText("b", "b")));
        assertTrue(parsedText4.equals(new StyledText("a", "a")));
        assertTrue(parsedText5.equals(new StyledText("default", "default")));
        assertTrue(parsedText6.equals(new StyledText("c", "c")));
        assertTrue(parsedText7.equals(new StyledText("d", "d")));
        assertTrue(parsedText8.equals(new StyledText("c", "c")));
        assertTrue(parsedText9.equals(new StyledText("default", "default")));
    }

    public void testMalformedTags() throws Exception
    {
        
    }
    
}
