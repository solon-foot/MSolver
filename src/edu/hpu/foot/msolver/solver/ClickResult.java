package edu.hpu.foot.msolver.solver;

import edu.hpu.foot.msolver.solver.impl.MSolverImpl;

import java.util.Objects;

public class ClickResult {
    public final int x, y;
    public final int t;

    public static final int T_CLICK = 0;
    public static final int T_CHRON = 1;
    public static final int T_FLAG = 2;

    public ClickResult(int x, int y, int t) {
        this.x = x;
        this.y = y;
        this.t = t;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClickResult that = (ClickResult) o;
        return x == that.x &&
                y == that.y &&
                t == that.t;
    }

    @Override
    public int hashCode() {

        return Objects.hash(x, y, t);
    }

    @Override
    public String toString() {
        return "ClickResult{" +
                "x=" + x +
                ", y=" + y +
                ", t=" + t +
                '}';
    }
}
