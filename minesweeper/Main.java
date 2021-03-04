package minesweeper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        
        System.out.println("How many mines do you want on the field?");
        
        Scanner scanner = new Scanner(System.in);
        int n = Integer.parseInt(scanner.nextLine());

        Random random = new Random(17);
        Set<Integer> mineSet = new HashSet<>();

        while (mineSet.size() < n) {
            int i = random.nextInt(81);
            mineSet.add(i);
        }


//        mineSet = Set.of(6,9,23,46,49,56,60,64,67,75);

        String[][] field = new String[9][9];
        IntStream.range(0, 81).forEach(i -> field[i / 9][i % 9] = ".");
/*
        mineSet.stream().forEach(i -> field[i / 9][i % 9] = "X");
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!"X".equals(field[i][j])) {
                    long number = getNumber(i, j, field);
                    field[i][j] = number != 0 ? number + "" : ".";
                }
            }
        }
        mineSet.stream().forEach(i -> field[i / 9][i % 9] = ".");
*/

        Game game = new Game(scanner, field, mineSet);

        game.play();

        scanner.close();
    }

    static long getNumber(int i, int j, String[][] field) {

        List<Position> list = Position.getSoround(i, j);
        return list.stream().filter(p -> "X".equals(field[p.getRow()][p.getCol()])).collect(Collectors.counting());
    }
}

enum Result {

    SUCCESS,
    FAILURE,
    CONTINUE,
    REINPUT
}

class Game {

    Scanner scanner;
    String[][] field;
    Set<Integer> mineSet;
    boolean initPlayFlag = true;

    Game(Scanner scanner, String[][] field, Set<Integer> mineSet) {
        this.scanner = scanner;
        this.field = field;
        this.mineSet = mineSet;
    }

    void play() {
        printField(Result.CONTINUE);
        Result result = Result.CONTINUE;
        while (result == Result.CONTINUE) {
            result = mark();
            initPlayFlag = false;
            hint(result);
            printField(result);
            result = judge(result);
            switch (result) {
                case SUCCESS:
                    System.out.println("Congratulations! You found all the mines!");
                    break;
                case FAILURE:
                    System.out.println("You stepped on a mine and failed!");
                    break;
                case CONTINUE:
                    break;
                case REINPUT:
                    break;
            }
        }    
    }

    void hint(Result result) {
        if (result == Result.FAILURE) {
            return;
        }
        long emptyCount = 1;
        long newEmptyCount = 0;

        while (emptyCount != newEmptyCount) {
            emptyCount = IntStream.range(0, 81).filter(i -> "/".equals(field[i / 9][i % 9])).count();
            IntStream.range(0, 81).filter(i -> "/".equals(field[i / 9][i % 9])).forEach(i -> oneHint(i / 9, i % 9));
            newEmptyCount = IntStream.range(0, 81).filter(i -> "/".equals(field[i / 9][i % 9])).count();
        }
    }

    void oneHint(int row, int col) {
        List<Position> list = Position.getSoround(row, col);
        list.stream().filter(p -> !isMine(p)).forEach(p -> {long number = getNumber(p); field[p.getRow()][p.getCol()] = number == 0 ? "/" : number + "";});
    }

    long getNumber(int row, int col) {
        List<Position> list = Position.getSoround(row, col);
        return list.stream().filter(p -> isMine(p)).count();
    }

    long getNumber(Position p) {
        return getNumber(p.getRow(), p.getCol());
    }

    boolean isMine(int row, int col) {
        return mineSet.contains(row * 9 + col);
    }

    boolean isMine(Position p) {
        return isMine(p.getRow(), p.getCol());
    }

    Result judge(Result result) {
        if (result == Result.FAILURE) {
            return result;
        }
        if (IntStream.range(0, 81).filter(i -> "*".equals(field[i / 9][i % 9])).count() == mineSet.size()) return Result.SUCCESS;
        if (IntStream.range(0, 81).filter(i -> "*".equals(field[i / 9][i % 9])).count() == 0 && 
            IntStream.range(0, 81).filter(i -> ".".equals(field[i / 9][i % 9])).count() == mineSet.size()) return Result.SUCCESS;
        if (mineSet.stream().filter(i -> "/".equals(field[i / 9][i % 9])).count() > 0) return Result.FAILURE;
        return Result.CONTINUE;
    }

    Result mark() {
        Result result = Result.REINPUT;
        while (result == Result.REINPUT) {
            System.out.println("Set/unset mines marks or claim a cell as free:");
            String[] items = scanner.nextLine().split("\\s+");
            int col = Integer.parseInt(items[0]) - 1;
            int row = Integer.parseInt(items[1]) - 1;
            String command = items[2];
            switch (command) {
                case "mine":
                    result = markMine(row, col);
                    break;
                case "free":
                    result = markFree(row, col);
                    break;
                default:
                    result = Result.REINPUT;
                    break;
            }
        }
        return result;
    }

    Result markMine(int row, int col) {
        if (".".equals(field[row][col])) {
            field[row][col] = "*";
            return Result.CONTINUE;
        } else if ("*".equals(field[row][col])) {
            field[row][col] = ".";
            return Result.CONTINUE;
        } else {
            System.out.println("There is a number here!");
            return Result.REINPUT;
        }
    }

    Result markFree(int row, int col) {
        if (isMine(row, col)) {
            if (initPlayFlag) {
                changeMine(row, col);
            } else {
                return Result.FAILURE;
            }
        }
        long number = getNumber(row, col);
        field[row][col] = number == 0 ? "/" : number + "";  
        return Result.CONTINUE;   
    }

    void changeMine(int row, int col) {
        int index = row * 9 + col;
        for (int i = 0; i < 81; i++) {
            if (!mineSet.contains(i)) {
                mineSet.add(i);
                break;
            }
        }
        mineSet.remove(index);
    }

    void printField(Result result) {
        System.out.println();
        System.out.println(" │123456789│");
        System.out.println("—│—————————│");
        for (int i = 0; i < 9; i++) {
            System.out.print((i + 1) + "│");
            for (int j = 0; j < 9; j++) {
                if (result == Result.FAILURE && isMine(i, j)) {
                    System.out.print("X");
                } else {
                    System.out.print(field[i][j]);
                }
            }
            System.out.println("│");
        }
        System.out.println("—│—————————│");
    }
}

class Position {

    static int limit = 9;
    int row;
    int col;

    Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    int getRow() {
        return row;
    }

    int getCol() {
        return col;
    }

    static List<Position> getSoround(int row, int col) {
        Position leftUp = new Position(row - 1, col - 1);
        Position up = new Position(row - 1, col);
        Position rightUp = new Position(row - 1, col + 1);
        Position left = new Position(row, col - 1);
        Position right = new Position(row, col + 1);
        Position leftDown = new Position(row + 1, col - 1);
        Position down = new Position(row + 1, col);
        Position rightDown = new Position(row + 1, col + 1); 
        List<Position> list = new ArrayList<>();
        list.add(leftUp);
        list.add(up);
        list.add(rightUp);
        list.add(left);
        list.add(right);
        list.add(leftDown);
        list.add(down);
        list.add(rightDown);
        return list.stream().filter(p -> p.getRow() >= 0 && p.getCol() >= 0 && p.getRow() < limit && p.getCol() < limit).collect(Collectors.toList());
    }
}