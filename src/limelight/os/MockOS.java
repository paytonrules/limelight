package limelight.os;

public class MockOS extends OS
{
  protected void turnOnKioskMode()
  {
  }

  protected void turnOffKioskMode()
  {
  }

  protected void startBrowserAt(String URL)
  {
  }

  public void configureSystemProperties()
  {
    System.setProperty("jruby.shell", "silly shell");
    System.setProperty("jruby.script", "sticky script");
  }
}
