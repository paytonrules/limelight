package limelight.ui.model2.inputs;

import limelight.ui.model2.updates.Updates;

import javax.swing.*;
import java.awt.*;

public class TextBox extends JTextField
{
  private TextBoxPanel panel;

  public TextBox(TextBoxPanel panel)
  {
    this.panel = panel;
  }

  public void repaint()
  {
    if(panel != null)
      panel.setNeededUpdate(Updates.shallowPaintUpdate);
  }

  public void repaint(long tm, int x, int y, int width, int height)
  {
    if(panel != null)
      panel.setNeededUpdate(Updates.shallowPaintUpdate);
  }

  public void repaint(Rectangle r)
  {
    if(panel != null)
      panel.setNeededUpdate(Updates.shallowPaintUpdate);
  }

  public boolean isShowing()
  {
    return true;
  }
}