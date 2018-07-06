package edu.hpu.foot.msolver.solver;

import java.util.List;

public interface MSolver {
    void setNoFlag(boolean noFlag);

    List<ClickResult> solve(int[][] board);
}
