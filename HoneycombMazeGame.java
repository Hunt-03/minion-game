import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.image.BufferedImage;

public class HoneycombMazeGame extends JPanel implements KeyListener {
    private static final int SIZE = 25; // Reduced size of the maze
    private static final int CELL_SIZE = 32; // Size of each cell
    private static final char GUARD = 'G';
    private static final char EXIT = 'E';
    private static final char EMPTY = ' ';
    private static final char WALL = '#';
    private static final char PLAYER = 'P';
    private static final char POWER_UP_YELLOW = 'Y';
    private static final char POWER_UP_BLUE = 'B';
    private static final char OBSTACLE = 'O';
    private BufferedImage wallImage;
    private BufferedImage playerImage;
    private BufferedImage guardImage;
    private BufferedImage exitImage;
    private BufferedImage powerUpYellowImage;
    private BufferedImage powerUpBlueImage;
    private BufferedImage obstacleImage;
    private BufferedImage playerUp;
    private BufferedImage playerDown;
    private BufferedImage playerLeft;
    private BufferedImage playerRight;
    private BufferedImage background;

    private static final int ROWS = 20; // Adjusted number of rows
    private static final int COLS = 20; // Adjusted number of columns

    private static char[][] maze = new char[SIZE][SIZE];
    private static int playerX, playerY;
    private static List<Point> guards = new ArrayList<>();
    private static List<Point> powerUps = new ArrayList<>();
    private static List<Point> obstacles = new ArrayList<>();
    private boolean hasPowerUpYellow = false;
    private boolean hasPowerUpBlue = false;
    private boolean powerUpActive = false;
    private static final int powerUpDuration = 10000; // 10 seconds

    private boolean hitGuard = false;
    private boolean reachedExit = false;
    private int lastKeyPressed = KeyEvent.VK_DOWN;
    private JLabel messageLabel;
    private JLabel scoreLabel;

    private int score = 0;
    private static int highScore = 0;
    private static int currentLevel = 1;
    private static int maxLevel = 3; // Maximum number of levels

    public HoneycombMazeGame() {
        setPreferredSize(new Dimension(SIZE * CELL_SIZE, SIZE * CELL_SIZE)); // Set panel size
        addKeyListener(this);
        setFocusable(true);
        loadImages(); // Call loadImages() to load the images when the game starts
        initializeMaze();
        startGuardMovement();
        generatePowerUps();
        generateObstacles();
        messageLabel = new JLabel();
        messageLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(messageLabel);
        updateMessage("");
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scoreLabel.setForeground(Color.BLUE);
        add(scoreLabel);
        updateScore();
    }

