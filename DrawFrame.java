import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;
import javax.swing.JComboBox.*;
import java.io.Serializable;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.geom.*;


class Figure implements Serializable{
  protected int x, y, width, height;   //図形の座標、幅、高さのフィールド//
  protected Color color;               //図形の色のフィールド//
  public Figure(int x, int y, int w, int h, Color c){
    this.x = x; this.y = y;
    width = w; height = h;
    color = c; 
  }
  public void setSize(int w, int h) {    //図形の大きさを設定するメソッド//
    width = w;
    height = h;
  }
  public void setLocation(int x, int y) {    //図形の位置(左端上の座標)を設定するメソッド
    this.x = x; this.y = y;
  }
  public void reshape(int x1, int y1, int x2, int y2) {     //図形の大きさや位置などの設定を変更するメソッド。内部でsetSizeメソッドとsetLocationメソッドを呼び出す。
    int newx = Math.min(x1, x2);
    int newy = Math.min(y1, y2);
    int neww = Math.abs(x1 - x2);
    int newh = Math.abs(y1 - y2);
    setLocation(newx, newy);
    setSize(neww, newh);
  }
  public void draw(Graphics2D g) {};
}

class RectangleFigure extends Figure implements Serializable{    //四角形としてdrawメソッドを設定しFigureに値を渡すクラス//
  public RectangleFigure(int x, int y, int w, int h, Color c) {
    super(x, y, w, h, c);
  }
  public void draw(Graphics2D g) {
    g.setPaint(color);
    g.draw(new Rectangle2D.Double(x, y, width, height));
  }
}

class CircleFigure extends Figure implements Serializable{    //楕円としてdrawメソッドを設定しFigureに値を渡すクラス//
  public CircleFigure(int x, int y, int w, int h, Color c){
    super(x, y, w, h, c);
  }
  public void draw(Graphics2D g) {
    g.setPaint(color);
    g.draw(new Ellipse2D.Double(x, y, width, height));
  }
}

class LineFigure extends Figure implements Serializable{     //直線としてdrawメソッドを設定しFigureに値を渡すクラス//
  public LineFigure(int x, int y, int w, int h, Color c){
    super(x, y, w, h, c);
  }
  public void draw(Graphics2D g) {
    g.setPaint(color);
    g.draw(new Line2D.Double(x, y, x + width, y + height));
  }
}
 
class DrawModel extends Observable {                              
  protected ArrayList<Figure> fig;           //描画した図形を格納しておく可変長配列fig//      
  protected Figure drawingFigure;            //現在描画している図形を記憶しておくフィールド// 
  protected Color currentcolor;             //選択されている、描画する図形の色//
  protected JComboBox<String> combobox;    //色を選択する部品//
  protected JComboBox<String> combobox2;  //形を選択する部品//
  protected JButton a;                   //図形の削除を行うボタン//
  private int i, j;                      //JComboboxのインデックス番号を取得するための変数//
  public DrawModel() {    
    combobox = new JComboBox<>();
    combobox.addActionListener( new SelectColorListener());
    combobox.addItem("red");
    combobox.addItem("blue");
    combobox.addItem("green");
    combobox2 = new JComboBox<>();
    combobox2.addActionListener( new SelectFigureListener());
    combobox2.addItem("Rectangle");
    combobox2.addItem("Circle");
    combobox2.addItem("Line");
    a = new JButton("Undo");
    a.addActionListener(new UndoListener());        
    fig = new ArrayList<Figure>();
    drawingFigure = null;
    try{                             //ストローク形式で保存されているファイルからの読み込み//
      ObjectInputStream in = new ObjectInputStream(new FileInputStream("fig.tmp"));
      fig = (ArrayList<Figure>)in.readObject();
      in.close();
      setChanged();
      notifyObservers();
    }catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }   
  }
  class SelectColorListener implements ActionListener {    //描画する図形の色をcomboboxで選択された色に変更する内部クラス//
    public void actionPerformed(ActionEvent e) {
      i = combobox.getSelectedIndex();
      if(i == 0){
        currentcolor = Color.red;
      }else if(i == 1){
        currentcolor = Color.blue;    
      }else if(i == 2){
        currentcolor = Color.green;
      }    
    }
  }

  class SelectFigureListener implements ActionListener {    //combobox2で選択された形のインデックス番号を保存しておく内部クラス//
    public void actionPerformed(ActionEvent e) {
      j = combobox2.getSelectedIndex();
    }
  }
  
  class UndoListener implements ActionListener {       //Undoボタンが押されると最新の図形を削除する内部クラス//
    public void actionPerformed(ActionEvent e) {
      if(fig.size() > 0){
        fig.remove(fig.size()-1);
      }
      setChanged();
      notifyObservers();
    }
  }
       
  public ArrayList<Figure> getFigures() {         //可変長配列figを返すメソッド//
    return fig;
  }

  public Figure getFigure(int idx) {             //指定されたfigの要素を返すメソッド//
    return fig.get(idx);
  }

  public void createFigure(int x, int y) {          //選択した形によって図形を作り、それをfigに格納してdrawingFigureに保存し、描画を行うメソッド//
    if(j == 0){
      Figure f = new RectangleFigure(x, y, 0, 0, currentcolor);
      fig.add(f);
      drawingFigure = f;
    }else if(j == 1){
      Figure f = new CircleFigure(x, y, 0, 0, currentcolor);
      fig.add(f);
      drawingFigure = f;
    }else if(j == 2){
      Figure f = new LineFigure(x, y, 0, 0, currentcolor);
      fig.add(f);
      drawingFigure = f;
    }
    setChanged();
    notifyObservers();
  }
  public void reshapeFigure(int x1, int y1, int x2, int y2) {      //reshapeメソッドを呼び出してObserverに通知し、現在描画している図形の変形を行うメソッド//
    if(drawingFigure != null) {
       drawingFigure.reshape(x1, y1, x2, y2);
       setChanged();
       notifyObservers();
    }
  }
}

