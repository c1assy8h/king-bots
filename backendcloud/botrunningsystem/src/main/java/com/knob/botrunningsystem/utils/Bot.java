package com.knob.botrunningsystem.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Bot implements java.util.function.Supplier<Integer>{ //让AI稍微智能那么一点，之前是nextMove return 1就只能一直往下走；现在是往非墙的位置走
    static class Cell {
        public int x, y;
        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

    }
    private boolean check_tail_increasing(int step) { //检查当前回合 蛇的长度是否会变长
        if(step <= 10) return true;
        return step % 3 == 1;
    }
    public List<Cell> getCells(int sx, int sy, String steps) { //
        List<Cell> res = new ArrayList<Cell>();

        int[] dx = {-1, 0, 1, 0}, dy = {0, -1, 0, 1};
        int x = sx, y = sy;
        int step = 0;
        res.add(new Cell(x,y));
        for(int i = 0; i < steps.length(); i++) {
            int d = steps.charAt(i) - '0';
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x,y));
            if(!check_tail_increasing(++ step)) {
                res.remove(0);
            }
        }
        return res;
    }

    public Integer nextMove(String input) {
        String[] strs = input.split("#");
        int[][] map = new int[13][14];
        for(int i = 0, k = 0; i < 13; i++){
            for(int j = 0; j < 14; j++){
                if(strs[0].charAt(k) == '1'){
                    map[i][j] = 1;
                }
            }
        }

        int aSx = Integer.parseInt(strs[1]), aSy = Integer.parseInt(strs[2]);
        int bSx = Integer.parseInt(strs[4]), bSy = Integer.parseInt(strs[5]);

        List<Cell> aCells = getCells(aSx, aSy, strs[3]);
        List<Cell> bCells = getCells(bSx, bSy, strs[6]);

        for(Cell c : aCells) map[c.x][c.y] = 1;
        for(Cell c : bCells) map[c.x][c.y] = 1;

        int[] dx = {-1, 0, 1, 0}, dy = {0, -1, 0, 1};
        for(int i = 0; i < 4; i++){
            int x = aCells.get(aCells.size()-1).x + dx[i], y = aCells.get(aCells.size()-1).y + dy[i];
            if(x >= 0 && x < 13 && y >= 0 && y < 14 && map[x][y] == 0){ //枚举上下左右四个方向，往非墙的位置走
                return i;
            }
        }
        return 0;
    }

    @Override
    public Integer get() {
        File file = new File("input.txt");
        try {
            Scanner sc = new Scanner(file);
            return nextMove(sc.next());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
