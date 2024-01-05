package kw.test;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/5 10:53
 */
public class WallpaperChanger {
    // main
    public static void main(String... args)
    {

    }

    // SPI (http://stackoverflow.com/users/228171/mark-peters)
    public interface SPI extends StdCallLibrary {

        //from MSDN article
        long SPI_SETDESKWALLPAPER = 20;
        long SPIF_UPDATEINIFILE = 0x01;
        long SPIF_SENDWININICHANGE = 0x02;

        SPI INSTANCE = (SPI) Native.loadLibrary("user32", SPI.class, new HashMap<String,Object>() {
            {
                put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
                put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
            }
        });

        boolean SystemParametersInfo(
                WinDef.UINT_PTR uiAction,
                WinDef.UINT_PTR uiParam,
                String pvParam,
                WinDef.UINT_PTR fWinIni
        );
    }


    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }


    public void setWall() {
        try {
            String USER = System.getProperty("user.home");
            String pathToWrite = USER+"\\Pictures\\";
            File create = new File(pathToWrite + "DesktopWallpaper.jpg");
            create.createNewFile();
            ImageIO.write(ImageIO.read(new File("./ad_progress.png")), "png", new File(pathToWrite + "DesktopWallpaper.jpg"));
            String path = pathToWrite + "DesktopWallpaper.jpg";
            WallpaperChanger.SPI.INSTANCE.SystemParametersInfo(
                    new WinDef.UINT_PTR(WallpaperChanger.SPI.SPI_SETDESKWALLPAPER),
                    new WinDef.UINT_PTR(0),
                    path,
                    new WinDef.UINT_PTR(WallpaperChanger.SPI.SPIF_UPDATEINIFILE |
                            WallpaperChanger.SPI.SPIF_SENDWININICHANGE)
            );
            System.exit(0);
        } catch (IOException er) {
            er.printStackTrace();
        }
    }
}
