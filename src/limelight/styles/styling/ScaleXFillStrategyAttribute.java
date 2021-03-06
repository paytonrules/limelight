package limelight.styles.styling;

import limelight.styles.abstrstyling.FillStrategyAttribute;
import limelight.styles.abstrstyling.XCoordinateAttribute;
import limelight.styles.abstrstyling.YCoordinateAttribute;

import java.awt.*;

public class ScaleXFillStrategyAttribute extends FillStrategyAttribute
{

  public String name()
  {
    return "scale_x";
  }

  public void fill(Graphics2D graphics, Image image, XCoordinateAttribute xCoordinate, YCoordinateAttribute yCoordinate)
  {
    int imageWidth = image.getWidth(null);
    int imageHeight = image.getHeight(null);
    Rectangle area = graphics.getClipBounds();
    int x = xCoordinate.getX(area.width, area);
    int y = yCoordinate.getY(imageHeight, area);

    graphics.drawImage(image, x, y, x + area.width, y + imageHeight,  0, 0, imageWidth, imageHeight, null);
  }
}
