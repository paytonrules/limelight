//- Copyright � 2008-2009 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.ui.model;

import junit.framework.TestCase;
import limelight.ui.api.MockStage;
import limelight.ui.*;
import limelight.Context;
import limelight.KeyboardFocusManager;
import limelight.styles.styling.*;
import limelight.styles.compiling.RealStyleAttributeCompilerFactory;
import limelight.os.MockOS;
import limelight.util.Colors;

import javax.swing.*;
import java.awt.*;

public class StageFrameTest extends TestCase
{
  private MockStage stage;
  private StageFrame frame;
  private FrameManager frameManager;
  public MockGraphicsDevice graphicsDevice;
  private MockOS os;
  private Insets insets;

  public void setUp() throws Exception
  {
    RealStyleAttributeCompilerFactory.install();
    frameManager = new InertFrameManager();
    Context.instance().frameManager = frameManager;
    Context.instance().keyboardFocusManager = new KeyboardFocusManager();

    stage = new MockStage();
    frame = new StageFrame(stage);

    graphicsDevice = new MockGraphicsDevice();
    frame.setGraphicsDevice(graphicsDevice);
    insets = new Insets(0, 0, 0, 0);
    frame.setScreenInsets(insets);

    os = new MockOS();
    Context.instance().os = os;
  }

  public void tearDown() throws Exception
  {
    try
    {
      frame.close();
    }
    catch(Exception e)
    {
      //ok
    }
  }

  public void testIcon() throws Exception
  {
    assertNotNull(frame.getIconImage());
  }

  public void testStage() throws Exception
  {
    assertSame(stage, frame.getStage());
  }

  public void testLoad() throws Exception
  {
    MockPanel panel = new MockPanel();
    frame.load(panel);

    RootPanel root = frame.getRoot();

    assertSame(panel, root.getPanel());
  }

  public void testLoadSetsDefaultCursor() throws Exception
  {
    frame.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    MockPanel panel = new MockPanel();
    frame.load(panel);

    assertEquals(Cursor.DEFAULT_CURSOR, frame.getContentPane().getCursor().getType());
  }

  public void testLoadWillDestroyPreviousRoots() throws Exception
  {
    MockPanel panel = new MockPanel();
    frame.load(panel);

    RootPanel firstRoot = frame.getRoot();
    assertEquals(true, firstRoot.isAlive());

    MockPanel panel2 = new MockPanel();
    frame.load(panel2);

    assertEquals(false, firstRoot.isAlive());
  }

  public void testAddsSelfToFrameManager() throws Exception
  {
    assertEquals(1, frameManager.getFrameCount());
    assertEquals(true, frameManager.isWatching(frame));
  }

  public void testDefaultCloseOperations() throws Exception
  {
    assertEquals(WindowConstants.DO_NOTHING_ON_CLOSE, frame.getDefaultCloseOperation());
  }

  public void testSetFullScreenWhenNotVisible() throws Exception
  {
    frame.setFullScreen(true);

    assertEquals(true, frame.isFullScreen());
    frame.open();
    assertEquals(frame, graphicsDevice.getFullScreenWindow());
  }

  public void testSetFullScreenWhenVisible() throws Exception
  {
    frame.open();
    frame.setFullScreen(true);

    assertEquals(true, frame.isFullScreen());
    assertEquals(frame, graphicsDevice.getFullScreenWindow());
  }

  public void testTurnOffFullScreenWhiledisplayed() throws Exception
  {
    frame.setFullScreen(true);
    frame.open();
    frame.setFullScreen(false);

    assertEquals(false, frame.isFullScreen());
    assertEquals(null, graphicsDevice.getFullScreenWindow());
  }

  public void testSetKioskMode() throws Exception
  {
    frame.setKiosk(true);

    assertEquals(true, frame.isKiosk());
  }

  public void testKioskWillGoFullscreenAndFramelessWhenOpened() throws Exception
  {
    frame.setKiosk(true);
    frame.open();

    assertEquals(frame, graphicsDevice.getFullScreenWindow());
    assertEquals(true, os.isInKioskMode());
  }

  public void testKioskWillGoFullscreenAndFramelessWhenClosed() throws Exception
  {
    frame.setKiosk(true);
    frame.open();
    frame.close();

    assertEquals(null, graphicsDevice.getFullScreenWindow());
    assertEquals(false, os.isInKioskMode());
  }

  public void testKioskModePreservesScreenSetting() throws Exception
  {
    frame.setFullScreen(false);
    frame.setKiosk(true);
    frame.open();
    frame.setKiosk(false);

    assertEquals(null, graphicsDevice.getFullScreenWindow());
    assertEquals(false, os.isInKioskMode());
  }

  public void testKioskModePreservesScreenSettingWithFullscreenOn() throws Exception
  {
    frame.setFullScreen(true);
    frame.setKiosk(true);
    frame.open();
    frame.setKiosk(false);

    assertEquals(frame, graphicsDevice.getFullScreenWindow());
    assertEquals(false, os.isInKioskMode());
  }

  public void testEnterKioskModeWhileOpen() throws Exception
  {
    frame.setKiosk(false);
    frame.open();
    frame.setKiosk(true);

    assertEquals(frame, graphicsDevice.getFullScreenWindow());
    assertEquals(true, os.isInKioskMode());
  }

