package kw.manager.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.wallper.asset.Asset;

import javax.swing.WindowConstants;

import kw.manager.core.base.ManagerBaseGame;
import kw.manager.core.base.ManagerBaseScreen;
import kw.manager.core.group.ItemGroup;
import kw.manager.core.group.TileGroup;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/5 10:48
 */
public class ManagerMainScreen extends ManagerBaseScreen {
    private TileGroup zhuoChongBtn;
    private TileGroup bizjiBtn;
    private ScrollPane scrollPane;
    private int currentShowIndex = -1;
    public ManagerMainScreen(ManagerBaseGame game) {
        super(game);
    }

    @Override
    public void initView() {
        super.initView();
        initBG();
        initTopPanel();
        initContent();
        initFooter();

        ManagerConstant.windowListener.windowForward();
    }

    private void initFooter() {
        Image footer = new Image(Asset.getAsset().getTexture("ui/viewTexture/product.png"));
        addActor(footer);
        footer.setPosition(ManagerConstant.GAMEWIDTH/2f,50,Align.center);
    }

    private void initBG() {
        Image managerBG = new Image(Asset.getAsset().getTexture("ui/white.png"));
        addActor(managerBG);
        managerBG.setColor(Color.valueOf("#f2e4d6"));
        managerBG.setSize(ManagerConstant.GAMEWIDTH,ManagerConstant.GAMEHIGHT);
        managerBG.setPosition(ManagerConstant.GAMEWIDTH/2.0f, ManagerConstant.GAMEHIGHT/2.0f, Align.center);
        managerBG.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
            }
        });
    }

    private void initTopPanel() {
        Group topPanel = new Group();
        addActor(topPanel);
        topPanel.setSize(ManagerConstant.GAMEWIDTH, 200);
        topPanel.setPosition(ManagerConstant.GAMEWIDTH / 2f, ManagerConstant.GAMEHIGHT + offsetY, Align.top);
        Image topBg = new Image(Asset.getAsset().getTexture("ui/white.png"));
        topPanel.addActor(topBg);
        topBg.setColor(187 / 255.f, 173 / 255.f, 160 / 255.f, 255 / 255.f);
        topBg.setSize(ManagerConstant.GAMEWIDTH, topPanel.getHeight());
        topBg.setPosition(topPanel.getWidth() / 2f, topPanel.getHeight() / 2f, Align.center);
        topPanel.addActor(new Table() {{
            bizjiBtn = new TileGroup("ui/viewTexture/title1.png");
            bizjiBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    setSelect(0);
                }
            });
            add(bizjiBtn);
            zhuoChongBtn = new TileGroup("ui/viewTexture/title2.png");
            add(zhuoChongBtn);
            zhuoChongBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    setSelect(1);
                }
            });
            pack();
        }});
    }

    public void setSelect(int index){
        if (currentShowIndex == index)return;
        if (index == 1){
            zhuoChongBtn.show();
            bizjiBtn.hide();
        }else {
            zhuoChongBtn.hide();
            bizjiBtn.show();
        }
        this.currentShowIndex = index;
        scrollPane.setScrollX(currentShowIndex * ManagerConstant.GAMEWIDTH);
        scrollPane.validate();
        scrollPane.updateVisualScroll();
    }

    private void initContent(){
        Table contentTable = createContent();
        scrollPane = new ScrollPane(contentTable);
        addActor(scrollPane);
        scrollPane.setSize(ManagerConstant.GAMEWIDTH,ManagerConstant.GAMEHIGHT-200 - 100);
        scrollPane.setCanTouchArea(0,0,0,0);
        scrollPane.setDebug(true);
        scrollPane.setY(100);
        setSelect(0);
    }

    private Table createContent() {
        Table table = new Table();
        initBz(table);
        initZc(table);
        return table;
    }

    public void initZc(Table table){
        Table table1 = new Table() {{
            FileHandle local = Gdx.files.local("resource/zhuochong");
            int i = 1;
            for (FileHandle fileHandle : local.list()) {
                String name = fileHandle.name();
                Texture localTexture = Asset.getAsset().getLocalTexture("resource/wallpaper/" + name + "/" + name + ".jpg");
                if (localTexture == null){
                    continue;
                }
                ItemGroup itemGroup = new ItemGroup(localTexture,name);
                add(itemGroup).padLeft(20).padRight(20).padBottom(10).padTop(10);
                if (i % 3 == 0) {
                    row();
                }
                i ++;
            }
            pack();
            align(Align.top);
        }};
        Group group = new Group();
        group.setSize(ManagerConstant.GAMEWIDTH,ManagerConstant.GAMEHIGHT-200 - 100);
        table.add(group);
        ScrollPane scrollPaneBZ = new ScrollPane(table1);
        group.addActor(scrollPaneBZ);
        scrollPaneBZ.setSize(ManagerConstant.GAMEWIDTH,ManagerConstant.GAMEHIGHT-200 - 100);
        scrollPaneBZ.setCanTouchArea(0,0,10000,1000);
        table.pack();
    }

    public void initBz(Table table){

        Table table1 = new Table() {{
            FileHandle local = Gdx.files.local("resource/wallpaper");
            int i = 1;
            for (FileHandle fileHandle : local.list()) {
                try {
                    String name = fileHandle.name();
                    Texture localTexture = Asset.getAsset().getLocalTexture("/resource/wallpaper/" + name + "/" + name + ".jpg");
                    if (localTexture == null){
                        continue;
                    }
                    ItemGroup itemGroup = new ItemGroup(localTexture,name);
                    add(itemGroup).padLeft(20).padRight(20).padBottom(10).padTop(10);
                    if (i % 3 == 0) {
                        row();
                    }
                    i++;
                }catch (Exception e){

                }
            }
            pack();

            align(Align.top);
        }};
        Group group = new Group();
        group.setSize(ManagerConstant.GAMEWIDTH,ManagerConstant.GAMEHIGHT-200 - 100);
        table.add(group);
        ScrollPane scrollPaneBZ = new ScrollPane(table1);
        group.addActor(scrollPaneBZ);
        scrollPaneBZ.setSize(ManagerConstant.GAMEWIDTH,ManagerConstant.GAMEHIGHT-200 - 100);

        scrollPaneBZ.setCanTouchArea(0,0,10000,1000);
        table.pack();
    }
}
