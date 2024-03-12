package kw.mulitplay.game.Mode;

import java.util.ArrayList;
import java.util.HashSet;

public class Snake {
	/**
	 * ����������ѭ����
	 */
	public int c=0;
	/**
	 * �����С
	 */
	public final static int size =10;
	/**
	 * ��ͼ��С
	 */
	public final static int map_size=100;
	/**
	 * ��ͷ
	 */
	private Node first;
	/**
	 * �ߵ�β�ͣ�ʵ����β���ߵ���һ���ڵ�
	 */
	private Node tail;
	/**
	 * ��β
	 */
	private Node last;
	/**
	 * �ߵ����ݽṹ
	 */
	private ArrayList<Node> s=new ArrayList<Node>();
	/**
	 * ��ͼ�������ߵĽڵ�,�ߵ�String�洢
	 */
	private HashSet<String> map=new HashSet<String>();

	/**
	 * ����
	 */
	private int dir;// 8 6 2 4  �� �� �� ��
	/**
	 * ʳ��
	 */
	private Node food;
	
	public Snake(){

	}
	public Snake(Node first,Node last,Node food,Node tail){
		this.first=first;
		this.last=last;
		this.food=food;
		this.tail=tail;
	}
	/**
	 * 增加一个节点   获取第一个节点   判断food和n的位置是不是一样的
	 *
	 * 如果不一样，那么需要删除一个末尾节点
	 * @param n
	 */
	private void add_Node(Node n){
		s.add(0, n);
		first=s.get(0);
		//�����ӵĽڵ㲻��ʳ�ȥ��β��
		if(!n.toString().equals(food.toString())){
			tail=last;//��¼β��
			s.remove(last);
			last=s.get(s.size()-1);
		}else{
			//如果是事物，就不删除节点，然后直接进行下一个
			food=RandomFood();
		}
	}
	/**
	 * �ƶ�  8 下 6 右  2 上  4 左
	 */
	public void move() {
		//下
		if(dir==8){
			Node n=new Node(first.getX(),first.getY()-10);
			add_Node(n);
		}
		//右
		if(dir==6){
			Node n=new Node(first.getX()+10,first.getY());
			add_Node(n);
		}
		//上
		if(dir==2){
			Node n=new Node(first.getX(),first.getY()+10);
			add_Node(n);
		}
//		左
		if(dir==4){
			Node n=new Node(first.getX()-10,first.getY());
			add_Node(n);
		}
		updataMap(s);
	}
	/**
	 * �������÷����move
	 * @param dir
	 */
	public void move(int dir){
		this.dir=dir;
		move();
	}
	/**
	 * �ж�dir�����ܲ�����
	 * @param
	 * @param dir
	 * @return
	 */
	public boolean canMove(int dir){
		if(dir==8){
			int X=first.getX();
			int Y=first.getY()-10;
			if(Y<10||map.contains(X+"-"+Y)){
				return false;
			}else{
				return true;
			}
		}
		if(dir==6){
			int X=first.getX()+10;
			int Y=first.getY();
			if(X>Snake.map_size||map.contains(X+"-"+Y)){
				return false;
			}else{
				return true;
			}
		}
		if(dir==2){
			int X=first.getX();
			int Y=first.getY()+10;
			if(Y>Snake.map_size||map.contains(X+"-"+Y)){
				return false;
			}else {
				return true;
			}
		}
		if(dir==4){
			int X=first.getX()-10;
			int Y=first.getY();
			if(X<10||map.contains(X+"-"+Y)){
				return false;
			}else{
				return true;
			}
		}
		return false;
	}
//	/**
//	 * StringתNode
//	 * @param s
//	 * @return
//	 */
//	public Node StringToNode(String s){
//		String []str=s.split("-");
//		int x = Integer.parseInt(str[0]);
//		int y = Integer.parseInt(str[1]);
//		return new Node(x,y);
//	}
	/**
	 * NodeתString,ͻȻ�������ֱ��д��toString����
	 * @param n
	 * @return
	 */
//	public String NodeToString(Node n){
//		return n.getX()+"-"+n.getY();
//	}
	/**
	 * ���µ�ͼ�Ϸ��ʹ���λ��
	 * @param s
	 */
	public void updataMap(ArrayList<Node> s){
		map.clear();//���Ƴ��ɵ�վλ��
		for(Node n:s){
			map.add(n.toString());
		}
	}
	/**
	 * ʳ����������
	 */
	public Node RandomFood() {
		c=0;
		while(true){
			int x = 0,y;
			 x = Snake.size*(int) (Math.random() * Snake.map_size/Snake.size)+10;
			 y = Snake.size*(int) (Math.random() * Snake.map_size/Snake.size)+10;
			Node n=new Node(x,y);
			//ʳ�ﲻ�ܳ�����������,s.contains��Ȼ��鲻��Node,
			//ͻȻ�뵽�ǲ���Node��û��дequals��������
			if(!s.contains(n)){
				return n;
			}
		}
	}
	

	/**
	 * 
	 * @return �߳�
	 */
	public int getLen() {
		return s.size();
	}
	/**
	 * @return β��lsat�ĺ�һ���ڵ�
	 */
	public Node getTail() {
		return tail;
	}
	
	public void setTail(Node tail) {
		this.tail = tail;
	}
	
	public HashSet<String> getMap() {
		return map;
	}
	public Node getFirst() {
		return first;
	}

	public Node getLast() {
		return last;
	}

	public ArrayList<Node> getS() {
		return s;
	}

	public void setFirst(Node first) {
		this.first = first;
	}
	public void setLast(Node last) {
		this.last = last;
	}
	public void setS(ArrayList<Node> s) {
		this.s = s;
	}
	public void setMap(HashSet<String> map) {
		this.map = map;
	}
	public void setFood(Node food) {
		this.food = food;
	}
	public int getDir() {
		return dir;
	}

	public void setDir(int dir) {
		this.dir = dir;
	}

	public Node getFood() {
		return food;
	}
}
