import java.awt.Image; 
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

public class BeeSprite implements DisplayableSprite, MovableSprite {

	private static final int FRAMES = 9;
	private static int framesPerSecond = 20;
	private static int millisecondsPerFrame = 1000 / framesPerSecond;
	private static Image[] imageLeft = null;
	private static Image[] imageRight = null;
	private long elapsedTime = 0;
	private double centerX = 0;
	private double centerY = 0;
	private double deltaX = 0;
	private double deltaY = 0;
	private double height = 50;
	private double width = 50;
	private int direction = 2; //1 = left; 2 = right
	private static int points = 0;
	public static boolean gameOver = false;
	private boolean displayScores = false;
	private final double VELOCITY = 200;
	
	public BeeSprite() {
		super();

		if (imageLeft == null) {
			try {				
				imageLeft = new Image[FRAMES];
				for (int i = 0; i < FRAMES; i++) {
					String path = String.format("res/bee/bee_left-%d.png", i);
					imageLeft[i] = ImageIO.read(new File(path));
				}
				imageRight = new Image[FRAMES];
				for (int i = 0; i < FRAMES; i++) {
					String path = String.format("res/bee/bee_right-%d.png", i);
					imageRight[i] = ImageIO.read(new File(path));
				}
			}
			catch (IOException e) {
				System.out.println(e.toString());
			}	
		}
		
	}
	
	public void setCenterX(double centerX) {
		this.centerX = centerX;	
	}

	public void setCenterY(double centerY) {
		this.centerY = centerY;	
	}
	
	public double getHeight() {
		return height;
	}

	public double getWidth() {
		return width;
	}	

	public void moveX(double pixelsPerSecond) {
		
	}

	public void moveY(double pixelsPerSecond) {
		
	}

	public void stop() {
		
	}

	public Image getImage() {		
		Image output = null;
		long frame = elapsedTime / millisecondsPerFrame;
		int index = (int) frame % FRAMES;
				
		if (deltaX < 0) {
			this.direction = 1;
		} else if (deltaX > 0) {
			this.direction = 2;
		}
		
		if (direction == 1) {
			output = imageLeft[index];
		} else if (direction == 2) {
			output = imageRight[index];
		} 
		
		return output;
		
	}

	public boolean getVisible() {
		return true;
	}

	public double getMinX() {
		return centerX - (width / 2);
	}

	public double getMaxX() {
		return centerX + (width / 2);
	}

	public double getMinY() {
		return centerY - (height / 2);
	}

	public double getMaxY() {
		return centerY + (height / 2);
	}

	public double getCenterX() {
		return centerX;
	}

	public double getCenterY() {
		return centerY;
	}

	public boolean getDispose() {
		return false;
	}
	
	public static int getPoints() {
		return points;
	}
	
	public static void addPoint() {
		points++;
	}

	public void update(Universe universe, KeyboardInput keyboard, long actual_delta_time) {
		
		elapsedTime += actual_delta_time;
		
		double velocityX = 0;
		double velocityY = 0;
		
		//LEFT	
		if (keyboard.keyDown(37)) {
			velocityX = -VELOCITY;
		}
		//UP
		if (keyboard.keyDown(38)) {
			velocityY = -VELOCITY;			
		}
		// RIGHT
		if (keyboard.keyDown(39)) {
			velocityX = VELOCITY;
		}
		// DOWN
		if (keyboard.keyDown(40)) {
			velocityY = VELOCITY;			
		}
		
		// Displays start game title
		if (elapsedTime < 3000 && centerX == 0) {
			StartGameTitleSprite.setVisible(true);
		} else {
			StartGameTitleSprite.setVisible(false);
		}
		
		// Actual game play
		if (gameOver == false) {
			
			GameOverTitleSprite.setVisible(false);
			HighScoreTitleSprite.setVisible(false);
			Highscores.setVisible(false);
			PressRTitleSprite.setVisible(false);
			displayScores = false;
		
			deltaX = velocityX * actual_delta_time * 0.001;
			deltaY = velocityY * actual_delta_time * 0.001;	
			
			if (checkBarrierCollision(universe, deltaX, 0) == false) {
				centerX += deltaX;
			}
			
			if (checkBarrierCollision(universe, 0, deltaY) == false) {
				centerY += deltaY;
			}
			
			if (checkWaspCollision(universe, 0, deltaY) == true) {
				gameOver = true;
			}
			
			WaspSprite.setPlayerX(centerX);
			WaspSprite.setPlayerY(centerY);
		
		} else {
			
			// End game: game over title then scores displayed after any key is pressed
			GameOverTitleSprite.setVisible(true);
			
			if (displayScores) {
				if (Highscores.getVisible() == false) {
					Highscores.addNewHighscore(AnimationFrame.getUsername(), getPoints());
				}
				GameOverTitleSprite.setVisible(false);
				Highscores.setVisible(true);
				PressRTitleSprite.setVisible(true);
				HighScoreTitleSprite.setVisible(true);
			} else {
				if (keyboard.anyKeyDown()) {
					displayScores = true;
					AnimationFrame.getUser = true;
				}
			}

		}
			
		// if 'r' is pressed at anytime, game play restarts
		if (keyboard.keyDown(82)) {				
			gameOver = false;
			points = 0;
			centerX = 0;
			centerY = 0;
		}
		
	}		
	
	public boolean checkWaspCollision(Universe sprites, double deltaX, double deltaY) {
		
		boolean colliding = false;
		
		for (DisplayableSprite sprite : sprites.getSprites()) {
					
				if (sprite instanceof WaspSprite) {
					if (CollisionDetection.overlaps(this.getMinX() + deltaX, this.getMinY() + deltaY, 
							this.getMaxX()  + deltaX, this.getMaxY() + deltaY, 
							sprite.getMinX(),sprite.getMinY(), 
							sprite.getMaxX(), sprite.getMaxY())) {
						colliding = true;
						break;					
					}
				}
		}
		return colliding;		
	}
	
	public boolean checkBarrierCollision(Universe sprites, double deltaX, double deltaY) {
		
		boolean colliding = false;
		
		for (DisplayableSprite sprite : sprites.getSprites()) {
			
			if (sprite instanceof BarrierSprite) {
				if (CollisionDetection.overlaps(this.getMinX() + deltaX, this.getMinY() + deltaY, 
						this.getMaxX()  + deltaX, this.getMaxY() + deltaY, 
						sprite.getMinX(),sprite.getMinY(), 
						sprite.getMaxX(), sprite.getMaxY())) {
					colliding = true;
					break;
				}
			}
		}
		return colliding;		
	}
		
}

