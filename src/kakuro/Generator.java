package kakuro;

import java.util.*;

public class Generator {
    private String[][] puzzleState;
    private int height;
    private int width;
    private String difficulty;
    private String symmetry;
    private boolean isSparse;
    private boolean doRefactoring;
    private boolean doBrute;
    private String[][] combinations;
    private ArrayList<String> puzzleWalls;
    private ArrayList<String> addedWalls;
    private ArrayList<String> puzzleCells;
    private ArrayList<String> invalidWalls;
    private ArrayList<String> invalidRemoveWalls;
    private boolean validPuzzle;
    private int genWallCount;
    private int regenNumberCount;
    private int sparseSolveCount;
    private boolean tooMuchRefactoring;
    private int emptySolveCount;
    private int clearNumbersCount;
    public String generate(int w, int h, String difficultySetting, String symmetrySetting, boolean sparse, boolean refactor, boolean brute,  String grid) {
        
        height = h;
        width = w;
        difficulty = difficultySetting;
        symmetry = symmetrySetting;
        isSparse = sparse;
        doRefactoring = refactor;
        doBrute = brute;
        puzzleWalls = new ArrayList<String>();
        addedWalls = new ArrayList<String>();
        puzzleCells = new ArrayList<String>();
        invalidWalls = new ArrayList<String>();
        invalidRemoveWalls = new ArrayList<String>();
        validPuzzle = true;
        regenNumberCount = 0;
        sparseSolveCount = 0;
        clearNumbersCount = 0;
        tooMuchRefactoring = false;
        String puzzleString = "";
        String puzzleString2 = "";
        
        //creates blank grid with walls on all sides
        puzzleState = new String[height][width];
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                if (x < 1 || y < 1 || y > (width - 2) || x > (height - 2)){
                    puzzleState[x][y] = "#";
                    puzzleWalls.add(x + "-" + y);
                }else{
                    puzzleState[x][y] = "@";
                    puzzleCells.add(x + "-" + y);
                }
            }
        }
        //checks for user inputted grid
        if(grid.equals("none")){
            generateGrid();
        }else{
            //takes user inputted grid string and puts into data structures
            String puzzString = grid;
            String[] splitPuzz = puzzString.split(":", 2);
            String puzzData = splitPuzz[1];
            String[] wxh = splitPuzz[0].split("x");
            String[] puzzData2 = puzzData.split(",");
            puzzleCells = new ArrayList<String>();
            puzzleWalls = new ArrayList<String>();
            addedWalls = new ArrayList<String>();
            int z = 0;
            for (int x=0;x < (height);x++) {
                for (int y=0; y < (width);y++) {
                    if (puzzData2[z].equals("@")){
                        puzzleState[x][y] = "@";
                        puzzleCells.add(x + "-" + y);
                    }else{
                        puzzleState[x][y] = "#";
                        puzzleWalls.add(x + "-" + y);
                        addedWalls.add(x + "-" + y);
                    }
                    z++;
                }
            }
        }
        //validates the generated or user inputted grid
        validPuzzle = validateGrid();
        
        //regenerates grid if invalid
        while (validPuzzle == false){
            System.out.println("regenerating");
            puzzleWalls = new ArrayList<String>();
            addedWalls = new ArrayList<String>();
            puzzleCells = new ArrayList<String>();
            invalidWalls = new ArrayList<String>();
            invalidRemoveWalls = new ArrayList<String>();
            puzzleState = new String[height][width];
            regenNumberCount = 0;
            tooMuchRefactoring = false;
            sparseSolveCount = 0;
            clearNumbersCount = 0;
            for (int x=0;x < (height);x++) {
                for (int y=0; y < (width);y++) {
                    if (x < 1 || y < 1 || y > (width - 2) || x > (height - 2)){
                        puzzleState[x][y] = "#";
                        puzzleWalls.add(x + "-" + y);
                    }else{
                        puzzleState[x][y] = "@";
                        puzzleCells.add(x + "-" + y);
                    }
                }
            }
            generateGrid();
            validPuzzle = validateGrid();
        }
        //fills grid with numbers
        generateSolution();
        
        //generates puzzle clues based on filled grid
        generateClues();
        
        //creates string representation of puzzle
        puzzleString = generatePuzzleString();
        
        //starts refactoring of puzzle until solved or limit is reached
        puzzleString2 = refactorSolution(puzzleString);
        
        //if the puzzle isn't complete, return none
        if(tooMuchRefactoring == false){
            //if dobrute is on, continue refactoring the puzzle to add or remove difficulty
            if(doBrute == false){
                return(puzzleString2);
            }else{
                String puzzleString3 = refactorClues(puzzleString2);
                return(puzzleString3);
            }
        }else{
            return("none");
        }
    }
    public String refactorClues(String puzzleString){
        String newPuzzleString = puzzleString;
        boolean validReplacement = false;
        int bruteCount = 0;
        Solver s = new Solver();
        String solution = s.solve(newPuzzleString, difficulty);
        String[] temp = solution.split(":");
        String isSolved = temp[0];
        String solutionString = temp[2];
        System.out.println(solutionString);
        int oldSolveCount = Integer.parseInt(temp[1]);
        int initSolveCount = Integer.parseInt(temp[1]);
        Collections.shuffle(puzzleCells);
        //clears puzzle clues
        while (bruteCount < puzzleCells.size()){
            for (int x=0;x < (height);x++) {
                for (int y=0; y < (width);y++) {
                    if (puzzleState[x][y].equals("#")){

                    }else if(puzzleState[x][y].contains(")")){
                        puzzleState[x][y] = "#";
                    }else{
                        
                    }
                }
            }
            //looks at each puzzle cell and finds all possible numbers that could replace the current number
            String cell = puzzleCells.get(bruteCount);
            String[] newSeedCell = cell.split("-");
            int x = Integer.parseInt(newSeedCell[0]);
            int y = Integer.parseInt(newSeedCell[1]);
            ArrayList<Integer> possibleNumbers = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));
            ArrayList<Integer> rowNumbers = new ArrayList<Integer>();
            ArrayList<Integer> columnNumbers = new ArrayList<Integer>();
            int rowTotal = 0;
            int columnTotal = 0;
            possibleNumbers.remove(Integer.valueOf(puzzleState[x][y]));
            //travels backward along rows and columns to remove candidates
            for (int x2=x;x2 > 0;x2--) {
                if(puzzleState[x2][y].equals("#")){
                    break;
                }else if(!puzzleState[x2][y].equals("@")){
                    possibleNumbers.remove(Integer.valueOf(puzzleState[x2][y]));
                    columnNumbers.add(Integer.valueOf(puzzleState[x2][y]));
                    columnTotal += Integer.valueOf(puzzleState[x2][y]);
                }
            }
            //travels forward as well
            for (int x2=x;x2 > 0;x2++) {
                if(puzzleState[x2][y].equals("#")){
                    break;
                }else if(!puzzleState[x2][y].equals("@")){
                    possibleNumbers.remove(Integer.valueOf(puzzleState[x2][y]));
                    columnNumbers.add(Integer.valueOf(puzzleState[x2][y]));
                    columnTotal += Integer.valueOf(puzzleState[x2][y]);
                }
            }
            for (int y2=y;y2 > 0;y2--) {
                if(puzzleState[x][y2].equals("#")){
                    break;
                }else if(!puzzleState[x][y2].equals("@")){
                    possibleNumbers.remove(Integer.valueOf(puzzleState[x][y2]));
                    rowNumbers.add(Integer.valueOf(puzzleState[x][y2]));
                    rowTotal += Integer.valueOf(puzzleState[x][y2]);
                }
            }
            for (int y2=y;y2 > 0;y2++) {
                if(puzzleState[x][y2].equals("#")){
                    break;
                }else if(!puzzleState[x][y2].equals("@")){
                    possibleNumbers.remove(Integer.valueOf(puzzleState[x][y2]));
                    rowNumbers.add(Integer.valueOf(puzzleState[x][y2]));
                    rowTotal += Integer.valueOf(puzzleState[x][y2]);
                }
            }
            //replaces the number in the current cell with each possible other number to test solvability and difficulty
            if(possibleNumbers.size() != 0){
                int possiblesCount = 0;
                Collections.shuffle(possibleNumbers);
                while (possiblesCount < possibleNumbers.size()){
                    String oldValue = puzzleState[x][y];
                    String newValue = String.valueOf(possibleNumbers.get(possiblesCount));
                    System.out.println("replacing " + oldValue +  " at " + x + "-" + y + "with " + newValue);
                    puzzleState[x][y] = newValue;
                    generateClues();
                    String tempPuzzleString = generatePuzzleString();
                    solution = s.solve(tempPuzzleString, difficulty);
                    temp = solution.split(":");
                    isSolved = temp[0];
                    int newSolveCount = Integer.parseInt(temp[1]);
                    solutionString = temp[2];
                    if(newSolveCount < oldSolveCount && isSolved.equals("true")){
                        oldSolveCount = newSolveCount;
                        newPuzzleString = tempPuzzleString;
                    }else{
                        puzzleState[x][y] = oldValue;
                    }
                    possiblesCount++;
                }
            }
            bruteCount++;
        }
        System.out.println("old solve count: " + initSolveCount +  " new solve count: " + oldSolveCount);
        return newPuzzleString;
    }
    public void generateGrid(){
        
        ArrayList<String> tempPuzzleCells = new ArrayList<String>(puzzleCells);
        Collections.shuffle(tempPuzzleCells);
        boolean validNewCell = false;
        //tests each cell to see if a wall can be placed
        for (String cell : tempPuzzleCells){
            //continues placing walls until a limit is reached
            if (((double)addedWalls.size() <= ((double)((height - 2) * (width - 2)) / 2.75) || (width * height > 400) || difficulty.equals("easy"))){
                if (!invalidWalls.contains(cell) && !addedWalls.contains(cell)){
                    String[] newSeedCell = cell.split("-");
                    int x = Integer.parseInt(newSeedCell[0]);
                    int y = Integer.parseInt(newSeedCell[1]);
                    validNewCell = validate(x, y);
                    if (validNewCell == true){
                        int x2 = height - 1 - x;
                        int y2 = width - 1 - y;
                        int x3 = y;
                        int y3 = width - 1 - x;
                        int x4 = height - 1 - y;
                        int y4 = x;

                        puzzleState[x][y] = "#";
                        puzzleWalls.add(x + "-" + y);
                        puzzleCells.remove(x + "-" + y);
                        addedWalls.add(x + "-" + y);
                        if(symmetry.equals("2rotational")){
                            puzzleState[x2][y2] = "#";
                            puzzleWalls.add(x2 + "-" + y2);                    
                            puzzleCells.remove(x2 + "-" + y2);
                            addedWalls.add(x2 + "-" + y2);
                        }
                        if(symmetry.equals("4rotational")){
                            puzzleState[x2][y2] = "#";
                            puzzleWalls.add(x2 + "-" + y2);                    
                            puzzleCells.remove(x2 + "-" + y2);
                            addedWalls.add(x2 + "-" + y2);
                            puzzleState[x3][y3] = "#";
                            puzzleWalls.add(x3 + "-" + y3);                    
                            puzzleCells.remove(x3 + "-" + 3);
                            addedWalls.add(x3 + "-" + y3);
                            puzzleState[x4][y4] = "#";
                            puzzleWalls.add(x4 + "-" + y4);                    
                            puzzleCells.remove(x4 + "-" + y4);
                            addedWalls.add(x4 + "-" + y4);
                        }
                        if(x == 2 && !addedWalls.contains((x - 1) + "-" + y)){
                            puzzleState[x - 1][y] = "#";
                            puzzleWalls.add((x - 1) + "-" + y);
                            addedWalls.add((x - 1) + "-" + y);
                            puzzleCells.remove((x - 1) + "-" + y);
                            if(symmetry.equals("2rotational")){
                                puzzleState[x2 + 1][y2] = "#";                           
                                puzzleWalls.add((x2 + 1) + "-" + y2);                         
                                addedWalls.add((x2 + 1) + "-" + y2);
                                puzzleCells.remove((x2 + 1) + "-" + y2);
                            }
                            if(symmetry.equals("4rotational")){
                                puzzleState[x2 + 1][y2] = "#";                           
                                puzzleWalls.add((x2 + 1) + "-" + y2);                         
                                addedWalls.add((x2 + 1) + "-" + y2);
                                puzzleCells.remove((x2 + 1) + "-" + y2);
                                puzzleState[x3 + 1][y3] = "#";                           
                                puzzleWalls.add((x3 + 1) + "-" + y3);                         
                                addedWalls.add((x3 + 1) + "-" + y3);
                                puzzleCells.remove((x3 + 1) + "-" + y3);
                                puzzleState[x4 + 1][y4] = "#";                           
                                puzzleWalls.add((x4 + 1) + "-" + y4);                         
                                addedWalls.add((x4 + 1) + "-" + y4);
                                puzzleCells.remove((x4 + 1) + "-" + y4);
                            }
                        }
                        if(x == (height - 3) && !addedWalls.contains((x + 1) + "-" + y)){
                            puzzleState[x + 1][y] = "#";
                            puzzleWalls.add((x + 1) + "-" + y);
                            addedWalls.add((x + 1) + "-" + y);
                            puzzleCells.remove((x + 1) + "-" + y);
                            if(symmetry.equals("2rotational")){
                                puzzleState[x2 - 1][y2] = "#";
                                puzzleWalls.add((x2 - 1) + "-" + y2);    
                                addedWalls.add((x2 - 1) + "-" + y2);              
                                puzzleCells.remove((x2 - 1) + "-" + y2);
                            }
                            if(symmetry.equals("4rotational")){
                                puzzleState[x2 - 1][y2] = "#";
                                puzzleWalls.add((x2 - 1) + "-" + y2);    
                                addedWalls.add((x2 - 1) + "-" + y2);              
                                puzzleCells.remove((x2 - 1) + "-" + y2);
                                puzzleState[x3 - 1][y3] = "#";
                                puzzleWalls.add((x3 - 1) + "-" + y3);    
                                addedWalls.add((x3 - 1) + "-" + y3);              
                                puzzleCells.remove((x3 - 1) + "-" + y3);
                                puzzleState[x4 - 1][y4] = "#";
                                puzzleWalls.add((x4 - 1) + "-" + y4);    
                                addedWalls.add((x4 - 1) + "-" + y4);              
                                puzzleCells.remove((x4 - 1) + "-" + y4);
                            }
                        }
                        if(y == 2 && !addedWalls.contains(x + "-" + (y - 1))){
                            puzzleState[x][y - 1] = "#";
                            puzzleWalls.add(x + "-" + (y - 1));
                            addedWalls.add(x + "-" + (y - 1));
                            puzzleCells.remove(x - 1 + "-" + (y - 1));
                            if(symmetry.equals("2rotational")){
                                puzzleState[x2][y2 + 1] = "#";
                                puzzleWalls.add(x2 + 1 + "-" + (y2 + 1));
                                addedWalls.add(x2 + 1 + "-" + (y2 + 1));
                                puzzleCells.remove(x2 + 1 + "-" + (y2 + 1));
                            }
                            if(symmetry.equals("4rotational")){
                                puzzleState[x2][y2 + 1] = "#";
                                puzzleWalls.add(x2 + 1 + "-" + (y2 + 1));
                                addedWalls.add(x2 + 1 + "-" + (y2 + 1));
                                puzzleCells.remove(x2 + 1 + "-" + (y2 + 1));
                                puzzleState[x3][y3 + 1] = "#";
                                puzzleWalls.add(x3 + 1 + "-" + (y3 + 1));
                                addedWalls.add(x3 + 1 + "-" + (y3 + 1));
                                puzzleCells.remove(x3 + 1 + "-" + (y3 + 1));
                                puzzleState[x4][y4 + 1] = "#";
                                puzzleWalls.add(x4 + 1 + "-" + (y4 + 1));
                                addedWalls.add(x4 + 1 + "-" + (y4 + 1));
                                puzzleCells.remove(x4 + 1 + "-" + (y4 + 1));
                            }
                        }
                        if(y == (width - 3) && !addedWalls.contains(x + "-" + (y + 1))){
                            puzzleState[x][y + 1] = "#";
                            puzzleWalls.add(x + "-" + (y + 1));
                            addedWalls.add(x + "-" + (y + 1));
                            puzzleCells.remove(x - 1 + "-" + (y + 1));
                            if(symmetry.equals("2rotational")){
                                puzzleState[x2][y2 - 1] = "#";
                                puzzleWalls.add(x2 + 1 + "-" + (y2 - 1));
                                addedWalls.add(x2 + 1 + "-" + (y2 - 1));
                                puzzleCells.remove(x2 + 1 + "-" + (y2 - 1));
                            }
                            if(symmetry.equals("4rotational")){
                                puzzleState[x2][y2 - 1] = "#";
                                puzzleWalls.add(x2 + 1 + "-" + (y2 - 1));
                                addedWalls.add(x2 + 1 + "-" + (y2 - 1));
                                puzzleCells.remove(x2 + 1 + "-" + (y2 - 1));
                                puzzleState[x3][y3 - 1] = "#";
                                puzzleWalls.add(x3 + 1 + "-" + (y3 - 1));
                                addedWalls.add(x3 + 1 + "-" + (y3 - 1));
                                puzzleCells.remove(x3 + 1 + "-" + (y3 - 1));
                                puzzleState[x4][y4 - 1] = "#";
                                puzzleWalls.add(x4 + 1 + "-" + (y4 - 1));
                                addedWalls.add(x4 + 1 + "-" + (y4 - 1));
                                puzzleCells.remove(x4 + 1 + "-" + (y4 - 1));
                            }
                        }
                    }
                }
            }
        }
        if(isSparse == true){
            int count = 0;
            ArrayList<String> tempAddedWalls = new ArrayList<String>(addedWalls);
            Collections.shuffle(tempAddedWalls);
            boolean validRemoveWall = false;
            for (String wall : tempAddedWalls){
                int numWallsToRemove = (int)(Math.round((height + width) / 4));
                if (!invalidRemoveWalls.contains(wall) && count%numWallsToRemove == 0){
                    String[] wallCell = wall.split("-");
                    int x = Integer.parseInt(wallCell[0]);
                    int y = Integer.parseInt(wallCell[1]);
                    int x2 = height - 1 - x;
                    int y2 = width - 1 - y;
                    int x3 = y;
                    int y3 = width - 1 - x;
                    int x4 = height - 1 - y;
                    int y4 = x;
                    
                    validRemoveWall = validateWallRemove(x, y);
                    if (validRemoveWall == true){
                        System.out.println("removing " + x + "-" + y);
                        puzzleState[x][y] = "@";
                        puzzleWalls.remove(x + "-" + y);
                        puzzleCells.add(x + "-" + y);
                        addedWalls.remove(x + "-" + y);
                        invalidRemoveWalls.add((x + 1) + "-" + y);
                        invalidRemoveWalls.add((x - 1) + "-" + y);
                        invalidRemoveWalls.add(x + "-" + (y + 1));
                        invalidRemoveWalls.add(x + "-" + (y - 1));
                        invalidRemoveWalls.add((x + 1) + "-" + (y + 1));
                        invalidRemoveWalls.add((x - 1) + "-" + (y - 1));
                        invalidRemoveWalls.add((x - 1) + "-" + (y + 1));
                        invalidRemoveWalls.add((x + 1) + "-" + (y - 1));
                        if(symmetry.equals("2rotational")){
                            puzzleState[x2][y2] = "@";
                            puzzleWalls.remove(x2 + "-" + y2);                    
                            puzzleCells.add(x2 + "-" + y2);
                            addedWalls.remove(x2 + "-" + y2);
                            invalidRemoveWalls.add((x2 + 1) + "-" + y2);
                            invalidRemoveWalls.add((x2 - 1) + "-" + y2);
                            invalidRemoveWalls.add(x2 + "-" + (y2 + 1));
                            invalidRemoveWalls.add(x2 + "-" + (y2 - 1));
                            invalidRemoveWalls.add((x2 + 1) + "-" + (y2 + 1));
                            invalidRemoveWalls.add((x2 - 1) + "-" + (y2 - 1));
                            invalidRemoveWalls.add((x2 - 1) + "-" + (y2 + 1));
                            invalidRemoveWalls.add((x2 + 1) + "-" + (y2 - 1));
                        }
                        if(symmetry.equals("4rotational")){
                            puzzleState[x2][y2] = "@";
                            puzzleWalls.remove(x2 + "-" + y2);                    
                            puzzleCells.add(x2 + "-" + y2);
                            addedWalls.remove(x2 + "-" + y2);
                            invalidRemoveWalls.add((x2 + 1) + "-" + y2);
                            invalidRemoveWalls.add((x2 - 1) + "-" + y2);
                            invalidRemoveWalls.add(x2 + "-" + (y2 + 1));
                            invalidRemoveWalls.add(x2 + "-" + (y2 - 1));
                            invalidRemoveWalls.add((x2 + 1) + "-" + (y2 + 1));
                            invalidRemoveWalls.add((x2 - 1) + "-" + (y2 - 1));
                            invalidRemoveWalls.add((x2 - 1) + "-" + (y2 + 1));
                            invalidRemoveWalls.add((x2 + 1) + "-" + (y2 - 1));
                            puzzleState[x3][y3] = "@";
                            puzzleWalls.remove(x3 + "-" + y3);                    
                            puzzleCells.add(x3 + "-" + y3);
                            addedWalls.remove(x3 + "-" + y3);
                            invalidRemoveWalls.add((x3 + 1) + "-" + y3);
                            invalidRemoveWalls.add((x3 - 1) + "-" + y3);
                            invalidRemoveWalls.add(x3 + "-" + (y3 + 1));
                            invalidRemoveWalls.add(x3 + "-" + (y3 - 1));
                            invalidRemoveWalls.add((x3 + 1) + "-" + (y3 + 1));
                            invalidRemoveWalls.add((x3 - 1) + "-" + (y3 - 1));
                            invalidRemoveWalls.add((x3 - 1) + "-" + (y3 + 1));
                            invalidRemoveWalls.add((x3 + 1) + "-" + (y3 - 1));
                            puzzleState[x4][y4] = "@";
                            puzzleWalls.remove(x4 + "-" + y4);                    
                            puzzleCells.add(x4 + "-" + y4);
                            addedWalls.remove(x4 + "-" + y4);
                            invalidRemoveWalls.add((x4 + 1) + "-" + y4);
                            invalidRemoveWalls.add((x4 - 1) + "-" + y4);
                            invalidRemoveWalls.add(x4 + "-" + (y4 + 1));
                            invalidRemoveWalls.add(x4 + "-" + (y4 - 1));
                            invalidRemoveWalls.add((x4 + 1) + "-" + (y4 + 1));
                            invalidRemoveWalls.add((x4 - 1) + "-" + (y4 - 1));
                            invalidRemoveWalls.add((x4 - 1) + "-" + (y4 + 1));
                            invalidRemoveWalls.add((x4 + 1) + "-" + (y4 - 1));
                        }
                    }else{
                        puzzleState[x][y] = "#";
                        if(symmetry.equals("2rotational")){
                            puzzleState[x2][y2] = "#";
                        }
                        if(symmetry.equals("4rotational")){
                            puzzleState[x2][y2] = "#";
                            puzzleState[x3][y3] = "#";
                            puzzleState[x4][y4] = "#";
                        }
                    }
                }
                count++;
            }
        }
        System.out.println(puzzleWalls.size());
        HashSet hs = new HashSet();
        hs.addAll(puzzleWalls);
        puzzleWalls.clear();
        puzzleWalls.addAll(hs);
        System.out.println(puzzleWalls.size());
        for (int i=0;i < (height);i++) {
            for (int j=0; j < (width);j++) {
                if (puzzleWalls.contains(i + "-" + j) && !puzzleState[i][j].equals("#")){
                    System.out.println("error");
                }
                if (puzzleCells.contains(i + "-" + j) && !puzzleState[i][j].equals("@")){
                    System.out.println("error 2 at " + i +"-" + j);
                }
            }
        }
    }
    public boolean validateWallRemove(int x, int y){
        boolean valid = true;
        ArrayList<String> tempWalls = new ArrayList<String>(puzzleWalls);
        ArrayList<String> tempCells = new ArrayList<String>(puzzleCells);
        int x2 = 0;
        int y2 = 0;
        int x3 = 0;
        int y3 = 0;
        int x4 = 0;
        int y4 = 0;
        if(symmetry.equals("2rotational")){
            x2 = height - 1 - x;
            y2 = width - 1 - y;
        }
        if(symmetry.equals("4rotational")){
            x2 = height - 1 - x;
            y2 = width - 1 - y;
            x3 = y;
            y3 = width - 1 - x;
            x4 = height - 1 - y;
            y4 = x;
        }
        tempWalls.remove(x + "-" + y);
        tempCells.add(x + "-" + y);
        if(symmetry.equals("2rotational")){
            tempWalls.remove(x2 + "-" + y2);
            tempCells.add(x2 + "-" + y2);
        }
        if(symmetry.equals("4rotational")){
            tempWalls.remove(x2 + "-" + y2);
            tempCells.add(x2 + "-" + y2);
            tempWalls.remove(x3 + "-" + y3);
            tempCells.add(x3 + "-" + y3);
            tempWalls.remove(x4 + "-" + y4);
            tempCells.add(x4 + "-" + y4);
        }
        puzzleState[x][y] = "@";
        if(symmetry.equals("2rotational")){
            puzzleState[x2][y2] = "@";
        }
        if(symmetry.equals("4rotational")){
            puzzleState[x2][y2] = "@";
            puzzleState[x3][y3] = "@";
            puzzleState[x4][y4] = "@";
        }
        valid = validateGrid();
        for (int i=0;i < (height);i++) {
            for (int j=0; j < (width);j++) {
                if(tempCells.contains(i + "-" + j) == true && tempWalls.contains(i + "-" + j) == false){
                    if (tempWalls.contains((i + 1) + "-" + j) == true && tempWalls.contains((i - 1) + "-" + j) == true){
                        valid = false;
                    }
                    if (tempWalls.contains(i + "-" + (j + 1)) == true && tempWalls.contains(i + "-" + (j - 1)) == true){
                        valid = false;
                    }
                }
            }
        }
        
        ArrayList<String> visitedCells = new ArrayList<String>();
        boolean found = false;
        for (int i=0;i < (height);i++) {
            for (int j=0; j < (width);j++) {
                if(puzzleState[i][j].equals("@") && found == false){
                    found = true;
                    travelCells(i, j, visitedCells, tempCells);
                }
            }
        }
        
        for (String cell: tempCells){
            if (!visitedCells.contains(cell)){
                valid = false;
            }
        }
        if (valid == false){
            invalidRemoveWalls.add(x + "-" + y);
            if(symmetry.equals("2rotational")){
                invalidRemoveWalls.add(x2 + "-" + y2);
            }
            if(symmetry.equals("4rotational")){
                invalidRemoveWalls.add(x2 + "-" + y2);
                invalidRemoveWalls.add(x3 + "-" + y3);
                invalidRemoveWalls.add(x4 + "-" + y4);
            }
        }
        return valid;
    }
    public boolean validate(int x, int y){
        boolean valid = true;
        boolean addedExtraWall = false;
        int x2 = height - 1 - x;
        int y2 = width - 1 - y;
        int x3 = y;
        int y3 = width - 1 - x;
        int x4 = height - 1 - y;
        int y4 = x;
        //calculates symmetries of cell to validate

        ArrayList<String> tempWalls;
        tempWalls = new ArrayList<String>(puzzleWalls);
        tempWalls.add(x + "-" + y);
        if(symmetry.equals("2rotational")){
            tempWalls.add(x2 + "-" + y2);
        }
        if(symmetry.equals("4rotational")){
            tempWalls.add(x2 + "-" + y2);
            tempWalls.add(x3 + "-" + y3);
            tempWalls.add(x4 + "-" + y4);
        }
        ArrayList<String> tempCells;
        tempCells = new ArrayList<String>(puzzleCells);
        tempCells.remove(x + "-" + y);
        if(symmetry.equals("2rotational")){
            tempCells.remove(x2 + "-" + y2);
        }
        if(symmetry.equals("4rotational")){
            tempCells.remove(x2 + "-" + y2);
            tempCells.remove(x3 + "-" + y3);
            tempCells.remove(x4 + "-" + y4);
        }
        //adds a second wall if first wall is placed 2 cells away from edge
        
        if(x == 2 && !tempWalls.contains((x - 1) + "-" + y)){
            tempWalls.add((x - 1) + "-" + y);
            tempCells.remove((x - 1) + "-" + y);
            if(symmetry.equals("2rotational")){
                tempWalls.add((x2 + 1) + "-" + y2);
                tempCells.remove((x2 + 1) + "-" + y2);
            }
            if(symmetry.equals("4rotational")){
                tempWalls.add((x2 + 1) + "-" + y2);
                tempCells.remove((x2 + 1) + "-" + y2);
                tempWalls.add((x3 + 1) + "-" + y3);
                tempCells.remove((x3 + 1) + "-" + y3);
                tempWalls.add((x4 + 1) + "-" + y4);
                tempCells.remove((x4 + 1) + "-" + y4);
            }
            addedExtraWall = true;
        }
        if(x == (height - 3) && !tempWalls.contains((x + 1) + "-" + y)){
            tempWalls.add((x + 1) + "-" + y);
            tempCells.remove((x + 1) + "-" + y);
            if(symmetry.equals("2rotational")){
                tempWalls.add((x2 - 1) + "-" + y2);
                tempCells.remove((x2 - 1) + "-" + y2);
            }
            if(symmetry.equals("4rotational")){
                tempWalls.add((x2 - 1) + "-" + y2);
                tempCells.remove((x2 - 1) + "-" + y2);
                tempWalls.add((x3 - 1) + "-" + y3);
                tempCells.remove((x3 - 1) + "-" + y3);
                tempWalls.add((x4 - 1) + "-" + y4);
                tempCells.remove((x4 - 1) + "-" + y4);
            }
            addedExtraWall = true;
        }
        if(y == 2 && !tempWalls.contains(x + "-" + (y - 1))){
            tempWalls.add(x + "-" + (y - 1));
            tempCells.remove(x - 1 + "-" + (y - 1));
            if(symmetry.equals("2rotational")){
                tempWalls.add(x2 + 1 + "-" + (y2 + 1));
                tempCells.remove(x2 + 1 + "-" + (y2 + 1));
            }
            if(symmetry.equals("4rotational")){
                tempWalls.add(x2 + 1 + "-" + (y2 + 1));
                tempCells.remove(x2 + 1 + "-" + (y2 + 1));
                tempWalls.add(x3 + 1 + "-" + (y3 + 1));
                tempCells.remove(x3 + 1 + "-" + (y3 + 1));
                tempWalls.add(x4 + 1 + "-" + (y4 + 1));
                tempCells.remove(x4 + 1 + "-" + (y4 + 1));
            }
            addedExtraWall = true;
        }
        if(y == (width - 3) && !tempWalls.contains(x + "-" + (y + 1))){
            tempWalls.add(x + "-" + (y + 1));
            tempCells.remove(x - 1 + "-" + (y + 1));
            if(symmetry.equals("2rotational")){
                tempWalls.add(x2 + 1 + "-" + (y2 - 1)); 
                tempCells.remove(x2 + 1 + "-" + (y2 - 1));
            }
            if(symmetry.equals("4rotational")){
                tempWalls.add(x2 + 1 + "-" + (y2 - 1)); 
                tempCells.remove(x2 + 1 + "-" + (y2 - 1));
                tempWalls.add(x3 + 1 + "-" + (y3 - 1)); 
                tempCells.remove(x3 + 1 + "-" + (y3 - 1));
                tempWalls.add(x4 + 1 + "-" + (y4 - 1)); 
                tempCells.remove(x4 + 1 + "-" + (y4 - 1));
            }
            addedExtraWall = true;
        }
        //prevents 1 cell long series
        for (int i=0;i < (height);i++) {
            for (int j=0; j < (width);j++) {
                if(puzzleState[i][j].equals("@") && tempWalls.contains(i + "-" + j) == false){
                    if (tempWalls.contains((i + 1) + "-" + j) == true && tempWalls.contains((i - 1) + "-" + j) == true){
                        valid = false;
                    }
                    if (tempWalls.contains(i + "-" + (j + 1)) == true && tempWalls.contains(i + "-" + (j - 1)) == true){
                        valid = false;
                    }
                }
            }
        }
        int surroundingWallCount = 0;
        if (valid == true){
            if(tempWalls.contains((x + 1) + "-" + y)){
                surroundingWallCount++;
            }
            if(tempWalls.contains((x - 1) + "-" + y)){
                surroundingWallCount++;
            }
            if(tempWalls.contains(x + "-" + (y + 1))){
                surroundingWallCount++;
            }
            if(tempWalls.contains(x + "-" + (y - 1))){
                surroundingWallCount++;
            }
            if(tempWalls.contains((x + 1) + "-" + (y + 1))){
                surroundingWallCount++;
            }
            if(tempWalls.contains((x - 1) + "-" + (y - 1))){
                surroundingWallCount++;
            }
            if(tempWalls.contains((x + 1) + "-" + (y - 1))){
                surroundingWallCount++;
            }
            if(tempWalls.contains((x - 1) + "-" + (y + 1))){
                surroundingWallCount++;
            }
            //maintains connectivity in puzzle
            //travels first section of cells it finds
            if(surroundingWallCount > 1 || addedExtraWall == true){
                ArrayList<String> visitedCells = new ArrayList<String>();
                boolean found = false;
                for (int i=0;i < (height);i++) {
                    for (int j=0; j < (width);j++) {
                        if(puzzleState[i][j].equals("@") && found == false){
                            found = true;
                            travelCells(i, j, visitedCells, tempCells);
                        }
                    }
                }

                for (String cell: tempCells){
                    if (!visitedCells.contains(cell)){
                        valid = false;
                        //System.out.println(visitedCells);
                        //System.out.println(tempCells);
                    }
                }
            }
        }
        //if hard, walls can't be touching more than 4 other walls
        if(difficulty.equals("hard") || difficulty.equals("max")){
            if (surroundingWallCount > 3){
                //valid = false;
            }
        }
        
        if (valid == false){
            invalidWalls.add(x + "-" + y);
            if(symmetry.equals("2rotational")){
                invalidWalls.add(x2 + "-" + y2);
            }
            if(symmetry.equals("4rotational")){
                invalidWalls.add(x2 + "-" + y2);
                invalidWalls.add(x3 + "-" + y3);
                invalidWalls.add(x4 + "-" + y4);
            }
        }
        return valid;
    }
    public void travelCells(int xCoord, int yCoord, ArrayList<String> visitedCells, ArrayList<String> tempCells){
        int x = xCoord;
        int y = yCoord;
        
        boolean continueTraveling = true;
        while(continueTraveling == true){
            
            if (!visitedCells.contains(x + "-" + y)){
                visitedCells.add(x + "-" + y);
            }
            if(puzzleState[x + 1][y].equals("@") && !visitedCells.contains((x + 1) + "-" + y) && tempCells.contains((x + 1) + "-" + y)){
                x++;
            }else if(puzzleState[x - 1][y].equals("@") && !visitedCells.contains((x - 1) + "-" + y) && tempCells.contains((x - 1) + "-" + y)){
                x--;
            }else if(puzzleState[x][y + 1].equals("@") && !visitedCells.contains(x + "-" + (y + 1)) && tempCells.contains(x + "-" + (y + 1))){
                y++;
            }else if(puzzleState[x][y - 1].equals("@") && !visitedCells.contains(x + "-" + (y - 1)) && tempCells.contains(x + "-" + (y - 1))){
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
    public void generateSolution(){
        regenNumberCount++;
        ArrayList<String> emptyCells = new ArrayList<String>();
        Collections.shuffle(puzzleCells);
        for (String cell : puzzleCells){
            String[] newSeedCell = cell.split("-");
            int x = Integer.parseInt(newSeedCell[0]);
            int y = Integer.parseInt(newSeedCell[1]);
            if(puzzleState[x][y].equals("@") && validPuzzle == true){
                emptyCells.add(x + "-" + y);
                ArrayList<Integer> possibleNumbers = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));
                ArrayList<Integer> rowNumbers = new ArrayList<Integer>();
                ArrayList<Integer> columnNumbers = new ArrayList<Integer>();
                int rowTotal = 0;
                int columnTotal = 0;
                //travels backward along rows and columns to remove candidates
                for (int x2=x;x2 > 0;x2--) {
                    if(puzzleState[x2][y].equals("#")){
                        break;
                    }else if(!puzzleState[x2][y].equals("@")){
                        possibleNumbers.remove(Integer.valueOf(puzzleState[x2][y]));
                        columnNumbers.add(Integer.valueOf(puzzleState[x2][y]));
                        columnTotal += Integer.valueOf(puzzleState[x2][y]);
                    }
                }
                //travels forward as well
                for (int x2=x;x2 > 0;x2++) {
                    if(puzzleState[x2][y].equals("#")){
                        break;
                    }else if(!puzzleState[x2][y].equals("@")){
                        possibleNumbers.remove(Integer.valueOf(puzzleState[x2][y]));
                        columnNumbers.add(Integer.valueOf(puzzleState[x2][y]));
                        columnTotal += Integer.valueOf(puzzleState[x2][y]);
                    }
                }
                for (int y2=y;y2 > 0;y2--) {
                    if(puzzleState[x][y2].equals("#")){
                        break;
                    }else if(!puzzleState[x][y2].equals("@")){
                        possibleNumbers.remove(Integer.valueOf(puzzleState[x][y2]));
                        rowNumbers.add(Integer.valueOf(puzzleState[x][y2]));
                        rowTotal += Integer.valueOf(puzzleState[x][y2]);
                    }
                }
                for (int y2=y;y2 > 0;y2++) {
                    if(puzzleState[x][y2].equals("#")){
                        break;
                    }else if(!puzzleState[x][y2].equals("@")){
                        possibleNumbers.remove(Integer.valueOf(puzzleState[x][y2]));
                        rowNumbers.add(Integer.valueOf(puzzleState[x][y2]));
                        rowTotal += Integer.valueOf(puzzleState[x][y2]);
                    }
                }
                Iterator<Integer> it = possibleNumbers.iterator();
                while (it.hasNext()) {
                    int possibility = it.next();
                    if(puzzleState[x - 1][y - 1].equals(Integer.toString(possibility)) && puzzleState[x - 1][y].equals(puzzleState[x][y - 1]) && !puzzleState[x][y - 1].equals("#")){
                        it.remove();
                        break;
                    }
                }
                //Collections.shuffle(possibleNumbers);
                //chooses a number for cell
                if (possibleNumbers.size() != 0){
                    
                    Random rand = new Random();
                    int chanceOfLargeOrSmall = rand.nextInt(2);
                    int largeOrSmall = 2;
                    int sizeToTrim = rand.nextInt(3);
                    if(chanceOfLargeOrSmall == 1){
                        if((rowNumbers.size() + columnNumbers.size()) != 0){
                            if ((rowTotal + columnTotal) / (rowNumbers.size() + columnNumbers.size()) > 5){
                                largeOrSmall = 0;
                            }else{
                                largeOrSmall = 1;
                            }
                        }
                    }
                    
                    if (possibleNumbers.size() > 2){
                        if (sizeToTrim == 1){
                            possibleNumbers.remove(0);
                            possibleNumbers.remove(possibleNumbers.size() - 1);
                        }
                    }
                    String choice = "";
                    if(largeOrSmall == 1){
                        choice = Integer.toString(possibleNumbers.get(0));
                    }else if(largeOrSmall == 0){
                        choice = Integer.toString(possibleNumbers.get(possibleNumbers.size() - 1));
                    }else{
                        Collections.shuffle(possibleNumbers);
                        choice = Integer.toString(possibleNumbers.get(0));
                    }
                    /*
                    Collections.shuffle(possibleNumbers);
                    String choice = Integer.toString(possibleNumbers.get(0));
                    */
                    puzzleState[x][y] = choice;
                }else{
                    clearNumbersCount++;
                    //System.out.println("regenerating numbers");
                    
                    for (int x2=0;x2 < (height);x2++) {
                        for (int y2=0; y2 < (width);y2++) {
                            if (puzzleState[x2][y2].contains("(") || puzzleState[x2][y2].equals("#")){
                                //System.out.print("#");
                            }else if(emptyCells.contains(x2 + "-" + y2) == true){
                                puzzleState[x2][y2] = "@";
                                //System.out.print("@");
                            }else{
                                //System.out.print(puzzleState[x2][y2]);
                            }
                        }
                        //System.out.println("");
                    }
                    generateSolution();
                }
            }
        }

    }
    public void generateClues(){
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                if(puzzleState[x][y].equals("#")){
                    int z = 1;
                    int seriesCount = 0;
                    while (z < 10){
                        if((x + z) != height && !puzzleState[x + z][y].equals("@") && !puzzleState[x + z][y].equals("#") && !puzzleState[x + z][y].contains("(")){
                            seriesCount += Integer.valueOf(puzzleState[x + z][y]);
                            z++;
                        }else{
                            break;
                        }
                    }
                    if (seriesCount != 0){
                        if(!puzzleState[x][y].contains("(")){
                            puzzleState[x][y] = "(" + seriesCount + ")";
                        }
                    }
                    z = 1;
                    seriesCount = 0;
                    while (z < 10){
                        if((y + z) != width && !puzzleState[x][y + z].equals("@") && !puzzleState[x][y + z].equals("#") && !puzzleState[x][y + z].contains("(")){
                            seriesCount += Integer.valueOf(puzzleState[x][y + z]);
                            z++;
                        }else{
                            break;
                        }
                    }
                    if (seriesCount != 0){
                        if(!puzzleState[x][y].contains("(")){
                            puzzleState[x][y] = "(" + seriesCount + ")";
                        }else{
                            String otherValue = puzzleState[x][y].substring(1, puzzleState[x][y].length() - 1);
                            puzzleState[x][y] = "(" + otherValue + "-" + seriesCount + ")";
                        }
                    }
                }
            }
        }
    }
    public String refactorSolution(String puzzleString){
        String puzzleString2 = puzzleString;
        System.out.println(puzzleString);
        Solver s = new Solver();
        String solution = s.solve(puzzleString, difficulty);
        String[] temp = solution.split(":");
        String isSolved = temp[0];
        solution = temp[2];
        while (!isSolved.equals("true") && tooMuchRefactoring == false){
            int index = solution.indexOf("@");
            int emptyCount = 0;
            while (index >= 0) {
                emptyCount++;
                index = solution.indexOf("@", index + 1);
            }
            float sparsity = (float)emptyCount / (solution.length() / 2);
            System.out.println(emptyCount + " " + (solution.length() / 2) + " " + sparsity);
            if (sparsity > .7){
                sparseSolveCount++;
            }
            if (sparsity > .9){
                emptySolveCount++;
            }
            if(difficulty.equals("hard") || difficulty.equals("max")){
                if(sparseSolveCount * height * width > 2000 || regenNumberCount * height * width > 10000 || emptySolveCount * height * width > 1 || clearNumbersCount > 2){
                    if(doRefactoring == true){
                        tooMuchRefactoring = true;
                    }
                }
            }else{
                if(sparseSolveCount * height * width > 2500 || regenNumberCount * height * width > 25000 || emptySolveCount * height * width > 1000 || clearNumbersCount > 2){
                    if(doRefactoring == true){
                        tooMuchRefactoring = true;
                    }
                }
            }

            temp = solution.split(",");
            
            int z = 0;
            for (int x=0;x < (height);x++) {
                for (int y=0; y < (width);y++) {
                    if (!puzzleState[x][y].equals("#") && !puzzleState[x][y].contains("(")){
                        puzzleState[x][y] = temp[z];
                        z++;
                    }
                    if (puzzleState[x][y].contains("(")){
                        puzzleState[x][y] = "#";
                    }
                }
            }
            System.out.println("refactoring");
            generateSolution();
            generateClues();
            
            puzzleString2 = generatePuzzleString();
            System.out.println(puzzleString2);
            String solution2 = s.solve(puzzleString2, difficulty);
            temp = solution2.split(":");
            isSolved = temp[0];
            solution = temp[2];
        }
        return puzzleString2;
    }
    public String generatePuzzleString(){
        String puzzleString = width + "x" + height + ":";
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                if(puzzleState[x][y].equals("#") || puzzleState[x][y].equals("@") || puzzleState[x][y].contains("(")){
                    puzzleString += puzzleState[x][y] + ",";
                }else{
                    puzzleString += "@,";
                }
            }
        }
        puzzleString = puzzleString.substring(0, puzzleString.length() - 1);
        return puzzleString;
    }
    public boolean validateGrid(){
        for (int x=0;x < (height);x++) {
            for (int y=0; y < (width);y++) {
                if(puzzleState[x][y].equals("#")){
                    int z = 1;
                    while (z < 11){
                        if((x + z) != height && puzzleState[x + z][y].equals("@") && !puzzleState[x + z][y].equals("#") && !puzzleState[x + z][y].contains("(")){
                            z++;
                        }else{
                            break;
                        }
                    }
                    if (z == 11){
                        return false;
                    }

                    z = 1;
                    while (z < 11){
                        if((y + z) != width && puzzleState[x][y + z].equals("@") && !puzzleState[x][y + z].equals("#") && !puzzleState[x][y + z].contains("(")){
                            z++;
                        }else{
                            break;
                        }
                    }
                    if (z == 11){
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