class ViewPanel extends JPanel implements Observer {
  protected DrawModel model;                       //DrawModelのオブジェクト//
  public ViewPanel(DrawModel m, DrawController c) {
    this.setBackground(Color.white);
    this.addMouseListener(c);
    this.addMouseMotionListener(c);
    model = m;
    model.addObserver(this);
  }
  public void paintComponent(Graphics g) {       //再描画とストローク形式、JPEG形式での保存を行うメソッド//
    super.paintComponent(g);
    ArrayList<Figure> fig = model.getFigures();
    BufferedImage readImage = null;             //描画した図形をJPEG形式でファイル"fig.jpeg"に出力する//
    if (readImage == null){
      readImage = 
      new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_BGR);
    }
    Graphics2D off = readImage.createGraphics();
      for(int i = 0; i < fig.size(); i++) {
      Figure f = fig.get(i);
      f.draw(off);
    }
    if (readImage != null){
      g.drawImage(readImage, 0, 0, this);
    }
            
    try {
      boolean result = ImageIO.write(readImage, "jpeg", new File("fig.jpeg"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    try{                        //描画した図形をストローク形式でファイル"fig.tmp"に出力する//
      ObjectOutput out = new ObjectOutputStream(new FileOutputStream("fig.tmp"));
      out.writeObject(fig);
      out.flush();
      out.close();
    }catch (FileNotFoundException e) {
      e.printStackTrace();
    }catch (IOException e) {
      e.printStackTrace();
    }
  }  
  public void update(Observable o, Object arg) {   //Observableから通知されるとrepaint()でpaintComponentメソッドを呼び出すメソッド//
    repaint();
  }
}

class DrawController implements MouseListener, MouseMotionListener {
  protected DrawModel model;                 //DrawModelのオブジェクト//
  protected int dragStartX, dragStartY;
  public DrawController(DrawModel a){                                 
    model = a;
  }
  public void mouseClicked(MouseEvent e) {}
  public void mousePressed(MouseEvent e) {    //マウスが押されるとその座標をドラッグの開始地点として記憶し、createFigureを呼び出して図形を描画するメソッド//
    dragStartX = e.getX();
    dragStartY = e.getY();
    model.createFigure(dragStartX, dragStartY);
  }
  public void mouseDragged(MouseEvent e) {      //マウスがドラッグされるとreshapeFigureを呼び出し、図形を変形するメソッド//
    model.reshapeFigure(dragStartX, dragStartY, e.getX(), e.getY());
  }
  public void mouseReleased(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}
  public void mouseMoved(MouseEvent e) {}
}

class DrawFrame extends JFrame {
  DrawModel model;               //DrawModelのオブジェクト//
  ViewPanel view;                //ViewPanelのオブジェクト//
  DrawController cont;          //DrawControllerのオブジェクト//
  public DrawFrame() {                           
    model = new DrawModel();
    cont = new DrawController(model);
    view = new ViewPanel(model, cont);
    this.setBackground(Color.black);
    this.setTitle("Draw Editor");
    this.setSize(500, 500);
    JPanel p = new JPanel();   
    p.setLayout(new GridLayout(1,3));
    p.add(model.combobox);
    p.add(model.combobox2);
    p.add(model.a);
    this.add(p, BorderLayout.NORTH);
    this.add(view);  
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setVisible(true);
  }
	
  public static void main(String[] args) {
    new DrawFrame();
  }
}
