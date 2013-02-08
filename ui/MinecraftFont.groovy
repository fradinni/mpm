import java.awt.Font
import java.io.InputStream
import java.util.Map
import java.util.concurrent.ConcurrentHashMap

public class MinecraftFont {

  // Prepare a static "cache" mapping font names to Font objects.
  private static String[] names = { "minecrafter.ttf" };

  private static Map<String, Font> cache = new ConcurrentHashMap<String, Font>(names.length);
  static {
    for (String name : names) {
      cache.put(name, getFont());
    }
  }

  public static Font getFont() {
    def name = "minecrafter.ttf"
    Font font = null;
    if (cache != null) {
      if ((font = cache.get(name)) != null) {
        return font;
      }
    }
    String fName = "ui/fonts/" + name;
    try {
      InputStream is = MinecraftFont.class.getResourceAsStream(fName);
      font = Font.createFont(Font.TRUETYPE_FONT, is);
      println "Font '${name}' was loaded !" 
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(fName + " not loaded.  Using serif font.");
      font = new Font("serif", Font.PLAIN, 24);
    }
    return font;
  }
}