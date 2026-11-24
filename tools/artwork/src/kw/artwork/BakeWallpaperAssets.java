package kw.artwork;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.files.FileHandle;

/** Offline authoring only. Runtime loads the resulting PNG/FNT assets through Asset. */
public final class BakeWallpaperAssets {
    private static Path root;
    private static void png(String name,BufferedImage image)throws Exception {Path file=root.resolve(name);Files.createDirectories(file.getParent());ImageIO.write(image,"png",file.toFile());}
    private static void png(String name,Pixmap image)throws Exception {Path file=root.resolve(name);Files.createDirectories(file.getParent());PixmapIO.writePNG(new FileHandle(file.toFile()),image);image.dispose();}
    private static Graphics2D graphics(BufferedImage image){Graphics2D g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);return g;}
    private static void uiAssets()throws Exception {
        BufferedImage white=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);white.setRGB(0,0,0xffffffff);png("ui/white.png",white);
        BufferedImage panel=new BufferedImage(460,640,BufferedImage.TYPE_INT_ARGB);Graphics2D g=graphics(panel);
        for(int i=0;i<8;i++){g.setColor(new java.awt.Color(88,63,34,8));g.fillRoundRect(5-i/2,10-i/2,450+i,620+i,30,30);}
        g.setColor(java.awt.Color.WHITE);g.fillRoundRect(3,3,454,625,28,28);g.setColor(new java.awt.Color(195,174,143,100));g.drawRoundRect(3,3,453,624,28,28);g.dispose();png("ui/panel.png",panel);
        BufferedImage rounded=new BufferedImage(128,64,BufferedImage.TYPE_INT_ARGB);g=graphics(rounded);g.setColor(java.awt.Color.WHITE);g.fillRoundRect(0,0,128,64,10,10);g.dispose();png("ui/rounded.png",rounded);
        for(String type:new String[]{"cat","hamster","panda"}){BufferedImage image=new BufferedImage(100,90,BufferedImage.TYPE_INT_ARGB);g=graphics(image);new PetArtworkSource().pet(g,type,0,0);g.dispose();png("muyu/pet-"+type+".png",image);}
        for(String kind:new String[]{"petal","leaf","snow","rain","ripple","star"}){
            BufferedImage image=new BufferedImage(kind.equals("ripple")?80:32,32,BufferedImage.TYPE_INT_ARGB);g=graphics(image);g.setColor(java.awt.Color.WHITE);
            switch(kind){case "petal","leaf"->g.fill(new Ellipse2D.Float(3,10,24,10));case "snow"->g.fillOval(11,11,10,10);case "rain"->{g.setStroke(new BasicStroke(2));g.drawLine(13,2,5,29);}case "ripple"->{g.setStroke(new BasicStroke(2));g.drawOval(2,9,75,13);}case "star"->{g.fillOval(12,12,8,8);g.drawLine(16,7,16,25);g.drawLine(7,16,25,16);}}
            g.dispose();png("weather/"+kind+".png",image);
        }
    }
    private static void font(String name,Font font,SortedSet<Integer> chars)throws Exception {
        BufferedImage probe=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);Graphics2D g=graphics(probe);g.setFont(font);FontMetrics fm=g.getFontMetrics();
        int max=0;for(int c:chars)max=Math.max(max,fm.charWidth(c));int cellW=max+6,cellH=fm.getHeight()+6,width=2048,columns=width/cellW,rows=2048/cellH,capacity=columns*rows,pages=(chars.size()+capacity-1)/capacity;
        StringBuilder entries=new StringBuilder();int index=0,page=0;BufferedImage image=null;Graphics2D painter=null;
        for(int code:chars){int next=index/capacity,slot=index%capacity;
            if(image==null||next!=page){if(image!=null){painter.dispose();png("fonts/"+name+"-"+page+".png",image);}page=next;
                int remaining=Math.min(capacity,chars.size()-page*capacity),height=((remaining+columns-1)/columns)*cellH;
                image=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);painter=graphics(image);painter.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);painter.setFont(font);painter.setColor(java.awt.Color.WHITE);
            }
            int x=(slot%columns)*cellW,y=(slot/columns)*cellH;
            Rectangle bounds=font.createGlyphVector(painter.getFontRenderContext(),new String(Character.toChars(code))).getPixelBounds(null,0,0);
            painter.drawString(new String(Character.toChars(code)),x+3,y+3+fm.getAscent());
            entries.append("char id=").append(code).append(" x=").append(x+3+bounds.x).append(" y=").append(y+3+fm.getAscent()+bounds.y).append(" width=").append(bounds.width).append(" height=").append(bounds.height).append(" xoffset=").append(bounds.x).append(" yoffset=").append(fm.getAscent()+bounds.y).append(" xadvance=").append(fm.charWidth(code)).append(" page=").append(page).append(" chnl=15\n");index++;
        }
        painter.dispose();png("fonts/"+name+"-"+page+".png",image);g.dispose();
        StringBuilder fnt=new StringBuilder("info face=\""+font.getName()+"\" size="+font.getSize()+" bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=0,0\ncommon lineHeight="+fm.getHeight()+" base="+fm.getAscent()+" scaleW=2048 scaleH=2048 pages="+pages+" packed=0\n");
        for(int p=0;p<pages;p++)fnt.append("page id=").append(p).append(" file=\"").append(name).append('-').append(p).append(".png\"\n");
        fnt.append("chars count=").append(chars.size()).append('\n').append(entries);Files.writeString(root.resolve("fonts/"+name+".fnt"),fnt);
    }
    public static void main(String[] args)throws Exception {
        root=Path.of(args.length==0?"assets/wallpapers":args[0]);Files.createDirectories(root);
        png("muyu/background.png",MuyuArtworkSource.background());png("muyu/fish.png",MuyuArtworkSource.fish());png("muyu/stick.png",MuyuArtworkSource.stick());uiAssets();
        SeasonArtworkSource seasons=new SeasonArtworkSource();for(int i=0;i<24;i++)png("seasons/term-"+i+".png",seasons.landscape(i));
        for(boolean moon:new boolean[]{false,true}){BufferedImage image=new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);Graphics2D g=graphics(image);g.translate(-1182,-54);
            var orb=SeasonArtworkSource.class.getDeclaredMethod("orb",Graphics2D.class,boolean.class,double.class);orb.setAccessible(true);orb.invoke(seasons,g,moon,0.0);g.dispose();png("weather/"+(moon?"moon":"sun")+".png",image);}
        com.badlogic.gdx.utils.GdxNativesLoader.load();CityArtworkSource city=new CityArtworkSource();png("city/asphalt.png",city.asphalt());
        for(int i=0;i<4;i++){Pixmap[] facade=city.facade(i);png("city/facade-"+i+".png",facade[0]);png("city/glow-"+i+".png",facade[1]);}
        png("city/sun.png",CityArtworkSource.sunTexture());png("weather/cloud.png",CityArtworkSource.cloudTexture());for(int i=0;i<32;i++)png("city/moon-"+i+".png",CityArtworkSource.moonTexture(i/32.0));
        SortedSet<Integer> body=new TreeSet<>(),numbers=new TreeSet<>();for(int c=32;c<127;c++){body.add(c);numbers.add(c);}var encoder=Charset.forName("GB2312").newEncoder();
        for(int c=0x4e00;c<=0x9fff;c++)if(encoder.canEncode((char)c))body.add(c);"·°¥，。！：；、（）—“”→…".codePoints().forEach(body::add);
        font("body",new Font("Microsoft YaHei",Font.PLAIN,32),body);font("numbers",new Font("Serif",Font.PLAIN,88),numbers);
        SortedSet<Integer> heading=new TreeSet<>(numbers);for(String name:kw.manager.core.seasons.SolarTerms.NAMES)name.codePoints().forEach(heading::add);font("heading",new Font("Microsoft YaHei",Font.PLAIN,64),heading);
        System.out.println("Exported wallpaper PNGs and bitmap fonts to "+root.toAbsolutePath());
    }
}
