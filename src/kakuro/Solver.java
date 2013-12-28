 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kakuro;
import java.util.*;
import java.util.regex.*;
import java.io.*;

/**
 *
 * @author Janet
 */
public class Solver {
    private String[][] puzzleState;
    private String[][] oldPuzzleState;
    private int height;
    private int width;
    private int solveCount;
    private int guessCount;
    private String difficulty;
    private String[][] candidates;
    private String[][] oldCandidates;
    private String[][] combinations;
    private String[][] startPuzzleState;
    private ArrayList<Series> seriesArray;
    private ArrayList<Series> subSeriesArray;
    private ArrayList<String> foundSurfaceSums;
    private boolean noCandidates;
    private boolean hasValue;
    private boolean doingGuess;
    private boolean invalidNumber;
    private int invalidX;
    private int invalidY;
    private int guessDepth;
    private int guessSolveCount;
    private int guessLimit;
    private int candidatesLimit;
    private int cellCount;
    private ArrayList<String> cells;
    public String solve(String puzzleString, String diff) {
        difficulty = diff;
        Puzzle puzzle = new Puzzle(puzzleString);
        height = puzzle.getHeight();
        width = puzzle.getWidth();
        puzzleState = puzzle.getPuzzleState();
        candidates = new String[height][width];
        seriesArray = new ArrayList<Series>();
        subSeriesArray = new ArrayList<Series>();
        foundSurfaceSums = new ArrayList<String>();
        solveCount = 0;
        guessCount = 0;
        noCandidates = false;
        hasValue = false;
        doingGuess = false;
        invalidNumber = false;
        invalidX = 0;
        invalidY = 0;
        guessDepth = 0;
        guessSolveCount = 0;
        guessLimit = 0;
        candidatesLimit = 0;
        cellCount = 0;
        cells = new ArrayList<String>();
        //sets combinations array
        initCombos();
        //creates series object for each series in puzzle
        createSeries();
        
        if (invalidNumber == true){
            return "Invalid number at " + invalidX + "-" + invalidY;
        }
        //generates initial candidates for each cell
        initCandidates();
        
        if (difficulty.equals("easy")){
            guessDepth = 0;
            guessLimit = 0;
            candidatesLimit = 9;
        }else if (difficulty.equals("medium")){
            guessDepth = 0;
            candidatesLimit = 9;
        }else if (difficulty.equals("hard")){
            guessDepth = 1;
            guessLimit = 1;
            candidatesLimit = 4;
        }else if (difficulty.equals("max")){
            guessDepth = 5;
            guessLimit = 20;
            candidatesLimit = 9;
        }
        //counts the cells
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                if (puzzleState[x][y].equals("@")){
                    cellCount++;
                    cells.add(x + "-" + y);
                }
            }
        }
        //starts solving
        solveLogic();
        //output puzzlestate
        
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                if (puzzleState[x][y].contains("(")){
                    System.out.print("#");
                }else{
                    System.out.print(puzzleState[x][y]);
                }
            }
            System.out.println("");
        }
        String solution = "";
        String solved = "true";
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                if (!(puzzleState[x][y].contains("(") || puzzleState[x][y].equals("#"))){
                    solution += puzzleState[x][y] + ",";
                }
                if (puzzleState[x][y].equals("@")){
                    solved = "false";
                }
            }
        }
        System.out.println(solveCount);
        return (solved + ":" + solveCount + ":" + solution.substring(0, solution.length() - 1));
        
    }
    public void solveLogic(){
        //creates snapshots of arrays for comparing at end of solvelogic
        oldPuzzleState = new String[height][width];
        //oldPuzzleState = puzzleState;
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                oldPuzzleState[x][y] = puzzleState[x][y];
            }
        }
        oldCandidates = new String[height][width];
        //oldPuzzleState = puzzleState;
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                oldCandidates[x][y] = candidates[x][y];
            }
        }
        //finds spots where puzzle can be divided into two
       surfaceSums();

        //solves cells with only one candidate
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                if (puzzleState[x][y].equals("@")){
                    if (candidates[x][y].length() == 1){
                        int value = Integer.parseInt(candidates[x][y]);
                        solveCell(x, y, value, "naked single");
                    }
                }
            }
	}
        
        //checks to see if cells all have candidates
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                if (puzzleState[x][y].equals("@")){
                    if (candidates[x][y].length() == 0){
                        //System.out.println("no candidates at:" + x + "-" + y);
                        noCandidates = true;
                    }
                }
            }
	}
        //removes solved series
        Iterator<Series> it = seriesArray.iterator();
        while (it.hasNext()) {
            Series currentSeries = it.next();
            
            if(currentSeries.getValues().size() == currentSeries.getLength()){
                it.remove();
            }
        }

        //removes candidates from cells with removed combos
        for (int i = 0; i < seriesArray.size(); i++) {
            Series currentSeries = seriesArray.get(i);
            removeCandidates(currentSeries);
        }
        for (int i = 0; i < subSeriesArray.size(); i++) {
            Series currentSeries = subSeriesArray.get(i);
            removeCandidates(currentSeries);
        }
        
        //removes combos when one of their numbers doesn't exist in candidates
        for (int i = 0; i < seriesArray.size(); i++) {
            Series currentSeries = seriesArray.get(i);
            removeCombos(currentSeries);
        }
        for (int i = 0; i < subSeriesArray.size(); i++) {
            Series currentSeries = subSeriesArray.get(i);
            removeCombos(currentSeries);
        }
        
        //find numbers that are required for a series, and if only one cell has that number in candidates, solves it
        if(!difficulty.equals("easy")){
            for (int i = 0; i < seriesArray.size(); i++) {
                Series currentSeries = seriesArray.get(i);
                singleOccurrence(currentSeries);
            }
            for (int i = 0; i < subSeriesArray.size(); i++) {
                Series currentSeries = subSeriesArray.get(i);
                singleOccurrence(currentSeries);
            }
        }
        //handles naked doubles
        if(!difficulty.equals("easy")){
            for (int i = 0; i < seriesArray.size(); i++) {
                Series currentSeries = seriesArray.get(i);
                nakedDouble(currentSeries);
            }
            for (int i = 0; i < subSeriesArray.size(); i++) {
                Series currentSeries = subSeriesArray.get(i);
                nakedDouble(currentSeries);
            }
        }
        //checks cells with two candidates two see if they're both in single combo
        if(!difficulty.equals("easy")){
            for (int i = 0; i < seriesArray.size(); i++) {
                Series currentSeries = seriesArray.get(i);
                twoCandidates(currentSeries);
            }
            for (int i = 0; i < subSeriesArray.size(); i++) {
                Series currentSeries = subSeriesArray.get(i);
                twoCandidates(currentSeries);

            }
        }

        //checks for solved
        boolean isSolved = true;
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                if (puzzleState[x][y].equals("@")){
                    isSolved = false;
                }
            }
        }
        //keeps solving until no change is made
        if(isSolved == false){
            if (doingGuess == false){
                if (((Arrays.deepEquals(puzzleState, oldPuzzleState) == false) || (Arrays.deepEquals(candidates, oldCandidates) == false)) && solveCount < 20000 && noCandidates == false) {
                    solveCount++;
                    solveLogic();
                }else{
                    if(guessCount < guessLimit){
                        solveCount++;
                        startGuess();
                        solveLogic();
                    }
                }
            //if the solving is being used for guessing, there can be a limit on the number of iterations
            }else{
                if (((Arrays.deepEquals(puzzleState, oldPuzzleState) == false) || (Arrays.deepEquals(candidates, oldCandidates) == false)) && solveCount < 20000 && noCandidates == false && guessSolveCount < guessDepth) {
                    guessSolveCount++;
                    solveCount++;
                    solveLogic();
                }
            }
        }
    }
    public void surfaceSums(){
         if (solveCount == 0 && !difficulty.equals("easy") && !difficulty.equals("medium")){
            for (int x=0;x < (height);x++) {
                for (int y=0; y < (width);y++) {
                    if (cells.contains(x + "-" + y)){
                        
                        int surroundingWallCount1 = 0;
                        int surroundingWallCount2 = 0;
                        boolean up = false;
                        boolean down = false;
                        boolean left = false;
                        boolean right = false;
                        boolean upright = false;
                        boolean upleft = false;
                        boolean downleft = false;
                        boolean downright = false;
                        if(puzzleState[x + 1][y].equals("#") || puzzleState[x + 1][y].contains("(")){
                            surroundingWallCount1++;
                            surroundingWallCount2++;
                            down = true;
                        }
                        if(puzzleState[x - 1][y].equals("#") || puzzleState[x - 1][y].contains("(")){
                            surroundingWallCount1++;
                            surroundingWallCount2++;
                            up = true;
                        }
                        if(puzzleState[x][y + 1].equals("#") || puzzleState[x][y + 1].contains("(")){
                            surroundingWallCount1++;
                            surroundingWallCount2++;
                            right = true;
                        }
                        if(puzzleState[x][y - 1].equals("#") || puzzleState[x][y - 1].contains("(")){
                            surroundingWallCount1++;
                            surroundingWallCount2++;
                            left = true;
                        }
                        if(puzzleState[x + 1][y + 1].equals("#") || puzzleState[x + 1][y + 1].contains("(")){
                            surroundingWallCount2++;
                            downright = true;
                        }
                        if(puzzleState[x + 1][y - 1].equals("#") || puzzleState[x + 1][y - 1].contains("(")){
                            surroundingWallCount2++;
                            downleft = true;
                        }
                        if(puzzleState[x - 1][y + 1].equals("#") || puzzleState[x - 1][y + 1].contains("(")){
                            surroundingWallCount2++;
                            upright = true;
                        }
                        if(puzzleState[x - 1][y - 1].equals("#") || puzzleState[x - 1][y - 1].contains("(")){
                            surroundingWallCount2++;
                            upleft = true;
                        }
                        
                        //finds places where sub-series can be deduced
                        if (surroundingWallCount1 == 1 && surroundingWallCount2 > 1 && !foundSurfaceSums.contains(x + "-" + y)){
                            boolean found = false;
                            String dir = "";
                            String dir2 = "";
                            if(up == true && (downright == true || downleft == true)){
                                found = true;
                                dir = "up";
                                if(downright == true){
                                    dir2 = "right";
                                }else if(downleft == true){
                                    dir2 = "left";
                                }
                            }
                            if(down == true && (upright == true || upleft == true)){
                                found = true;
                                dir = "down";
                                if(upright == true){
                                    dir2 = "right";
                                }else if(upleft == true){
                                    dir2 = "left";
                                }
                            }
                            if(left == true && (upright == true || downright == true)){
                                found = true;
                                dir = "left";
                                if(upright == true){
                                    dir2 = "up";
                                }else if(downright == true){
                                    dir2 = "down";
                                }
                            }
                            if(right == true && (upleft == true || downleft == true)){
                                found = true;
                                dir = "right";
                                if(upleft == true){
                                    dir2 = "up";
                                }else if(downleft == true){
                                    dir2 = "down";
                                }
                            }
                            if (found == true){
                                
                                ArrayList<String> visitedCells = new ArrayList<String>();
                                puzzleState[x][y] = "#";
                                if(dir.equals("up")){
                                    travelCells(x + 1, y, visitedCells);
                                }else if(dir.equals("down")){
                                    travelCells(x - 1, y, visitedCells); 
                                }else if(dir.equals("left")){
                                    travelCells(x, y + 1, visitedCells); 
                                }else if(dir.equals("right")){
                                    travelCells(x, y - 1, visitedCells); 
                                }
                                puzzleState[x][y] = "@";
                                if ((visitedCells.size() + 1) != cellCount){
                                    
                                    int horCount = 0;
                                    int vertCount = 0;
                                    //foundSurfaceSums.add(x + "-" + y);
                                    //System.out.println("found one at " + x + " " + y + ": " + visitedCells.size() + " " + cellCount);
                                    for (int i = 0; i < seriesArray.size(); i++) {
                                        Series currentSeries = seriesArray.get(i);
                                        for (int j = 0; j < currentSeries.getCells().size(); j++) {
                                            String currentCell = currentSeries.getCells().get(j);
                                            if (visitedCells.contains(currentCell)){
                                                if(currentSeries.getDirection() == "h"){
                                                    horCount += currentSeries.getTotal();
                                                }else{
                                                    vertCount += currentSeries.getTotal();
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    int total = Math.abs(horCount - vertCount);
                                    //System.out.println(dir + " " + dir2 + " at: " + x + "-" + y + " difference " + total);
                                    String direction = "";
                                    String startCell = "";
                                    ArrayList<String> seriesCells = new ArrayList<String>();
                                    if(dir2.equals("down")){
                                        direction = "v";
                                        int z = 0;
                                        startCell = ((x + 1) + "-" + y);
                                        while (z < 10){
                                            if(!puzzleState[x + 1 + z][y].equals("#") && !puzzleState[x + 1 + z][y].contains("(")){
                                                seriesCells.add((x + 1 + z) + "-" + y);
                                            }else{
                                                break;
                                            }
                                            z++;
                                        }
                                    }else if(dir2.equals("up")){
                                        direction = "v";
                                        int z = 0;
                                        startCell = ((x - 1) + "-" + y);
                                        while (z < 10){
                                            if(!puzzleState[x - 1 - z][y].equals("#") && !puzzleState[x - 1 - z][y].contains("(")){
                                                seriesCells.add((x - 1 - z) + "-" + y);
                                            }else{
                                                break;
                                            }
                                            z++;
                                        }
                                    }else if(dir2.equals("left")){
                                        direction = "h";
                                        int z = 0;
                                        startCell = (x + "-" + (y - 1));
                                        while (z < 10){
                                            if(!puzzleState[x][y - 1 - z].equals("#") && !puzzleState[x][y - 1 - z].contains("(")){
                                                seriesCells.add(x + "-" + (y - 1 - z));
                                            }else{
                                                break;
                                            }
                                            z++;
                                        }
                                    }else if(dir2.equals("right")){
                                        direction = "h";
                                        int z = 0;
                                        startCell = (x + "-" + (y + 1));
                                        while (z < 10){
                                            if(!puzzleState[x][y + 1 + z].equals("#") && !puzzleState[x][y + 1 + z].contains("(")){
                                                seriesCells.add(x + "-" + (y + 1 + z));
                                            }else{
                                                break;
                                            }
                                            z++;
                                        }
                                    }
                                    if (seriesCells.size() > 1){
                                        String comboString = combinations[seriesCells.size()][total];
                                        ArrayList<String> combosList = new ArrayList<String>(Arrays.asList(comboString.split(",")));
                                        ArrayList<Integer> values = new ArrayList<Integer>();
                                        Series newSeries = new Series(startCell, direction, seriesCells.size(), total, seriesCells, combosList, values);
                                        subSeriesArray.add(newSeries);
                                        //System.out.println(newSeries.toString());
                                    }
                                }
                            }
                        }
                        //solves places where puzzle can be divided cleanly in two
                        if (surroundingWallCount1 == 2){
                            ArrayList<String> visitedCells = new ArrayList<String>();
                            puzzleState[x][y] = "#";
                            if(cells.contains((x + 1) + "-" + y)){
                                travelCells(x + 1, y, visitedCells);
                            }else if(cells.contains((x - 1) + "-" + y)){
                                travelCells(x - 1, y, visitedCells); 
                            }else if(cells.contains(x + "-" + (y + 1))){
                                travelCells(x, y + 1, visitedCells); 
                            }else if(cells.contains(x + "-" + (y - 1))){
                                travelCells(x, y - 1, visitedCells); 
                            }
                            puzzleState[x][y] = "@";
                            if ((visitedCells.size() + 1) != cellCount){
                                int horCount = 0;
                                int vertCount = 0;
                                foundSurfaceSums.add(x + "-" + y);
                                //System.out.println("found one at " + x + " " + y + ": " + visitedCells.size() + " " + cellCount);
                                for (int i = 0; i < seriesArray.size(); i++) {
                                    Series currentSeries = seriesArray.get(i);
                                    for (int j = 0; j < currentSeries.getCells().size(); j++) {
                                        String currentCell = currentSeries.getCells().get(j);
                                        if (visitedCells.contains(currentCell)){
                                            if(currentSeries.getDirection() == "h"){
                                                horCount += currentSeries.getTotal();
                                            }else{
                                                vertCount += currentSeries.getTotal();
                                            }
                                            break;
                                        }
                                    }
                                }
                                int difference = Math.abs(horCount - vertCount);
                                solveCell(x, y, difference, "surface sums");
                            }
                        }
                    }
                }
            }
            //changes candidates based on subseries
            for (int i = 0; i < subSeriesArray.size(); i++) {
                Series currentSeries = subSeriesArray.get(i);
                String comboNumbers = "";
                for (int k = 0; k < currentSeries.getCombos().size(); k++) {
                    String combos = currentSeries.getCombos().get(k);
                    for (int l = 0; l < combos.length(); l++) {
                        String[] nums = combos.split("");
                        for (int m = 0; m < nums.length; m++) {
                            if(!comboNumbers.contains(nums[m])){
                                comboNumbers += nums[m];
                            }
                        }
                    }
                }
                for (int j = 0; j < currentSeries.getCells().size(); j++) {
                    String[] coords = currentSeries.getCells().get(j).split("-");
                    int candsx = Integer.parseInt(coords[0]);
                    int candsy = Integer.parseInt(coords[1]);   
                    String[] tempCandidates = candidates[candsx][candsy].split("");
                    for (int n = 0; n < tempCandidates.length; n++) {
                        if (!comboNumbers.contains(tempCandidates[n])){
                            String blank = "-";
                            char blankChar = blank.charAt(0);
                            candidates[candsx][candsy] = candidates[candsx][candsy].replace(candidates[candsx][candsy].charAt(n - 1), blankChar);
                            //System.out.println("removing " + tempCandidates[n] + " at " + candsx + "-" + candsy + " because of " + comboNumbers);
                        }
                    }
                    candidates[candsx][candsy] = candidates[candsx][candsy].replace("-", "");
                }
            }
        }
    }
    public void twoCandidates(Series currentSeries){
        String num1 = "";
        String num2 = "";
        for (int j = 0; j < currentSeries.getCells().size(); j++) {
            String coords[] = currentSeries.getCells().get(j).split("-");
            int candsx = Integer.parseInt(coords[0]);
            int candsy = Integer.parseInt(coords[1]);
            if (candidates[candsx][candsy].length() == 2) {
                String[] temp = candidates[candsx][candsy].split("");
                num1 = temp[1];
                num2 = temp[2];
                boolean foundCombo = false;
                for (int k = 0; k < currentSeries.getCombos().size(); k++) {
                    if(currentSeries.getCombos().get(k).contains(num1) && currentSeries.getCombos().get(k).contains(num2)){
                        foundCombo = true;
                    }
                }
                if(foundCombo == false){
                    //System.out.println("removing " + num1 + " and " + num2);
                    for (int l = 0; l < currentSeries.getCells().size(); l++) {
                        if(l != j){
                            coords = currentSeries.getCells().get(l).split("-");
                            candsx = Integer.parseInt(coords[0]);
                            candsy = Integer.parseInt(coords[1]);

                            String blank = "-";
                            char blankChar = blank.charAt(0);
                            candidates[candsx][candsy] = candidates[candsx][candsy].replace(num1, "-");
                            candidates[candsx][candsy] = candidates[candsx][candsy].replace(num2, "-");
                            candidates[candsx][candsy] = candidates[candsx][candsy].replace("-", "");
                        }
                    }
                }
            }
        }
    }
    public void nakedDouble(Series currentSeries){
        if (currentSeries.getLength() != 2){
            ArrayList<String> candidatesList = new ArrayList<String>();
            for (int j = 0; j < currentSeries.getCells().size(); j++) {
                String coords[] = currentSeries.getCells().get(j).split("-");
                int candsx = Integer.parseInt(coords[0]);
                int candsy = Integer.parseInt(coords[1]);
                if(candidates[candsx][candsy].length() == 2){
                    candidatesList.add(candidates[candsx][candsy]);
                }
            }
            //finds naked doubles
            String nakedDouble = "";
            Iterator<String> it = candidatesList.iterator();
            while (it.hasNext()) {
                String cands = it.next();
                it.remove();
                if(candidatesList.contains(cands) == true){
                    nakedDouble += cands;
                }
            }
            //removes candidates in naked double from other cells
            for (int j = 0; j < currentSeries.getCells().size(); j++) {
                String coords[] = currentSeries.getCells().get(j).split("-");
                int candsx = Integer.parseInt(coords[0]);
                int candsy = Integer.parseInt(coords[1]);
                for (int l = 0; l < candidates[candsx][candsy].length(); l++) {
                    if (nakedDouble.length() == 2 && nakedDouble.contains(String.valueOf(candidates[candsx][candsy].charAt(l))) && !candidates[candsx][candsy].equals(nakedDouble)){
                        String blank = "-";
                        char blankChar = blank.charAt(0);
                        //System.out.println(candidates[candsx][candsy] + " " + nakedDouble);
                        //System.out.println("removing " + candidates[candsx][candsy].charAt(l) + " from " + currentSeries.getCells().get(j));
                        candidates[candsx][candsy] = candidates[candsx][candsy].replace(candidates[candsx][candsy].charAt(l), blankChar);
                    }
                }
                candidates[candsx][candsy] = candidates[candsx][candsy].replace("-", "");
            }
            //removes combos without naked double
            if (nakedDouble.length() == 2){
                Iterator<String> it2 = currentSeries.getCombos().iterator();
                while (it2.hasNext()) {
                    String combo = it2.next();
                    String[] nums = nakedDouble.split("");
                    //System.out.println(combo + " " + nakedDouble);
                    if(!combo.contains(nums[1]) && !combo.contains(nums[2])){
                        //System.out.println("removing " + combo + " because of " + nakedDouble);
                        it2.remove();
                    }
                }
            }
        }
    }
    public void singleOccurrence(Series currentSeries){
        ArrayList<String> required = new ArrayList<String>();
        //loops through combos keeping only items in each arraylist
        for (int j = 0; j < currentSeries.getCombos().size(); j++) {
            String[] numbers = currentSeries.getCombos().get(j).split("");
            ArrayList<String> nums = new ArrayList<String>(Arrays.asList(numbers));
            if (j == 0){
                required.addAll(nums);
            }else if (required.size() != 0){
                required.retainAll(nums);
            }else{
                break;
            }
            required.removeAll(Collections.singleton(""));
        }
        //if there is one occurence of a candidate in a series, and that candidate is required, solve it
        if(required.size() != 0){
            ArrayList<String> allCandidates = new ArrayList<String>();
            for (int k = 0; k < currentSeries.getCells().size(); k++) {
                String coords[] = currentSeries.getCells().get(k).split("-");
                int candsx = Integer.parseInt(coords[0]);
                int candsy = Integer.parseInt(coords[1]);
                String[] cands = candidates[candsx][candsy].split("");
                ArrayList<String> tempCandidates = new ArrayList<String>(Arrays.asList(cands));
                allCandidates.addAll(tempCandidates);
            }
            for (int l = 0; l < required.size(); l++) {
                int occurrences = Collections.frequency(allCandidates, required.get(l)); 
                if (occurrences == 1){
                    for (int m = 0; m < currentSeries.getCells().size(); m++) {           
                        String coords[] = currentSeries.getCells().get(m).split("-");
                        int candsx = Integer.parseInt(coords[0]);
                        int candsy = Integer.parseInt(coords[1]); 
                        if (candidates[candsx][candsy].contains(String.valueOf(required.get(l)))){
                            candidates[candsx][candsy] = String.valueOf(required.get(l));
                        }
                    }
                }
            }
        }
    }
    public void removeCombos(Series currentSeries){
        if (difficulty.equals("easy") && currentSeries.getLength() > 3){
               //doesn't remove combos on easy if series is longer than 3
        }else{
            String allCandidates = "";
            for (int j = 0; j < currentSeries.getCells().size(); j++) {
                String coords[] = currentSeries.getCells().get(j).split("-");
                int candsx = Integer.parseInt(coords[0]);
                int candsy = Integer.parseInt(coords[1]); 
                allCandidates += candidates[candsx][candsy];
            }
            Iterator<String> it = currentSeries.getCombos().iterator();
            while (it.hasNext()) {
                String numbers = it.next();
                String[] nums = numbers.split("");
                for (int l = 0; l < nums.length; l++) {
                    if(!allCandidates.contains(nums[l]) && !currentSeries.getValues().contains(nums[l])){
                        //System.out.println("removing " + numbers + " in " + currentSeries.getStartCell() + " because of " + allCandidates);
                        it.remove();
                        break;
                    }
                }
            }
        }
    }
    public void removeCandidates(Series currentSeries){
        String nums = "";
        for (int j = 0; j < currentSeries.getCombos().size(); j++) {
            String combos = currentSeries.getCombos().get(j);
            nums += combos;
        }

        for (int k = 0; k < currentSeries.getCells().size(); k++) {
            String coords[] = currentSeries.getCells().get(k).split("-");
            int candsx = Integer.parseInt(coords[0]);
            int candsy = Integer.parseInt(coords[1]); 
            for (int l = 0; l < candidates[candsx][candsy].length(); l++) {
                if (!nums.contains(String.valueOf(candidates[candsx][candsy].charAt(l)))){
                    String blank = "-";
                    char blankChar = blank.charAt(0);
                    candidates[candsx][candsy] = candidates[candsx][candsy].replace(candidates[candsx][candsy].charAt(l), blankChar);
                }
            }
            candidates[candsx][candsy] = candidates[candsx][candsy].replace("-", "");
        }
    }
    public void travelCells(int x, int y, ArrayList<String> visitedCells){
        boolean continueTraveling = true;
        while(continueTraveling == true){
            if (!visitedCells.contains(x + "-" + y)){
                visitedCells.add(x + "-" + y);
            }
            if(!puzzleState[x + 1][y].equals("#") && !puzzleState[x + 1][y].contains("(") && !visitedCells.contains((x + 1) + "-" + y)){
                x++;
            }else if(!puzzleState[x - 1][y].equals("#") && !puzzleState[x - 1][y].contains("(") && !visitedCells.contains((x - 1) + "-" + y)){
                x--;
            }else if(!puzzleState[x][y + 1].equals("#") && !puzzleState[x][y + 1].contains("(") && !visitedCells.contains(x + "-" + (y + 1))){
                y++;
            }else if(!puzzleState[x][y - 1].equals("#") && !puzzleState[x][y - 1].contains("(") && !visitedCells.contains(x + "-" + (y - 1))){
                y--;
            }else{
                int index = visitedCells.indexOf(x + "-" + y);
                if (index != 0){
                    String[] lastCell = visitedCells.get(index - 1).split("-");
                    int x2 = Integer.parseInt(lastCell[0]);
                    int y2 = Integer.parseInt(lastCell[1]);
                    x = x2;
                    y = y2;
                }else{
                    continueTraveling = false;
                }
            }
        }
    }
    public void solveCell(int x, int y, int value, String source){
        
        
        if(puzzleState[x][y] != String.valueOf(value)){
            String cell = (x + "-" + y);
            //removes combos that don't contain value just solved
            for (int i = 0; i < seriesArray.size(); i++) {
                Series thisSeries = seriesArray.get(i);
                if (thisSeries.getCells().contains(cell)){
                    if(!thisSeries.getValues().contains(value)){
                        modifyValues(thisSeries, cell, value);
                        Iterator<String> it = thisSeries.getCombos().iterator();
                        while (it.hasNext()) {
                            String combo = it.next();
                            if (!combo.contains(String.valueOf(value))) {
                                //System.out.println("removed " + combo + " in " + thisSeries.getStartCell());
                                it.remove();
                            }
                        }
                    }else{
                        System.out.println(value + " already in series at:" + x + "-" + y);
                        hasValue = true;
                    }
                }
            }
            //does subseries also
            for (int i = 0; i < subSeriesArray.size(); i++) {
                
                Series thisSeries = subSeriesArray.get(i);
                if (thisSeries.getCells().contains(cell)){
                    if(!thisSeries.getValues().contains(value)){
                        modifyValues(thisSeries, cell, value);
                        Iterator<String> it = thisSeries.getCombos().iterator();
                        while (it.hasNext()) {
                            String combo = it.next();
                            if (!combo.contains(String.valueOf(value))) {
                                //System.out.println("removed " + combo + " in " + thisSeries.getStartCell());
                                it.remove();
                            }
                        }
                    }else{
                        System.out.println(value + " already in series at:" + cell);
                        hasValue = true;
                    }
                }
            }
            puzzleState[x][y] = String.valueOf(value);
            candidates[x][y] = String.valueOf(value);
        }else{
            System.out.println("duplicate value at:" + x + "-" + y);
        }
    }
    public void modifyValues(Series thisSeries, String cell, int value){
        thisSeries.addValue(value);
        for (int j = 0; j < thisSeries.getCells().size(); j++) {
            String coords[] = thisSeries.getCells().get(j).split("-");
            int candsx = Integer.parseInt(coords[0]);
            int candsy = Integer.parseInt(coords[1]); 
            if (candidates[candsx][candsy].contains(String.valueOf(value))){
                candidates[candsx][candsy] = candidates[candsx][candsy].replace(String.valueOf(value), "");
            }
	}
    }
    public void startGuess(){
        guessCount++;
        //holds candidates and puzzle state for resetting later
        
        String[][] startCandidates;
        startCandidates = new String[height][width];
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                startCandidates[x][y] = candidates[x][y];
            }
        }

        startPuzzleState = new String[height][width];
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                startPuzzleState[x][y] = puzzleState[x][y];
            }
        }
        ArrayList<Series> startSeriesArray = new ArrayList<Series>();
        for(Series s : seriesArray){
            startSeriesArray.add(s.clone());
        }
        ArrayList<Series> startSubSeriesArray = new ArrayList<Series>();
        for(Series s : subSeriesArray){
            startSubSeriesArray.add(s.clone());
        }
        for (int i = 0; i < startSeriesArray.size(); i++) {
            if (seriesArray.get(i).getDirection().equals("h")){
                Series thisSeries = seriesArray.get(i);
                for (int j = 0; j < thisSeries.getCells().size(); j++) {
                    String[] coords = thisSeries.getCells().get(j).split("-");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    if(candidates[x][y].length() != 1 && candidates[x][y].length() <= candidatesLimit){
                        for (int k = 0; k < candidates[x][y].length(); k++) {
                            int value = Integer.parseInt(String.valueOf(candidates[x][y].charAt(k)));
                            //System.out.println("testing " + value + " at " + x + "-" + y);
                            doingGuess = true;
                            solveCell(x, y, value, "brute force");
                            solveLogic();
                            guessSolveCount = 0;
                            doingGuess = false;
                            for (int x2=0;x2 < (height);x2++) {
                                for (int y2=0; y2 < (width);y2++) {
                                    candidates[x2][y2] = startCandidates[x2][y2];
                                }
                            }
                            for (int x2=0;x2 < (height);x2++) {
                                for (int y2=0; y2 < (width);y2++) {
                                    puzzleState[x2][y2] = startPuzzleState[x2][y2];
                                }
                            }
                            seriesArray.clear();
                            for(Series s : startSeriesArray){
                                seriesArray.add(s.clone());
                            }
                            subSeriesArray.clear();
                            for(Series s : startSubSeriesArray){
                                subSeriesArray.add(s.clone());
                            }
                            //remove candidates that cause invalid poisitions
                            if (noCandidates == true || hasValue == true){
                                //System.out.println("removed " + value + " at " + x + "-" + y);
                                startCandidates[x][y] = startCandidates[x][y].replace(String.valueOf(value), "");
                            }
                            noCandidates = false;
                            hasValue = false;
                        }
                    }
                }
            }
        }
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                candidates[x][y] = startCandidates[x][y];
            }
        }
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                puzzleState[x][y] = startPuzzleState[x][y];
            }
        }
        seriesArray.clear();
        for(Series s : startSeriesArray){
            seriesArray.add(s.clone());
        }
        subSeriesArray.clear();
        for(Series s : startSubSeriesArray){
            subSeriesArray.add(s.clone());
        }
    }
    public void createSeries(){
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                if (puzzleState[x][y].contains("(") && invalidNumber != true){
                    int seriesLength;
                    Series newSeries;
                    String direction = "";
                    ArrayList<String> seriesCells;
                    ArrayList<String> combos;
                    ArrayList<Integer> values;
                    int z = 0;
                    String[] nums = puzzleState[x][y].split("-");
                    if (nums.length == 2){
                        nums[0] = nums[0].substring(1);
			nums[1] = nums[1].substring(0, nums[1].length() - 1);
                        seriesCells = new ArrayList<String>();
                        while (z < 10){
                            if(puzzleState[x][y + 1 + z].equals("@")){
                                direction = "h";
                                seriesCells.add(x + "-" + (y + 1 + z));
                            }else{
                                break;
                            }
                            z++;
                        }
                        String comboString = "";
                        try{
                             comboString = combinations[seriesCells.size()][Integer.parseInt(nums[1])];
                        }
                        catch (ArrayIndexOutOfBoundsException e){
                            invalidNumber = true;
                            invalidX = x;
                            invalidY = y;
                        }
                        if (invalidNumber != true && combinations[seriesCells.size()][Integer.parseInt(nums[1])] != null){
                            //System.out.println(nums[1]);
                            ArrayList<String> combosList = new ArrayList<String>(Arrays.asList(comboString.split(",")));
                            values = new ArrayList<Integer>();
                            newSeries = new Series(x + "-" + y, direction, seriesCells.size(), Integer.parseInt(nums[1]), seriesCells, combosList, values);
                            seriesArray.add(newSeries);
                        }else{
                            invalidNumber = true;
                            invalidX = x;
                            invalidY = y;
                        }
                        
                        seriesCells = new ArrayList<String>();
                        z = 0;
                        while (z < 10){
                            if((x + 1 + z) < height && puzzleState[x + 1 + z][y].equals("@")){
                                direction = "v";
                                seriesCells.add((x + 1 + z) + "-" + y);
                            }else{
                                break;
                            }
                            z++;
                        }
                        comboString = "";
                        
                        try{
                             comboString = combinations[seriesCells.size()][Integer.parseInt(nums[0])];
                        }
                        catch (ArrayIndexOutOfBoundsException e){
                            invalidNumber = true;
                            invalidX = x;
                            invalidY = y;
                        }
                        if (invalidNumber != true && combinations[seriesCells.size()][Integer.parseInt(nums[0])] != null){
                            comboString = combinations[seriesCells.size()][Integer.parseInt(nums[0])];
                            //System.out.println(nums[0]);
                            ArrayList<String> combosList = new ArrayList<String>(Arrays.asList(comboString.split(",")));
                            values = new ArrayList<Integer>();
                            newSeries = new Series(x + "-" + y, direction, seriesCells.size(), Integer.parseInt(nums[0]), seriesCells, combosList, values);
                            seriesArray.add(newSeries);
                        }else{
                            invalidNumber = true;
                            invalidX = x;
                            invalidY = y;
                        }
                    }else{
                        
                        seriesCells = new ArrayList<String>();
                        nums[0] = nums[0].substring(1);
			nums[0] = nums[0].substring(0, nums[0].length() - 1);
                        
                        while (z < 10){
                            if(puzzleState[x][y + 1 + z].equals("@")){
                                direction = "h";
                                seriesCells.add(x + "-" + (y + 1 + z));
                            }else{
                                break;
                            }
                            z++;
                        }
                        z = 0;   
                        while (z < 10){
                            if((x + 1 + z) < height && puzzleState[x + 1 + z][y].equals("@")){
                                direction = "v";
                                seriesCells.add((x + 1 + z) + "-" + y);
                            }else{
                                break;
                            }
                            z++;
                        }
                        String comboString = "";
                        try{
                            comboString = combinations[seriesCells.size()][Integer.parseInt(nums[0])];
                        }
                        catch (ArrayIndexOutOfBoundsException e){
                            invalidNumber = true;
                            invalidX = x;
                            invalidY = y;
                        }
                        if (invalidNumber != true && combinations[seriesCells.size()][Integer.parseInt(nums[0])] != null){

                            //System.out.println(comboString + seriesCells.size() + " " + nums[0]);

                            ArrayList<String> combosList = new ArrayList<String>(Arrays.asList(comboString.split(",")));

                            values = new ArrayList<Integer>();
                            newSeries = new Series(x + "-" + y, direction, seriesCells.size(), Integer.parseInt(nums[0]), seriesCells, combosList, values);
                            seriesArray.add(newSeries);

                        }else{
                            invalidNumber = true;
                            invalidX = x;
                            invalidY = y;
                        }
                    }
                }
            }
        }
    }
    public void initCandidates(){
        int x;
        int y;
        for (int i = 0; i < seriesArray.size(); i++) {
            if (seriesArray.get(i).getDirection().equals("h")){
                Series currentSeries = seriesArray.get(i);
                for (int j = 0; j < currentSeries.getCells().size(); j++) {
                    String[] coords = currentSeries.getCells().get(j).split("-");
                    x = Integer.parseInt(coords[0]);
                    y = Integer.parseInt(coords[1]);    
                    candidates[x][y] = "";
                    for (int k = 0; k < currentSeries.getCombos().size(); k++) {
                        String combos = currentSeries.getCombos().get(k);
                        for (int l = 0; l < combos.length(); l++) {
                            String[] nums = combos.split("");
                            for (int m = 0; m < nums.length; m++) {
                                if (!candidates[x][y].contains(nums[m])){
                                    candidates[x][y] += nums[m];
                                }
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < seriesArray.size(); i++) {
            if (seriesArray.get(i).getDirection().equals("v")){
                Series currentSeries = seriesArray.get(i);
                for (int j = 0; j < currentSeries.getCells().size(); j++) {
                    String[] coords = currentSeries.getCells().get(j).split("-");
                    x = Integer.parseInt(coords[0]);
                    y = Integer.parseInt(coords[1]);    
                    String nums = "";
                    for (int k = 0; k < currentSeries.getCombos().size(); k++) {
                        String[] combos = currentSeries.getCombos().get(k).split(",");
                        for (int l = 0; l < combos.length; l++) {
                            nums += combos[l];
                        }
                    }
                    String candidatesArray[] = candidates[x][y].split("");
                    for (int m = 0; m < candidatesArray.length; m++) {
                        if (!nums.contains(candidatesArray[m])){
                            //System.out.println(nums + " " + candidatesArray[m] + "at" + candidates[x][y].indexOf(candidatesArray[m]));
                            candidates[x][y] = candidates[x][y].replace(candidatesArray[m], "");
                        }
                    }
                }
            }
        }
    }
    public void initCombos(){
        combinations = new String[10][46];
        combinations[1][1] = "1";
        combinations[1][2] = "2";
        combinations[1][3] = "3";
        combinations[1][4] = "4";
        combinations[1][5] = "5";
        combinations[1][6] = "6";
        combinations[1][7] = "7";
        combinations[1][8] = "8";
        combinations[1][9] = "9";
        combinations[2][3] = "12";
	combinations[2][4] = "13";
	combinations[2][5] = "14,23";
	combinations[2][6] = "15,24";
	combinations[2][7] = "16,25,34";
	combinations[2][8] = "17,26,35";
	combinations[2][9] = "18,27,36,45";
	combinations[2][10] = "19,28,37,46";
	combinations[2][11] = "29,38,47,56";
	combinations[2][12] = "39,48,57";
	combinations[2][13] = "49,58,67";
	combinations[2][14] = "59,68";
	combinations[2][15] = "69,78";
	combinations[2][16] = "79";
	combinations[2][17] = "89";
        combinations[3][6] = "123";
	combinations[3][7] = "124";
	combinations[3][8] = "125,134";
	combinations[3][9] = "126,135,234";
	combinations[3][10] = "127,136,145,235";
	combinations[3][11] = "128,137,146,236,245";
	combinations[3][12] = "129,138,147,156,237,246,345";
	combinations[3][13] = "139,148,157,238,247,256,346";
	combinations[3][14] = "149,158,167,239,248,257,347,356";
	combinations[3][15] = "159,168,249,258,267,348,357,456";
	combinations[3][16] = "169,178,259,268,349,358,367,457";
	combinations[3][17] = "179,269,278,359,368,458,467";
	combinations[3][18] = "189,279,369,378,459,468,567";
	combinations[3][19] = "289,379,469,478,568";
	combinations[3][20] = "389,479,569,578";
	combinations[3][21] = "489,579,678";
	combinations[3][22] = "589,679";
	combinations[3][23] = "689";
	combinations[3][24] = "789";
	combinations[4][10] = "1234";
	combinations[4][11] = "1235";
        combinations[4][12] = "1236,1245";
        combinations[4][13] = "1237,1246,1345";
        combinations[4][14] = "1238,1247,1256,1346,2345";
        combinations[4][15] = "1239,1248,1257,1347,1356,2346";
        combinations[4][16] = "1249,1258,1267,1348,1357,1456,2347,2356";
        combinations[4][17] = "1259,1268,1349,1358,1367,1457,2348,2357,2456";
        combinations[4][18] = "1269,1278,1359,1368,1458,1467,2349,2358,2367,2457,3456";
        combinations[4][19] = "1279,1369,1378,1459,1468,1567,2359,2368,2458,2467,3457";
        combinations[4][20] = "1289,1379,1469,1478,1568,2369,2378,2459,2468,2567,3458,3467";
        combinations[4][21] = "1389,1479,1569,1578,2379,2469,2478,2568,3459,3468,3567";
        combinations[4][22] = "1489,1579,1678,2389,2479,2569,2578,3469,3478,3568,4567";
        combinations[4][23] = "1589,1679,2489,2579,2678,3479,3569,3578,4568";
        combinations[4][24] = "1689,2589,2679,3489,3579,3678,4569,4578";
        combinations[4][25] = "1789,2689,3589,3679,4579,4678";
        combinations[4][26] = "2789,3689,4589,4679,5678";
        combinations[4][27] = "3789,4689,5679";
        combinations[4][28] = "4789,5689";
        combinations[4][29] = "5789";
        combinations[4][30] = "6789";
        combinations[5][15] = "12345";
        combinations[5][16] = "12346";
        combinations[5][17] = "12347,12356";
        combinations[5][18] = "12348,12357,12456";
        combinations[5][19] = "12349,12358,12367,12457,13456";
        combinations[5][20] = "12359,12368,12458,12467,13457,23456";
        combinations[5][21] = "12369,12378,12459,12468,12567,13458,13467,23457";
        combinations[5][22] = "12379,12469,12478,12568,13459,13468,13567,23458,23467";
        combinations[5][23] = "12389,12479,12569,12578,13469,13478,13568,14567,23459,23468,23567";
        combinations[5][24] = "12489,12579,12678,13479,13569,13578,14568,23469,23478,23568,24567";
	combinations[5][25] = "12589,12679,13489,13579,13678,14569,14578,23479,23569,23578,24568,34567";
        combinations[5][26] = "12689,13589,13679,14579,14678,23489,23579,23678,24569,24578,34568";
        combinations[5][27] = "12789,13689,14589,14679,15678,23589,23679,24579,24678,34569,34578";
        combinations[5][28] = "13789,14689,15679,23689,24589,24679,25678,34579,34678";
        combinations[5][29] = "14789,15689,23789,24689,25679,34589,34679,35678";
        combinations[5][30] = "15789,24789,25689,34689,35679,45678";
        combinations[5][31] = "16789,25789,34789,35689,45679";
        combinations[5][32] = "26789,35789,45689";
        combinations[5][33] = "36789,45789";
	combinations[5][34] = "46789";
        combinations[5][35] = "56789";
        combinations[6][21] = "123456";
        combinations[6][22] = "123457";
        combinations[6][23] = "123458,123467";
        combinations[6][24] = "123459,123468,123567";
        combinations[6][25] = "123469,123478,123568,124567";
        combinations[6][26] = "123479,123569,123578,124568,134567";
        combinations[6][27] = "123489,123579,123678,124569,124578,134568,234567";
        combinations[6][28] = "123589,123679,124579,124678,134569,134578,234568";
        combinations[6][29] = "123689,124589,124679,125678,134579,134678,234569,234578";
        combinations[6][30] = "123789,124689,125679,134589,134679,135678,234579,234678";
        combinations[6][31] = "124789,125689,134689,135679,145678,234589,234679,235678";
        combinations[6][32] = "125789,134789,135689,145679,234689,235679,245678";
        combinations[6][33] = "126789,135789,145689,234789,235689,245679,345678";
        combinations[6][34] = "136789,145789,235789,245689,345679";
        combinations[6][35] = "146789,236789,245789,345689";
        combinations[6][36] = "156789,246789,345789";
        combinations[6][37] = "256789,346789";
        combinations[6][38] = "356789";
        combinations[6][39] = "456789";
        combinations[7][28] = "1234567";
        combinations[7][29] = "1234568";
        combinations[7][30] = "1234569,1234578";
        combinations[7][31] = "1234579,1234678";
        combinations[7][32] = "1234589,1234679,1235678";
        combinations[7][33] = "1234689,1235679,1245678";
        combinations[7][34] = "1234789,1235689,1245679,1345678";
        combinations[7][35] = "1235789,1245689,1345679,2345678";
        combinations[7][36] = "1236789,1245789,1345689,2345679";
        combinations[7][37] = "1246789,1345789,2345689";
        combinations[7][38] = "1256789,1346789,2345789";
        combinations[7][39] = "1356789,2346789";
        combinations[7][40] = "1456789,2356789";
        combinations[7][41] = "2456789";
        combinations[7][42] = "3456789";
        combinations[8][36] = "12345678";
        combinations[8][37] = "12345679";
        combinations[8][38] = "12345689";
        combinations[8][39] = "12345789";
        combinations[8][40] = "12346789";
        combinations[8][41] = "12356789";
        combinations[8][42] = "12456789";
        combinations[8][43] = "13456789";
        combinations[8][44] = "23456789";
	combinations[9][45] = "123456789";
    }
}
