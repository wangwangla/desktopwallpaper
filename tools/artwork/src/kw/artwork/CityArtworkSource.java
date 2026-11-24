package kw.artwork;
import com.badlogic.gdx.graphics.Pixmap;
import java.util.Random;
public final class CityArtworkSource {
private final Random random=new Random(1141694);
    static Pixmap sunTexture() {
        Pixmap image=new Pixmap(256,256,Pixmap.Format.RGBA8888);
        for(int y=0;y<256;y++)for(int x=0;x<256;x++) {
            double r=Math.hypot((x-127.5)/128,(y-127.5)/128);
            float alpha=(float)(Math.exp(-r*r*9)*.15+Math.exp(-r*r*45)*.25);
            if(r<.10)alpha=1; image.setColor(1,1,1,Math.min(1,alpha));image.drawPixel(x,y);
        }
        return image;
    }
    static Pixmap cloudTexture() {
        Pixmap image=new Pixmap(256,104,Pixmap.Format.RGBA8888);
        for(int y=0;y<104;y++)for(int x=0;x<256;x++) {
            double nx=x/256.0,ny=y/104.0,amount=0;
            for(int i=0;i<6;i++) {double cx=.17+i*.13,cy=.43+Math.sin(i*1.8)*.10;
                amount+=Math.exp(-Math.pow((nx-cx)*8,2)-Math.pow((ny-cy)*3.4,2));}
            float alpha=clamp((float)(amount-.22)*.8f,0,.9f);
            float tone=.83f+(1-y/104f)*.17f; image.setColor(tone,tone,tone,alpha);image.drawPixel(x,y);
        }
        return image;
    }
    static Pixmap moonTexture(double phase) {
        Pixmap image=new Pixmap(128,128,Pixmap.Format.RGBA8888);Random noise=new Random(9);
        double a=phase*Math.PI*2;
        for(int y=0;y<128;y++)for(int x=0;x<128;x++) {
            double nx=(x-63.5)/61,ny=(y-63.5)/61,rr=nx*nx+ny*ny;
            if(rr>1)continue; double nz=Math.sqrt(1-rr);
            double incidence=nx*Math.sin(a)-nz*Math.cos(a);
            float lit=incidence>0?.80f:.08f; lit*=.84f+noise.nextFloat()*.16f;
            image.setColor(lit,lit,lit,1);image.drawPixel(x,y);
        }
        return image;
    }
    Pixmap asphalt() {
        Pixmap image = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
        Random noise = new Random(41);
        for (int y = 0; y < 128; y++) for (int x = 0; x < 128; x++) {
            float gray = .17f + noise.nextFloat() * .07f;
            image.setColor(gray, gray * 1.02f, gray * 1.04f, 1); image.drawPixel(x, y);
        }
        return image;
    }
    Pixmap[] facade(int family) {
        Pixmap face = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
        Pixmap glow = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
        float[][] tones = {{.49f,.49f,.46f},{.37f,.43f,.46f},{.58f,.55f,.49f},{.30f,.35f,.39f}};
        float[] tint = tones[family];
        for (int y = 0; y < 128; y++) for (int x = 0; x < 128; x++) {
            float noise = (random.nextFloat() - .5f) * .06f;
            face.setColor(tint[0] + noise, tint[1] + noise, tint[2] + noise, 1); face.drawPixel(x, y);
        }
        glow.setColor(0,0,0,1); glow.fill();
        for (int row = 0; row < 4; row++) for (int col = 0; col < 4; col++) {
            int x = 5 + col * 32, y = 5 + row * 32;
            face.setColor(.16f,.20f,.23f,1); face.fillRectangle(x - 2, y - 2, 25, 25);
            float reflection = .23f + random.nextFloat() * .16f;
            face.setColor(reflection*.82f, reflection*.98f, reflection*1.14f,1); face.fillRectangle(x,y,21,21);
            face.setColor(.53f,.58f,.6f,.6f); face.fillRectangle(x+1,y+2,18,2);
            face.setColor(tint[0]*.65f,tint[1]*.65f,tint[2]*.65f,1); face.fillRectangle(x+10,y,1,21);
            if (random.nextFloat() < .58f) {
                glow.setColor(.65f + random.nextFloat() * .35f, .59f, .31f,1); glow.fillRectangle(x,y,21,21);
                glow.setColor(0,0,0,1); glow.fillRectangle(x+10,y,1,21);
            }
        }
        return new Pixmap[] {face, glow};
    }
private static float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
}
