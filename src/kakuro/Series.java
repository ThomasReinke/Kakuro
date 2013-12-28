/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kakuro;
import java.util.*;
/**
 *
 * @author Janet
 */
public class Series {
    private String startCell;
    private String direction;
    private int length;
    private int total;
    private ArrayList<String> cells = new ArrayList<String>();
    private ArrayList<String> combos = new ArrayList<String>();
    private ArrayList<Integer> values = new ArrayList<Integer>();
    public Series(String startCell, String direction, int length, int total, ArrayList<String> cells, ArrayList<String> combos, ArrayList<Integer> values){
        this.startCell = startCell;
        this.direction = direction;
        this.length = length;
        this.total = total;
        this.cells = cells;
        this.combos = combos;
        this.values = values;
    }
    public Series(){
        
    }
    //deeply clones the series for backup
    public Series clone(){
        Series newSeries = new Series();
        newSeries.startCell = this.startCell;
        newSeries.direction = this.direction;
        newSeries.length = this.length;
        newSeries.total = this.total;
        newSeries.cells = new ArrayList<String>(this.cells);
        newSeries.combos = new ArrayList<String>(this.combos);
        newSeries.values = new ArrayList<Integer>(this.values);
        return newSeries;
    }
    public String getStartCell() {
        return startCell;
    }
    public String getDirection() {
        return direction;
    }
    public int getLength() {
        return length;
    }
    public int getTotal() {
        return total;
    }
    public ArrayList<String> getCells(){
        return cells;
    }
    public ArrayList<String> getCombos(){
        return combos;
    }
    public ArrayList<Integer> getValues(){
        return values;
    }
    public void addValue(int value){
        values.add(value);
    }
    public void removeCombo(int index){
        combos.remove(index);
    }
    public String toString(){
        String seriesString = startCell + " " + direction + " " + length + " " + total + " " + cells.toString() + " " + values.toString() + " " + combos.toString();
        return seriesString;
    }
}
