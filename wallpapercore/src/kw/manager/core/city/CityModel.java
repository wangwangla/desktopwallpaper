package kw.manager.core.city;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import java.util.*;

/** A textured coastal district. Static meshes are grouped by material rather than by window. */
public final class CityModel implements Disposable {
    private static final long VERTICES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        | VertexAttributes.Usage.TextureCoordinates;
    public final Model model;
    public final ModelInstance instance;
    public Material road, sea;
    private final List<ColorAttribute> windows = new ArrayList<>();
    private final kw.manager.core.WallpaperAssets assets=new kw.manager.core.WallpaperAssets();
    private final ModelBuilder builder = new ModelBuilder();
    private final Map<Material, MeshBuilder> writers = new LinkedHashMap<>();
    private final Random random = new Random(1141694);
    private int part;

    public CityModel(String importedModel) {
        Texture asphalt = asphalt();
        road = new Material("road", TextureAttribute.createDiffuse(asphalt),
            ColorAttribute.createSpecular(.12f, .13f, .15f, 1), FloatAttribute.createShininess(42));
        sea = new Material("sea", ColorAttribute.createDiffuse(.12f, .25f, .31f, 1),
            ColorAttribute.createSpecular(.65f, .7f, .7f, 1), FloatAttribute.createShininess(90));
        if (importedModel != null && !importedModel.trim().isEmpty() && Gdx.files.absolute(importedModel).exists()) {
            model = new G3dModelLoader(new JsonReader()).loadModel(Gdx.files.absolute(importedModel),assets::texturePath);
            for(var iterator=model.getManagedDisposables().iterator();iterator.hasNext();)if(iterator.next() instanceof Texture)iterator.remove();
            instance = new ModelInstance(model);
            BoundingBox box = instance.calculateBoundingBox(new BoundingBox());
            Vector3 size = box.getDimensions(new Vector3()), center = box.getCenter(new Vector3());
            float scale = 68f / Math.max(size.x, Math.max(size.z, size.y * 1.5f));
            instance.transform.setToScaling(scale, scale, scale).translate(-center.x, -box.min.y, -center.z);
            return;
        }
        builder.begin();
        MeshPartBuilder ground = mesh(road); ground.setUVRange(0, 0, 80, 50);
        ground.box(0, -.28f, 40, 260, .45f, 146);
        MeshPartBuilder ocean = mesh(sea); ocean.box(0, -.75f, -81, 300, .16f, 96);
        MeshPartBuilder quay = mesh(material("quay", .44f, .44f, .41f));
        quay.box(0, -.2f, -34, 260, .9f, 2.3f);
        Material sidewalk = material("pavement", .53f, .53f, .50f);
        Material roof = material("roof", .33f, .34f, .34f);
        Material trim = material("metal", .53f, .57f, .58f);
        MeshPartBuilder pavements = mesh(sidewalk), roofs = mesh(roof), details = mesh(trim);
        MeshPartBuilder stripes = mesh(material("road-markings", .85f, .84f, .77f));
        MeshPartBuilder grass = mesh(material("park", .20f, .30f, .19f));
        Material[] facades = new Material[4]; MeshPartBuilder[] walls = new MeshPartBuilder[4];
        for (int i = 0; i < facades.length; i++) {
            Texture[] facade = facade(i);
            ColorAttribute glow = ColorAttribute.createEmissive(0, 0, 0, 1); windows.add(glow);
            facades[i] = new Material("building-" + i, TextureAttribute.createDiffuse(facade[0]),
                TextureAttribute.createEmissive(facade[1]), glow,
                ColorAttribute.createSpecular(.18f, .22f, .25f, 1), FloatAttribute.createShininess(34));
            walls[i] = mesh(facades[i]);
        }
        for (int row = 0; row < 4; row++) for (int col = 0; col < 6; col++) {
            float x = -31.5f + col * 12.6f, z = -22.5f + row * 15;
            pavements.box(x, .04f, z, 10.7f, .22f, 12.9f);
            if (row == 2 && col == 3) { grass.box(x, .19f, z, 9.9f, .13f, 12.1f); continue; }
            int family = (col + row) % 4;
            if (row == 0 && col >= 2 && col <= 4) {
                float height = 30 + random.nextFloat() * 17;
                building(walls[family], roofs, details, x, z, 8.7f, height, 10.5f);
                // Setback crowns and equipment rooms break up the skyline.
                walls[family].setUVRange(0, 0, 2.8f / 10.6f, 5.8f / 10.4f);
                walls[family].box(x, height + 1.5f, z, 5.8f, 2.8f, 6.7f);
                details.box(x, height + 3.5f, z, .18f, 3, .18f);
            } else {
                for (int half = 0; half < 2; half++) {
                    float height = 7 + random.nextInt(7) * 2.65f;
                    building(walls[(family + half) % 4], roofs, details, x + (half == 0 ? -2.6f : 2.6f),
                        z, 4.35f, height, 10.5f);
                }
            }
        }
        // Street centerlines and zebra crossings.
        for (float x = -37.8f; x < 39; x += 12.6f) {
            for (float z = -28; z < 32; z += 3.5f) stripes.box(x, .006f, z, .11f, .012f, 1.5f);
            for (float z = -30; z <= 30; z += 15)
                for (int n = 0; n < 5; n++) stripes.box(x - 1 + n * .42f, .012f, z + 2.2f, .22f, .02f, 1.6f);
        }
        for (float z = -30; z <= 30; z += 15)
            for (float x = -38; x < 40; x += 3.4f) stripes.box(x, .012f, z, 1.5f, .02f, .1f);
        MeshPartBuilder trunks = mesh(material("bark", .26f, .22f, .17f));
        MeshPartBuilder leaves = mesh(material("foliage", .13f, .24f, .13f));
        for (int i = 0; i < 38; i++) {
            float x, z;
            if (i < 14) { x = 2 + random.nextFloat() * 9; z = 3 + random.nextFloat() * 10; }
            else { x = -39 + random.nextFloat() * 78; z = 31.6f; }
            trunks.box(x, 1.25f, z, .23f, 2.4f, .23f);
            leaves.sphere(new Matrix4().setToTranslation(x, 3.0f, z), 2.4f, 3.0f, 2.2f, 10, 7);
        }
        Material lamp = new Material("lamp", ColorAttribute.createDiffuse(.8f, .72f, .49f, 1),
            ColorAttribute.createEmissive(.5f, .32f, .08f, 1));
        ColorAttribute lampGlow = (ColorAttribute) lamp.get(ColorAttribute.Emissive); windows.add(lampGlow);
        MeshPartBuilder poles = mesh(material("lamp-posts", .24f, .25f, .25f)), bulbs = mesh(lamp);
        for (float x = -35; x < 40; x += 12.6f) for (float z = -29; z <= 31; z += 15) {
            poles.box(x, 2.2f, z, .12f, 4.4f, .12f); poles.box(x + .35f, 4.4f, z, .8f, .1f, .14f);
            bulbs.box(x + .68f, 4.3f, z, .38f, .12f, .32f);
        }
        // Parked vehicles, roof cabins, glazing and wheel details.
        MeshPartBuilder cars = mesh(material("cars", .35f, .38f, .40f));
        MeshPartBuilder glass = mesh(material("car-glass", .075f, .13f, .17f));
        MeshPartBuilder tyres = mesh(material("tyres", .065f, .066f, .068f));
        for (int i = 0; i < 28; i++) {
            float x = -34 + random.nextFloat() * 68, z = -28.9f + (i % 5) * 15;
            cars.box(x, .46f, z, 1.75f, .56f, .78f); glass.box(x - .08f, .9f, z, .9f, .38f, .68f);
            tyres.box(x - .56f, .23f, z, .28f, .35f, .86f); tyres.box(x + .53f, .23f, z, .28f, .35f, .86f);
        }
        for (Map.Entry<Material, MeshBuilder> entry : writers.entrySet()) {
            Mesh mesh = entry.getValue().end();
            builder.part("city-" + part++, mesh, GL20.GL_TRIANGLES, entry.getKey());
        }
        writers.clear();
        model = builder.end(); instance = new ModelInstance(model);
        windows.clear();
        for (Material material : instance.materials) {
            if ("road".equals(material.id)) road = material;
            if ("sea".equals(material.id)) sea = material;
            ColorAttribute glow = (ColorAttribute) material.get(ColorAttribute.Emissive);
            if (glow != null) windows.add(glow);
        }
    }

