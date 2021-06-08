import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;


public class AnimationFrame extends JFrame {

	final public static int FRAMES_PER_SECOND = 60;
	final public static int SCREEN_HEIGHT = 750;
	final public static int SCREEN_WIDTH = 1000;

	private int xpCenter = SCREEN_WIDTH / 2;
	private int ypCenter = SCREEN_HEIGHT / 2;

	private double scale = 1;
	//point in universe on which the screen will center
	private double xCenter = 0;		
	private double yCenter = 0;

	private JPanel panel = null;	
	private JLabel lblPoints;
	private JLabel lblFirstPlace;
	private JLabel lblSecondPlace;
	private JLabel lblThirdPlace;
	private JLabel lblFourthPlace;
	private JLabel lblFifthPlace;
	public static boolean getUser = false;
	private static String username = "";

	private static boolean stop = false;

	private long current_time = 0;								//MILLISECONDS
	private long next_refresh_time = 0;							//MILLISECONDS
	private long last_refresh_time = 0;
	private long minimum_delta_time = 1000 / FRAMES_PER_SECOND;	//MILLISECONDS
	private long actual_delta_time = 0;							//MILLISECONDS
	private long elapsed_time = 0;
	private boolean isPaused = false;

	private KeyboardInput keyboard = new KeyboardInput();
	private Universe universe = null;

	//local (and direct references to various objects in universe ... should reduce lag by avoiding dynamic lookup
	private Animation animation = null;
	private DisplayableSprite player1 = null;
	private ArrayList<DisplayableSprite> sprites = null;
	private Background background = null;
	boolean centreOnPlayer = false;
	int universeLevel = 0;
	
