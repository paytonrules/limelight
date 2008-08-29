//- Copyright 2008 8th Light, Inc.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.ui.api;

import limelight.SceneLoader;

import java.util.Map;

public interface Scene extends Prop
{
  SceneLoader getLoader();
  Map getStyles();
}