    private void building(MeshPartBuilder walls, MeshPartBuilder roofs, MeshPartBuilder details,
                          float x, float z, float width, float height, float depth) {
        // BoxShapeBuilder's wall faces advance U along height and V along width.
        walls.setUVRange(0, 0, height / 10.6f, width / 10.4f);
        walls.box(x, height / 2 + .17f, z, width, height, depth);
        roofs.box(x, height + .28f, z, width + .12f, .24f, depth + .12f);
        roofs.box(x, height + .68f, z + 1.6f, width * .46f, .65f, 2.0f);
        for (int i = 0; i < 3; i++) details.box(x - width * .3f + i * width * .24f,
            height + .65f, z - 2.4f, .65f, .55f, 1.0f);
        details.box(x, .6f, z + depth / 2 + .13f, width * .66f, .18f, .6f);
    }
    private MeshPartBuilder mesh(Material material) {
        MeshBuilder writer = new MeshBuilder();
        writer.begin(VERTICES, GL20.GL_TRIANGLES);
        writers.put(material, writer);
        return writer;
    }
    private Material material(String name, float r, float g, float b) { return new Material(name, ColorAttribute.createDiffuse(r, g, b, 1)); }

    private Texture tiled(String path){Texture texture=assets.texture(path);texture.setWrap(Texture.TextureWrap.Repeat,Texture.TextureWrap.Repeat);return texture;}
    private Texture asphalt(){return tiled("city/asphalt.png");}
    private Texture[] facade(int family){return new Texture[]{tiled("city/facade-"+family+".png"),tiled("city/glow-"+family+".png")};}
    public void lighting(float night, float rain, Color water) {
        for (ColorAttribute glow : windows) glow.color.set(night * .95f, night * .68f, night * .31f, 1);
        ((ColorAttribute) road.get(ColorAttribute.Specular)).color.set(.12f + rain*.38f,.13f + rain*.38f,.15f + rain*.38f,1);
        ((ColorAttribute) sea.get(ColorAttribute.Diffuse)).color.set(water);
    }
    @Override public void dispose() { model.dispose();assets.close(); }
}
