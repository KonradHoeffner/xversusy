import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import jline.ConsoleReader;

public class Main
{
	private static final char	ASTEROID	= '.';
	private static final char REPAIR_KIT	= 'H';
	private static final char POINT = 'P';

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	static final int ASTEROID_START_RATE = 10;
	static int asteroidRate = ASTEROID_START_RATE;
	static final int REPAIR_KIT_SPAWN_RATE = 2000;
	static final int POINT_SPAWN_RATE = 250;

	private static final long	SLEEP_MS_MIN	= 60;

	private static final int	MAX_HITS	= 10;

	private static final long	SLOWNESS_DECREASE_PER_HIT	= 10;

	static int WIN_POINTS = 10;
	//	private static final String	REPAIR_KIT	= ANSI_GREEN+"@"+ANSI_RESET;


	static int SLEEP_MS_START = 200;
	static int sleepMs	= SLEEP_MS_START;
	static int COLUMNS = 80;
	static int ROWS = 30;
	static Random r = new Random();

	static LinkedList<String> screen = new LinkedList<>();

	static final int JUST_REPAIRED_FRAMES = 10;
	static final int JUST_HIT_FRAMES = 3;
	private static final int	EXTENSION_RATE	= 3;

	static int justRepairedFramesLeft = 0;
	static int justHitFramesLeft = 0;
	static String hitColor = "";

	static boolean finished = false;

	static class Ship
	{
		public final String name;
		public final String color;
		final int startX;
		final int startY;
		public int x;
		public int y;
		public char c;
		public int getX() {return x;}
		public int getY() {return y;}
		char up,down,left,right;
		public int hits = 0;
		public int	points = 0;
		public Ship(String name, String color, int x, int y, char c, char up,char down, char left, char right)
		{this.name=name;this.color=color;this.startX=x;this.startY=y;this.x=x;this.y=y;this.c=c;this.up=up;this.down=down;this.left=left;this.right=right;}

		public void reset()
		{
			x=startX;
			y=startY;
			points = 0;
			hits = 0;
		}
	}

	static List<Ship> ships = Arrays.asList(
			new Ship("USS Simonia",ANSI_BLUE,40,ROWS-1,'X','w','s','a','d'),
			new Ship("Kondor 2000",ANSI_RED,60,ROWS-1,'Y','i','k','j','l'));

	static void initScreen()
	{
		screen.clear();
		for (int y = 0; y < ROWS; y++)
		{
			screen.add("                                                                                ");
			//			screen.add(randomLine());
		}
	}

	static String randomLine()
	{
		StringBuilder sb = new StringBuilder();
		for(int x=0;x<COLUMNS;x++)
		{
			char letter;
			letter = r.nextInt(asteroidRate)==0?ASTEROID:' ';
			if(r.nextInt(REPAIR_KIT_SPAWN_RATE)==0) {letter=REPAIR_KIT;}
			if(r.nextInt(POINT_SPAWN_RATE)==0) {letter=POINT;}
			sb.append(letter);
		}
		return sb.toString();
	}

	static String replaceChar(String s, int pos, char c)
	{
		String replaced = s.substring(0, pos)+c;
		if(pos<s.length()-1) replaced += s.substring(pos+1);
		return replaced;
	}

