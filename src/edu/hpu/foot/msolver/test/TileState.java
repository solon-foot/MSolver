package edu.hpu.foot.msolver.test;

 final class TileState {

     static final int T_0 = 0;
     static final int T_1 = 1;
     static final int T_2 = 2;
     static final int T_3 = 3;
     static final int T_4 = 4;
     static final int T_5 = 5;
     static final int T_6 = 6;
     static final int T_7 = 7;
     static final int T_8 = 8;

     static final int T_NONE = -1;//not open;
     static final int T_OUTSIDE = -2;
     static final int T_MIME = -3;
     static final int T_FLAG = -4;
     static final int T_FLAG_ERROR = -5;
     static final int T_MIME_CLICK = -6;
     static final int T_ERROR = -10;

     static String toString(int t) {
        if (t >= 0) {
            return "　①②③④⑤⑥⑦⑧".charAt(t) + "";
        }
        switch (t) {
            case TileState.T_NONE:
                return "█";//"▓";
            case TileState.T_MIME:
                return "※";//"✸";
            case TileState.T_FLAG:
                return "🚩";
            case TileState.T_ERROR:
                return "×";
            case TileState.T_MIME_CLICK:
                return "❤";//"❅";
        }
        return "+";
    }
}