  public void testHidingWhileInKioskMode() throws Exception
  {
    frame.setKiosk(true);
    frame.open();
    frame.setVisible(false);

    assertEquals(null, graphicsDevice.getFullScreenWindow());
    assertEquals(false, os.isInKioskMode());
  }

  public void testShowingAfterHidingWhileInKioskMode() throws Exception
  {
    frame.setKiosk(true);
    frame.open();
    frame.setVisible(false);
    frame.setVisible(true);

    assertEquals(frame, graphicsDevice.getFullScreenWindow());
    assertEquals(true, os.isInKioskMode());
  }

  public void testFullscreenOffWhenInKioskMode() throws Exception
  {
    frame.setKiosk(true);
    frame.setFullScreen(true);
    frame.open();

    frame.setFullScreen(false);
    assertEquals(frame, graphicsDevice.getFullScreenWindow());
  }

  public void testHideAndShow() throws Exception
  {
    frame.open();
    assertEquals(true, frame.isVisible());

    frame.setVisible(false);
    assertEquals(false, frame.isVisible());

    frame.setVisible(true);
    assertEquals(true, frame.isVisible());
  }

  public void testHideAndShowWithFullScreen() throws Exception
  {
    frame.setFullScreen(true);
    frame.open();
    frame.setVisible(false);
    assertEquals(null, graphicsDevice.getFullScreenWindow());

    frame.setVisible(true);
    assertEquals(frame, graphicsDevice.getFullScreenWindow());
  }

  public void testSettingFullScreenWhileHidden() throws Exception
  {
    frame.open();
    frame.setVisible(false);
    frame.setFullScreen(true);
    assertEquals(null, graphicsDevice.getFullScreenWindow());
  }

  public void testSetBackgroundColor() throws Exception
  {
    frame.setBackgroundColor("blue");
    assertEquals(Colors.resolve("blue"), frame.getBackground());
    assertEquals("#0000FF", frame.getBackgroundColor());

    frame.setBackgroundColor("#abc");
    assertEquals(Colors.resolve("#abc"), frame.getBackground());
    assertEquals("#AABBCC", frame.getBackgroundColor());
  }

  public void testShouldRetainSizeAndLocationWhenComingOutOfFullscreen() throws Exception
  {
    frame.setSizeStyles(128, 456);
    frame.setLocationStyles(12, 34);
    frame.open();

    frame.setFullScreen(true);
    frame.setFullScreen(false);

    assertEquals(new Dimension(128, 456), frame.getSize());
    assertEquals(new Point(12, 34), frame.getLocation());
  }

  public void testShouldAllowClose() throws Exception
  {
    stage.shouldAllowClose = false;
    assertEquals(false, frame.shouldAllowClose());

    stage.shouldAllowClose = true;
    assertEquals(true, frame.shouldAllowClose());
  }

  public void testSettingDimensionStyles() throws Exception
  {
    graphicsDevice.defaultConfiguration.bounds = new Rectangle(0, 0, 1000, 1000);
    insets.set(10, 20, 30, 40);

    frame.setSizeStyles(50, 100);
    assertEquals(new StaticPixelsAttribute(50), frame.getWidthStyle());
    assertEquals(new StaticPixelsAttribute(100), frame.getHeightStyle());
    assertEquals(new Dimension(50, 100), frame.getSize());

    frame.setSizeStyles("50%", "100%");
    assertEquals(new PercentagePixelsAttribute(50.0), frame.getWidthStyle());
    assertEquals(new PercentagePixelsAttribute(100.0), frame.getHeightStyle());
    assertEquals(new Dimension(470, 960), frame.getSize());
  }

  public void testSettingLocationStyles() throws Exception
  {
    graphicsDevice.defaultConfiguration.bounds = new Rectangle(0, 0, 1000, 1000);
    insets.set(10, 20, 30, 40);

    frame.setSize(100, 100);

    frame.setLocationStyles(50, 100);
    assertEquals(new StaticXCoordinateAttribute(50), frame.getXLocationStyle());
    assertEquals(new StaticYCoordinateAttribute(100), frame.getYLocationStyle());
    assertEquals(new Point(insets.left + 50, insets.top + 100), frame.getLocation());

    frame.setLocationStyles("50%", "75%");
    assertEquals(new PercentageXCoordinateAttribute(50.0), frame.getXLocationStyle());
    assertEquals(new PercentageYCoordinateAttribute(75.0), frame.getYLocationStyle());
    assertEquals(new Point(490, 730), frame.getLocation());
  }

  public void testApplyingLocationBeforeSizeWillAdjustBeforeOpening() throws Exception
  {
    graphicsDevice.defaultConfiguration.bounds = new Rectangle(0, 0, 1000, 1000);
    insets.set(10, 20, 30, 40);
    
    frame.setLocationStyles("center", "center");
    frame.setSizeStyles(100, 100);
    frame.open();

    assertEquals(new Point(440, 440), frame.getLocation());
  }

  public void testVitality() throws Exception
  {
    assertEquals(true, frame.isVital());

    frame.setVital(false);

    assertEquals(false, frame.isVital());
  }
}
