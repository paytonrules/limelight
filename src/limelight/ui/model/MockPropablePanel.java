//- Copyright � 2008-2009 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.ui.model;

import limelight.ui.MockPanel;
import limelight.ui.Panel;
import limelight.ui.PaintablePanel;
import limelight.ui.painting.PaintAction;
import limelight.ui.painting.Border;
import limelight.ui.api.PropablePanel;
import limelight.ui.api.MockProp;
import limelight.ui.api.Prop;
import limelight.util.Box;
import limelight.styles.FlatStyle;
import limelight.styles.Style;

public class MockPropablePanel extends MockPanel implements PropablePanel, PaintablePanel
{
  public final MockProp prop;
  public Style style;
  public Box childConsumableBox;
  private int prepForSnapWidth;
  private int prepForSnapHeight;
  public boolean wasLaidOut;
  public boolean wasRepainted;
  public boolean wasFloatLaidOut;
  public Box boxInsideMargins = new Box(0, 0, 100, 100);
  public Box boxInsideBorders = new Box(0, 0, 100, 100);
  private String name;

  public MockPropablePanel()
  {
    prop = new MockProp();
    style = prop.getStyle();
  }

  public MockPropablePanel(String name)
  {
    this();
    this.name = name;
  }

  public Box getChildConsumableArea()
  {
    if(childConsumableBox != null)
      return childConsumableBox;
    else
      return super.getChildConsumableArea();
  }

  public void paintImmediately(int x, int y, int width, int height)
  {
  }

  public void setAfterPaintAction(PaintAction action)
  {
  }

  public void setText(String text)
  {
  }

  public void repaint()
  {
    wasRepainted = true;
  }

  public String getText()
  {
    return null;
  }

  public Box getBoxInsideBorders()
  {
    return boxInsideBorders;
  }

  public Style getStyle()
  {
    return style;
  }

  public Border getBorderShaper()
  {
    return new Border(getStyle(), boxInsideMargins);
  }

  public Prop getProp()
  {
    return prop;
  }

  //Used by layout
  public void snapToSize()
  {
    setSize(prepForSnapWidth, prepForSnapHeight);
  }

  public void doLayout()
  {
    super.doLayout();
    snapToSize();
    for(Panel child : children)
      child.doLayout();
    wasLaidOut = true;
  }

  public void doFloatLayout()
  {
    super.doFloatLayout();
    wasFloatLaidOut = true;
  }

  public void prepForSnap(int width, int height)
  {
    prepForSnapWidth = width;
    prepForSnapHeight = height;
  }

  public String toString()
  {
    return super.toString() + ":" + name;
  }
}