	static boolean draw()
	{
		System.out.println("\033[H\033[2J");
		ships.forEach(ship-> System.out.format(ship.color+ship.name+" %3d hits %2d points "+ANSI_RESET,ship.hits,ship.points));
		System.out.print("asteroid rate "+asteroidRate);
		System.out.print(" slowness "+sleepMs);
		System.out.println();
		//		System.out.print(ANSI_);
		//		for(String line: screen)
		Set<Integer> shipYs = ships.stream().map(Ship::getY).collect(Collectors.toSet());
		for (int y = 0; y < ROWS; y++)
		{
			if(shipYs.contains(y))
			{
				for(int x=0;x<COLUMNS;x++)
				{
					String c = String.valueOf(screen.get(y).charAt(x));
					for(Ship ship: ships)
					{
						if(ship.y==y&&ship.x==x)
						{
							if(c.charAt(0)==POINT)
							{
								ship.points ++;
								String replaced = replaceChar(screen.get(y), x, ' ');
								screen.remove(y);
								screen.add(y, replaced);

								if(ship.points>=WIN_POINTS)
								{
									System.out.println(ship.name+" has won.");
									return true;
								}
								justRepairedFramesLeft=JUST_REPAIRED_FRAMES;
							}
							if(c.charAt(0)==REPAIR_KIT)
							{
								ship.hits=0;
								justRepairedFramesLeft=JUST_REPAIRED_FRAMES;
							}
							if(c.charAt(0)==ASTEROID)
							{
								if(sleepMs>SLEEP_MS_MIN)
								{
									sleepMs-=SLOWNESS_DECREASE_PER_HIT;
									asteroidRate=ASTEROID_START_RATE*SLEEP_MS_START/sleepMs;
								}
								ship.hits++;
								hitColor = ship.color;
								justHitFramesLeft= JUST_HIT_FRAMES;
								if(ship.hits>=MAX_HITS)
								{
									System.out.println(ship.name+" has lost.");
									return true;
								}
							}
							c = ship.color+ship.c+ANSI_RESET;
						}
					}
					System.out.print(c);
				}
				System.out.println();
			}
			else
			{
				if(justRepairedFramesLeft>0)
				{
					System.out.println(ANSI_GREEN+screen.get(y)+ANSI_RESET);
				}
				else
					if(justHitFramesLeft>0)
					{
						System.out.println(hitColor+screen.get(y)+ANSI_RED);
					}
					else
					{
						System.out.println(screen.get(y));
					}
			}
		}
		if(justRepairedFramesLeft>0) justRepairedFramesLeft--;
		if(justHitFramesLeft>0) justHitFramesLeft--;
		screen.removeLast();
		String line = randomLine();
		String firstLine = screen.getFirst();
		// vertically
		for(int x=1;x<COLUMNS-1;x++)
		{
			if(firstLine.charAt(x-1)==ASTEROID&&r.nextInt(EXTENSION_RATE)==0) {line=line.substring(0, x)+ASTEROID+line.substring(x+1);}
			if(firstLine.charAt(x+1)==ASTEROID&&r.nextInt(EXTENSION_RATE)==0) {line=line.substring(0, x)+ASTEROID+line.substring(x+1);}
			if(firstLine.charAt(x)==ASTEROID&&r.nextInt(EXTENSION_RATE)==0) {line=line.substring(0, x)+ASTEROID+line.substring(x+1);}

			if(firstLine.charAt(x-1)==' '&&r.nextInt(EXTENSION_RATE)==0) {line=line.substring(0, x)+' '+line.substring(x+1);}
			if(firstLine.charAt(x+1)==' '&&r.nextInt(EXTENSION_RATE)==0) {line=line.substring(0, x)+' '+line.substring(x+1);}
			if(firstLine.charAt(x)==' '&&r.nextInt(EXTENSION_RATE)==0) {line=line.substring(0, x)+' '+line.substring(x+1);}

			if(firstLine.charAt(x)==' '&&r.nextBoolean()) {line=line.substring(0, x)+' '+line.substring(x+1);}
		}
		// horizontally
		for(int x=1;x<COLUMNS-2;x++)
		{
			if(line.charAt(x)==ASTEROID&&r.nextInt(EXTENSION_RATE)==0)
			{line=line.substring(0, x)+ASTEROID+ASTEROID+line.substring(x+2);}
		}
		screen.addFirst(line);
		return false;
	}

	static void keyboardThread()
	{
		ConsoleReader in;
		try
		{
			in = new ConsoleReader();
			while(!finished)
			{
				char c = (char)in.readVirtualKey();
				for(Ship ship: ships)
				{
					if(c==ship.up&&ship.y>0) {ship.y--;}
					if(c==ship.down&&ship.y<ROWS-1) {ship.y++;}
					if(c==ship.left&&ship.x>0) {ship.x--;}
					if(c==ship.right&&ship.x<COLUMNS-1) {ship.x++;}
				}
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws InterruptedException
	{
		while(true)
		{
			finished = false;
			sleepMs = SLEEP_MS_START;
			asteroidRate = ASTEROID_START_RATE;
			new Thread(Main::keyboardThread).start();
			initScreen();

			while(!finished)
			{
				finished = draw();
				int distance = Math.abs(ships.get(0).y-ships.get(1).y)+Math.abs(ships.get(0).x-ships.get(1).x);
				distance = Math.max(5, distance);
				sleepMs = SLEEP_MS_START*10/distance;
				Thread.sleep(sleepMs);
			}
			ships.forEach(Ship::reset);
			Thread.sleep(5000);
		}
	}

}