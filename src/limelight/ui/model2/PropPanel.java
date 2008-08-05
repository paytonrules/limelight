package limelight.ui.model2;

import limelight.LimelightError;
import limelight.Context;
import limelight.styles.Style;
import limelight.styles.StyleDescriptor;
import limelight.styles.StyleObserver;
import limelight.ui.PaintablePanel;
import limelight.ui.Painter;
import limelight.ui.Panel;
import limelight.ui.api.Prop;
import limelight.ui.api.PropablePanel;
import limelight.ui.model2.inputs.ScrollBarPanel;
import limelight.ui.model2.updates.Updates;
import limelight.ui.painting.BackgroundPainter;
import limelight.ui.painting.Border;
import limelight.ui.painting.BorderPainter;
import limelight.ui.painting.PaintAction;
import limelight.util.Box;
import limelight.util.Util;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.LinkedList;

public class PropPanel extends BasePanel implements PropablePanel, PaintablePanel, StyleObserver
{
  private Prop prop;
  private PropPanelLayout layout;
  private LinkedList<Painter> painters;
  private Border borderShaper;
  private TextAccessor textAccessor;
  private Box boxInsideMargins;
  private Box boxInsideBorders;
  private Box boxInsidePadding;
  private Box childConsumableArea;
  private Style style;
  private PaintAction afterPaintAction;
  private ScrollBarPanel verticalScrollBar;
  private ScrollBarPanel horizontalScrollBar;

  public PropPanel(Prop prop)
  {
    this.prop = prop;
    buildPainters();
    layout = new PropPanelLayout(this);
    textAccessor = new TextPaneTextAccessor(this);
    getStyle().addObserver(this);
  }

  private void buildPainters()
  {
    painters = new LinkedList<Painter>();
    painters.add(new BackgroundPainter(this));
    painters.add(new BorderPainter(this));
  }

  public String getText()
  {
    return textAccessor.getText();
  }

  public void setText(String text) throws LimelightError
  {
    if(!Util.equal(text, getText()))
      setNeededUpdate(Updates.layoutAndPaintUpdate);
    textAccessor.setText(text);
  }

  public TextAccessor getTextAccessor()
  {
    return textAccessor;
  }

  public void setTextAccessor(TextAccessor textAccessor)
  {
    this.textAccessor = textAccessor;
  }

  public void snapToSize()
  {
    Box r = getParent().getChildConsumableArea();
    int newWidth = translateDimension(getProp().getStyle().getWidth(), r.width);
    int newHeight = translateDimension(getProp().getStyle().getHeight(), r.height);
    setSize(newWidth, newHeight);
  }

  private int translateDimension(String sizeString, int maxSize)
  {
    if(sizeString == null)
      return 0;
    else if("auto".equals(sizeString))
      return maxSize;
    else if(sizeString.endsWith("%"))
    {
      double percentage = Double.parseDouble(sizeString.substring(0, sizeString.length() - 1));
      return (int) ((percentage * 0.01) * (double) maxSize);
    }
    else
    {
      return Integer.parseInt(sizeString);
    }
  }

  public Prop getProp()
  {
    return prop;
  }

  public Panel getOwnerOfPoint(Point point)
  {
    Point relativePoint = new Point(point.x - getX(), point.y - getY());
    if(verticalScrollBar != null && verticalScrollBar.containsRelativePoint(relativePoint))
      return verticalScrollBar;
    else if(horizontalScrollBar != null && horizontalScrollBar.containsRelativePoint(relativePoint))
      return horizontalScrollBar;

    for(Panel panel : children)
      if(panel.isFloater() && panel.containsRelativePoint(relativePoint))
        return panel.getOwnerOfPoint(relativePoint);

    for(Panel panel : children)
      if(!panel.isFloater() && panel.containsRelativePoint(relativePoint))
        return panel.getOwnerOfPoint(relativePoint);

    return this;
  }

  public Box getBoxInsideMargins()
  {
    if(boxInsideMargins == null)
    {
      boxInsideMargins = (Box) getBoundingBox().clone();
      boxInsideMargins.shave(getStyle().asInt(getStyle().getTopMargin()), getStyle().asInt(getStyle().getRightMargin()), getStyle().asInt(getStyle().getBottomMargin()), getStyle().asInt(getStyle().getLeftMargin()));
    }
    return boxInsideMargins;
  }

  public Box getBoxInsideBorders()
  {
    if(boxInsideBorders == null)
    {
      boxInsideBorders = (Box) getBoxInsideMargins().clone();
      boxInsideBorders.shave(getStyle().asInt(getStyle().getTopBorderWidth()), getStyle().asInt(getStyle().getRightBorderWidth()), getStyle().asInt(getStyle().getBottomBorderWidth()), getStyle().asInt(getStyle().getLeftBorderWidth()));
    }
    return boxInsideBorders;
  }

  public Box getBoxInsidePadding()
  {
    if(boxInsidePadding == null)
    {
      boxInsidePadding = (Box) getBoxInsideBorders().clone();
      boxInsidePadding.shave(getStyle().asInt(getStyle().getTopPadding()), getStyle().asInt(getStyle().getRightPadding()), getStyle().asInt(getStyle().getBottomPadding()), getStyle().asInt(getStyle().getLeftPadding()));
    }
    return boxInsidePadding;
  }

