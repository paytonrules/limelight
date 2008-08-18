package limelight.ui.model.inputs;

import limelight.ui.Panel;
import limelight.ui.model.BasePanel;
import limelight.ui.model.PropPanel;
import limelight.ui.model.TextAccessor;
import limelight.ui.model.updates.Updates;
import limelight.util.Box;
import limelight.styles.Style;
import limelight.Context;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public abstract class InputPanel extends BasePanel
{
  private Component component;

  protected InputPanel()
  {
    component = createComponent();
  }

  protected abstract Component createComponent();
  protected abstract TextAccessor createTextAccessor();

  public Component getComponent()
  {
    return component;
  }

  public boolean canBeBuffered()
  {
    return false;
  }

  public void setParent(limelight.ui.Panel panel)
  {
    super.setParent(panel);
    if(panel instanceof PropPanel)
    {
      PropPanel propPanel = (PropPanel) panel;
      propPanel.sterilize();
      propPanel.setTextAccessor(createTextAccessor());
    }
  }

  public void setSize(int w, int h)
  {
    super.setSize(w, h);
    component.setSize(w, h);
  }

  public void setLocation(int x, int y)
  {
    super.setLocation(x, y);
    component.setLocation(x, y);
  }

  public Box getChildConsumableArea()
  {
    return getBoundingBox();
  }

  public Box getBoxInsidePadding()
  {
    return getBoundingBox();
  }

  public Style getStyle()
  {
    return getParent().getStyle();
  }

  public int getWidth()
  {
    return component.getWidth();
  }

  public int getHeight()
  {
    return component.getHeight();
  }

  public void paintOn(Graphics2D graphics)
  {
    component.paint(graphics);
  }

  public void mousePressed(MouseEvent e)
  {
    e = translatedEvent(e);
    e.setSource(component);
    for(MouseListener mouseListener : component.getMouseListeners())
      mouseListener.mousePressed(e);
  }

  public void mouseReleased(MouseEvent e)
  {
    e = translatedEvent(e);
    e.setSource(component);
    for(MouseListener mouseListener : component.getMouseListeners())
      mouseListener.mouseReleased(e);
  }

  public void mouseClicked(MouseEvent e)
  {
    e = translatedEvent(e);
    Context.instance().keyboardFocusManager.focusPanel(this);
    for(MouseListener mouseListener : component.getMouseListeners())
      mouseListener.mouseClicked(e);
    setNeededUpdate(Updates.shallowPaintUpdate);
  }

  public void mouseDragged(MouseEvent e)
  {
    e = translatedEvent(e);
    e.setSource(component);
    for(MouseMotionListener mouseListener : component.getMouseMotionListeners())
      mouseListener.mouseDragged(e);
  }

  public InputPanel nextInputPanel()
  {
    InputPanel next = null;
    InputPanel first = null;
    boolean foundMe = false;
    
    for(Panel panel : getRoot())
    {
      if(panel instanceof InputPanel)
      {
        if(foundMe)
        {
          next = (InputPanel)panel;
          break;
        }
        else if(panel == this)
          foundMe = true;
        if(first == null)
          first = (InputPanel)panel;
      }
    }

    if(next != null)
      return next;
    else
      return first;
  }

  public InputPanel previousInputPanel()
  {
    InputPanel previous = null;

    for(Panel panel : getRoot())
    {
      if(panel instanceof InputPanel)
      {
        if(panel == this && previous != null)
        {
          break;
        }
        else
        {
          previous = (InputPanel)panel;
        }
      }
    }

    return previous;
  }
}