package kw.mulitplay.game.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;


import kw.manager.core.Constant;
import kw.mulitplay.game.AI.SnakeAi;
import kw.mulitplay.game.Mode.Node;
import kw.mulitplay.game.Mode.Snake;


public class SnakePanel extends Group{
	Snake snake;
	SnakeAi ai;

	/**
	 * Create the panel.
	 */
	public SnakePanel() {

		Image image = new Image(new Texture("white.png"));
		addActor(image);
		image.setColor(Color.valueOf("88888888"));
		image.setSize(Constant.GAMEWIDTH,Constant.GAMEHIGHT);
		snake=new Snake();

		setSize(snake.map_size * snake.size,snake.map_size * snake.size);
		setDebug(true);
		Node n=new Node(10,10);//�ߵĳ�ʼλ��

		snake.getS().add(n);
		snake.setFirst(n);
		snake.setLast(n);
		snake.setTail(new Node(0,10));//last�ĺ�һ���ڵ�
		snake.setFood(new Node(80,80));//ʳ���ʼλ��
		ai=new SnakeAi();

		touch = true;
	}

	private Pixmap getPixmap(int i2, int i3) {
		i3 = Color.valueOf("FFFFFF").toIntBits();
		Pixmap pixmap = new Pixmap(i2, i2, Pixmap.Format.RGBA8888);
		for (int i = 0; i < i2; i++) {
			for (int i1 = 0; i1 < i2; i1++) {
				pixmap.drawPixel(i, i1, i3);
			}
		}
		return pixmap;
	}

	boolean touch = false;

	Array<Image> array = new Array<>();

	private float time = 0;

	@Override
	public void act(float delta) {
		super.act(delta);
		time += delta;
		if (time > 0.04F){
			time = 0;
			index=0;
			paintSnake(snake);
			paintFood(snake.getFood());
			int dir=ai.play2(snake,snake.getFood());//ѡ����ԣ�play ,play1,play2
			if (dir == -1){
				runnable.run();
				return;
			}
			snake.move(dir);

		}
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (!touch)return;
		super.draw(batch,1);
	}
	private int index = 0;
	/**
	 * @param snake
	 */
	public void paintSnake(Snake snake){
		for (int i = 0; i < snake.getS().size(); i++) {
			Node n = snake.getS().get(i);
			index ++;
			Image image;
			if (array.size>index){
				image = array.get(index - 1);
				image.setPosition((n.getX()-2)*10,(n.getY()-2)*10);
			}else {
				image = new Image(find());
				array.add(image);
				image.setPosition((n.getX()-2)*10,(n.getY()-2)*10);
				addActor(image);
			}
			image.setOrigin(Align.center);

//			if (i==0){
//				if (image!=null) {
//					image.setColor(Color.BLACK);
//				}
//			}else if (i==snake.getS().size()-1){
//				if (image!=null){
//					image.setColor(Color.WHITE);
//				}
//			}else {
//				if (image!=null){
//					image.setColor(Color.BROWN);
//				}
//			}
		}
	}
	/**
	 * ��ʳ��
	 * @param food
	 */
	public void paintFood(Node food){
		index++;
		if (array.size>index){
			Image image = array.get(index - 1);
			image.setPosition((food.getX()-2)*10,(food.getY()-2)*10);
			image.setOrigin(Align.center);

		}else {
//			Pixmap pixmap = getPixmap(20, 100);

			Image image = new Image(find());
			array.add(image);
			addActor(image);
			image.setPosition((food.getX()-2)*10,(food.getY()-2)*10);
			image.setOrigin(Align.center);


		}

	}
	public Texture find(){
		FileHandle xxxx = Gdx.files.internal("xxxx");
		FileHandle[] list = xxxx.list();
		FileHandle handle = list[(int) (Math.random() * (list.length - 1))];
		return new Texture(handle);
	}

	private Runnable runnable;
	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}
}
