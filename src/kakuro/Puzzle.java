/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kakuro;
import java.io.*;
import java.util.Arrays;
/**
 *
 * @author Janet
 */
public class Puzzle {
    private int height;
    private int width;
    private String puzzleString;
    private String[][] puzzleState;
    public Puzzle(String puzzString) {
        puzzleString = puzzString;
        String[] splitPuzz = puzzleString.split(":", 2);
        String puzzData = splitPuzz[1];
        String[] wxh = splitPuzz[0].split("x");
        width = Integer.parseInt(wxh[0]);
        height = Integer.parseInt(wxh[1]);
        String[] puzzData2 = puzzData.split(",");
        puzzleState = new String[height][width];
        int z = 0;
	for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                //System.out.println(puzzData2[z]);
                /*
                if (puzzData2[z].equals("#")) {
                    puzzleState[x][y] = "#";
                }else if (puzzData2[z].equals("@")){
                    puzzleState[x][y] = "@";
                }
                */
                puzzleState[x][y] = (puzzData2[z]);
                
                z++;
            }
	}
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int h) {
        height = h;
    }

    public int getWidth() {
        return width;
    }
    public void setWidth(int w) {
        width = w;
    }

    public String getPuzzleString() {
        return puzzleString;
    }
    public void setPuzzleString(String puzzString) {
        puzzleString = puzzString;
    }

    public String[][] getPuzzleState() {
        return puzzleState;
    }
}