    private void loadImages() {
        try {
            // Load images using the base path
            BufferedImage originalWallImage = ImageIO.read(new File("wall_image.png"));
            BufferedImage originalPlayerImage = ImageIO.read(new File("player_image.png"));

            // Calculate the new dimensions for the wall and player images
            int wallWidth = CELL_SIZE * 3; // Adjust as needed
            int wallHeight = CELL_SIZE * 3; // Adjust as needed
            int playerWidth = CELL_SIZE; // Adjust as needed
            int playerHeight = CELL_SIZE; // Adjust as needed

            // Resize wall and player images to match the new dimensions
            wallImage = resizeImage(originalWallImage, wallWidth, wallHeight);
            playerImage = resizeImage(originalPlayerImage, playerWidth, playerHeight);

            // Load other images with the same resizing logic
            guardImage = ImageIO.read(new File("guard_image.png"));
            exitImage = ImageIO.read(new File("exit_image.png"));
            powerUpYellowImage = ImageIO.read(new File("power_up_yellow_image.png"));
            powerUpBlueImage = ImageIO.read(new File("power_up_blue_image.png"));
            obstacleImage = ImageIO.read(new File("obstacle_image.png"));

            playerUp = ImageIO.read(new File("player_up.png"));
            playerDown = ImageIO.read(new File("player_down.png"));
            playerLeft = ImageIO.read(new File("player_left.png"));
            playerRight = ImageIO.read(new File("player_right.png"));

            background = ImageIO.read(new File("background.jpg")); // Load background image
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    private void initializeMaze() {
        // Initialize all cells to EMPTY
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                maze[i][j] = EMPTY;
            }
        }

        // Create outer boundary
        for (int i = 0; i < SIZE; i++) {
            maze[i][0] = WALL;
            maze[0][i] = WALL;
            maze[i][SIZE - 1] = WALL;
            maze[SIZE - 1][i] = WALL;
        }

        // Create inner walls
        for (int i = 2; i < SIZE - 2; i++) {
            for (int j = 2; j < SIZE - 2; j++) {
                if (i % 2 == 0 && j % 2 == 0) {
                    maze[i][j] = WALL;
                }
            }
        }

        // Create exit
        maze[SIZE - 2][SIZE - 2] = EXIT;

        // Update player's starting position
        playerX = 1;
        playerY = 1;
        maze[playerX][playerY] = PLAYER;

        // Place guards
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int guardX, guardY;
            do {
                guardX = random.nextInt(SIZE);
                guardY = random.nextInt(SIZE);
            } while (maze[guardX][guardY] != EMPTY);

            maze[guardX][guardY] = GUARD;
            guards.add(new Point(guardX, guardY));
        }
    }

    private void generatePowerUps() {
        Random random = new Random();
        int powerUpX = random.nextInt(SIZE);
        int powerUpY = random.nextInt(SIZE);
        while (maze[powerUpX][powerUpY] != EMPTY) {
            powerUpX = random.nextInt(SIZE);
            powerUpY = random.nextInt(SIZE);
        }
        maze[powerUpX][powerUpY] = POWER_UP_YELLOW;
        powerUps.add(new Point(powerUpX, powerUpY));

        powerUpX = random.nextInt(SIZE);
        powerUpY = random.nextInt(SIZE);
        while (maze[powerUpX][powerUpY] != EMPTY) {
            powerUpX = random.nextInt(SIZE);
            powerUpY = random.nextInt(SIZE);
        }
        maze[powerUpX][powerUpY] = POWER_UP_BLUE;
        powerUps.add(new Point(powerUpX, powerUpY));
    }

    private void generateObstacles() {
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            int obstacleX = random.nextInt(SIZE);
            int obstacleY = random.nextInt(SIZE);
            while (maze[obstacleX][obstacleY] != EMPTY) {
                obstacleX = random.nextInt(SIZE);
                obstacleY = random.nextInt(SIZE);
            }
            maze[obstacleX][obstacleY] = OBSTACLE;
            // Double the size of the obstacle
            obstacles.add(new Point(obstacleX, obstacleY));
            obstacles.add(new Point(obstacleX + 1, obstacleY));
            obstacles.add(new Point(obstacleX, obstacleY + 1));
            obstacles.add(new Point(obstacleX + 1, obstacleY + 1));
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background image
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

        // Draw maze elements...
        drawMazeElements(g);

        // Draw legend...
        drawLegend(g);
    }

    private void drawMazeElements(Graphics g) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int x = j * CELL_SIZE;
                int y = i * CELL_SIZE;
                switch (maze[i][j]) {
                    case WALL:
                        g.drawImage(wallImage, x, y, CELL_SIZE, CELL_SIZE, null);
                        break;
                    case PLAYER:
                        // Draw player based on movement direction
                        if (playerX == i && playerY == j) {
                            if (lastKeyPressed == KeyEvent.VK_UP) {
                                g.drawImage(playerUp, x, y, CELL_SIZE, CELL_SIZE, null);
                            } else if (lastKeyPressed == KeyEvent.VK_DOWN) {
                                g.drawImage(playerDown, x, y, CELL_SIZE, CELL_SIZE, null);
                            } else if (lastKeyPressed == KeyEvent.VK_LEFT) {
                                g.drawImage(playerLeft, x, y, CELL_SIZE, CELL_SIZE, null);
                            } else if (lastKeyPressed == KeyEvent.VK_RIGHT) {
                                g.drawImage(playerRight, x, y, CELL_SIZE, CELL_SIZE, null);
                            } else {
                                // Default image when no movement
                                g.drawImage(playerImage, x, y, CELL_SIZE, CELL_SIZE, null);
                            }
                        }
                        break;
                    case GUARD:
                        g.drawImage(guardImage, x, y, CELL_SIZE, CELL_SIZE, null);
                        break;
                    case EXIT:
                        g.drawImage(exitImage, x, y, CELL_SIZE, CELL_SIZE, null);
                        break;
                    case POWER_UP_YELLOW:
                        g.drawImage(powerUpYellowImage, x, y, CELL_SIZE, CELL_SIZE, null);
                        break;
                    case POWER_UP_BLUE:
                        g.drawImage(powerUpBlueImage, x, y, CELL_SIZE, CELL_SIZE, null);
                        break;
                    case OBSTACLE:
                        g.drawImage(obstacleImage, x, y, CELL_SIZE, CELL_SIZE * 2, null); // Draw obstacle
                                                                                          // with
                        // increased size
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void drawLegend(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Legend:", SIZE * CELL_SIZE + 10, 20);
        g.setColor(Color.BLUE);
        g.fillOval(SIZE * CELL_SIZE + 10, 30, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.BLACK);
        g.drawString("- Player", SIZE * CELL_SIZE + 30, 45);
        g.setColor(Color.RED);
        g.fillOval(SIZE * CELL_SIZE + 10, 60, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.BLACK);
        g.drawString("- Guard", SIZE * CELL_SIZE + 30, 75);
        g.setColor(Color.GREEN);
        g.fillRect(SIZE * CELL_SIZE + 10, 90, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.BLACK);
        g.drawString("- Exit", SIZE * CELL_SIZE + 30, 105);
        g.setColor(Color.YELLOW);
        g.fillRect(SIZE * CELL_SIZE + 10, 12, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.BLACK);
        g.drawString("- Power-Up (Yellow)", SIZE * CELL_SIZE + 30, 13);
        g.setColor(Color.CYAN);
        g.fillRect(SIZE * CELL_SIZE + 10, 150, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.BLACK);
        g.drawString("- Power-Up (Blue)", SIZE * CELL_SIZE + 30, 16);
        g.setColor(Color.BLACK);
        g.fillRect(SIZE * CELL_SIZE + 10, 180, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.GREEN);
        g.fillOval(SIZE * CELL_SIZE + 10 + CELL_SIZE / 4, 180 + CELL_SIZE / 4, CELL_SIZE / 2, CELL_SIZE / 2);
        g.setColor(Color.BLACK);
        g.drawString("- Obstacle", SIZE * CELL_SIZE + 30, 195);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Honeycomb Maze");
            HoneycombMazeGame mazePanel = new HoneycombMazeGame();
            frame.add(mazePanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create menu bar
            JMenuBar menuBar = new JMenuBar();
            frame.setJMenuBar(menuBar);

            // Create file menu
            JMenu fileMenu = new JMenu("File");
            menuBar.add(fileMenu);

            // Create start menu item
            JMenuItem startMenuItem = new JMenuItem("Start");
            startMenuItem.addActionListener(e -> mazePanel.restartGame());
            fileMenu.add(startMenuItem);

            // Create high score menu item
            JMenuItem highScoreMenuItem = new JMenuItem("High Score");
            highScoreMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(frame, "High Score: " + highScore,
                    "High Score", JOptionPane.INFORMATION_MESSAGE));
            fileMenu.add(highScoreMenuItem);

            // Create exit menu item
            JMenuItem exitMenuItem = new JMenuItem("Exit");
            exitMenuItem.addActionListener(e -> System.exit(0));
            fileMenu.add(exitMenuItem);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!hitGuard && !reachedExit) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_UP && playerX > 0 && canMoveTo(playerX - 1, playerY)) {
                movePlayer(playerX - 1, playerY);
                lastKeyPressed = KeyEvent.VK_UP;
            } else if (key == KeyEvent.VK_DOWN && playerX < SIZE - 1 && canMoveTo(playerX + 1, playerY)) {
                movePlayer(playerX + 1, playerY);
                lastKeyPressed = KeyEvent.VK_DOWN;
            } else if (key == KeyEvent.VK_LEFT && playerY > 0 && canMoveTo(playerX, playerY - 1)) {
                movePlayer(playerX, playerY - 1);
                lastKeyPressed = KeyEvent.VK_LEFT;
            } else if (key == KeyEvent.VK_RIGHT && playerY < SIZE - 1 && canMoveTo(playerX, playerY + 1)) {
                movePlayer(playerX, playerY + 1);
                lastKeyPressed = KeyEvent.VK_RIGHT;
            } else if (key == KeyEvent.VK_UP && key == KeyEvent.VK_LEFT && playerX > 0 && playerY > 0
                    && canMoveTo(playerX - 1, playerY - 1)) {
                movePlayer(playerX - 1, playerY - 1);
                lastKeyPressed = KeyEvent.VK_UP;
            } else if (key == KeyEvent.VK_UP && key == KeyEvent.VK_RIGHT && playerX > 0 && playerY < SIZE - 1
                    && canMoveTo(playerX - 1, playerY + 1)) {
                movePlayer(playerX - 1, playerY + 1);
                lastKeyPressed = KeyEvent.VK_UP;
            } else if (key == KeyEvent.VK_DOWN && key == KeyEvent.VK_LEFT && playerX < SIZE - 1 && playerY > 0
                    && canMoveTo(playerX + 1, playerY - 1)) {
                movePlayer(playerX + 1, playerY - 1);
                lastKeyPressed = KeyEvent.VK_DOWN;
            } else if (key == KeyEvent.VK_DOWN && key == KeyEvent.VK_RIGHT && playerX < SIZE - 1 && playerY < SIZE - 1
                    && canMoveTo(playerX + 1, playerY + 1)) {
                movePlayer(playerX + 1, playerY + 1);
                lastKeyPressed = KeyEvent.VK_DOWN;
            }
            if (hitGuard) {
                restartGame();
            } else if (reachedExit) {
                if (score > highScore) {
                    highScore = score;
                }
                JOptionPane.showMessageDialog(this, "Congratulations! You reached the exit!\nYour Score: " + score,
                        "Game Over", JOptionPane.INFORMATION_MESSAGE);
                if (currentLevel < maxLevel) {
                    currentLevel++;
                    restartGame();
                } else {
                    System.exit(0);
                }
            }
            repaint();
        }
    }

    private boolean canMoveTo(int x, int y) {
        if (maze[x][y] == WALL) {
            return hasPowerUpYellow;
        }
        return true;
    }

    private void movePlayer(int newX, int newY) {
        // Check if the new position is an obstacle
        if (maze[newX][newY] == OBSTACLE) {
            // Player cannot move through obstacles
            return;
        }

        if (maze[newX][newY] == GUARD) {
            if (hasPowerUpBlue) {
                maze[newX][newY] = EMPTY;
            } else {
                hitGuard = true;
                updateMessage("You hit a guard! Game Over.");
                return;
            }
        }
        if (maze[newX][newY] == EXIT) {
            reachedExit = true;
            return;
        }
        if (maze[newX][newY] == POWER_UP_YELLOW) {
            updateMessage("You collected a power-up! (Yellow)");
            hasPowerUpYellow = true;
        } else if (maze[newX][newY] == POWER_UP_BLUE) {
            updateMessage("You collected a power-up! (Blue)");
            hasPowerUpBlue = true;
        }
        maze[playerX][playerY] = EMPTY;
        playerX = newX;
        playerY = newY;
        maze[playerX][playerY] = PLAYER;
        score++;
        updateScore();
        repaint();
    }

    private void startGuardMovement() {
        new Thread(() -> {
            Random random = new Random();
            while (!hitGuard && !reachedExit) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (Point guard : guards) {
                    int direction = random.nextInt(8);
                    int newX = guard.x;
                    int newY = guard.y;
                    switch (direction) {
                        case 0: // Up
                            if (guard.x > 0 && maze[guard.x - 1][guard.y] != WALL
                                    && maze[guard.x - 1][guard.y] != POWER_UP_YELLOW
                                    && maze[guard.x - 1][guard.y] != POWER_UP_BLUE
                                    && maze[guard.x - 1][guard.y] != OBSTACLE) {
                                newX = guard.x - 1;
                            }
                            break;
                        case 1: // Down
                            if (guard.x < SIZE - 1 && maze[guard.x + 1][guard.y] != WALL
                                    && maze[guard.x + 1][guard.y] != POWER_UP_YELLOW
                                    && maze[guard.x + 1][guard.y] != POWER_UP_BLUE
                                    && maze[guard.x + 1][guard.y] != OBSTACLE) {
                                newX = guard.x + 1;
                            }
                            break;
                        case 2: // Left
                            if (guard.y > 0 && maze[guard.x][guard.y - 1] != WALL
                                    && maze[guard.x][guard.y - 1] != POWER_UP_YELLOW
                                    && maze[guard.x][guard.y - 1] != POWER_UP_BLUE
                                    && maze[guard.x][guard.y - 1] != OBSTACLE) {
                                newY = guard.y - 1;
                            }
                            break;
                        case 3: // Right
                            if (guard.y < SIZE - 1 && maze[guard.x][guard.y + 1] != WALL
                                    && maze[guard.x][guard.y + 1] != POWER_UP_YELLOW
                                    && maze[guard.x][guard.y + 1] != POWER_UP_BLUE
                                    && maze[guard.x][guard.y + 1] != OBSTACLE) {
                                newY = guard.y + 1;
                            }
                            break;
                        case 4: // Up-Left
                            if (guard.x > 0 && guard.y > 0 && maze[guard.x - 1][guard.y - 1] != WALL
                                    && maze[guard.x - 1][guard.y - 1] != POWER_UP_YELLOW
                                    && maze[guard.x - 1][guard.y - 1] != POWER_UP_BLUE
                                    && maze[guard.x - 1][guard.y - 1] != OBSTACLE) {
                                newX = guard.x - 1;
                                newY = guard.y - 1;
                            }
                            break;
                        case 5: // Up-Right
                            if (guard.x > 0 && guard.y < SIZE - 1 && maze[guard.x - 1][guard.y + 1] != WALL
                                    && maze[guard.x - 1][guard.y + 1] != POWER_UP_YELLOW
                                    && maze[guard.x - 1][guard.y + 1] != POWER_UP_BLUE
                                    && maze[guard.x - 1][guard.y + 1] != OBSTACLE) {
                                newX = guard.x - 1;
                                newY = guard.y + 1;
                            }
                            break;
                        case 6: // Down-Left
                            if (guard.x < SIZE - 1 && guard.y > 0 && maze[guard.x + 1][guard.y - 1] != WALL
                                    && maze[guard.x + 1][guard.y - 1] != POWER_UP_YELLOW
                                    && maze[guard.x + 1][guard.y - 1] != POWER_UP_BLUE
                                    && maze[guard.x + 1][guard.y - 1] != OBSTACLE) {
                                newX = guard.x + 1;
                                newY = guard.y - 1;
                            }
                            break;
                        case 7: // Down-Right
                            if (guard.x < SIZE - 1 && guard.y < SIZE - 1 && maze[guard.x + 1][guard.y + 1] != WALL
                                    && maze[guard.x + 1][guard.y + 1] != POWER_UP_YELLOW
                                    && maze[guard.x + 1][guard.y + 1] != POWER_UP_BLUE
                                    && maze[guard.x + 1][guard.y + 1] != OBSTACLE) {
                                newX = guard.x + 1;
                                newY = guard.y + 1;
                            }
                            break;
                    }
                    if (newX != guard.x || newY != guard.y) {
                        maze[guard.x][guard.y] = EMPTY;
                        guard.x = newX;
                        guard.y = newY;
                        maze[guard.x][guard.y] = GUARD;
                    }
                }
                repaint();
            }
        }).start();
    }

    private void restartGame() {
        maze = new char[SIZE][SIZE];
        guards.clear();
        obstacles.clear();
        powerUps.clear();
        hitGuard = false;
        reachedExit = false;
        hasPowerUpYellow = false;
        hasPowerUpBlue = false;
        powerUpActive = false;
        score = 0;
        playerX = 0;
        playerY = 1;
        updateMessage("");
        updateScore();
        initializeMaze();
        startGuardMovement();
        generatePowerUps();
        generateObstacles();
        repaint();
    }

    private void updateMessage(String msg) {
        messageLabel.setText(msg);
    }

    private void updateScore() {
        scoreLabel.setText("Score: " + score);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
