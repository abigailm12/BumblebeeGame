import java.util.ArrayList;

public interface Universe {

	//STATIC VARIABLES
	
	//INSTANCE VARIABLES

	public double getScale();


	public double getXCenter();
	public double getYCenter();
	
	public void setXCenter(double xCenter);
	public void setYCenter(double yCenter);
	
	public boolean isComplete();
	public void setComplete(boolean complete);
	
	public Background getBackground();
	public DisplayableSprite getPlayer1();
	public DisplayableSprite getFlower();
	public boolean centerOnPlayer();
	public boolean gameOver();

	public ArrayList<DisplayableSprite> getSprites();	
	

	public void update(KeyboardInput keyboard, long actual_delta_time);


    
	
}

