//- Copyright � 2008-2009 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.ui.model;

import limelight.Context;
import limelight.styles.abstrstyling.*;
import limelight.util.Colors;
import limelight.ui.Panel;
import limelight.ui.api.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StageFrame extends JFrame implements KeyListener
{
  private static final StyleAttributeCompiler widthCompiler = Context.instance().styleAttributeCompilerFactory.compiler("dimension", "stage width");
  private static final StyleAttributeCompiler heightCompiler = Context.instance().styleAttributeCompilerFactory.compiler("dimension", "stage height");
  private static final StyleAttributeCompiler xCompiler = Context.instance().styleAttributeCompilerFactory.compiler("x-coordinate", "stage x-coordinate");
  private static final StyleAttributeCompiler yCompiler = Context.instance().styleAttributeCompilerFactory.compiler("y-coordinate", "stage y-coordinate");
  private static final NoneableAttribute<IntegerAttribute> NONE = new NoneableAttribute<IntegerAttribute>(null);

  private Stage stage;
  protected RootPanel root;
  private Insets insets;
  private boolean fullscreen;
  private boolean hasMenuBar;
  private GraphicsDevice graphicsDevice; //used for testing
  private Insets screenInsets; //used for testing
  private boolean kiosk;
  private Dimension sizeBeforeFullScreen;
  private Point locationBeforeFullScreen;
  private DimensionAttribute widthStyle = (DimensionAttribute)widthCompiler.compile(500);
  private DimensionAttribute heightStyle = (DimensionAttribute)heightCompiler.compile(500);
  private XCoordinateAttribute xLocationStyle = (XCoordinateAttribute)xCompiler.compile("center");
  private YCoordinateAttribute yLocationStyle = (YCoordinateAttribute)yCompiler.compile("center");
  private boolean vital = true;

  protected StageFrame()
  {
  }

  public StageFrame(Stage stage)
  {
    this();
    this.stage = stage;
    setContentPane(new LimelightContentPane(this));
    setBackground(Color.WHITE);

    Context.instance().frameManager.watch(this);
    setIconImage(new ImageIcon(Context.instance().limelightHome + "/bin/icons/icon_48.gif").getImage());
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addKeyListener(this);
  }

  public void doLayout()
  {
    super.doLayout();
    refresh();
  }

  public void close()
  {
    setVisible(false);
    exitKioskOrFullscreenIfNeeded();
    dispose();
  }

  public void open()
  {
    if(!hasMenuBar)
      setJMenuBar(null);

    applySizeStyles();
    applyLocationStyles();
    
    setVisible(true);
    refresh();
  }

  public void setVisible(boolean visible)
  {
    if(visible == isVisible())
      return;
    super.setVisible(visible);

    if(visible)
    {
      enterKioskOrFullscreenIfNeeded();
    }
    else
      exitKioskOrFullscreenIfNeeded();
  }

  public void refresh()
  {
    if(root != null)
    {
      root.getPanel().markAsNeedingLayout();
    }
  }

  //TODO - MDM - auto dimensions are currently not supported.  This needs to be addressed. 
  public void setSizeStyles(Object widthValue, Object heightValue)
  {
    widthStyle = (DimensionAttribute) widthCompiler.compile(widthValue);
    heightStyle = (DimensionAttribute) heightCompiler.compile(heightValue);

    applySizeStyles();
  }
  
  public DimensionAttribute getWidthStyle()
  {
    return widthStyle;
  }

  public DimensionAttribute getHeightStyle()
  {
    return heightStyle;
  }

  public void setLocationStyles(Object xValue, Object yValue)
  {
    xLocationStyle = (XCoordinateAttribute) xCompiler.compile(xValue);
    yLocationStyle = (YCoordinateAttribute) yCompiler.compile(yValue);

    applyLocationStyles();
  }

  public XCoordinateAttribute getXLocationStyle()
  {
    return xLocationStyle;
  }

  public YCoordinateAttribute getYLocationStyle()
  {
    return yLocationStyle;
  }

  public void load(Panel child)
  {
    if(root != null)
      root.destroy();
    getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    root = new RootPanel(this);
    root.setPanel(child);
  }

  public Stage getStage()
  {
    return stage;
  }

  public void alert(String message)
  {
    JOptionPane.showMessageDialog(this, message, "Limelight Alert", JOptionPane.WARNING_MESSAGE);
  }

  public RootPanel getRoot()
  {
    return root;
  }

  public int getVerticalInsetWidth()
  {
    if(insets == null)
      calculateInsets();
    return insets.top + insets.bottom;
  }

  public int getHorizontalInsetWidth()
  {
    if(insets == null)
      calculateInsets();
    return insets.left + insets.right;
  }

  public void setFullScreen(boolean setting)
  {
    if(fullscreen != setting)
    {
      fullscreen = setting;
      if(fullscreen && isVisible() && this != getGraphicsDevice().getFullScreenWindow())
        turnFullScreenOn();
      else if(this == getGraphicsDevice().getFullScreenWindow() && !kiosk)
        turnFullscreenOff();
    }
  }

  public void setScreenInsets(Insets insets)  // Used for testing
  {
    screenInsets = insets;
  }

  public void setGraphicsDevice(GraphicsDevice device)  // Used for testing
  {
    this.graphicsDevice = device;
  }

  public GraphicsDevice getGraphicsDevice()
  {
    if(graphicsDevice != null)
      return graphicsDevice;
    return getGraphicsConfiguration().getDevice();
  }

  public boolean isFullScreen()
  {
    return fullscreen;
  }

  public void setHasMenuBar(boolean value)
  {
    hasMenuBar = value;
  }

  public void keyTyped(KeyEvent e)
  {
    Panel panel = root.getPanel();
    if(panel != null)
      panel.keyTyped(e);
  }

  public void keyPressed(KeyEvent e)
  {
    Panel panel = root.getPanel();
    if(panel != null)
      panel.keyPressed(e);
  }

  public void keyReleased(KeyEvent e)
  {
    Panel panel = root.getPanel();
    if(panel != null)
      panel.keyReleased(e);
  }

  public void setBackgroundColor(Object colorString)
  {
    setBackground(Colors.resolve(colorString.toString()));
  }

  public String getBackgroundColor()
  {
    return Colors.toString(getBackground());
  }

  public void setKiosk(boolean value)
  {
    if(kiosk == value)
      return;
    kiosk = value;
    if(isVisible())
    {
      if(kiosk)
      {
        if(getGraphicsDevice().getFullScreenWindow() != this)
          turnFullScreenOn();
        Context.instance().os.enterKioskMode();
      }
      else
      {
        if(!fullscreen && getGraphicsDevice().getFullScreenWindow() == this)
          turnFullscreenOff();
        Context.instance().os.exitKioskMode();
      }
    }
  }

  public boolean isKiosk()
  {
    return kiosk;
  }

  public boolean shouldAllowClose()
  {
    return stage.should_allow_close();
  }

  public boolean isVital()
  {
    return vital;
  }

  public void setVital(boolean value)
  {
    vital = value;
  }

  // Protected ////////////////////////////////////////////

  protected void setStage(Stage stage)
  {
    this.stage = stage;
  }

  // Private //////////////////////////////////////////////

  private void enterKioskOrFullscreenIfNeeded()
  {
    if(fullscreen || kiosk)
      turnFullScreenOn();
    if(kiosk)
      Context.instance().os.enterKioskMode();
  }

  private void calculateInsets()
  {
    Dimension size = getSize();
    setSize(0, 0);
    setVisible(true);
    insets = getInsets();
    setVisible(false);
    setSize(size);
    if(getJMenuBar() != null)
      insets.top += getJMenuBar().getHeight();
  }

  private void exitKioskOrFullscreenIfNeeded()
  {
    if(fullscreen || kiosk)
      turnFullscreenOff();
    if(kiosk)
      Context.instance().os.exitKioskMode();
  }

  private void turnFullScreenOn()
  {
    sizeBeforeFullScreen = getSize();
    locationBeforeFullScreen = getLocation();
    getGraphicsDevice().setFullScreenWindow(this);
  }

  private void turnFullscreenOff()
  {
    getGraphicsDevice().setFullScreenWindow(null);
    if(sizeBeforeFullScreen != null)
      super.setSize(sizeBeforeFullScreen);
    if(locationBeforeFullScreen != null)
      super.setLocation(locationBeforeFullScreen);
  }

  private limelight.util.Box getUsableScreenBounds()
  {
    GraphicsConfiguration config = getGraphicsDevice().getDefaultConfiguration();
    limelight.util.Box bounds = new limelight.util.Box(config.getBounds());

    Insets insets = screenInsets == null ? Toolkit.getDefaultToolkit().getScreenInsets(config) : screenInsets;

    bounds.shave(insets.top, insets.right, insets.bottom, insets.left);

    return bounds;
  }

  private void applySizeStyles()
  {
    Rectangle usableBounds = getUsableScreenBounds();

    int width = widthStyle.calculateDimension(usableBounds.width, NONE, NONE);
    int height = heightStyle.calculateDimension(usableBounds.height, NONE, NONE);

    setSize(width, height);
  }

  private void applyLocationStyles()
  {
    Rectangle usableBounds = getUsableScreenBounds();

    int x = xLocationStyle.getX(getWidth(), usableBounds);
    int y = yLocationStyle.getY(getHeight(), usableBounds);

    setLocation(x, y);
  }
}

