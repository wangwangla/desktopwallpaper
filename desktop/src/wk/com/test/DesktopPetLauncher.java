package wk.com.test;

import static com.sun.jna.platform.win32.WinUser.GWL_EXSTYLE;
import static com.sun.jna.platform.win32.WinUser.SWP_FRAMECHANGED;
import static com.sun.jna.platform.win32.WinUser.SWP_NOMOVE;
import static com.sun.jna.platform.win32.WinUser.SWP_NOSIZE;
import static org.lwjgl.system.windows.User32.SWP_NOZORDER;
import static org.lwjgl.system.windows.User32.WS_EX_CLIENTEDGE;
import static org.lwjgl.system.windows.User32.WS_EX_DLGMODALFRAME;
import static org.lwjgl.system.windows.User32.WS_EX_STATICEDGE;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;

import org.lwjgl.glfw.GLFW;

import java.util.Scanner;

import kw.test.DynamicUtils;

public class DesktopPetLauncher {
    private Lwjgl3Application app;
    public static void main(String[] args) {
        DesktopPetLauncher launcher = new DesktopPetLauncher();
        launcher.run();
    }


    public long run(){

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        // Configure FPS
        config.setForegroundFPS(30);
        config.setIdleFPS(30);
        // Configure window layout
        config.setDecorated(false);
        config.setResizable(false);

        Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        int screenWidth = displayMode.width;



        int gameWindowSize = Math.max(620, (int) (screenWidth * 0.38f));
        config.setWindowedMode(gameWindowSize, gameWindowSize);
        config.setWindowPosition(0, 100);
        // Configure window title
        final String TITLE = "Gomoku";
        config.useOpenGL3(true, 3, 2);
        config.setTitle(TITLE);
        config.setInitialVisible(true);
        config.setTransparentFramebuffer(false);

        config.setInitialBackgroundColor(new Color(0, 0, 0, 0));

        // Instantiate the App
        this.app = new Lwjgl3Application();

        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void created(Lwjgl3Window window) {
                System.out.println("Window created!");
// ---- 关键：延迟 1 帧再绑定 (否则 handle 为 0) ----
                Gdx.app.postRunnable(() -> {
                    long h = window.getWindowHandle();
                    //置顶
                    GLFW.glfwSetWindowAttrib(h, GLFW.GLFW_FLOATING, GLFW.GLFW_TRUE);
                });
            }
        });
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                if (sc.nextLine().equals("EXIT")) {
                    DynamicUtils.destroyWallpaper(app.getWindowHandle());
                    System.out.println("Wallpaper cleaned.");
                    System.exit(0);
                }
            }
        }).start();
        app.init(new GomokuGame(), config);
        return app.getWindowHandle();
    }

    /** A self-contained 15 x 15 Gomoku game. */
    private static class GomokuGame extends ApplicationAdapter {
        private static final int SIZE = 15;
        private static final int EMPTY = 0;
        private static final int BLACK = 1;
        private static final int WHITE = 2;
        private static final float WORLD_SIZE = 720f;
        private static final float BOARD_LEFT = 65f;
        private static final float BOARD_BOTTOM = 75f;
        private static final float GRID = 42f;

        private final int[][] board = new int[SIZE][SIZE];
        private final int[] historyRows = new int[SIZE * SIZE];
        private final int[] historyCols = new int[SIZE * SIZE];
        private OrthographicCamera camera;
        private FitViewport viewport;
        private ShapeRenderer shapes;
        private SpriteBatch batch;
        private BitmapFont font;
        private int currentPlayer = BLACK;
        private int winner = EMPTY;
        private int moveCount;

        @Override
        public void create() {
            camera = new OrthographicCamera();
            viewport = new FitViewport(WORLD_SIZE, WORLD_SIZE, camera);
            camera.position.set(WORLD_SIZE / 2f, WORLD_SIZE / 2f, 0);
            camera.update();
            shapes = new ShapeRenderer();
            batch = new SpriteBatch();
            font = new BitmapFont();
            font.getData().setScale(1.45f);

            Gdx.input.setInputProcessor(new InputAdapter() {
                @Override
                public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                    if (button == Input.Buttons.LEFT) placeAt(screenX, screenY);
                    return true;
                }

                @Override
                public boolean keyDown(int keycode) {
                    if (keycode == Input.Keys.R) reset();
                    if (keycode == Input.Keys.U || keycode == Input.Keys.BACKSPACE) undo();
                    if (keycode == Input.Keys.ESCAPE) Gdx.app.exit();
                    return true;
                }
            });
        }

        private void placeAt(int screenX, int screenY) {
            if (winner != EMPTY) return;
            Vector3 point = viewport.unproject(new Vector3(screenX, screenY, 0));
            int col = Math.round((point.x - BOARD_LEFT) / GRID);
            int row = Math.round((point.y - BOARD_BOTTOM) / GRID);
            if (row < 0 || row >= SIZE || col < 0 || col >= SIZE || board[row][col] != EMPTY) return;

            board[row][col] = currentPlayer;
            historyRows[moveCount] = row;
            historyCols[moveCount] = col;
            moveCount++;
            if (hasFive(row, col, currentPlayer)) {
                winner = currentPlayer;
            } else if (moveCount < SIZE * SIZE) {
                currentPlayer = currentPlayer == BLACK ? WHITE : BLACK;
            }
        }

        private boolean hasFive(int row, int col, int player) {
            int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
            for (int[] direction : directions) {
                int count = 1 + count(row, col, direction[0], direction[1], player)
                        + count(row, col, -direction[0], -direction[1], player);
                if (count >= 5) return true;
            }
            return false;
        }

        private int count(int row, int col, int dr, int dc, int player) {
            int result = 0;
            row += dr;
            col += dc;
            while (row >= 0 && row < SIZE && col >= 0 && col < SIZE && board[row][col] == player) {
                result++;
                row += dr;
                col += dc;
            }
            return result;
        }

        private void undo() {
            if (moveCount == 0) return;
            moveCount--;
            board[historyRows[moveCount]][historyCols[moveCount]] = EMPTY;
            currentPlayer = moveCount % 2 == 0 ? BLACK : WHITE;
            winner = EMPTY;
        }

        private void reset() {
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) board[row][col] = EMPTY;
            }
            currentPlayer = BLACK;
            winner = EMPTY;
            moveCount = 0;
        }

        @Override
        public void render() {
            Gdx.gl.glClearColor(0.08f, 0.09f, 0.11f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            camera.update();
            shapes.setProjectionMatrix(camera.combined);

            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(Color.valueOf("D5A85A"));
            shapes.rect(35, 45, 650, 650);
            shapes.end();

            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(Color.valueOf("553819"));
            for (int i = 0; i < SIZE; i++) {
                float offset = i * GRID;
                shapes.line(BOARD_LEFT, BOARD_BOTTOM + offset,
                        BOARD_LEFT + (SIZE - 1) * GRID, BOARD_BOTTOM + offset);
                shapes.line(BOARD_LEFT + offset, BOARD_BOTTOM,
                        BOARD_LEFT + offset, BOARD_BOTTOM + (SIZE - 1) * GRID);
            }
            shapes.end();

            shapes.begin(ShapeRenderer.ShapeType.Filled);
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if (board[row][col] == EMPTY) continue;
                    shapes.setColor(board[row][col] == BLACK ? Color.valueOf("151515") : Color.valueOf("F4F1E8"));
                    shapes.circle(BOARD_LEFT + col * GRID, BOARD_BOTTOM + row * GRID, 17f, 32);
                }
            }
            if (moveCount > 0) {
                shapes.setColor(Color.RED);
                shapes.circle(BOARD_LEFT + historyCols[moveCount - 1] * GRID,
                        BOARD_BOTTOM + historyRows[moveCount - 1] * GRID, 4f, 16);
            }
            shapes.end();

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            font.setColor(Color.WHITE);
            String status = winner == BLACK ? "BLACK WINS!" : winner == WHITE ? "WHITE WINS!"
                    : moveCount == SIZE * SIZE ? "DRAW" : (currentPlayer == BLACK ? "BLACK'S TURN" : "WHITE'S TURN");
            font.draw(batch, status + "    R: Restart    U: Undo    ESC: Exit", 48, 30);
            batch.end();
        }

        @Override
        public void resize(int width, int height) {
            viewport.update(width, height, false);
        }

        @Override
        public void dispose() {
            shapes.dispose();
            batch.dispose();
            font.dispose();
        }
    }



    private static void removeWindowShadow(WinDef.HWND hwnd) {
        // 1. 去掉 DWM 阴影
        final int DWMWA_NCRENDERING_POLICY = 2;
        final int DWMNCRP_DISABLED = 0;
        Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_NCRENDERING_POLICY, new int[]{DWMNCRP_DISABLED}, 4);

        // 2. 去掉系统边框阴影
        int style = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE);
        style &= ~(WinUser.WS_CAPTION | WinUser.WS_THICKFRAME); // 去掉标题栏和可调整边框
        User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_STYLE, style);

        int exStyle = User32.INSTANCE.GetWindowLong(hwnd, GWL_EXSTYLE);
        exStyle &= ~(WS_EX_DLGMODALFRAME | WS_EX_CLIENTEDGE | WS_EX_STATICEDGE); // 去掉额外边框
        User32.INSTANCE.SetWindowLong(hwnd, GWL_EXSTYLE, exStyle);

        // 更新窗口，让修改生效
        User32.INSTANCE.SetWindowPos(hwnd, null, 0, 0, 0, 0,
                SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    }

    // DWM API，用于取消 Aero 阴影
    public interface Dwmapi extends com.sun.jna.Library {
        Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class);

        int DwmSetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, IntByReference pvAttribute, int cbAttribute);
        int DwmSetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, int[] pvAttribute, int cbAttribute);
    }

    public static void removeShadow(long hwndPointer) {
        WinDef.HWND hwnd = new WinDef.HWND(Pointer.createConstant(hwndPointer));
        User32.INSTANCE.SetWindowLong(hwnd, GWL_EXSTYLE, WinUser.WS_EX_LAYERED);
//        Dwmapi.DwmExtendFrameIntoClientArea(hwnd, new MARGINS(-1));
    }

    boolean dragging = false;
    double dragOffsetX, dragOffsetY;
    public void drag(){
        long window = app.getWindowHandle();
        // 鼠标移动
//        GLFW.glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
//            if (dragging) {
//                // 获取屏幕位置
//                int[] winX = new int[1];
//                int[] winY = new int[1];
//                GLFW.glfwGetWindowPos(window, winX, winY);
//
//                // 新位置 = 旧窗口位置 + 鼠标移动差
//                int newX = (int)(winX[0] + xpos - dragOffsetX);
//                int newY = (int)(winY[0] + ypos - dragOffsetY);
//
//                GLFW.glfwSetWindowPos(window, newX, newY);
//            }
//        });

        // 鼠标按下
//        GLFW.glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
//            System.out.println("========================");
//            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
//                if (action == GLFW.GLFW_PRESS) {
//                    dragging = true;
//                    System.out.println("========================");
//
//                    double[] xpos = new double[1];
//                    double[] ypos = new double[1];
//                    GLFW.glfwGetCursorPos(win, xpos, ypos);
//
//                    // 记录鼠标按下时窗口相对位置
//                    dragOffsetX = xpos[0];
//                    dragOffsetY = ypos[0];
//                } else if (action == GLFW.GLFW_RELEASE) {
//                    dragging = false;
//                }
//            }
//        });

    }
}
