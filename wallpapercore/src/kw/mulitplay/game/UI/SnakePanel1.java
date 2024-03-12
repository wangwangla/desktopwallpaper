package kw.mulitplay.game.UI;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import java.awt.event.KeyAdapter;

import kw.mulitplay.game.AI.SnakeAi;
import kw.mulitplay.game.Mode.Node;
import kw.mulitplay.game.Mode.Snake;


public class SnakePanel1 extends Group{
	Snake snake;
	private int dir =2;
	private Stage s;
	/**
	 * Create the panel.
	 */
	public SnakePanel1(Stage s) {
		this.s = s;
		Pixmap pixmap = getPixmap(620, 30);
		Image image = new Image(new Texture(pixmap));
		addActor(image);
		image.setColor(Color.valueOf("888888"));
		snake=new Snake();
		Node n=new Node(10,10);//�ߵĳ�ʼλ��
		snake.getS().add(n);
		snake.setFirst(n);
		snake.setLast(n);
		snake.setTail(new Node(0,10));//last�ĺ�һ���ڵ�
		snake.setFood(new Node(80,80));//ʳ���ʼλ��

//		8 下 6 右  2 上  4 左

		s.addListener(BackInputListener());
	}

	private InputListener BackInputListener() {
		return new InputListener(){
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if ((keycode == Input.Keys.LEFT)) {
					dir = 4;
				}else if ((keycode == Input.Keys.RIGHT)) {
					dir = 6;
				}else if ((keycode == Input.Keys.DOWN)) {
					dir = 8;
				}else if ((keycode == Input.Keys.UP)) {
					dir = 2;
				}
				return super.keyDown(event, keycode);
			}
		};
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

	private float time = 1;

	@Override
	public void act(float delta) {
		super.act(delta);
		time+=delta;
		if (time > 0.2F) {
			time = 0;
			index = 0;
			paintSnake(snake);
			paintFood(snake.getFood());
			snake.move(dir);
		}

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
				image.setPosition(n.getX()*3,n.getY()*3);
			}else {
				Pixmap pixmap = getPixmap(20, 100);
				image = new Image(new Texture(pixmap));
				array.add(image);
				image.setPosition(n.getX(),n.getY());
				addActor(image);
			}
			if (i==0){
				if (image!=null) {
					image.setColor(Color.BLACK);
				}
			}else if (i==snake.getS().size()-1){
				if (image!=null){
					image.setColor(Color.WHITE);
				}
			}else {
				if (image!=null){
					image.setColor(Color.BROWN);
				}
			}
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
			image.setPosition(food.getX()*3,food.getY()*3);
			image.setColor(Color.BROWN);
		}else {
			Pixmap pixmap = getPixmap(20, 100);
			Image image = new Image(new Texture(pixmap));
			array.add(image);
			addActor(image);
			image.setPosition(food.getX()*3,food.getY()*3);
			image.setColor(Color.BROWN);
		}
	}
}
