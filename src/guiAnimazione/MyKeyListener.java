package guiAnimazione;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Key listener per l'animazione comandata da tastiera
 * @author Francesco Pham
 */
public class MyKeyListener implements KeyListener {
    private boolean isUpPressed = false;
    private boolean isDownPressed = false;
    private boolean isLeftPressed = false;
    private boolean isRightPressed = false;
    
    @Override
    public void keyTyped(KeyEvent e) {
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT) {
            isLeftPressed = true;
        }
        
        if (key == KeyEvent.VK_RIGHT) {
            isRightPressed = true;
        }
        
        if (key == KeyEvent.VK_UP) {
            isUpPressed = true;
        }
        
        if (key == KeyEvent.VK_DOWN) {
            isDownPressed = true;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            isLeftPressed = false;
        }
        
        if (key == KeyEvent.VK_RIGHT) {
            isRightPressed = false;
        }
        
        if (key == KeyEvent.VK_UP) {
            isUpPressed = false;
        }
        
        if (key == KeyEvent.VK_DOWN) {
            isDownPressed = false;
        }
    }
    
    public boolean isUpPressed() {
        return isUpPressed;
    }
    
    public boolean isDownPressed() {
        return isDownPressed;
    }
    
    public boolean isLeftPressed() {
        return isLeftPressed;
    }
    
    public boolean isRightPressed() {
        return isRightPressed;
    }
}