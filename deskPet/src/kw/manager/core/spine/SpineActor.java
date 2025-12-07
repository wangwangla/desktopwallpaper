package kw.manager.core.spine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.wallper.csv.spine.AnimationState;
import com.wallper.csv.spine.AnimationStateData;
import com.wallper.csv.spine.Skeleton;
import com.wallper.csv.spine.SkeletonData;
import com.wallper.csv.spine.SkeletonRenderer;
import com.wallper.csv.spine.attachments.RegionAttachment;
import com.wallper.csv.spine.utils.SkeletonDataLoader;

import com.wallper.asset.Asset;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/18 15:48
 */
public class SpineActor extends Actor {
        protected Skeleton skeleton;
        private AnimationState state;
        private SkeletonRenderer renderer;
        private AnimationStateData animData;
        private String path;
        private float rootBoneScaleX = 1.0F;
        private float rootBoneScaleY = 1.0F;
        private float offsetX;
        private float offsetY;
        private AssetManager assetamnagerinstance;
        private boolean active;
        private boolean isClip;
        private float w;
        private float h;
        private boolean customize = true;
        private float beginY;
        private float beginX;

        public SpineActor(String path) {
            this.path = path;
            this.flushAsset();
            this.init(path);
        }

        public SpineActor(String path, boolean flag) {
            this.path = path;
            this.flushAsset();
            this.init(path);
        }

        public SpineActor(String path, String atlas) {
            this.path = path;
            this.assetamnagerinstance = Asset.assetManager;
            if (!this.assetamnagerinstance.isLoaded(path + ".json")) {
                SkeletonDataLoader.SkeletonDataParameter mainSkeletonParameter = new SkeletonDataLoader.SkeletonDataParameter();
                this.assetamnagerinstance.load(path + ".json", SkeletonData.class, mainSkeletonParameter);
                this.assetamnagerinstance.finishLoading();
            }

            this.init(path);
        }

        public SpineActor(String path, String atlas, AssetManager assetManager) {
            this.path = path;
            this.assetamnagerinstance = assetManager;
            if (!this.assetamnagerinstance.isLoaded(path + ".json")) {
                assetManager.load(atlas, TextureAtlas.class);
                assetManager.finishLoading();
                SkeletonDataLoader.SkeletonDataParameter mainSkeletonParameter = new SkeletonDataLoader.SkeletonDataParameter();

                this.assetamnagerinstance.load(path + ".json", SkeletonData.class, mainSkeletonParameter);
                this.assetamnagerinstance.finishLoading();
            }

            this.init(path);
        }

        public void flushAsset() {
            Asset.getAsset();
            this.assetamnagerinstance = Asset.assetManager;
            if (!this.assetamnagerinstance.isLoaded(this.path + ".json")) {
                this.assetamnagerinstance.load(this.path + ".json", SkeletonData.class);
                this.assetamnagerinstance.finishLoading();
            }

        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return this.active;
        }

        public void setColor(Color color) {
            this.skeleton.setColor(color);
            super.setColor(color);
        }

        public void setSize(float width, float height) {
            super.setSize(width, height);
            this.skeleton.getRootBone().setPosition(width / 2.0F, height / 2.0F);
        }

        public void init(String path) {
            this.renderer = Asset.getAsset().getRenderer();
            SkeletonData data = (SkeletonData)this.assetamnagerinstance.get(path + ".json");
            this.skeleton = new Skeleton(data);
            this.animData = new AnimationStateData(data);
            this.state = new AnimationState(this.animData);
            this.rootBoneScaleX = this.skeleton.getRootBone().getScaleX();
            this.rootBoneScaleY = this.skeleton.getRootBone().getScaleY();
        }

        public void setAnimation(String name, boolean loop) {
            this.state.setAnimation(0, name, loop);
        }