	public AnimationFrame(Animation animation)
	{
		super("");
		
		this.animation = animation;
		this.setVisible(true);		
		this.setFocusable(true);
		this.setSize(SCREEN_WIDTH + 20, SCREEN_HEIGHT + 36);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				this_windowClosing(e);
			}
		});

		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				keyboard.keyPressed(arg0);
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				keyboard.keyReleased(arg0);
			}
		});

		Container cp = getContentPane();
		cp.setBackground(Color.BLACK);
		cp.setLayout(null);

		panel = new DrawPanel();
		panel.setLayout(null);
		panel.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		getContentPane().add(panel, BorderLayout.CENTER);	
		
		//highscores
		
		lblFirstPlace = new JLabel("");
		lblFirstPlace.setForeground(Color.WHITE);
		lblFirstPlace.setFont(new Font("Tahoma", Font.BOLD, 30));
		lblFirstPlace.setBounds(300, 300, 300, 36);
		getContentPane().add(lblFirstPlace);
		getContentPane().setComponentZOrder(lblFirstPlace, 0);
		
		lblSecondPlace = new JLabel("");
		lblSecondPlace.setForeground(Color.WHITE);
		lblSecondPlace.setFont(new Font("Tahoma", Font.BOLD, 30));
		lblSecondPlace.setBounds(300, 340, 300, 36);
		getContentPane().add(lblSecondPlace);
		getContentPane().setComponentZOrder(lblSecondPlace, 0);
		
		lblThirdPlace = new JLabel("");
		lblThirdPlace.setForeground(Color.WHITE);
		lblThirdPlace.setFont(new Font("Tahoma", Font.BOLD, 30));
		lblThirdPlace.setBounds(300, 380, 300, 36);
		getContentPane().add(lblThirdPlace);
		getContentPane().setComponentZOrder(lblThirdPlace, 0);
		
		lblFourthPlace = new JLabel("");
		lblFourthPlace.setForeground(Color.WHITE);
		lblFourthPlace.setFont(new Font("Tahoma", Font.BOLD, 30));
		lblFourthPlace.setBounds(300, 420, 300, 36);
		getContentPane().add(lblFourthPlace);
		getContentPane().setComponentZOrder(lblFourthPlace, 0);
		
		lblFifthPlace = new JLabel("");
		lblFifthPlace.setForeground(Color.WHITE);
		lblFifthPlace.setFont(new Font("Tahoma", Font.BOLD, 30));
		lblFifthPlace.setBounds(300, 460, 300, 36);
		getContentPane().add(lblFifthPlace);
		getContentPane().setComponentZOrder(lblFifthPlace, 0);

	}

	public void start()
	{
		Thread thread = new Thread()
		{
			public void run()
			{
				animationLoop();
				System.out.println("run() complete");
			}
		};

		thread.start();
		System.out.println("main() complete");

	}	
	private void animationLoop() {

		universe = animation.getNextUniverse();
		universeLevel++;

		while (stop == false && universe != null) {

			sprites = universe.getSprites();
			player1 = universe.getPlayer1();
			background = universe.getBackground();
			centreOnPlayer = universe.centerOnPlayer();
			this.scale = universe.getScale();
			this.xCenter = universe.getXCenter();
			this.yCenter = universe.getYCenter();

			// main game loop
			while (stop == false && universe.isComplete() == false) {

				//adapted from http://www.java-gaming.org/index.php?topic=24220.0
				last_refresh_time = System.currentTimeMillis();
				next_refresh_time = current_time + minimum_delta_time;

				//sleep until the next refresh time
				while (current_time < next_refresh_time)
				{
					//allow other threads (i.e. the Swing thread) to do its work
					Thread.yield();

					try {
						Thread.sleep(1);
					}
					catch(Exception e) {    					
					} 

					//track current time
					current_time = System.currentTimeMillis();
				}

				//read input
				keyboard.poll();
				handleKeyboardInput();

				//UPDATE STATE
				updateTime();
				universe.update(keyboard, actual_delta_time);
				updateControls();

				//REFRESH
				this.repaint();
			}

			universe = animation.getNextUniverse();

		}

		System.out.println("animation complete");
		AudioPlayer.setStopAll(true);
		dispose();	

	}

	private void updateControls() {
		this.lblPoints.setText(String.valueOf(BeeSprite.getPoints()));
		
		//if 
		
		if (Highscores.getVisible()) {
			this.lblFirstPlace.setText(Highscores.getHighscore(0));
			this.lblSecondPlace.setText(Highscores.getHighscore(1));
			this.lblThirdPlace.setText(Highscores.getHighscore(2));
			this.lblFourthPlace.setText(Highscores.getHighscore(3));
			this.lblFifthPlace.setText(Highscores.getHighscore(4));
		} else {
			this.lblFirstPlace.setText("");
			this.lblSecondPlace.setText("");
			this.lblThirdPlace.setText("");
			this.lblFourthPlace.setText("");
			this.lblFifthPlace.setText("");
		}
		
		if (getUser) {
			username = JOptionPane.showInputDialog(panel, "What's your name?");
			getUser = false;
		}

	}
	
	public static String getUsername() {
		return username;
	}

	private void updateTime() {

		current_time = System.currentTimeMillis();
		actual_delta_time = (isPaused ? 0 : current_time - last_refresh_time);
		last_refresh_time = current_time;
		elapsed_time += actual_delta_time;

	}



	private void handleKeyboardInput() {

		if (keyboard.keyDown(112)) {
			scale *= 1.01;
		}
		if (keyboard.keyDown(113)) {
			scale /= 1.01;
		}
	}

	class DrawPanel extends JPanel {

		public void paintComponent(Graphics g)
		{	
			if (universe == null) {
				return;
			}

			if (player1 != null && centreOnPlayer) {
				xCenter = player1.getCenterX();
			}

			paintBackground(g, background);

			for (DisplayableSprite activeSprite : sprites) {
				DisplayableSprite sprite = activeSprite;
				if (sprite.getVisible()) {
					if (sprite.getImage() != null) {
						g.drawImage(sprite.getImage(), translateX(sprite.getMinX()), translateY(sprite.getMinY()), scaleX(sprite.getWidth()), scaleY(sprite.getHeight()), null);
					}
					else {
						g.setColor(Color.BLUE);
						g.fillRect(translateX(scale * (sprite.getMinX())), translateY(sprite.getMinY()), scaleX(sprite.getWidth()), scaleY(sprite.getHeight()));					
					}
				}

			}

		}
		
		private int translateX(double x) {
			return xpCenter + scaleX(x - xCenter);
		}
		
		private int scaleX(double x) {
			return (int) Math.round(scale * x);
		}
		private int translateY(double y) {
			return ypCenter + scaleY(y - yCenter);
		}		
		private int scaleY(double y) {
			return (int) Math.round(scale * y);
		}

		private void paintBackground(Graphics g, Background background) {

			if ((g == null) || (background == null)) {
				return;
			}
			
			//what tile covers the top-left corner?
			double xTopLeft = ( xCenter - (xpCenter / scale));
			double yTopLeft =  (yCenter - (ypCenter / scale)) ;
			
			int row = background.getRow((int)yTopLeft);
			int col = background.getCol((int)xTopLeft);
			Tile tile = null;

			boolean rowDrawn = false;
			boolean screenDrawn = false;
			while (screenDrawn == false) {
				while (rowDrawn == false) {
					tile = background.getTile(col, row);
					if (tile.getWidth() <= 0 || tile.getHeight() <= 0) {
						//no increase in width; will cause an infinite loop, so consider this screen to be done
						g.setColor(Color.GRAY);
						g.fillRect(0,0, SCREEN_WIDTH, SCREEN_HEIGHT);					
						rowDrawn = true;
						screenDrawn = true;						
					}
					else {
						Tile nextTile = background.getTile(col+1, row+1);
						int pwidth = translateX(nextTile.getMinX()) - translateX(tile.getMinX());
						int pheight = translateY(nextTile.getMinY()) - translateY(tile.getMinY());
						g.drawImage(tile.getImage(), translateX(tile.getMinX()), translateY(tile.getMinY()), pwidth, pheight, null);
					}					
					//does the RHE of this tile extend past the RHE of the visible area?
					if (translateX(tile.getMinX() + tile.getWidth()) > SCREEN_WIDTH || tile.isOutOfBounds()) {
						rowDrawn = true;
					}
					else {
						col++;
					}
				}
				//does the bottom edge of this tile extend past the bottom edge of the visible area?
				if (translateY(tile.getMinY() + tile.getHeight()) > SCREEN_HEIGHT || tile.isOutOfBounds()) {
					screenDrawn = true;
				}
				else {
					//TODO - should be passing in a double, as this represents a universe coordinate
					col = background.getCol((int)xTopLeft);
					row++;
					rowDrawn = false;
				}
			}
		}				
	}
	protected void this_windowClosing(WindowEvent e) {
		System.out.println("windowClosing()");
		stop = true;
		dispose();	
	}
}