  public Box getChildConsumableArea()
  {
    if(childConsumableArea == null)
    {
      getBoxInsidePadding();
      int width = verticalScrollBar == null ? boxInsidePadding.width : boxInsidePadding.width - verticalScrollBar.getWidth();
      int height = horizontalScrollBar == null ? boxInsidePadding.height : boxInsidePadding.height - horizontalScrollBar.getHeight();
      childConsumableArea = new Box(boxInsidePadding.x, boxInsidePadding.y, width, height);
    }
    return childConsumableArea;
  }

  public void doLayout()
  {
    if(borderShaper != null)
      borderShaper.updateDimentions();

    layout.doLayout();

    //TODO MDM added because it's needed... kinda fishy though.  There'a a better way.
    if(borderShaper != null)
      borderShaper.setBounds(getBoxInsideMargins());
  }

  public void paintOn(Graphics2D graphics)
  {
    for(Painter painter : painters)
      painter.paint(graphics);

    if(afterPaintAction != null)
      afterPaintAction.invoke(graphics);
  }

  public Style getStyle()
  {
    if(style == null)
    {
      style = prop.getStyle();
    }
    return style;
  }

  public Border getBorderShaper()
  {
    if(borderShaper == null)
      borderShaper = new Border(getStyle(), getBoxInsideMargins());
    return borderShaper;
  }

  public void mousePressed(MouseEvent e)
  {
    getProp().mouse_pressed(translatedEvent(e));
  }

  public void mouseReleased(MouseEvent e)
  {
    getProp().mouse_released(translatedEvent(e));
  }

  public void mouseClicked(MouseEvent e)
  {
    getProp().mouse_clicked(translatedEvent(e));
  }

  public void mouseDragged(MouseEvent e)
  {
    getProp().mouse_dragged(translatedEvent(e));
  }

  public void mouseEntered(MouseEvent e)
  {
    getProp().mouse_entered(translatedEvent(e));
    getProp().hover_on();
  }

  public void mouseExited(MouseEvent e)
  {
    getProp().mouse_exited(translatedEvent(e));
    getProp().hover_off();
  }

  public void mouseMoved(MouseEvent e)
  {
    getProp().mouse_moved(translatedEvent(e));
  }

  public void mouseWheelMoved(MouseWheelEvent e)
  {
    boolean isVertical = e.getModifiers() % 2 == 0;
    ScrollBarPanel scrollBar = isVertical ? verticalScrollBar : horizontalScrollBar;
    if(scrollBar != null)
      scrollBar.setValue(scrollBar.getValue() + e.getUnitsToScroll());
    else
      getParent().mouseWheelMoved(e);
  }

  public void setCursor(Cursor cursor)
  {
    getRoot().setCursor(cursor);
  }

  public void repaint()
  {
//System.err.println("repaint: " + this + ": " + (getParent() != null) + ", " + (getStyle().changed(Style.WIDTH) || getStyle().changed(Style.WIDTH)));
    //TODO Handle the case when the parent needs to repaint.
//    if(getParent() != null && (getStyle().changed(Style.WIDTH) || getStyle().changed(Style.WIDTH)))
//      getParent().repaint();
//    else
//    {
    doLayout();
    PaintJob job = new PaintJob(getAbsoluteBounds());
    //TODO Why are we painting the root panel here?  So wastful! Maybe. Transparency?
    job.paint(((RootPanel) getRoot()).getPanel()); //TODO - cast should not be neccessary here.
    job.applyTo(getRoot().getGraphics());
//    }
  }

  public void paintImmediately(int a, int b, int c, int d)
  {
    repaint();
  }

  public String toString()
  {
    return "PropPanel - " + getProp().getName();
  }

  public void setAfterPaintAction(PaintAction action)
  {
    afterPaintAction = action;
  }

  public PaintAction getAfterPaintAction()
  {
    return afterPaintAction;
  }

  public PropPanelLayout getLayout()
  {
    return layout;
  }

  public LinkedList<Painter> getPainters()
  {
    return painters;
  }

  public boolean isFloater()
  {
    return "on".equals(getStyle().getFloat());
  }

  //TODO super.clearCache() deals with absolut positioning.  Here the boxes are all relative.  They're uneccessarily being cleared.
  public void clearCache()
  {
    super.clearCache();
    boxInsideMargins = null;
    boxInsideBorders = null;
    boxInsidePadding = null;
    childConsumableArea = null;
  }

  public void styleChanged(StyleDescriptor descriptor, String value)
  {
    setNeededUpdate(Updates.layoutAndPaintUpdate);
  }

  public ScrollBarPanel getVerticalScrollBar()
  {
    return verticalScrollBar;
  }

  public ScrollBarPanel getHorizontalScrollBar()
  {
    return horizontalScrollBar;
  }

  public void addVerticalScrollBar()
  {
    verticalScrollBar = new ScrollBarPanel(ScrollBarPanel.VERTICAL);
    add(verticalScrollBar);
    childConsumableArea = null;
  }

  public void addHorizontalScrollBar()
  {
    horizontalScrollBar = new ScrollBarPanel(ScrollBarPanel.HORIZONTAL);
    add(horizontalScrollBar);
    childConsumableArea = null;
  }

  public void removeVerticalScrollBar()
  {
    remove(verticalScrollBar);
    verticalScrollBar = null;
    childConsumableArea = null;
  }

  public void removeHorizontalScrollBar()
  {
    remove(horizontalScrollBar);
    horizontalScrollBar = null;
    childConsumableArea = null;
  }

  public void playSound(String filename)
  {
    Context.instance().audioPlayer.playAuFile(filename);
  }
}