        public void setSpineOffset(float offsetX, float offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public void setAnimation(String name, final String name2) {
            this.setAnimation(name, false);
            this.getAnimaState().addListener(new AnimationState.AnimationStateAdapter() {
                public void complete(AnimationState.TrackEntry entry) {
                    setAnimation(name2, true);
                }
            });
        }

        public void setScale(float scaleXY) {
            super.setScale(scaleXY);
            this.skeleton.getRootBone().setScale(scaleXY);
        }

        protected void positionChanged() {
            super.positionChanged();
            this.skeleton.setPosition(this.getX() + this.offsetX, this.getY() + this.offsetY);
        }

        private RegionAttachment attachment;

        private TextureRegion region;

        public void changeTexture(String b1, String b2, TextureRegion region){
            attachment = (RegionAttachment) skeleton.getAttachment(b1, b2);
            this.region = region;
        }


        public void setRotation(float degrees) {
            this.skeleton.getRootBone().setRotation(degrees);
            super.setRotation(degrees);
        }

        public void setSkin(String name) {
            this.skeleton.setSkin(name);
            this.skeleton.setSlotsToSetupPose();
        }

        public void setAttachment(String s, String s1) {
            this.skeleton.setAttachment(s, s1);
        }

        public void dispose() {
            this.remove();
        }

        public void unloadSpine() {
            Asset.getAsset();
            if (Asset.assetManager.isLoaded(this.path + ".json")) {
                Asset.getAsset();
                Asset.assetManager.unload(this.path + ".json");
            }

            this.remove();
        }

        public void addAction(Action action) {
            super.addAction(action);
        }

        public void draw(Batch batch, float parentAlpha) {
            float alpha = this.getColor().a * parentAlpha;
            Color color = this.skeleton.getColor();
            float oldAlpha = color.a;
            Color var10000 = this.skeleton.getColor();
            var10000.a *= alpha;


            this.state.update(Gdx.graphics.getDeltaTime());
            this.state.apply(this.skeleton);
            if (this.customize) {
                this.skeleton.getRootBone().setScale(
                        rootBoneScaleX * getScaleX(),
                        rootBoneScaleY * getScaleY());
            }
            this.skeleton.updateWorldTransform();
            int src = batch.getBlendSrcFunc();
            int dst = batch.getBlendDstFunc();

            if (attachment!=null && region!= null) {
                attachment.setRegion(region);
            }

            if (this.isClip) {
                batch.flush();
                if (this.clipBegin(this.getX() + this.beginX, this.getY() + this.beginY, this.w, this.h)) {
                    if (batch instanceof PolygonSpriteBatch) {
                        this.renderer.draw((PolygonSpriteBatch)batch, this.skeleton);
                    } else {
                        this.renderer.draw(batch, this.skeleton);
                    }

                    batch.flush();
                    this.clipEnd();
                }
            } else {
                this.renderer.draw(batch, this.skeleton);
            }

            batch.setBlendFunction(src, dst);
            color.a = oldAlpha;
        }

        public void setH(float h) {
            this.h = h;
        }

        public void setW(float w) {
            this.w = w;
        }

        public void setClip(boolean clip) {
            this.isClip = clip;
        }

        public Stage getStage() {
            return super.getStage();
        }

        public AnimationState getAnimaState() {
            return this.state;
        }

        public SkeletonData getsData() {
            return this.skeleton.getData();
        }

        public void setCustomizeScale(boolean b) {
            this.customize = b;
        }

        public void setTimeScale(float timeScale) {
            this.state.setTimeScale(timeScale);
        }

        public void completeDispose() {
            this.getAnimaState().addListener(new AnimationState.AnimationStateAdapter() {
                public void complete(AnimationState.TrackEntry entry) {
                    super.complete(entry);
                    remove();
                }
            });
        }

        public void completeRomove() {
            this.getAnimaState().addListener(new AnimationState.AnimationStateAdapter() {
                public void complete(AnimationState.TrackEntry entry) {
                    super.complete(entry);
                    remove();
                }
            });
        }

        public void setFlipX() {
        }

        public void setBeginY(int i) {
            this.beginY = (float)i;
        }

        public void setBeginX(int i) {
            this.beginX = (float)i;
        }
}
