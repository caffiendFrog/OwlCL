package com.essaid.owlcl.core.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

public interface IOwlclManager {

  public Path getTemporaryDirectory();

  public void loadNativeLibrary(InputStream stream);

  public String getNativeResourcePrefix(String resourceGroupName);

  public URL getCodeUrl();

  public File getCodeJar();

  public File getCodeDirectory();

  public File getCodeExtDirectory();

  public File getHomeDirectory();

  public File getCurrentDirectory();

  public File getWorkDirectory();

  public File getWorkExtDirectory();

}