package limelight.os;

import limelight.Context;
import limelight.io.TempDirectory;

import java.io.IOException;

public abstract class OS
{
  protected boolean inKioskMode;
  protected SystemExecution runtime = new RuntimeExecution();

  protected abstract void turnOnKioskMode();
  protected abstract void turnOffKioskMode();
  protected abstract void startBrowserAt(String URL) throws IOException;
  public abstract void configureSystemProperties();

  public String dataRoot()
  {
    return new TempDirectory().getRoot().getAbsolutePath();
  }

  public void enterKioskMode()
  {
    if(inKioskMode)
      return;
    turnOnKioskMode();
    inKioskMode = true;
  }

  public void exitKioskMode()
  {
    inKioskMode = false;
    turnOffKioskMode();
  }

  public boolean isInKioskMode()
  {
    return inKioskMode;
  }

  public void appIsStarting()
  {
  }

  public void openProduction(String productionPath)
  {
    Context.instance().studio.open(productionPath);
  }

  public void openURL(String URL) throws IOException
  {
    startBrowserAt(URL);
  }

  public void setRuntime(SystemExecution runtime)
  {
    this.runtime = runtime;
  }
}
